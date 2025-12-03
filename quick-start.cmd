@echo off
echo.
echo ===============================
echo   QUICK START MODE (H2)
echo   No Docker Required
echo ===============================
echo.
echo NOTE: This uses H2 in-memory database for rapid development.
echo For production-like environment, use 'start.cmd' instead.
echo.

if "%1"=="clean" (
    echo Cleaning and starting H2 environment...
    call mvnw.cmd clean compile -DskipTests
    echo Clean build completed!
    echo.
    pause
    goto :end
)

if "%1"=="info" (
    goto :info
)

if "%1"=="help" (
    goto :help
)

echo Starting H2 development server (no Docker dependencies)...
echo.
echo Features enabled:
echo - H2 in-memory database
echo - Mock file storage (no MinIO)
echo - Test data pre-loaded
echo - Full API documentation
echo - Zero Docker dependencies
echo.
echo TIP: Run 'quick-start.cmd info' to see URLs
echo.

call mvnw.cmd spring-boot:run -Pdocker-free -DskipTests
goto :end

:info
echo.
echo ===============================
echo   QUICK START DEVELOPMENT URLS
echo ===============================
echo Server:           http://localhost:8081
echo API Docs:         http://localhost:8081/swagger-ui/index.html
echo H2 Console:       http://localhost:8081/h2-console
echo   JDBC URL:       jdbc:h2:mem:motivise_dev
echo   Username:       sa
echo   Password:       (empty)
echo Actuator Health:  http://localhost:8081/actuator/health
echo.
echo ===============================
echo   TEST DATA AVAILABLE
echo ===============================
echo Users:
echo   anna.schmidt@example.com / Password123!
echo   max.meier@example.com / Password123!
echo   admin@motivise.app / AdminPass456!
echo.
echo Posts: Sample study posts pre-loaded
echo Media: Mock file storage (no actual files)
echo.
echo ===============================
echo   DEVELOPMENT NOTES
echo ===============================
echo - In-memory database (no persistence)
echo - No Docker dependencies required
echo - Perfect for rapid prototyping
echo - Data resets on server restart
echo.
echo For production-like MySQL environment:
echo   start.cmd
echo.
goto :end

:help
echo.
echo Quick Start Commands:
echo.
echo   quick-start.cmd       - Start H2 development (no Docker)
echo   quick-start.cmd clean - Clean build then start
echo   quick-start.cmd info  - Show URLs and test data
echo   quick-start.cmd help  - Show this help
echo.
echo Alternative modes:
echo   start.cmd         - Standard Docker/MySQL development
echo   test.cmd          - Run tests only
echo   clean-install.cmd - Nuclear recovery option
echo.

:end
