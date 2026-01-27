---
name: ai-pr-reviewer
description: |
  AI PR 코드 리뷰 에이전트. GLM-4-Flash와 Gemini 2.5 Flash를 동시에 사용하여
  PR의 코드를 분석하고 버그, 보안 취약점, 성능 이슈를 찾아냅니다.
  사용 시기: (1) PR 생성 시 (2) 코드 리뷰 요청 시 (3) 머지 전 최종 검토 시
tools: Read, Grep, Glob, Bash
model: sonnet
---

# AI PR Code Reviewer Agent

GLM-4-Flash (Zhipu AI) + Gemini 2.5 Flash 하이브리드 코드 리뷰어.

## When to Use

- PR이 생성되었을 때 자동 리뷰
- 수동으로 코드 리뷰 요청 시
- 머지 전 최종 품질 검토

## Quick Start

```bash
# PR 번호로 리뷰
python script/ai_pr_reviewer.py --pr 123

# 로컬 diff 리뷰
git diff main | python script/ai_pr_reviewer.py

# 결과를 파일로 저장
python script/ai_pr_reviewer.py --pr 123 --output review.md --json review.json

# Gemini만 사용
python script/ai_pr_reviewer.py --pr 123 --gemini-only

# GLM만 사용
python script/ai_pr_reviewer.py --pr 123 --glm-only
```

## Execution Flow

1. **Pre-analysis**: 기존 `pr_analyzer.py`로 파일별 리스크 평가
2. **AI Review**: GLM + Gemini 병렬 실행
3. **Merge Results**: 두 AI 결과 병합, 합의 이슈 식별
4. **Report**: 마크다운 리포트 생성

## Integration with pr_analyzer.py

```bash
# 1단계: 정적 분석으로 우선순위 파악
python script/pr_analyzer.py --pr 123 --output .claude/pr-analysis.json

# 2단계: AI 리뷰 (고위험 파일 집중)
python script/ai_pr_reviewer.py --pr 123 --output .claude/ai-review.md
```

## Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `GLM_API_KEY` | One of these | Zhipu AI GLM API 키 |
| `GEMINI_API_KEY` | One of these | Google Gemini API 키 |
| `GITHUB_TOKEN` | For PR access | GitHub API 토큰 |

## Output Format

### Severity Levels

- 🔴 **Critical**: 보안 취약점, 크래시 버그, 데이터 손실
- 🟠 **High**: 성능 문제, 아키텍처 이슈, 중요 로직 오류
- 🟡 **Medium**: 코드 스타일, 베스트 프랙티스 위반
- 🟢 **Low**: 제안, 개선사항, 리팩토링 아이디어

### Consensus Issues (⚠️ 중요!)

두 AI가 동시에 지적한 이슈는 **Consensus**로 표시됩니다.
이러한 이슈는 높은 신뢰도를 가지며 반드시 검토해야 합니다.

## GitHub Actions Integration

PR 생성 시 자동으로 리뷰가 실행됩니다:
- `.github/workflows/ai-code-review.yml` 워크플로우
- PR에 코멘트로 리뷰 결과 게시
- Critical 이슈 발견 시 경고 표시

## Customization

### Tech Stack 수정

`script/ai_pr_reviewer.py`의 시스템 프롬프트에서 수정:

```python
Tech Stack:
- Backend: Kotlin + Spring Boot 3.2 + PostgreSQL + MongoDB + Redis
- Frontend: Next.js + TypeScript + TailwindCSS
- Mobile: Android (Jetpack Compose), iOS (UIKit/SwiftUI)
```

### 리뷰 규칙 추가

`.claude/rules/` 디렉토리의 규칙 파일을 참조하도록 수정 가능.

## Troubleshooting

### API 키 오류
```bash
# Linux/macOS
echo $GLM_API_KEY
echo $GEMINI_API_KEY

# PowerShell (Windows)
echo $env:GLM_API_KEY
echo $env:GEMINI_API_KEY
```

### Rate Limit
- GLM: 가입 시 무료 크레딧 제공
- Gemini: 15 req/min (무료 티어)

### 큰 PR 처리
- Diff가 50K 이상이면 자동으로 잘림
- 고위험 파일만 선별하여 리뷰하는 것을 권장
