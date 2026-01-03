# 스크립트 우선 원칙 (Script-First Principle)

이 규칙은 모든 작업에 우선 적용됩니다.

## 핵심 원칙

**"과정은 스크립트에게, 결과만 AI에게"**

- 데이터 수집, 정제, 변환은 스크립트가 수행 (토큰 소모 0)
- AI는 최종 결과만 읽고 판단, 요약, 창의적 작업 수행 (토큰 최소화)

## 다음 작업은 반드시 스크립트로 처리

### 웹 스크래핑 및 파싱

**❌ 직접 HTML 파싱하지 말 것**
```bash
# ✅ 스크립트가 HTML 파싱 → JSON 반환
python script/scrape_clean.py {url}
# 출력: {"colors": ["#fff", "#000"], "fonts": ["Inter"]}
```

**토큰 절감**: HTML 전체(5,000토큰) → JSON(300토큰) = 94% 절감

### 코드 분석

**❌ 파일 하나하나 읽으며 분석하지 말 것**
```bash
# ✅ 정적 분석기 실행 → JSON 리포트만 읽기
python script/code_review_analyzer.py --output .claude/review-report.json
```

**정적 분석기**:
- **Kotlin**: `ktlint`, `detekt`
- **TypeScript**: `eslint`, `tsc`
- **Python**: `flake8`, `mypy`

**토큰 절감**: 전체 파일 분석(15,000토큰) → JSON 리포트(800토큰) = 95% 절감

### 이미지/미디어 처리

**❌ 이미지 Base64를 컨텍스트에 로드하지 말 것**
```bash
# ✅ 로컬 스크립트로 처리
node script/process_image.js {input} {output}
```

**토큰 절감**: Base64 이미지(수만 토큰) → 처리 결과(0토큰)

### 대량 파일 작업

**❌ 파일 하나씩 생성/수정하지 말 것**
```bash
# ✅ 배치 처리 스크립트
python script/bulk_processor.py --config {json}
# 출력: {"created": 100, "failed": 0}
```

**토큰 절감**: 파일별 생성(10,000토큰) → 통계 반환(100토큰) = 99% 절감

### 대규모 코드베이스 분석

**❌ 파일 하나하나 읽지 말 것**
```bash
# ✅ Repomix로 전체 코드 병합
python script/code_merger.py --output merged_code.txt
# AI는 merged_code.txt 하나만 읽음
```

**토큰 절감**: 개별 파일 읽기(20,000토큰) → 병합 파일(5,000토큰) = 75% 절감

## AI 에이전트 역할

AI는 다음 작업만 수행:

1. **스크립트 실행 결정**: 어떤 스크립트를 실행할지 선택
2. **JSON 결과 읽기**: 스크립트 출력(JSON, 통계)만 읽기
3. **최종 판단**: 아키텍처, 비즈니스 로직 등 고차원 판단
4. **요약 작성**: 사용자에게 전달할 최종 요약

## 금지 사항

- ❌ HTML/XML 전체를 직접 파싱
- ❌ 이미지 Base64를 대화 내역에 포함
- ❌ 대량 파일을 하나씩 생성
- ❌ 정적 분석 가능한 항목을 수동으로 체크

## 효과 측정

각 작업 후 `/context` 명령으로 토큰 사용량 확인

**목표**:
- 현재 122k/200k (61%) → 목표 50k/200k (25%)
- 평균 45-60% 토큰 절감
- 응답 속도 2-3배 향상
