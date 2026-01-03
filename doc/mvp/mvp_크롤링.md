# 🕷️ FanPulse MVP 데이터 적재(4주) (Live/News)

> 원본 전체 문서: `크롤링.md`  
> MVP는 “라이브 기능”을 위해 필요한 **최소 적재 방식**만 정의합니다.

---

## 1. MVP 원칙
- 4주 MVP에서 가장 큰 리스크는 “외부 API/크롤링 불안정”이므로,
  - **1차(MVP)**: seed(큐레이션) 기반으로 `streaming_events`, `crawled_news`를 채움
  - **2차(다음 릴리즈)**: YouTube/News API 등으로 자동 갱신 확장

---

## 1.1 용어 정리: `streaming_events`가 뭐야?

- `streaming_events`는 MVP에서 **라이브 화면(H006/H019)** 에서 보여줄 “라이브 방송(예정/진행/종료)”의 **메타데이터**를 담는 테이블입니다.
- 핵심은 **임베드 재생 가능한 URL(`stream_url`)** 을 저장해두고, 앱은 `GET /live`, `GET /live/{id}`로 이를 받아서 화면을 구성합니다.

> 참고: `doc/mvp/mvp_데이터베이스_정의서.md`의 `streaming_events`, `doc/mvp/mvp_API_명세서.md`의 `GET /live/{id}` 응답 필드(`streamUrl`)

---

## 1.2 MVP 크롤링(적재) 범위가 “작은” 이유

- MVP는 “크롤링 기술” 자체가 목표가 아니라, **H001/H006/H019/H011에서 사용자에게 보여줄 데이터가 끊기지 않게** 만드는 게 목표입니다.
- 그래서 4주 MVP에선 불안정/변동이 큰 자동 크롤링(셀레니움/플레이wright/다중 사이트 파싱) 대신,
  - **운영 가능한 seed(큐레이션) → DB upsert**로 “항상 데이터가 있는 상태”를 먼저 보장합니다.
- 이 방향은 `doc/mvp/mvp_기획서.md`의 “seed 기반 적재” 결정과 동일합니다.

---

## 2. MVP 적재 방식(권장)

### 옵션 A: 수동 seed → DB 적재(가장 빠름)
- 운영자가 `seed_live_events.json`(또는 구글 시트 export) 형태로 이벤트를 관리
- 배포 시 또는 주기적으로 DB에 upsert

- 운영자가 `seed_news.json`(또는 구글 시트 export) 형태로 뉴스 목록을 관리
- 배포 시 또는 주기적으로 DB(`crawled_news`)에 upsert

### 옵션 B: 간단 스케줄러(차선)
- 1시간마다 seed 목록의 URL/상태만 점검해 `status/viewer_count` 갱신

- 1일 1회 seed 뉴스 목록을 upsert(또는 최신 N개만 유지)

---

## 2.1 MVP 라이브 데이터는 어디서 구해?

MVP는 자동 크롤링/YouTube API 연동을 하지 않고, 운영/기획이 **seed(큐레이션)** 로 수집합니다.

### MVP 권장(고정): YouTube만 지원
- MVP의 `stream_url`은 **YouTube embed URL**(예: `https://www.youtube.com/embed/VIDEO_ID`)을 사용합니다.
- Weverse Live 등은 임베드/재생 제약이 자주 있어서 **MVP에서는 제외(Next)** 로 두는 게 안전합니다.

### 수집 방법(가장 단순한 운영 플로우)
1. `seed_artists.json`으로 아티스트를 먼저 등록한다
2. “대상 채널 리스트”를 만든다 (아티스트/소속사/방송사 공식 채널)
3. 각 채널의 예정/진행 라이브를 확인한다(YouTube Live 탭/홈)
4. 라이브 링크에서 `VIDEO_ID`를 추출한다
   - `https://www.youtube.com/watch?v=VIDEO_ID` 형태
5. seed에 아래 필드를 채워 넣고 DB로 upsert한다

---

## 2.2 `seed_live_events` 포맷(권장)

### (A) JSON 파일 예시 (`seed_live_events.json`)

```json
[
  {
    "title": "2025 신년 팬미팅 라이브",
    "description": "새해를 맞아 팬들과 함께하는 특별한 시간",
    "artistId": "550e8400-e29b-41d4-a716-446655440099",
    "streamUrl": "https://www.youtube.com/embed/VIDEO_ID",
    "thumbnailUrl": "https://img.youtube.com/vi/VIDEO_ID/hqdefault.jpg",
    "status": "SCHEDULED",
    "scheduledAt": "2025-01-15T14:00:00Z"
  }
]
```

