# Implementation Plan: News Sync Batch (crawled_news → news)

**Status**: 📝 Draft (승인 대기)
**Issue**: [#272](https://github.com/jskjw157/FanPulse/issues/272)
**Branch**: `feature/272-news-sync-batch`
**Ready Date**: 2026-04-27
**Last Updated**: 2026-04-27 (Phase 1 완료)
**Estimated Completion**: 2026-05-02 (약 10-14h)
**Scope Size**: Medium (5 phases)
**Related Learning**: PR #238 / PR #271 / Issue #166, #224 — 코루틴·트랜잭션 경계 분리 패턴(REQUIRES_NEW + 동기 위임)을 본 플랜에도 동일 적용

---

**⚠️ CRITICAL INSTRUCTIONS**: After completing each phase:
1. ✅ Check off completed task checkboxes
2. 🧪 Run all quality gate validation commands
3. ⚠️ Verify ALL quality gate items pass
4. 📅 Update "Last Updated" date above
5. 📝 Document learnings in Notes section
6. ➡️ Only then proceed to next phase

⛔ **DO NOT skip quality gates or proceed with failing checks**

---

## 📋 Overview

### Feature Description
현재 FanPulse 웹에 노출되는 뉴스 15건은 모두 `n.news.naver.com/aespa-1` 같은 **가짜 URL**로 수동 insert 된 데모 데이터다. 사용자가 뉴스 카드를 클릭하면 404가 나오고 즉시 이탈한다. 한편 Django 크롤러(`ai/api/services/news_crawler.py`)는 실제 Naver Open API로 `crawled_news` 테이블을 채우고 있으나(44건), Spring이 노출하는 `news` 테이블과는 **완전히 분리**되어 있다.

이 플랜은 **Spring 측에 `@Scheduled` 배치 Job을 추가**해서:
1. Django가 넣은 `crawled_news` 레코드를 주기적으로 읽고
2. 아티스트 이름(한/영/멤버) 매칭으로 `artist_id`를 부여하고
3. 키워드 룰로 `NewsCategory`를 분류하고
4. `news` 테이블에 upsert (source_url 유니크)

함으로써, 사용자에게 **실제 클릭 가능한 Naver 뉴스**가 노출되도록 한다.

### Success Criteria
- [ ] 배치가 10분 주기로 실행되며 ShedLock으로 다중 인스턴스 동시 실행 차단
- [ ] 기존 Naver `crawled_news` 44건 중 **매칭 성공 건** 모두 `news` 테이블로 이관
- [ ] 프로덕션 `GET /api/v1/news` 응답에 실제 클릭 가능한 Naver URL 노출 (404 없음)
- [ ] 동일 `crawled_news.url` 재실행 시 중복 insert 없음 (idempotent)
- [ ] 배치 실패 시 Spring이 계속 기동 (Fail-Open, 기존 `news` 데이터 유지)
- [ ] 기존 `news` 테이블의 fake URL 15건 제거 또는 hide 처리
- [ ] `docs/mvp/mvp_크롤링.md` 문서가 실제 구현과 일치하도록 갱신

### User Impact
- **사용자**: 뉴스 카드 클릭 시 실제 기사로 이동 (현재는 404)
- **PM/기획**: "사용자 반응"을 실데이터 기반으로 측정 가능 (CTR, 체류시간)
- **운영**: 시드 JSON 편집 없이 Django 크롤러만 돌리면 뉴스가 자동 갱신됨

---

## 🏗️ Architecture Decisions

| Decision | Rationale | Trade-offs |
|----------|-----------|------------|
| **Spring에서 `crawled_news` 읽기, Django는 건드리지 않음** | Django는 AI Sidecar 전용 역할 유지 (MEMORY.md 원칙). 변환 로직은 비즈니스 규칙이므로 Spring 소유가 맞음 | Spring ↔ DB 스키마 커플링 증가. Django가 `crawled_news` 스키마 변경 시 Spring도 수정 필요 → Flyway 마이그레이션 검토 체크리스트 추가 |
| **JPA `@Immutable` 읽기 전용 Entity로 `crawled_news` 매핑** | Django는 `crawled_news`의 writer, Spring은 reader. 혼동 방지 | Entity가 2 layer(domain + infra)로 나뉘면 과한 boilerplate. `infra`에만 두고 서비스에서 직접 사용 |
| **매칭 규칙: 아티스트 한글/영문/멤버명이 title OR content에 포함** | 간단한 String.contains 룰. 머신러닝/임베딩 불필요. MVP 적합 | 오탐 가능 (예: "New Jeans"가 일반 표현일 때). 1차 대응: 매칭 우선순위 = englishName > 한글명 > 멤버명. 2차: 로그로 오탐 사례 수집 |
| **카테고리 분류: 키워드 룰 엔진** | MVP 범위, 유지보수 쉬움. `["발매","release","앨범"] → RELEASE` 식 매핑 | 신규 키워드 추가 시 코드 수정 필요. fallback = GENERAL |
| **중복 방지: `news.source_url` 유니크 제약 + `findBySourceUrl` 조회 후 insert** | 이미 `NewsPort.findBySourceUrl`가 존재. 유니크 제약은 DB 레벨에서도 검증됨 | Race condition 상황에서 충돌 가능 → try/catch로 duplicate key 무시 |
| **증분 처리: `published_at` 워터마크 (lastSyncedAt)** | 전체 테이블 full scan 방지. ShedLock 메타 테이블 or 별도 `sync_cursor` 테이블 | 단순성 위해 **첫 버전은 Full scan + findBySourceUrl 체크**로 시작 (44건 규모). 10k 건 넘어갈 때 워터마크 도입 |
| **스케줄 주기: 10분 (cron: `0 */10 * * * *`)** | Naver 뉴스 신선도 + DB 부하 균형. Django 크롤러 주기와 맞춤 | 너무 짧으면 불필요한 쿼리. 설정으로 외부화 |
| **Feature flag: `fanpulse.scheduler.news-sync.enabled`** | 기존 LiveDiscoveryScheduler 패턴 동일. 프로덕션 문제 시 즉시 off | 개발/CI 환경에서 기본 off여야 test 간섭 없음 |
| **실패 정책: Fail-Open (에러는 log + metric, Spring 기동 유지)** | MEMORY.md 원칙(Moderation/Summarizer와 동일). 배치 실패가 전체 서비스를 멈추면 안 됨 | 조용히 실패할 수 있음 → Micrometer metric + 알림 필수 |

### Schema Mapping Table

| `crawled_news` (Django) | `news` (Spring) | 변환 로직 |
|-------------------------|-----------------|-----------|
| `id` (bigint) | — | 사용 안 함 (Spring은 UUID 사용) |
| `title` | `title` | 그대로 복사 (length 500 truncate) |
| `content` or `origin_news` | `content` | `content` nullable → content/origin_news/title 순 fallback |
| `url` | `source_url` | **매칭 키 (유니크)** |
| `source` | `source_name` | null 시 "Naver" fallback, length 100 truncate |
| `thumbnail_url` | `thumbnailUrl` (setter) | 그대로 |
| `published_at` | `publishedAt` | null 시 `created_at` fallback |
| — | `artistId` | **매칭 로직으로 산출** |
| — | `category` | **분류 로직으로 산출** |
| — | `visible` | 기본 true |

---

## 📦 Dependencies

### Required Before Starting
- [x] Postgres `crawled_news` 테이블 존재 (Django 마이그레이션 완료)
- [x] Postgres `news` 테이블 존재 (Flyway V1 이후)
- [x] Postgres `artists` 테이블에 Naver API 매칭 대상 아티스트 데이터 존재
- [x] Spring `@EnableScheduling` 활성화 (LiveDiscoveryScheduler 이미 동작 중이므로 확인됨)
- [x] ShedLock dependency (기존 사용 중)

### External Dependencies
- Spring Boot 3.x + Kotlin (현재)
- `net.javacrumbs.shedlock:shedlock-spring` (현재)
- `io.github.microutils:kotlin-logging-jvm` (현재)
- Testcontainers `org.testcontainers:postgresql` (기존 사용 중 확인 필요)

---

## 🧪 Test Strategy

### Testing Approach
**TDD Principle**: 각 Phase 에서 production 코드 전에 실패하는 테스트부터 작성. 기존 FanPulse 패턴(JUnit5 + MockK + Testcontainers + JaCoCo) 재사용.

### Test Pyramid for This Feature
| Test Type | Coverage Target | Purpose |
|-----------|-----------------|---------|
| **Unit Tests** | ≥90% | 매칭/분류 도메인 로직, Sync use case (mocked ports) |
| **Integration Tests** | Critical paths | JPA Entity/Adapter (`@DataJpaTest` + Testcontainers), Scheduler 실제 배치 1회 |
| **E2E Tests** | 1 path | Docker Compose 스택에서 Django crawler → Spring batch → `GET /api/v1/news` 연속 검증 (수동 smoke) |

### Test File Organization
```
backend/src/test/kotlin/com/fanpulse/
├── domain/content/
│   ├── NewsMatcherTest.kt              (Phase 1: unit)
│   └── NewsCategoryClassifierTest.kt   (Phase 1: unit)
├── infrastructure/persistence/content/
│   └── CrawledNewsAdapterTest.kt       (Phase 2: @DataJpaTest + Testcontainers)
├── application/service/content/
│   └── NewsSyncServiceImplTest.kt      (Phase 3: unit with mockk)
└── infrastructure/scheduler/
    └── NewsSyncSchedulerTest.kt        (Phase 4: unit with mockk)
```

### Coverage Requirements by Phase
- **Phase 1 (도메인 로직)**: ≥90% — 순수 Kotlin, edge case 테스트 용이
- **Phase 2 (Infra read)**: ≥80% — Testcontainers 통합
- **Phase 3 (Use case)**: ≥90% — 상태 없는 서비스, mock 용이
- **Phase 4 (Scheduler)**: ≥70% — 실행 트리거만 검증 (로직은 Phase 3에서 커버)
- **Phase 5 (Cleanup/Docs)**: 코드 변경 없음 — manual checklist

### Test Naming Convention (프로젝트 기존 컨벤션 따름)
```kotlin
// Kotlin + JUnit5 backticks
class NewsMatcherTest {
    @Test
    fun `한글 아티스트명이 title에 포함되면 매칭된다`() { ... }

    @Test
    fun `englishName이 content에 포함되면 매칭된다`() { ... }

    @Test
    fun `복수 아티스트 매칭 시 모두 반환한다`() { ... }
}
```

---

## 🚀 Implementation Phases

### Phase 1: 매칭/분류 도메인 로직
**Goal**: 순수 Kotlin 도메인 클래스 2종 (`NewsMatcher`, `NewsCategoryClassifier`). DB 의존 없이 완전 단위 테스트 가능.
**Estimated Time**: 2h
**Status**: ✅ Completed (2026-04-27)

#### Tasks

**🔴 RED: Write Failing Tests First**
- [x] **Test 1.1**: `NewsMatcherTest.kt` 작성
  - File: `backend/src/test/kotlin/com/fanpulse/domain/content/NewsMatcherTest.kt`
  - Expected: 컴파일 실패 (NewsMatcher 클래스 없음)
  - Test cases:
    - `한글 아티스트명이 title에 포함되면 해당 아티스트 반환`
    - `영문 아티스트명이 content에 포함되면 해당 아티스트 반환`
    - `멤버명이 title에 포함되면 아티스트 반환`
    - `복수 아티스트 매칭 시 모두 반환`
    - `어떤 아티스트도 매칭 안 되면 빈 리스트 반환`
    - `대소문자 무시 매칭 (aespa vs AESPA vs aespa)`
    - `띄어쓰기 차이 무시 (New Jeans vs NewJeans vs newjeans)`
    - `부분 문자열이 단어 경계에 걸치면 매칭 (주의: "aespa의" 는 매칭, "kespace" 는 매칭 안 됨 — tokenization 정책 문서화)`
    - `비활성(active=false) 아티스트는 매칭에서 제외`

- [x] **Test 1.2**: `NewsCategoryClassifierTest.kt` 작성
  - File: `backend/src/test/kotlin/com/fanpulse/domain/content/NewsCategoryClassifierTest.kt`
  - Expected: 컴파일 실패 (NewsCategoryClassifier 없음)
  - Test cases:
    - `title에 "발매" 포함 시 RELEASE`
    - `title에 "release" 포함 시 RELEASE`
    - `title에 "콘서트" 포함 시 TOUR`
    - `title에 "tour" 포함 시 TOUR`
    - `title에 "시상식" 포함 시 AWARD`
    - `title에 "award" 포함 시 AWARD`
    - `title에 "예능" 포함 시 VARIETY`
    - `title에 "instagram" 포함 시 SOCIAL_MEDIA`
    - `title에 "collab" 포함 시 COLLABORATION`
    - `매칭 키워드 없으면 GENERAL`
    - `복수 키워드 충돌 시 우선순위 (RELEASE > TOUR > AWARD > ...)`
    - `title 우선, content 보조`

**🟢 GREEN: Implement to Make Tests Pass**
- [x] **Task 1.3**: `NewsMatcher` 구현
  - File: `backend/src/main/kotlin/com/fanpulse/domain/content/NewsMatcher.kt`
  - Interface:
    ```kotlin
    class NewsMatcher {
        fun match(title: String, content: String?, artists: List<Artist>): List<Artist>
    }
    ```
  - Logic: normalize(소문자, 공백 제거) → 각 artist의 name/englishName/members 각각 동일 normalize → `contains` 체크

- [x] **Task 1.4**: `NewsCategoryClassifier` 구현
  - File: `backend/src/main/kotlin/com/fanpulse/domain/content/NewsCategoryClassifier.kt`
  - Interface:
    ```kotlin
    object NewsCategoryClassifier {
        fun classify(title: String, content: String?): NewsCategory
    }
    ```
  - Logic: 우선순위 순서대로 키워드 맵 순회 → 첫 매칭 반환 → 없으면 `GENERAL`

**🔵 REFACTOR: Clean Up Code**
- [x] **Task 1.5**: Refactor
  - normalize 함수를 `private fun` 추출
  - 키워드 맵을 `companion object`의 `val` 상수로
  - KDoc 한국어로 작성 (MEMORY.md 원칙)
  - 테스트 실행 후 커버리지 ≥90% 확인

#### Quality Gate ✋

**⚠️ STOP: Do NOT proceed to Phase 2 until ALL checks pass**

**TDD Compliance**:
- [ ] Red 단계 commit에 실패 테스트가 기록됨
- [ ] 테스트가 먼저 실패했다가 구현 후 green 됨
- [ ] JaCoCo 커버리지 리포트에서 해당 클래스 ≥90%

**Build & Tests**:
- [ ] `./gradlew :backend:compileKotlin` 성공
- [ ] `./gradlew :backend:test --tests "*NewsMatcher*" --tests "*NewsCategoryClassifier*"` 전부 pass

**Code Quality**:
- [ ] ktlint 통과
- [ ] KDoc 한국어로 작성됨 (영어 없음)

**Validation Commands**:
```bash
cd backend
./gradlew compileKotlin compileTestKotlin
./gradlew test --tests "com.fanpulse.domain.content.NewsMatcherTest" --tests "com.fanpulse.domain.content.NewsCategoryClassifierTest"
./gradlew jacocoTestReport
open build/reports/jacoco/test/html/index.html
./gradlew ktlintCheck
```

**Manual Test Checklist**:
- [ ] REPL/main에서 `NewsMatcher.match("aespa 신곡 발매", null, [aespaArtist])` 호출 → aespa 반환 확인

---

### Phase 2: `crawled_news` 읽기 전용 Infra
**Goal**: Django가 쓰고 있는 `crawled_news` 테이블을 Spring에서 **읽기 전용**으로 접근할 수 있는 Entity/Repository/Port.
**Estimated Time**: 2h
**Status**: ⏳ Pending
**Dependencies**: Phase 1 완료

#### Tasks

**🔴 RED: Write Failing Tests First**
- [ ] **Test 2.1**: `CrawledNewsAdapterTest.kt` 작성 (integration)
  - File: `backend/src/test/kotlin/com/fanpulse/infrastructure/persistence/content/CrawledNewsAdapterTest.kt`
  - Framework: `@DataJpaTest` + Testcontainers Postgres
  - Expected: 컴파일 실패 (CrawledNewsAdapter 없음)
  - Test cases:
    - `crawled_news 테이블에 insert된 row를 findAll로 조회할 수 있다`
    - `published_at 기준 내림차순 정렬로 조회`
    - `페이지네이션 동작 확인 (limit/offset)`
    - `빈 테이블에서 빈 리스트 반환`
  - Setup: `@Sql` 또는 native JDBC로 테스트 데이터 삽입 (Django 엔티티 없이)

**🟢 GREEN: Implement to Make Tests Pass**
- [ ] **Task 2.2**: `CrawledNews` JPA Entity (읽기 전용)
  - File: `backend/src/main/kotlin/com/fanpulse/infrastructure/persistence/content/CrawledNewsEntity.kt`
  - `@Entity`, `@Immutable`, `@Table(name = "crawled_news")`
  - 필드: id, title, content, originNews, thumbnailUrl, url, source, publishedAt, createdAt, updatedAt
  - ⚠️ **Spring이 이 테이블에 쓰지 않도록 save() 금지** — Repository는 read-only 메소드만

- [ ] **Task 2.3**: `CrawledNewsJpaRepository`
  - File: `backend/src/main/kotlin/com/fanpulse/infrastructure/persistence/content/CrawledNewsJpaRepository.kt`
  - `JpaRepository<CrawledNewsEntity, Long>` — but only `findAll(Pageable)` 노출 위해 Custom interface 검토
  - 메소드:
    - `findAllByOrderByPublishedAtDesc(pageable: Pageable): Page<CrawledNewsEntity>`

- [ ] **Task 2.4**: `CrawledNewsReader` Port + Adapter
  - Port: `backend/src/main/kotlin/com/fanpulse/domain/content/port/CrawledNewsReader.kt`
    ```kotlin
    interface CrawledNewsReader {
        fun findRecent(limit: Int): List<CrawledNewsSnapshot>
    }
    /**
     * Django 측 crawled_news 1행을 Spring 도메인으로 옮기기 위한 읽기 전용 스냅샷.
     *
     * Timezone 정책:
     * - Django는 USE_TZ=True 가정 → DB에는 UTC로 저장됨 (TIMESTAMP WITH TIME ZONE)
     * - 따라서 publishedAt 은 UTC 기준 Instant 로 직접 매핑 (별도 변환 X)
     * - 만약 Django settings.TIME_ZONE 이 변경되면 V119 마이그레이션 사전 점검 필수
     */
    data class CrawledNewsSnapshot(
        val title: String,
        val content: String?,
        val url: String,
        val source: String?,
        val thumbnailUrl: String?,
        val publishedAt: Instant?  // UTC 기준 Instant
    )
    ```
  - Adapter: `infrastructure/persistence/content/CrawledNewsAdapter.kt`
    - `@Component` + `@Transactional(readOnly = true)`
    - Entity → Snapshot 매핑

**🔵 REFACTOR: Clean Up Code**
- [ ] **Task 2.5**: Refactor
  - Snapshot → 도메인 값 객체, Entity는 infra에만 노출되게 격리
  - KDoc 한국어
  - `Snapshot`에 `toNewsCreatable()` helper 함수 검토 (Phase 3 용)

#### Quality Gate ✋

**⚠️ STOP: Do NOT proceed to Phase 3 until ALL checks pass**

**TDD Compliance**:
- [ ] Red 단계 commit 존재
- [ ] Testcontainers Postgres 기동 확인
- [ ] 커버리지 CrawledNewsAdapter ≥80%

**Build & Tests**:
- [ ] `./gradlew :backend:test --tests "*CrawledNewsAdapter*"` pass
- [ ] Postgres Testcontainer 가 실제로 뜨고 내려감 (로그 확인)

**Persistence Safety** (중요):
- [ ] `grep -r "crawledNewsRepository.save" backend/src/main` → **0 matches**
- [ ] `@Immutable` annotation 붙어있음
- [ ] Spring application 기동 시 Hibernate schema validation 통과 (`spring.jpa.hibernate.ddl-auto=validate` 로컬 테스트)

**Code Quality**:
- [ ] ktlint 통과
- [ ] KDoc 한국어

**Validation Commands**:
```bash
cd backend
./gradlew test --tests "com.fanpulse.infrastructure.persistence.content.CrawledNewsAdapterTest"
./gradlew jacocoTestReport
grep -rn "crawledNewsRepository.save\|crawledNewsRepository.delete\|crawledNewsRepository.deleteAll" src/main
# 결과: no matches expected
```

**Manual Test Checklist**:
- [ ] 로컬 Docker Compose로 Postgres + Django 기동 → Django 크롤러 1회 실행 → Spring에서 CrawledNewsReader.findRecent(50) 호출 (임시 REST endpoint or IntegrationTest) → 실제 row 반환 확인

---

### Phase 3: NewsSyncService (Application)
**Goal**: `CrawledNewsReader` → `NewsMatcher` → `NewsCategoryClassifier` → `NewsPort.save` 흐름을 오케스트레이션하는 use case.
**Estimated Time**: 3h
**Status**: ⏳ Pending
**Dependencies**: Phase 1 & 2 완료

#### 🔑 트랜잭션 경계 설계 결정 (PR #238 / #224 학습 반영)

배치는 **여러 건을 처리하는 동안 한 건이 실패해도 나머지는 살려야** 한다 (부분 성공). 이를 위해:

1. **`NewsSyncServiceImpl`에는 `@Transactional` 붙이지 않는다** (감싸면 1건 실패 시 전체 롤백).
2. **새 동기 컴포넌트 `TransactionalNewsUpserter`를 도입**해 1건 단위 upsert를 별도 트랜잭션으로 격리한다.
   - `@Component`
   - `upsert(...)` 메소드에 `@Transactional(propagation = Propagation.REQUIRES_NEW)` 적용
   - 자기 호출(self-invocation) 회피를 위해 반드시 **별도 클래스**여야 함 (`MetadataRefreshServiceImpl` ↔ `TransactionalMetadataUpdater` 패턴 그대로).
3. `NewsSyncServiceImpl`은 try/catch로 1건 실패를 흡수하고 다음 항목 계속 처리.

> 참고: `docs/architecture/coroutine-transactional.md` (PR #271) — suspend 함수가 아닌 일반 메소드라도 "배치 부분 성공"이 필요한 곳에서는 동일하게 `REQUIRES_NEW + 동기 위임` 패턴을 권장.

#### Tasks

**🔴 RED: Write Failing Tests First**
- [ ] **Test 3.1**: `NewsSyncServiceImplTest.kt` 작성
  - File: `backend/src/test/kotlin/com/fanpulse/application/service/content/NewsSyncServiceImplTest.kt`
  - Framework: JUnit5 + MockK (ports mock)
  - Expected: 컴파일 실패 (NewsSyncService 인터페이스 없음)
  - Test cases:
    - `crawled_news 1건 → 매칭 아티스트 1명 → news 1건 insert`
    - `crawled_news 1건 → 매칭 아티스트 2명 → news 2건 insert (각 아티스트별)`
    - `crawled_news 1건 → 매칭 아티스트 0명 → insert 안 함 + skipped 카운트 증가`
    - `동일 (source_url, artist_id) 가 이미 news 테이블에 있으면 skip (idempotent)`
    - `복수 아티스트 매칭 중 일부가 이미 존재하면 나머지만 insert`
    - `분류기가 반환한 category가 News에 반영됨`
    - `thumbnail_url null 허용`
    - `content null → title을 content로 fallback (News는 content not null)`
    - `publishedAt null → createdAt fallback`
    - `TransactionalNewsUpserter.upsert 예외 발생 시 전체 배치 중단하지 않고 다음 항목 처리 (Fail-Open 원칙)`
    - `sync 결과 리포트 반환: total, inserted, skipped, failed`
    - 🆕 `findBySourceUrlIn 으로 일괄 조회되어 N+1 쿼리가 발생하지 않는다 (verify 호출 횟수 = 1)`
    - 🆕 `artists 는 배치 시작 시 1회만 로드된다 (artistPort.findAllActive 호출 횟수 = 1)`

- [ ] **Test 3.2**: `TransactionalNewsUpserterTest.kt` 작성 🆕
  - File: `backend/src/test/kotlin/com/fanpulse/application/service/content/TransactionalNewsUpserterTest.kt`
  - Framework: `@SpringBootTest` + Testcontainers Postgres (실제 트랜잭션 격리 검증)
  - Test cases:
    - `1건 upsert 성공 시 트랜잭션 커밋된다`
    - `중간에 1건이 예외를 던져도 앞서 커밋된 건은 살아있다 (REQUIRES_NEW 격리)`
    - `unique 제약 위반 시 DataIntegrityViolationException 을 던진다 (caller 가 흡수)`

- [ ] **Test 3.3**: 로그/메트릭 관찰 테스트 (선택적 통합)
  - 확인: `logger.info { "News sync completed: ..." }` 호출 검증
  - MockK `spyk` 또는 LogCaptor 사용

**🟢 GREEN: Implement to Make Tests Pass**
- [ ] **Task 3.4**: `NewsSyncService` 인터페이스
  - File: `backend/src/main/kotlin/com/fanpulse/application/service/content/NewsSyncService.kt`
  - Interface:
    ```kotlin
    interface NewsSyncService {
        fun syncRecent(limit: Int = 100): NewsSyncReport
    }
    data class NewsSyncReport(
        val total: Int,
        val inserted: Int,
        val skipped: Int,
        val failed: Int,
        val errors: List<String>
    )
    ```

- [ ] **Task 3.5**: `TransactionalNewsUpserter` 🆕 (PR #238/#224 패턴 동일 적용)
  - File: `backend/src/main/kotlin/com/fanpulse/application/service/content/TransactionalNewsUpserter.kt`
  - 1건 단위 upsert를 **별도 트랜잭션**으로 격리하는 동기 컴포넌트.
    ```kotlin
    /**
     * 1건의 News upsert를 REQUIRES_NEW 트랜잭션에서 처리한다.
     *
     * NewsSyncServiceImpl 이 배치 루프를 도는 동안 한 건이 실패해도
     * 다른 건이 살아남도록 격리하기 위함. 자기 호출(self-invocation)
     * 회피를 위해 NewsSyncServiceImpl 과 반드시 별도 클래스여야 한다.
     *
     * 참고: docs/architecture/coroutine-transactional.md (PR #271)
     */
    @Component
    class TransactionalNewsUpserter(
        private val newsPort: NewsPort,
    ) {
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        fun upsert(news: News): UpsertOutcome {
            // findBySourceUrlAndArtistId 로 중복 검사 후 save
            // (Phase 3 N+1 회피 일괄 조회와 별개로, 트랜잭션 내 race 방지용 최종 가드)
        }
    }

    enum class UpsertOutcome { INSERTED, SKIPPED_DUPLICATE }
    ```

- [ ] **Task 3.6**: `NewsSyncServiceImpl` (오케스트레이션, 트랜잭션 없음)
  - File: `backend/src/main/kotlin/com/fanpulse/application/service/content/NewsSyncServiceImpl.kt`
  - ⚠️ **`@Transactional` 붙이지 않는다** — 1건 실패가 전체 롤백 되지 않도록.
  - Dependencies: `CrawledNewsReader`, `ArtistPort`, `NewsPort`, `NewsMatcher`, `NewsCategoryClassifier`, `TransactionalNewsUpserter`
  - Flow:
    1. `artists = artistPort.findAllActive(...)` — **배치 시작 시 1회만 로드**.
       - ⚠️ 신규/비활성 아티스트는 **다음 사이클(최대 10분 지연)에 반영**됨. 의도된 동작 — 배치 1회 동안의 일관성 확보가 우선.
    2. `snapshots = crawledNewsReader.findRecent(limit)`
    3. 🆕 **N+1 회피 일괄 조회**: `existing = newsPort.findBySourceUrlIn(snapshots.map { it.url })` — 1쿼리로 기존 (source_url, artist_id) 셋을 메모리에 적재. 이후 모든 중복 검사는 in-memory `Set<Pair<String, UUID>>` 으로 처리.
    4. for each snapshot:
       - `matched = newsMatcher.match(snapshot.title, snapshot.content, artists)`
       - if matched.isEmpty → skipped++, continue
       - for each artist in matched:
         - if `(snapshot.url, artist.id) in existingKeys` → skipped++, continue
         - try: `transactionalNewsUpserter.upsert(News.create(...))` → outcome 따라 inserted/skipped++
         - catch DataIntegrityViolationException (race condition) → skipped++
         - catch other → log + failed++
    5. return `NewsSyncReport`
  - ⚠️ 복수 아티스트 매칭 시 **동일 source_url이 아티스트마다 1건씩 저장**되는 현상 → 유니크 제약을 `(source_url, artist_id)` 복합으로 해야 함. Flyway 마이그레이션 Task 3.9 에 포함.

- [ ] **Task 3.7**: `NewsPort.findBySourceUrlIn` 메소드 추가 🆕
  - File: `backend/src/main/kotlin/com/fanpulse/domain/content/port/NewsPort.kt` (interface)
  - 시그니처: `fun findBySourceUrlIn(sourceUrls: Collection<String>): List<News>`
  - 기존 `findBySourceUrl(url)` 은 유지 (다른 호출자 영향 X)
  - Adapter 구현: JPA Query Method `findAllBySourceUrlIn` 추가

- [ ] **Task 3.8**: Flyway 마이그레이션 사전 체크 (Pre-flight) 🆕 — V119 작성 전 필수
  - **체크 1: 기존 유니크 제약/인덱스 이름 확인**
    ```sql
    SELECT conname, pg_get_constraintdef(oid)
    FROM pg_constraint
    WHERE conrelid = 'news'::regclass AND contype IN ('u', 'p');
    SELECT indexname, indexdef FROM pg_indexes
    WHERE tablename = 'news' AND indexdef ILIKE '%source_url%';
    ```
  - **체크 2 🆕: 기존 데이터에 `artist_id NULL` 인 row 가 있는지** (NULL 이 있으면 복합 유니크 변경이 의도와 다르게 동작)
    ```sql
    SELECT COUNT(*) FROM news WHERE artist_id IS NULL;
    -- 0 이어야 함. 0 이 아니면 → fake 15건 정리(Phase 5)를 먼저 진행하거나
    -- artist_id NOT NULL 제약을 V119 에 함께 포함시킬지 결정.
    ```
  - **체크 3 🆕: 기존 데이터 (source_url, artist_id) 중복 사전 검증**
    ```sql
    SELECT source_url, artist_id, COUNT(*)
    FROM news WHERE artist_id IS NOT NULL
    GROUP BY 1, 2 HAVING COUNT(*) > 1;
    -- 0행이어야 V119 ADD CONSTRAINT 가 통과. 1행이라도 있으면
    -- 우선 dedupe DML(or hide)을 V119 앞에 배치.
    ```
  - 결과를 `.migration_preflight_notes.md` 에 기록 (gitignore 추가, 커밋 X)
  - 모든 체크 PASS 한 환경(로컬/dev/prod 각각)을 본문에 표로 기록 후 V119 작성 진행
  - 만약 이미 `(source_url, artist_id)` 복합 유니크가 존재하면 V119 skip (마이그레이션 파일 자체를 만들지 않음)

- [ ] **Task 3.9**: Flyway 마이그레이션 — 유니크 제약 변경
  - File: `backend/src/main/resources/db/migration/V119__news_source_url_artist_unique.sql`
  - 내용 (Task 3.8 결과 반영):
    ```sql
    -- 기존 source_url 단독 유니크가 있으면 DROP (제약명은 Task 3.8 결과로 치환)
    ALTER TABLE news DROP CONSTRAINT IF EXISTS news_source_url_key;
    ALTER TABLE news DROP CONSTRAINT IF EXISTS news_source_url_uniq;
    ALTER TABLE news DROP CONSTRAINT IF EXISTS uk_news_source_url;
    DROP INDEX IF EXISTS news_source_url_idx;
    DROP INDEX IF EXISTS idx_news_source_url;

    -- (source_url, artist_id) 복합 유니크 제약 추가
    ALTER TABLE news
      ADD CONSTRAINT news_source_url_artist_id_unique UNIQUE (source_url, artist_id);
    ```
  - ⚠️ Task 3.8 로 확인한 정확한 제약명을 우선 DROP 하되, 방어적으로 `IF EXISTS` 로 여러 후보를 나열한다.

**🔵 REFACTOR: Clean Up Code**
- [ ] **Task 3.10**: Refactor
  - 복잡한 for loop를 `snapshots.flatMap { snapshot -> matcher.match(...).map { snapshot to it } }` 형태로 재구성 검토
  - 로깅 레벨 검토: info (성공 개수), warn (실패 개수), debug (개별 스킵)
  - Transactional 경계: **per-snapshot** REQUIRES_NEW (Task 3.5 의 `TransactionalNewsUpserter`) 만 사용 — 1건 실패가 전체 롤백 안 되게

#### Quality Gate ✋

**⚠️ STOP: Do NOT proceed to Phase 4 until ALL checks pass**

**TDD Compliance**:
- [ ] 모든 11개 Test case 통과
- [ ] 커버리지 NewsSyncServiceImpl ≥90%

**Build & Tests**:
- [ ] `./gradlew :backend:test --tests "*NewsSyncService*"` pass
- [ ] Flyway V119 마이그레이션 로컬 Postgres에서 clean run (`./gradlew flywayMigrate` 가능하면)

**Data Integrity**:
- [ ] 기존 `news` 테이블에 중복 (source_url + artist_id) 없는지 검증 쿼리
  ```sql
  SELECT source_url, artist_id, COUNT(*) FROM news GROUP BY 1,2 HAVING COUNT(*) > 1;
  ```

**Code Quality**:
- [ ] ktlint 통과
- [ ] KDoc 한국어

**Validation Commands**:
```bash
cd backend
./gradlew test --tests "com.fanpulse.application.service.content.NewsSyncServiceImplTest"
./gradlew jacocoTestReport
# Flyway 검증 (로컬 Postgres)
./gradlew flywayValidate
```

**Manual Test Checklist**:
- [ ] 로컬 Postgres에 테스트 crawled_news 3건 삽입 → `NewsSyncService.syncRecent(10)` 수동 호출 → news 테이블 증가 확인
- [ ] 동일 syncRecent 재실행 → 신규 insert 0건 (idempotent 확인)

---

### Phase 4: Scheduler + 설정 + 운영 관찰
**Goal**: `NewsSyncService`를 cron으로 실행하는 스케줄러 추가. LiveDiscoveryScheduler 패턴 그대로.
**Estimated Time**: 2h
**Status**: ⏳ Pending
**Dependencies**: Phase 3 완료

#### Tasks

**🔴 RED: Write Failing Tests First**
- [ ] **Test 4.1**: `NewsSyncSchedulerTest.kt` 작성
  - File: `backend/src/test/kotlin/com/fanpulse/infrastructure/scheduler/NewsSyncSchedulerTest.kt`
  - Framework: JUnit5 + MockK
  - Expected: 컴파일 실패 (NewsSyncScheduler 없음)
  - Test cases (LiveDiscoverySchedulerTest 동일 스타일):
    - `syncNews() 호출 시 newsSyncService.syncRecent() 가 호출된다`
    - `syncRecent() 가 예외를 던져도 스케줄러는 예외를 흡수한다 (Fail-Open)`
    - `성공 시 로그에 total/inserted/skipped/failed가 기록된다`

**🟢 GREEN: Implement to Make Tests Pass**
- [ ] **Task 4.2**: `NewsSyncScheduler` 구현
  - File: `backend/src/main/kotlin/com/fanpulse/infrastructure/scheduler/NewsSyncScheduler.kt`
  - 템플릿: `LiveDiscoveryScheduler.kt` 100% 미러링
    ```kotlin
    @Component
    @ConditionalOnProperty(
        name = ["fanpulse.scheduler.news-sync.enabled"],
        havingValue = "true",
        matchIfMissing = false
    )
    class NewsSyncScheduler(
        private val newsSyncService: NewsSyncService
    ) {
        @Scheduled(cron = "\${fanpulse.scheduler.news-sync.cron:0 */10 * * * *}")
        @SchedulerLock(
            name = "newsSyncScheduler",
            lockAtMostFor = "9m",
            lockAtLeastFor = "1m"
        )
        fun syncNews() {
            val startTime = Instant.now()
            logger.info { "Starting news sync at $startTime" }
            try {
                val report = newsSyncService.syncRecent(limit = 100)
                val duration = Duration.between(startTime, Instant.now())
                logger.info {
                    "News sync completed in ${duration.toMillis()}ms: " +
                        "total=${report.total}, inserted=${report.inserted}, " +
                        "skipped=${report.skipped}, failed=${report.failed}"
                }
                if (report.errors.isNotEmpty()) {
                    logger.warn { "Sync errors: ${report.errors.take(5)}" }
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to run news sync" }
            }
        }
    }
    ```

- [ ] **Task 4.3**: `application.yml` 설정 추가
  - File: `backend/src/main/resources/application.yml`
  - 기존 `live-discovery` 블록 옆에:
    ```yaml
    fanpulse:
      scheduler:
        news-sync:
          enabled: true      # 프로덕션 default on
          cron: "0 */10 * * * *"  # 10분마다
    ```
  - `application-dev.yml` / test profile 에서는 `enabled: false`

- [ ] **Task 4.4**: 문서성 KDoc 한국어로 작성

**🟢 GREEN: Metric 계측 (필수) 🆕**
- [ ] **Task 4.4.5**: Micrometer 메트릭 추가 (Fail-Open 조용한 실패 방지)
  - File: `NewsSyncScheduler.kt` 또는 `NewsSyncServiceImpl.kt`
  - MeterRegistry 주입 후 아래 3개 Counter 등록:
    - `fanpulse.news_sync.inserted_total` — 신규 insert 건수
    - `fanpulse.news_sync.skipped_total` — 중복/미매칭 스킵
    - `fanpulse.news_sync.failed_total` — 변환/저장 실패 (tag: `reason=match|classify|persist`)
  - 1개 Gauge: `fanpulse.news_sync.last_run_epoch_seconds` — 마지막 성공 시각
  - **테스트**: `meterRegistry.counter(...).count()` 가 증가하는지 검증하는 unit test 1건 추가 (Test 4.1 에 포함)

**🔵 REFACTOR: Clean Up Code**
- [ ] **Task 4.5**: Refactor
  - LiveDiscoveryScheduler와 공통 부분이 눈에 띄면 `AbstractScheduledJob` 추출 검토 (but 과한 추상화면 skip)
  - Grafana/Alerting: 메트릭은 등록만 하고 대시보드/alert 룰은 **별도 PR** (이 플랜 범위 밖)

#### Quality Gate ✋

**⚠️ STOP: Do NOT proceed to Phase 5 until ALL checks pass**

**TDD Compliance**:
- [ ] Red 단계 commit 존재
- [ ] 3개 테스트 모두 pass

**Build & Tests**:
- [ ] `./gradlew :backend:test --tests "*NewsSyncScheduler*"` pass
- [ ] `./gradlew :backend:bootRun` 기동 시 로그에 `NewsSyncScheduler` 등록 확인
  ```
  Scheduled task registered: newsSyncScheduler
  ```

**Configuration Safety**:
- [ ] test profile 에서 `enabled: false` 확인 (CI 간섭 방지)
- [ ] `@ConditionalOnProperty matchIfMissing = false` → 설정 누락 시 off

**Code Quality**:
- [ ] ktlint 통과

**Validation Commands**:
```bash
cd backend
./gradlew test --tests "com.fanpulse.infrastructure.scheduler.NewsSyncSchedulerTest"
./gradlew bootRun --args='--spring.profiles.active=dev' &
sleep 20
# 10분 대기 후 로그 확인
grep "News sync completed" logs/application.log
```

**Manual Test Checklist**:
- [ ] Docker Compose 전체 스택 기동
- [ ] Django 크롤러 1회 강제 실행 (`python manage.py crawl_news` 또는 수동 endpoint)
- [ ] 10분 대기 또는 cron을 `*/1`로 임시 변경 → Spring log에 sync 결과 기록 확인
- [ ] Postgres: `SELECT COUNT(*) FROM news WHERE created_at > NOW() - INTERVAL '1 hour';` 증가 확인
- [ ] `GET http://localhost:8080/api/v1/news?size=20` → 응답에 실제 Naver URL 포함된 뉴스 확인

---

### Phase 5: 레거시 데이터 정리 + MVP 문서 갱신
**Goal**: 기존 fake URL 15건 정리, MVP 크롤링 문서를 실제 구현과 일치시키기.
**Estimated Time**: 1h
**Status**: ⏳ Pending
**Dependencies**: Phase 4 프로덕션 배포 후 배치가 최소 1회 성공했는지 확인

#### Tasks

**🟢 IMPLEMENTATION (테스트 불필요, 데이터/문서 작업)**

- [ ] **Task 5.1**: 기존 fake news 15건 조사
  - SQL:
    ```sql
    SELECT id, title, source_url, source_name, created_at
    FROM news
    WHERE source_url LIKE '%aespa-%'
       OR source_url LIKE '%nj-%'
       OR source_url LIKE '%n.news.naver.com/%-%'
    ORDER BY created_at;
    ```
  - 저장된 15건 리스트를 플랜의 Notes 섹션에 기록

- [ ] **Task 5.2**: Fake news 정리 전략 결정
  - Option A: **삭제** (`DELETE FROM news WHERE ...`)
  - Option B: **숨김** (`UPDATE news SET visible = false WHERE ...`)
  - **권장: B (숨김)** — 롤백 가능, view_count/history 보존

- [ ] **Task 5.2.1**: id 리스트 확정 (Dry-run) 🆕
  - ⚠️ 반드시 V120 작성 전 실행. 개발/스테이징/프로덕션 각 환경의 Postgres에서 동일하게 수행
  - **regex 패턴은 추측이므로 절대 V120 SQL 에 직접 사용하지 않음**. 대신 Task 5.1 에서 사람이 검토한 id 리스트만 신뢰.
  - 사전 후보 조회 (탐색용 SELECT — 실제 영향 X):
    ```sql
    SELECT id, title, source_url, source_name, visible, created_at
    FROM news
    WHERE source_url ~ '^https?://(n\.)?news\.naver\.com/[a-z]+-[0-9]+$'
       OR source_url LIKE '%/aespa-%'
       OR source_url LIKE '%/nj-%'
    ORDER BY created_at;
    ```
  - **검증 절차**:
    1. 위 SELECT 결과를 Task 5.1 목록과 1:1 비교 (id 단위)
    2. 추가/누락 row 가 있으면 → 사람이 직접 판단 후 최종 id 리스트 확정 (false positive/negative 방지)
    3. 최종 id 리스트를 `Notes 섹션 ## V120 Target IDs` 에 환경별로 기록 (dev/staging/prod 각각 다를 수 있음)
  - **결과 형식 예시** (Notes 에 기록):
    ```
    [prod] V120 Target IDs (15건, 2026-04-27 14:30 KST):
      - 9d2a1b3c-..., title="aespa OMG 신곡 발표"
      - 1f4e2c5d-..., title="뉴진스 신보 티저"
      - ... (총 15건)
    ```

- [ ] **Task 5.2.2**: Flyway 마이그레이션 작성 — Task 5.2.1 의 id 리스트 사용
  - **regex/LIKE 사용 금지**. 오직 Task 5.2.1 에서 확정한 id 리스트만 사용 (V120 은 환경마다 SQL 이 다를 수 있음 → Flyway placeholder 또는 환경별 보조 스크립트 사용 검토)
  - File: `backend/src/main/resources/db/migration/V120__hide_legacy_fake_news.sql`
    ```sql
    -- 참조: Task 5.2.1 prod 결과 (15건, 2026-04-27 기록)
    -- regex 가 아닌 명시적 id 리스트로 hide → false positive 0% 보장
    UPDATE news
    SET visible = false
    WHERE id IN (
        '9d2a1b3c-xxxx-xxxx-xxxx-xxxxxxxxxxxx',
        '1f4e2c5d-xxxx-xxxx-xxxx-xxxxxxxxxxxx'
        -- ... (Task 5.2.1 prod id 리스트 전체 명시)
    );
    ```
  - **환경 차이 처리**:
    - 옵션 A: 환경별 V120 placeholder (`${fake_news_ids}`) 사용 — Flyway placeholders 설정
    - 옵션 B: V120 은 prod 기준으로 작성하고, dev/staging 은 별도 1회성 보조 스크립트로 처리
    - **권장: B** (Flyway placeholder 도입 부담 회피, V120 자체는 단일 SQL 유지)
  - ⚠️ 하단 Quality Gate 적용 전/후 검증 쿼리로 반드시 재확인

- [ ] **Task 5.3**: MVP 크롤링 문서 갱신
  - File: `docs/mvp/mvp_크롤링.md`
  - 변경 포인트:
    - L10 "1차(MVP): seed(큐레이션) 기반으로만 채움" → "1차(MVP): Django Naver 크롤러 + Spring 동기 배치로 `news` 테이블 자동 채움"
    - L11 삭제 또는 "2차: YouTube API / 추가 소스 확장"로 대체
    - L156 Stretch A 섹션 현 구현에 맞춰 재정리
    - L171 완료기준 갱신: "10분 주기 sync가 성공하고, `/api/v1/news` 응답에 클릭 가능한 실제 URL 포함"
  - 새 아키텍처 다이어그램 추가:
    ```
    Naver Open API
         ↓ (Django crawler, 10분 주기)
    crawled_news (Postgres, Django owner)
         ↓ (Spring NewsSyncScheduler, 10분 주기)
         ↓ NewsMatcher + NewsCategoryClassifier
    news (Postgres, Spring owner)
         ↓ (REST API)
    GET /api/v1/news → 웹 UI
    ```

- [ ] **Task 5.4**: MEMORY.md 업데이트
  - File: `~/.claude/projects/-Users-ohchaeeun-source-FanPulse/memory/MEMORY.md` (사용자 auto-memory)
  - 추가: News sync 배치 아키텍처 메모 (Spring owns news, Django owns crawled_news, Sync 주기 10분)

#### Quality Gate ✋

**Data Safety**:
- [ ] V120 마이그레이션 적용 **전** `SELECT COUNT(*) FROM news WHERE visible=true AND id IN (<Task 5.2.1 id 리스트>);` = 15 확인
- [ ] V120 적용 **후** 동일 쿼리 = 0 확인
- [ ] V120 적용 **후** `SELECT COUNT(*) FROM news WHERE visible=true;` ≥ Phase 4 sync 성공 건수 확인 (새 데이터 살아있음)
- [ ] **id 리스트 외 row 가 visible=false 가 되지 않았는지 확인** (false positive 방지):
  ```sql
  SELECT COUNT(*) FROM news
  WHERE visible = false
    AND id NOT IN (<Task 5.2.1 id 리스트>);
  ```
  결과 = 0 이어야 함 (V120 직전 시점 기준; 다른 경로로 hide 된 건 별도 검토)
- [ ] `GET /api/v1/news` 응답에 fake URL 없음

**Documentation**:
- [ ] `docs/mvp/mvp_크롤링.md` 변경사항 PR 리뷰 통과
- [ ] 아키텍처 다이어그램이 실제 코드와 일치

**Manual Test Checklist**:
- [ ] 웹 UI 뉴스 탭 방문 → 노출되는 모든 뉴스 카드 클릭 → 404 없음
- [ ] 동일 뉴스 중복 노출 없음 (대부분 `(source_url, artist_id)` 유니크로 방지됨)

---

## ⚠️ Risk Assessment

| Risk | Probability | Impact | Mitigation Strategy |
|------|-------------|--------|---------------------|
| **Django `crawled_news` 스키마 변경 시 Spring Entity 깨짐** | Medium | High | Phase 2 Entity에 모든 컬럼 `nullable` 허용, Hibernate `ddl-auto=validate`로 기동 시 검증. `ai/api/models.py` 변경 PR에 "Spring 영향 확인" 체크리스트 추가 |
| **매칭 오탐 ("New Jeans"가 일반 표현 매칭)** | Medium | Medium | Phase 1에서 tokenization 정책 (단어 경계) 문서화. 1주 운영 후 오탐 로그 분석 → 블랙리스트 키워드 추가 |
| **매칭 누락 (별명/애칭 미매칭)** | High | Low | MVP 범위 초과. 2차에서 임베딩 기반 검토. 운영 로그로 skip된 title 샘플링 |
| **스케줄러 동시 실행 (다중 인스턴스)** | Low | Medium | ShedLock `lockAtLeastFor="1m"` 적용. LiveDiscovery와 동일 패턴 검증됨 |
| **Flyway V119 유니크 제약 변경 시 기존 데이터 충돌** | Low | High | Task 3.8 사전 체크로 `SELECT source_url, artist_id, COUNT(*) FROM news GROUP BY 1,2 HAVING COUNT(*) > 1;` = 0 확인. 기존 15건은 아티스트 매칭이 다 붙어있을 가능성 낮음 |
| **Naver API rate limit (Django 측)** | Low | Medium | Django 책임. Spring은 read only. 로그 모니터링만 |
| **Fail-Open으로 조용히 실패** | Medium | High | Phase 4 refactor 단계에서 Micrometer counter + Grafana alert 설정 (별도 PR 가능) |
| **`news` 테이블 볼륨 폭증 (1만 건/일 이상)** | Low | Medium | 현재 Naver API 일일 소화량 ~1000건 수준. 10만 건 도달 시 파티셔닝 검토. MVP 단계에서는 무시 |
| **@Transactional self-invocation 으로 트랜잭션 미적용** | Medium | High | Task 3.5: `TransactionalNewsUpserter` 를 별도 `@Component` 로 분리하여 Spring AOP proxy 경유. Task 3.6 의 `NewsSyncServiceImpl` 자체에는 `@Transactional` 사용 금지. Test 3.2 로 호출 경로 검증 (PR #238 학습) |
| **N+1 쿼리로 배치 시간 폭증** | Medium | Medium | Task 3.7: `NewsPort.findBySourceUrlIn(urls)` bulk 조회로 1쿼리 보장. Test 3.1 의 `verify(exactly = 1)` 로 회귀 방지 |
| **V120 fake URL regex false positive** | Medium | High | regex 사용 금지. Task 5.2.1 에서 사람이 검토한 id 리스트를 Task 5.2.2 의 `WHERE id IN (...)` 로 명시. Phase 5 Quality Gate 에 false positive 방지 SELECT 포함 |

---

## 🔄 Rollback Strategy

### If Phase 1 Fails
**Steps to revert**:
- `NewsMatcher.kt`, `NewsCategoryClassifier.kt` 및 해당 테스트 삭제
- 다른 레이어 미침 영향 없음 (순수 도메인)

### If Phase 2 Fails
**Steps to revert**:
- `CrawledNewsEntity.kt`, `CrawledNewsJpaRepository.kt`, `CrawledNewsAdapter.kt`, `CrawledNewsReader.kt`, `CrawledNewsSnapshot` 삭제
- Hibernate validate 에러 날 수 있음 → `spring.jpa.hibernate.ddl-auto=validate` 의 대상에서 제외되는지 확인
- Phase 1 상태로 복귀

### If Phase 3 Fails
**Steps to revert**:
- `NewsSyncService*.kt`, `TransactionalNewsUpserter.kt` 및 테스트 파일 삭제 (Task 3.4-3.6)
- `NewsPort.findBySourceUrlIn` 메서드 제거 (Task 3.7)
- **Flyway V119 롤백 필수** (유니크 제약 복원):
  ```sql
  ALTER TABLE news DROP CONSTRAINT news_source_url_artist_id_unique;
  ALTER TABLE news ADD CONSTRAINT news_source_url_key UNIQUE (source_url);
  ```
  - Flyway는 마이그레이션 rollback을 기본 지원 안 함 → 수동 SQL + `flyway_schema_history` 레코드 삭제 or `V119.1__rollback_...sql` 신규 마이그레이션
- Phase 2 상태로 복귀

### If Phase 4 Fails
**Steps to revert**:
- `application.yml`에서 `fanpulse.scheduler.news-sync.enabled: false` 즉시 변경 → 재배포
- `NewsSyncScheduler.kt` 삭제 (선택적)
- 이미 insert된 news는 유지 (rollback 불필요) or Phase 5 방식으로 hide

### If Phase 5 Fails
**Steps to revert**:
- V120 역마이그레이션: `UPDATE news SET visible = true WHERE id IN (<Task 5.2.1 id 리스트>);` (regex 사용 금지 — 동일 id 리스트로만 복원)
- 별도 V120.1 보조 마이그레이션 작성 권장 (Flyway history 일관성)
- MVP 문서 revert

---

## 📊 Progress Tracking

### Completion Status
- **Phase 1** (매칭/분류 도메인): ⏳ 0%
- **Phase 2** (crawled_news 읽기 infra): ⏳ 0%
- **Phase 3** (NewsSyncService): ⏳ 0%
- **Phase 4** (Scheduler): ⏳ 0%
- **Phase 5** (Cleanup/Docs): ⏳ 0%

**Overall Progress**: 0% complete

### Time Tracking
| Phase | Estimated | Actual | Variance |
|-------|-----------|--------|----------|
| Phase 1 | 2h | - | - |
| Phase 2 | 2h | - | - |
| Phase 3 | 3h | - | - |
| Phase 4 | 2h | - | - |
| Phase 5 | 1h | - | - |
| **Total** | 10h (best) ~ 14h (with V119/V120 마이그레이션 환경별 조율, Pre-flight 재실행, fail-open 메트릭 알람 튜닝 시간 포함) | - | - |

---

## 📝 Notes & Learnings

### Plan Review History (2026-04-27)

PR #271 (코루틴·트랜잭션 가이드 문서화) 작업 직후, 동일한 학습을 본 플랜에 적용하기 위해 v1 → v2 리뷰를 수행했다. 8개 항목을 식별하고 모두 반영함.

| # | 영역 | 발견 | 반영 |
|---|------|------|------|
| 1 | 메타데이터 | Status/Issue/Branch 필드 부재 | 헤더에 Issue #272, Branch, Ready Date, Related Learning 추가 |
| 2 | 메타데이터 | Estimated Completion 부재 | 2026-05-02, 10-14h 명시 |
| 3 | 데이터 안전 | V119 사전 점검 1건뿐 | artist_id NULL count + (source_url, artist_id) 중복 체크 추가 (Task 3.8) |
| 4 | 트랜잭션 | Service 직접 @Transactional 사용 → self-invocation 위험 | PR #238 패턴 도입: `TransactionalNewsUpserter` 별도 컴포넌트 + REQUIRES_NEW (Task 3.5) |
| 5 | 데이터 안전 | V120 regex `LIKE '%aespa-%'` false positive 위험 | id 리스트 기반 `WHERE id IN (...)` 로 변경, Task 5.2.1 dry-run 절차 강화 |
| 6 | 정책 | Snapshot publishedAt 의 timezone 정책 미명시 | `CrawledNewsSnapshot` KDoc 에 USE_TZ=True UTC 직접 매핑 정책 명시 |
| 7 | 성능 | Artist cache 갱신 주기 모호 | 10분 staleness 명시 (배치 주기와 동일하므로 stale 가능성 1주기 이내) |
| 8 | 성능 | Repository 호출이 source_url 단건씩 N+1 위험 | `NewsPort.findBySourceUrlIn(urls)` bulk 메서드 (Task 3.7) + `verify(exactly = 1)` 테스트 (Test 3.1) |

**핵심 학습**: PR #238 의 self-invocation 회피 패턴은 코루틴뿐만 아니라 **동기 배치 컨텍스트에서도 동일하게 필요**. AOP proxy 가 우회되는 조건은 호출 방식(코루틴/동기)이 아니라 **같은 클래스 내부 호출**이기 때문.

### Implementation Notes
- (Phase 진행하며 기록)

### Blockers Encountered
- (발생 시 기록)

### Fake News 15건 리스트 (Phase 5.1에서 채울 것)
```
(SELECT 결과 붙여넣기 — Task 5.2.1 dry-run 결과를 환경별로 분리 기록)
[dev]   id 리스트:
[stg]   id 리스트:
[prod]  id 리스트:
```

### V120 Target IDs (Task 5.2.1 dry-run 결과)
```
(Task 5.2.1 실행 후 환경별 id 리스트 + 실행 시각 기록)
```

### Improvements for Future Plans
- (완료 후 회고)

---

## 📚 References

### Related GitHub Issues / PRs
- **Issue #272** (이 플랜의 트래킹 이슈) — [feat(backend): 뉴스 동기화 배치 (crawled_news → news) 구현](https://github.com/jskjw157/FanPulse/issues/272)
- **PR #238** — `fix/166-metadata-refresh-transactional`: 메타데이터 갱신에서 코루틴 + @Transactional self-invocation 회피 패턴 도입. 본 플랜의 `TransactionalNewsUpserter` 패턴이 PR #238의 `TransactionalMetadataUpdater` 와 동일 구조.
- **PR #271** — `docs/166-coroutine-transactional`: PR #238 의 학습을 [docs/architecture/coroutine-transactional.md](../architecture/coroutine-transactional.md) 로 정리. 본 플랜은 동일 가이드를 동기 배치 컨텍스트에 적용.
- **Issue #166** — 메타데이터 코루틴 + 트랜잭션 문제 이슈
- **Issue #224** — Self-invocation 학습 이슈

### Documentation
- [Coroutine + Transactional 가이드](../architecture/coroutine-transactional.md) — Phase 3 트랜잭션 경계 설계의 근거
- [MVP 크롤링 문서](../mvp/mvp_크롤링.md) — Phase 5에서 갱신 대상
- [LiveDiscoveryScheduler](../../backend/src/main/kotlin/com/fanpulse/infrastructure/scheduler/LiveDiscoveryScheduler.kt) — 스케줄러 패턴 참조
- [SeedLoaderRunner](../../backend/src/main/kotlin/com/fanpulse/infrastructure/seed/SeedLoaderRunner.kt) — News.create() 사용 예시

### Related Code
- Django crawler: `ai/api/services/news_crawler.py`
- Django model: `ai/api/models.py` (`CrawledNews`)
- Spring News domain: `backend/src/main/kotlin/com/fanpulse/domain/content/News.kt`
- Spring News port: `backend/src/main/kotlin/com/fanpulse/domain/content/port/NewsPort.kt`
- **PR #238 패턴 참조**: `backend/src/main/kotlin/com/fanpulse/infrastructure/metadata/TransactionalMetadataUpdater.kt` (REQUIRES_NEW + 별도 컴포넌트)

### Architecture Principles (MEMORY.md 발췌)
- Django: AI Sidecar only → `crawled_news` writer 유지
- Spring: Main business logic (Hexagonal) → `news` writer, `crawled_news` reader
- Fail-Open strategy: 배치 실패 시 계속 진행, 전체 중단 금지
- KDoc 한국어 단일 언어 작성

---

## ✅ Final Checklist

**Before marking plan as COMPLETE**:
- [ ] 5 phases 완료, 각 Quality Gate 통과
- [ ] 전체 integration test: Django crawler 실행 → 10분 대기 → `news` 테이블 증가 확인
- [ ] 프로덕션 `GET /api/v1/news` 응답에 클릭 가능한 Naver URL 노출
- [ ] 기존 fake 15건 hidden 처리 완료 (id 리스트 기반, regex 사용 X)
- [ ] **PR #238 학습 적용 검증** (Task 3.5/3.6): `TransactionalNewsUpserter` 가 별도 컴포넌트, `REQUIRES_NEW` 적용, Service 자체 `@Transactional` 없음
- [ ] **N+1 회피 검증** (Task 3.7 + Test 3.1): 100건 배치에서 `findBySourceUrlIn` 호출 횟수 = 1 (테스트로 자동 검증)
- [ ] `docs/mvp/mvp_크롤링.md` 갱신 완료
- [ ] JaCoCo 전체 커버리지 저하 없음 (기존 대비 ±2% 이내)
- [ ] ktlint 전체 통과
- [ ] Flyway 마이그레이션 V119, V120 프로덕션 적용 완료
- [ ] PM/기획 사이드에 "뉴스가 실데이터로 바뀌었음" 알림
- [ ] 1주간 운영 관찰 (오탐/누락 로그 샘플링) → 회고

---

**Plan Status**: 📝 Draft (승인 대기 — 8개 리뷰 이슈 반영 완료, 2026-04-27)
**Next Action**: 플랜 최종 검토 → 승인 시 Phase 1 착수
**Blocked By**: None
**Issue**: [#272](https://github.com/jskjw157/FanPulse/issues/272)
**Branch**: `feature/272-news-sync-batch`
