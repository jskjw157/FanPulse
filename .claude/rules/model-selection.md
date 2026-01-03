# Model Selection Strategy

작업 복잡도에 따른 모델 선택 전략입니다.

## Opus (고품질, 고비용)

**사용 시기:**
- 새로운 아키텍처 설계
- 복잡한 비즈니스 로직 구현
- 코드베이스 탐색 (Explore)
- 구현 계획 수립 (Plan)
- 중요한 보안 리뷰
- 레거시 마이그레이션 분석

**예시:**
- `/ddd-planning` - Bounded Context 설계
- `/feature-planner` - 복잡한 기능 계획
- `backend-architect` - 시스템 아키텍처 설계

## Sonnet (균형, 경제적)

**사용 시기:**
- 계획된 코드 구현 (Implement)
- 표준 CRUD 작업
- 테스트 코드 생성
- 간단한 리팩토링
- 코드 리뷰 (보안/성능 분석 포함)

**예시:**
- 계획서 기반 코드 작성
- API 엔드포인트 구현
- 단위 테스트 생성
- `code-reviewer` - 코드 품질/보안 분석

## Haiku (빠르고 저렴)

**사용 시기:**
- 간단한 설정 검증
- 주석/문서 추가
- 포맷팅
- 간단한 수정
- 정적 분석 결과 기반 검토

**예시:**
- `config-reviewer` - 설정 파일 검증
- `doc-writer` - 문서화 작업
- 린팅 오류 수정
- 변수명 개선

## 파이프라인 조합 전략

대규모 기능 개발 시 권장 워크플로우:

```
1. Explore (Opus)    - 코드베이스 탐색, 아키텍처 이해
2. Plan (Opus)       - 구현 계획 수립, 설계 검토
3. Implement (Sonnet) - 계획 기반 코드 작성
4. Review (Sonnet)   - 코드 품질/보안 분석
5. Document (Haiku)  - 문서화, 주석 추가
```

**비용 예시:**
- Explore (Opus): ~$2.50
- Plan (Opus): ~$2.00
- Implement (Sonnet): ~$1.50
- Review + Document (Sonnet/Haiku): ~$0.50
- **총합: ~$6.50** (품질 유지하며 비용 최적화)

## 에이전트별 권장 모델

| 에이전트 | 모델 | 이유 |
|---------|------|-----|
| backend-architect | opus | 아키텍처 설계는 고차원 판단 필요 |
| code-reviewer | sonnet | 보안/성능 분석에 중간 수준 필요 |
| config-reviewer | haiku | 정적 검증, 체크리스트 기반 |
| doc-writer | haiku | 템플릿 기반 문서화 |

## 동적 모델 선택 힌트

작업 중 복잡도가 예상보다 높으면 상위 모델 사용을 권장:

- Haiku 작업 중 복잡한 판단 필요 → Sonnet으로 전환
- Sonnet 작업 중 아키텍처 결정 필요 → Opus로 전환

**핵심 원칙**: 복잡도에 맞는 모델 선택으로 품질과 비용의 균형
