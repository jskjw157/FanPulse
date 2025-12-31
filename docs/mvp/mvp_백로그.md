# 📋 FanPulse MVP 백로그 (4주)

> 원칙: 매주 “동작 데모”가 가능한 **버티컬 슬라이스**로 자른다.

---

## Sprint 1 (Week 1) — Skeleton + Contract

### 공통
- [PM/기획] MVP 포함/제외 확정, 수용기준/테스트케이스 초안
- [디자인] H001/H002/H006/H011/H016/H018/H019/H010 상태(로딩/에러/빈) 정의
- [백엔드: 정지원] `Identity/Live/Content` 컨텍스트 분리 + OpenAPI v1 초안

### iOS(김송) / Android(나유성) / Web(이승구/정지원)
- 라우팅: `/login` → `/` → `/live-detail` → `/news-detail` → `/search` → `/mypage` → `/settings`
- 더미 데이터로 Home/Live/News/Search/My/Settings UI 데모
- 로컬 저장: 최근 검색어/즐겨찾기 스키마만 잡기(구현은 후순위 가능)
- 공통 에러 처리 UX: 실패 시 `/error` 화면 전환(더미로라도 플로우 연결)
- Web: 모바일/데스크탑 2단계 반응형 레이아웃 뼈대(브레이크포인트만 먼저)

**데모**: 3플랫폼 동일 플로우 + 더미 임베드/뉴스 상세 화면

---

## Sprint 2 (Week 2) — Auth E2E (Email + Google)

### 백엔드(정지원)
- 회원가입/로그인 API + 토큰 발급
- 인증 미들웨어(인증 필요 엔드포인트 가드)
- Google 로그인 API(`/auth/google`) + ID Token 검증 + 계정 연동(oauth_accounts)
- `/me` (프로필 요약)

### 클라이언트
- 로그인/회원가입 연동 + 토큰 저장/세션 유지
- Google 로그인(1종) 연동(iOS/Android SDK, Web GIS) + 토큰 교환(`/auth/google`)  ※ Kakao/Naver/Apple 제외
- 실패/만료 처리 → `/error` 또는 로그인 리다이렉트
- 마이/설정 화면 최소 구현(프로필 요약 + 로그아웃)

**데모**: 이메일/비번 + Google 로그인으로 홈 진입 + 로그아웃

---

## Sprint 3 (Week 3) — Live/News E2E + Seed 적재

### 백엔드(정지원)
- 라이브 목록/상세 API + `streaming_events` 스키마 적용
- 뉴스 목록/상세 API + `crawled_news` 스키마 적용
- 검색 API(최소): Live/News 대상 `GET /search?q=...`

### 크롤링/적재(정지원, 나영민)
- MVP는 seed 기반 upsert(파일/시트 → DB)
- seed 파일 포맷 확정 + 운영 플로우(업데이트 주기) 문서화
- 라이브 데이터 소스는 MVP에서 **YouTube embed URL 고정**(자동 크롤링/YouTube API는 Next)
- (선택) 1시간 갱신 잡으로 `status/viewer_count` 갱신

### 클라이언트
- 실제 데이터로 Home(H001)/Live(H006/H019)/News(H011)/Search(H018) 연동

**데모**: 실데이터로 홈→라이브 재생 + 홈→뉴스 상세 + 검색 동작

---

## Sprint 4 (Week 4) — 로컬 즐겨찾기/최근검색 + QA + Release

### 공통
- 크래시/에러 로깅 최소 세팅, 성능 점검
- 회귀 체크리스트로 QA

### 크롤링/적재(정지원, 나영민) — Stretch 포함
- (Stretch A) Google News RSS → `crawled_news` upsert(일 1회) + 최신 N개 유지
- (Stretch B) YouTube 메타데이터 보강(가능하면): 제목/썸네일 업데이트 또는 상태 갱신

### 클라이언트
- 로컬 즐겨찾기(북마크) + 최근 검색어 저장/삭제
- 상태(로딩/에러/빈) 마감 정리
- 설정(H010): 로그아웃/앱 정보 등 “마감” 항목 확정 및 반영

### 배포
- Web: preview 배포
- iOS: TestFlight
- Android: 내부테스트 트랙

**데모**: 릴리즈 후보(RC)
