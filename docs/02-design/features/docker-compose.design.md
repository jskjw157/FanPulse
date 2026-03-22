# Design: Docker Compose 통합 (Spring + Django + Postgres)

> Issue: #207
> Plan: docs/01-plan/features/docker-compose.plan.md
> Created: 2026-03-18

## 1. 파일 구조

```
FanPulse/
├── docker-compose.yml          # 신규 (3 services)
├── .env.docker.example         # 신규 (환경 변수 예시)
├── backend/
│   ├── Dockerfile              # 신규 (Spring multi-stage)
│   └── .dockerignore           # 신규
├── ai/
│   ├── Dockerfile              # 신규 (Django multi-stage)
│   └── .dockerignore           # 신규
└── backend/docker-compose.yml  # 삭제 (루트로 통합)
```

## 2. Spring Dockerfile (`backend/Dockerfile`)

```dockerfile
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
```

### 설계 포인트
- `gradle dependencies`를 먼저 실행 → 소스 변경 시 의존성 레이어 캐시 재사용
- `-x test`: 테스트는 CI에서 별도 실행, Docker 빌드 시간 절약
- `alpine` JRE: 이미지 크기 ~250MB (일반 JRE 대비 40% 절감)
- non-root user (`appuser`): 보안 기본

## 3. Django Dockerfile (`ai/Dockerfile`)

```dockerfile
# ── Stage 1: Build ──
FROM python:3.11-slim AS builder
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    gcc libpq-dev && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir --prefix=/install -r requirements.txt

# ── Stage 2: Runtime ──
FROM python:3.11-slim
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    libpq5 && rm -rf /var/lib/apt/lists/*

COPY --from=builder /install /usr/local
COPY . .

RUN addgroup --system appgroup && adduser --system --group appgroup appuser

EXPOSE 8000

USER appuser

CMD ["sh", "-c", "python manage.py migrate --noinput && gunicorn config.wsgi:application --bind 0.0.0.0:8000 --workers 1 --timeout 120"]
```

### 설계 포인트
- `gcc`, `libpq-dev`: psycopg2 컴파일에 필요 → builder에서만 설치
- `libpq5`: 런타임 PostgreSQL 클라이언트 라이브러리만 복사
- `--workers 1`: LLM 모델이 worker마다 로드됨 → 단일 worker로 메모리 절약
- `--timeout 120`: AI 추론 시간이 길 수 있음 (기본 30초 → 120초)
- `migrate --noinput`: 컨테이너 시작 시 자동 마이그레이션

## 4. docker-compose.yml (루트)

```yaml
services:
  postgres:
    image: postgres:14
    container_name: fanpulse-postgres
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: ${POSTGRES_DB:-fanpulse}
      POSTGRES_USER: ${POSTGRES_USER:-fanpulse}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:-fanpulse}
    volumes:
      - fanpulse_postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER:-fanpulse} -d ${POSTGRES_DB:-fanpulse}"]
      interval: 5s
      timeout: 5s
      retries: 10
    restart: unless-stopped
    networks:
      - fanpulse-net

  spring:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: fanpulse-spring
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      DB_HOST: postgres
      DB_PORT: 5432
      DB_NAME: ${POSTGRES_DB:-fanpulse}
      DB_USERNAME: ${POSTGRES_USER:-fanpulse}
      DB_PASSWORD: ${POSTGRES_PASSWORD:-fanpulse}
      AI_SERVICE_URL: http://django-ai:8000
      JWT_SECRET: ${JWT_SECRET:-default-dev-secret-key-256-bits-long-for-hs256-algorithm}
      FANPULSE_SCHEDULER_LIVE_DISCOVERY_ENABLED: "false"
      FANPULSE_SCHEDULER_METADATA_REFRESH_ENABLED: "false"
    depends_on:
      postgres:
        condition: service_healthy
    healthcheck:
      test: ["CMD-SHELL", "wget -qO- http://localhost:8080/actuator/health || exit 1"]
      interval: 10s
      timeout: 5s
      retries: 12
      start_period: 30s
    restart: on-failure
    networks:
      - fanpulse-net

  django-ai:
    build:
      context: ./ai
      dockerfile: Dockerfile
    container_name: fanpulse-django-ai
    ports:
      - "8000:8000"
    environment:
      USE_POSTGRES: "true"
      POSTGRES_HOST: postgres
      POSTGRES_PORT: 5432
      POSTGRES_DB: ${POSTGRES_DB:-fanpulse}
      POSTGRES_USER: ${POSTGRES_USER:-fanpulse}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:-fanpulse}
      DJANGO_DEBUG: "false"
      ALLOWED_HOSTS: "localhost,127.0.0.1,django-ai"
      AI_SERVICE_ACCEPTED_KEYS: ${AI_API_KEY:-test-api-key}
    depends_on:
      postgres:
        condition: service_healthy
    healthcheck:
      test: ["CMD-SHELL", "python -c \"import urllib.request; urllib.request.urlopen('http://localhost:8000/api/health')\""]
      interval: 10s
      timeout: 5s
      retries: 12
      start_period: 15s
    restart: on-failure
    networks:
      - fanpulse-net

volumes:
  fanpulse_postgres_data:

networks:
  fanpulse-net:
    driver: bridge
```

