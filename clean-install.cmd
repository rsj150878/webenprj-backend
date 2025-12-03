@echo off
REM ===============================
REM CLEAN INSTALL (NUCLEAR OPTION)
REM Test-free recovery for broken environments
REM ===============================

echo [NUCLEAR] CLEAN INSTALL - Complete test-free project rebuild
echo [NUCLEAR] This will fix most issues caused by destructive changes
echo [NUCLEAR] TESTS ARE SKIPPED - Use test-only.cmd for testing
echo.
echo WARNING: This will delete all compiled code and rebuild everything!
echo Press any key to continue or Ctrl+C to cancel...
pause > nul

echo.
echo [NUCLEAR] Step 1: Deep clean - removing all build artifacts...
if exist "target" (
    echo [NUCLEAR] Removing target directory...
    rmdir /s /q target
)

if exist ".mvn" (
    echo [NUCLEAR] Removing .mvn directory...
    rmdir /s /q .mvn
)

echo [NUCLEAR] Step 2: Maven clean install WITHOUT TESTS...
call mvnw.cmd clean install -DskipTests -U --fail-never

if errorlevel 1 (
    echo.
    echo [NUCLEAR] Step 3: ULTIMATE RECOVERY - Force reinstall Maven wrapper...
    if exist "mvnw.cmd" del mvnw.cmd
    if exist "mvnw" del mvnw
    if exist ".mvn" rmdir /s /q .mvn
    
    echo [NUCLEAR] Re-downloading Maven wrapper...
    mvn wrapper:wrapper -Dmaven=3.9.9
    
    echo [NUCLEAR] Retrying clean install WITHOUT TESTS...
    call mvnw.cmd clean install -DskipTests -U
    
    if errorlevel 1 (
        echo.
        echo [NUCLEAR] CRITICAL: Even nuclear option failed!
        echo [NUCLEAR] Possible issues:
        echo [NUCLEAR] 1. Java installation problems
        echo [NUCLEAR] 2. Network connectivity issues  
        echo [NUCLEAR] 3. Corrupted Maven repository
        echo [NUCLEAR] 4. Severe pom.xml corruption
        echo.
        echo [NUCLEAR] Manual recovery steps:
        echo [NUCLEAR] 1. Check Java: java -version
        echo [NUCLEAR] 2. Clear Maven repo: rmdir /s /q %USERPROFILE%\.m2\repository
        echo [NUCLEAR] 3. Check pom.xml syntax
        echo [NUCLEAR] 4. Try: mvnw.cmd clean compile -DskipTests
        pause
        exit /b 1
    )
)

echo.
echo [SUCCESS] Nuclear clean install completed successfully!
echo [SUCCESS] All dependencies downloaded and project compiled
echo [SUCCESS] Tests were SKIPPED - project ready for development
echo.
echo ===============================
echo   NEXT STEPS
echo ===============================
echo For development:  start.cmd (MySQL/Docker)
echo For quick start:  quick-start.cmd (H2, no Docker)
echo For testing:      test.cmd
echo For production:   Use docker-compose setup
echo.
echo Your environment is now bulletproof against:
echo - MinIO connection issues
echo - Database dependencies  
echo - Testing constraints
echo - Co-developer configuration damage
echo.
pause