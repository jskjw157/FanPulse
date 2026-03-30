# Feature Plan: YouTube Metadata Auto-Refresh

## Issue Reference
- **Issue**: #159 - [Crawling] YouTube 메타데이터 자동 갱신
- **Branch**: `feature/159-crawling-youtube`
- **Priority**: low (MVP Stretch Goal)
- **Sprint**: Sprint 4: QA + Release

---

## 1. Overview

### 1.1 Problem Statement
`streaming_events` 테이블에 저장된 YouTube 라이브 스트리밍의 메타데이터(제목, 썸네일, 상태, 시청자 수)가 시간이 지남에 따라 outdated 될 수 있습니다. 현재는 seed 데이터로 수동 입력되며 자동 갱신 기능이 없습니다.

### 1.2 Solution
YouTube oEmbed API를 활용하여 메타데이터를 주기적으로 자동 갱신하는 스케줄러를 구현합니다.

### 1.3 Why oEmbed?
| 방식 | 장점 | 단점 |
|------|------|------|
| **oEmbed (권장)** | API 키 불필요, 할당량 제한 없음, 구현 간단 | 시청자 수 조회 불가, 상태 추론 필요 |
| YouTube Data API v3 | 정확한 상태/시청자 수 조회 | API 키 필요, 일일 할당량 제한 (10,000 units) |

**MVP 결정**: oEmbed 우선 구현 (API 키 없이 동작), 추후 Data API 확장 가능

---

## 2. Scope

### 2.1 In Scope
- [x] YouTube oEmbed 클라이언트 구현
- [x] 메타데이터 갱신 서비스 구현 (title, thumbnail_url)
- [x] 상태 추론 로직 (oEmbed 응답 기반)
- [x] 스케줄러 구현
  - 1시간마다: LIVE 상태 이벤트 갱신
  - 1일 1회: 전체 이벤트 갱신 (SCHEDULED 포함)
- [x] 실행 로그 기록

### 2.2 Out of Scope (Next Phase)
- [ ] YouTube Data API v3 연동 (시청자 수 정확한 조회)
- [ ] Weverse Live 메타데이터 갱신 (Next)
- [ ] 실시간 WebSocket 기반 상태 업데이트
- [ ] 알림 서비스 연동 (라이브 시작 알림)

---

## 3. Technical Design

### 3.1 Architecture

```
+-------------------+     +-----------------------+     +-------------------+
|   Scheduler       | --> | MetadataRefreshService| --> | StreamingEventRepo|
| (Spring @Scheduled)|    |                       |     |                   |
+-------------------+     +-----------------------+     +-------------------+
                                    |
                                    v
                          +-------------------+
                          | YouTubeOEmbedClient|
                          | (HTTP Client)      |
                          +-------------------+
                                    |
                                    v
                          +-------------------+
                          | YouTube oEmbed API|
                          | (External)        |
                          +-------------------+
```

### 3.2 Key Components

#### 3.2.1 YouTubeOEmbedClient
YouTube oEmbed API 호출 클라이언트

```kotlin
// 위치: infrastructure/external/youtube/YouTubeOEmbedClient.kt

interface YouTubeOEmbedClient {
    fun fetchMetadata(videoId: String): YouTubeMetadata?
}

data class YouTubeMetadata(
    val title: String,
    val thumbnailUrl: String,
    val authorName: String,
    val providerName: String
)
```

**oEmbed API Endpoint:**
```
GET https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v={VIDEO_ID}&format=json
```

**Response Example:**
```json
{
  "title": "Video Title",
  "author_name": "Channel Name",
  "author_url": "https://www.youtube.com/@channel",
  "type": "video",
  "height": 113,
  "width": 200,
  "version": "1.0",
  "provider_name": "YouTube",
  "provider_url": "https://www.youtube.com/",
  "thumbnail_height": 360,
  "thumbnail_width": 480,
  "thumbnail_url": "https://i.ytimg.com/vi/VIDEO_ID/hqdefault.jpg",
  "html": "<iframe ...></iframe>"
}
```

#### 3.2.2 MetadataRefreshService
메타데이터 갱신 비즈니스 로직

```kotlin
// 위치: application/service/MetadataRefreshService.kt

interface MetadataRefreshService {
    fun refreshLiveEvents(): RefreshResult
    fun refreshAllEvents(): RefreshResult
    fun refreshEvent(eventId: UUID): Boolean
}

data class RefreshResult(
    val total: Int,
    val updated: Int,
    val failed: Int,
    val errors: List<RefreshError>
)
```