### 설계 포인트
- `start_period`: Spring 30s (Flyway + JVM warmup), Django 15s (migrate)
- `retries: 12`: 10s × 12 = 120초 여유 (AC1의 60초보다 넉넉하게)
- Postgres healthcheck 후 Spring/Django 시작 → Flyway 안전
- Django healthcheck: `curl` 미설치이므로 Python urllib 사용
- 스케줄러 비활성화: Spring 이미지에 yt-dlp 없음

## 5. .env.docker.example

```env
# ─── PostgreSQL ───
POSTGRES_DB=fanpulse
POSTGRES_USER=fanpulse
POSTGRES_PASSWORD=fanpulse

# ─── Spring Boot ───
JWT_SECRET=change-me-in-production-at-least-256-bits-long

# ─── Django AI Sidecar ───
AI_API_KEY=test-api-key
```

## 6. .dockerignore 파일

### `backend/.dockerignore`
```
.git
.gradle
build
*.log
.idea
.vscode
docker-compose.yml
test-output.log
```

### `ai/.dockerignore`
```
.git
__pycache__
*.pyc
db.sqlite3
.env
*.log
legacy/
```

## 7. 환경 변수 매핑 검증

| 서비스 | compose env | 코드에서 읽는 위치 | 기본값 |
|--------|------------|-------------------|--------|
| Spring | `DB_HOST` | `application.yml: ${DB_HOST:localhost}` | localhost |
| Spring | `DB_USERNAME` | `application.yml: ${DB_USERNAME:fanpulse}` | fanpulse |
| Spring | `AI_SERVICE_URL` | `application.yml: ${AI_SERVICE_URL:http://localhost:8001}` | localhost:8001 |
| Spring | `JWT_SECRET` | `application.yml: ${JWT_SECRET:...}` | dev default |
| Django | `USE_POSTGRES` | `settings.py:140` | false |
| Django | `POSTGRES_HOST` | `settings.py:150` | localhost |
| Django | `POSTGRES_USER` | `settings.py:148` | postgres |
| Django | `POSTGRES_PASSWORD` | `settings.py:149` | (empty) |
| Django | `DJANGO_DEBUG` | `settings.py:44` | True |
| Django | `ALLOWED_HOSTS` | `settings.py:48` | localhost,127.0.0.1 |
| Django | `AI_SERVICE_ACCEPTED_KEYS` | `settings.py:51` | (empty) |

## 8. 구현 순서

1. `backend/Dockerfile` 작성 + `docker build` 테스트
2. `ai/Dockerfile` 작성 + `docker build` 테스트
3. 루트 `docker-compose.yml` 작성
4. `.env.docker.example` 작성
5. `docker-compose up -d` → AC1~AC4 검증
6. `.dockerignore` 추가
7. `backend/docker-compose.yml` 삭제
8. clean rebuild 후 최종 검증
