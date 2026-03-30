# Implementation Plan: Spring API Key 인증 + Django 경로 동기화 (#206 Part 1)

**Status**: ⏳ Pending
**Started**: 2026-03-08
**Last Updated**: 2026-03-08
**Issue**: #206 (댓글 AI 필터링 E2E) — Spring 측 사전 작업
**Worktree**: `.worktrees/206` (branch: `feature/206-comment-ai-filtering`)
**Parent Plan**: `PLAN_205-207_ai_pipeline_integration.md` Phase 2

---

**CRITICAL INSTRUCTIONS**: After completing each phase:
1. Check off completed task checkboxes
2. Run all quality gate validation commands
3. Verify ALL quality gate items pass
4. Update "Last Updated" date above
5. Document learnings in Notes section
6. Only then proceed to next phase

**DO NOT skip quality gates or proceed with failing checks**

---

## Overview

### Feature Description
Spring 백엔드 WebClient가 Django AI Sidecar에 `X-Api-Key` 헤더를 전송하고,
Phase 1에서 변경된 `/api/ai/*` 경로와 동기화한다.
401 응답은 Fail-Closed로 처리하여 인증 설정 오류를 빠르게 감지한다.

### Success Criteria
- [ ] SC-1: WireMock 테스트에서 `X-Api-Key` 헤더 전송 확인
- [ ] SC-2: 모든 어댑터가 `/api/ai/*` 경로로 요청
- [ ] SC-3: 401 응답 → fallback 없이 예외 전파
- [ ] SC-4: 401이 CB failure rate에 반영되지 않음 (ignoreExceptions)
- [ ] SC-5: apiKey 미설정 시 앱 시작 실패 (fail-fast)

### User Impact
Spring → Django 통신이 인증 보호 하에 동작하여 보안 강화.
인증 설정 오류 시 즉시 감지되어 디버깅 시간 단축.

---

## Architecture Decisions

| Decision | Rationale | Trade-offs |
|----------|-----------|------------|
| WebClient defaultHeader로 API Key 전송 | 모든 요청에 자동 적용, 어댑터 수정 불필요 | 엔드포인트별 키 분리 불가 (현재 불필요) |
| @PostConstruct fail-fast | 런타임 401보다 시작 시 실패가 디버깅 용이 | 개발 환경에서도 키 설정 필요 (enabled=false로 우회) |
| CB ignoreExceptions에 401 추가 | 인증 실패는 일시적 장애가 아님, CB 열림 방지 | 잘못된 키로 인한 401 연쇄는 CB가 감지 못함 |
| ExchangeFilterFunction으로 키 REDACTED 로깅 | 디버그 로그에서 키 노출 방지 | 로그 레벨 DEBUG일 때만 동작 |

---

## Dependencies

### Required Before Starting
- [x] Phase 1 완료: Django `/api/ai/*` 경로 + `ApiKeyPermission` (worktree 205, 커밋 2f6c6e6)
- [x] #204 완료: Spring AI 클라이언트 (worktree 203) — 머지 필요

### Pre-Work
- [ ] Worktree 206에 worktree 203 코드 머지 (`origin/feature/203-spring-ai-client`)

### External Dependencies
- Resilience4j (이미 설치됨)
- WireMock (이미 설치됨)

---

## Test Strategy

### Test Pyramid
| Test Type | Coverage Target | Purpose |
|-----------|-----------------|---------|
| Unit Tests (MockK) | >=80% | AiServiceProperties validation, fallback 분기 |
| Integration Tests (WireMock) | Critical paths | API Key 헤더 전송, 경로 정합성, 401 처리 |

### Test File Organization
```
src/test/kotlin/com/fanpulse/infrastructure/external/ai/
├── AiServiceConfigTest.kt          (신규 — API Key 헤더 + fail-fast)
├── AiNewsSummarizerAdapterTest.kt   (수정 — 경로 + 헤더)
├── AiModerationAdapterTest.kt       (수정 — 경로 + 헤더)
├── AiCommentFilterAdapterTest.kt    (수정 — 경로 + 헤더)
└── AiServiceResilienceTest.kt       (수정 — 401 Fail-Closed)
```

---

## Implementation Phases

### Phase 2A: API Key 헤더 전송 + Fail-Fast
**Goal**: WebClient에 `X-Api-Key` 기본 헤더를 추가하고, apiKey 미설정 시 시작 실패
**Estimated Time**: 1.5시간
**Status**: ⏳ Pending

#### Pre-Work
- [ ] **Task 0**: Worktree 206에 worktree 203 머지
  - `cd .worktrees/206 && git merge origin/feature/203-spring-ai-client`
  - 머지 후 `./gradlew compileKotlin`으로 빌드 확인

#### Tasks

**RED: Write Failing Tests First**
- [ ] **Test 2A.1**: API Key 헤더 전송 WireMock 테스트
  - File: `AiServiceConfigTest.kt` (신규)
  - 테스트 케이스:
    - WebClient가 `X-Api-Key: test-key` 헤더를 모든 요청에 포함
    - WireMock stub에서 헤더 검증 (`withHeader("X-Api-Key", equalTo(...))`)
  - Expected: FAIL — WebClient에 defaultHeader 없음

