@echo off
echo 🚀 Starting Motivise in PRODUCTION mode...
echo ⚠️  Make sure Docker is running: docker-compose up -d mysql
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod