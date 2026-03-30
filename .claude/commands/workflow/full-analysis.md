---
description: 전체 프로젝트 종합 분석 (7개 에이전트 병렬)
allowed-tools: Task, Read, Grep, Glob, Bash
---

# Full Analysis Workflow

프로젝트 전체를 7개 에이전트로 종합 분석합니다.

## Pre-Flight Scripts

가능하면 스크립트를 먼저 실행하여 토큰을 절감합니다:

```bash
# 정적 분석 (있는 경우)
python script/code_review_analyzer.py --output .claude/review-report.json 2>&1 || echo "Script not available"

# Config 검증 (있는 경우)
python script/config_validator.py --target .claude/ --output .claude/config-report.json 2>&1 || echo "Script not available"
```

## Parallel Agents (7개 동시 실행)

다음 에이전트를 **모두 병렬로 실행**:

| Agent | Focus |
|-------|-------|
| **code-reviewer** | 코드 품질 분석 |
| **backend-architect** | 아키텍처 리뷰 |
| **kotlin-analyzer** | Kotlin 정적 분석 |
| **config-reviewer** | Claude 설정 검증 |
| **doc-writer** | 문서 완성도 확인 |
| **progress-analyzer** | 진행률 추적 |
| **test-orchestrator** | 테스트 상태 확인 |

## Result Aggregation

모든 에이전트 결과를 수집하여 우선순위별로 분류:

- **Critical**: 즉시 수정 필요
- **High**: 빠른 시일 내 수정 권장
- **Medium**: 개선 권장
- **Low**: 참고 사항

## Expected Output

```
## Full Analysis Report

### Executive Summary
- Overall Health: [GREEN/YELLOW/RED]
- Code Quality: {score}/100
- Test Coverage: {coverage}%
- Documentation: {doc_score}%
- Architecture: {arch_status}

### Critical Issues (즉시 수정)
{critical_issues or "None found"}

### High Priority ({count}건)
1. [{source}] {issue_description}
2. [{source}] {issue_description}

### Medium Priority ({count}건)
1. [{source}] {issue_description}

### Low Priority ({count}건)
1. [{source}] {issue_description}

### Progress Status
- Current Week: W{N}
- Progress: {percentage}%
- Blockers: {blocker_count}개

### Action Items (우선순위 순)
1. [CRITICAL] {action}
2. [HIGH] {action}
3. [MEDIUM] {action}

### Token Usage
- Pre-flight scripts: Saved ~{tokens} tokens
- Total agents: 7
- Aggregation overhead: ~1,000 tokens
```
