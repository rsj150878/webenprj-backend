@echo off

if "%1"=="clean" (
    echo Cleaning...
    call mvnw.cmd clean compile -DskipTests
    if errorlevel 1 exit /b 1
)

if "%1"=="info" (
    echo.
    echo Server:        http://localhost:8081
    echo Swagger:       http://localhost:8081/swagger-ui/index.html
    echo phpMyAdmin:    http://localhost:8080 (root/rootpassword)
    echo MinIO:         http://localhost:9001 (minioadmin/minioadmin)
    echo DB:            jdbc:mysql://localhost:3306/webbackend (webbackenduser/webbackendpassword)
    exit /b 0
)

echo Starting Docker services...
docker-compose up -d mysql phpmyadmin minio
if errorlevel 1 (
    echo Docker failed - is Docker Desktop running?
    exit /b 1
)

echo Waiting for MySQL...
timeout /t 12 /nobreak > nul

set SPRING_PROFILES_ACTIVE=
if not defined JWT_SECRET_KEY set JWT_SECRET_KEY=dev-only-jwt-secret-key-min-32-chars-do-not-use-in-prod

echo Starting Spring Boot (MySQL)...
call mvnw.cmd spring-boot:run -DskipTests