- [ ] **Test 2A.2**: apiKey 미설정 시 fail-fast 테스트
  - File: `AiServiceConfigTest.kt`
  - 테스트 케이스:
    - `AiServiceProperties(apiKey = "")` + `enabled = true` → 예외 발생
    - `AiServiceProperties(apiKey = "")` + `enabled = false` → 정상 (NoOp)
  - Expected: FAIL — apiKey 필드가 없음

**GREEN: Implement**
- [ ] **Task 2A.3**: `AiServiceProperties`에 `apiKey` 필드 추가
  - File: `AiServiceConfig.kt`
  - `val apiKey: String = ""`
  - `application.yml`: `fanpulse.ai-service.api-key: ${AI_SERVICE_API_KEY:}`

- [ ] **Task 2A.4**: WebClient `defaultHeader("X-Api-Key", ...)` 추가
  - File: `AiServiceConfig.kt`
  - `WebClient.builder().defaultHeader("X-Api-Key", aiServiceProperties.apiKey)`

- [ ] **Task 2A.5**: `@PostConstruct` fail-fast 검증 추가
  - File: `AiServiceConfig.kt`
  - `init { require(apiKey.isNotBlank() || !enabled) { "AI_SERVICE_API_KEY must be set..." } }`

- [ ] **Task 2A.6**: ExchangeFilterFunction으로 API Key REDACTED 로깅
  - 요청 로그에서 `X-Api-Key` 값을 `***` 로 마스킹
  - DEBUG 레벨에서만 동작

**REFACTOR**
- [ ] **Task 2A.7**: KDoc 정리 + 불필요한 로깅 제거

#### Quality Gate

```bash
cd .worktrees/206/backend
./gradlew test --tests "*.AiServiceConfigTest"
# 새 테스트 전부 통과
```

- [ ] Tests written BEFORE production code
- [ ] WebClient에 X-Api-Key 헤더 포함 확인
- [ ] apiKey 미설정 + enabled=true → 시작 실패
- [ ] apiKey 미설정 + enabled=false → 정상 시작

---

### Phase 2B: 어댑터 경로 동기화
**Goal**: 3개 어댑터의 PATH 상수를 Django Phase 1 변경에 맞춤
**Estimated Time**: 1시간
**Status**: ⏳ Pending

#### Tasks

**RED: Write Failing Tests First**
- [ ] **Test 2B.1**: 새 경로 WireMock 스텁 테스트
  - Files: 기존 `Ai*AdapterTest.kt` 3개 수정
  - 변경:
    - `stubFor(post("/api/summarize"))` → `stubFor(post("/api/ai/summarize"))`
    - `stubFor(post("/api/comments/filter/test"))` → `stubFor(post("/api/ai/filter"))`
    - `stubFor(post("/api/moderation/check"))` → `stubFor(post("/api/ai/moderate"))`
    - `stubFor(post("/api/moderation/batch"))` → `stubFor(post("/api/ai/moderate/batch"))`
  - WireMock stub에 `withHeader("X-Api-Key", ...)` 검증도 추가
  - Expected: FAIL — 어댑터가 아직 구 경로 사용

**GREEN: Implement**
- [ ] **Task 2B.2**: 어댑터 경로 상수 업데이트
  - `AiNewsSummarizerAdapter.kt`: `"/api/summarize"` → `"/api/ai/summarize"`
  - `AiCommentFilterAdapter.kt`: `"/api/comments/filter/test"` → `"/api/ai/filter"`
  - `AiModerationAdapter.kt`:
    - `"/api/moderation/check"` → `"/api/ai/moderate"`
    - `"/api/moderation/batch"` → `"/api/ai/moderate/batch"`

**REFACTOR**
- [ ] **Task 2B.3**: PATH 상수 네이밍 통일 (e.g., `SUMMARIZE_PATH`, `FILTER_PATH`, `MODERATE_PATH`)

#### Quality Gate

```bash
cd .worktrees/206/backend
./gradlew test --tests "*.AiNewsSummarizerAdapterTest" --tests "*.AiModerationAdapterTest" --tests "*.AiCommentFilterAdapterTest"
# 기존 + 수정된 테스트 전부 통과
```

- [ ] WireMock 스텁이 `/api/ai/*` 경로로 변경됨
- [ ] 모든 어댑터 테스트에서 X-Api-Key 헤더 검증 포함

---

### Phase 2C: 401 Fail-Closed 처리
**Goal**: 인증 실패(401)는 Fail-Open하지 않고 즉시 예외 전파. CB가 401을 무시하도록 설정.
**Estimated Time**: 1.5시간
**Status**: ⏳ Pending

#### Tasks

**RED: Write Failing Tests First**
- [ ] **Test 2C.1**: 401 → Fail-Closed 동작 테스트
  - File: `AiServiceResilienceTest.kt` (확장)
  - 테스트 케이스:
    - Django가 401 반환 → `AiServiceException` (또는 적절한 예외) 전파
    - fallback이 호출되지 않음 (`AiServiceFallback` 미사용)
  - Expected: FAIL — 현재 fallback이 모든 에러를 잡음

