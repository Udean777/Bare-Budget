package service

import (
	"fmt"
	"time"

	"github.com/google/uuid"
	"github.com/ssajudn/barebudget-server/internal/models"
	"github.com/ssajudn/barebudget-server/internal/repository"
)

type Service struct {
	repo *repository.Repository
}

func NewService(repo *repository.Repository) *Service {
	return &Service{repo: repo}
}

// User Services
func (s *Service) SyncUser(user *models.User) error {
	return s.repo.UpsertUser(user)
}

func (s *Service) MigrateGuestData(guestUserID, targetUserID string) error {
	return s.repo.MigrateGuestData(guestUserID, targetUserID)
}

// Transaction Services
// Wallet Services
func (s *Service) CreateWallet(w *models.Wallet) error {
	return s.repo.CreateWallet(w)
}

func (s *Service) GetWallets(userID string) ([]models.Wallet, error) {
	return s.repo.GetWalletsByUserID(userID)
}

func (s *Service) UpdateWallet(w *models.Wallet) error {
	return s.repo.UpdateWallet(w)
}

func (s *Service) DeleteWallet(userID string, id uuid.UUID) error {
	return s.repo.DeleteWallet(userID, id)
}
func (s *Service) CreateTransaction(t *models.Transaction) error {
	return s.repo.CreateTransaction(t)
}

func (s *Service) GetTransactions(userID string, startDate, endDate time.Time, category string, page, limit int) ([]models.Transaction, int64, error) {
	if limit <= 0 {
		limit = 20
	}
	if page <= 0 {
		page = 1
	}
	offset := (page - 1) * limit
	return s.repo.GetTransactionsByUserID(userID, startDate, endDate, category, limit, offset)
}

func (s *Service) DeleteTransaction(userID string, id uuid.UUID) error {
	return s.repo.DeleteTransaction(userID, id)
}

// DueBill Services
func (s *Service) CreateDueBill(d *models.DueBill) error {
	return s.repo.CreateDueBill(d)
}

func (s *Service) GetDueBills(userID string, status string) ([]models.DueBill, error) {
	return s.repo.GetDueBillsByUserID(userID, status)
}

func (s *Service) UpdateDueBill(userID string, id uuid.UUID, fields map[string]interface{}) error {
	return s.repo.UpdateDueBill(userID, id, fields)
}

func (s *Service) UpdateDueBillStatus(userID string, id uuid.UUID, status models.DueBillStatus, walletID *string) error {
	return s.repo.UpdateDueBillStatus(userID, id, status, walletID)
}

func (s *Service) DeleteDueBill(userID string, id uuid.UUID) error {
	return s.repo.DeleteDueBill(userID, id)
}

// Goal Services
func (s *Service) CreateGoal(g *models.Goal) error {
	return s.repo.CreateGoal(g)
}

func (s *Service) GetGoals(userID string) ([]models.Goal, error) {
	return s.repo.GetGoalsByUserID(userID)
}

func (s *Service) DepositToGoal(userID string, id uuid.UUID, walletID string, amount int64) error {
	return s.repo.DepositToGoal(userID, id, walletID, amount)
}

func (s *Service) UpdateGoal(userID string, id uuid.UUID, fields map[string]interface{}) error {
	return s.repo.UpdateGoal(userID, id, fields)
}

func (s *Service) DeleteGoal(userID string, id uuid.UUID) error {
	return s.repo.DeleteGoal(userID, id)
}

// Budget & Dashboard Runway Services
type DashboardSummary struct {
	MonthlyBudget      int64                        `json:"monthly_budget"`
	TotalSpent         int64                        `json:"total_spent"`
	RemainingBudget    int64                        `json:"remaining_budget"`
	DaysPassed         int                          `json:"days_passed"`
	DaysInMonth        int                          `json:"days_in_month"`
	AverageDailySpend  int64                        `json:"average_daily_spend"`
	EstimatedDeathDay  int                          `json:"estimated_death_day"`
	RunwayMessage      string                       `json:"runway_message"`
	TopCategories      []repository.CategorySummary `json:"top_categories"`
	UnpaidDueBillsSum  int64                        `json:"unpaid_due_bills_sum"`
	NetWorth           int64                        `json:"net_worth"`
	RecentTransactions []models.Transaction         `json:"recent_transactions"`
}

func (s *Service) SetBudget(userID string, limit int64, monthYear string) error {
	existing, err := s.repo.GetBudget(userID, monthYear)
	if err == nil && existing != nil {
		return fmt.Errorf("budget already set for %s: only one update per month allowed", monthYear)
	}
	b := &models.Budget{
		UserID:       userID,
		MonthlyLimit: limit,
		MonthYear:    monthYear,
	}
	return s.repo.UpsertBudget(b)
}

