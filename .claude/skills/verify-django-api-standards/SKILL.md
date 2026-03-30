---
name: verify-django-api-standards
description: Django AI Sidecar API 표준 검증 (API Key 인증, RFC7807, URL 패턴). 뷰 추가/수정 후 사용.
---

# verify-django-api-standards

## Purpose

Django AI Sidecar의 API 표준 준수 여부를 검증합니다:

1. **API Key 인증** — HealthCheck 외 모든 뷰에 `ApiKeyPermission` 적용 여부
2. **RFC7807 에러 형식** — exception handler와 미들웨어 설정 여부
3. **URL 패턴** — AI 엔드포인트의 `/api/ai/*` 접두사 일관성
4. **보안 설정** — SECRET_KEY, DEBUG, ALLOWED_HOSTS 프로덕션 설정

## When to Run

- `ai/api/views.py`에 새 뷰 클래스를 추가한 후
- `ai/api/urls.py`에 새 URL 패턴을 추가한 후
- `ai/config/settings.py`를 수정한 후
- Django AI sidecar 관련 PR 전

## Related Files

| File | Purpose |
|------|---------|
| `ai/api/views.py` | API 뷰 클래스 (permission_classes 확인) |
| `ai/api/urls.py` | URL 패턴 정의 (경로 규칙 확인) |
| `ai/api/permissions.py` | ApiKeyPermission 클래스 정의 |
| `ai/api/exception_handlers.py` | RFC7807 exception handler + middleware |
| `ai/config/settings.py` | Django 설정 (보안, 미들웨어, DRF 설정) |
| `ai/api/tests/test_api_key_auth.py` | API Key 인증 테스트 |
| `ai/api/tests/test_error_format.py` | RFC7807 에러 형식 테스트 |

## Workflow

### Step 1: API Key 인증 검사

**도구:** Grep

모든 APIView 서브클래스의 `permission_classes`를 확인합니다:

```bash
# 모든 뷰 클래스와 permission_classes 확인
grep -n 'class.*APIView\|permission_classes' ai/api/views.py
```

**규칙:**
- `HealthCheckView` → `permission_classes = []` (인증 불필요)
- 그 외 모든 뷰 → `permission_classes = [ApiKeyPermission]` 필수

**PASS 기준:** HealthCheck 외 모든 뷰에 `ApiKeyPermission`이 설정됨.
**FAIL 기준:** `permission_classes`가 없거나 빈 배열 `[]`인 뷰가 HealthCheck 외에 존재.

**수정:** 해당 뷰에 `permission_classes = [ApiKeyPermission]` 추가.

### Step 2: RFC7807 설정 검사

**도구:** Grep

#### 2a. Exception Handler 설정

```bash
grep -n 'EXCEPTION_HANDLER' ai/config/settings.py
```

**PASS 기준:** `'EXCEPTION_HANDLER': 'api.exception_handlers.rfc7807_exception_handler'` 존재.

#### 2b. Middleware 설정

```bash
grep -n 'RFC7807ContentTypeMiddleware' ai/config/settings.py
```

**PASS 기준:** `'api.exception_handlers.RFC7807ContentTypeMiddleware'`가 MIDDLEWARE 리스트에 존재.

#### 2c. RFC7807 필수 필드 검사

```bash
grep -n '"type"\|"title"\|"status"\|"detail"\|"instance"' ai/api/exception_handlers.py
```

**PASS 기준:** `type`, `title`, `status`, `detail`, `instance` 5개 필드가 모두 존재.

**수정:** 누락된 필드 또는 설정을 추가.

### Step 3: URL 패턴 검사

**도구:** Read + Grep

```bash
# urls.py에서 모든 path 정의 확인
grep -n "path('" ai/api/urls.py
```

**규칙:**
- AI 관련 엔드포인트: `ai/` 접두사 필수 (예: `ai/filter`, `ai/moderate`, `ai/summarize`)
- config/urls.py에서 `path('api/', include('api.urls'))` 설정 → 최종 경로: `/api/ai/*`
- Health check: `health` (예외)
- 뉴스 관련: `news/` 접두사

**PASS 기준:** AI 엔드포인트가 모두 `ai/` 접두사를 사용.
**FAIL 기준:** AI 기능 엔드포인트가 `ai/` 접두사 없이 정의됨.

### Step 4: 보안 설정 검사

**도구:** Grep

```bash
# 환경 변수 기반 보안 설정 확인
grep -n 'SECRET_KEY\|DEBUG\|ALLOWED_HOSTS\|AI_SERVICE_ACCEPTED_KEYS' ai/config/settings.py
```

**규칙:**
- `SECRET_KEY`: 환경 변수에서 읽어야 함 (`os.environ.get`)
- `DEBUG`: 환경 변수 기반 (프로덕션에서 False)
- `ALLOWED_HOSTS`: 하드코딩된 `*` 사용 금지 (프로덕션)
- `AI_SERVICE_ACCEPTED_KEYS`: 환경 변수에서 읽어야 함

**PASS 기준:** 모든 보안 설정이 환경 변수 기반.
**FAIL 기준:** 하드코딩된 시크릿 또는 `DEBUG = True` 고정.

## Output Format

```markdown
## Django API Standards 검증 결과

| # | 검사 | 상태 | 상세 |
|---|------|------|------|
| 1 | API Key 인증 | PASS/FAIL | N개 뷰 중 M개 미적용 |
| 2 | RFC7807 설정 | PASS/FAIL | handler/middleware/필드 |
| 3 | URL 패턴 | PASS/FAIL | ai/ 접두사 일관성 |
| 4 | 보안 설정 | PASS/FAIL | 환경 변수 기반 여부 |

### 위반 사항 (있는 경우)
| 파일 | 라인 | 위반 | 수정 방법 |
|------|------|------|----------|
| views.py | 53 | NewView에 ApiKeyPermission 누락 | permission_classes 추가 |
```

## Exceptions

다음은 **위반이 아닙니다**:

1. **HealthCheckView의 빈 permission_classes** — Health check는 인증 없이 접근 가능해야 합니다. 로드밸런서와 Docker healthcheck에서 사용됩니다.
2. **개발 환경의 DEBUG=True** — `settings.py`에서 환경 변수 기반으로 설정하되, 기본값이 True인 것은 개발 편의를 위해 허용됩니다 (프로덕션에서 환경 변수로 오버라이드).
3. **ALLOWED_HOSTS에 localhost 포함** — Docker 환경에서 `localhost`, `127.0.0.1`, `django-ai` (서비스명)는 허용됩니다.