- [ ] **Test 2C.2**: 401이 CB failure rate에 미반영 테스트
  - File: `AiServiceResilienceTest.kt`
  - 테스트 케이스:
    - 401 연속 발생 → CB 상태가 CLOSED 유지 (OPEN 전이 안 됨)
  - Expected: FAIL — ignoreExceptions 미설정

**GREEN: Implement**
- [ ] **Task 2C.3**: 어댑터 fallback에서 401 분기 처리
  - Files: `AiNewsSummarizerAdapter.kt`, `AiModerationAdapter.kt`, `AiCommentFilterAdapter.kt`
  - fallback 메서드에서:
    ```kotlin
    if (ex is WebClientResponseException.Unauthorized) throw ex  // 재전파
    return AiServiceFallback.summarize(...)  // 나머지는 Fail-Open
    ```

- [ ] **Task 2C.4**: application.yml CB ignoreExceptions 설정
  - 각 CB instance에 `ignoreExceptions` 추가:
    ```yaml
    resilience4j.circuitbreaker.instances:
      aiSummarizer:
        ignoreExceptions:
          - org.springframework.web.reactive.function.client.WebClientResponseException$Unauthorized
    ```

**REFACTOR**
- [ ] **Task 2C.5**: 401 처리 공통 유틸 추출 검토
  - 3개 어댑터의 401 분기가 동일 → `AiServiceFallback`에 공통 메서드 고려

#### Quality Gate

```bash
cd .worktrees/206/backend
./gradlew test
# 전체 테스트 통과 (기존 + 새 테스트)
```

- [ ] 401 응답 시 fallback 미호출, 예외 전파 확인
- [ ] 401 연속 발생 시 CB 상태 CLOSED 유지 확인
- [ ] 500/503 등 일반 에러는 기존대로 Fail-Open

---

## Risk Assessment

| Risk | Probability | Impact | Mitigation Strategy |
|------|-------------|--------|---------------------|
| Worktree 203 머지 충돌 | Low | Medium | 203은 새 파일 추가 위주, 기존 파일 수정 최소 |
| WireMock 테스트 깨짐 (경로 변경) | High | Low | 스텁 경로만 변경, 응답 구조 동일 |
| 개발 환경 API Key 설정 누락 | Medium | Low | `enabled=false` 기본값 또는 README에 설정 가이드 |
| CB ignoreExceptions 오타 | Low | High | 테스트 2C.2에서 검증 |

---

## Rollback Strategy

### If Phase 2A Fails
- `git revert` — `AiServiceConfig.kt`, `application.yml` 변경 되돌리기
- API Key 없이 동작하는 이전 상태로 복원

### If Phase 2B Fails
- 어댑터 PATH 상수를 구 경로로 복원
- Phase 2A는 유지 (독립적)

### If Phase 2C Fails
- fallback 401 분기 제거, CB ignoreExceptions 삭제
- Phase 2A, 2B는 유지 (독립적)

---

## Progress Tracking

### Completion Status
- **Phase 2A**: ⏳ 0%
- **Phase 2B**: ⏳ 0%
- **Phase 2C**: ⏳ 0%

**Overall Progress**: 0% complete

### Time Tracking
| Phase | Estimated | Actual | Variance |
|-------|-----------|--------|----------|
| Phase 2A | 1.5h | - | - |
| Phase 2B | 1h | - | - |
| Phase 2C | 1.5h | - | - |
| **Total** | 4h | - | - |

---

## Notes & Learnings

### Phase 1 교훈 (Django 측)
- DRF `NotAuthenticated` → 401이지만, `WWW-Authenticate` 헤더 없으면 403으로 변환됨
- MagicMock은 `__version__` 등 dunder 속성을 자동 처리 못함 → 수동 설정 필요
- DRF renderer가 Content-Type을 덮어씀 → middleware 패턴으로 해결

---

## References

### Related Issues
- Issue #205: Django API 표준화 (Phase 1, 완료)
- Issue #206: 댓글 AI 필터링 E2E
- Issue #204: Spring AI 클라이언트 (worktree 203)

### Key Files
- `AiServiceConfig.kt` — WebClient + Properties
- `AiServiceFallback.kt` — Fail-Open fallback 결과
- `application.yml` — CB + timeout 설정
- 어댑터 3개: `AiNewsSummarizerAdapter.kt`, `AiModerationAdapter.kt`, `AiCommentFilterAdapter.kt`

---

## Final Checklist

- [ ] 전체 phase 완료 + quality gate 통과
- [ ] `./gradlew test` 전체 통과
- [ ] API Key 설정 가이드 application.yml 주석 추가
- [ ] Phase 1 Django 커밋과 경로 정합성 확인

---

**Plan Status**: ⏳ Pending
**Next Action**: Phase 2A RED — API Key 헤더 전송 WireMock 테스트 작성
**Blocked By**: Worktree 206에 203 코드 머지
