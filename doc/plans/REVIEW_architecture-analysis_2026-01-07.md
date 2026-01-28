# FanPulse 아키텍처 분석 보고서

**분석일**: 2026-01-07
**분석 범위**: Backend 코드, 문서 정합성, GitHub 이슈 구조
**분석 도구**: feature-dev:code-reviewer, github-issues, Explore agent

---

## 1. 백엔드 코드 리뷰 결과

### 1.1 Critical Issues (4개)

| # | 문제 | 파일 | 확신도 | 설명 |
|---|------|------|--------|------|
| 1 | **Transaction 관리 버그** | `MetadataRefreshServiceImpl.kt:62` | 95% | `@Transactional` 중복으로 deadlock 위험. `TransactionalMetadataUpdater`가 이미 `REQUIRES_NEW` 사용 |
| 2 | **Thread Starvation** | `MetadataRefreshScheduler.kt:39,65` | 85% | `runBlocking`이 스케줄러 스레드 차단. 장시간 작업 시 다른 스케줄 작업 지연 |
| 3 | **Blocking Reactive** | `YouTubeOEmbedClientImpl.kt:57` | 80% | WebClient `.block()` 사용으로 reactive 이점 상실. coroutine과 혼용 시 deadlock 위험 |
| 4 | **JPA Entity 설계** | `StreamingEvent.kt` | 82% | `equals()`/`hashCode()` 미구현. Hibernate 영속성 컨텍스트 오동작 가능 |

### 1.2 수정 방안

#### Issue #1: Transaction 관리 버그
```kotlin
// MetadataRefreshServiceImpl.kt:62
// @Transactional  // ❌ 제거 필요
override suspend fun refreshEvent(eventId: UUID): Boolean {
    // TransactionalMetadataUpdater가 이미 REQUIRES_NEW로 관리
    metadataUpdater.updateEventMetadata(event)
}
```

#### Issue #2: Thread Starvation
```kotlin
// MetadataRefreshScheduler.kt
// @Async("metadataRefreshExecutor") 추가하고
// runBlocking 제거
suspend fun refreshLiveMetadata() {
    val result = metadataRefreshService.refreshLiveEvents()
}

// MetadataRefreshConfig.kt에 Executor 추가
@Bean
fun metadataRefreshExecutor(): Executor {
    return ThreadPoolTaskExecutor().apply {
        corePoolSize = 2
        maxPoolSize = 4
        setThreadNamePrefix("metadata-refresh-")
    }
}
```

#### Issue #3: Blocking Reactive
```kotlin
// YouTubeOEmbedClientImpl.kt:57
// .block() 대신 awaitSingle() 사용
.bodyToMono(OEmbedResponse::class.java)
.timeout(Duration.ofMillis(timeoutMs))
.retryWhen(...)
.awaitSingle()  // ✅ Non-blocking suspend
```

#### Issue #4: JPA Entity 설계
```kotlin
// StreamingEvent.kt에 추가
override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is StreamingEvent) return false
    return id == other.id
}

override fun hashCode(): Int = id.hashCode()
```

### 1.3 아키텍처 강점

| 항목 | 평가 | 상세 |
|------|------|------|
| **DDD 적용** | ✅ 우수 | Ports & Adapters 패턴, 도메인 이벤트 모델링 |
| **레이어 분리** | ✅ 우수 | domain, application, infrastructure 명확 분리 |
| **보안** | ✅ 양호 | 환경변수로 credentials 관리, Circuit Breaker로 에러 격리 |
| **테스트** | ✅ 우수 | MockK, WireMock 활용, 통합 테스트 포함 |
| **설정 관리** | ✅ 양호 | Resilience4j 설정, Feature flag 적용 |

### 1.4 추가 권장 사항 (Lower Priority)

1. **DB 마이그레이션**: Flyway/Liquibase 미적용. `ddl-auto: validate`로 초기 스키마 생성 실패 위험
2. **페이지네이션**: `findByStatus()` 전체 로드. 대용량 시 메모리 이슈
3. **Admin API**: 메타데이터 갱신 수동 트리거 엔드포인트 없음
4. **Event Handler**: 도메인 이벤트 발행되나 `@EventListener` 핸들러 미구현

---

## 2. 문서 정합성 분석

### 2.1 문서 디렉토리 구조

