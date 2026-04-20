# v2 — 인증 / 계정 (Auth)

**Status**: Skeleton (draft) — 실제 스펙은 착수 시 별도 문서 승격
**Last Updated**: 2026-04-20
**관련**: [백로그.md §1](백로그.md#1-인증--계정-auth) · [roadmap.md](roadmap.md)

---

## 1. 범위

| 우선순위 | 항목 | 비고 |
| --- | --- | --- |
| P0 | 이메일 회원가입 | Google OAuth 단독에서 벗어남 |
| P1 | 추가 소셜 로그인 (Kakao → Naver → Apple) | Kakao 선행 (국내 유입) |
| Defer | 계정 동기화 (저장글/최근검색어) | 로컬 저장 이탈률 관찰 후 |

---

## 2. 핵심 설계 질문 (착수 전 결정 필요)

- [ ] **계정 통합 전략**: Google OAuth 기존 계정 + 이메일 신규 가입 시 동일 이메일 충돌 처리? (자동 머지 vs 사용자 선택)
- [ ] **비밀번호 정책**: 최소 길이/복잡도/주기 변경/해시 알고리즘 (Argon2id 권장)
- [ ] **이메일 인증 플로우**: 가입 즉시 활성화 vs 이메일 인증 후 활성화
- [ ] **메일러 인프라**: SES vs SendGrid vs 국내(네이버 웍스) — 스팸/전송률 비교
- [ ] **소셜 로그인 신원 확정**: provider마다 email 미제공 케이스(Apple private relay 등) 대응
- [ ] **약관/개인정보 동의**: provider 추가 시 약관 재동의 플로우

---

## 3. 선행 의존성

- **인프라**: 메일러 선택/셋업 (비용, DNS SPF/DKIM/DMARC)
- **법무**: 개인정보처리방침 업데이트 (소셜 provider 추가 시마다)
- **보안**: 비밀번호 저장 정책, rate limiting (로그인 시도)

---

## 4. Discovery Spike (착수 전)

- [ ] Google OAuth 기존 유저 데이터 구조 확인 — 이메일 머지 가능성 조사
- [ ] Kakao/Naver/Apple OAuth provider 별 email/identity 필드 차이 정리
- [ ] 메일러 비용 시뮬레이션 (MVP WAU × 3 가정)

---

## 5. 관련 작업

- MVP 원문: `docs/mvp/mvp_PRD.md:36`, `docs/mvp/mvp_기획서.md:19,89`, `docs/mvp/mvp_기획서.md:30-31`
- 구현 이슈: (착수 시 기재)
