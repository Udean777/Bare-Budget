package handler

import (
	"strconv"
	"strings"
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
		Type       models.TransactionType     `json:"type"`
		WalletID   *string                    `json:"wallet_id"`
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
		Type:       req.Type,
		WalletID:   req.WalletID,
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
		ProviderName      string                   `json:"provider_name"`
		ProviderIconURL   string                   `json:"provider_icon_url"`
		TotalAmount       int64                    `json:"total_amount"`
		DueDate           string                   `json:"due_date"` // "2006-01-02"
		IsRecurring       bool                     `json:"is_recurring"`
		RecurringInterval models.RecurringInterval `json:"recurring_interval"`
		Notes             string                   `json:"notes"`
	}
	if err := c.BodyParser(&req); err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{"error": "invalid request body"})
	}

	dueDate, err := time.Parse("2006-01-02", req.DueDate)
	if err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{"error": "due_date format must be YYYY-MM-DD"})
	}

	d := &models.DueBill{
		UserID:            userID,
		ProviderName:      req.ProviderName,
		ProviderIconURL:   req.ProviderIconURL,
		TotalAmount:       req.TotalAmount,
		DueDate:           dueDate,
		Status:            models.DueBillUnpaid,
		IsRecurring:       req.IsRecurring,
		RecurringInterval: req.RecurringInterval,
		Notes:             req.Notes,
	}

	if err := h.svc.CreateDueBill(d); err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{"error": err.Error()})
	}

	return c.Status(fiber.StatusCreated).JSON(d)
}

func (h *Handler) UpdateDueBill(c *fiber.Ctx) error {
	userID := middleware.GetUserID(c)
	idStr := c.Params("id")
	id, err := uuid.Parse(idStr)
	if err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{"error": "invalid bill id"})
	}

	var req struct {
		ProviderName      string                   `json:"provider_name"`
		ProviderIconURL   string                   `json:"provider_icon_url"`
		TotalAmount       int64                    `json:"total_amount"`
		DueDate           string                   `json:"due_date"` // "2006-01-02"
		IsRecurring       *bool                    `json:"is_recurring"`
		RecurringInterval models.RecurringInterval `json:"recurring_interval"`
		Notes             string                   `json:"notes"`
	}
	if err := c.BodyParser(&req); err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{"error": "invalid request body"})
	}

	fields := make(map[string]interface{})
	if req.ProviderName != "" {
		fields["provider_name"] = req.ProviderName
	}
	fields["provider_icon_url"] = req.ProviderIconURL
	if req.TotalAmount > 0 {
		fields["total_amount"] = req.TotalAmount
	}
	if req.DueDate != "" {
		dueDate, err := time.Parse("2006-01-02", req.DueDate)
		if err != nil {
			return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{"error": "due_date format must be YYYY-MM-DD"})
		}
		fields["due_date"] = dueDate
	}
	if req.IsRecurring != nil {
		fields["is_recurring"] = *req.IsRecurring
	}
	fields["recurring_interval"] = req.RecurringInterval
	fields["notes"] = req.Notes

	if len(fields) == 0 {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{"error": "no fields to update"})
	}

	if err := h.svc.UpdateDueBill(userID, id, fields); err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{"error": err.Error()})
	}

	return c.JSON(fiber.Map{"status": "updated"})
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
		Status   models.DueBillStatus `json:"status"`
		WalletID *string              `json:"wallet_id"`
	}
	if err := c.BodyParser(&req); err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{"error": "invalid request body"})
	}

	if err := h.svc.UpdateDueBillStatus(userID, id, req.Status, req.WalletID); err != nil {
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
		if strings.Contains(err.Error(), "already set") {
			return c.Status(fiber.StatusConflict).JSON(fiber.Map{"error": err.Error()})
		}
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

// Goals Handlers
func (h *Handler) CreateGoal(c *fiber.Ctx) error {
	userID := middleware.GetUserID(c)
	var req struct {
		Name         string `json:"name"`
		TargetAmount int64  `json:"target_amount"`
		TargetDate   string `json:"target_date"` // YYYY-MM-DD
		ColorHex     string `json:"color_hex"`
		Notes        string `json:"notes"`
	}
	if err := c.BodyParser(&req); err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{"error": "invalid request body"})
	}

	if req.Name == "" || req.TargetAmount <= 0 {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{"error": "name and target_amount are required"})
	}

	targetDate, _ := time.Parse("2006-01-02", req.TargetDate)
	if req.ColorHex == "" {
		req.ColorHex = "#4E73DF"
	}

	goal := models.Goal{
		UserID:       userID,
		Name:         req.Name,
		TargetAmount: req.TargetAmount,
		TargetDate:   targetDate,
		ColorHex:     req.ColorHex,
		Notes:        req.Notes,
	}

	if err := h.svc.CreateGoal(&goal); err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{"error": err.Error()})
	}

	return c.Status(fiber.StatusCreated).JSON(goal)
}

func (h *Handler) GetGoals(c *fiber.Ctx) error {
	userID := middleware.GetUserID(c)
	goals, err := h.svc.GetGoals(userID)
	if err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{"error": err.Error()})
	}

	return c.JSON(fiber.Map{"data": goals})
}

