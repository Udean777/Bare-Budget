package database

import (
	"log"

	"github.com/ssajudn/barebudget-server/internal/models"
	"gorm.io/driver/postgres"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
)

var DB *gorm.DB

func Connect(databaseURL string) (*gorm.DB, error) {
	db, err := gorm.Open(postgres.Open(databaseURL), &gorm.Config{
		Logger: logger.Default.LogMode(logger.Info),
	})
	if err != nil {
		return nil, err
	}

	log.Println("Database connection established")

	// Auto-migration
	err = db.AutoMigrate(
		&models.User{},
		&models.Wallet{},
		&models.Transaction{},
		&models.DueBill{},
		&models.Budget{},
		&models.Goal{},
		&models.IdempotencyKey{},
	)
	if err != nil {
		log.Printf("AutoMigrate error: %v", err)
		return nil, err
	}

	log.Println("Database migration completed")
	DB = db
	return db, nil
}
