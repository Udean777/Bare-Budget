package handler

import (
	"strconv"
	"time"

	"github.com/gofiber/fiber/v2"
	"github.com/google/uuid"
	"github.com/ssajudn/barebudget-server/internal/middleware"
	"github.com/ssajudn/barebudget-server/internal/models"
	"github.com/ssajudn/barebudget-server/internal/service"
)

type Handler struct {
	svc *service.Service
}

func NewHandler(svc *service.Service) *Handler {
	return &Handler{svc: svc}
}

// User Sync Handler
func (h *Handler) SyncUser(c *fiber.Ctx) error {
	userID := middleware.GetUserID(c)
	if userID == "" {
		return c.Status(fiber.StatusUnauthorized).JSON(fiber.Map{"error": "unauthorized"})
	}

	var req struct {
		Email    string `json:"email"`
		Name     string `json:"name"`
		FCMToken string `json:"fcm_token"`
	}
	if err := c.BodyParser(&req); err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{"error": "invalid request body"})
	}

	user := &models.User{
		ID:       userID,
		Email:    req.Email,
		Name:     req.Name,
		FCMToken: req.FCMToken,
	}

	if err := h.svc.SyncUser(user); err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{"error": err.Error()})
	}

	return c.JSON(fiber.Map{"status": "success", "user": user})
}

func (h *Handler) MigrateGuestData(c *fiber.Ctx) error {
	targetUserID := middleware.GetUserID(c)
	if targetUserID == "" {
		return c.Status(fiber.StatusUnauthorized).JSON(fiber.Map{"error": "unauthorized"})
	}

	var req struct {
		GuestUserID string `json:"guest_user_id"`
	}
	if err := c.BodyParser(&req); err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{"error": "invalid request body"})
	}

	if req.GuestUserID == "" {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{"error": "guest_user_id is required"})
	}

	if err := h.svc.MigrateGuestData(req.GuestUserID, targetUserID); err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{"error": err.Error()})
	}

	return c.JSON(fiber.Map{
		"status":         "success",
		"message":        "guest data migrated successfully",
		"target_user_id": targetUserID,
	})
}

// Transaction Handlers
func (h *Handler) CreateTransaction(c *fiber.Ctx) error {
	userID := middleware.GetUserID(c)
	if userID == "" {
		return c.Status(fiber.StatusUnauthorized).JSON(fiber.Map{"error": "unauthorized"})
	}

	var req struct {
		Amount     int64                      `json:"amount"`
		Category   models.TransactionCategory `json:"category"`
		Merchant   string                     `json:"merchant"`
		Date       string                     `json:"date"` // RFC3339 or "2006-01-02"
		Notes      string                     `json:"notes"`
		ReceiptURL string                     `json:"receipt_url"`
	}
	if err := c.BodyParser(&req); err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{"error": "invalid request body"})
	}

	txDate := time.Now()
	if req.Date != "" {
		if t, err := time.Parse(time.RFC3339, req.Date); err == nil {
			txDate = t
		} else if t, err := time.Parse("2006-01-02", req.Date); err == nil {
			txDate = t
		}
	}

	tx := &models.Transaction{
		UserID:     userID,
		Amount:     req.Amount,
		Category:   req.Category,
		Merchant:   req.Merchant,
		Date:       txDate,
		Notes:      req.Notes,
		ReceiptURL: req.ReceiptURL,
	}

	if err := h.svc.CreateTransaction(tx); err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{"error": err.Error()})
	}

	return c.Status(fiber.StatusCreated).JSON(tx)
}

func (h *Handler) GetTransactions(c *fiber.Ctx) error {
	userID := middleware.GetUserID(c)
	if userID == "" {
		return c.Status(fiber.StatusUnauthorized).JSON(fiber.Map{"error": "unauthorized"})
	}

	category := c.Query("category")
	page, _ := strconv.Atoi(c.Query("page", "1"))
	limit, _ := strconv.Atoi(c.Query("limit", "20"))

	var startDate, endDate time.Time
	if s := c.Query("start_date"); s != "" {
		startDate, _ = time.Parse("2006-01-02", s)
	}
	if e := c.Query("end_date"); e != "" {
		endDate, _ = time.Parse("2006-01-02", e)
	}

	list, total, err := h.svc.GetTransactions(userID, startDate, endDate, category, page, limit)
	if err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{"error": err.Error()})
	}

	return c.JSON(fiber.Map{
		"data":  list,
		"total": total,
		"page":  page,
		"limit": limit,
	})
}

