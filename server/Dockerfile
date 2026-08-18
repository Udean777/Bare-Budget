# Stage 1: Build
FROM golang:1.24-alpine AS builder

WORKDIR /app

RUN apk add --no-cache git

COPY go.mod go.sum ./
RUN go mod download

COPY . .

RUN CGO_ENABLED=0 GOOS=linux go build -ldflags="-w -s" -o barebudget-api ./cmd/api

# Stage 2: Minimal Runtime Image
FROM alpine:latest

WORKDIR /app

RUN apk --no-cache add ca-certificates tzdata

COPY --from=builder /app/barebudget-api .
COPY .env.example .env

EXPOSE 8080

CMD ["./barebudget-api"]
