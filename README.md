# Motivise – Backend

Spring Boot REST API for the Motivise study micro-blogging platform. Built for the WEBEN course at FH Technikum Wien.

## Prerequisites

- Java 21
- Docker Desktop (for standard mode) or none (for quick mode)

## First-Time Setup

```bash
# Copy env template and generate JWT secret
cp .env.example .env

# Generate secret (macOS/Linux)
openssl rand -base64 32

# Generate secret (Windows PowerShell)
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))

# Paste the generated value into .env as JWT_SECRET_KEY
```

## Running the App

**Standard mode** (MySQL + Docker):
```bash
start.cmd          # or: start.cmd clean
```

**Quick mode** (H2 in-memory, no Docker):
```bash
quick-start.cmd    # or: quick-start.cmd clean
```

**Run tests**:
```bash
test.cmd           # opens coverage report in browser
```

## URLs

| Mode     | Service      | URL                                         |
| -------- | ------------ | ------------------------------------------- |
| Both     | API          | http://localhost:8081                       |
| Both     | Swagger      | http://localhost:8081/swagger-ui/index.html |
| Standard | phpMyAdmin   | http://localhost:8080 (root/rootpassword)   |
| Standard | MinIO        | http://localhost:9001                       |
| Quick    | H2 Console   | http://localhost:8081/h2-console (sa/empty) |

## Test Credentials

- anna.schmidt@example.com / `Password123!`
- max.meier@example.com / `Password123!`
- admin@motivise.app / `AdminPass456!`

## Mode Comparison

| Feature         | Standard (`start.cmd`)   | Quick (`quick-start.cmd`) |
| --------------- | ------------------------ | ------------------------- |
| Database        | MySQL (persistent)       | H2 (in-memory)            |
| Docker required | Yes                      | No                        |
| Startup time    | ~30s                     | ~10s                      |
| File storage    | MinIO                    | Mock                      |
| Data persists   | Yes                      | No                        |

## API Overview

See Swagger for full docs. Key endpoints:

- `POST /auth/login` – get JWT token
- `POST /users` – register (public)
- `GET /users/me` – current user
- `GET /posts` – list posts
- `POST /posts` – create post
- `POST /medias` – upload file (jpg, png, gif, pdf, max 25MB)
- `GET /medias/{id}` – download file (public, for `<img>` tags)

## Project Structure

```
src/main/java/at/fhtw/webenprjbackend/
├── controller/     # REST endpoints
├── service/        # Business logic
├── repository/     # Spring Data JPA
├── entity/         # User, Post, Media
├── dto/            # Request/response objects
├── security/       # JWT, Spring Security
├── filestorage/    # MinIO + mock storage
└── config/         # Spring config
```

## Troubleshooting

**Port 8081 in use**: `netstat -ano | findstr :8081`

**Docker issues**: `docker-compose down -v && start.cmd`

**Broken environment**: `clean-install.cmd`

**Check Java version**: `java -version` (needs 21)
