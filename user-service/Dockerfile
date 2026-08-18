# --- Build Stage ---
FROM maven:3.9.6-eclipse-temurin-21-jammy AS build

# Set the working directory inside the container to the project root
WORKDIR /app

# Copy the entire project from the build context (which is the project root)
COPY . .



# Change working directory to the user-service module
WORKDIR /app/user-service

# Build the application (this will build the user-service module)
RUN mvn clean package -DskipTests

# --- Run Stage ---
FROM eclipse-temurin:21-jre-jammy AS run

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the build stage
# The JAR will be in /app/user-service/target/user-service-1.0.0.jar from the build stage
COPY --from=build /app/user-service/target/user-service-1.0.0.jar app.jar

# Expose the port your Spring Boot application runs on (e.g., 8081 from application.properties)
EXPOSE 8081

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]