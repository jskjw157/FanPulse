# ── Stage 1: Build ──
FROM gradle:8-jdk17 AS builder
WORKDIR /app

# 의존성 캐싱 레이어
COPY build.gradle.kts settings.gradle.kts gradle.properties* ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon || true

# 소스 복사 + 빌드
COPY src ./src
RUN gradle bootJar -x test --no-daemon

# ── Stage 2: Runtime ──
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

USER appuser
ENTRYPOINT ["java", "-jar", "app.jar"]
