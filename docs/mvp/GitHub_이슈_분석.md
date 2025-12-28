# 🧾 FanPulse GitHub 이슈 분석 (MVP 기준)

분석 대상: `jskjw157/FanPulse` **Open issues 102개** (2025-12-28 기준, GitHub REST API로 수집)

---

## 1) 한 줄 요약

- 현재 GitHub 이슈는 **“전체 제품(커뮤니티/투표/차트/콘서트/리워드/인프라)” 중심**으로 쌓여 있고, 최신 `docs/mvp/*`의 **4주 MVP 범위(Auth+Live+News+Search+My/Settings+Error, iOS/Android/반응형 Web)** 와는 **우선순위/내용/문서 링크가 많이 어긋나 있음**.

---

## 2) 현재 이슈 상태 요약(정량)

### 라벨 분포(상위)
- `feature` 88
- `backend` 28
- `ios` 25
- `android` 25
- `crawling` 14
- `infra` 8
- `high-priority` 24

### 구조적 문제(메타)
- 이슈 본문에서 **`../document/*` 경로를 참조하는 이슈 32개** (현재 레포에는 `docs/*`가 최신)
- **MongoDB/pymongo 언급 13개**(예: #20, #1~#3, #90 등) → 현재 문서/의사결정은 **PostgreSQL + seed upsert(MVP)** 방향과 충돌
- `high-priority`가 **MVP 밖의 기능(커뮤니티/투표/차트/콘서트/푸시 등)** 에도 다수 붙어 있어, 4주 MVP 진행 시 혼선 가능

---

## 3) MVP와 “정합성 있는 이슈”(재사용 추천)

> MVP 정의: `docs/mvp/mvp_기획서.md` 기준(Email+Google, Live/News는 seed→DB upsert, iOS/Android/반응형 Web)

### 백엔드(정지원)
- #16 `[백엔드] OAuth 2.0 인증 시스템 구현` → **범위 수정 필요**: Kakao/Naver 제거, **Google + Email/Password**로 재정의
- #17 `[백엔드] JWT 토큰 관리` → **그대로 재사용 가능(단, 엔드포인트 네이밍은 `docs/mvp/mvp_API_계약.md`로 정렬)**
- #18 `[백엔드] 사용자 프로필 API` → **MVP는 `GET /me`(요약)만 먼저**, 수정/삭제는 Next로 미루는 게 안전
- #19 `[백엔드] PostgreSQL 스키마 마이그레이션` → **범위 축소 필요**: MVP 테이블만(Flyway/V1) 먼저, 나머지는 Next로 분리

### iOS(김송)
- #41 로그인/회원가입(H002) → **범위 수정 필요**: Google + Email/Password만, 문서 링크 `docs/*`로 교체
- #92 회원가입(H002-1)
- #42 메인(H001) → **차트/피드 등은 MVP 제외**, Live/News 카드 중심으로 조정
- #52 라이브 목록(H006), #71 라이브 상세(H019)
- #56 마이페이지(H016), #76 에러(H024)

### Android(나유성)
- #31 로그인/회원가입(H002) → **범위 수정 필요**: Google + Email/Password만, 문서 링크 `docs/*`로 교체
- #91 회원가입(H002-1)
- #32 메인(H001) → **차트/피드 등은 MVP 제외**, Live/News 카드 중심으로 조정
- #36 라이브 목록(H006), #65 라이브 상세(H019)
- #40 마이페이지(H016), #70 에러(H024)

### 네비게이션(선택)
- #107/#108 메뉴 드로어(H025) → MVP에 “꼭”은 아니지만, **플랫폼별 공통 네비게이션**이 필요하면
  - 드로어 항목을 MVP 화면만 남기고(Home/Live/Search/My/Settings/Logout) **스코프 축소**해서 재사용 추천

---

## 4) MVP 기준으로 “빠진 이슈”(추가 생성 추천)

현재 오픈 이슈만으로는 MVP를 끝까지 밀어붙이기 어려운 구멍이 있습니다.

### 공통/프로젝트
- `[MVP] 4주 스프린트/마일스톤 세팅` (Week1~4, 수용기준/데모 정의)
- `[Backend][MVP] DDD 모듈 구조(Identity/Live/Content) + OpenAPI v1 고정`

### 백엔드(필수 API 구멍)
- `[Backend][MVP] Auth: Email 회원가입/로그인 + /me`
- `[Backend][MVP] Auth: Google ID Token 검증 + 계정 연동(/auth/google)`
- `[Backend][MVP] Live: read-only 목록/상세 API (embed URL 기반)`  ※ AWS 스트리밍/시작/종료는 Next
- `[Backend][MVP] News: 목록/상세 API (seed 적재 데이터 read)`
- `[Backend][MVP] Search: Live/News 통합 검색 API (GET /search?q=...)`
- `[Crawling/Seed][MVP] seed(JSON/CSV/Sheet export) → PostgreSQL upsert 도구`  ※ “크롤링” 대신 운영 가능한 적재 도구

### 클라이언트(iOS/Android/Web)
- `[iOS][MVP] 검색(H018) 화면` (Live/News 통합 검색 + 최근 검색어 로컬)
- `[Android][MVP] 검색(H018) 화면`
- `[iOS][MVP] 설정(H010) 화면` (로그아웃/앱 정보 최소)
- `[Android][MVP] 설정(H010) 화면`
- `[iOS][MVP] 뉴스 상세(H011) 화면`
- `[Android][MVP] 뉴스 상세(H011) 화면`
- `[Web][MVP] MVP 전 화면 스켈레톤 + 반응형` (H001/H002/H002-1/H006/H019/H011/H018/H016/H010/H024)

---

## 5) MVP 기준 “정리(Defer/정정) 추천”

### (A) 문서 링크 정리
- 이슈 본문에서 `../document/*.md` → `../docs/*.md` 또는 `../docs/mvp/*.md`로 교체 필요

### (B) 데이터/DB 방향 정리
- #20 MongoDB 컬렉션 설계: **MVP 범위 밖 + DB 의사결정 충돌** → `Next`로 내리거나 `deprecated` 처리 권장
- #1~#5 뉴스 크롤링: MVP는 seed upsert이므로 **크롤러/스케줄러/정제는 Next**, 대신 “seed 적재 도구” 이슈로 대체 권장
- #90 라이브 메타데이터 크롤링: MVP는 seed upsert이므로 **YouTube API/크롤링은 Next**

### (C) 우선순위 라벨 재조정(혼선 제거)
- `high-priority`가 붙은 커뮤니티/투표/차트/콘서트/푸시 관련 이슈는 MVP에서 제외이므로 `high-priority` 해제 또는 `phase2` 라벨로 이동 권장

---

## 6) 바로 실행 가능한 “정리 체크리스트”

1. **MVP 라벨/마일스톤 추가**: `mvp`, `phase2`, `week1~week4`(또는 milestone) 중 택1
2. **MVP 재사용 이슈 1차 정정**: #16/#19/#31/#41/#32/#42/#36/#52의 “범위/문서 링크/소셜 종류” 수정
3. **MVP 누락 이슈 생성**: Web, News Detail, Search, Settings, Seed upsert, News API, Search API
4. **문서 경로 교체**: `document` → `docs` (32개)
5. **DB 충돌 정리**: MongoDB 관련(#20 및 pymongo 언급) Next로 격리

