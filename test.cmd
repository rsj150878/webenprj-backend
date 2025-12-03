@echo off
echo.
echo ===============================
echo   TEST MODE
echo   Isolated Testing Environment
echo ===============================
echo.

echo [TEST] Starting isolated testing environment...
echo [TEST] Using in-memory H2 database (completely isolated)
echo.

REM Set environment for test-isolated mode
set SPRING_PROFILES_ACTIVE=test-isolated

REM Clean and run tests with coverage
echo [TEST] Cleaning previous test results...
call mvnw.cmd clean -Ptest-isolated

if errorlevel 1 (
    echo [ERROR] Clean failed!
    pause
    exit /b 1
)

echo [TEST] Running tests with coverage reporting...
call mvnw.cmd test -Ptest-isolated

if errorlevel 1 (
    echo.
    echo [ERROR] Tests failed!
    echo [ERROR] Check test output above for details
    echo.
    pause
    exit /b 1
)

echo [TEST] Generating coverage report...
call mvnw.cmd jacoco:report -Ptest-isolated

if errorlevel 1 (
    echo [WARNING] Coverage report generation had issues, but tests passed
)

echo.
echo ===============================
echo   TEST RESULTS
echo ===============================
echo [SUCCESS] All tests passed!
echo [INFO] Coverage report: target\site\jacoco\index.html
echo [INFO] Test results: target\surefire-reports\
echo.

REM Try to open coverage report if it exists
if exist "target\site\jacoco\index.html" (
    echo [INFO] Opening coverage report in default browser...
    start "" "target\site\jacoco\index.html"
) else (
    echo [WARNING] Coverage report not found at expected location
)

echo.
pause
