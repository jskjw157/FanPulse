# 💽 FanPulse MVP 데이터베이스 정의서 (4주)

> 원본 전체 문서: `데이터베이스_정의서.md`  
> 본 문서는 **MVP(로그인/회원가입 + 라이브)** 범위만 발췌/축약합니다.

---

## 1. 원칙
- MVP는 **단일 PostgreSQL** 기준으로 정의합니다.
- 커뮤니티(MongoDB), 크롤링(crawled_*), 포인트/결제 등은 MVP 제외.

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
| token       | TEXT UNIQUE | 액세스 토큰 |
| expires_at  | TIMESTAMP | 만료 |

### 2.3 oauth_accounts
| 컬럼명          | 타입                 | 설명 |
|----------------|----------------------|------|
| id (PK)        | UUID                 | OAuth 계정 ID |
| user_id (FK)   | UUID                 | 사용자 ID |
| provider       | VARCHAR(20)          | `GOOGLE` |
| provider_user_id | VARCHAR(255)       | Google subject |
| email          | VARCHAR(100)         | Google email(선택) |
| created_at     | TIMESTAMP DEFAULT NOW() | 생성일 |

### 2.4 streaming_events
| 컬럼명        | 타입                 | 설명 |
|-------------|----------------------|------|
| id (PK)     | UUID                 | 스트리밍 이벤트 ID |
| title       | VARCHAR(255)         | 제목 |
| description | TEXT                 | 설명(선택) |
| stream_url  | TEXT                 | 임베드/원본 URL |
| thumbnail_url | TEXT               | 썸네일 |
| artist_name | VARCHAR(255)         | 아티스트명(초기엔 문자열로 단순화) |
| scheduled_at| TIMESTAMP            | 예정 시간 |
| started_at  | TIMESTAMP            | 시작 시간 |
| ended_at    | TIMESTAMP            | 종료 시간 |
| status      | VARCHAR(20)           | SCHEDULED/LIVE/ENDED |
| viewer_count| INT DEFAULT 0         | 시청자 수(선택) |
| created_at  | TIMESTAMP DEFAULT NOW() | 생성일 |

### 2.5 crawled_news
| 컬럼명       | 타입                    | 설명 |
|------------|-------------------------|------|
| id (PK)    | UUID                    | 뉴스 ID |
| title      | VARCHAR(255)            | 제목 |
| content    | TEXT                    | 본문/요약(초기엔 텍스트로 단순화) |
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
