---
name: doc-writer
description: Kotlin/Spring 코드 문서화 전문 skill. KDoc 작성, 인라인 주석 추가, 문서화 품질 검토를 수행합니다. 사용 시기: (1) 코드에 KDoc/주석 추가 요청 시 (2) 문서화 품질 검토 요청 시 (3) "문서화", "KDoc", "주석 추가" 키워드 사용 시 (4) 새 API endpoint 작성 후 (project)
---

# Doc Writer

Kotlin/Spring 프로젝트의 코드 문서화를 수행합니다.

## 핵심 원칙

1. **DRY**: Interface에 KDoc 작성 시 Impl에 중복 금지
2. **독자 중심**: "왜"와 "어떻게"에 집중, "무엇"은 코드가 설명
3. **Self-Documenting Code**: 좋은 네이밍이 최고의 문서

## 문서화 우선순위

| 레이어 | 우선순위 | 방식 |
|--------|----------|------|
| Controller | ⭐⭐⭐ 필수 | KDoc (endpoint 설명, 파라미터, 응답) |
| Service Interface | ⭐⭐ 권장 | KDoc (비즈니스 의미 설명) |
| Service Impl | ⭐ 선택 | 인라인 주석 (구현 노트만) |
| DTO (단순) |  ⭐⭐ 권장 | 인라인 주석 |
| DTO (복잡) | ⭐⭐ 권장 | KDoc |
| Private 함수 | ⭐ 선택 | 인라인 주석 |

## 워크플로우

1. 대상 파일 읽기
2. 레이어 유형 판단 (Controller/Service/DTO 등)
3. 우선순위에 따라 문서화 수준 결정
4. KDoc 또는 인라인 주석 작성
5. DRY 원칙 확인 (중복 체크)

## 상세 가이드

- **KDoc 작성법**: See [references/kdoc-guidelines.md](references/kdoc-guidelines.md)
