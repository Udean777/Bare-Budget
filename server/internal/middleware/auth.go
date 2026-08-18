package middleware

import (
	"strings"

	"github.com/gofiber/fiber/v2"
)

// AuthMiddleware extracts Firebase UID or fallback token for local dev
func AuthMiddleware() fiber.Handler {
	return func(c *fiber.Ctx) error {
		authHeader := c.Get("Authorization")
		if authHeader == "" {
			return c.Status(fiber.StatusUnauthorized).JSON(fiber.Map{
				"error": "missing authorization header",
			})
		}

		parts := strings.Split(authHeader, " ")
		if len(parts) != 2 || parts[0] != "Bearer" {
			return c.Status(fiber.StatusUnauthorized).JSON(fiber.Map{
				"error": "invalid authorization header format",
			})
		}

		token := parts[1]

		// Store user info in Fiber Locals
		c.Locals("userID", token)
		c.Locals("userEmail", c.Get("X-User-Email"))

		return c.Next()
	}
}

func GetUserID(c *fiber.Ctx) string {
	if val, ok := c.Locals("userID").(string); ok {
		return val
	}
	return ""
}
