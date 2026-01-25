# FanPulse Backend

FanPulse 백엔드는 K-Pop 아티스트의 YouTube 라이브 및 영상을 자동으로 발견하고 추적하는 실시간 스트리밍 메타데이터 서비스입니다.

## 프로젝트 개요

FanPulse는 Fan(팬)과 Pulse(맥박)의 합성어로, K-Pop 팬 커뮤니티의 실시간 스트리밍 정보를 자동으로 수집하고 제공합니다.

### 주요 기능

- **자동 라이브 발견**: yt-dlp를 이용해 YouTube 채널의 라이브/영상을 매시간 크롤링
- **메타데이터 관리**: 라이브 상태, 시청자 수, 썸네일 등을 주기적으로 갱신
- **내구성 있는 외부 연동**: Resilience4j의 Circuit Breaker와 Retry 패턴으로 안정성 확보
- **분산 환경 지원**: ShedLock으로 다중 인스턴스 환경에서 스케줄러 중복 실행 방지
- **모니터링 및 메트릭**: Prometheus 기반 메트릭으로 실시간 성능 추적

## 기술 스택

| 분류 | 기술 |
|------|------|
| 언어 | Kotlin 1.9.22 |
| 프레임워크 | Spring Boot 3.2.2 |
| 데이터베이스 | PostgreSQL 14 |
| 마이그레이션 | Flyway |
| 내구성 | Resilience4j (Circuit Breaker, Retry) |
| 크롤러 | yt-dlp |
| 분산 Lock | ShedLock 5.10.2 |
| 모니터링 | Micrometer + Prometheus |

## 커서 기반 페이지네이션 아키텍처 (Issue #133)

### 설계 결정사항 (Architecture Decision Records)

**Issue**: #133 라이브 목록 및 상세 API 구현
**관련 문서**: [PLAN_live-streaming-api-cursor-pagination.md](../docs/plans/PLAN_live-streaming-api-cursor-pagination.md)

#### 1. 복합 커서 (Composite Cursor)

**결정**: `(scheduledAt, id)` 복합 키 기반 커서 사용

**근거**:
- **안정성**: 이벤트 추가/삭제 시에도 페이지 경계 유지
- **정렬 보장**: 동일한 scheduledAt 값에서도 id로 완전한 정렬 순서 보장
- **인덱스 효율**: 데이터베이스 복합 인덱스 활용으로 빠른 쿼리
  - 인덱스: `idx_streaming_events_status_scheduled(status, scheduledAt DESC, id DESC)`

**트레이드오프**:
- 단순 ID 기반 커서보다 구현 복잡도 증가
- 해결: 명확한 KDoc과 테스트로 유지보수성 확보

#### 2. 배치 아티스트 조회 (Batch Artist Lookup)

**결정**: N+1 쿼리 문제 해결을 위해 배치 조회 구현

**메커니즘**:
```kotlin
// 1. 이벤트 목록 조회 (1 query)
val events = streamingEventPort.findWithCursor(status, limit, cursor)

// 2. 고유 아티스트 ID 추출
val artistIds = events.items.map { it.artistId }.distinct()

// 3. 모든 아티스트 이름을 1번의 쿼리로 조회
val artistNames = artistPort.findNamesByIds(artistIds)  // 1 query

// 4. Map을 이용한 상수 시간 조회
events.items.map { event ->
    StreamingEventListItem(
        artistName = artistNames[event.artistId] ?: "Unknown Artist"
    )
}
```

**성능 개선**:
- 커서 페이지네이션: O(limit)
- 아티스트 배치 조회: O(distinct_artists)
- 총합: O(limit + distinct_artists)
- 이전 개별 조회: O(limit * artists) ≈ O(limit * 20) = 20x 성능 개선

#### 3. Base64 JSON 인코딩

**결정**: 커서를 Base64 인코딩된 JSON으로 직렬화

```
JSON: {"scheduledAt":1704000000000,"id":"5633cdf3-1613-4163-a305-8c14a5143221"}
Base64: eyJzY2hlZHVsZWRBdCI6MTcwNDAwMDAwMDAwMCwiaWQiOiI1NjMzY2RmZi0xNjEzLTQxNjMtYTMwNS04YzE0YTUxNDMyMjEifQ==
```

**장점**:
- 디코딩 시 사람이 읽을 수 있는 JSON 형태로 디버깅 용이
- URL-safe Base64 인코딩 (RFC 4648)
- Jackson이나 추가 라이브러리 없이 Java 기본 Base64 클래스로 구현 가능

