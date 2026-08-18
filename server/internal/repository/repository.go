package repository

import (
	"time"

	"github.com/google/uuid"
	"github.com/ssajudn/barebudget-server/internal/models"
	"gorm.io/gorm"
)

type Repository struct {
	db *gorm.DB
}

func NewRepository(db *gorm.DB) *Repository {
	return &Repository{db: db}
}

// User Repo
func (r *Repository) UpsertUser(user *models.User) error {
	return r.db.Save(user).Error
}

func (r *Repository) GetUserByID(id string) (*models.User, error) {
	var user models.User
	err := r.db.First(&user, "id = ?", id).Error
	return &user, err
}

// Transaction Repo
func (r *Repository) CreateTransaction(t *models.Transaction) error {
	return r.db.Create(t).Error
}

func (r *Repository) GetTransactionsByUserID(userID string, startDate, endDate time.Time, category string, limit, offset int) ([]models.Transaction, int64, error) {
	var list []models.Transaction
	var total int64

	query := r.db.Model(&models.Transaction{}).Where("user_id = ?", userID)

	if !startDate.IsZero() && !endDate.IsZero() {
		query = query.Where("date >= ? AND date <= ?", startDate, endDate)
	}
	if category != "" {
		query = query.Where("category = ?", category)
	}

	err := query.Count(&total).Error
	if err != nil {
		return nil, 0, err
	}

	err = query.Order("date desc").Limit(limit).Offset(offset).Find(&list).Error
	return list, total, err
}

func (r *Repository) DeleteTransaction(userID string, id uuid.UUID) error {
	return r.db.Where("id = ? AND user_id = ?", id, userID).Delete(&models.Transaction{}).Error
}

func (r *Repository) GetMonthlySpent(userID string, startOfMonth, endOfMonth time.Time) (int64, error) {
	var total int64
	err := r.db.Model(&models.Transaction{}).
		Where("user_id = ? AND date >= ? AND date <= ?", userID, startOfMonth, endOfMonth).
		Select("COALESCE(SUM(amount), 0)").
		Scan(&total).Error
	return total, err
}

type CategorySummary struct {
	Category models.TransactionCategory `json:"category"`
	Total    int64                      `json:"total"`
	Count    int64                      `json:"count"`
}

func (r *Repository) GetMonthlyCategoryBreakdown(userID string, startOfMonth, endOfMonth time.Time) ([]CategorySummary, error) {
	var result []CategorySummary
	err := r.db.Model(&models.Transaction{}).
		Select("category, COALESCE(SUM(amount), 0) as total, COUNT(id) as count").
		Where("user_id = ? AND date >= ? AND date <= ?", userID, startOfMonth, endOfMonth).
		Group("category").
		Order("total desc").
		Scan(&result).Error
	return result, err
}

// DueBill Repo
func (r *Repository) CreateDueBill(d *models.DueBill) error {
	return r.db.Create(d).Error
}

func (r *Repository) GetDueBillsByUserID(userID string, status string) ([]models.DueBill, error) {
	var list []models.DueBill
	query := r.db.Where("user_id = ?", userID)
	if status != "" {
		query = query.Where("status = ?", status)
	}
	err := query.Order("due_date asc").Find(&list).Error
	return list, err
}

func (r *Repository) UpdateDueBillStatus(userID string, id uuid.UUID, status models.DueBillStatus) error {
	return r.db.Model(&models.DueBill{}).Where("id = ? AND user_id = ?", id, userID).Update("status", status).Error
}

func (r *Repository) DeleteDueBill(userID string, id uuid.UUID) error {
	return r.db.Where("id = ? AND user_id = ?", id, userID).Delete(&models.DueBill{}).Error
}

// Budget Repo
func (r *Repository) UpsertBudget(b *models.Budget) error {
	var existing models.Budget
	err := r.db.Where("user_id = ? AND month_year = ?", b.UserID, b.MonthYear).First(&existing).Error
	if err != nil {
		if err == gorm.ErrRecordNotFound {
			return r.db.Create(b).Error
		}
		return err
	}
	return r.db.Model(&existing).Update("monthly_limit", b.MonthlyLimit).Error
}

func (r *Repository) GetBudget(userID string, monthYear string) (*models.Budget, error) {
	var b models.Budget
	err := r.db.Where("user_id = ? AND month_year = ?", userID, monthYear).First(&b).Error
	if err != nil {
		return nil, err
	}
	return &b, nil
}
