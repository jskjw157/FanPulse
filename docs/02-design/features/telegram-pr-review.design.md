# Design: Claude Code Telegram PR Review (cokacdir)

> Plan: docs/01-plan/features/telegram-pr-review.plan.md
> Created: 2026-03-22
> Updated: 2026-03-22 (Channels → cokacdir 전환)
> 통신 방식: 방안 B (단방향 알림 + Claude Code가 커밋 상태 직접 설정)
> 봇 런타임: cokacdir --ccserver (Rust 바이너리, Channels 의존 없음)

## 1. 파일 구조

```
FanPulse/
├── .github/
│   └── workflows/
│       ├── ai-code-review.yml          # 삭제 (기존 Gemini 워크플로우)
│       └── pr-review-notify.yml        # 신규 (Telegram 알림 전용)
├── script/
│   └── ai_pr_reviewer.py              # 삭제 (72KB Gemini 스크립트)
└── .claude/
    └── rules/
        └── 02-pr-review-instructions.md  # 신규 (Claude Code 리뷰 프롬프트)
```

## 2. 시퀀스 다이어그램

```
Developer        GitHub          GitHub Action     Telegram       cokacdir (로컬)
   │               │                 │                │                │
   │─ PR 생성 ────→│                 │                │           [--ccserver 상시 실행]
   │               │─ PR 이벤트 ────→│                │           [세션: /start ~/FanPulse]
   │               │                 │── curl ───────→│                │
   │               │                 │← 200 OK ──────│                │
   │               │                 │ (즉시 종료)     │                │
   │               │                 │                │── 폴링 수신 ──→│
   │               │                 │                │                │
   │               │                 │                │  Claude Code CLI 실행
   │               │                 │                │  (--resume 세션 복원)
   │               │                 │                │   gh pr diff   │
   │               │                 │                │   코드 리뷰     │
   │               │                 │                │   gh pr comment │
   │               │                 │                │                │
   │               │←── gh api (commit status) ───────────────────────│
   │               │    context: "claude-code-review"                  │
   │               │    state: success/failure                         │
   │               │                 │                │                │
   │               │── Ruleset 체크 ─│                │                │
   │               │   (claude-code-review 상태 확인)  │                │
   │←─ 머지 허용/차단│                │                │                │
```

## 3. GitHub Action 워크플로우 (`pr-review-notify.yml`)

```yaml
name: PR Review Notify

on:
  pull_request:
    types: [opened, synchronize, reopened]

concurrency:
  group: pr-notify-${{ github.event.pull_request.number }}
  cancel-in-progress: true

permissions:
  contents: read
  pull-requests: read

jobs:
  notify:
    name: Notify Telegram
    runs-on: ubuntu-latest
    if: github.event.pull_request.draft == false
    steps:
      - name: Check for reviewable files
        id: check
        env:
          GH_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: |
          FILES=$(gh pr view ${{ github.event.pull_request.number }} \
            --repo ${{ github.repository }} \
            --json files --jq '.files[].path')
          if echo "$FILES" | grep -qE '\.kt$|\.java$|\.py$|\.ts$|\.tsx$|\.js$|\.jsx$'; then
            echo "should_notify=true" >> $GITHUB_OUTPUT
          else
            echo "should_notify=false" >> $GITHUB_OUTPUT
            echo "No code files changed, skipping notification"
          fi

      - name: Send Telegram notification
        if: steps.check.outputs.should_notify == 'true'
        env:
          TELEGRAM_BOT_TOKEN: ${{ secrets.TELEGRAM_BOT_TOKEN }}
          TELEGRAM_CHAT_ID: ${{ secrets.TELEGRAM_CHAT_ID }}
        run: |
          PR_NUM="${{ github.event.pull_request.number }}"
          PR_TITLE="${{ github.event.pull_request.title }}"
          PR_AUTHOR="${{ github.event.pull_request.user.login }}"
          PR_URL="${{ github.event.pull_request.html_url }}"
          PR_HEAD="${{ github.event.pull_request.head.ref }}"
          PR_BASE="${{ github.event.pull_request.base.ref }}"
          PR_SHA="${{ github.event.pull_request.head.sha }}"
          EVENT_TYPE="${{ github.event.action }}"

          # parse_mode 없이 plain text 전송 (PR 제목에 <, >, & 등이 있으면 HTML 파싱 실패)
          MESSAGE="[PR-REVIEW-REQUEST]
          repo: ${{ github.repository }}
          pr: #${PR_NUM}
          title: ${PR_TITLE}
          author: ${PR_AUTHOR}
          head: ${PR_HEAD}
          base: ${PR_BASE}
          sha: ${PR_SHA}
          event: ${EVENT_TYPE}
          url: ${PR_URL}"

          curl -s -X POST \
            "https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendMessage" \
            -d "chat_id=${TELEGRAM_CHAT_ID}" \
            --data-urlencode "text=${MESSAGE}"
```

