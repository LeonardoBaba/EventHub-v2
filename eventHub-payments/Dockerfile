FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY eventhub-contracts/pom.xml eventhub-contracts/
COPY eventhub-api/pom.xml eventhub-api/
COPY eventHub-payments/pom.xml eventHub-payments/

COPY eventhub-contracts/src eventhub-contracts/src
COPY eventHub-payments/src eventHub-payments/src

RUN mvn -pl eventHub-payments -am clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app

COPY --from=build --chown=app:app /app/eventHub-payments/target/*.jar app.jar

ARG PAYMENTS_SERVER_PORT
ENV PAYMENTS_SERVER_PORT=${PAYMENTS_SERVER_PORT}

USER app

ENTRYPOINT ["java", "-jar", "app.jar"]