> DB 매핑 예: `streamUrl` → `stream_url`, `thumbnailUrl` → `thumbnail_url`, `artistId` → `artist_id`
> 선행 조건: `artists` 테이블에 아티스트를 먼저 upsert하고, `artistId`로 참조합니다.

### (B) JSON 파일 예시 (`seed_artists.json`)

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440099",
    "name": "아티스트명",
    "agency": "소속사",
    "genre": "K-POP",
    "fandomName": "팬덤명",
    "profileImageUrl": "https://cdn.fanpulse.app/artists/profile.jpg",
    "description": "아티스트 소개",
    "debutDate": "2013-06-13"
  }
]
```

> DB 매핑 예: `profileImageUrl` → `profile_image_url`, `fandomName` → `fandom_name`, `debutDate` → `debut_date`

### (C) 구글 시트 컬럼 예시(Export → JSON/CSV → upsert)
- `title` (필수)
- `artistId` (필수: `artists.id`)
- `streamUrl` (필수: YouTube embed URL)
- `thumbnailUrl` (선택)
- `status` (필수: `SCHEDULED`/`LIVE`/`ENDED`)
- `scheduledAt` (필수: ISO8601)
- `startedAt` (선택)
- `endedAt` (선택)
- `description` (선택)
- `viewerCount` (선택)

### Upsert 키(최소 규칙)
- MVP에서는 DB 컬럼이 단순하므로 **`streamUrl`을 유니크 키처럼 취급**해서 upsert하는 것을 권장합니다.
  - 같은 `streamUrl`이 들어오면: 메타데이터(title/status/time/viewerCount 등) 업데이트

---

## 2.3 `seed_news` 포맷(권장)

### JSON 파일 예시 (`seed_news.json`)
```json
[
  {
    "title": "아티스트 컴백 소식",
    "content": "요약(또는 1~2문장 발췌)",
    "thumbnailUrl": "https://cdn.fanpulse.app/news/thumbnail.jpg",
    "url": "https://example.com/news/123",
    "source": "Google News",
    "publishedAt": "2025-01-15T09:00:00Z"
  }
]
```

> DB 매핑 예: `publishedAt` → `published_at`, `thumbnailUrl` → `thumbnail_url`

### Upsert 키(최소 규칙)
- `url`을 유니크 키처럼 취급해서 upsert 권장(같은 기사면 업데이트)

---

## 2.4 MVP “확장(Stretch)” (Week 4 포함): 자동 수집을 조금만 더 넣는다

아래는 MVP 범위를 크게 넓히지 않으면서(= 화면/기능 추가 없음) 데이터 운영 부담을 줄이는 범위이며, **Week 4에 MVP 포함**으로 진행합니다.

### Stretch A: Google News RSS → `crawled_news` upsert
- 장점: 구현이 단순하고, “완전 수동 seed”보다 운영 부담이 낮음
- 범위: RSS 파싱 + 중복 제거(url 기준) + 최신 N개 유지
- 주의: 요약/NLP/정제 파이프라인(코어NLP/KoNLPy 등)은 MVP 제외

### Stretch B: YouTube 메타데이터 “보강”만 자동화
- 범위: seed로 받은 `streamUrl(=VIDEO_ID)`에 대해 썸네일/제목을 보강하거나 상태를 갱신
- 주의: YouTube Data API 키/쿼터/에러처리까지 포함되면 일정 리스크가 커서, **Week 4에서 Stretch A 완료 후** 착수 권장

---

## 3. API 요구사항(요약)
- 클라이언트는 `GET /live`, `GET /live/{id}`, `GET /news`, `GET /news/{id}`로 화면 구현 가능해야 함
- 라이브 상세는 `stream_url`만으로 임베드 재생 가능해야 함(= MVP에서는 YouTube embed URL 권장)

---

## 4. MVP 완료 기준(적재 관점)
- seed로 `streaming_events` / `crawled_news`가 채워지고, `GET /live`, `GET /news`에 **항상 0개 이상** 내려올 수 있다
- 운영 플로우(누가/어디서/어떤 포맷으로/얼마나 자주 업데이트하는지)가 문서화되어 있다
