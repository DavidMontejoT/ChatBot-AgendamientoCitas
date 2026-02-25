# Multi-stage build for Spring Boot application
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy backend files
COPY backend/pom.xml .
COPY backend/src ./src

# Build application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/citas-medicas-1.0.0.jar app.jar

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
