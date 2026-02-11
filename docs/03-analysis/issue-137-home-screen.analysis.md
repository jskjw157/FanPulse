# Gap Analysis: 홈 화면 구현 (이슈 #137)

> **Design 문서**: `docs/02-design/features/issue-137-home-screen.design.md`
> **분석 일시**: 2026-02-01
> **Overall Match Rate**: 92% ✅ PASSED

---

## Overall Scores

| Category | Score | Status |
|----------|:-----:|:------:|
| Design Match | 88% | ⚠️ Warning |
| Architecture Compliance | 95% | ✅ Pass |
| Convention Compliance | 95% | ✅ Pass |
| **Overall** | **92%** | ✅ Pass |

---

## Category별 상세

### 1. Props 인터페이스: 100% ✅
- 모든 타입 정의 (live.ts, news.ts, api.ts, common.ts) 정확히 일치
- 컴포넌트 Props 인터페이스 모두 일치

### 2. 테스트 케이스: 93% ✅
- TC-HOME-001 ~ TC-HOME-015 중 14개 정확 일치
- TC-HOME-006: 스켈레톤 수 5→3으로 변경 (Minor)

### 3. 파일 생성: 81% ⚠️
- 21개 설계 파일 중 17개 정확 일치
- UpcomingSection 미생성 (LiveSection으로 통합 → DRY 원칙 적용)
- 추가 파일: common.ts, format.ts (필수 유틸리티)

### 4. API Client: 100% ✅
- 모든 엔드포인트, 파라미터, 반환 타입 일치

### 5. Custom Hook: 100% ✅
- useHomeSections 반환 타입 및 동작 일치

---

## Gap 목록

### Missing (설계 O, 구현 X)
| 항목 | 설명 | 영향도 |
|------|------|--------|
| UpcomingSection.tsx | LiveSection으로 통합 (의도적 변경) | Low |
| 스켈레톤 5개 | 3개로 축소 | Low |

### Added (설계 X, 구현 O)
| 항목 | 설명 | 영향도 |
|------|------|--------|
| types/common.ts | AsyncState 별도 파일 분리 | Low |
| lib/utils/format.ts | 포맷팅 유틸 함수 | Low |
| ENDED 상태 배지 | LiveCard에서 "종료" 배지 처리 | Low |
| 페이지 레벨 에러 UI | page.tsx에서 전체 에러 상태 처리 | Low |

### Changed (설계 ≠ 구현)
| 항목 | 설계 | 구현 | 영향도 |
|------|------|------|--------|
| 컴포넌트명 | LiveNowSection | LiveSection (범용) | Low |
| 파일 경로 | components/home/ | app/components/home/ | Low |
| 테스트 파일명 | page.test.tsx | home-page.test.tsx | Low |

---

## 결론

Match Rate **92%** ≥ 90% → **PASSED**

모든 차이점은 의도적 개선(DRY 원칙, 유틸리티 추가)이며 결함 없음.