func (h *Handler) DepositToGoal(c *fiber.Ctx) error {
	userID := middleware.GetUserID(c)
	idStr := c.Params("id")
	id, err := uuid.Parse(idStr)
	if err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{"error": "invalid goal id"})
	}

	var req struct {
		Amount   int64  `json:"amount"` // Can be positive (deposit) or negative (withdraw)
		WalletID string `json:"wallet_id"`
	}
	if err := c.BodyParser(&req); err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{"error": "invalid request body"})
	}

	if req.Amount == 0 {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{"error": "amount cannot be zero"})
	}
	if req.WalletID == "" {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{"error": "wallet_id is required"})
	}

	if err := h.svc.DepositToGoal(userID, id, req.WalletID, req.Amount); err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{"error": err.Error()})
	}

	return c.JSON(fiber.Map{"status": "success", "message": "deposit updated successfully"})
}

func (h *Handler) UpdateGoal(c *fiber.Ctx) error {
	userID := middleware.GetUserID(c)
	idStr := c.Params("id")
	id, err := uuid.Parse(idStr)
	if err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{"error": "invalid goal id"})
	}

	var req struct {
		Name         string `json:"name"`
		TargetAmount int64  `json:"target_amount"`
		TargetDate   string `json:"target_date"`
		ColorHex     string `json:"color_hex"`
		Notes        string `json:"notes"`
	}
	if err := c.BodyParser(&req); err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{"error": "invalid request body"})
	}

	fields := make(map[string]interface{})
	if req.Name != "" {
		fields["name"] = req.Name
	}
	if req.TargetAmount > 0 {
		fields["target_amount"] = req.TargetAmount
	}
	if req.TargetDate != "" {
		if t, err := time.Parse("2006-01-02", req.TargetDate); err == nil {
			fields["target_date"] = t
		} else if t, err := time.Parse(time.RFC3339, req.TargetDate); err == nil {
			fields["target_date"] = t
		}
	}
	if req.ColorHex != "" {
		fields["color_hex"] = req.ColorHex
	}
	if req.Notes != "" {
		fields["notes"] = req.Notes
	}

	if len(fields) == 0 {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{"error": "no fields to update"})
	}

	if err := h.svc.UpdateGoal(userID, id, fields); err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{"error": err.Error()})
	}

	return c.JSON(fiber.Map{"status": "updated"})
}

func (h *Handler) DeleteGoal(c *fiber.Ctx) error {
	userID := middleware.GetUserID(c)
	idStr := c.Params("id")
	id, err := uuid.Parse(idStr)
	if err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{"error": "invalid goal id"})
	}

	if err := h.svc.DeleteGoal(userID, id); err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{"error": err.Error()})
	}

	return c.JSON(fiber.Map{"status": "deleted"})
}

// Wallet Handlers
func (h *Handler) GetWallets(c *fiber.Ctx) error {
	userID := middleware.GetUserID(c)
	wallets, err := h.svc.GetWallets(userID)
	if err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{"error": err.Error()})
	}
	return c.JSON(wallets)
}

func (h *Handler) CreateWallet(c *fiber.Ctx) error {
	userID := middleware.GetUserID(c)
	var wallet models.Wallet
	if err := c.BodyParser(&wallet); err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{"error": "invalid payload"})
	}
	wallet.UserID = userID
	if err := h.svc.CreateWallet(&wallet); err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{"error": err.Error()})
	}
	return c.JSON(wallet)
}

func (h *Handler) UpdateWallet(c *fiber.Ctx) error {
	userID := middleware.GetUserID(c)
	idParam := c.Params("id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{"error": "invalid uuid"})
	}

	var payload models.Wallet
	if err := c.BodyParser(&payload); err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{"error": "invalid payload"})
	}

	wallets, _ := h.svc.GetWallets(userID)
	var existing *models.Wallet
	for _, w := range wallets {
		if w.ID == id {
			existing = &w
			break
		}
	}
	if existing == nil {
		return c.Status(fiber.StatusNotFound).JSON(fiber.Map{"error": "wallet not found"})
	}

	existing.Name = payload.Name
	existing.ColorHex = payload.ColorHex
	existing.IconName = payload.IconName

	if err := h.svc.UpdateWallet(existing); err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{"error": err.Error()})
	}
	return c.JSON(existing)
}

func (h *Handler) DeleteWallet(c *fiber.Ctx) error {
	userID := middleware.GetUserID(c)
	idParam := c.Params("id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{"error": "invalid uuid"})
	}

	if err := h.svc.DeleteWallet(userID, id); err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{"error": err.Error()})
	}
	return c.JSON(fiber.Map{"success": true})
}

// Analytics Handlers
func (h *Handler) GetCashflowAnalytics(c *fiber.Ctx) error {
	userID := middleware.GetUserID(c)
	data, err := h.svc.GetCashflowAnalytics(userID)
	if err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{"error": err.Error()})
	}
	return c.JSON(fiber.Map{"data": data})
}

func (h *Handler) GetNetWorthAnalytics(c *fiber.Ctx) error {
	userID := middleware.GetUserID(c)
	data, err := h.svc.GetNetWorthAnalytics(userID)
	if err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{"error": err.Error()})
	}
	return c.JSON(fiber.Map{"data": data})
}
