# 🗂 FanPulse MVP IA (Information Architecture)

## 1) 목적
MVP 범위 내 정보 구조와 내비게이션 체계를 정리한다.

---

## 2) 전역 내비게이션 (Global Nav)
- Home
- Live
- Search
- My
- Settings

> 모바일: Bottom Tab + More(또는 My 내부 메뉴)  
> Web: 상단 탭 + 우측 My 메뉴

---

## 3) 페이지 맵 (MVP)
### 인증
- /login (H002, Google OAuth)

### 홈/라이브/뉴스
- / (H001)
- /live (H006)
- /live/:id (H019)
- /news (뉴스 목록)
- /news/:id (H011)

### 검색
- /search (H018)

### 마이/설정
- /mypage (H016)
- /settings (H010)

### 공통
- /error (H024)

---

## 4) 계층 구조(텍스트 트리)
Home
- Live
  - Live Detail
- News
  - News Detail

Search

My
- Settings

Error

---

## 5) 관련 문서
- `doc/mvp/mvp_PRD.md`
- `doc/mvp/mvp_user_journey.md`
- `doc/mvp/mvp_화면_정의서.md`

---

## 6) 변경 이력

| 버전  | 날짜       | 변경 내용                     |
| ----- | ---------- | ----------------------------- |
| 1.0.0 | 2026-01-03 | 최초 작성 (작성자: 정지원) |
| 1.1.0 | 2026-04-13 | /news 페이지 추가, 회원가입 탭 제거, Google OAuth로 변경 |
