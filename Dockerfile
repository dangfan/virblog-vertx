# Multi-stage build for Kotlin/Vert.x application
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

# Copy gradle files
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

# Download dependencies (cached layer)
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY src ./src

# Build the application
RUN gradle jar --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Create data directory
RUN mkdir -p /app/data

# Expose application port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]