#### 3.2.3 MetadataRefreshScheduler
스케줄링 컴포넌트

```kotlin
// 위치: infrastructure/scheduler/MetadataRefreshScheduler.kt

@Component
class MetadataRefreshScheduler(
    private val service: MetadataRefreshService
) {
    // 1시간마다 LIVE 상태 갱신
    @Scheduled(cron = "0 0 * * * *")
    fun refreshLiveMetadata() { ... }

    // 매일 자정 전체 갱신
    @Scheduled(cron = "0 0 0 * * *")
    fun refreshAllMetadata() { ... }
}
```

### 3.3 Database Schema

기존 `streaming_events` 테이블 사용 (변경 없음):

| Column | Type | Description |
|--------|------|-------------|
| id | UUID | PK |
| title | VARCHAR(255) | 제목 (갱신 대상) |
| thumbnail_url | TEXT | 썸네일 (갱신 대상) |
| stream_url | TEXT | YouTube embed URL |
| status | VARCHAR(20) | SCHEDULED/LIVE/ENDED |
| viewer_count | INT | 시청자 수 (oEmbed로는 갱신 불가) |
| ... | ... | ... |

### 3.4 Video ID 추출 로직

`stream_url` 형식: `https://www.youtube.com/embed/{VIDEO_ID}?...`

```kotlin
fun extractVideoId(streamUrl: String): String? {
    val regex = Regex("youtube\\.com/embed/([a-zA-Z0-9_-]{11})")
    return regex.find(streamUrl)?.groupValues?.get(1)
}
```

### 3.5 상태 추론 로직

oEmbed API는 라이브 상태를 직접 반환하지 않습니다. 다음 규칙으로 추론:

| 현재 상태 | 조건 | 새 상태 |
|-----------|------|---------|
| SCHEDULED | scheduledAt < now | LIVE (추정) |
| LIVE | oEmbed 404 or endedAt < now | ENDED |
| ENDED | - | 변경 없음 |

**Note**: 정확한 상태 판단은 YouTube Data API 필요 (Next Phase)

---

## 4. Implementation Phases

### Phase 1: Infrastructure (Day 1)
1. **YouTubeOEmbedClient 구현**
   - HTTP 클라이언트 설정 (WebClient/RestTemplate)
   - oEmbed API 호출 및 응답 파싱
   - 에러 핸들링 (404, timeout, rate limit)
   - Video ID 추출 유틸리티

2. **테스트**
   - Unit test: Video ID 추출
   - Integration test: oEmbed API 호출 (WireMock)

### Phase 2: Service Layer (Day 2)
1. **MetadataRefreshService 구현**
   - refreshLiveEvents(): LIVE 상태 이벤트만 갱신
   - refreshAllEvents(): 전체 이벤트 갱신
   - refreshEvent(id): 단일 이벤트 갱신
   - 트랜잭션 관리

2. **RefreshResult 및 로깅**
   - 갱신 결과 집계
   - 실패 사유 기록

3. **테스트**
   - Unit test: 서비스 로직
   - Integration test: Repository 연동

### Phase 3: Scheduler (Day 3)
1. **MetadataRefreshScheduler 구현**
   - @Scheduled 설정
   - 동시 실행 방지 (ShedLock 또는 @SchedulerLock)
   - 실행 로그 기록

2. **설정**
   - application.yml: cron 표현식 외부화
   - 스케줄러 활성화/비활성화 플래그

3. **테스트**
   - Scheduler 테스트 (awaitility)
   - 동시 실행 방지 테스트

### Phase 4: Integration & QA (Day 4)
1. **E2E 테스트**
   - 실제 YouTube 영상으로 통합 테스트
   - 스케줄러 실행 확인

2. **모니터링 설정**
   - 로그 포맷 정리
   - 메트릭 추가 (선택)

---

## 5. File Structure

