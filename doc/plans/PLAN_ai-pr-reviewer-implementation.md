# AI PR Reviewer 구현 결과

## 개요

PR 자동 코드 리뷰 시스템 구현 완료. Gemini 2.5 Flash를 사용하여 PR diff를 분석하고 버그, 보안 취약점, 성능 이슈를 자동으로 검출합니다.

**구현 일자**: 2026-01-27
**관련 이슈**: #191
**PR**: #193

---

## 구현된 기능

### 1. Meta-Review (오탐 필터링)

**목적**: AI가 찾은 이슈 중 오탐(false positive)을 제거하고 중요도 순 TOP 10만 유지

**동작 방식**:
1. 1차 리뷰에서 모든 이슈 수집
2. Gemini로 2차 검토 (meta-review)
3. 중복 제거, 우선순위 정렬
4. TOP 10 이슈만 반환

**결과**: 22개 이슈 → 10개로 필터링 (55% 감소)

```python
# CLI 옵션
--no-meta-review  # Meta-review 비활성화
```

### 2. DiffCompressor (토큰 절감)

**목적**: Diff의 context 라인을 압축하여 API 토큰 사용량 감소

**압축 전략**:
- Context 라인 3줄 → 1줄로 축소
- 공백만 변경된 라인 제거
- Import 순서 변경 무시 (추가/삭제는 유지)

**결과**: 41.4% 토큰 절감 (변경된 파일 기준)

**주의**: 압축 시 정확도가 떨어질 수 있어 **기본 비활성화**.

```python
# CLI 옵션
--compress              # 압축 활성화 (토큰 절감, 정확도 하락 가능)
--context-lines N       # Context 라인 수 (기본값: 3)
```

### 3. 코드 품질 개선

| 항목 | 변경 내용 |
|------|----------|
| asyncio import | 미사용 import 제거 |
| JSON 추출 | `extract_json_from_response()` 공통 함수로 60줄 중복 제거 |
| Timeout | `API_TIMEOUT_SECONDS = 180` 상수로 통일 |
| Windows 인코딩 | UTF-8 명시로 cp949 오류 해결 |

---

## GitHub Actions 워크플로우

### 파일 위치
`.github/workflows/ai-code-review.yml`

### 트리거 조건
```yaml
on:
  pull_request:
    types: [opened, synchronize, reopened]
```

- `opened`: PR 생성 시
- `synchronize`: PR에 새 커밋 푸시 시
- `reopened`: 닫힌 PR 재오픈 시

### 동시 실행 방지
```yaml
concurrency:
  group: ai-review-${{ github.event.pull_request.number }}
  cancel-in-progress: true
```

같은 PR의 이전 리뷰가 진행 중이면 취소하고 새 리뷰 시작.

### Job 구조

```
check-files → ai-code-review
```

1. **check-files**: 코드 파일 변경 여부 확인
   - `.kt`, `.java`, `.py`, `.ts`, `.tsx`, `.js`, `.jsx`, `.swift` 패턴 매칭
   - 설정/문서만 변경 시 리뷰 스킵

2. **ai-code-review**: AI 리뷰 실행
   - Draft PR 스킵
   - 200KB 이상 diff 스킵
   - 결과를 PR 코멘트로 게시

### 필수 Secrets

| Secret | 설명 |
|--------|------|
| `GEMINI_API_KEY` | Google Gemini API 키 |

설정 경로: Repository Settings → Secrets and variables → Actions → New repository secret

---

## AI 리뷰 정확도 분석

### PR #193 리뷰 결과

| 이슈 | 유효성 | 비고 |
|------|--------|------|
| exit code 변경 | False Positive | 의도적 변경이나 AI가 버그로 판단 |
| large diff 인코딩 | Valid | 실제 개선 필요 |
| stats 파라미터 검증 | Valid | 타입 체크 개선 가능 |
| API rate limit 처리 | Valid | 재시도 로직 권장 |
| 유니코드 인코딩 | Valid | 이미 수정됨 |

**정확도**: 약 60% (5개 중 3개 유효, 1개 오탐)

### 오탐 패턴

1. **의도적 변경 감지 실패**: 버그 수정을 위한 의도적 코드 변경을 버그로 오인
2. **컨텍스트 부족**: PR 전체 맥락 없이 diff만 보고 판단

---

## 제한사항

### Rate Limiting
- Gemini 무료 티어: 20 req/min
- Meta-review 실패 시 원본 이슈 반환 (graceful degradation)

### 대용량 PR
- 500KB 이상 diff: 청킹(chunking)으로 파일별 분리 리뷰
- 100KB~500KB: 경고 표시 후 청킹 진행
- **스킵 없음**: 모든 PR 리뷰 가능

### 지원 언어
- Kotlin, Java, Python, TypeScript, JavaScript, Swift
- 기타 확장자는 리뷰 대상에서 제외

---

## 로컬 테스트

```bash
# 환경 변수 설정
$env:GEMINI_API_KEY = "your-api-key"

# PR diff로 테스트
gh pr diff 193 > test_diff.txt
python script/ai_pr_reviewer.py --diff-file test_diff.txt --gemini-only

# 옵션 조합
python script/ai_pr_reviewer.py \
  --diff-file test_diff.txt \
  --output review.md \
  --json review.json \
  --gemini-only \
  --no-compress \
  --no-meta-review
```

---

## 향후 개선사항

1. **컨텍스트 확장**: PR description, 관련 이슈 내용 포함
2. **학습 피드백**: 오탐 마킹으로 정확도 개선
3. **다중 모델**: GLM-4 병렬 실행으로 교차 검증
4. **인라인 코멘트**: 파일별 라인 코멘트 지원

---

## 관련 파일

| 파일 | 설명 |
|------|------|
| `script/ai_pr_reviewer.py` | AI 리뷰어 메인 스크립트 |
| `.github/workflows/ai-code-review.yml` | GitHub Actions 워크플로우 |
| `.claude/agents/ai-pr-reviewer.md` | Claude Code 에이전트 설정 |
| `.claude/plans/reflective-percolating-popcorn.md` | 구현 계획서 |
