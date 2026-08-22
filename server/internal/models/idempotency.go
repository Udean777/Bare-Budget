package models

import "time"

type IdempotencyKey struct {
	UserID     string    `gorm:"type:varchar(128);primaryKey" json:"user_id"`
	Key        string    `gorm:"type:varchar(128);primaryKey" json:"key"`
	Response   string    `gorm:"type:text" json:"response"`
	CreatedAt  time.Time `json:"created_at"`
}
