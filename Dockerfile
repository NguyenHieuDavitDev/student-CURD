# --- build ---
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline -DskipTests || true
COPY src ./src
RUN mvn -B -q -DskipTests package

# --- runtime ---
FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl tzdata \
    && addgroup -S spring && adduser -S spring -G spring
ENV TZ=Asia/Ho_Chi_Minh
WORKDIR /app

COPY --from=build /app/target/schoolmanager.jar app.jar
RUN mkdir -p /data/uploads && chown -R spring:spring /data/uploads /app

USER spring
EXPOSE 8080

# Chờ DB + context Spring (start_period dài cho lần chạy đầu / SQL Server chậm).
HEALTHCHECK --interval=15s --timeout=5s --start-period=120s --retries=5 \
  CMD curl -fsS http://127.0.0.1:8080/actuator/health/readiness >/dev/null || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
