@echo off

set SPRING_PROFILES_ACTIVE=test-isolated
set JWT_SECRET_KEY=test-jwt-secret-key-minimum-32-characters-for-testing

echo Running tests...
call mvnw.cmd clean test jacoco:report -Ptest-isolated
if errorlevel 1 (
    echo Tests failed!
    pause
    exit /b 1
)

echo Tests passed!
if exist "target\site\jacoco\index.html" start "" "target\site\jacoco\index.html"
pause
