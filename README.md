# 🎓 Motivise – Study Micro-Blogging Platform (Backend)

Motivise is a small social platform where students can share short learning updates, daily progress, motivation, and study tips.
It's like a learning diary that helps students stay motivated and connect with others who are also studying.

---

## 🌟 Main Idea

Students can post short "micro-blog" updates about what they studied today.
Each post can include:
- Text about what they did
- Images of notes or whiteboards
- Short PDF files (like cheat sheets)
- Study tags (e.g., #math, #marketing)

Other students can like or comment on posts.
Optional "study streaks" show how many days in a row a user has been learning.

---

## 🚀 Getting Started

### Prerequisites
- **Java 21** (included in VS Code Java Extension Pack)
- **Docker Desktop** (for standard MySQL development)
- **Git** for cloning the repository

### 🔐 Security Setup (IMPORTANT - First Time Setup)

**Before running the application**, you must configure environment variables for security:

```bash
# 1. Copy the environment template
cp .env.example .env

# 2. Generate a strong JWT secret (on macOS/Linux)
openssl rand -base64 32

# On Windows (PowerShell):
# [Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))

# 3. Edit .env file and replace JWT_SECRET_KEY with the generated value
# Example: JWT_SECRET_KEY=AhCJ4lNWxhr+bha4JNNtECAXvE41JLADJ5AqydXhBew=
```

**⚠️ NEVER commit .env file to version control!** (Already in .gitignore)

The `.env` file contains:
- `JWT_SECRET_KEY` - Secure secret for JWT token signing (REQUIRED)
- `JWT_EXPIRATION_MS` - Token expiration time in milliseconds
- `DB_PASSWORD` - Database password for production mode
- Other configuration values

**Note:** Quick Start mode has safe defaults for development, but standard mode requires all secrets to be set.

### 🛠️ Standard Development (Recommended)

**Production-like environment with MySQL and Docker**

```bash
# Clone and start in standard mode
git clone https://github.com/rsj150878/webenprj-backend
cd webenprj-backend
start.cmd
```

**Features:**
- MySQL 8.0 database (persistent)
- MinIO file storage
- phpMyAdmin for database management
- Data persists across restarts
- Production-like environment

### ⚡ Quick Start Mode

**Super fast startup, no Docker needed!**

```bash
# Clone and start in quick mode
git clone https://github.com/rsj150878/webenprj-backend
cd webenprj-backend
quick-start.cmd
```

**Features:**
- H2 in-memory database
- Mock file storage
- Test data pre-loaded
- Zero Docker dependencies
- Perfect for rapid prototyping

### 🧪 Testing

**Comprehensive test suite with coverage reporting**

```bash
# Run all tests with coverage
test.cmd
```

The coverage report automatically opens in your browser at `target/site/jacoco/index.html`.

### 🧹 Clean Build (When changing dependencies)

```bash
# Standard mode with clean build
start.cmd clean

# Quick start mode with clean build
quick-start.cmd clean

# Nuclear option (if environment is broken)
clean-install.cmd
```

---

## 🌐 Access Points

### Standard Development Mode (`start.cmd`)

| Service | URL | Credentials |
|---------|-----|-------------|
| **API Server** | http://localhost:8081 | - |
| **Swagger UI** | http://localhost:8081/swagger-ui/index.html | - |
| **phpMyAdmin** | http://localhost:8080 | root / rootpassword |
| **MinIO Console** | http://localhost:9001 | minioadmin / minioadmin |

**Database Connection:**
- Host: localhost:3306
- Database: webbackend
- Username: webbackenduser
- Password: webbackendpassword

### Quick Start Mode (`quick-start.cmd`)

| Service | URL | Credentials |
|---------|-----|-------------|
| **API Server** | http://localhost:8081 | - |
| **Swagger UI** | http://localhost:8081/swagger-ui/index.html | - |
| **H2 Console** | http://localhost:8081/h2-console | sa / (empty) |

**H2 Database Connection:**
- JDBC URL: `jdbc:h2:mem:motivise_dev`
- Username: `sa`
- Password: (leave empty)
- Note: Data resets on server restart

### 🔑 Test Login Credentials

These users are available in both modes:
- **📧 anna.schmidt@example.com** / Password: `Password123!`
- **📧 max.meier@example.com** / Password: `Password123!`
- **📧 admin@motivise.app** / Password: `AdminPass456!`

---

## 🔧 Development Modes Comparison

| Feature | Standard (`start.cmd`) | Quick Start (`quick-start.cmd`) |
|---------|------------------------|----------------------------------|
| **Startup Time** | 🐢 ~30-45 seconds | ⚡ ~10 seconds |
| **Database** | MySQL 8.0 (persistent) | H2 (in-memory) |
| **Docker Required** | ✅ Yes | ❌ No |
| **Test Data** | Auto-loaded | Auto-loaded |
| **Database UI** | phpMyAdmin | H2 Console |
| **File Storage** | MinIO (real files) | Mock (no files saved) |
| **API Documentation** | ✅ Swagger enabled | ✅ Swagger enabled |
| **Data Persistence** | ✅ Survives restarts | ❌ Lost on restart |
| **SQL Logging** | ❌ Minimal | ✅ Detailed |
| **Use Case** | Production testing, data persistence | Rapid prototyping, quick testing |

---

## 🛠️ Command Reference

### Development Commands

```bash
# Start standard development (MySQL + Docker)
start.cmd              # Start server
start.cmd clean        # Clean build + start
start.cmd info         # Show URLs and database info
start.cmd help         # Show help

# Start quick development (H2, no Docker)
quick-start.cmd        # Start server
quick-start.cmd clean  # Clean build + start
quick-start.cmd info   # Show URLs and test credentials
quick-start.cmd help   # Show help
```

### Testing Commands

```bash
# Run all tests with coverage
test.cmd
```

After tests complete, the coverage report opens automatically in your browser.

**Coverage Reports:**
- **HTML Report**: `target/site/jacoco/index.html`
- **Test Results**: `target/surefire-reports/`

### Recovery Commands

```bash
# Nuclear option - complete rebuild
clean-install.cmd

# Use when:
# - Dependencies are corrupted
# - Maven cache is broken
# - Environment is completely broken
```

### Manual Commands (Advanced)

```bash
# Manual start with specific profile
./mvnw spring-boot:run -Pdocker-free    # H2 mode
./mvnw spring-boot:run                  # MySQL mode

# Manual test run
./mvnw clean test jacoco:report

# Start only Docker services
docker-compose up -d mysql phpmyadmin minio

# Stop Docker services
docker-compose down
```

---

## 🔧 Troubleshooting

### Common Issues

#### Backend won't start
```bash
# Check if port 8081 is already in use
# On Windows:
netstat -ano | findstr :8081

# Try clean build
start.cmd clean
# OR
quick-start.cmd clean

# Check Java version (must be Java 21)
java -version
```

#### Docker/MySQL issues (Standard mode)
```bash
# Check if Docker Desktop is running
docker ps

# View Docker service status
docker-compose ps

# View logs
docker-compose logs mysql

# Restart services
docker-compose restart

# Nuclear option - remove all data and restart
docker-compose down -v
start.cmd

# Alternative: Use quick-start mode (no Docker)
quick-start.cmd
```

#### Tests failing
```bash
# Try nuclear recovery
clean-install.cmd

# Then run tests again
test.cmd

# View detailed test results
# Open: target/surefire-reports/index.html
```

#### Environment is completely broken
```bash
# Nuclear recovery option
clean-install.cmd

# This will:
# - Delete all build artifacts
# - Clear Maven cache
# - Rebuild everything from scratch
# - Skip all tests during rebuild
```

---

## 🎯 Quick Workflow Tips

### For Daily Development

**Option 1: Standard Mode (Recommended)**
```bash
1. start.cmd                 # Start with MySQL/Docker
2. Make your code changes
3. Server auto-restarts on changes
4. Access Swagger: http://localhost:8081/swagger-ui/index.html
5. Use phpMyAdmin to view database
```

**Option 2: Quick Mode (Faster)**
```bash
1. quick-start.cmd           # Fast H2 startup
2. Make your code changes
3. Server auto-restarts on changes
4. Access H2 Console: http://localhost:8081/h2-console
```

### For Testing

```bash
1. Make your code changes
2. test.cmd                  # Run tests with coverage
3. Review coverage report (opens automatically)
4. Fix any failing tests
```

### After Dependency Changes

```bash
# When you modify pom.xml
start.cmd clean              # Standard mode
# OR
quick-start.cmd clean        # Quick mode
```

### When Environment is Broken

```bash
clean-install.cmd            # Nuclear recovery
```

---

## 📁 Project Structure

```
webenprj-backend/
├── src/main/java/at/fhtw/webenprjbackend/
│   ├── controller/          # REST API endpoints
│   ├── service/             # Business logic
│   ├── repository/          # Data access (Spring Data JPA)
│   ├── entity/              # JPA entities (User, Post, Media)
│   ├── dto/                 # Data Transfer Objects
│   ├── security/            # JWT auth, Spring Security
│   │   └── jwt/             # JWT token handling
│   ├── filestorage/         # File storage abstraction
│   │   └── minio/           # MinIO implementation
│   └── config/              # Spring configuration
├── src/main/resources/
│   ├── application.properties              # Default config
│   ├── application-docker-free.properties  # H2 config
│   ├── application-test-isolated.properties # Test config
│   └── db/migration/        # Flyway migrations
├── src/test/               # Test files
├── start.cmd               # Standard development (MySQL)
├── quick-start.cmd         # Quick development (H2)
├── test.cmd                # Run tests
├── clean-install.cmd       # Nuclear recovery
├── docker-compose.yml      # Docker services
├── pom.xml                 # Maven dependencies
└── README.md               # This file
```

---

## 🔑 Key Technologies

- **Spring Boot 3** - Modern Java framework
- **Spring Security** - JWT authentication
- **Spring Data JPA** - Database access with Hibernate
- **MySQL 8.0** - Production database
- **H2 Database** - Development database
- **Flyway** - Database migrations
- **MinIO** - Object storage (files/images)
- **OpenAPI/Swagger** - API documentation
- **JaCoCo** - Test coverage reporting
- **JUnit 5** - Testing framework

---

## 🚀 Next Steps

1. **Set up environment variables** - Copy `.env.example` to `.env`
2. **Choose your mode:**
   - `start.cmd` - Production-like MySQL environment
   - `quick-start.cmd` - Fast H2 prototyping
3. **Access Swagger** - http://localhost:8081/swagger-ui/index.html
4. **Test the API** - Use the test credentials provided
5. **Run tests** - Use `test.cmd` to ensure everything works

---

## 📞 Support

If you encounter issues:
1. Check the Troubleshooting section above
2. Try `clean-install.cmd` for nuclear recovery
3. Check application logs in the terminal
4. Review test results in `target/surefire-reports/`

---

## 📝 License

This project is part of a university assignment at FH Technikum Wien.
