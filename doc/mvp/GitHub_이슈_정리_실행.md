# ✅ GitHub 이슈 정리 실행 가이드 (MVP 기준)

대상 레포: `jskjw157/FanPulse`  
기준 문서: `docs/mvp/GitHub_이슈_분석.md`

---

## 0) 사전 조건 (중요)

- `gh`가 로그인되어 있어야 합니다. 현재 환경은 `gh auth status`가 **invalid token**으로 떠서, 먼저 재인증이 필요합니다.
  - 재인증: `gh auth login -h github.com`
  - 기존 계정 정리(필요 시): `gh auth logout -h github.com -u jskjw157`

> 보안상 토큰을 채팅에 붙여넣지 말고, 로컬에서 `gh auth login`으로 처리하세요.

---

## 1) Dry-run (변경 계획만 보기)

이미 받아둔 오픈 이슈 JSON을 기반으로 “무엇을 바꿀지”만 출력합니다.

```bash
python3 scripts/github_mvp_sync.py \
  --input-json .agent/cache/issues_open.json \
  --input-json .agent/cache/issues_open_page2.json
```

추가로 “MVP 누락 이슈 생성 목록”까지 같이 보고 싶으면:

```bash
python3 scripts/github_mvp_sync.py \
  --input-json .agent/cache/issues_open.json \
  --input-json .agent/cache/issues_open_page2.json \
  --create-missing
```

---

## 2) Apply (실제 GitHub 이슈 수정/생성)

### 2-1) 안전한 기본 적용(추천)

- `../document/*` 링크를 `../docs/*`로 교체
- 핵심 이슈(#16/#31/#41 등)는 “Google only” 방향으로 최소 정리(라인 제거 중심)
- MVP 대상 이슈들에 `mvp` 라벨 + `MVP (4주)` 마일스톤 부여
- `mvp/phase2/web` 라벨과 `MVP (4주)/Phase2` 마일스톤이 없으면 생성

```bash
python3 scripts/github_mvp_sync.py --apply --create-missing
```

### 2-2) (선택) MVP 밖 high-priority 라벨 정리까지

MVP가 아닌데 `high-priority`가 붙어 혼선을 만드는 이슈들을 `phase2`로 내리고 `high-priority`를 제거합니다.

```bash
python3 scripts/github_mvp_sync.py --apply --create-missing --demote-non-mvp-high-priority
```

---

## 3) 적용 후 확인

- `docs/mvp/GitHub_이슈_분석.md`에 적힌 “MVP 재사용 이슈”들이 `mvp`로 잘 묶였는지 확인
- 이슈 본문 링크가 `docs/`로 정상 이동되는지 확인
- 새로 생성된 MVP 누락 이슈가 `MVP (4주)` 마일스톤에 들어갔는지 확인

