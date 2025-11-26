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

# Production mode (MySQL database)
./prod.cmd

# Clean build (after dependency changes)
./dev.cmd clean
./prod.cmd clean
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
2. Make changes to your code
3. Restart with `Ctrl+C` then `./dev.cmd` again

### For Testing Production Features:
1. `./prod.cmd` - Test with real database
2. Check phpMyAdmin for data persistence
3. Verify API endpoints work without Swagger

### After Major Changes:
1. `./dev.cmd clean` - Clean development build
2. `./prod.cmd clean` - Clean production build
3. Test both modes work correctly

---

## 📚 What You Get

- **🛠️ Development Mode**: Instant startup, test data, easy debugging
- **🚀 Production Mode**: Real database, proper security, deployment-ready
- **🧹 Smart Building**: Clean builds when needed, fast starts when possible
- **📱 Easy Commands**: No more remembering long Maven commands
- **🔄 Profile Switching**: Seamless environment changes
