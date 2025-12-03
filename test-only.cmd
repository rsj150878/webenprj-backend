@echo off
REM ===============================
REM TEST-ONLY MODE
REM Bulletproof isolated testing without external dependencies
REM ===============================

echo [TEST-ONLY] Starting bulletproof testing environment...
echo [TEST-ONLY] Pure in-memory testing - completely isolated!

REM Set environment for test-isolated mode
set SPRING_PROFILES_ACTIVE=test-isolated

REM Clean and run tests with coverage
echo [TEST-ONLY] Cleaning previous test results...
call mvnw.cmd clean -Ptest-isolated

if errorlevel 1 (
    echo [ERROR] Clean failed!
    pause
    exit /b 1
)

echo [TEST-ONLY] Running tests with coverage reporting...
call mvnw.cmd test -Ptest-isolated

if errorlevel 1 (
    echo [ERROR] Tests failed!
    echo [ERROR] Check test output above for details
    pause
    exit /b 1
)

echo [TEST-ONLY] Generating coverage report...
call mvnw.cmd jacoco:report -Ptest-isolated

if errorlevel 1 (
    echo [WARNING] Coverage report generation had issues, but tests passed
)

echo.
echo [SUCCESS] All tests passed!
echo [INFO] Coverage report generated at: target\site\jacoco\index.html
echo [INFO] Test results available in: target\surefire-reports\
echo.

REM Try to open coverage report if it exists
if exist "target\site\jacoco\index.html" (
    echo [INFO] Opening coverage report in default browser...
    start "" "target\site\jacoco\index.html"
) else (
    echo [WARNING] Coverage report not found at expected location
)

pause