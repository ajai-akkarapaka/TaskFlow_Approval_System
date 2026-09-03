# Build stage (Uses Maven with Eclipse Temurin OpenJDK 25)
FROM maven:3.9.6-eclipse-temurin-25 AS build
COPY . .
RUN mvn clean package -DskipTests

# Run stage (Uses lightweight Eclipse Temurin Alpine OpenJDK 25)
FROM eclipse-temurin:25-jdk-alpine
COPY --from=build /target/taskflow-approval-system-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
