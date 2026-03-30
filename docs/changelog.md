# FanPulse Changelog

모든 주요 변경사항을 기록합니다. [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)를 따릅니다.

---

## [2026-03-22] - Docker Compose 3서비스 통합 완료

### Added
- Docker Compose 통합 구성 (Spring Boot, Django AI Sidecar, PostgreSQL)
- Spring Boot multi-stage Dockerfile (gradle 캐싱, non-root user)
- Django AI multi-stage Dockerfile (gunicorn 1 worker, migrate --fake-initial)
- 루트 레벨 `docker-compose.yml` (fanpulse-net bridge 네트워크)
- `.env.docker.example` (환경 변수 템플릿)
- `backend/.dockerignore`, `ai/.dockerignore` (빌드 최적화)
- `backend/src/main/resources/db/migration/V117__create_news_table.sql` (News 엔티티)
- 검증 스킬: `verify-flyway-entity-sync`, `verify-django-api-standards`

### Changed
- `backend/src/main/resources/db/migration/V7__create_content_tables.sql`: `CREATE TABLE` → `CREATE TABLE IF NOT EXISTS` (5개 테이블)
- Django 실행 명령: `migrate --fake-initial --noinput` 플래그 추가
- `.gitignore`: Docker 관련 패턴 추가

### Fixed
- Docker Desktop 좀비 컨테이너 포트 충돌 이슈
- SeedModels JSON 타입 불일치 (id: Long → UUID)
- Flyway 마이그레이션 레이스 컨디션
- Django 공유 데이터베이스 마이그레이션 충돌

### Metrics
- 설계 일치율: 95.6% (43/45 항목)
- AC 만족도: 100% (4/4 항목)
- 초기 부팅 시간: ~45초 (목표: <60초)
- 메모리 사용: ~5.5GB (목표: <6GB)

---

## [2026-03-15] - 댓글 AI 필터링 (#206) 완료

### Added
- Django AI Sidecar 기본 구조 (comment filter, summarizer)
- Spring Boot AI Client (WebClient 기반, Circuit Breaker 패턴)
- PostgreSQL 공유 데이터베이스 (Flyway 마이그레이션)

---

## [2026-02-28] - 초기 프로젝트 세팅

### Added
- FanPulse 프로젝트 초기화
- Spring Boot backend (Hexagonal Architecture)
- Next.js frontend
- Django AI Sidecar 스켈레톤