**트레이드오프**:
- 이진 인코딩보다 크기 약 2-3배 (하지만 URL 길이 제한 문제 없음)
- 정규표현식 기반 JSON 파싱 (성능 우려 없음, 작은 JSON)

#### 4. Limit + 1 페칭 전략

**결정**: `hasMore` 플래그 결정을 위해 limit보다 1개 많게 조회

```kotlin
// 요청: limit=20
// 실제 쿼리: SELECT ... LIMIT 21

val response = if (items.size > limit) {
    // 21개 조회됨 → 마지막 아이템으로 nextCursor 생성
    CursorPageResponse(
        items = items.take(limit),  // 처음 20개만 반환
        nextCursor = items[limit].encodeCursor(),  // 21번째 아이템으로 다음 커서
        hasMore = true
    )
} else {
    // limit개 이하 조회됨 → 마지막 페이지
    CursorPageResponse(
        items = items,
        nextCursor = null,
        hasMore = false
    )
}
```

**장점**:
- COUNT(*) 쿼리 불필요 (오프셋 페이지네이션의 성능 문제 해결)
- 초대형 테이블에서 매우 빠름
- O(limit) 복잡도

**트레이드오프**:
- 매 요청마다 1개 여분의 행 조회 (미미한 오버헤드)
- 구현 시 limit vs items.size 비교 로직 필요

#### 5. Hexagonal Architecture (포트/어댑터 패턴)

**결정**: 도메인 로직을 Spring/JPA에서 독립적으로 유지

**레이어 구조**:
```
Controller (Web)
    ↓
Service (Application)
    ↓
Port (Domain)
    ↓
Adapter (Infrastructure)
```

**파일 구조**:
- `domain/common/CursorPagination.kt`: 도메인 모델 (프레임워크 독립적)
- `application/dto/StreamingEventDtos.kt`: 응답 DTO
- `application/service/StreamingEventQueryServiceImpl.kt`: 비즈니스 로직
- `infrastructure/web/StreamingEventController.kt`: REST 엔드포인트
- `infrastructure/persistence/StreamingEventAdapter.kt`: 저장소 어댑터

**장점**:
- 도메인 로직이 Spring에 종속되지 않음
- 쉬운 테스트 (mock 객체 주입)
- 저장소 구현 변경 시 도메인 로직 영향 없음

**트레이드오프**:
- 파일/클래스 개수 증가 (5개 레이어)
- 초기 학습곡선 (하지만 장기적으로 유지보수성 개선)

### 관련 코드

**도메인 모델**:
- `/Users/ohchaeeun/source/FanPulse/backend/src/main/kotlin/com/fanpulse/domain/common/CursorPagination.kt`
  - `CursorPageRequest`: 커서 페이지 요청
  - `DecodedCursor`: 디코딩된 커서 (scheduledAt + id)
  - `CursorPageResult<T>`: 페이지 결과

**서비스 구현**:
- `/Users/ohchaeeun/source/FanPulse/backend/src/main/kotlin/com/fanpulse/application/service/streaming/StreamingEventQueryServiceImpl.kt`
  - `getWithCursor()`: 배치 아티스트 조회로 N+1 문제 해결

**컨트롤러**:
- `/Users/ohchaeeun/source/FanPulse/backend/src/main/kotlin/com/fanpulse/infrastructure/web/streaming/StreamingEventController.kt`
  - `getEventsWithCursor()`: 커서 기반 페이지네이션 엔드포인트
  - `getEventDetail()`: 이벤트 상세 조회

## 프로젝트 구조 (DDD)

```
backend/
├── src/main/kotlin/com/fanpulse/
│   ├── application/          # 애플리케이션 서비스 계층
│   │   └── service/
│   │       ├── LiveDiscoveryService
│   │       └── MetadataRefreshService
│   │
│   ├── domain/               # 도메인 모델 (비즈니스 로직)
│   │   ├── discovery/        # 스트림 발견 도메인
│   │   └── streaming/        # 스트리밍 이벤트 도메인
│   │
│   ├── infrastructure/       # 인프라 계층
│   │   ├── config/           # Spring 설정
│   │   ├── external/         # 외부 시스템 어댑터 (yt-dlp)
│   │   ├── scheduler/        # 스케줄 작업
│   │   └── persistence/      # JPA 저장소 구현
│   │
│   └── FanPulseApplication.kt
│
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/         # Flyway 마이그레이션
│
└── build.gradle.kts
```

## 시작하기

