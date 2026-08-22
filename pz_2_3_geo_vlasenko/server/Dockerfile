# ---------- Stage 1: build ----------
FROM maven:3.9-eclipse-temurin-26 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests package && \
    cp target/*.jar target/app.jar

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:26-jre-alpine AS runtime
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
USER app
COPY --from=build /build/target/app.jar ./app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]