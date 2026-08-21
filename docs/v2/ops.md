# v2 — 운영 / 관측성 (Ops)

**Status**: Skeleton (draft) — 실제 스펙은 착수 시 별도 문서 승격
**Last Updated**: 2026-04-20
**관련**: [백로그.md §6](백로그.md#6-운영--관측성-ops) · [roadmap.md](roadmap.md)

---

## 1. 범위

| 우선순위 | 항목 | 비고 |
| --- | --- | --- |
| P2 | Grafana 대시보드 + 기본 Alerting | MVP Micrometer metrics 기반 |
| Defer | 로그 집계 / APM (Loki·Tempo·Sentry·DataDog) | 비용 대비 평가 후 |
| Defer | A/B 테스트 인프라 | 추천/피드 가설 생긴 후 |

> **Note**: Ops 항목은 **v2 핵심 서비스 기능은 아님**. "서비스 런칭 후 안정 운영을 위한 최소 관측성"만 v2 범위에 포함하고, APM·A/B는 별도 운영 로드맵으로 분리.

---

## 2. 핵심 설계 질문 (P2 범위)

- [ ] **Grafana 호스팅**: Self-host(Docker) vs Grafana Cloud 무료 티어 — 비용/유지보수
- [ ] **메트릭 수집 경로**: Prometheus scrape vs Grafana Agent vs OTLP
- [ ] **알림 채널**: Slack(개발팀) + PagerDuty/Opsgenie(온콜) — 단계적 도입
- [ ] **SLO 정의**: news_sync 성공률, API p95 레이턴시, AI sidecar availability
- [ ] **대시보드 범위**: news_sync 외 어떤 배치/API를 최초 커버? (인증/커뮤니티 작성률/실시간 동시접속)

---

## 3. Defer 항목 재평가 트리거

| 항목 | 재평가 시점 |
| --- | --- |
| 로그 집계/APM | v2.0 출시 후 장애 분석에서 "로그만으로 부족" 판단 시 |
| A/B 테스트 | 추천/피드 가설이 문서화되어 실험 대상이 3개 이상 쌓일 때 |

---

## 4. Discovery Spike (P2 착수 전)

- [ ] MVP Micrometer 등록 metric 목록 인벤토리 (`news_sync_*` 외 전체)
- [ ] Grafana Cloud 무료 티어 한도 확인 (시리즈 수, 저장 기간)
- [ ] SLO 초안 — 각 지표별 목표치와 번호(p95/p99) 합의
- [ ] 알림 피로도 방지 정책 — 중복/스팸 알림 억제 규칙

---

## 5. 관련 작업

- MVP 원문: 관련 직접 명시 없음, `PLAN_news-sync-batch.md` Task 4.5 / Risk 표 L699 참조
- 관련 플랜: [`docs/plans/PLAN_news-sync-batch.md`](../plans/PLAN_news-sync-batch.md)
- 구현 이슈: (착수 시 기재)
