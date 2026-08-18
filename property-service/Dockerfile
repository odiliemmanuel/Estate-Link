# --- Build Stage ---
FROM maven:3.9.6-eclipse-temurin-21-jammy AS build

WORKDIR /app

# Copy the entire project from the project root context
COPY . .

# Build the property-service module
WORKDIR /app/property-service
RUN mvn clean package -DskipTests

# --- Run Stage ---
FROM eclipse-temurin:21-jre-jammy AS run

WORKDIR /app

COPY --from=build /app/property-service/target/property-service-1.0.0.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]