```
doc/
├── 프로젝트 기획서.md                    # 전체 프로젝트 기획
├── 데이터베이스_정의서.md               # 전체 DB 정의 (35.7KB)
├── 화면_정의서.md                       # 전체 화면 정의 (68.5KB)
├── mvp/                                 # MVP(4주) 전용 문서
│   ├── mvp_기획서.md                   # MVP 목표, 범위, 스케줄
│   ├── mvp_PRD.md                      # MVP 제품 요구사항
│   ├── mvp_화면_정의서.md             # MVP 화면 (10개)
│   ├── mvp_API_명세서.md              # MVP API (⚠️ 미완성)
│   ├── mvp_데이터베이스_정의서.md     # MVP DB (6개 테이블)
│   └── mvp_백로그.md                  # MVP 기술 백로그
├── ddd/                                 # DDD 설계 문서
│   ├── domain-model.md                 # 10개 Bounded Context
│   ├── context-map.md                  # Context 관계도
│   ├── ubiquitous-language.md         # 194개 용어 정의
│   └── bounded-contexts/              # Context별 상세
└── claude_cc/                           # Claude Code 설정 문서
```

### 2.2 문서별 완성도

| 문서 | 완성도 | 평가 |
|------|--------|------|
| MVP 화면정의서 | 85% | 10개 화면 정의 완료 |
| MVP DB정의서 | 95% | 6개 테이블 완벽 정의 |
| DDD 문서 | 90% | 10개 Context, 194개 용어 |
| **MVP API 명세서** | **30%** | ❌ **심각한 미완성** |

### 2.3 화면-API-DB 정합성 매트릭스

```
Context        | 화면 | API | DB  | 정합성
---------------|------|-----|-----|--------
Identity       |      |     |     |
├─ 회원가입    | ✅   | ✅  | ✅  | ✅ 완전 일관
├─ 로그인      | ✅   | ✅  | ✅  | ✅ 완전 일관
├─ Google OAuth| ✅   | ❌  | ✅  | ⚠️ API 미정의
└─ 로그아웃    | ✅   | ❌  | ✅  | ❌ API 미정의

Streaming      |      |     |     |
├─ 라이브 목록 | ✅   | ❌  | ✅  | ❌ API 미정의
└─ 라이브 상세 | ✅   | ❌  | ✅  | ❌ API 미정의

Content        |      |     |     |
├─ 뉴스 목록   | ✅   | ❌  | ✅  | ❌ API 미정의
└─ 뉴스 상세   | ✅   | ❌  | ✅  | ❌ API 미정의

Search         |      |     |     |
└─ 통합 검색   | ✅   | ❌  | ❌  | ❌ 완전 미정의
```

### 2.4 MVP API 명세서 누락 엔드포인트

| 엔드포인트 | 메서드 | 설명 | 우선순위 |
|-----------|--------|------|---------|
| `/auth/google` | POST | Google OAuth 로그인 | High |
| `/auth/logout` | POST | 로그아웃 | High |
| `/api/v1/streaming-events` | GET | 라이브 목록 조회 | High |
| `/api/v1/streaming-events/{id}` | GET | 라이브 상세 조회 | High |
| `/news` | GET | 뉴스 목록 조회 | High |
| `/news/{id}` | GET | 뉴스 상세 조회 | High |
| `/search` | GET | 통합 검색 | High |

---

## 3. GitHub 이슈 분석

### 3.1 현재 상태

- **GitHub CLI 인증**: 미완료 (`gh auth login` 필요)
- **저장소**: `jskjw157/FanPulse`

### 3.2 권장 마일스톤 구조 (MVP 4주)

| 마일스톤 | 기간 | 목표 |
|---------|------|------|
| Sprint 1: Skeleton + Contract | Week 1 | 프로젝트 기본 구조, API 계약 정의 |
| Sprint 2: Auth E2E | Week 2 | 인증 기능 완성 (이메일 + Google) |
| Sprint 3: Live/News E2E | Week 3 | 라이브/뉴스 기능 + Seed 데이터 |
| Sprint 4: QA + Release | Week 4 | 로컬 저장 + QA + 배포 |

### 3.3 권장 라벨 체계

