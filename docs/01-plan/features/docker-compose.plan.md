# Plan: Docker Compose 통합 (Spring + Django + Postgres)

> Issue: #207
> Created: 2026-03-18
> Status: Approved (리뷰 반영 완료)

## 1. 목적

`docker-compose up -d` 한 번으로 Spring Boot + Django AI Sidecar + PostgreSQL 전체 스택을 실행한다.
로컬 개발 환경 셋업 시간을 줄이고, 팀원 누구나 동일한 환경에서 개발할 수 있게 한다.

## 2. 현재 상태

| 항목 | 현재 |
|------|------|
| PostgreSQL | `backend/docker-compose.yml`에 Postgres만 정의 (포트 5432) |
| Spring Boot | 로컬 실행 (`./gradlew bootRun`), Dockerfile 없음 |
| Django AI | 로컬 실행 (`python manage.py runserver`), Dockerfile 없음 |
| 서비스 간 통신 | Spring → Django: `AI_SERVICE_URL` (PR #211 머지 후 사용 가능) |

### Prerequisites

- PR #211 (댓글 AI 필터링) 머지 필요 — `fanpulse.ai-service.base-url` 설정 포함
- 또는 `application.yml`에 `AI_SERVICE_URL` 환경 변수 바인딩 직접 추가

## 3. 목표 아키텍처

```
┌─────────────────────────────────────────────────┐
│ docker-compose.yml (루트)                        │
│                                                  │
│  ┌──────────┐  ┌──────────┐  ┌──────────────┐   │
│  │ postgres │  │ spring   │  │ django-ai    │   │
│  │ :5432    │  │ :8080    │  │ :8000        │   │
│  └──────────┘  └──────────┘  └──────────────┘   │
│       ▲              │  ▲           │            │
│       │              │  └───────────┘            │
│       └──────────────┘   (internal network)      │
│                                                  │
│  fanpulse-net (bridge)                           │
└─────────────────────────────────────────────────┘
```

- **postgres**: PostgreSQL 14, 볼륨 영속, healthcheck
- **spring**: Gradle multi-stage Dockerfile, Java 17, Flyway 자동 마이그레이션
- **django-ai**: Python multi-stage Dockerfile, Gunicorn (1 worker), Django migrate 자동
- **네트워크**: `fanpulse-net` bridge, 서비스 간 hostname으로 접근

## 4. Acceptance Criteria (Issue #207)

- [ ] AC1: `docker-compose up -d` 후 3개 서비스 60초 내 healthy
- [ ] AC2: `curl http://localhost:8080/actuator/health` → 200
- [ ] AC3: `curl -H "X-API-Key: test" http://localhost:8000/api/health` → 200
- [ ] AC4: `docker-compose logs -f`로 3개 서비스 로그 확인 가능

## 5. Phase 분해

### Phase 1: Dockerfile 작성 (Spring + Django)

**목표**: 각 서비스의 독립 Docker 이미지 빌드 성공

**산출물**:
- `backend/Dockerfile` (Spring Boot multi-stage)
- `ai/Dockerfile` (Django multi-stage)
- 각각 `docker build` 성공

**Spring Dockerfile 전략**:
```
Stage 1 (builder): gradle:8-jdk17
  → COPY build.gradle.kts settings.gradle.kts → ./gradlew dependencies (캐싱)
  → COPY src → ./gradlew bootJar -x test
Stage 2 (runtime): eclipse-temurin:17-jre-alpine
  → COPY --from=builder build/libs/*.jar app.jar
  → EXPOSE 8080
  → ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Django Dockerfile 전략**:
```
Stage 1 (builder): python:3.11-slim
  → COPY requirements.txt → pip install --prefix=/install
Stage 2 (runtime): python:3.11-slim
  → COPY --from=builder /install /usr/local
  → COPY . /app
  → EXPOSE 8000
  → CMD ["gunicorn", "config.wsgi:application", "--bind", "0.0.0.0:8000", "--workers", "1"]
```
※ `--workers 1`: LLM 모델이 worker마다 로드되므로 메모리 절약 필수

**Quality Gate**:
- [ ] `docker build -t fanpulse-spring ./backend` 성공
- [ ] `docker build -t fanpulse-django-ai ./ai` 성공
- [ ] 이미지 크기 확인 (Spring < 400MB, Django < 3GB — torch 포함)

### Phase 2: docker-compose.yml 통합

**목표**: 3개 서비스를 하나의 compose 파일로 오케스트레이션

**산출물**:
- `/docker-compose.yml` (루트, 기존 backend/ 것 대체)
- `.env.docker.example` (환경 변수 예시)

**핵심 설정**:
| 서비스 | 포트 | depends_on | healthcheck | restart |
|--------|------|------------|-------------|---------|
| postgres | 5432:5432 | - | `pg_isready` | unless-stopped |
| spring | 8080:8080 | postgres (healthy) | `/actuator/health` | on-failure |
| django-ai | 8000:8000 | postgres (healthy) | `/api/health` | on-failure |

**환경 변수 — Spring**:
```
DB_HOST=postgres
DB_PORT=5432
DB_NAME=fanpulse
DB_USERNAME=fanpulse
DB_PASSWORD=fanpulse
AI_SERVICE_URL=http://django-ai:8000
JWT_SECRET=${JWT_SECRET}
FANPULSE_SCHEDULER_LIVE_DISCOVERY_ENABLED=false  # yt-dlp 미설치
FANPULSE_SCHEDULER_METADATA_REFRESH_ENABLED=false
```

**환경 변수 — Django**:
```
USE_POSTGRES=true
POSTGRES_HOST=postgres
POSTGRES_PORT=5432
POSTGRES_DB=fanpulse
POSTGRES_USER=fanpulse
POSTGRES_PASSWORD=fanpulse
DJANGO_DEBUG=false
ALLOWED_HOSTS=localhost,127.0.0.1,django-ai
AI_SERVICE_ACCEPTED_KEYS=${AI_API_KEY:-test-api-key}
```

**`.env.docker.example`**:
```
# PostgreSQL
POSTGRES_DB=fanpulse
POSTGRES_USER=fanpulse
POSTGRES_PASSWORD=fanpulse

# Spring
JWT_SECRET=change-me-in-production-at-least-256-bits

# Django AI
AI_API_KEY=test-api-key
```

**Quality Gate**:
- [ ] `docker-compose up -d` 성공
- [ ] `docker-compose ps` — 3개 서비스 healthy
- [ ] AC1~AC4 전부 통과

### Phase 3: .dockerignore + 정리

**목표**: 이미지 최적화 및 기존 compose 정리

**산출물**:
- `backend/.dockerignore`
- `ai/.dockerignore`
- `backend/docker-compose.yml` 삭제 (루트로 통합됨)

**Quality Gate**:
- [ ] 불필요한 파일 미포함 확인 (.git, build/, node_modules, __pycache__)
- [ ] clean build 후 이미지 크기 재확인

## 6. 기술 결정

| 결정 | 선택 | 이유 |
|------|------|------|
| compose 파일 위치 | 루트 (`/docker-compose.yml`) | Spring, Django, Postgres 모두 접근 |
| Spring 빌드 | multi-stage (Gradle) | 빌드 도구 미포함 → 이미지 경량화 |
| Django 실행 | Gunicorn (1 worker) | LLM 모델 메모리 공유 불가 → 단일 worker |
| 네트워크 | 커스텀 bridge | 서비스 간 hostname 기반 통신 |
| 기존 compose | 삭제 (`backend/docker-compose.yml`) | 루트 compose로 통합 |
| 스케줄러 | Docker 환경에서 비활성화 | yt-dlp 미설치, 별도 처리 필요 |

## 7. 리스크

| 리스크 | 확률 | 영향 | 대응 |
|--------|------|------|------|
| Django AI 모델 다운로드 느림 (2~4GB) | 높음 | 중간 | cache 볼륨으로 영속화 |
| Spring Gradle 빌드 느림 | 중간 | 낮음 | dependency layer 캐싱 |
| 메모리 부족 (LLM 로딩) | 중간 | 높음 | `--workers 1`, compose `mem_limit: 4g` |
| Flyway 마이그레이션 충돌 | 낮음 | 높음 | `depends_on: postgres(healthy)` |
| yt-dlp 미설치 (Spring 이미지) | 높음 | 낮음 | 스케줄러 env로 비활성화 |
| Django ALLOWED_HOSTS 미설정 | 높음 | 높음 | compose env에 `django-ai` 호스트 추가 |

## 8. 작업 브랜치

- 브랜치: `feature/207-docker-compose`
- Worktree: `.worktrees/207` (이미 존재)
