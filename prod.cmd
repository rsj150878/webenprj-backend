@echo off
echo.
echo ===============================
echo   Production Build
echo ===============================

if "%1"=="clean" (
    echo Cleaning and creating production build
    mvnw clean package -DskipTests
    echo.
    echo Production JAR created!
    echo Location: target\webenprjbackend-0.0.1-SNAPSHOT.jar
    echo.
    pause
    goto :end
)

echo Checking if production JAR exists
if not exist "target\webenprjbackend-0.0.1-SNAPSHOT.jar" (
    echo JAR file not found! Building production package first
    echo.
    mvnw clean package -DskipTests
    echo.
)

echo Starting production server
echo.
echo Server will be available at: http://localhost:8081
echo.
echo Press Ctrl+C to stop the server
echo.

java -jar target\webenprjbackend-0.0.1-SNAPSHOT.jar

:end