package middleware

import (
	"strings"

	"github.com/gofiber/fiber/v2"
	"github.com/ssajudn/barebudget-server/internal/auth"
)

// AuthMiddleware verifies Firebase ID token and stores verified UID.
// ponytail: dev fallback allowed only when ENV != production and no Firebase project configured
func AuthMiddleware() fiber.Handler {
	verifier := auth.NewVerifierFromEnv()
	return AuthMiddlewareWithVerifier(verifier)
}

func AuthMiddlewareWithVerifier(verifier auth.TokenVerifier) fiber.Handler {
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
		uid, err := verifier.VerifyIDToken(token)
		if err != nil || uid == "" {
			return c.Status(fiber.StatusUnauthorized).JSON(fiber.Map{
				"error": "invalid or unverified token",
			})
		}

		c.Locals("userID", uid)
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
