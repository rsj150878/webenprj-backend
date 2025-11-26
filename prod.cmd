@echo off
if "%1"=="clean" (
    echo Starting Motivise in PRODUCTION mode with CLEAN BUILD...
    echo Cleaning, compiling, and running tests...
    ./mvnw clean compile test
) else (
    echo Starting Motivise in PRODUCTION mode...
)
echo   Make sure Docker is running: docker-compose up -d mysql
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod