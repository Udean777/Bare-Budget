package config

import (
	"log"
	"os"
	"strings"

	"github.com/joho/godotenv"
)

type Config struct {
	Port               string
	DatabaseURL        string
	Environment        string
	CORSAllowedOrigins []string
	IsProduction       bool
}

func LoadConfig() *Config {
	// 1. Try loading environment-specific .env (e.g. .env.production, .env.development)
	env := os.Getenv("ENV")
	if env == "" {
		env = "development"
	}

	envFile := ".env." + env
	if err := godotenv.Load(envFile); err != nil {
		// Fallback to standard .env
		if err := godotenv.Load(".env"); err != nil {
			log.Println("No .env file found, using system environment variables")
		}
	}

	port := os.Getenv("PORT")
	if port == "" {
		port = "8080"
	}

	dbURL := os.Getenv("DATABASE_URL")
	if dbURL == "" {
		dbURL = "host=localhost user=postgres password=postgres dbname=barebudget port=5432 sslmode=disable"
	}

	corsRaw := os.Getenv("CORS_ALLOWED_ORIGINS")
	var origins []string
	if corsRaw == "" || corsRaw == "*" {
		origins = []string{"*"}
	} else {
		for _, o := range strings.Split(corsRaw, ",") {
			trimmed := strings.TrimSpace(o)
			if trimmed != "" {
				origins = append(origins, trimmed)
			}
		}
	}

	isProd := strings.EqualFold(env, "production")

	return &Config{
		Port:               port,
		DatabaseURL:        dbURL,
		Environment:        env,
		CORSAllowedOrigins: origins,
		IsProduction:       isProd,
	}
}