func (s *Service) GetDashboardSummary(userID string, now time.Time) (*DashboardSummary, error) {
	monthYear := now.Format("2006-01")
	// Start from beginning of current month (00:00:00) to the end of the month (23:59:59)
	startOfMonth := time.Date(now.Year(), now.Month(), 1, 0, 0, 0, 0, time.UTC)
	endOfMonth := startOfMonth.AddDate(0, 1, 0).Add(-time.Nanosecond)
	daysInMonth := endOfMonth.Day()
	daysPassed := now.Day()

	// 1. Get Monthly Budget
	budget, _ := s.repo.GetBudget(userID, monthYear)
	var monthlyBudget int64 = 0
	if budget != nil {
		monthlyBudget = budget.MonthlyLimit
	}

	// 2. Get Total Spent for entire month up to endOfMonth
	totalSpent, err := s.repo.GetMonthlySpent(userID, startOfMonth, endOfMonth)
	if err != nil {
		return nil, err
	}

	// 3. Calculate Financial Runway
	remainingBudget := monthlyBudget - totalSpent
	var avgDailySpend int64 = 0
	if daysPassed > 0 {
		avgDailySpend = totalSpent / int64(daysPassed)
	}

	var estimatedDeathDay int = daysInMonth
	var runwayMsg string

	if monthlyBudget <= 0 {
		runwayMsg = "Monthly budget is not set yet. Set your budget to track your financial runway!"
	} else if remainingBudget <= 0 {
		estimatedDeathDay = daysPassed
		runwayMsg = fmt.Sprintf("Runway exhausted! Your budget was exceeded on day %d.", daysPassed)
	} else if avgDailySpend > 0 {
		daysLeft := int(remainingBudget / avgDailySpend)
		projectedDay := daysPassed + daysLeft
		if projectedDay < daysInMonth {
			estimatedDeathDay = projectedDay
			runwayMsg = fmt.Sprintf("At your current spending rate (Rp %s/day), your budget will run out on day %d!", formatCurrency(avgDailySpend), projectedDay)
		} else {
			estimatedDeathDay = daysInMonth
			runwayMsg = "Your financial runway is safe until the end of the month. Keep it up!"
		}
	} else {
		runwayMsg = "No expenses recorded this month yet. Your budget is untouched!"
	}

	// 4. Get Category Breakdown
	categories, err := s.repo.GetMonthlyCategoryBreakdown(userID, startOfMonth, endOfMonth)
	if err != nil {
		return nil, err
	}

	// 5. Get Unpaid DueBills Sum
	bills, err := s.repo.GetDueBillsByUserID(userID, string(models.DueBillUnpaid))
	var unpaidSum int64 = 0
	if err == nil {
		for _, b := range bills {
			unpaidSum += b.TotalAmount
		}
	}

	// 6. Get Recent 5 Transactions
	recentTxs, _, _ := s.repo.GetTransactionsByUserID(userID, time.Time{}, time.Time{}, "", 5, 0)

	// 7. Get Net Worth (Sum of Wallets)
	wallets, _ := s.repo.GetWalletsByUserID(userID)
	var netWorth int64 = 0
	for _, w := range wallets {
		netWorth += w.Balance
	}

	return &DashboardSummary{
		MonthlyBudget:      monthlyBudget,
		TotalSpent:         totalSpent,
		RemainingBudget:    remainingBudget,
		DaysPassed:         daysPassed,
		DaysInMonth:        daysInMonth,
		AverageDailySpend:  avgDailySpend,
		EstimatedDeathDay:  estimatedDeathDay,
		RunwayMessage:      runwayMsg,
		TopCategories:      categories,
		UnpaidDueBillsSum:  unpaidSum,
		NetWorth:           netWorth,
		RecentTransactions: recentTxs,
	}, nil

}

func formatCurrency(amount int64) string {
	str := fmt.Sprintf("%d", amount)
	n := len(str)
	if n <= 3 {
		return str
	}
	var res string
	for i, c := range str {
		if (n-i)%3 == 0 && i != 0 {
			res += "."
		}
		res += string(c)
	}
	return res
}

// Analytics Services
func (s *Service) GetCashflowAnalytics(userID string) ([]repository.CashflowDataPoint, error) {
	return s.repo.GetMonthlyCashflow(userID, 6)
}

func (s *Service) GetNetWorthAnalytics(userID string) ([]repository.NetWorthDataPoint, error) {
	return s.repo.GetMonthlyNetWorthTrend(userID, 6)
}
