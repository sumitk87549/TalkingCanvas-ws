# Multi-stage build for Spring Boot application

# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:resolve dependency:resolve-plugins

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create uploads directory
RUN mkdir -p /app/uploads

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port (Render sets PORT env var which overrides this)
EXPOSE 8088

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=120s \
  CMD wget --quiet --tries=1 --spider http://localhost:${PORT:-8088}/api/actuator/health/liveness || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