### 설계 포인트

- **permissions 최소화**: `contents: read`, `pull-requests: read`만 필요 (write 불필요)
- **즉시 종료**: Telegram 전송 후 바로 종료, GitHub Action 러너 점유 시간 최소
- **구조화된 메시지**: `[PR-REVIEW-REQUEST]` 접두사로 Claude Code가 파싱 가능
- **sha 포함**: Claude Code가 커밋 상태를 설정할 때 필요한 SHA 값
- **이 job은 머지 차단에 사용하지 않음**: Ruleset은 커밋 상태를 별도로 체크

## 4. Telegram 메시지 포맷

### GitHub Action → Telegram (수신 메시지)

```
[PR-REVIEW-REQUEST]
repo: ohchaeeun/FanPulse
pr: #195
title: feat: 백엔드 AI 게시글/댓글 자동 필터링 기능
author: ohchaeeun
head: Feature/88-백엔드-ai-게시글댓글-자동-필터링-기능
base: master
sha: abc1234567890
event: synchronize
url: https://github.com/ohchaeeun/FanPulse/pull/195
```

### cokacdir → Claude Code에 전달되는 형태

cokacdir가 Telegram 메시지를 수신하면 Claude Code CLI에 stdin으로 메시지 내용을 전달.
Claude Code는 `.claude/rules/02-pr-review-instructions.md`의 규칙에 따라 `[PR-REVIEW-REQUEST]`를 인식하고 리뷰를 수행한다.

```
[PR-REVIEW-REQUEST]
repo: ohchaeeun/FanPulse
pr: #195
title: feat: 백엔드 AI 게시글/댓글 자동 필터링 기능
author: ohchaeeun
head: Feature/88-백엔드-ai-게시글댓글-자동-필터링-기능
base: master
sha: abc1234567890
event: synchronize
url: https://github.com/ohchaeeun/FanPulse/pull/195
```

## 5. Claude Code Instructions (`.claude/rules/02-pr-review-instructions.md`)

```markdown
# PR Review via Telegram Channel

## 트리거

Telegram 채널에서 `[PR-REVIEW-REQUEST]` 접두사가 포함된 메시지를 수신하면
자동으로 PR 코드 리뷰를 수행한다.

## 리뷰 절차

1. 메시지에서 PR 번호와 SHA 추출
2. **중복 체크**: 동일 PR의 이전 리뷰 요청이 Telegram 큐에 있으면 마지막 SHA만 리뷰.
   판별 방법: 메시지 내 `pr: #번호`가 동일한 요청 중 가장 최근 것만 처리.
   이전 요청은 Telegram 회신 "skipped (newer SHA exists)" 후 스킵.
3. `gh pr diff {PR번호}` 실행하여 diff 조회
4. diff 내용만 리뷰 (diff에 없는 파일은 절대 리뷰하지 않음)
5. **기존 리뷰 코멘트 삭제 후 재게시**:
   ```bash
   # 기존 "Claude Code Review" 코멘트 ID 조회
   COMMENT_ID=$(gh api repos/{owner}/{repo}/issues/{PR번호}/comments \
     --jq '.[] | select(.body | startswith("## Claude Code Review")) | .id' | head -1)
   # 있으면 삭제
   [ -n "$COMMENT_ID" ] && gh api -X DELETE repos/{owner}/{repo}/issues/comments/$COMMENT_ID
   # 새 코멘트 게시
   gh pr comment {PR번호} --body "{리뷰 내용}"
   ```
6. 커밋 상태를 설정하여 머지 차단/허용 결정

## 리뷰 기준

### 리뷰 대상
- diff에 포함된 코드 변경사항만 리뷰
- .kt, .java, .py, .ts, .tsx, .js, .jsx 파일만 리뷰

### 리뷰하지 않는 것
- diff에 없는 파일
- 설정 파일 (.yml, .json, .xml) 변경
- 스타일/포맷팅 이슈 (버그를 유발하지 않는 한)
- 주석 누락

### Severity 기준
- **critical**: 프로덕션 크래시, 데이터 손실, 보안 취약점 (머지 차단)
- **high**: 확실한 버그, 보안 우려 (머지 차단하지 않음, 경고)
- **medium**: 잠재적 이슈, 개선 권장
- **low**: 사소한 개선 제안

### 머지 차단 기준
- critical 이슈가 1개 이상 → failure (머지 차단)
- critical 없음 → success (머지 허용)

## 커밋 상태 설정

```bash
# 성공 (critical 이슈 없음)
gh api repos/{owner}/{repo}/statuses/{sha} \
  -f state=success \
  -f context=claude-code-review \
  -f description="No critical issues found" \
  -f target_url={PR_URL}

# 실패 (critical 이슈 있음)
gh api repos/{owner}/{repo}/statuses/{sha} \
  -f state=failure \
  -f context=claude-code-review \
  -f description="Found {N} critical issue(s)" \
  -f target_url={PR_URL}
```

