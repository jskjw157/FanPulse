---
description: 프로젝트 진행 상황을 병렬 에이전트로 분석
allowed-tools: Task, Read, Grep, Glob
argument-hint: [week-number]
---

# Progress Check Workflow

프로젝트 진행 상황을 여러 에이전트로 동시에 분석합니다.

## Current Context

- Target Week: $ARGUMENTS (default: latest from work-plan/)
- Git Branch: !`git branch --show-current`
- Recent Commits: !`git log --oneline -3`

## Parallel Analysis

다음 3개 에이전트를 **병렬로 실행**하세요:

1. **progress-analyzer**: 기획서 대비 진행률 분석
   - `document/기획서.md` vs 현재 구현 상태
   - `work-plan/w{N}_plan.md` 항목별 완료 여부

2. **code-reviewer**: 코드 품질 분석
   - 최근 변경 사항 리뷰
   - 잠재적 이슈 식별

3. **backend-architect**: 아키텍처 정합성 검토
   - API 설계 패턴 확인
   - 서비스 구조 검증

## Expected Output

모든 에이전트 결과를 집계하여 다음 형식으로 출력:

```
## Progress Check Report - W{N}

### Overall Status: [GREEN/YELLOW/RED]

### Milestone Progress
- 완료: {completed_count}개
- 진행중: {in_progress_count}개
- 미착수: {not_started_count}개

### Code Quality
- 발견된 이슈: {total_issues}개
- Critical: {critical_count}개

### Architecture
- 정합성: {alignment_status}
- 주의사항: {concerns_list}

### Action Items
1. [HIGH] {priority_1_action}
2. [MEDIUM] {priority_2_action}
```
