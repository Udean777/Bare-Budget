package main

import (
	"log"

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
	})

	// Middlewares
	app.Use(logger.New())
	app.Use(recover.New())
	app.Use(cors.New(cors.Config{
		AllowOrigins: "*",
		AllowHeaders: "Origin, Content-Type, Accept, Authorization, X-User-Email",
		AllowMethods: "GET, POST, PUT, PATCH, DELETE, OPTIONS",
	}))

	// Health Check
	app.Get("/health", func(c *fiber.Ctx) error {
		return c.JSON(fiber.Map{
			"status": "ok",
			"app":    "Bare Budget API",
		})
	})

	// API v1 Router
	api := app.Group("/api/v1")
	api.Use(middleware.AuthMiddleware())

	// Auth / User Sync
	api.Post("/auth/sync", h.SyncUser)

	// Dashboard & Sisa Napas
	api.Get("/dashboard/summary", h.GetDashboardSummary)
	api.Post("/budget", h.SetBudget)

	// Transactions
	api.Get("/transactions", h.GetTransactions)
	api.Post("/transactions", h.CreateTransaction)
	api.Delete("/transactions/:id", h.DeleteTransaction)

	// PayLater Tracker
	api.Get("/paylaters", h.GetPayLaters)
	api.Post("/paylaters", h.CreatePayLater)
	api.Patch("/paylaters/:id/status", h.UpdatePayLaterStatus)
	api.Delete("/paylaters/:id", h.DeletePayLater)

	log.Printf("Bare Budget server starting on port %s in %s mode...", cfg.Port, cfg.Environment)
	log.Fatal(app.Listen(":" + cfg.Port))
}
