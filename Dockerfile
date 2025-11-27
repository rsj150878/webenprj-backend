# ===============================
# 🧱 Build Stage
# ===============================
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Erst pom.xml + checkstyle, damit Layer gecached wird
COPY pom.xml .
COPY checkstyle.xml .

# Dependencies cachen (schnellerer Build)
RUN mvn dependency:go-offline -DskipTests

# Jetzt Source Code kopieren
COPY src ./src

# Tests überspringen, damit DB-Verbindung beim Build nicht erforderlich ist
RUN mvn clean install package -DskipTests

# ===============================
# 🚀 Runtime Stage
# ===============================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Nur die gebaute JAR ins Runtime-Image übernehmen
COPY --from=build /app/target/webenprjbackend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
