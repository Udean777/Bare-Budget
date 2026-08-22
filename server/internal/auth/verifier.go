package auth

import (
	"encoding/base64"
	"encoding/json"
	"errors"
	"os"
	"strings"
)

// TokenVerifier defines the contract for validating authentication tokens
// and extracting the authenticated user's unique identifier.
// Abstracting this via an interface enables unit testing and modular verifier backends.
type TokenVerifier interface {
	VerifyIDToken(token string) (uid string, err error)
}

type FirebaseVerifier struct {
	projectID string
}

func NewVerifierFromEnv() TokenVerifier {
	pid := os.Getenv("FIREBASE_PROJECT_ID")
	if pid == "" {
		pid = os.Getenv("FIREBASE_PROJECTID")
	}
	return &FirebaseVerifier{projectID: pid}
}

func (v *FirebaseVerifier) VerifyIDToken(token string) (string, error) {
	if token == "" {
		return "", errors.New("empty token")
	}
	// Dev fallback only when not in production and no projectID configured
	if token == "dev-user-123" {
		if os.Getenv("ENV") == "production" || v.projectID != "" {
			return "", errors.New("dev token not allowed in production")
		}
		return token, nil
	}
	// If Firebase project not configured, fall back to unverified JWT parsing with warning
	// In production with projectID set, this should be replaced by Firebase Admin SDK verification
	if v.projectID == "" {
		uid, err := parseJWTUid(token)
		if err != nil {
			return "", err
		}
		return uid, nil
	}
	// TODO: integrate firebase.google.com/go/v4/auth when service account is available
	// For now, parse UID from token payload as placeholder
	uid, err := parseJWTUid(token)
	if err != nil {
		return "", err
	}
	return uid, nil
}

func parseJWTUid(token string) (string, error) {
	parts := strings.Split(token, ".")
	if len(parts) != 3 {
		// Not a JWT — treat as raw UID only in non-prod dev mode
		if os.Getenv("ENV") != "production" {
			return token, nil
		}
		return "", errors.New("invalid token format")
	}
	payload, err := base64.RawURLEncoding.DecodeString(parts[1])
	if err != nil {
		return "", err
	}
	var claims map[string]interface{}
	if err := json.Unmarshal(payload, &claims); err != nil {
		return "", err
	}
	if uid, ok := claims["user_id"].(string); ok && uid != "" {
		return uid, nil
	}
	if uid, ok := claims["uid"].(string); ok && uid != "" {
		return uid, nil
	}
	if sub, ok := claims["sub"].(string); ok && sub != "" {
		return sub, nil
	}
	return "", errors.New("uid not found in token")
}
