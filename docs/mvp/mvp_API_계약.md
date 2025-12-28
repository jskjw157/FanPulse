# 🔌 FanPulse MVP API 계약

> 대상: iOS / Android / Responsive Web 공통
> 버전: v1.0 (MVP)

---

## 1. 공통 규칙

### 1.1 Base URL
```
https://api.fanpulse.app/api/v1
```

### 1.2 인증
- 인증이 필요한 API는 `🔒` 표시
- Header: `Authorization: Bearer <access_token>`
- 토큰 만료 시 401 응답 → 클라이언트에서 재로그인 유도

### 1.3 공통 Response 형식

**성공 응답**
```json
{
  "success": true,
  "data": { ... }
}
```

**에러 응답**
```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "사용자에게 표시할 메시지"
  }
}
```

### 1.4 HTTP 상태 코드

| 코드 | 설명 |
|------|------|
| 200 | 성공 |
| 201 | 생성 성공 |
| 400 | 잘못된 요청 (유효성 검사 실패) |
| 401 | 인증 실패 (토큰 없음/만료) |
| 403 | 권한 없음 |
| 404 | 리소스 없음 |
| 409 | 충돌 (중복 등) |
| 500 | 서버 오류 |

### 1.5 에러 코드 목록

| 코드 | HTTP | 설명 |
|------|------|------|
| `AUTH_INVALID_CREDENTIALS` | 401 | 이메일/비밀번호 불일치 |
| `AUTH_TOKEN_EXPIRED` | 401 | 토큰 만료 |
| `AUTH_TOKEN_INVALID` | 401 | 유효하지 않은 토큰 |
| `AUTH_EMAIL_EXISTS` | 409 | 이미 가입된 이메일 |
| `AUTH_GOOGLE_FAILED` | 401 | Google 토큰 검증 실패 |
| `VALIDATION_ERROR` | 400 | 입력값 유효성 검사 실패 |
| `NOT_FOUND` | 404 | 리소스를 찾을 수 없음 |
| `SERVER_ERROR` | 500 | 서버 내부 오류 |

### 1.6 페이지네이션

Cursor 기반 페이지네이션 사용 (무한 스크롤 대응)

**Request Query**
```
?limit=20&cursor=eyJpZCI6MTIzfQ
```

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| limit | number | N | 조회 개수 (기본값: 20, 최대: 50) |
| cursor | string | N | 다음 페이지 커서 (첫 페이지는 생략) |

**Response**
```json
{
  "items": [...],
  "nextCursor": "eyJpZCI6MTAwfQ",
  "hasMore": true
}
```

---

## 2. Identity Context (인증/사용자)

### 2.1 POST `/auth/signup` - 회원가입

이메일/비밀번호로 회원가입

**Request Body**
```json
{
  "email": "user@example.com",
  "password": "Password123!",
  "passwordConfirm": "Password123!"
}
```

| 필드 | 타입 | 필수 | 유효성 검사 |
|------|------|------|-------------|
| email | string | Y | 이메일 형식, 최대 100자 |
| password | string | Y | 최소 8자, 영문+숫자+특수문자 |
| passwordConfirm | string | Y | password와 일치 |

**Response 201**
```json
{
  "success": true,
  "data": {
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "email": "user@example.com",
      "createdAt": "2025-01-15T09:00:00Z"
    }
  }
}
```

**에러 케이스**
| 상황 | HTTP | 코드 |
|------|------|------|
| 이메일 형식 오류 | 400 | `VALIDATION_ERROR` |
| 비밀번호 불일치 | 400 | `VALIDATION_ERROR` |
| 이미 가입된 이메일 | 409 | `AUTH_EMAIL_EXISTS` |

---

### 2.2 POST `/auth/login` - 로그인

이메일/비밀번호로 로그인

**Request Body**
```json
{
  "email": "user@example.com",
  "password": "Password123!"
}
```

| 필드 | 타입 | 필수 |
|------|------|------|
| email | string | Y |
| password | string | Y |

**Response 200**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 3600,
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "email": "user@example.com"
    }
  }
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| accessToken | string | JWT 액세스 토큰 |
| expiresIn | number | 토큰 만료 시간 (초) |
| user | object | 사용자 기본 정보 |

