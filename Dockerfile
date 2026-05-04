# Stage 1: Build the application
FROM gradle:8-jdk17 AS builder
WORKDIR /app

# Copy all project files into the container
COPY . .

# Build the jar file, skipping tests to speed up the process
RUN gradle build -x test

# Stage 2: Create the final lightweight image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy only the compiled jar file from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
