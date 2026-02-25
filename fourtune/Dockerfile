# Build stage
FROM eclipse-temurin:25-jdk-alpine AS builder

ARG MODULE_NAME

WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Grant execute permission to gradlew
RUN chmod +x gradlew

# Copy full source so Gradle can configure all subprojects (e.g. common:core)
COPY . .

# Download dependencies (cache layer) — run after COPY so :common:core etc. exist
RUN ./gradlew dependencies --no-daemon || true

# Build application
RUN ./gradlew :${MODULE_NAME}:bootjar -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:25-jre-alpine

ARG MODULE_NAME

WORKDIR /app

# Add non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy jar from builder
COPY --from=builder /app/${MODULE_NAME}/build/libs/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Duser.timezone=Asia/Seoul", \
  "-jar", \
  "app.jar"]

