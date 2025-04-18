# Stage 1: Build the application
FROM gradle:jdk21-alpine AS builder

# Set working directory
WORKDIR /app

# Copy gradle configuration files
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Download dependencies (this layer will be cached unless dependencies change)
RUN gradle dependencies --no-daemon

# Copy source code
COPY src ./src

# Build the application
RUN gradle build --no-daemon -x test

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre-alpine

# Create a non-root user for security
RUN addgroup --system --gid 1001 appuser && \
    adduser --system --uid 1001 --ingroup appuser appuser

# Set working directory
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Set ownership to non-root user
RUN chown -R appuser:appuser /app

# Use the non-root user
USER appuser

# Configure JVM options for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
