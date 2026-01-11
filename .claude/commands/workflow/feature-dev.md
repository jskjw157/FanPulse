---
description: 기능 개발 워크플로우 (설계 → 리뷰 → 문서화)
allowed-tools: Task, Read, Write, Edit, Grep, Glob, Bash
argument-hint: [feature-description]
---

# Feature Development Workflow

기능 개발을 위한 단계별 워크플로우입니다.

## Feature Requirements

$ARGUMENTS

## Phase 1: Design (Sequential)

먼저 **backend-architect** 에이전트로 설계를 진행합니다:

- API 엔드포인트 정의
- 데이터베이스 스키마 변경사항
- 서비스 레이어 구조

**설계 승인 후 다음 단계로 진행합니다.**

## Phase 2: Implementation

사용자가 직접 구현합니다. 필요시 도움을 요청하세요.

## Phase 3: Review (Parallel)

구현 완료 후 다음 3개 에이전트를 **병렬로 실행**:

1. **code-reviewer**: 코드 품질 리뷰
   - 변경된 파일 분석
   - 보안 취약점 확인
   - 베스트 프랙티스 준수 여부

2. **doc-writer**: 문서 생성/업데이트
   - KDoc/JSDoc 주석
   - README 업데이트
   - API 문서화

3. **backend-architect**: 아키텍처 검증
   - 설계 의도 준수 확인
   - 확장성 고려사항

## Expected Output

```
## Feature Development Report

### Design Phase
- API Endpoints: {count}개 정의
- DB Changes: {schema_changes}

### Implementation Review
- Code Quality: {score}/100
- Security Issues: {count}개
- Documentation: {status}

### Approval Status
- [ ] Code Review Passed
- [ ] Documentation Complete
- [ ] Architecture Aligned

### Next Steps
1. {action_1}
2. {action_2}
```