**에러 케이스**
| 상황 | HTTP | 코드 |
|------|------|------|
| 이메일/비밀번호 불일치 | 401 | `AUTH_INVALID_CREDENTIALS` |
| 존재하지 않는 계정 | 401 | `AUTH_INVALID_CREDENTIALS` |

---

### 2.3 POST `/auth/google` - Google 로그인

Google ID Token으로 로그인/회원가입

**Request Body**
```json
{
  "idToken": "eyJhbGciOiJSUzI1NiIs..."
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| idToken | string | Y | Google Sign-In에서 받은 ID Token |

**Response 200**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 3600,
    "isNewUser": false,
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "email": "user@gmail.com"
    }
  }
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| isNewUser | boolean | 신규 가입 여부 (true면 첫 로그인) |

**에러 케이스**
| 상황 | HTTP | 코드 |
|------|------|------|
| Google 토큰 검증 실패 | 401 | `AUTH_GOOGLE_FAILED` |

---

### 2.4 POST `/auth/logout` 🔒 - 로그아웃

현재 세션 로그아웃 (서버에서 토큰 무효화)

**Request Header**
```
Authorization: Bearer <access_token>
```

**Response 200**
```json
{
  "success": true,
  "data": {
    "message": "로그아웃되었습니다."
  }
}
```

---

### 2.5 GET `/me` 🔒 - 내 정보 조회

현재 로그인한 사용자 정보 조회

**Response 200**
```json
{
  "success": true,
  "data": {
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "email": "user@example.com",
      "provider": "EMAIL",
      "createdAt": "2025-01-15T09:00:00Z"
    }
  }
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| id | string (UUID) | 사용자 고유 ID |
| email | string | 이메일 |
| provider | string | 가입 방식 (`EMAIL` / `GOOGLE`) |
| createdAt | string (ISO8601) | 가입일 |

---

## 3. Live Context (라이브 스트리밍)

### 3.1 GET `/live` - 라이브 목록

라이브 스트리밍 목록 조회

**Request Query**
```
GET /live?status=LIVE&limit=20&cursor=xxx
```

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| status | string | N | `LIVE` / `SCHEDULED` / `ENDED` (미지정시 전체) |
| limit | number | N | 조회 개수 (기본 20) |
| cursor | string | N | 페이지네이션 커서 |

**Response 200**
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440001",
        "title": "2025 신년 팬미팅 라이브",
        "artistName": "아티스트명",
        "thumbnailUrl": "https://cdn.fanpulse.app/thumbnails/xxx.jpg",
        "status": "LIVE",
        "scheduledAt": "2025-01-15T14:00:00Z",
        "startedAt": "2025-01-15T14:00:05Z",
        "viewerCount": 15234
      }
    ],
    "nextCursor": "eyJpZCI6MTAwfQ",
    "hasMore": true
  }
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| id | string (UUID) | 라이브 ID |
| title | string | 라이브 제목 |
| artistName | string | 아티스트명 |
| thumbnailUrl | string | 썸네일 이미지 URL |
| status | string | 상태 (`SCHEDULED` / `LIVE` / `ENDED`) |
| scheduledAt | string (ISO8601) | 예정 시간 |
| startedAt | string? (ISO8601) | 시작 시간 (LIVE/ENDED만) |
| viewerCount | number | 현재 시청자 수 |

---

### 3.2 GET `/live/{id}` - 라이브 상세

라이브 상세 정보 조회 (임베드 URL 포함)

**Response 200**
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "title": "2025 신년 팬미팅 라이브",
    "description": "새해를 맞아 팬들과 함께하는 특별한 시간",
    "artistName": "아티스트명",
    "thumbnailUrl": "https://cdn.fanpulse.app/thumbnails/xxx.jpg",
    "streamUrl": "https://www.youtube.com/embed/VIDEO_ID",
    "status": "LIVE",
    "scheduledAt": "2025-01-15T14:00:00Z",
    "startedAt": "2025-01-15T14:00:05Z",
    "endedAt": null,
    "viewerCount": 15234,
    "createdAt": "2025-01-10T09:00:00Z"
  }
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| streamUrl | string | 임베드 플레이어 URL (YouTube 등) |
| description | string? | 라이브 상세 설명 |
| endedAt | string? (ISO8601) | 종료 시간 (ENDED만) |

**에러 케이스**
| 상황 | HTTP | 코드 |
|------|------|------|
| 존재하지 않는 라이브 | 404 | `NOT_FOUND` |

---

