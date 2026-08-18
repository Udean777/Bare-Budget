package models

import (
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

type User struct {
	ID        string         `gorm:"primaryKey;type:varchar(128)" json:"id"` // Firebase UID
	Email     string         `gorm:"type:varchar(255);uniqueIndex;not null" json:"email"`
	Name      string         `gorm:"type:varchar(255)" json:"name"`
	FCMToken  string         `gorm:"type:text" json:"fcm_token,omitempty"`
	CreatedAt time.Time      `json:"created_at"`
	UpdatedAt time.Time      `json:"updated_at"`
	DeletedAt gorm.DeletedAt `gorm:"index" json:"-"`
}

type TransactionCategory string

const (
	CategoryFood          TransactionCategory = "FOOD"
	CategoryTransport     TransactionCategory = "TRANSPORT"
	CategoryBills         TransactionCategory = "BILLS"
	CategoryShopping      TransactionCategory = "SHOPPING"
	CategoryEntertainment TransactionCategory = "ENTERTAINMENT"
	CategorySocial        TransactionCategory = "SOCIAL"
	CategoryOther         TransactionCategory = "OTHER"
)

type Transaction struct {
	ID          uuid.UUID           `gorm:"type:uuid;primaryKey" json:"id"`
	UserID      string              `gorm:"type:varchar(128);index;not null" json:"user_id"`
	Amount      int64               `gorm:"not null" json:"amount"`
	Category    TransactionCategory `gorm:"type:varchar(50);not null" json:"category"`
	Merchant    string              `gorm:"type:varchar(255)" json:"merchant"`
	Date        time.Time           `gorm:"not null;index" json:"date"`
	Notes       string              `gorm:"type:text" json:"notes"`
	ReceiptURL  string              `gorm:"type:text" json:"receipt_url,omitempty"`
	CreatedAt   time.Time           `json:"created_at"`
	UpdatedAt   time.Time           `json:"updated_at"`
	DeletedAt   gorm.DeletedAt      `gorm:"index" json:"-"`
}

func (t *Transaction) BeforeCreate(tx *gorm.DB) (err error) {
	if t.ID == uuid.Nil {
		t.ID = uuid.New()
	}
	return
}

type DueBillStatus string

const (
	DueBillUnpaid DueBillStatus = "UNPAID"
	DueBillPaid   DueBillStatus = "PAID"
)

type RecurringInterval string

const (
	RecurringNone    RecurringInterval = "NONE"
	RecurringWeekly  RecurringInterval = "WEEKLY"
	RecurringMonthly RecurringInterval = "MONTHLY"
	RecurringYearly  RecurringInterval = "YEARLY"
)

type DueBill struct {
	ID                uuid.UUID         `gorm:"type:uuid;primaryKey" json:"id"`
	UserID            string            `gorm:"type:varchar(128);index;not null" json:"user_id"`
	ProviderName      string            `gorm:"type:varchar(100);not null" json:"provider_name"` // e.g. "Shopee", "Electricity", "Kredivo"
	TotalAmount       int64             `gorm:"not null" json:"total_amount"`
	DueDate           time.Time         `gorm:"not null" json:"due_date"`
	Status            DueBillStatus     `gorm:"type:varchar(20);default:'UNPAID'" json:"status"`
	IsRecurring       bool              `gorm:"default:false" json:"is_recurring"`
	RecurringInterval RecurringInterval `gorm:"type:varchar(20);default:'NONE'" json:"recurring_interval"`
	Notes             string            `gorm:"type:text" json:"notes"`
	CreatedAt         time.Time         `json:"created_at"`
	UpdatedAt         time.Time         `json:"updated_at"`
	DeletedAt         gorm.DeletedAt    `gorm:"index" json:"-"`
}

func (d *DueBill) BeforeCreate(tx *gorm.DB) (err error) {
	if d.ID == uuid.Nil {
		d.ID = uuid.New()
	}
	return
}

type Budget struct {
	ID           uuid.UUID      `gorm:"type:uuid;primaryKey" json:"id"`
	UserID       string         `gorm:"type:varchar(128);index;not null" json:"user_id"`
	MonthlyLimit int64          `gorm:"not null" json:"monthly_limit"`
	MonthYear    string         `gorm:"type:varchar(7);not null;index" json:"month_year"` // "YYYY-MM"
	CreatedAt    time.Time      `json:"created_at"`
	UpdatedAt    time.Time      `json:"updated_at"`
	DeletedAt    gorm.DeletedAt `gorm:"index" json:"-"`
}

func (b *Budget) BeforeCreate(tx *gorm.DB) (err error) {
	if b.ID == uuid.Nil {
		b.ID = uuid.New()
	}
	return
}

type Goal struct {
	ID           uuid.UUID      `gorm:"type:uuid;primaryKey" json:"id"`
	UserID       string         `gorm:"type:varchar(128);index;not null" json:"user_id"`
	Name         string         `gorm:"type:varchar(150);not null" json:"name"`
	TargetAmount int64          `gorm:"not null" json:"target_amount"`
	CurrentAmount int64         `gorm:"default:0" json:"current_amount"`
	TargetDate   time.Time      `json:"target_date"`
	ColorHex     string         `gorm:"type:varchar(20);default:'#4E73DF'" json:"color_hex"`
	Notes        string         `gorm:"type:text" json:"notes"`
	CreatedAt    time.Time      `json:"created_at"`
	UpdatedAt    time.Time      `json:"updated_at"`
	DeletedAt    gorm.DeletedAt `gorm:"index" json:"-"`
}

func (g *Goal) BeforeCreate(tx *gorm.DB) (err error) {
	if g.ID == uuid.Nil {
		g.ID = uuid.New()
	}
	return
}