**플랫폼 라벨**:
- `platform:web` (#0366d6)
- `platform:android` (#3DDC84)
- `platform:ios` (#000000)
- `platform:backend` (#d73a4a)
- `platform:devops` (#fbca04)

**타입 라벨**:
- `type:feature` (#a2eeef)
- `type:bug` (#d73a4a)
- `type:enhancement` (#84b6eb)
- `type:infrastructure` (#fbca04)

**우선순위 라벨**:
- `priority:high` (#d93f0b)
- `priority:medium` (#fbca04)
- `priority:low` (#0e8a16)

**카테고리 라벨**:
- `category:auth` (#c5def5)
- `category:live` (#f9d0c4)
- `category:news` (#fef2c0)
- `category:search` (#bfdadc)

### 3.4 권장 이슈 수 (총 42개)

| Sprint | 이슈 수 | 주요 내용 |
|--------|--------|----------|
| Sprint 1 | 6개 | 프로젝트 구조 (Backend/Web/iOS/Android), 에러 페이지 |
| Sprint 2 | 12개 | 회원가입/로그인 API, 인증 화면, Google OAuth, 마이페이지 |
| Sprint 3 | 15개 | 라이브/뉴스 API, 홈/목록/상세 화면, 검색, Seed 데이터 |
| Sprint 4 | 7개 | 로컬 저장, 배포 환경, QA |
| Stretch | 2개 | RSS 크롤링, YouTube 메타데이터 갱신 |

---

## 4. 종합 Action Items

### 4.1 즉시 해결 (Week 1)

**백엔드 Critical 이슈**:
- [ ] `MetadataRefreshServiceImpl.kt:62` - `@Transactional` 제거
- [ ] `YouTubeOEmbedClientImpl.kt:57` - `.block()` → `.awaitSingle()` 변경
- [ ] `StreamingEvent.kt` - `equals()`/`hashCode()` 구현
- [ ] `MetadataRefreshScheduler.kt` - 별도 thread pool 구성

**문서 완성**:
- [ ] MVP API 명세서 - 7개 엔드포인트 추가
  - `POST /auth/google`
  - `POST /auth/logout`
  - `GET /api/v1/streaming-events`, `GET /api/v1/streaming-events/{id}`
  - `GET /api/v1/news`, `GET /api/v1/news/{id}`
  - `GET /api/v1/search`

**GitHub 설정**:
- [ ] `gh auth login` 실행
- [ ] 라벨 생성 (17개)
- [ ] 마일스톤 생성 (4개)
- [ ] 이슈 생성 (42개)

### 4.2 중기 (Week 2-3)

- [ ] DB 마이그레이션 스크립트 (Flyway/Liquibase)
- [ ] 대용량 조회 페이지네이션 추가
- [ ] Admin API 엔드포인트 추가
- [ ] Domain Event Handler 구현
- [ ] Search Context DB 스키마 정의

### 4.3 장기 (Week 4+)

- [ ] Event Storming 문서 완성
- [ ] Seed 데이터 적재 스크립트 가이드
- [ ] 클라이언트별 임베드 구현 가이드

---

## 5. 프로젝트 건강도 요약

| 영역 | 점수 | 평가 |
|------|------|------|
| 아키텍처 설계 | ⭐⭐⭐⭐ (4/5) | DDD + Hexagonal 잘 적용 |
| 코드 품질 | ⭐⭐⭐ (3/5) | 4개 Critical 이슈 해결 필요 |
| 문서화 | ⭐⭐⭐ (3/5) | API 명세서 30% 완성 |
| 테스트 커버리지 | ⭐⭐⭐⭐ (4/5) | 포괄적 테스트 존재 |
| 이슈 관리 | ⭐⭐ (2/5) | 체계적 이슈 생성 필요 |

**종합 평가**: MVP 출시 전 백엔드 Critical 이슈 해결과 API 명세서 완성이 필수. GitHub 이슈 체계 구축으로 프로젝트 가시성 확보 필요.

---

## 부록: 분석에 사용된 에이전트

| 에이전트 | ID | 용도 |
|---------|-----|------|
| feature-dev:code-reviewer | a4a930e | 백엔드 코드 리뷰 |
| github-issues | aea6746 | GitHub 이슈/마일스톤 분석 |
| Explore | a62c6e0 | 문서 구조 탐색 |

---

*Report generated by Claude Code Architecture Review*