func (h *Handler) DeleteTransaction(c *fiber.Ctx) error {
	userID := middleware.GetUserID(c)
	idStr := c.Params("id")
	id, err := uuid.Parse(idStr)
	if err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{"error": "invalid transaction id"})
	}

	if err := h.svc.DeleteTransaction(userID, id); err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{"error": err.Error()})
	}

	return c.JSON(fiber.Map{"status": "deleted"})
}

// DueBill Handlers
func (h *Handler) CreateDueBill(c *fiber.Ctx) error {
	userID := middleware.GetUserID(c)
	var req struct {
		ProviderName string `json:"provider_name"`
		TotalAmount  int64  `json:"total_amount"`
		DueDate      string `json:"due_date"` // "2006-01-02"
		Notes        string `json:"notes"`
	}
	if err := c.BodyParser(&req); err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{"error": "invalid request body"})
	}

	dueDate, err := time.Parse("2006-01-02", req.DueDate)
	if err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{"error": "due_date format must be YYYY-MM-DD"})
	}

	d := &models.DueBill{
		UserID:       userID,
		ProviderName: req.ProviderName,
		TotalAmount:  req.TotalAmount,
		DueDate:      dueDate,
		Status:       models.DueBillUnpaid,
		Notes:        req.Notes,
	}

	if err := h.svc.CreateDueBill(d); err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{"error": err.Error()})
	}

	return c.Status(fiber.StatusCreated).JSON(d)
}

func (h *Handler) GetDueBills(c *fiber.Ctx) error {
	userID := middleware.GetUserID(c)
	status := c.Query("status")

	list, err := h.svc.GetDueBills(userID, status)
	if err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{"error": err.Error()})
	}

	return c.JSON(fiber.Map{"data": list})
}

func (h *Handler) UpdateDueBillStatus(c *fiber.Ctx) error {
	userID := middleware.GetUserID(c)
	idStr := c.Params("id")
	id, err := uuid.Parse(idStr)
	if err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{"error": "invalid bill id"})
	}

	var req struct {
		Status models.DueBillStatus `json:"status"`
	}
	if err := c.BodyParser(&req); err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{"error": "invalid request body"})
	}

	if err := h.svc.UpdateDueBillStatus(userID, id, req.Status); err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{"error": err.Error()})
	}

	return c.JSON(fiber.Map{"status": "updated"})
}

func (h *Handler) DeleteDueBill(c *fiber.Ctx) error {
	userID := middleware.GetUserID(c)
	idStr := c.Params("id")
	id, err := uuid.Parse(idStr)
	if err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{"error": "invalid bill id"})
	}

	if err := h.svc.DeleteDueBill(userID, id); err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{"error": err.Error()})
	}

	return c.JSON(fiber.Map{"status": "deleted"})
}

// Budget & Dashboard Handlers
func (h *Handler) SetBudget(c *fiber.Ctx) error {
	userID := middleware.GetUserID(c)
	var req struct {
		MonthlyLimit int64  `json:"monthly_limit"`
		MonthYear    string `json:"month_year"` // YYYY-MM
	}
	if err := c.BodyParser(&req); err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{"error": "invalid request body"})
	}

	if req.MonthYear == "" {
		req.MonthYear = time.Now().Format("2006-01")
	}

	if err := h.svc.SetBudget(userID, req.MonthlyLimit, req.MonthYear); err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{"error": err.Error()})
	}

	return c.JSON(fiber.Map{"status": "budget set successfully"})
}

func (h *Handler) GetDashboardSummary(c *fiber.Ctx) error {
	userID := middleware.GetUserID(c)
	summary, err := h.svc.GetDashboardSummary(userID, time.Now())
	if err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{"error": err.Error()})
	}

	return c.JSON(summary)
}
