# --- Build Stage ---
FROM maven:3.9.6-eclipse-temurin-21-jammy AS build

WORKDIR /app

COPY . .

WORKDIR /app/inspection-service
RUN mvn clean package -DskipTests

# --- Run Stage ---
FROM eclipse-temurin:21-jre-jammy AS run

WORKDIR /app

COPY --from=build /app/inspection-service/target/inspection-service-1.0.0.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
