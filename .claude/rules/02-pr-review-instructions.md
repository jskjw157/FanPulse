# PR Review via Telegram Channel

## 트리거

Telegram 채널에서 `[PR-REVIEW-REQUEST]` 접두사가 포함된 메시지를 수신하면
자동으로 PR 코드 리뷰를 수행한다.

수신 메시지 형식:
```
[PR-REVIEW-REQUEST]
repo: {owner}/{repo}
pr: #{번호}
title: {PR 제목}
author: {작성자}
head: {헤드 브랜치}
base: {베이스 브랜치}
sha: {커밋 SHA}
event: {opened|synchronize|reopened}
url: {PR URL}
```

## 리뷰 절차

1. 메시지에서 `pr:`, `sha:`, `repo:`, `url:` 필드 추출
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
- **이 PR에서 변경하지 않은 기존 코드의 아키텍처 문제** (pre-existing issues)
- **다른 브랜치/이슈에서 다뤄야 할 기존 설계 결함**

### 리뷰 범위 원칙
- **이 PR이 도입한 변경사항**만 리뷰한다
- 기존 코드의 문제점은 이 PR의 책임이 아니다
- 단, 이 PR의 변경이 기존 코드에 **사이드 이펙트**를 일으키는 경우는 지적한다
- 예: 새로 추가한 코드가 기존 트랜잭션 경계를 깨뜨리는 경우 → 지적 대상
- 예: 기존 코드에 원래 있던 아키텍처 위반 → 지적 대상 아님

### Severity 기준
- **critical**: 프로덕션 크래시, 데이터 손실, 보안 취약점 (머지 차단)
- **high**: 확실한 버그, 보안 우려 (머지 차단하지 않음, 경고)
- **medium**: 잠재적 이슈, 개선 권장
- **low**: 사소한 개선 제안

### 머지 차단 기준
- critical 이슈가 1개 이상 → failure (머지 차단)
- critical 없음 → success (머지 허용)

## 프로젝트 컨텍스트

- Spring Boot + Kotlin (Hexagonal Architecture)
- Django AI Sidecar (요약, 필터링, 모더레이션)
- Fail-Open 전략: AI 실패 → 콘텐츠 허용 (Moderation/Summarizer)
- Fail-Pending 전략: AI 실패 → PENDING 상태 (Comment filter)
- CircuitBreaker + Fallback 패턴 사용

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

```
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
결과: success / failure
이슈: critical {N}, high {N}, medium {N}
```

스킵한 경우:
```
PR #{번호} skipped (newer SHA exists)
```
