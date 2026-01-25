@echo off

if "%1"=="clean" (
    echo Cleaning...
    call mvnw.cmd clean compile -DskipTests
    if errorlevel 1 exit /b 1
)

if "%1"=="info" (
    echo.
    echo Server:     http://localhost:8081
    echo Swagger:    http://localhost:8081/swagger-ui/index.html
    echo H2 Console: http://localhost:8081/h2-console (sa / empty password)
    echo JDBC URL:   jdbc:h2:mem:motivise_dev
    echo.
    echo Test logins:
    echo   anna.schmidt@example.com / Password123!
    echo   max.meier@example.com / Password123!
    echo   admin@motivise.app / AdminPass456!
    exit /b 0
)

set SPRING_PROFILES_ACTIVE=docker-free
if not defined JWT_SECRET_KEY set JWT_SECRET_KEY=dev-only-jwt-secret-key-min-32-chars-do-not-use-in-prod

echo Starting Spring Boot (H2, no Docker)...
call mvnw.cmd spring-boot:run -Pdocker-free -DskipTests