## PR 코멘트 포맷

```markdown
## Claude Code Review

**Summary:** {1-2문장 요약}

| Severity | Count |
|----------|-------|
| Critical | {N} |
| High | {N} |
| Medium | {N} |

### Issues

#### [Critical] {제목}
**File:** `{파일경로}` (line {줄번호})
{설명 및 수정 제안}

---
*Reviewed by Claude Code via Telegram Channel*
```

## Telegram 회신

리뷰 완료 후 Telegram으로 간단한 결과를 회신한다:

```
PR #{번호} 리뷰 완료
결과: ✅ success / ❌ failure
이슈: critical {N}, high {N}, medium {N}
```
```

## 6. GitHub Commit Status API

### 상태 설정 (Claude Code 실행)

```bash
# POST /repos/{owner}/{repo}/statuses/{sha}
gh api repos/ohchaeeun/FanPulse/statuses/{SHA} \
  -f state=success \
  -f context=claude-code-review \
  -f description="No critical issues found" \
  -f target_url="https://github.com/ohchaeeun/FanPulse/pull/{PR_NUM}"
```

### 상태값

| state | 의미 | 머지 |
|-------|------|------|
| `success` | critical 이슈 없음 | 허용 |
| `failure` | critical 이슈 존재 | 차단 |
| `pending` | 리뷰 진행 중 | 대기 |
| `error` | 리뷰 실행 실패 | 차단 |

### 중요: `context` 값

`context=claude-code-review` 를 고정값으로 사용.
Repository Ruleset에서 이 값을 Required Status Check로 등록한다.

## 7. Repository Ruleset 변경

### 기존

```
Ruleset: "AI Review Protection" (id: 12174915)
Required Status Check: "AI Code Review" (GitHub Actions job name)
Enforcement: active
```

### 변경

```
Ruleset: "AI Review Protection" (id: 12174915)
Required Status Check: "claude-code-review" (commit status context)
Enforcement: active
```

### 변경 방법

```bash
# GitHub UI: Settings → Rules → Rulesets → "AI Review Protection" 편집
# Required status checks → "AI Code Review" 제거 → "claude-code-review" 추가

