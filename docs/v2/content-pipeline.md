# v2 — 콘텐츠 수집 파이프라인 (Content Pipeline)

**Status**: Skeleton (draft) — 실제 스펙은 착수 시 별도 문서 승격
**Last Updated**: 2026-04-20
**관련**: [백로그.md §3](백로그.md#3-콘텐츠-수집--데이터-파이프라인) · [roadmap.md](roadmap.md)

---

## 1. 범위

| 우선순위 | 항목 | 비고 |
| --- | --- | --- |
| P0 | 차트 크롤러 (Melon/Bugs/Spotify Top 등) | 콘텐츠 확장 첫 축 |
| P1 | 콘서트 크롤러 (인터파크/예스24/티켓링크) | 차트 파이프라인 재사용 |
| P2 | YouTube Data API — 메타데이터 보강 (Stretch B) | streamUrl → 썸네일/제목/상태 자동 갱신 |
| P2 | News API 확장 (Google News RSS, 해외 소스) | MVP Naver Open API 기반 연장 |
| Defer | NLP 정제 파이프라인 (KoNLPy 등) | 콘텐츠 볼륨 충분 후 |
| Defer | 임베딩 기반 아티스트 매칭 | 키워드 매칭 누락률 측정 후 |
| Defer | 광고 크롤러 | 광고 모델(P2) 기반 필요 |
| Defer | Weverse Live 임베드 | 정책/저작권 재조사 필요 |

---

## 2. 핵심 설계 질문

- [ ] **공통 크롤러 프레임워크**: 차트/콘서트를 관통하는 공통 스케줄러/리트라이/에러/알림 추상화 (Django Celery Beat vs 자체)
- [ ] **robots.txt/약관 준수**: 각 소스별 rate limit, 스크레이핑 약관 재검토
- [ ] **데이터 정합성**: 동일 콘서트/차트 항목 중복 제거 키 (UUID 매핑 테이블)
- [ ] **영속화 전략**: crawled_* 중간 테이블 → Spring sync 배치 (MVP news-sync 구조 재사용)
- [ ] **YouTube API 쿼터**: 일 10,000 units 한도 관리 — 우선순위 영상 선정 로직
- [ ] **임베딩 매칭 트리거**: 키워드 매칭 precision/recall 측정 방법 정의

---

## 3. 선행 의존성

- **MVP news-sync 배치 구조** (`PLAN_news-sync-batch.md`) — 차트/콘서트 sync 재사용
- **Django AI sidecar** — 기사/콘서트 요약 재사용
- **관측성**: Micrometer metrics 확장 (news_sync 패턴 → chart_sync/concert_sync)

---

## 4. Discovery Spike

- [ ] 차트 소스별 robots.txt/약관 확인 (Melon/Bugs/Spotify)
- [ ] 콘서트 소스별 데이터 구조/갱신 주기 조사
- [ ] YouTube Data API 쿼터 사용량 시뮬레이션 (영상 N건 × 갱신 주기)
- [ ] 임베딩 매칭 precision/recall 측정용 gold-set 설계 (뉴진스/NewJeans/NJZ 케이스 등)

---

## 5. 관련 작업

- MVP 원문: `docs/mvp/mvp_PRD.md:62`, `docs/mvp/mvp_크롤링.md:11,12,56,156,158-160`
- 관련 플랜: [`docs/plans/PLAN_news-sync-batch.md`](../plans/PLAN_news-sync-batch.md)
- 구현 이슈: (착수 시 기재)
