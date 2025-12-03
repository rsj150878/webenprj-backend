# 🎓 Motivise – Study Micro-Blogging Platform (Backend)

Motivise is a small social platform where students can share short learning updates, daily progress, motivation, and study tips.
It's like a learning diary that helps students stay motivated and connect with others who are also studying.

---

## 🌟 Main Idea
Students can post short "micro-blog" updates about what they studied today.
Each post can include:
- text about what they did
- images of notes or whiteboards
- short PDF files (like cheat sheets)
- study tags (e.g., #math, #marketing)

Other students can like or comment on posts.
Optional "study streaks" show how many days in a row a user has been learning.

---

## 🚀 Super Simple Setup

### Prerequisites
- **Java 21** (included in VS Code Java Extension Pack)
- **Docker Desktop** (for production mode only)
- **Git** for cloning the repository

### 🔐 Security Setup (IMPORTANT - First Time Setup)

**Before running the application**, you must configure environment variables for security:

```bash
# 1. Copy the environment template
cp .env.example .env

# 2. Generate a strong JWT secret (on macOS/Linux)
openssl rand -base64 32

# 3. Edit .env file and replace JWT_SECRET_KEY with the generated value
# Example: JWT_SECRET_KEY=AhCJ4lNWxhr+bha4JNNtECAXvE41JLADJ5AqydXhBew=
```

**⚠️ NEVER commit .env file to version control!** (Already in .gitignore)

The `.env` file contains:
- `JWT_SECRET_KEY` - Secure secret for JWT token signing (REQUIRED)
- `JWT_EXPIRATION_MS` - Token expiration time in milliseconds
- `DB_PASSWORD` - Database password for production mode
- Other configuration values

**Note:** Development mode (`docker-free`) has safe defaults, but production requires all secrets to be set explicitly.

### 🛠️ Development Mode (Recommended for coding)

**Super fast startup, no Docker needed!**

```bash
# Clone and start in dev mode
git clone https://github.com/rsj150878/webenprj-backend
cd webenprj-backend
./dev.cmd
```

### 🚀 Production Mode (Real database testing)

**Requires Docker for MySQL database**

```bash
# Clone and start in production mode
git clone https://github.com/rsj150878/webenprj-backend
cd webenprj-backend
./prod.cmd
```

### 🧪 Testing Commands

**Comprehensive test suite with coverage reporting**

```bash
# Run all tests
./test.cmd

# Run tests with coverage report
./test.cmd coverage

# Run specific test class
./test.cmd specific PostServiceTest

# Quick test run (no coverage)
./test.cmd quick

# See all test options
./test.cmd help
```

### 🧹 Clean Build (When changing dependencies)

```bash
# Development with clean build
./dev.cmd clean

# Production with clean build
./prod.cmd clean
```

---

## 🌐 Access Points

| Mode | API | Swagger | Database UI | Test Users |
|------|-----|---------|-------------|------------|
| **Dev** | http://localhost:8081 | [Swagger UI](http://localhost:8081/swagger-ui/index.html) | [H2 Console](http://localhost:8081/h2-console) | ✅ Auto-loaded |
| **Prod** | http://localhost:8081 | ❌ Disabled | [phpMyAdmin](http://localhost:8080) | ✅ From migrations |

### 🔑 Test Login Credentials (Development)
- **📧 anna.schmidt@example.com** / Password: `Password123!`
- **📧 max.meier@example.com** / Password: `Password123!`  
- **📧 admin@motivise.app** / Password: `AdminPass456!`

### 🗄️ H2 Console Access (Development Only)
- **URL**: http://localhost:8081/h2-console
- **JDBC URL**: `jdbc:h2:mem:motivise_dev`
- **Username**: `sa`
- **Password**: (leave empty)
- **Note**: ⚡ In-memory only - data is reset on each restart

### 📊 Test Coverage Reports
After running `./test.cmd coverage`:
- **HTML Report**: Open `target/site/jacoco/index.html` in browser
- **Current Coverage**: ~21% overall, 31% service layer
- **Test Reports**: View `target/surefire-reports/` for detailed results

---

## 🔧 Development vs Production

| Feature | Development (`./dev.cmd`) | Production (`./prod.cmd`) |
|---------|---------------------------|---------------------------|
| **Startup Time** | ⚡ ~10 seconds | 🐢 ~45 seconds |
| **Database** | H2 (in-memory) | MySQL (persistent) |
| **Docker Required** | ❌ No | ✅ Yes |
| **Test Data** | ✅ Auto-loaded | ✅ From migrations |
| **Database UI** | H2 Console | phpMyAdmin |
| **API Documentation** | ✅ Swagger enabled | ❌ Disabled (security) |
| **Data Persistence** | ❌ Lost on restart | ✅ Survives restarts |
| **SQL Logging** | ✅ Detailed | ❌ Minimal |

---

## 🛠️ Command Reference

### Quick Start Commands
```bash
# Development mode (fast, no Docker)
./dev.cmd

# Get development URLs and test data
./dev.cmd info

# Production mode (MySQL database)
./prod.cmd

# Clean build (after dependency changes)
./dev.cmd clean
./prod.cmd clean
```

### Testing Commands
```bash
# Run all tests
./test.cmd

# Run tests with coverage report
./test.cmd coverage

# Run only unit tests (DTO + Service layer)
./test.cmd unit

# Run only integration tests
./test.cmd integration

# Quick test run (no coverage)
./test.cmd quick

# Run specific test class
./test.cmd specific PostServiceTest

# Show all test options
./test.cmd help
```

### Development Helper Commands
```bash
# Start server
./dev.cmd

# Show development URLs and credentials (run in another terminal)
./dev.cmd info

# Show all development options
./dev.cmd help
```

### When to Use Clean Build
🚨 **Use `clean` when:**
- Adding new dependencies to `pom.xml`
- Updating Spring Boot or Java version
- After pulling Git changes that include `pom.xml`
- Getting "class not found" errors

⚡ **Normal startup when:**
- Just changing Java code
- Modifying properties files
- Regular daily development

### Manual Commands (if needed)
```bash
# Development mode (manual)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Production mode (manual)
docker-compose up -d mysql
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

# Manual test run with coverage
./mvnw clean test jacoco:report
```

---

## 🔧 Troubleshooting

### Development Issues
```bash
# If app won't start
./dev.cmd clean  # Try clean build

# If H2 console doesn't work
# Make sure you're using: ./dev.cmd (not prod.cmd)

# Check application logs for errors in terminal
```

### Testing Issues
```bash
# If tests fail to run
./test.cmd quick  # Try quick test run

# If coverage report doesn't generate
./mvnw clean test jacoco:report  # Manual coverage run

# View detailed test results
# Open: target/surefire-reports/index.html
```

### Production Issues
```bash
# Check if Docker is running
docker ps

# Check MySQL status
docker-compose ps mysql

# View MySQL logs  
docker-compose logs mysql

# Restart MySQL
docker-compose restart mysql

# Clean build if having issues
./prod.cmd clean
```

### Database Connection Issues
```bash
# Check if MySQL container is running
docker-compose ps

# Restart all services
docker-compose restart

# View database logs
docker-compose logs mysql
```

---

## 🎯 Quick Workflow Tips

### For Daily Development:
1. `./dev.cmd` - Start coding immediately
2. `./dev.cmd info` - Get URLs and test credentials (in another terminal)
3. Make changes to your code
4. `./test.cmd quick` - Run tests while developing
5. Restart with `Ctrl+C` then `./dev.cmd` again

### For Testing and Quality Assurance:
1. `./test.cmd coverage` - Generate full test coverage report
2. Open `target/site/jacoco/index.html` to view coverage
3. `./test.cmd specific TestClassName` - Debug specific failing tests
4. `./test.cmd unit` - Quick unit test runs during development

### For Testing Production Features:
1. `./prod.cmd` - Test with real database
2. Check phpMyAdmin for data persistence
3. Verify API endpoints work without Swagger

### After Major Changes:
1. `./dev.cmd clean` - Clean development build
2. `./test.cmd coverage` - Verify tests still pass
3. `./prod.cmd clean` - Clean production build
4. Test both modes work correctly

---
