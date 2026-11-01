# ==========================================
# Stage 1: Build the JAR using Maven
# ==========================================
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# Stage 2: Minimal lightweight Runtime
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as a non-root system user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy built artifact from builder stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]


