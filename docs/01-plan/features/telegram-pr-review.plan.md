# Plan: Claude Code Telegram Channel PR Review

> Created: 2026-03-22
> Status: Draft
> Replaces: `.github/workflows/ai-code-review.yml` (Gemini 기반)

## 1. 목적

현재 Gemini 2.5 Flash 기반 AI Code Review를 **Claude Code + Telegram Channel**로 교체한다.
Gemini가 PR diff 외 레거시 코드를 리뷰하여 false positive가 발생하고, 이로 인해 PR 머지가 차단되는 문제를 해결한다.

### 핵심 목표
- Claude Code가 PR 리뷰를 직접 수행 (로컬 코드베이스 전체 접근 가능)
- Telegram을 이벤트 브릿지로 사용 (ngrok/tunnel 불필요)
- ANTHROPIC_API_KEY 없이 claude.ai 로그인만으로 동작
- GitHub 커밋 상태 설정으로 머지 차단/허용 제어

## 2. 현재 상태

| 항목 | 현재 |
|------|------|
| 리뷰 모델 | Gemini 2.5 Flash (`--gemini-only`) |
| 리뷰 스크립트 | `script/ai_pr_reviewer.py` (72KB, 과도하게 복잡) |
| 워크플로우 | `.github/workflows/ai-code-review.yml` |
| 인증 | `GEMINI_API_KEY` (GitHub Secrets) |
| 상태 체크 | job name "AI Code Review" → Repository Ruleset "AI Review Protection" (id: 12174915) |
| 문제점 | diff 외 레거시 코드(`ai/` 디렉토리) 리뷰, false positive로 머지 차단 |

## 3. 목표 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│  GitHub                                                      │
│                                                              │
│  PR 이벤트 (opened/synchronize/reopened)                      │
│       │                                                      │
│       ▼                                                      │
│  GitHub Action (경량)                                         │
│  ┌──────────────────────────────────────┐                    │
│  │ 1. gh pr diff → pr_info 생성         │                    │
│  │ 2. curl → Telegram Bot API           │                    │
│  │ 3. 리뷰 완료 대기 (polling)            │                    │
│  │ 4. 결과에 따라 exit 0 / exit 1        │                    │
│  └──────────────────────────────────────┘                    │
└──────────────┬───────────────────────────────────────────────┘
               │ Telegram Bot API
               ▼
┌─────────────────────────────────────────────────────────────┐
│  Telegram 서버                                               │
│  (메시지 중계, 폴링 방식으로 Claude Code가 수신)                  │
└──────────────┬───────────────────────────────────────────────┘
               │ 폴링 (공인 IP 불필요)
               ▼
┌─────────────────────────────────────────────────────────────┐
│  로컬 머신 (Mac)                                              │
│                                                              │
│  Claude Code (--channels plugin:telegram)                    │
│  ┌──────────────────────────────────────┐                    │
│  │ 1. Telegram 메시지 수신 (PR 정보)      │                    │
│  │ 2. gh pr diff {PR번호} 실행           │                    │
│  │ 3. 로컬 코드베이스 참조하여 리뷰         │                    │
│  │ 4. gh pr comment → 리뷰 코멘트 게시    │                    │
│  │ 5. gh api → 커밋 상태 설정             │                    │
│  │    (success/failure)                  │                    │
│  └──────────────────────────────────────┘                    │
└─────────────────────────────────────────────────────────────┘
```

### 핵심 포인트
- **Telegram = 폴링 방식**: Claude Code가 Telegram API를 주기적으로 확인, 로컬에 포트 노출 불필요
- **Claude Code = 로컬 실행**: 전체 코드베이스 접근 가능, claude.ai 로그인으로 인증
- **GitHub API = 양방향**: Claude Code가 `gh` CLI로 PR diff 조회 + 커밋 상태 설정

## 4. Acceptance Criteria

| # | 조건 | 검증 방법 |
|---|------|----------|
| AC1 | PR 생성/업데이트 시 Telegram으로 알림 전송 | GitHub Action 로그 확인 |
| AC2 | Claude Code가 Telegram 메시지 수신 후 PR diff 리뷰 | 터미널에서 채널 이벤트 수신 확인 |
| AC3 | 리뷰 결과가 PR 코멘트로 게시 | PR 코멘트 확인 |
| AC4 | 커밋 상태가 success/failure로 설정 | GitHub Status Checks 확인 |
| AC5 | critical 이슈 없으면 머지 가능 | PR merge 버튼 활성화 |
| AC6 | critical 이슈 있으면 머지 차단 | PR merge 버튼 비활성화 |
| AC7 | ANTHROPIC_API_KEY 없이 동작 | claude.ai 로그인만으로 리뷰 수행 |

## 5. 구현 단계

### Phase 1: cokacdir + Telegram 봇 설정 (준비)
- [ ] cokacdir 설치: `curl -fsSL https://cokacdir.cokac.com/install.sh | bash`
- [ ] BotFather에서 Telegram 봇 생성 → 토큰 획득
- [ ] `cokacdir --ccserver YOUR_BOT_TOKEN` 실행
- [ ] Telegram에서 봇에 첫 메시지 전송 (오너 등록/Imprinting)
- [ ] `/start ~/source/FanPulse` (작업 디렉토리 설정)
- [ ] `/silent` (도구 호출 메시지 숨김)
- [ ] `/instruction` (PR 리뷰 시스템 프롬프트 설정, 선택사항)
- [ ] `crontab -e` → `@reboot nohup cokacdir --ccserver TOKEN &` 추가

