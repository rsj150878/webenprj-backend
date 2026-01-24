@echo off

echo Clean install - this will delete target/ and rebuild everything
echo Press any key to continue or Ctrl+C to cancel...
pause > nul

if exist "target" rmdir /s /q target
if exist ".mvn" rmdir /s /q .mvn

echo Rebuilding (tests skipped)...
call mvnw.cmd clean install -DskipTests -U --fail-never

if errorlevel 1 (
    echo Retrying with fresh Maven wrapper...
    if exist "mvnw.cmd" del mvnw.cmd
    if exist "mvnw" del mvnw
    if exist ".mvn" rmdir /s /q .mvn
    mvn wrapper:wrapper -Dmaven=3.9.9
    call mvnw.cmd clean install -DskipTests -U

    if errorlevel 1 (
        echo Build failed. Check Java version (needs 21) and network connection.
        pause
        exit /b 1
    )
)

echo Done. Run start.cmd or quick-start.cmd to start the server.
pause
