@echo off
if "%1"=="clean" (
    echo Starting Motivise in DEVELOPMENT mode with CLEAN BUILD...
    echo Cleaning, compiling, and running tests...
    ./mvnw clean compile test
) else (
    echo Starting Motivise in DEVELOPMENT mode...
)
echo H2 database, test data, H2 console enabled
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev