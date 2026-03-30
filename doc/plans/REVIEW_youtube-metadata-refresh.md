# YouTube 메타데이터 자동 갱신 기능 종합 검토 결과

> **검토일**: 2026-01-07
> **브랜치**: `feature/159-crawling-youtube`
> **관련 이슈**: #159, #160, #90

---

## 1. 검토 개요

| 검토 영역 | 에이전트 | 결과 |
|----------|---------|------|
| 코드 품질 | code-reviewer | 4.4/5 |
| 아키텍처 | backend-architect | 7.2/10 |
| 이슈 관리 | github-issues | 마일스톤 조정 필요 |
| DDD 준수 | ddd-planning | 3.4/5 |

---

## 2. Code Review 결과

### 2.1 점수

| 항목 | 점수 | 평가 |
|------|------|------|
| 코드 품질 | 4.5/5 | 우수한 아키텍처, Clean Code |
| 보안 | 4.0/5 | 기본 보안 양호 |
| 에러 처리 | 4.5/5 | 포괄적 예외 처리, Retry 메커니즘 |
| 테스트 커버리지 | 4.0/5 | 35개 테스트 케이스 |
| 설계 일치도 | 5.0/5 | MVP 크롤링 문서와 완벽 일치 |

### 2.2 긍정적 발견

- Clean Architecture/DDD 원칙 준수
- 인터페이스 기반 설계로 테스트 용이성 확보
- Kotlin 관용적 표현 활용
- 포괄적인 예외 처리 (3단계: 특정 → 일반 → 모든 예외)
- Exponential backoff Retry 전략

### 2.3 개선 필요 사항

**P1 (즉시)**
- 트랜잭션 범위 최적화 (개별 이벤트별 분리)
- `application.yml` 기본 비밀번호 제거

**P2 (다음 스프린트)**
- `Thread.sleep()` → 코루틴 변경
- URL 검증 강화 (길이 제한, 도메인 검증)
- Circuit Breaker 패턴 도입

---

## 3. Backend Architecture 검토 결과

### 3.1 디렉토리 구조

```
backend/src/main/kotlin/com/fanpulse/
├── domain/streaming/          # Domain Layer
├── application/service/       # Application Layer
└── infrastructure/            # Infrastructure Layer
    ├── config/
    ├── scheduler/
    └── external/youtube/
```

### 3.2 평가

| 항목 | 점수 | 평가 |
|------|------|------|
| 레이어 분리 | 7/10 | 기본 구조 양호, 의존성 방향 위반 |
| 외부 API 연동 | 8/10 | Retry, Timeout 잘 구현 |
| 설정 관리 | 9/10 | 외부화 우수 |
| 확장성 | 6/10 | 분산 환경 미고려 |
| 테스트 용이성 | 6/10 | Domain JPA 의존 제약 |

### 3.3 의존성 문제

| 문제점 | 파일 | 설명 |
|--------|------|------|
| JPA 의존성 Domain 침투 | `StreamingEvent.kt` | `jakarta.persistence.*` 직접 의존 |
| Repository JPA 상속 | `StreamingEventRepository.kt` | Domain에서 JpaRepository 상속 |
| Application→Infrastructure | `MetadataRefreshServiceImpl.kt` | infrastructure 패키지 직접 import |

### 3.4 권장 개선 아키텍처

```
domain/
├── streaming/
│   ├── StreamingEvent.kt           # 순수 Domain Entity
│   └── port/
│       ├── StreamingEventRepository.kt  # Domain Port
│       └── VideoMetadataClient.kt       # Domain Port

infrastructure/
├── persistence/
│   └── JpaStreamingEventRepository.kt   # JPA 구현
└── external/youtube/
    └── YouTubeOEmbedClientAdapter.kt    # Client 구현
```

---

## 4. GitHub Issues 검토 결과

### 4.1 이슈 구현 상태

| 이슈 | 제목 | 구현 상태 | 문제점 |
|------|------|----------|--------|
| #159 | YouTube 메타데이터 자동 갱신 | ✅ 완료 | 마일스톤 불일치 |
| #160 | K-pop 라이브 자동 발견 | 미착수 | MVP 범위 밖 |
| #90 | 라이브 메타데이터 크롤링 | Phase2 | 적절 |

### 4.2 마일스톤 문제

**MVP 백로그 문서 vs 실제 배치**

| Sprint | 백로그 문서 범위 | 실제 배치 | 문제 |
|--------|------------------|-----------|------|
| Sprint 1 | Skeleton + Contract | #159, #160 | 크롤링은 Sprint 1 범위 밖 |
| Sprint 4 | Stretch: YouTube 메타데이터 보강 | - | #159가 본래 속해야 할 곳 |

**권장 조치**
- #159: Sprint 1 → **Sprint 4** 이동
- #160: Sprint 1 → **Phase2** 이동 또는 삭제

### 4.3 라벨링 개선

| 이슈 | 현재 | 권장 |
|------|------|------|
| #159 | `priority:low` | `priority:medium`, `category:live` 추가 |
| #160 | MVP 라벨 없음 | `phase2` 라벨 추가 |

---

## 5. DDD 준수 검토 결과

### 5.1 평가

| 항목 | 점수 | 상태 |
|------|------|------|
| Bounded Context | 4/5 | 양호 (문서화 필요) |
| Aggregate Root | 3/5 | 불변식 보호 부족 |
| Repository 패턴 | 3/5 | JPA 의존성 분리 필요 |
| Domain Event | 2/5 | 미구현 |
| Ubiquitous Language | 4/5 | 양호 |

### 5.2 Aggregate Root 문제

**현재 문제점**
```kotlin
// StreamingEvent.kt - 외부에서 직접 상태 변경 가능
event.status = StreamingStatus.LIVE  // 위험!
event.startedAt = Instant.now()
```

**권장 개선**
```kotlin
class StreamingEvent {
    fun goLive(): StreamingStarted {
        require(status == StreamingStatus.SCHEDULED)
        status = StreamingStatus.LIVE
        startedAt = Instant.now()
        return StreamingStarted(id, startedAt!!)
    }
}
```

### 5.3 누락 문서

- `doc/ddd/bounded-contexts/streaming.md` 파일 필요
- `ubiquitous-language.md`에 "메타데이터 갱신" 용어 추가 필요

---

## 6. 종합 우선순위 액션 아이템

### P1 (즉시 수정) - ✅ 완료

| # | 항목 | 출처 | 담당 | 상태 |
|---|------|------|------|------|
| 1 | 트랜잭션 범위 최적화 | Code Review | Backend | ✅ 완료 |
| 2 | `application.yml` 기본 비밀번호 제거 | Code Review | Backend | ✅ 완료 |
| 3 | 이슈 #159 마일스톤 Sprint 4로 변경 | GitHub Issues | PM | ✅ 완료 |

### P2 (다음 스프린트) - ✅ 완료

| # | 항목 | 출처 | 담당 | 상태 |
|---|------|------|------|------|
| 4 | Domain/Infrastructure 레이어 분리 | Architecture | Backend | ✅ 완료 |
| 5 | Aggregate 불변식 보호 (캡슐화) | DDD | Backend | ✅ 완료 |
| 6 | 분산 락 추가 (ShedLock) | Architecture | Backend | ✅ 완료 |
| 7 | `Thread.sleep()` → 코루틴 변경 | Code Review | Backend | ✅ 완료 |
| 8 | streaming.md Bounded Context 문서 | DDD | Docs | ✅ 완료 |

### P3 (MVP 이후) - ✅ 완료

| # | 항목 | 출처 | 담당 | 상태 |
|---|------|------|------|------|
| 9 | Circuit Breaker 패턴 도입 | Code Review | Backend | ✅ 완료 |
| 10 | Domain Event 구현 | DDD | Backend | ✅ 완료 |
| 11 | Micrometer 메트릭 추가 | Architecture | Backend | ✅ 완료 |
| 12 | 이슈 #160 Sprint 2 이동 | GitHub Issues | PM | ✅ 완료 |

---

## 7. 구현 상세

### 7.1 P1 구현 내용

#### 트랜잭션 범위 최적화
- `TransactionalMetadataUpdater` 컴포넌트 신규 생성
- `@Transactional(propagation = Propagation.REQUIRES_NEW)` 적용
- 개별 이벤트 실패 시 해당 트랜잭션만 롤백

```kotlin
// TransactionalMetadataUpdater.kt
@Component
class TransactionalMetadataUpdater(
    private val eventPort: StreamingEventPort,
    private val oEmbedClient: YouTubeOEmbedClient,
    private val videoIdExtractor: YouTubeVideoIdExtractor,
    private val domainEventPublisher: DomainEventPublisher
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun updateEventMetadata(event: StreamingEvent): Boolean { ... }
}
```

#### 기본 비밀번호 제거
- `application.yml`에서 `password: ${DB_PASSWORD}` 환경변수 참조로 변경

### 7.2 P2 구현 내용

#### Domain/Infrastructure 레이어 분리
- `StreamingEventPort` 도메인 포트 인터페이스 생성
- `StreamingEventRepository`가 Port를 구현하도록 변경
- Application 레이어에서 Port 인터페이스만 의존

```
domain/streaming/port/
└── StreamingEventPort.kt      # 도메인 포트 인터페이스
```

#### Aggregate 불변식 보호
- `StreamingEvent`를 `data class` → `class`로 변경
- 상태 변경 필드에 `private set` 적용
- 도메인 메서드 추가: `goLive()`, `end()`, `updateMetadata()`

```kotlin
class StreamingEvent {
    var status: StreamingStatus = status
        private set

    fun goLive(now: Instant = Instant.now()) {
        require(status == StreamingStatus.SCHEDULED)
        status = StreamingStatus.LIVE
        startedAt = now
    }
}
```

#### 코루틴 적용
- `MetadataRefreshService` 인터페이스 메서드를 `suspend fun`으로 변경
- `Thread.sleep()` → `delay()` 변경
- 스케줄러에서 `runBlocking` 사용

### 7.3 P3 구현 내용

#### Circuit Breaker 패턴 (Resilience4j)

**의존성 추가**
```kotlin
implementation("io.github.resilience4j:resilience4j-spring-boot3:2.2.0")
implementation("io.github.resilience4j:resilience4j-kotlin:2.2.0")
```

**설정** (`application.yml`)
```yaml
resilience4j:
  circuitbreaker:
    instances:
      youtubeOEmbed:
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50
        waitDurationInOpenState: 30s
        permittedNumberOfCallsInHalfOpenState: 3
```

**적용**
```kotlin
@Component
class YouTubeOEmbedClientImpl(...) : YouTubeOEmbedClient {
    @CircuitBreaker(name = "youtubeOEmbed", fallbackMethod = "fallbackFetchMetadata")
    override fun fetchMetadata(videoId: String): YouTubeMetadata? { ... }

    private fun fallbackFetchMetadata(videoId: String, e: Exception): YouTubeMetadata? {
        logger.warn { "Circuit breaker fallback triggered for video $videoId" }
        return null
    }
}
```

#### Domain Event 구현

**생성된 파일**
```
domain/common/
├── DomainEvent.kt              # 도메인 이벤트 인터페이스
└── DomainEventPublisher.kt     # 이벤트 발행 포트

domain/streaming/event/
└── StreamingEventMetadataUpdated.kt  # 메타데이터 갱신 이벤트

infrastructure/event/
└── SpringDomainEventPublisher.kt     # Spring 기반 구현
```

**이벤트 구조**
```kotlin
data class StreamingEventMetadataUpdated(
    val streamingEventId: UUID,
    val previousTitle: String,
    val newTitle: String,
    val previousThumbnailUrl: String?,
    val newThumbnailUrl: String?,
    val titleChanged: Boolean,
    val thumbnailChanged: Boolean,
    override val eventId: UUID,
    override val occurredAt: Instant
) : AbstractDomainEvent(eventId, occurredAt)
```

#### Micrometer 메트릭 추가

**의존성**
```kotlin
implementation("org.springframework.boot:spring-boot-starter-actuator")
implementation("io.micrometer:micrometer-registry-prometheus")
```

**메트릭**
| 메트릭 이름 | 타입 | 설명 |
|------------|------|------|
| `metadata.refresh.events.processed` | Counter | 처리된 이벤트 수 |
| `metadata.refresh.events.updated` | Counter | 업데이트 성공 수 |
| `metadata.refresh.events.failed` | Counter | 실패 수 |
| `metadata.refresh.duration` | Timer | 갱신 소요 시간 |

**엔드포인트**
- `/actuator/health` - 헬스 체크 (Circuit Breaker 상태 포함)
- `/actuator/prometheus` - Prometheus 메트릭
- `/actuator/circuitbreakers` - Circuit Breaker 상태

#### 이슈 #160 마일스톤 변경
- Sprint 1 → Sprint 2 이동
- 마일스톤 설명에 이슈 추가

---

## 8. 결론

### 총평

**코드 품질**: 4.4/5 → **4.7/5** (개선됨)

P1~P3 액션 아이템 구현 후:
- 트랜잭션 격리로 안정성 향상
- Port/Adapter 패턴으로 테스트 용이성 개선
- Aggregate 캡슐화로 DDD 준수도 향상
- Circuit Breaker로 장애 전파 방지
- 메트릭으로 운영 가시성 확보

### 배포 가능 여부

- ✅ **즉시 배포 가능**
- 모든 P1/P2/P3 핵심 항목 완료
- 35개 테스트 통과

### 잔여 작업

| 항목 | 상태 | 비고 |
|------|------|------|
| 분산 락 (ShedLock) | ✅ 완료 | PR 리뷰 반영으로 추가 |
| Bounded Context 문서 | ✅ 완료 | `doc/ddd/bounded-contexts/streaming.md` 작성 |

---

## 변경 이력

| 버전 | 날짜 | 변경 내용 |
|------|------|----------|
| 1.0.0 | 2026-01-07 | 최초 작성 |
| 1.1.0 | 2026-01-07 | P1/P2/P3 구현 완료, 구현 상세 섹션 추가 |
| 1.2.0 | 2026-01-11 | ShedLock 추가, Bounded Context 문서 완료 |
