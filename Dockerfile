# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY checkstyle.xml .

RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package

# Run stage
FROM openjdk:21-jdk-slim
COPY --from=build /app/target/webenprjbackend-0.0.1-SNAPSHOT.jar /usr/local/lib/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/usr/local/lib/app.jar"]