### 사전 요구사항

- Java 21+
- Docker & Docker Compose
- yt-dlp

### 1. yt-dlp 설치

```bash
# macOS
brew install yt-dlp

# Linux/Windows
pip install yt-dlp
```

### 2. PostgreSQL 실행

```bash
cd backend
docker-compose up -d
```

### 3. 환경 변수 설정

```bash
cp .env.example .env
```

기본값:
```
DB_HOST=localhost
DB_PORT=5432
DB_NAME=fanpulse
DB_USERNAME=fanpulse
DB_PASSWORD=fanpulse
```

### 4. 애플리케이션 실행

```bash
export $(cat .env | xargs) && ./gradlew bootRun
```

서버: http://localhost:8080

## 환경 변수

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `DB_HOST` | localhost | PostgreSQL 호스트 |
| `DB_PORT` | 5432 | PostgreSQL 포트 |
| `DB_NAME` | fanpulse | 데이터베이스 이름 |
| `DB_USERNAME` | fanpulse | DB 사용자명 |
| `DB_PASSWORD` | fanpulse | DB 비밀번호 |

## 데이터베이스 마이그레이션

Flyway가 애플리케이션 시작 시 자동 실행됩니다.

### 주요 마이그레이션

| 버전 | 설명 |
|------|------|
| V102 | artist_channels 테이블 생성 |
| V103 | streaming_events 컬럼 추가 (external_id, platform, source_url) |
| V104 | ShedLock 테이블 생성 |
| V105 | 22개 K-Pop 아티스트 시드 데이터 |

## 스케줄러

### Live Discovery Scheduler

매시간 활성 K-Pop 아티스트 채널에서 라이브/새 영상을 발견합니다.

**설정** (`application.yml`):
```yaml
fanpulse:
  scheduler:
    live-discovery:
      enabled: true
      cron: "0 0 * * * *"    # 매시간 정각
      max-concurrency: 5     # 동시 처리 5개
```

**처리 흐름**:
1. `artist_channels` 테이블에서 활성 YouTube 채널 조회
2. yt-dlp로 각 채널의 `/videos` 탭 크롤링 (병렬 처리)
3. 발견된 영상을 `streaming_events` 테이블에 저장/갱신
4. 채널의 `last_crawled_at` 업데이트

## yt-dlp 크롤러

### YtDlpStreamDiscoveryAdapter

YouTube 채널에서 영상 메타데이터를 추출합니다.

**설정**:
```yaml
fanpulse:
  discovery:
    ytdlp:
      command: yt-dlp
      timeout-ms: 30000      # 타임아웃 30초
      playlist-limit: 30     # 최대 30개 영상
      extract-flat: false
```

**추출 데이터**:
- `externalId`: YouTube 비디오 ID
- `title`: 영상 제목
- `status`: LIVE, SCHEDULED, ENDED
- `scheduledAt`, `startedAt`, `endedAt`
- `viewerCount`: 시청자 수
- `thumbnailUrl`: 썸네일

## 지원 아티스트 채널 (22개)

| 세대 | 아티스트 | 채널 |
|------|---------|------|
| **4세대 걸그룹** | NewJeans | @NewJeans_official |
| | aespa | @aespa |
| | IVE | @IVEstarship |
| | LE SSERAFIM | @le_sserafim |
| | (G)I-DLE | @G_I_DLE |
| | ITZY | @ITZY |
| **3세대 걸그룹** | BLACKPINK | @BLACKPINK |
| | TWICE | @TWICE |
| | Red Velvet | @RedVelvet |
| **4세대 보이그룹** | Stray Kids | @StrayKids |
| | ENHYPEN | @ENHYPEN |
| | TXT | @TOMORROW_X_TOGETHER |
| | ATEEZ | @ATEEZofficial |
| | THE BOYZ | @the_boyz |
| **3세대 보이그룹** | SEVENTEEN | @pledis17 |
| | NCT DREAM | @NCTDREAM |
| | NCT 127 | @NCTsmtown |
| | EXO | @weareone.EXO |
| **솔로** | IU | @dlwlrma |
| **기타** | RIIZE | @RIIZE_official |
| | Kep1er | @official_kep1er |
| | HYBE LABELS | @HYBELABELS |

## Circuit Breaker & Retry

### yt-dlp Circuit Breaker

```yaml
resilience4j:
  circuitbreaker:
    instances:
      ytdlp:
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50
        waitDurationInOpenState: 60s
```

