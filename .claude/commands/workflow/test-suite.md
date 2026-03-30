---
description: 테스트 스위트 분석 및 실행
allowed-tools: Task, Read, Grep, Glob, Bash
argument-hint: [category: unit|integration|e2e|all]
---

# Test Suite Workflow

테스트 스위트를 분석하고 실행합니다.

## Test Category

분석 대상: $ARGUMENTS (default: all)

## Pre-Flight: Test Execution

먼저 테스트를 실행합니다:

```bash
./gradlew test --continue 2>&1 | tail -30
```

## Parallel Analysis

다음 에이전트를 **병렬로 실행**:

1. **test-orchestrator**: 테스트 결과 분석
   - 테스트 실행 결과 파싱
   - 실패 원인 분석
   - 커버리지 리포트

2. **playwright-test-planner** (e2e 또는 all인 경우):
   - E2E 테스트 계획 검토
   - 누락된 시나리오 식별

## Test Locations

| Category | Location | Pattern |
|----------|----------|---------|
| Unit | `src/test/**/unit/**` | `*Test.kt` |
| Integration | `src/test/**/integration/**` | `*IntegrationTest.kt` |
| E2E | `tests/**` | `*.spec.ts` |

## Expected Output

```
## Test Suite Report

### Execution Summary
- Total: {total} tests
- Passed: {passed} ({pass_rate}%)
- Failed: {failed}
- Skipped: {skipped}
- Execution Time: {time}s

### By Category
| Category | Passed | Failed |
|----------|--------|--------|
| Unit | {unit_passed} | {unit_failed} |
| Integration | {int_passed} | {int_failed} |
| E2E | {e2e_passed} | {e2e_failed} |

### Failures
1. {test_name} - {failure_reason}
2. {test_name} - {failure_reason}

### Coverage
- Line: {line_coverage}%
- Branch: {branch_coverage}%

### Recommendations
1. {recommendation_1}
2. {recommendation_2}
```
