@echo off
echo.
echo ===============================
echo   STANDARD DEVELOPMENT MODE
echo   (MySQL + Docker)
echo ===============================
echo.

if "%1"=="clean" (
    echo Cleaning and rebuilding project...
    echo.
    call mvnw.cmd clean compile -DskipTests
    if errorlevel 1 (
        echo.
        echo [ERROR] Build failed!
        pause
        exit /b 1
    )
    echo.
    echo [SUCCESS] Clean build completed!
    echo.
)

if "%1"=="info" (
    goto :info
)

if "%1"=="help" (
    goto :help
)

echo Starting Docker services (MySQL, phpMyAdmin, MinIO)...
docker-compose up -d mysql phpmyadmin minio

if errorlevel 1 (
    echo.
    echo [ERROR] Docker Compose failed to start!
    echo [INFO] Make sure Docker Desktop is running.
    echo [INFO] Try: docker-compose up -d
    pause
    exit /b 1
)

echo.
echo Waiting for MySQL to be healthy (this may take 10-15 seconds)...
echo.
timeout /t 12 /nobreak > nul

echo Starting Spring Boot application with MySQL...
echo.
echo Features enabled:
echo - MySQL 8.0 database (persistent)
echo - MinIO file storage
echo - phpMyAdmin (database management)
echo - Flyway migrations
echo - API documentation (Swagger)
echo.
echo TIP: Run 'start.cmd info' to see URLs
echo.
echo Clearing any previous Spring profile settings...
set SPRING_PROFILES_ACTIVE=

REM Set JWT secret for development (generate your own for production!)
if not defined JWT_SECRET_KEY (
    set JWT_SECRET_KEY=dev-only-jwt-secret-key-min-32-chars-do-not-use-in-prod
)

call mvnw.cmd spring-boot:run -DskipTests
goto :end

:info
echo.
echo ===============================
echo   STANDARD DEVELOPMENT URLS
echo ===============================
echo Server:           http://localhost:8081
echo API Docs:         http://localhost:8081/swagger-ui/index.html
echo phpMyAdmin:       http://localhost:8080
echo MinIO Console:    http://localhost:9001
echo   Username:       minioadmin
echo   Password:       minioadmin
echo Actuator Health:  http://localhost:8081/actuator/health
echo.
echo ===============================
echo   DATABASE CONNECTION
echo ===============================
echo Database:         webbackend
echo Username:         webbackenduser
echo Password:         webbackendpassword
echo JDBC URL:         jdbc:mysql://localhost:3306/webbackend
echo.
echo ===============================
echo   DEVELOPMENT NOTES
echo ===============================
echo - Persistent MySQL database
echo - Production-like environment
echo - Data survives restarts
echo - Requires Docker Desktop running
echo.
echo For quick H2-based development without Docker:
echo   quick-start.cmd
echo.
goto :end

:help
echo.
echo Standard Development Commands:
echo.
echo   start.cmd       - Start with Docker/MySQL (default)
echo   start.cmd clean - Clean build then start
echo   start.cmd info  - Show URLs and database info
echo   start.cmd help  - Show this help
echo.
echo Alternative modes:
echo   quick-start.cmd - Fast H2 development (no Docker)
echo   test.cmd        - Run tests only
echo   clean-install.cmd - Nuclear recovery option
echo.

:end
