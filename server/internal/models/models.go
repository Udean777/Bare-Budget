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
	CategoryFood          TransactionCategory = "FOOD"          // GoFood, Grab, Dine-in
	CategoryTransport     TransactionCategory = "TRANSPORT"     // Gojek, Grab, Bensin, KRL
	CategoryBills         TransactionCategory = "BILLS"         // Pulsa, Listrik, WiFi
	CategoryShopping      TransactionCategory = "SHOPPING"      // Belanja, Supermarket
	CategoryEntertainment TransactionCategory = "ENTERTAINMENT" // Bioskop, Game, Netflix
	CategorySocial        TransactionCategory = "SOCIAL"        // Arisan, Kondangan
	CategoryOther         TransactionCategory = "OTHER"
)

type Transaction struct {
	ID         uuid.UUID           `gorm:"type:uuid;primaryKey" json:"id"`
	UserID     string              `gorm:"type:varchar(128);index;not null" json:"user_id"`
	Amount     int64               `gorm:"not null" json:"amount"` // in IDR (rupiah)
	Category   TransactionCategory `gorm:"type:varchar(50);not null" json:"category"`
	Merchant   string              `gorm:"type:varchar(255)" json:"merchant"`
	Date       time.Time           `gorm:"not null;index" json:"date"`
	Notes      string              `gorm:"type:text" json:"notes"`
	ReceiptURL string              `gorm:"type:text" json:"receipt_url,omitempty"`
	CreatedAt  time.Time           `json:"created_at"`
	UpdatedAt  time.Time           `json:"updated_at"`
	DeletedAt  gorm.DeletedAt      `gorm:"index" json:"-"`
}

func (t *Transaction) BeforeCreate(tx *gorm.DB) (err error) {
	if t.ID == uuid.Nil {
		t.ID = uuid.New()
	}
	return
}

type PayLaterStatus string

const (
	PayLaterUnpaid PayLaterStatus = "UNPAID"
	PayLaterPaid   PayLaterStatus = "PAID"
)

type PayLater struct {
	ID           uuid.UUID      `gorm:"type:uuid;primaryKey" json:"id"`
	UserID       string         `gorm:"type:varchar(128);index;not null" json:"user_id"`
	PlatformName string         `gorm:"type:varchar(100);not null" json:"platform_name"` // e.g. "Shopee PayLater", "GoPay Later", "Kredivo"
	TotalBill    int64          `gorm:"not null" json:"total_bill"`
	DueDate      time.Time      `gorm:"not null" json:"due_date"`
	Status       PayLaterStatus `gorm:"type:varchar(20);default:'UNPAID'" json:"status"`
	Notes        string         `gorm:"type:text" json:"notes"`
	CreatedAt    time.Time      `json:"created_at"`
	UpdatedAt    time.Time      `json:"updated_at"`
	DeletedAt    gorm.DeletedAt `gorm:"index" json:"-"`
}

func (p *PayLater) BeforeCreate(tx *gorm.DB) (err error) {
	if p.ID == uuid.Nil {
		p.ID = uuid.New()
	}
	return
}

type Budget struct {
	ID           uuid.UUID      `gorm:"type:uuid;primaryKey" json:"id"`
	UserID       string         `gorm:"type:varchar(128);index;not null" json:"user_id"`
	MonthlyLimit int64          `gorm:"not null" json:"monthly_limit"`
	MonthYear    string         `gorm:"type:varchar(7);not null;index" json:"month_year"` // Format: "YYYY-MM", e.g. "2026-08"
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
