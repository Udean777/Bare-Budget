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

// Wallet Repo
func (r *Repository) CreateWallet(w *models.Wallet) error {
	return r.db.Create(w).Error
}

func (r *Repository) GetWalletsByUserID(userID string) ([]models.Wallet, error) {
	var list []models.Wallet
	err := r.db.Where("user_id = ?", userID).Order("created_at asc").Find(&list).Error
	return list, err
}

func (r *Repository) UpdateWallet(w *models.Wallet) error {
	return r.db.Save(w).Error
}

func (r *Repository) DeleteWallet(userID string, id uuid.UUID) error {
	return r.db.Where("id = ? AND user_id = ?", id, userID).Delete(&models.Wallet{}).Error
}

// Transaction Repo
func (r *Repository) CreateTransaction(t *models.Transaction) error {
	return r.db.Transaction(func(tx *gorm.DB) error {
		if err := tx.Create(t).Error; err != nil {
			return err
		}
		
		if t.WalletID != nil && *t.WalletID != "" {
			var wallet models.Wallet
			if err := tx.First(&wallet, "id = ?", *t.WalletID).Error; err != nil {
				return err
			}
			
			if t.Type == models.TypeIncome {
				wallet.Balance += t.Amount
			} else if t.Type == models.TypeExpense || t.Type == "" {
				wallet.Balance -= t.Amount
			}
			
			if err := tx.Save(&wallet).Error; err != nil {
				return err
			}
		}
		return nil
	})
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

	query.Count(&total)

	err := query.Order("date desc, created_at desc").Limit(limit).Offset(offset).Find(&list).Error
	return list, total, err
}

func (r *Repository) DeleteTransaction(userID string, id uuid.UUID) error {
	return r.db.Transaction(func(tx *gorm.DB) error {
		var t models.Transaction
		if err := tx.Where("id = ? AND user_id = ?", id, userID).First(&t).Error; err != nil {
			return err
		}
		
		if t.WalletID != nil && *t.WalletID != "" {
			var wallet models.Wallet
			if err := tx.First(&wallet, "id = ?", *t.WalletID).Error; err == nil {
				if t.Type == models.TypeIncome {
					wallet.Balance -= t.Amount
				} else if t.Type == models.TypeExpense || t.Type == "" {
					wallet.Balance += t.Amount
				}
				if err := tx.Save(&wallet).Error; err != nil {
					return err
				}
			}
		}
		
		return tx.Delete(&t).Error
	})
}

func (r *Repository) GetMonthlySpent(userID string, startOfMonth, endOfMonth time.Time) (int64, error) {
	var total int64
	err := r.db.Model(&models.Transaction{}).
		Where("user_id = ? AND date >= ? AND date <= ? AND (type = ? OR type IS NULL OR type = '')", userID, startOfMonth, endOfMonth, models.TypeExpense).
		Select("COALESCE(SUM(amount), 0)").
		Scan(&total).Error
	return total, err
}
// DueBill Repo
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

// Data Migration (Guest -> Authenticated User)
func (r *Repository) MigrateGuestData(guestUserID, targetUserID string) error {
	if guestUserID == "" || targetUserID == "" || guestUserID == targetUserID {
		return nil
	}

	return r.db.Transaction(func(tx *gorm.DB) error {
		// 1. Migrate Transactions
		if err := tx.Model(&models.Transaction{}).
			Where("user_id = ?", guestUserID).
			Update("user_id", targetUserID).Error; err != nil {
			return err
		}

		// 2. Migrate Due Bills
		if err := tx.Model(&models.DueBill{}).
			Where("user_id = ?", guestUserID).
			Update("user_id", targetUserID).Error; err != nil {
			return err
		}

		// 3. Migrate Budgets (Update existing or rename user_id)
		var guestBudgets []models.Budget
		if err := tx.Where("user_id = ?", guestUserID).Find(&guestBudgets).Error; err != nil {
			return err
		}

		for _, gb := range guestBudgets {
			var targetBudget models.Budget
			err := tx.Where("user_id = ? AND month_year = ?", targetUserID, gb.MonthYear).First(&targetBudget).Error
			if err != nil {
				if err == gorm.ErrRecordNotFound {
					// Simply reassign user_id
					if err := tx.Model(&models.Budget{}).Where("id = ?", gb.ID).Update("user_id", targetUserID).Error; err != nil {
						return err
					}
				} else {
					return err
				}
			} else {
				// Target already has a budget for this month, delete guest duplicate or keep target
				tx.Where("id = ?", gb.ID).Delete(&models.Budget{})
			}
		}

		// 4. Migrate Goals
		if err := tx.Model(&models.Goal{}).
			Where("user_id = ?", guestUserID).
			Update("user_id", targetUserID).Error; err != nil {
			return err
		}

		return nil
	})
}

// Goal Repo
func (r *Repository) CreateGoal(g *models.Goal) error {
	return r.db.Create(g).Error
}

func (r *Repository) GetGoalsByUserID(userID string) ([]models.Goal, error) {
	var list []models.Goal
	err := r.db.Where("user_id = ?", userID).Order("created_at desc").Find(&list).Error
	return list, err
}

func (r *Repository) GetGoalByID(userID string, id uuid.UUID) (*models.Goal, error) {
	var g models.Goal
	err := r.db.Where("id = ? AND user_id = ?", id, userID).First(&g).Error
	if err != nil {
		return nil, err
	}
	return &g, nil
}

func (r *Repository) UpdateGoalAmount(userID string, id uuid.UUID, addedAmount int64) error {
	return r.db.Model(&models.Goal{}).
		Where("id = ? AND user_id = ?", id, userID).
		Update("current_amount", gorm.Expr("current_amount + ?", addedAmount)).Error
}

func (r *Repository) DeleteGoal(userID string, id uuid.UUID) error {
	return r.db.Where("id = ? AND user_id = ?", id, userID).Delete(&models.Goal{}).Error
}
