---
name: code-review
description: |
  코드 리뷰 스킬. Git 변경사항, PR, 특정 파일을 분석하여 품질/보안/성능 관점에서 리뷰를 제공합니다.
  사용 시기: (1) /review - 현재 staged/unstaged 변경사항 리뷰 (2) /review <file> - 특정 파일 리뷰
  (3) /review-pr <number> - GitHub PR 리뷰 (4) 코드 품질 검토 요청 시 (5) 보안 취약점 검토 요청 시 (project)
---

# Code Review Skill

## Quick Start

```bash
/review              # 현재 변경사항 리뷰
/review src/file.kt  # 특정 파일 리뷰
/review-pr 123       # PR #123 리뷰
```

## Workflow

### 1. 변경사항 수집

```bash
# staged + unstaged 변경사항
git diff HEAD

# PR인 경우
gh pr view <number> --json files,commits,body
gh pr diff <number>
```

### 2. 리뷰 수행

각 변경사항에 대해 다음 관점으로 분석:

**필수 체크**
- [ ] 버그 가능성 (null 처리, 경계값, 예외 처리)
- [ ] 보안 취약점 (injection, XSS, 인증/인가)
- [ ] 성능 이슈 (N+1 쿼리, 불필요한 연산, 메모리 누수)

**권장 체크**
- [ ] 코드 가독성 (네이밍, 복잡도)
- [ ] 테스트 커버리지
- [ ] 기존 패턴과의 일관성

### 3. 결과 출력 형식

```markdown
## Code Review Summary

### Critical Issues (Must Fix)
- 🔴 [보안] SQL Injection 위험: `UserRepository.kt:45`
  - 문제: 사용자 입력이 직접 쿼리에 삽입됨
  - 해결: Parameterized query 사용

### Warnings (Should Fix)
- 🟡 [성능] N+1 쿼리 패턴: `OrderService.kt:78`
  - 문제: 루프 내에서 개별 쿼리 실행
  - 해결: JOIN fetch 또는 batch 조회

### Suggestions (Nice to Have)
- 🟢 [가독성] 함수 분리 권장: `PaymentController.kt:120`
  - 이유: 함수가 50줄 이상으로 단일 책임 위반

### Positive Highlights
- ✨ 적절한 예외 처리: `AuthService.kt:35`
- ✨ 명확한 테스트 케이스 추가

### Statistics
- Files reviewed: 5
- Critical: 1 | Warnings: 2 | Suggestions: 3
```

## Review Guidelines

### Kotlin/Spring Boot 특화

- `@Transactional` 누락 여부
- Repository 메서드 네이밍 컨벤션
- DTO/Entity 분리 확인
- Exception 처리 패턴 (BusinessException 사용)

### 이 프로젝트 컨텍스트

- `ApiResponse<T>` wrapper 사용 여부
- First-Writer-Wins 패턴 준수
- Namespace Enforcement 규칙 적용

상세 체크리스트: [references/checklist.md](references/checklist.md)
