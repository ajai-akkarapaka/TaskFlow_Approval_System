# Multi-stage Dockerfile: build the jar with Maven, then produce a small runtime image.
# This avoids committing 'target/' or the built jar to the repository.

# Builder stage: use an official Maven image with JDK to build the project
FROM maven:3.8.8-eclipse-temurin-17 AS builder
WORKDIR /app
# Copy only the files needed for a reproducible Maven build
COPY pom.xml ./
COPY src ./src
# Build the project (skip tests for faster builds on CI); produces target/*.jar
RUN mvn -B -DskipTests package

# Runtime stage: smaller image containing only the built jar
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
# Use $PORT provided by Render; fall back to 8080 when $PORT is empty
ENTRYPOINT ["sh","-c","java -Dserver.port=${PORT:-8080} -jar /app.jar"]
