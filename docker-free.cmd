@echo off
echo.
echo ===============================
echo   DOCKER-FREE Development Mode
echo   ^(H2 Database + No Testing^)
echo ===============================

if "%1"=="clean" (
    echo Cleaning and starting docker-free environment ^(skipping tests^)
    mvnw clean compile -DskipTests
    echo Clean build completed without tests!
    pause
    goto :end
)

if "%1"=="info" (
    goto :info
)

if "%1"=="help" (
    goto :help
)

echo Starting docker-free development server ^(bypassing all tests^)
echo.
echo Features enabled:
echo - H2 File-based database ^(persistent across restarts^)
echo - Mock file storage ^(no MinIO^)
echo - All test data pre-loaded
echo - Full API documentation
echo - ZERO testing constraints
echo.
echo TIP: Run 'docker-free.cmd info' to see URLs
echo.

mvnw spring-boot:run -Pdocker-free -DskipTests
goto :end

:info
echo.
echo ===============================
echo   DOCKER-FREE DEVELOPMENT URLS
echo ===============================
echo Server:           http://localhost:8081
echo API Docs:         http://localhost:8081/swagger-ui/index.html
echo H2 Console:       http://localhost:8081/h2-console
echo   JDBC URL:       jdbc:h2:file:./h2-data/motivise_dev
echo   Username:       sa
echo   Password:       ^(empty^)
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
echo Media: Mock file storage ^(no actual files^)
echo.
echo ===============================
echo   DEVELOPMENT NOTES
echo ===============================
echo - Database persists between restarts
echo - No Docker dependencies required
echo - No testing constraints
echo - Perfect for rapid development
echo.
goto :end

:help
echo.
echo Docker-Free Commands:
echo.
echo   docker-free.cmd       - Start test-free development
echo   docker-free.cmd clean - Clean build ^(skip tests^)
echo   docker-free.cmd info  - Show URLs and test data
echo   docker-free.cmd help  - Show this help
echo.
echo For testing use:       test-only.cmd
echo For nuclear recovery:  clean-install.cmd
echo.

:end