FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY eventhub-contracts/pom.xml eventhub-contracts/
COPY eventhub-api/pom.xml eventhub-api/
COPY eventHub-payments/pom.xml eventHub-payments/

COPY eventhub-contracts/src eventhub-contracts/src
COPY eventhub-api/src eventhub-api/src

RUN mvn -pl eventhub-api -am clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app

COPY --from=build --chown=app:app /app/eventhub-api/target/*.jar app.jar

ARG API_SERVER_PORT
ENV API_SERVER_PORT=${API_SERVER_PORT}

USER app

HEALTHCHECK --interval=10s --timeout=5s --start-period=40s --retries=5 \
    CMD wget -qO- "http://localhost:${API_SERVER_PORT}/actuator/health" || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
