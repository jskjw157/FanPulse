---
description: GitHub 이슈 기반 기능 구현 파이프라인 실행 (DDD → Plan → TDD → Test)
allowed-tools: Bash(git branch:*), Bash(gh issue:*), Task, Read, Grep, Glob, TodoWrite, AskUserQuestion
argument-hint: "[issue-number]"
model: opus
---

# GitHub 이슈 기반 기능 구현

## 현재 상태

- 브랜치: !`git branch --show-current`
- 리포지토리: !`gh repo view --json nameWithOwner -q .nameWithOwner 2>/dev/null || echo "인증 필요"`

## 작업 지시

당신은 `issue-feature-builder` 에이전트입니다. GitHub 이슈를 기반으로 기능 구현 파이프라인을 실행하세요.

### Step 1: 이슈 번호 확인

1. 인자로 이슈 번호가 주어진 경우: `$ARGUMENTS` 사용
2. 인자가 없는 경우: 브랜치명에서 추출
   - `feature/{number}-*` → 이슈 #{number}
   - `fix/{number}-*` → 이슈 #{number}
   - `issue-{number}-*` → 이슈 #{number}

### Step 2: 이슈 정보 조회

```bash
gh issue view {issue-number} --json number,title,body,labels,milestone,assignees
```

이슈 정보를 사용자에게 보여주세요:
- 제목, 설명
- 라벨, 마일스톤
- Acceptance Criteria (있는 경우)

### Step 3: 워크플로우 선택

AskUserQuestion 도구로 사용자에게 물어보세요:

**옵션:**
1. **Full Pipeline** (권장) - DDD 분석 → 기능 계획 → TDD 구현 → 테스트 자동화
2. **Quick Implementation** - 기능 계획 → TDD 구현
3. **Domain Focus** - DDD 분석 → 기능 계획
4. **Test Focus** - 테스트 자동화만

### Step 4: 파이프라인 실행

선택된 워크플로우에 따라 Task 도구로 에이전트를 순차 실행:

| Phase | Agent | 설명 |
|-------|-------|------|
| DDD | `ddd-planning` | 도메인 분석, Bounded Context, Aggregate 설계 |
| Plan | `feature-planner` (skill) | TDD 기반 Phase별 계획 수립 |
| TDD | `backend-development:tdd-orchestrator` | Red-Green-Refactor 구현 |
| Test | `full-stack-orchestration:test-automator` | 테스트 자동화, 커버리지 검증 |

### Step 5: 품질 게이트 검증

각 Phase 완료 후 검증:
- [ ] 빌드 성공
- [ ] 테스트 통과
- [ ] 린팅 통과
- [ ] 커버리지 80%+

### Step 6: 완료 요약

구현 결과 요약:
- 생성/수정된 파일
- 테스트 커버리지
- 다음 단계 (PR 생성 등)

## 참고 문서

- 에이전트 정의: @.claude/agents/issue-feature-builder.md
- 스킬 정의: @.claude/skills/issue-feature/SKILL.md
- DDD 문서: `doc/ddd/`
- 계획서 위치: `docs/plans/`

## 진행 상황 추적

TodoWrite 도구로 진행 상황을 추적하세요:
```
[ ] 이슈 확인 및 분석
[ ] 워크플로우 선택
[ ] DDD 분석 (선택 시)
[ ] 기능 계획 (선택 시)
[ ] TDD 구현 (선택 시)
[ ] 테스트 자동화 (선택 시)
[ ] 품질 게이트 검증
[ ] 완료 요약
```

$ARGUMENTS
