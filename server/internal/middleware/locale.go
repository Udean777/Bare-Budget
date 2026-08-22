package middleware

import (
	"strings"

	"github.com/gofiber/fiber/v2"
)

// LocaleMiddleware parses the incoming Accept-Language header (e.g. from mobile clients)
// and extracts a normalized language code ("en" or "id") into request locals context.
func LocaleMiddleware() fiber.Handler {
	return func(c *fiber.Ctx) error {
		lang := parseAcceptLanguage(c.Get("Accept-Language"))
		c.Locals("lang", lang)
		return c.Next()
	}
}

func parseAcceptLanguage(header string) string {
	if header == "" {
		return "en"
	}
	// e.g. "id, en;q=0.9" or "en-US,en;q=0.9,id;q=0.8"
	parts := strings.Split(header, ",")
	for _, p := range parts {
		// strip q-value
		tag := strings.TrimSpace(strings.Split(p, ";")[0])
		tag = strings.ToLower(tag)
		// primary subtag: "id" from "id-ID" or "en-us"
		if idx := strings.Index(tag, "-"); idx != -1 {
			tag = tag[:idx]
		}
		// normalize legacy "in" -> "id"
		if tag == "in" {
			tag = "id"
		}
		if tag == "id" || tag == "en" {
			return tag
		}
	}
	return "en"
}

// GetLang returns "id" or "en" from context, default "en".
func GetLang(c *fiber.Ctx) string {
	if v, ok := c.Locals("lang").(string); ok && v != "" {
		return v
	}
	return "en"
}
