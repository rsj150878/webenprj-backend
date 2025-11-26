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

## 🚀 Quick Setup Guide

### Prerequisites
- **Docker Desktop** installed and running
- **Java 21** (included in VS Code Java Extension Pack)
- **Git** for cloning the repository

### 🐳 Start the Application

```bash
# 1. Clone the repository
git clone https://github.com/rsj150878/webenprj-backend
cd webenprj-backend

# 2. Start database services ( / Have Docker running)
docker-compose up -d mysql

# 3. Wait for MySQL to be ready (about 30 seconds)
# You can check with: docker-compose logs mysql

# 4. Start Spring Boot application
./mvnw spring-boot:run
```

### 🌐 Access the Application

- **API Endpoints**: http://localhost:8081
- **API Documentation (Swagger)**: http://localhost:8081/swagger-ui/index.html
- **phpMyAdmin** (Database UI): http://localhost:8080

---

## 🔧 Troubleshooting

### Database Connection Issues
```bash
# Check if MySQL is running
docker-compose ps

# View MySQL logs
docker-compose logs mysql

# Restart database
docker-compose restart mysql
```
