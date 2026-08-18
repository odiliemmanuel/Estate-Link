# --- Build Stage ---
FROM maven:3.9.6-eclipse-temurin-21-jammy AS build

WORKDIR /app
COPY . .

RUN mvn -q package -DskipTests

# --- Run Stage ---
FROM eclipse-temurin:21-jre-jammy AS run

WORKDIR /app

COPY --from=build /app/target/api-gateway-1.0.0.jar app.jar

EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]
