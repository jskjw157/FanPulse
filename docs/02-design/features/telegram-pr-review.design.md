# Design: Claude Code Telegram Channel PR Review

> Plan: docs/01-plan/features/telegram-pr-review.plan.md
> Created: 2026-03-22
> 통신 방식: 방안 B (단방향 알림 + Claude Code가 커밋 상태 직접 설정)

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
Developer        GitHub          GitHub Action     Telegram       Claude Code (로컬)
   │               │                 │                │                │
   │─ PR 생성 ────→│                 │                │                │
   │               │─ PR 이벤트 ────→│                │                │
   │               │                 │── gh api ─────→│                │
   │               │                 │  (pending 설정) │                │
   │               │                 │── curl ───────→│                │
   │               │                 │← 200 OK ──────│                │
   │               │                 │ (즉시 종료)     │                │
   │               │                 │                │── 폴링 수신 ──→│
   │               │                 │                │                │
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
          # gh pr view --json files는 존재하지 않는 필드이므로 REST API 사용
          FILES=$(gh api repos/${{ github.repository }}/pulls/${{ github.event.pull_request.number }}/files \
            --jq '.[].filename')
          if echo "$FILES" | grep -qE '\.kt$|\.java$|\.py$|\.ts$|\.tsx$|\.js$|\.jsx$'; then
            echo "should_notify=true" >> $GITHUB_OUTPUT
          else
            echo "should_notify=false" >> $GITHUB_OUTPUT
            echo "No code files changed, skipping notification"
          fi

      - name: Set initial pending status
        if: steps.check.outputs.should_notify == 'true'
        env:
          GH_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: |
          # Claude Code가 오프라인이어도 pending 상태가 설정되어 Ruleset이 머지를 대기시킴
          gh api repos/${{ github.repository }}/statuses/${{ github.event.pull_request.head.sha }} \
            -f state=pending \
            -f context=claude-code-review \
            -f description="Waiting for Claude Code review..." \
            -f target_url="${{ github.event.pull_request.html_url }}"

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

### Claude Code에서 수신되는 형태

```xml
<channel source="telegram" chat_id="123456" sender_id="789">
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
</channel>
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

## 8. Claude Code 실행 설정

### 시작 명령어

```bash
claude --channels plugin:telegram@claude-plugins-official
```

### 필요 사전 설정

| 항목 | 명령어 | 비고 |
|------|--------|------|
| Telegram 플러그인 설치 | `/plugin install telegram@claude-plugins-official` | Claude Code 내 실행 |
| 봇 토큰 설정 | `/telegram:configure <token>` | BotFather에서 받은 토큰 |
| 계정 페어링 | `/telegram:access pair <code>` | 봇에 메시지 후 받는 코드 |
| 접근 제한 | `/telegram:access policy allowlist` | 허용된 사용자만 |
| gh CLI 인증 | `gh auth login` | GitHub API 접근용 |
| Bun 설치 | `curl -fsSL https://bun.sh/install \| bash` | 채널 플러그인 런타임 |

### 권한 관련

Claude Code가 리뷰 수행 시 `gh` 명령을 사용하므로, 터미널을 떠나있을 때 권한 프롬프트에서
멈출 수 있다. 두 가지 해결 방법:

1. **allowedTools 사전 설정** (권장):
   `.claude/settings.json`에 다음 추가:
   ```json
   {
     "permissions": {
       "allow": [
         "Bash(gh pr diff:*)",
         "Bash(gh pr comment:*)",
         "Bash(gh pr view:*)",
         "Bash(gh api:*)",
         "Read",
         "Grep",
         "Glob"
       ]
     }
   }
   ```

2. **`--dangerously-skip-permissions`** (주의):
   모든 도구를 허가 없이 실행. 신뢰 환경에서만 사용.

## 9. Fallback: Claude Code 오프라인 시

Claude Code가 꺼져있으면 Telegram 메시지가 수신되지 않고, 커밋 상태도 설정되지 않는다.
이 경우 PR에 `claude-code-review` 상태가 없어서 Ruleset에 의해 머지가 차단된다.

### 대응 방안

| 상황 | 대응 |
|------|------|
| Claude Code 임시 오프라인 | Claude Code 켜면 밀린 Telegram 메시지 처리 |
| 장기 오프라인 | admin 권한으로 `--admin` 머지 |
| 긴급 핫픽스 | Ruleset 일시 비활성화 또는 admin 머지 |

### Telegram 메시지 큐잉

Telegram은 봇이 오프라인이어도 메시지를 보관한다 (24시간 이내).
Claude Code가 다시 켜지면 밀린 메시지를 순차적으로 처리한다.

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