- 10회 호출 중 5회 이상 실패 시 60초 차단
- 자동 복구 (HALF_OPEN → CLOSED)

### Retry

```yaml
resilience4j:
  retry:
    instances:
      ytdlp:
        maxAttempts: 3
        waitDuration: 2s
        enableExponentialBackoff: true
        exponentialBackoffMultiplier: 2
```

- 최대 3회 재시도
- 지수 백오프: 2초 → 4초

## API 문서 (Swagger)

애플리케이션 실행 후 Swagger UI에서 전체 API를 테스트할 수 있습니다.

| URL | 설명 |
|-----|------|
| http://localhost:8080/swagger-ui/index.html | Swagger UI (인터랙티브 문서) |
| http://localhost:8080/v3/api-docs | OpenAPI 3.0 JSON |
| http://localhost:8080/v3/api-docs.yaml | OpenAPI 3.0 YAML |

## API 엔드포인트

### Streaming Events API (MVP - Cursor-based Pagination)

| 엔드포인트 | 메서드 | 설명 |
|-----------|--------|------|
| `/api/v1/streaming-events` | GET | 스트리밍 이벤트 목록 (커서 기반 페이지네이션) |
| `/api/v1/streaming-events/{id}` | GET | 특정 스트리밍 이벤트 상세 정보 |

#### 커서 기반 페이지네이션 사용 가이드

**개념**: 커서 기반 페이지네이션은 오프셋 기반 페이지네이션보다 안정적이고 효율적입니다:
- 아이템 추가/삭제 시에도 페이지 경계가 안정적 유지
- 인덱스를 효율적으로 활용한 빠른 조회
- 대규모 데이터셋에서 COUNT 쿼리 불필요

**사용 예시**:

```bash
# 1. 첫 번째 페이지 요청 (limit=20)
curl "http://localhost:8080/api/v1/streaming-events?limit=20&status=LIVE"

# 응답:
# {
#   "success": true,
#   "data": {
#     "items": [
#       {
#         "id": "550e8400-e29b-41d4-a716-446655440000",
#         "title": "NewJeans LIVE Performance",
#         "artistId": "...",
#         "artistName": "NewJeans",
#         "thumbnailUrl": "https://...",
#         "status": "LIVE",
#         "scheduledAt": "2026-01-24T14:00:00Z",
#         "startedAt": "2026-01-24T14:05:00Z",
#         "viewerCount": 15000
#       },
#       ...
#     ],
#     "nextCursor": "eyJzY2hlZHVsZWRBdCI6MTcwNDAwMDAwMDAwMCwiaWQiOiI1NjMzY2RmZi0xNjEzLTQxNjMtYTMwNS04YzE0YTUxNDMyMjEifQ==",
#     "hasMore": true
#   }
# }

# 2. 다음 페이지 요청 (이전 응답의 nextCursor 사용)
curl "http://localhost:8080/api/v1/streaming-events?limit=20&status=LIVE&cursor=eyJzY2hlZHVsZWRBdCI6MTcwNDAwMDAwMDAwMCwiaWQiOiI1NjMzY2RmZi0xNjEzLTQxNjMtYTMwNS04YzE0YTUxNDMyMjEifQ=="

# 3. hasMore=false이거나 nextCursor=null이 될 때까지 반복
```

**쿼리 파라미터**:

| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| `limit` | integer | N | 페이지당 아이템 수 (1-50, 기본값 20) | `?limit=30` |
| `status` | enum | N | 상태 필터 (LIVE, SCHEDULED, ENDED) | `?status=LIVE` |
| `cursor` | string | N | 이전 응답의 nextCursor (첫 페이지에서는 생략) | `?cursor={encoded}` |

**응답 필드**:

| 필드 | 타입 | 설명 |
|------|------|------|
| `success` | boolean | 요청 성공 여부 |
| `data.items` | array | 현재 페이지의 스트리밍 이벤트 목록 |
| `data.nextCursor` | string | 다음 페이지의 커서 (null이면 마지막 페이지) |
| `data.hasMore` | boolean | 다음 페이지 존재 여부 |

**클라이언트 구현 예시 (JavaScript)**:

```javascript
async function fetchStreamingEvents(status = null, limit = 20) {
  let cursor = null;
  const allEvents = [];

  while (true) {
    // 커서 기반 페이지네이션으로 데이터 조회
    const params = new URLSearchParams({ limit });
    if (status) params.append('status', status);
    if (cursor) params.append('cursor', cursor);

    const response = await fetch(
      `/api/v1/streaming-events?${params.toString()}`
    );
    const json = await response.json();
    const page = json.data;

    allEvents.push(...page.items);

    // 더 이상 데이터가 없으면 종료
    if (!page.hasMore) break;

    // 다음 페이지의 커서로 업데이트
    cursor = page.nextCursor;
  }

  return allEvents;
}

// 사용
const liveEvents = await fetchStreamingEvents('LIVE');
```

