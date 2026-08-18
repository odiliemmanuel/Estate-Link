# Generic multi-stage image for any Estate-Link reactor module
# (user, property, inspection, offer, analytics, notification, eureka).
# Build with: docker build --build-arg MODULE=offer-service --build-arg PORT=8084 .

# --- Build Stage ---
FROM maven:3.9.6-eclipse-temurin-21-jammy AS build

WORKDIR /app
COPY . .

# MODULE is the reactor module name; -am also builds 'common'.
ARG MODULE
RUN --mount=type=cache,target=/root/.m2 \
    mvn -q -pl ${MODULE} -am package -DskipTests

# --- Run Stage ---
FROM eclipse-temurin:21-jre-jammy AS run

WORKDIR /app

ARG MODULE
ARG PORT
COPY --from=build /app/${MODULE}/target/${MODULE}-1.0.0.jar app.jar

EXPOSE ${PORT}
ENTRYPOINT ["java", "-jar", "app.jar"]
