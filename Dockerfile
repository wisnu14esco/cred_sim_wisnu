# syntax=docker/dockerfile:1

# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -DskipTests package

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre

RUN useradd --system --create-home --uid 10001 simulator \
    && mkdir -p /data \
    && chown simulator /data

WORKDIR /app
COPY --from=build /build/target/credit_simulator.jar /app/credit_simulator.jar

ENV CREDIT_SIMULATOR_SHEETS=/data/sheets.json
USER simulator

ENTRYPOINT ["java", "-jar", "/app/credit_simulator.jar"]