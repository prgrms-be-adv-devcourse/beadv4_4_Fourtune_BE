# Build stage
FROM eclipse-temurin:25-jdk-alpine AS builder

ARG MODULE_NAME

WORKDIR /app

# Copy Gradle wrapper files only
COPY gradlew .
COPY gradle gradle

# Download Gradle distribution via wget (more reliable than Java's HttpURLConnection)
# then redirect wrapper to local file to avoid network issues in build env
RUN apk add --no-cache wget && \
    GRADLE_VERSION=$(grep distributionUrl gradle/wrapper/gradle-wrapper.properties | sed 's/.*gradle-\(.*\)-bin.*/\1/') && \
    wget -q --tries=5 --retry-connrefused \
        "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" \
        -O /tmp/gradle.zip && \
    sed -i "s|distributionUrl=.*|distributionUrl=file:///tmp/gradle.zip|" \
        gradle/wrapper/gradle-wrapper.properties && \
    chmod +x gradlew && ./gradlew --version --no-daemon

# Copy build files and full source
COPY build.gradle .
COPY settings.gradle .
COPY . .

# Download dependencies (cache layer)
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

