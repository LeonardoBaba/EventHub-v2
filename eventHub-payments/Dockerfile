FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY pom.xml .

COPY eventhub-api/pom.xml eventhub-api/
COPY eventHub-payments/pom.xml eventHub-payments/

COPY eventHub-payments/src eventHub-payments/src

RUN mvn -pl eventHub-payments clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/eventHub-payments/target/*.jar app.jar

ARG PAYMENTS_SERVER_PORT
ENV PAYMENTS_SERVER_PORT=${PAYMENTS_SERVER_PORT}

ENTRYPOINT ["java", "-jar", "app.jar"]