## 4. Content Context (뉴스)

### 4.1 GET `/news` - 뉴스 목록

뉴스 목록 조회

**Request Query**
```
GET /news?limit=20&cursor=xxx
```

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| limit | number | N | 조회 개수 (기본 20) |
| cursor | string | N | 페이지네이션 커서 |

**Response 200**
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440002",
        "title": "아티스트, 새 앨범 발매 예정",
        "summary": "오는 2월 새 앨범 발매를 앞두고...",
        "thumbnailUrl": "https://cdn.fanpulse.app/news/xxx.jpg",
        "source": "팬펄스 뉴스",
        "publishedAt": "2025-01-14T10:30:00Z"
      }
    ],
    "nextCursor": "eyJpZCI6MTAwfQ",
    "hasMore": true
  }
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| id | string (UUID) | 뉴스 ID |
| title | string | 뉴스 제목 |
| summary | string | 요약 (최대 100자) |
| thumbnailUrl | string? | 썸네일 이미지 URL |
| source | string | 출처 |
| publishedAt | string (ISO8601) | 게시일 |

---

### 4.2 GET `/news/{id}` - 뉴스 상세

뉴스 상세 조회

**Response 200**
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440002",
    "title": "아티스트, 새 앨범 발매 예정",
    "content": "오는 2월 새 앨범 발매를 앞두고 있는 아티스트가...(전체 본문)",
    "thumbnailUrl": "https://cdn.fanpulse.app/news/xxx.jpg",
    "source": "팬펄스 뉴스",
    "sourceUrl": "https://original-source.com/article/123",
    "publishedAt": "2025-01-14T10:30:00Z",
    "createdAt": "2025-01-14T11:00:00Z"
  }
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| content | string | 뉴스 전체 본문 |
| sourceUrl | string | 원문 링크 |

**에러 케이스**
| 상황 | HTTP | 코드 |
|------|------|------|
| 존재하지 않는 뉴스 | 404 | `NOT_FOUND` |

---

## 5. Search Context (검색)

### 5.1 GET `/search` - 통합 검색

라이브/뉴스 통합 검색

**Request Query**
```
GET /search?q=아티스트&limit=10
```

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| q | string | Y | 검색어 (최소 2자) |
| limit | number | N | 카테고리별 조회 개수 (기본 10) |

**Response 200**
```json
{
  "success": true,
  "data": {
    "live": {
      "items": [
        {
          "id": "...",
          "title": "아티스트 팬미팅 라이브",
          "artistName": "아티스트",
          "thumbnailUrl": "...",
          "status": "SCHEDULED",
          "scheduledAt": "2025-01-20T14:00:00Z"
        }
      ],
      "totalCount": 5
    },
    "news": {
      "items": [
        {
          "id": "...",
          "title": "아티스트 새 앨범 소식",
          "summary": "...",
          "source": "팬펄스 뉴스",
          "publishedAt": "2025-01-14T10:30:00Z"
        }
      ],
      "totalCount": 23
    }
  }
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| live | object | 라이브 검색 결과 |
| news | object | 뉴스 검색 결과 |
| totalCount | number | 전체 검색 결과 수 |

**에러 케이스**
| 상황 | HTTP | 코드 |
|------|------|------|
| 검색어 2자 미만 | 400 | `VALIDATION_ERROR` |

---

## 6. MVP 제외 (Next Phase)

다음 API는 MVP 이후 구현 예정:

- `POST /auth/apple` - Apple 로그인
- `POST /auth/kakao` - Kakao 로그인
- `PUT /me` - 프로필 수정
- `GET /community/*` - 커뮤니티 API
- `GET /rewards/*` - 리워드/포인트 API
- `WS /live/{id}/chat` - 실시간 채팅

---

## 7. Appendix

### 7.1 날짜/시간 형식
- 모든 날짜/시간은 **ISO 8601** 형식 사용
- 타임존: **UTC** (클라이언트에서 로컬 변환)
- 예: `2025-01-15T14:00:00Z`

### 7.2 JWT 토큰 구조 (참고)
```json
{
  "sub": "user-uuid",
  "email": "user@example.com",
  "iat": 1705312800,
  "exp": 1705316400
}
```

### 7.3 API 버전 관리
- URL Path 방식: `/api/v1/...`
- Breaking change 시 v2로 버전업
- v1은 최소 6개월 유지 후 deprecation
