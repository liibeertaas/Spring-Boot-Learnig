# server/Dockerfile

# ---------- Stage 1: build ----------
FROM maven:3.9-eclipse-temurin-26 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
ARG APP_VERSION=dev
RUN mvn -B -DskipTests -Drevision=${APP_VERSION} package && \
    cp target/*.jar target/app.jar

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:26-jre-alpine AS runtime
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
USER app
COPY --from=build /build/target/app.jar ./app.jar

ARG SERVER_PORT=5004
ENV SERVER_PORT=${SERVER_PORT}
EXPOSE ${SERVER_PORT}
ENTRYPOINT ["java", "-jar", "/app/app.jar"]