**커서 메커니즘 상세**:

1. **Composite Cursor**: 커서는 `(scheduledAt, id)` 복합 키를 Base64 인코딩한 것
   - `scheduledAt`: 이벤트의 예정 시간 (UnixTimestamp in ms)
   - `id`: 이벤트의 UUID
   - 이 조합은 완전한 정렬 순서를 보장

2. **Limit + 1 Strategy**: 더 많은 데이터 존재 여부 판단
   - 요청한 limit보다 1개 많게 조회 (예: limit=20이면 21개 조회)
   - 21개가 반환되면 hasMore=true, 마지막 항목이 nextCursor가 됨
   - 20개 이하 반환되면 hasMore=false (마지막 페이지)
   - 실제 응답은 항상 limit개 이하

3. **N+1 Query Prevention**: 배치 아티스트 조회
   - 각 이벤트의 아티스트 이름을 효율적으로 조회
   - 단일 배치 쿼리로 모든 아티스트 정보 가져오기
   - 개별 쿼리 없음 (N+1 문제 해결)

### Identity API (인증 및 사용자 관리)

| 엔드포인트 | 메서드 | 설명 |
|-----------|--------|------|
| `/api/v1/auth/register` | POST | 회원가입 |
| `/api/v1/auth/login` | POST | 로그인 |
| `/api/v1/auth/validate` | POST | 토큰 검증 |

자세한 API 명세는 [API Specification v1](doc/api/api-spec-v1.md)을 참조하세요.

### Actuator

| 엔드포인트 | 설명 |
|-----------|------|
| `GET /actuator/health` | 헬스 체크 |
| `GET /actuator/metrics` | 메트릭 조회 |
| `GET /actuator/prometheus` | Prometheus 형식 |
| `GET /actuator/circuitbreakers` | Circuit Breaker 상태 |

## 에러 처리 (RFC 7807)

