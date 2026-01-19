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