```
backend/
├── src/main/kotlin/com/fanpulse/
│   ├── infrastructure/
│   │   ├── external/
│   │   │   └── youtube/
│   │   │       ├── YouTubeOEmbedClient.kt
│   │   │       ├── YouTubeOEmbedClientImpl.kt
│   │   │       ├── YouTubeOEmbedResponse.kt
│   │   │       └── YouTubeVideoIdExtractor.kt
│   │   └── scheduler/
│   │       └── MetadataRefreshScheduler.kt
│   ├── application/
│   │   └── service/
│   │       ├── MetadataRefreshService.kt
│   │       └── MetadataRefreshServiceImpl.kt
│   └── domain/
│       └── streaming/
│           └── StreamingEvent.kt (기존)
│
├── src/test/kotlin/com/fanpulse/
│   ├── infrastructure/
│   │   └── external/
│   │       └── youtube/
│   │           ├── YouTubeOEmbedClientTest.kt
│   │           └── YouTubeVideoIdExtractorTest.kt
│   ├── application/
│   │   └── service/
│   │       └── MetadataRefreshServiceTest.kt
│   └── integration/
│       └── MetadataRefreshIntegrationTest.kt
│
└── src/main/resources/
    └── application.yml (scheduler 설정 추가)
```

---

## 6. Configuration

### application.yml
```yaml
fanpulse:
  youtube:
    oembed:
      base-url: https://www.youtube.com/oembed
      timeout: 5000
      retry:
        max-attempts: 3
        delay: 1000

  scheduler:
    metadata-refresh:
      enabled: true
      live-cron: "0 0 * * * *"      # 매시 정각
      all-cron: "0 0 0 * * *"       # 매일 자정
      batch-size: 50
```

---

## 7. API (Optional Enhancement)

### 수동 갱신 엔드포인트 (관리자용)

```
POST /admin/streaming-events/refresh
Authorization: Bearer <admin_token>

Response 200:
{
  "success": true,
  "data": {
    "total": 150,
    "updated": 145,
    "failed": 5,
    "executedAt": "2025-01-15T10:00:00Z"
  }
}
```

---

## 8. Error Handling

| Error | Handling |
|-------|----------|
| oEmbed 404 | 영상 삭제됨 - status를 ENDED로 변경 |
| oEmbed timeout | 재시도 후 스킵, 다음 스케줄에서 재시도 |
| Rate limit | 배치 사이 delay 추가 (1초) |
| DB 오류 | 트랜잭션 롤백, 에러 로깅 |

---

## 9. Testing Strategy

### 9.1 Unit Tests
- `YouTubeVideoIdExtractor`: 다양한 URL 형식 파싱
- `YouTubeOEmbedClient`: Mock 응답 처리
- `MetadataRefreshService`: 비즈니스 로직

### 9.2 Integration Tests
- `YouTubeOEmbedClient` + WireMock
- `MetadataRefreshService` + TestContainers (PostgreSQL)

### 9.3 Coverage Target
- Business logic: 80%+
- Infrastructure: 70%+

---

## 10. Acceptance Criteria

- [ ] oEmbed API를 통해 YouTube 영상의 title, thumbnail_url을 조회할 수 있다
- [ ] 1시간마다 LIVE 상태 이벤트의 메타데이터가 자동 갱신된다
- [ ] 매일 자정 전체 이벤트의 메타데이터가 자동 갱신된다
- [ ] 갱신 결과가 로그로 기록된다
- [ ] 테스트 커버리지 80% 이상

---

## 11. Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| YouTube API 변경 | 서비스 중단 | 에러 모니터링, 알림 설정 |
| 대량 요청 시 rate limit | 갱신 실패 | 배치 처리, delay 추가 |
| oEmbed로 상태 정확도 낮음 | UX 저하 | Data API 확장 계획 (Next) |

---

## 12. Dependencies

### External
- YouTube oEmbed API (무료, 인증 불필요)

### Internal
- `streaming_events` 테이블 (기존)
- Spring Scheduler

---

## 13. Timeline

| Day | Task | Owner |
|-----|------|-------|
| 1 | Phase 1: YouTubeOEmbedClient | Backend |
| 2 | Phase 2: MetadataRefreshService | Backend |
| 3 | Phase 3: Scheduler | Backend |
| 4 | Phase 4: Integration & QA | Backend |

**Total**: 4 days

---

## 14. References

- [YouTube oEmbed Documentation](https://oembed.com/)
- [MVP 크롤링 문서](/doc/mvp/mvp_크롤링.md) - Stretch B 참조
- [DB 정의서](/doc/mvp/mvp_데이터베이스_정의서.md) - streaming_events 스키마
- [크롤링 실행 스케줄](/doc/크롤링.md) - 스케줄 정책

---

## 15. Change Log

| Version | Date | Description |
|---------|------|-------------|
| 1.0.0 | 2026-01-04 | Initial plan creation |