#### Quality Gate
```bash
# Telegram에서 봇에 "hello" → Claude Code 응답 확인
# /pwd → ~/source/FanPulse 경로 확인
# /session → 세션 ID 확인
```

### Phase 2: GitHub Action 워크플로우 교체
- [ ] 기존 `.github/workflows/ai-code-review.yml` 백업
- [ ] `pr-review-notify.yml` 작성 (PR 정보 → Telegram 전송, 즉시 종료)
- [ ] GitHub Secrets 추가: `TELEGRAM_BOT_TOKEN`, `TELEGRAM_CHAT_ID`

#### Quality Gate
```bash
# 테스트 PR 생성 → Telegram 메시지 수신 확인
# GitHub Action 로그에서 curl 성공 확인
```

### Phase 3: Claude Code 리뷰 로직
- [ ] `.claude/rules/02-pr-review-instructions.md` 작성 (리뷰 규칙)
- [ ] `gh pr diff` → 리뷰 → `gh pr comment` 파이프라인 테스트
- [ ] `gh api repos/{owner}/{repo}/statuses/{sha}` 커밋 상태 설정 테스트
- [ ] 리뷰 기준 정의 (critical/high/medium/low)

#### Quality Gate
```bash
# 테스트 PR에 대해 리뷰 수행 확인
# PR 코멘트 게시 확인
# 커밋 상태 설정 확인 (GitHub UI에서 체크)
```

### Phase 4: Repository Ruleset 업데이트
- [ ] 기존 "AI Review Protection" 룰셋의 status check 이름 확인/변경
- [ ] 새 커밋 상태 컨텍스트와 매칭 (`AI Code Review` 또는 새 이름)
- [ ] 머지 차단/허용 E2E 테스트

#### Quality Gate
```bash
# critical 이슈 있는 PR → 머지 차단 확인
# 이슈 없는 PR → 머지 허용 확인
```

### Phase 5: 정리 및 전환
- [ ] 기존 `script/ai_pr_reviewer.py` 제거 또는 아카이브
- [ ] 기존 `GEMINI_API_KEY` Secret 제거
- [ ] PR #195 리뷰 → 머지 테스트
- [ ] 문서 업데이트

## 6. 리스크 및 대응

| 리스크 | 영향도 | 대응 |
|--------|--------|------|
| Claude Code 세션이 꺼져있을 때 PR 생성 | 높음 | GitHub Action에 타임아웃 설정, 실패 시 수동 리뷰 안내 코멘트 게시 |
| Telegram 채널이 Research Preview | 중간 | `--dangerously-load-development-channels` 불필요 (공식 채널), 하지만 API 변경 가능성 있음 |
| Claude Code 권한 프롬프트에서 대기 | 중간 | `--dangerously-skip-permissions` 사용 (신뢰 환경에서만) 또는 allowedTools 사전 설정 |
| PR diff가 너무 클 때 | 낮음 | Claude Code가 자체적으로 파일 단위 리뷰 가능 (로컬 코드베이스 접근) |
| GitHub Action 리뷰 완료 대기 타임아웃 | 중간 | polling 주기 + 최대 대기 시간 설정 (예: 5분) |

## 7. GitHub Action ↔ Claude Code 통신 방식

### 방안 A: Telegram 양방향 (권장)
1. GitHub Action → Telegram 봇에 PR 정보 전송
2. Claude Code가 Telegram으로 수신 → 리뷰 수행
3. Claude Code가 `gh api`로 직접 커밋 상태 설정
4. GitHub Action은 커밋 상태를 polling하여 결과 확인

### 방안 B: Telegram 단방향 + GitHub Action 독립
1. GitHub Action → Telegram 봇에 PR 정보 전송 (알림 목적)
2. Claude Code가 수신 → 리뷰 → 커밋 상태 설정
3. GitHub Action은 자체적으로 종료 (커밋 상태는 별도 체크)
4. Repository Ruleset이 커밋 상태를 직접 확인

**방안 B가 더 단순**: GitHub Action은 알림만 보내고 종료.
커밋 상태 설정은 Claude Code가 담당. Ruleset이 상태를 체크.

## 8. 필요 Secrets / 환경

| 항목 | 용도 | 설정 위치 |
|------|------|----------|
| `TELEGRAM_BOT_TOKEN` | GitHub Action에서 Telegram 봇에 메시지 전송 | GitHub Secrets |
| `TELEGRAM_CHAT_ID` | 메시지 대상 채팅 ID | GitHub Secrets |
| Telegram 봇 토큰 | Claude Code Telegram 채널 인증 | `~/.claude/channels/telegram/.env` (로컬) |
| `gh` CLI 인증 | Claude Code에서 GitHub API 사용 | 로컬 `gh auth login` |

## 9. 의존성

- cokacdir (Rust 바이너리, curl로 설치)
- Claude Code CLI (`npm install -g @anthropic-ai/claude-code`)
- `gh` CLI 인증 완료 (로컬)
- Telegram 봇 토큰 (BotFather)

## 10. 예상 일정

| Phase | 소요 |
|-------|------|
| Phase 1: Telegram 봇 설정 | 30분 |
| Phase 2: GitHub Action 교체 | 1시간 |
| Phase 3: 리뷰 로직 구현 | 1시간 |
| Phase 4: Ruleset 업데이트 | 15분 |
| Phase 5: 정리 및 전환 | 30분 |
| **합계** | **~3시간** |
