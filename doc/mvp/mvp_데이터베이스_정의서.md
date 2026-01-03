# 💽 FanPulse MVP 데이터베이스 정의서 (4주)

> 원본 전체 문서: `데이터베이스_정의서.md`  
> 본 문서는 **MVP(로그인/회원가입 + 라이브 + 뉴스 + 아티스트)** 범위만 발췌/축약합니다.

---

## 1. 원칙
- MVP는 **단일 PostgreSQL** 기준으로 정의합니다.
- 커뮤니티(MongoDB), 크롤링 중 뉴스 외 항목(crawled_charts, crawled_concerts, crawled_ads 등), 포인트/결제 등은 MVP 제외.

---

## 2. 테이블

### 2.1 users
| 컬럼명        | 타입                 | 설명 |
|-------------|----------------------|------|
| id (PK)     | UUID                 | 사용자 ID |
| email       | VARCHAR(100) UNIQUE  | 이메일 (Google 로그인만 사용하는 경우 NULL 허용) |
| password_hash | TEXT               | 비밀번호 해시 (Google 로그인만 사용하는 경우 NULL 허용) |
| created_at  | TIMESTAMP DEFAULT NOW() | 생성일 |

> **비고**: Google 로그인만 사용하는 사용자도 존재할 수 있으므로 `email/password_hash`는 NULL 허용으로 운영하거나, 별도 identity 테이블로 분리합니다(MVP는 아래 `oauth_accounts`로 커버).

### 2.2 auth_tokens
| 컬럼명        | 타입      | 설명 |
|-------------|-----------|------|
| id (PK)     | UUID      | 토큰 ID |
| user_id (FK)| UUID      | 사용자 ID |
| access_token | TEXT UNIQUE | 액세스 토큰 |
| access_expires_at | TIMESTAMP | 만료 |

> **비고**: MVP는 액세스 토큰만 저장하며, 리프레시 토큰은 Next 단계에서 도입합니다.

### 2.3 oauth_accounts
| 컬럼명          | 타입                 | 설명 |
|----------------|----------------------|------|
| id (PK)        | UUID                 | OAuth 계정 ID |
| user_id (FK)   | UUID                 | 사용자 ID |
| provider       | VARCHAR(20)          | `GOOGLE` |
| provider_user_id | VARCHAR(255)       | Google subject |
| email          | VARCHAR(100)         | Google email(선택) |
| created_at     | TIMESTAMP DEFAULT NOW() | 생성일 |

### 2.4 artists
| 컬럼명            | 타입                 | 설명 |
|-----------------|----------------------|------|
| id (PK)         | UUID                 | 아티스트 고유 식별자 |
| name            | VARCHAR(100)         | 아티스트/그룹명 |
| debut_date      | DATE                 | 데뷔 날짜 |
| agency          | VARCHAR(100)         | 소속사 |
| genre           | VARCHAR(50)          | 장르 |
| fandom_name     | VARCHAR(50)          | 팬덤 명칭 |
| profile_image_url | TEXT               | 프로필 이미지 URL |
| description     | TEXT                 | 소개글 |
| created_at      | TIMESTAMP DEFAULT NOW() | 생성일 |

> **데이터 입력 방식**: MVP에서는 시드 데이터(seed data)로 초기 아티스트 목록을 입력합니다. 향후 관리자 페이지 구현 시 CRUD 기능을 추가할 예정입니다.

### 2.5 streaming_events
| 컬럼명        | 타입                 | 설명 |
|-------------|----------------------|------|
| id (PK)     | UUID                 | 스트리밍 이벤트 ID |
| title       | VARCHAR(255)         | 제목 |
| description | TEXT                 | 설명(선택) |
| stream_url  | TEXT                 | YouTube 임베드 URL (아래 참조) |
| thumbnail_url | TEXT               | 썸네일 |
| artist_id   | UUID                 | 아티스트 ID (artists.id) |
| scheduled_at| TIMESTAMP            | 예정 시간 |
| started_at  | TIMESTAMP            | 시작 시간 |
| ended_at    | TIMESTAMP            | 종료 시간 |
| status      | VARCHAR(20)           | SCHEDULED/LIVE/ENDED |
| viewer_count| INT DEFAULT 0         | 시청자 수(선택) |
| created_at  | TIMESTAMP DEFAULT NOW() | 생성일 |

#### stream_url 저장 형식

YouTube 임베드용 URL을 저장합니다. API 응답 시 그대로 반환됩니다.

| 항목 | 값 |
|------|-----|
| **저장 형식** | `https://www.youtube.com/embed/{VIDEO_ID}?rel=0&modestbranding=1&playsinline=1` |
| **예시** | `https://www.youtube.com/embed/dQw4w9WgXcQ?rel=0&modestbranding=1&playsinline=1` |

> **참고**: 원본 YouTube URL(`https://www.youtube.com/watch?v=VIDEO_ID`)이 아닌 임베드 URL 형식으로 저장해야 클라이언트에서 바로 사용 가능

### 2.6 crawled_news
| 컬럼명       | 타입                    | 설명 |
|------------|-------------------------|------|
| id (PK)    | UUID                    | 뉴스 ID |
| title      | VARCHAR(255)            | 제목 |
| content    | TEXT                    | 본문/요약 |
| thumbnail_url | TEXT                | 썸네일 이미지 URL(선택) |
| url        | VARCHAR(500)            | 원문 링크 |
| source     | VARCHAR(100)            | 출처 |
| published_at | TIMESTAMP             | 게시일 |
| created_at | TIMESTAMP DEFAULT NOW() | 적재 시각 |

---

## 3. MVP 인덱스(권장)
- `streaming_events(status, scheduled_at)`
- `users(email)`
- `oauth_accounts(provider, provider_user_id)`
- `crawled_news(published_at)`

---

## 4. 변경 이력

| 버전  | 날짜       | 변경 내용                     |
| ----- | ---------- | ----------------------------- |
| 1.0.0 | 2026-01-03 | 최초 작성 (작성자: 정지원) |