# ⚠️ API PUT은 전체 ruleset 페이로드를 덮어쓰므로 GitHub UI 방식을 권장
# API를 사용할 경우 먼저 GET으로 현재 ruleset을 조회 후 수정하여 PUT해야 함
```

## 8. cokacdir 봇 서버 설정

### 8-1. 설치

```bash
/bin/bash -c "$(curl -fsSL https://cokacdir.cokac.com/install.sh)"
```

사전 조건:
- Claude Code CLI 설치: `npm install -g @anthropic-ai/claude-code`
- gh CLI 인증: `gh auth login`

### 8-2. BotFather 봇 생성

1. Telegram에서 @BotFather → `/newbot`
2. 봇 이름 + username 설정 (예: `fanpulse_review_bot`)
3. API 토큰 복사 (예: `1234567890:ABCdef...`)

### 8-3. 봇 서버 시작

```bash
cokacdir --ccserver YOUR_BOT_TOKEN
```

### 8-4. 오너 등록 (Imprinting)

봇 최초 실행 후 Telegram에서 봇에게 **첫 메시지를 보낸 사용자**가 자동으로 오너로 등록.
⚠️ 외부인이 먼저 메시지를 보내지 않도록 즉시 등록할 것.

### 8-5. 세션 설정 (Telegram에서 실행)

```
/start ~/source/FanPulse          # 작업 디렉토리 설정 (세션 자동 복원)
/instruction [PR 리뷰 규칙]       # 시스템 프롬프트 추가 (선택사항, .claude/rules/와 조합)
/silent                            # 도구 호출 중간 메시지 숨김
```

### 8-6. 세션 관리 방식

cokacdir는 Claude Code 세션을 **영구 보존**한다:
- `bot_settings.json`에 마지막 세션 경로 저장
- `/start` 없이 메시지 보내면 마지막 세션 자동 복원
- Claude Code `--resume SESSION_ID`로 기존 세션 이어서 실행
- 세션 히스토리 (대화 내역)도 복원됨

### 8-7. 자동 시작 (재부팅 시)

```bash
# crontab에 추가
crontab -e
@reboot nohup cokacdir --ccserver YOUR_BOT_TOKEN &
```

### 8-8. 권한 관련

cokacdir는 Claude Code를 `--dangerously-skip-permissions`로 실행한다.
별도의 allowedTools 설정이 필요 없음 — 모든 도구가 자동 허용.

⚠️ 오너 등록이 완료된 후에는 해당 Telegram 사용자 ID만 봇 사용 가능.

### 8-9. 주요 Telegram 명령어

| 명령어 | 설명 |
|--------|------|
| `/start [path]` | 세션 시작/복원 |
| `/stop` | 현재 AI 작업 중단 |
| `/clear` | 대화 기록 초기화 |
| `/pwd` | 현재 작업 디렉토리 확인 |
| `/instruction [text]` | 시스템 프롬프트 설정/확인 |
| `/instruction_clear` | 시스템 프롬프트 삭제 |
| `/silent` | 도구 호출 메시지 숨김 토글 |
| `/session` | 현재 세션 ID 확인 |
| `/model [provider:model]` | AI 모델 변경 (예: `claude:claude-sonnet-4-6`) |
| `/shell [command]` | 셸 명령 직접 실행 |
| `/download [path]` | 파일 다운로드 |

### 8-10. 의존성 비교 (기존 Channels vs cokacdir)

| 항목 | Channels (이전 설계) | cokacdir (현재) |
|------|---------------------|-----------------|
| 런타임 | Bun (JS) | 없음 (Rust 바이너리) |
| Claude Code Channels | 필요 (연구 프리뷰) | **불필요** |
| 플러그인 설치 | `/plugin install` + 페어링 | **불필요** |
| 권한 설정 | allowedTools 수동 설정 | **자동** (--dangerously-skip-permissions) |
| 세션 복원 | 기존 세션에 이벤트 push | **--resume로 세션 복원** |
| 자동 시작 | 수동 `--channels` 플래그 | **crontab @reboot** |

## 9. Fallback: cokacdir 오프라인 시

cokacdir 봇 서버가 꺼져있으면 Telegram 메시지가 수신되지 않고, 커밋 상태도 설정되지 않는다.
이 경우 PR에 `claude-code-review` 상태가 없어서 Ruleset에 의해 머지가 차단된다.

### 대응 방안

| 상황 | 대응 |
|------|------|
| cokacdir 임시 오프라인 | 재시작하면 밀린 Telegram 메시지 자동 처리 |
| 장기 오프라인 | admin 권한으로 `--admin` 머지 |
| 긴급 핫픽스 | Ruleset 일시 비활성화 또는 admin 머지 |
| 재부팅 | `@reboot` crontab으로 자동 재시작 |

### Telegram 메시지 큐잉

Telegram은 봇이 오프라인이어도 메시지를 보관한다 (24시간 이내).
cokacdir가 재시작되면 밀린 메시지를 순차적으로 처리한다.

### 네트워크 재연결

cokacdir는 네트워크 단절 시 exponential backoff으로 자동 재연결 (5초 → 최대 60초).
세션은 `bot_settings.json`에 영구 저장되므로 재시작 후에도 세션이 복원된다.

단, **동일 PR에 여러 이벤트(synchronize)가 쌓인 경우** 마지막 SHA만 리뷰하면 된다.
Claude Code instructions에서 동일 PR의 이전 리뷰 요청은 스킵하도록 명시.

## 10. 삭제 대상

| 파일 | 사유 |
|------|------|
| `.github/workflows/ai-code-review.yml` | Gemini 워크플로우 → `pr-review-notify.yml`로 교체 |
| `script/ai_pr_reviewer.py` | 72KB Gemini 리뷰 스크립트, 더 이상 필요 없음 |
| GitHub Secret: `GEMINI_API_KEY` | Gemini API 키 |
| GitHub Secret: `GLM_API_KEY` | GLM API 키 (사용 안 됨) |

## 11. GitHub Secrets 변경

### 추가

| Secret | 값 | 용도 |
|--------|---|------|
| `TELEGRAM_BOT_TOKEN` | BotFather에서 발급 | GitHub Action → Telegram 메시지 전송 |
| `TELEGRAM_CHAT_ID` | 봇과의 1:1 채팅 ID (숫자, 예: `123456789`) | 메시지 대상 지정. BotFather 봇에 `/start` 후 `https://api.telegram.org/bot{TOKEN}/getUpdates`로 확인 |

### 삭제

| Secret | 사유 |
|--------|------|
| `GEMINI_API_KEY` | Gemini 워크플로우 제거됨 |

## 12. 동일 PR 중복 리뷰 방지

PR에 여러 커밋이 push되면 `synchronize` 이벤트가 반복 발생한다.

### GitHub Action 측

```yaml
concurrency:
  group: pr-notify-${{ github.event.pull_request.number }}
  cancel-in-progress: true
```

이전 실행 중인 알림을 취소하고 최신만 전송.

### Claude Code 측

Instructions에 명시:
- Telegram 메시지 큐에 동일 PR 번호의 요청이 여러 개 있으면 **마지막 SHA만 리뷰**
- 이전 요청은 "skipped (newer request exists)" 로 Telegram 회신만 하고 스킵
