package main

import (
	"log"
	"strings"

	"github.com/gofiber/fiber/v2"
	"github.com/gofiber/fiber/v2/middleware/cors"
	"github.com/gofiber/fiber/v2/middleware/logger"
	"github.com/gofiber/fiber/v2/middleware/recover"
	"github.com/ssajudn/barebudget-server/internal/config"
	"github.com/ssajudn/barebudget-server/internal/database"
	"github.com/ssajudn/barebudget-server/internal/handler"
	"github.com/ssajudn/barebudget-server/internal/middleware"
	"github.com/ssajudn/barebudget-server/internal/repository"
	"github.com/ssajudn/barebudget-server/internal/service"
)

func main() {
	cfg := config.LoadConfig()

	// Initialize Database
	db, err := database.Connect(cfg.DatabaseURL)
	if err != nil {
		log.Fatalf("Could not connect to database: %v", err)
	}

	repo := repository.NewRepository(db)
	svc := service.NewService(repo)
	h := handler.NewHandler(svc)

	app := fiber.New(fiber.Config{
		AppName: "Bare Budget API v1",
		// In production, disable verbose banners
		DisableStartupMessage: cfg.IsProduction,
	})

	// Middlewares
	if !cfg.IsProduction {
		app.Use(logger.New())
	}
	app.Use(recover.New())
	app.Use(cors.New(cors.Config{
		AllowOrigins: strings.Join(cfg.CORSAllowedOrigins, ", "),
		AllowHeaders: "Origin, Content-Type, Accept, Authorization, X-User-Email",
		AllowMethods: "GET, POST, PUT, PATCH, DELETE, OPTIONS",
	}))

	// Health Check
	app.Get("/health", func(c *fiber.Ctx) error {
		return c.JSON(fiber.Map{
			"status": "ok",
			"app":    "Bare Budget API",
			"env":    cfg.Environment,
		})
	})

	// API v1 Router
	api := app.Group("/api/v1")
	api.Use(middleware.AuthMiddleware())

	// Auth / User Sync & Migration
	api.Post("/auth/sync", h.SyncUser)
	api.Post("/auth/migrate-guest", h.MigrateGuestData)

	// Dashboard & Financial Runway
	api.Get("/dashboard/summary", h.GetDashboardSummary)
	api.Post("/budget", h.SetBudget)

	// Transactions
	// Wallets
	api.Get("/wallets", h.GetWallets)
	api.Post("/wallets", h.CreateWallet)
	api.Put("/wallets/:id", h.UpdateWallet)
	api.Delete("/wallets/:id", h.DeleteWallet)
	api.Get("/transactions", h.GetTransactions)
	api.Post("/transactions", h.CreateTransaction)
	api.Delete("/transactions/:id", h.DeleteTransaction)

	// Due Bills Tracker
	api.Get("/due-bills", h.GetDueBills)
	api.Post("/due-bills", h.CreateDueBill)
	api.Patch("/due-bills/:id/status", h.UpdateDueBillStatus)
	api.Delete("/due-bills/:id", h.DeleteDueBill)

	// Savings Goals Tracker
	api.Get("/goals", h.GetGoals)
	api.Post("/goals", h.CreateGoal)
	api.Post("/goals/:id/deposit", h.DepositToGoal)
	api.Delete("/goals/:id", h.DeleteGoal)

	log.Printf("Bare Budget server starting on port %s in [%s] mode...", cfg.Port, cfg.Environment)
	log.Fatal(app.Listen(":" + cfg.Port))
}
