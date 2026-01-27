# AI 코드 리뷰 봇 설정 가이드

FanPulse 프로젝트를 위한 AI 코드 리뷰 봇 (GLM + Gemini 하이브리드) 설정 가이드입니다.

## 📋 개요

이 봇은 **GLM-4-Flash**와 **Gemini 2.5 Flash**를 동시에 사용하여 PR을 리뷰합니다.

### 왜 두 개의 AI를 사용하나요?

| AI | 강점 | 특징 |
|----|------|------|
| **GLM-4-Flash** | 빠른 응답, 코드 이해력, 무료 | Zhipu AI (중국) |
| **Gemini 2.5 Flash** | 안정성, 빠른 응답, 넓은 컨텍스트 | Google |

두 AI가 **동시에 지적한 이슈**는 높은 신뢰도를 가집니다.

---

## 🔑 1단계: API 키 발급

### GLM API 키 (Zhipu AI)

1. [https://open.bigmodel.cn/](https://open.bigmodel.cn/) 접속
2. 회원가입 (Zhipu AI 계정 필요)
3. API Key 발급
4. **무료 한도**: 신규 가입 시 무료 크레딧 제공

### Gemini API 키 (Google)

1. [https://aistudio.google.com/apikey](https://aistudio.google.com/apikey) 접속
2. Google 계정으로 로그인
3. "Create API Key" 클릭
4. **무료**: 완전 무료 (Rate limit만 존재)

---

## 🔧 2단계: GitHub Secrets 설정

Repository Settings → Secrets and variables → Actions에서 추가:

| Secret 이름 | 값 | 필수 여부 |
|------------|-----|----------|
| `GLM_API_KEY` | Zhipu AI GLM API 키 | 둘 중 하나 필수 |
| `GEMINI_API_KEY` | Google Gemini API 키 | 둘 중 하나 필수 |

> **Note**: 두 키 모두 설정하면 하이브리드 모드로 동작합니다.
> 하나만 설정해도 해당 AI만으로 리뷰가 가능합니다.

---

## 📁 3단계: 파일 확인

다음 파일들이 프로젝트에 있는지 확인:

```
.github/
└── workflows/
    └── ai-code-review.yml    # GitHub Actions 워크플로우

script/
├── ai_pr_reviewer.py         # 메인 리뷰어 스크립트
└── pr_analyzer.py            # 정적 분석 (기존)

.claude/
└── agents/
    └── ai-pr-reviewer.md     # Claude 에이전트 정의
```

---

## 🚀 4단계: 테스트

### 로컬 테스트

```bash
# 환경 변수 설정 (Linux/macOS)
export GLM_API_KEY="your-glm-api-key"
export GEMINI_API_KEY="your-gemini-api-key"

# PowerShell (Windows)
$env:GLM_API_KEY = "your-glm-api-key"
$env:GEMINI_API_KEY = "your-gemini-api-key"

# 로컬 diff로 테스트
git diff main | python script/ai_pr_reviewer.py

# PR 번호로 테스트 (gh CLI 필요)
python script/ai_pr_reviewer.py --pr 123

# 결과를 파일로 저장
python script/ai_pr_reviewer.py --pr 123 \
  --output review.md \
  --json review.json

# Gemini만 사용
python script/ai_pr_reviewer.py --pr 123 --gemini-only

# GLM만 사용
python script/ai_pr_reviewer.py --pr 123 --glm-only
```

### GitHub Actions 테스트

1. 새로운 PR 생성
2. Actions 탭에서 "AI Code Review" 워크플로우 확인
3. PR에 코멘트로 리뷰 결과가 게시되는지 확인

---

## ⚙️ 고급 설정

### 특정 브랜치만 리뷰

`.github/workflows/ai-code-review.yml` 수정:

```yaml
on:
  pull_request:
    types: [opened, synchronize, reopened]
    branches:
      - main
      - develop
```

### Draft PR 스킵

이미 설정되어 있습니다:
```yaml
if: github.event.pull_request.draft == false
```

### Critical 이슈 시 빌드 실패

워크플로우에서 주석 해제:
```yaml
if [ "$CRITICAL" -gt 0 ]; then
  echo "::error::Found $CRITICAL critical issue(s)"
  exit 1  # 이 줄 주석 해제
fi
```

### Tech Stack 커스터마이징

`script/ai_pr_reviewer.py`의 시스템 프롬프트 수정:

```python
Tech Stack:
- Backend: Kotlin + Spring Boot 3.2 + PostgreSQL + MongoDB + Redis
- Frontend: Next.js + TypeScript + TailwindCSS
- Mobile: Android (Jetpack Compose), iOS (UIKit/SwiftUI)
```

---

## 📊 리뷰 결과 해석

### Severity 레벨

| 레벨 | 아이콘 | 의미 | 액션 |
|------|-------|------|------|
| Critical | 🔴 | 보안, 크래시, 데이터 손실 | **반드시 수정** |
| High | 🟠 | 성능, 아키텍처 문제 | 수정 권장 |
| Medium | 🟡 | 스타일, 베스트 프랙티스 | 검토 필요 |
| Low | 🟢 | 제안, 개선사항 | 선택적 |

### Consensus Issues ⚠️

**두 AI가 모두 지적한 이슈**는 특별히 표시됩니다:
- 높은 신뢰도
- 우선적으로 검토 필요
- `🔥 Consensus` 태그로 표시

---

## 🔍 문제 해결

### "No AI providers configured" 오류

```bash
# 환경 변수 확인 (Linux/macOS)
echo $GLM_API_KEY
echo $GEMINI_API_KEY

# PowerShell (Windows)
echo $env:GLM_API_KEY
echo $env:GEMINI_API_KEY

# GitHub Secrets 확인
# Repository → Settings → Secrets → Actions
```

### Rate Limit 오류

| Provider | 무료 한도 | 해결책 |
|----------|----------|--------|
| GLM | 가입 시 무료 크레딧 | 대기 후 재시도 |
| Gemini | 15 req/min | 대기 후 재시도 |

### 큰 PR 처리 실패

Diff가 너무 크면 자동으로 잘립니다:
- GLM: 30,000자
- Gemini: 50,000자

해결책:
1. PR을 작게 나누기
2. `pr_analyzer.py`로 고위험 파일만 선별하여 리뷰

---

## 💰 비용

**완전 무료!**

| Provider | 무료 한도 | 월 비용 |
|----------|----------|--------|
| GLM-4-Flash | 가입 시 무료 크레딧 | **$0** |
| Gemini | 무제한 (rate limit만) | **$0** |

---

## 📚 관련 문서

- [GLM-4 API 문서](https://open.bigmodel.cn/dev/api)
- [Gemini API 문서](https://ai.google.dev/gemini-api/docs)
- [GitHub Actions 문서](https://docs.github.com/actions)
- [FanPulse PR 가이드](./team_git_commit_convention_conventional_commits.md)

---

## 🤝 기여

버그 리포트나 개선 제안은 GitHub Issues로 등록해주세요.

---

**Created**: 2026-01-27
**Updated**: 2026-01-27
**Maintainer**: FanPulse Team