FanPulse API는 [RFC 7807 Problem Details for HTTP APIs](https://www.rfc-editor.org/rfc/rfc7807) 표준을 따릅니다.

### 에러 응답 구조

모든 에러는 `application/problem+json` Content-Type으로 반환됩니다:

```json
{
  "type": "https://api.fanpulse.com/errors/email-already-exists",
  "title": "Email Already Exists",
  "status": 409,
  "detail": "The email 'user@example.com' is already registered",
  "instance": "/api/v1/auth/register",
  "timestamp": "2026-01-19T22:10:00Z",
  "errorCode": "EMAIL_ALREADY_EXISTS",
  "errors": [
    {
      "field": "email",
      "code": "already_exists",
      "message": "Email already exists",
      "rejectedValue": "user@example.com"
    }
  ]
}
```

### 주요 필드

| 필드 | 설명 | 필수 |
|------|------|------|
| `type` | 에러 타입을 식별하는 URI | O |
| `title` | 사람이 읽을 수 있는 에러 요약 | O |
| `status` | HTTP 상태 코드 | O |
| `detail` | 이번 에러 발생에 대한 구체적 설명 | X |
| `instance` | 에러가 발생한 요청 URI | X |
| `timestamp` | 에러 발생 시각 (ISO 8601) | O |
| `errorCode` | 머신 리더블한 에러 코드 | X |
| `errors` | 필드별 검증 에러 목록 | X |

### 에러 타입 목록

#### 인증 에러 (401)

- `INVALID_CREDENTIALS`: 잘못된 이메일 또는 비밀번호
- `INVALID_TOKEN`: 유효하지 않은 토큰
- `TOKEN_EXPIRED`: 만료된 토큰

#### 검증 에러 (400)

- `VALIDATION_FAILED`: 요청 데이터 검증 실패
- `INVALID_REQUEST`: 잘못된 요청 형식
- `INVALID_PASSWORD`: 비밀번호 정책 위반

#### Not Found 에러 (404)

- `USER_NOT_FOUND`: 사용자를 찾을 수 없음
- `RESOURCE_NOT_FOUND`: 리소스를 찾을 수 없음

#### 충돌 에러 (409)

- `EMAIL_ALREADY_EXISTS`: 이메일 중복
- `USERNAME_ALREADY_EXISTS`: 사용자명 중복

#### 서버 에러 (500)

- `INTERNAL_ERROR`: 내부 서버 오류

### 프론트엔드 에러 처리 예시

#### TypeScript (Axios)

```typescript
import axios from 'axios';

interface ProblemDetail {
  type: string;
  title: string;
  status: number;
  detail?: string;
  instance?: string;
  timestamp: string;
  errorCode?: string;
  errors?: Array<{
    field: string;
    code: string;
    message: string;
    rejectedValue?: any;
  }>;
}

async function registerUser(email: string, password: string) {
  try {
    const response = await axios.post('/api/v1/auth/register', {
      email,
      password,
      username: 'user123'
    });
    return response.data;
  } catch (error) {
    if (axios.isAxiosError(error) && error.response) {
      const problem: ProblemDetail = error.response.data;

      // 에러 타입별 처리
      switch (problem.errorCode) {
        case 'EMAIL_ALREADY_EXISTS':
          console.error('이미 사용 중인 이메일입니다.');
          break;
        case 'VALIDATION_FAILED':
          problem.errors?.forEach(err => {
            console.error(`${err.field}: ${err.message}`);
          });
          break;
        default:
          console.error(`에러 발생: ${problem.detail}`);
      }
    }
    throw error;
  }
}
```

#### JavaScript (Fetch API)

```javascript
async function registerUser(email, password) {
  try {
    const response = await fetch('/api/v1/auth/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ email, password, username: 'user123' })
    });

    if (!response.ok) {
      const problem = await response.json();

      if (problem.errorCode === 'EMAIL_ALREADY_EXISTS') {
        alert('이미 사용 중인 이메일입니다.');
      } else if (problem.errorCode === 'VALIDATION_FAILED') {
        const fieldErrors = problem.errors
          .map(err => `${err.field}: ${err.message}`)
          .join('\n');
        alert(fieldErrors);
      } else {
        alert(problem.detail || problem.title);
      }

      throw new Error(problem.detail);
    }

    return await response.json();
  } catch (error) {
    console.error('Registration failed:', error);
    throw error;
  }
}
```

### 에러 처리 Best Practices

1. **errorCode 기반 분기**: `errorCode` 필드를 사용하여 클라이언트에서 에러 타입 식별
2. **필드 에러 표시**: `errors` 배열을 순회하여 각 필드별 검증 메시지 출력
3. **사용자 친화적 메시지**: `detail` 필드를 그대로 사용하거나 i18n으로 번역
4. **trace ID 활용**: 서버 로그와 매칭하기 위해 `traceId` 저장 (있는 경우)
5. **재시도 로직**: 500번대 에러는 exponential backoff로 재시도 고려

## 테스트

```bash
# 전체 테스트
./gradlew test

# 특정 테스트
./gradlew test --tests "*YtDlp*"
```

## 문제 해결

### yt-dlp 타임아웃

```yaml
fanpulse:
  discovery:
    ytdlp:
      timeout-ms: 60000  # 60초로 증가
```

### Circuit Breaker OPEN

- **원인**: 연속 실패로 Circuit Breaker 열림
- **해결**: 60초 후 자동 복구 또는 `/actuator/circuitbreakers` 확인

### 데이터베이스 연결 실패

```bash
# PostgreSQL 재시작
docker-compose down && docker-compose up -d
```

### 스케줄러 미실행

```yaml
# application.yml 확인
fanpulse:
  scheduler:
    live-discovery:
      enabled: true
```

## 로깅

```yaml
logging:
  level:
    com.fanpulse: DEBUG
```

주요 로그:
```
INFO  Starting live discovery at 2026-01-12T14:00:00Z
DEBUG Executing yt-dlp for https://www.youtube.com/@NewJeans_official/videos
INFO  Live discovery completed in 12345ms: total=150, upserted=42, failed=0
```

## 개발 가이드

### 로컬 개발 환경

```bash
git clone https://github.com/your-org/FanPulse.git
cd FanPulse/backend
docker-compose up -d
./gradlew bootRun
```

### 코드 스타일

- Kotlin 공식 스타일 가이드 준수
- ktlint: `./gradlew ktlintFormat`
- detekt: `./gradlew detekt`

### 새 기능 추가 절차

1. Domain 모델 설계 (DDD)
2. Port 인터페이스 정의
3. Application Service 구현
4. Infrastructure Adapter 구현
5. 테스트 작성 (TDD)

## 라이센스

MIT License
