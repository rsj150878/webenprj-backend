@echo off
echo.
echo ===============================
echo   Development Environment
echo ===============================

if "%1"=="clean" (
    echo Cleaning, compiling, and running tests
    mvnw clean compile test
    echo.
    echo Build completed successfully!
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

echo Starting development server
echo.
echo TIP: Run 'dev.cmd info' in another terminal to see development URLs
echo.

mvnw spring-boot:run
goto :end

:info
echo.
echo ===============================
echo   DEVELOPMENT URLS
echo ===============================
echo Server:           http://localhost:8081
echo API Documentation: http://localhost:8081/swagger-ui/index.html
echo H2 Database:      http://localhost:8081/h2-console
echo   ^(JDBC URL: jdbc:h2:mem:testdb^)
echo   ^(Username: sa, Password: ^<empty^>^)
echo.
echo ===============================
echo   Test Data Available
echo ===============================
echo Users:
echo   anna.schmidt@example.com / Password123!
echo   max.meier@example.com / Password123!
echo   admin@motivise.app / AdminPass456!
echo.
echo Posts: Sample study posts pre-loaded
echo.
goto :end

:help
echo.
echo Development Commands:
echo.
echo   dev.cmd           - Start development server
echo   dev.cmd clean     - Clean build and run tests
echo   dev.cmd info      - Show development URLs and test data
echo   dev.cmd help      - Show this help
echo.

:end