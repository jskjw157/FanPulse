# Implementation Plan: 댓글 AI 필터링 E2E 통합 (#206 Phase 3)

**Status**: ⏳ Pending
**Started**: 2026-03-08
**Last Updated**: 2026-03-08
**Issue**: #206 (댓글 AI 필터링 E2E 통합)
**Worktree**: `.worktrees/206` (branch: `feature/206-comment-ai-filter`)
**Parent Plan**: Phase 1 (Django API 표준화) + Phase 2 (Spring API Key + 경로 동기화) 완료 상태

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
댓글 생성 시 Django AI 사이드카의 필터링을 호출하고, 결과에 따라 댓글 상태를
APPROVED/BLOCKED/PENDING으로 분기한다. AI 장애 시 Fail-Pending 전략으로
사용자 경험을 유지하면서 위험 콘텐츠 노출을 방지한다.

### Success Criteria
- [ ] SC-1: 정상 댓글 → `APPROVED` 상태로 저장
- [ ] SC-2: 금칙어/스팸 댓글 → `BLOCKED` 상태로 저장
- [ ] SC-3: AI 다운 → `PENDING` 상태로 저장 + 202 Accepted 응답
- [ ] SC-4: `comment.filter.approved/blocked/pending` Micrometer 메트릭 노출
- [ ] SC-5: CommentFilterLog에 모든 필터링 결과 기록
- [ ] SC-6: 기존 74개 AI 테스트 + 새 테스트 전부 통과

### User Impact
- 정상 댓글: 즉시 노출 (APPROVED)
- 유해 댓글: 즉시 차단 (BLOCKED) — 커뮤니티 건전성 유지
- AI 장애: 사용자 댓글은 접수되나 보류 (PENDING) — UX 유지 + 안전성 확보

---

## Architecture Decisions

| Decision | Rationale | Trade-offs |
|----------|-----------|------------|
| Comment를 PostgreSQL에 생성 | News 등 기존 엔티티와 동일 패턴, JPA + Flyway로 관리 | MongoDB 이슈 #25와 스택 불일치 (추후 마이그레이션 가능) |
| post_id를 VARCHAR(24)로 참조 | MongoDB ObjectId 참조 — saved_posts 테이블과 동일 패턴 | FK 제약 없음 (데이터 정합성은 앱 레벨) |
| Fail-Pending 전략 | Fail-Open(모두 허용)보다 안전, Fail-Closed(모두 차단)보다 UX 우수 | PENDING 댓글 배치 검토 시스템 필요 (후속 이슈) |
| FilterResult.filterType으로 상태 분기 | 기존 Fail-Open fallback의 filterType="fallback" 활용 | AiServiceFallback 변경 없이 서비스 레이어에서 해석 |
| CommentFilterLog 별도 테이블 | 감사 추적 + 재검토 시 원본 AI 응답 참조 가능 | 저장 비용 증가 (댓글당 1행 추가) |

---

## Dependencies

### Required Before Starting
- [x] Phase 1 완료: Django `/api/ai/*` 경로 + `ApiKeyPermission`
- [x] Phase 2 완료: Spring API Key 인증 + 경로 동기화 + 401 Fail-Closed
- [x] `CommentFilterPort` + `AiCommentFilterAdapter` 구현 완료
- [x] `AiServiceFallback.filterFallback()` → `filterType="fallback"` 반환

### External Dependencies
- Micrometer (Spring Boot Actuator 기본 포함)
- Spring Data JPA + Flyway (이미 사용 중)
- WireMock (테스트, 이미 설치됨)
- MockK (테스트, 이미 설치됨)

---

## Test Strategy

### Test Pyramid
| Test Type | Coverage Target | Purpose |
|-----------|-----------------|---------|
| Unit Tests (MockK) | >=90% | CommentService 상태 분기 로직, Comment 엔티티 |
| Integration Tests (WireMock) | Critical paths | Controller → Service → AI 전체 플로우 |
| Repository Tests (H2) | >=80% | CommentPort JPA 쿼리 검증 |

### Test File Organization
```
src/test/kotlin/com/fanpulse/
├── domain/comment/
│   └── CommentTest.kt                      (엔티티 단위 테스트)
├── application/service/comment/
│   └── CommentCommandServiceTest.kt        (서비스 로직 단위 테스트)
├── infrastructure/persistence/comment/
│   └── CommentAdapterTest.kt               (리포지토리 통합 테스트)
└── infrastructure/web/comment/
    └── CommentControllerTest.kt            (API 통합 테스트)
```

---

## Implementation Phases

### Phase 3A: Comment 도메인 모델 + DB 마이그레이션
**Goal**: Comment/CommentFilterLog 엔티티, CommentStatus enum, Persistence Port, Flyway 스키마
**Estimated Time**: 2시간
**Status**: ⏳ Pending

#### Tasks

**RED: Write Failing Tests First**
- [ ] **Test 3A.1**: Comment 엔티티 단위 테스트
  - File: `domain/comment/CommentTest.kt` (신규)
  - 테스트 케이스:
    - `Comment.create()` → 기본 상태 PENDING, UUID 생성, createdAt 설정
    - `Comment.approve()` → 상태 APPROVED로 변경
    - `Comment.block(reason)` → 상태 BLOCKED + reason 저장
    - `Comment.create()` 빈 content → `IllegalArgumentException`
    - 대댓글: parentCommentId 설정 가능
  - Expected: FAIL — Comment 클래스 없음
  - Mock: 없음 (순수 도메인 로직)

- [ ] **Test 3A.2**: CommentPort + CommentAdapter Repository 테스트
  - File: `infrastructure/persistence/comment/CommentAdapterTest.kt` (신규)
  - 테스트 케이스:
    - save() → ID로 조회 가능
    - findByPostId(postId, pageable) → 페이지네이션 동작
    - findById() → 존재하지 않는 ID → null
  - Expected: FAIL — 테이블/엔티티 없음
  - Dependencies: H2 in-memory DB, Spring Test

**GREEN: Implement**
- [ ] **Task 3A.3**: `CommentStatus` enum 생성
  - File: `domain/comment/CommentStatus.kt` (신규)
  - Values: `APPROVED`, `BLOCKED`, `PENDING`

- [ ] **Task 3A.4**: `Comment` JPA 엔티티 생성
  - File: `domain/comment/Comment.kt` (신규)
  - Fields:
    - `id: UUID` (PK)
    - `postId: String` (VARCHAR 24, MongoDB ObjectId 참조)
    - `userId: UUID` (FK → users)
    - `content: String` (TEXT)
    - `status: CommentStatus` (ENUM, 기본값 PENDING)
    - `blockReason: String?` (차단 사유)
    - `parentCommentId: UUID?` (대댓글)
    - `createdAt: Instant`, `updatedAt: Instant`
  - Methods: `create()`, `approve()`, `block(reason)`
  - Pattern: News 엔티티와 동일 (private constructor + companion object factory)

- [ ] **Task 3A.5**: `CommentFilterLog` JPA 엔티티 생성
  - File: `infrastructure/persistence/comment/CommentFilterLog.kt` (신규)
  - **Note**: 감사 로그는 도메인 불변식이 아닌 인프라 관심사 → infrastructure 레이어에 배치
  - Fields:
    - `id: UUID` (PK)
    - `commentId: UUID` (FK → comments, NOT NULL)
    - `isFiltered: Boolean` (NOT NULL)
    - `filterType: String` (NOT NULL, "LLM", "rule", "fallback", "noop")
    - `reason: String?`
    - `ruleName: String?`
    - `createdAt: Instant`
  - Method: `create(commentId, filterResult)`

- [ ] **Task 3A.6**: Port 인터페이스 생성
  - File: `domain/comment/port/CommentPort.kt` (신규)
    - `save(comment: Comment): Comment`
    - `findById(id: UUID): Comment?`
    - `findByPostIdAndStatus(postId: String, status: CommentStatus, pageRequest: PageRequest): PageResult<Comment>`
  - File: `application/port/out/CommentFilterLogPort.kt` (신규, application 레이어)
    - `save(log: CommentFilterLog): CommentFilterLog`

- [ ] **Task 3A.7**: JPA Repository + Adapter 구현
  - File: `infrastructure/persistence/comment/CommentJpaRepository.kt` (신규)
  - File: `infrastructure/persistence/comment/CommentAdapter.kt` (신규)
  - File: `infrastructure/persistence/comment/CommentFilterLogJpaRepository.kt` (신규)
  - File: `infrastructure/persistence/comment/CommentFilterLogAdapter.kt` (신규)
  - Pattern: NewsAdapter와 동일

- [ ] **Task 3A.8**: Flyway migration
  - File: `resources/db/migration/V117__create_comment_tables.sql` (신규)
  - Tables: `comments` + `comment_filter_logs`
  - **ENUM → VARCHAR+CHECK**: `status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('APPROVED', 'BLOCKED', 'PENDING'))`
  - **NOT NULL**: `is_filtered BOOLEAN NOT NULL`, `filter_type VARCHAR(50) NOT NULL`
  - Indexes: `comments(post_id, status, created_at DESC)` (3-column composite), `comments(user_id)`, `comment_filter_logs(comment_id)`
  - Partial index: `comments(parent_comment_id) WHERE parent_comment_id IS NOT NULL`
  - FK: `parent_comment_id → comments(id) ON DELETE SET NULL DEFERRABLE INITIALLY DEFERRED`
  - `COMMENT ON TABLE/COLUMN` 한글 주석 필수 (프로젝트 컨벤션)
  - `chk_comments_content_not_empty CHECK (LENGTH(TRIM(content)) > 0)`

**REFACTOR**
- [ ] **Task 3A.9**: Comment `@PreUpdate`로 updatedAt 자동 갱신 + KDoc 정리

#### Quality Gate

```bash
cd .worktrees/206/backend
./gradlew test --tests "*.CommentTest" --tests "*.CommentAdapterTest"
./gradlew test  # 기존 테스트 포함 전체 통과
```

- [ ] Tests written BEFORE production code
- [ ] Comment.create() → PENDING 기본 상태
- [ ] approve()/block() 상태 변경 동작
- [ ] Repository save/find 동작
- [ ] Flyway 마이그레이션 성공
- [ ] 기존 테스트 깨지지 않음

---

### Phase 3B: CommentService 핵심 비즈니스 로직
**Goal**: createComment()에서 AI 필터 호출 → 상태 분기 → 저장 + FilterLog 기록
**Estimated Time**: 2.5시간
**Status**: ⏳ Pending
**Dependencies**: Phase 3A 완료

#### Tasks

**RED: Write Failing Tests First**
- [ ] **Test 3B.1**: CommentCommandService 단위 테스트
  - File: `application/service/comment/CommentCommandServiceTest.kt` (신규)
  - Mock: `CommentFilterPort`, `CommentPort`, `CommentFilterLogPort`, `MeterRegistry`
  - 테스트 케이스:
    - 정상 댓글 (isFiltered=false, filterType="LLM") → APPROVED 저장
    - 금칙어 댓글 (isFiltered=true, filterType="rule") → BLOCKED 저장
    - AI 다운 (isFiltered=false, filterType="fallback") → PENDING 저장
    - 각 케이스에서 CommentFilterLog 저장 검증
    - 빈 content → `IllegalArgumentException`
  - Expected: FAIL — CommentCommandService 없음

- [ ] **Test 3B.2**: Fail-Pending 전략 테스트
  - File: 위와 동일 파일 (별도 @Nested)
  - 테스트 케이스:
    - filterType="fallback" → status=PENDING (Fail-Open과 다름!)
    - filterType="noop" (AI disabled) → status=APPROVED
    - filterType="LLM" + isFiltered=false → status=APPROVED
    - filterType="LLM" + isFiltered=true → status=BLOCKED
  - Expected: FAIL — 상태 분기 로직 없음

**GREEN: Implement**
- [ ] **Task 3B.3**: CommentCommandService 인터페이스 + 구현체
  - File: `application/service/comment/CommentCommandService.kt` (신규, 인터페이스)
  - File: `application/service/comment/CommentCommandServiceImpl.kt` (신규)
  - Core method:
    ```kotlin
    fun createComment(command: CreateCommentCommand): CommentResponse
    ```
  - Flow:
    1. `CommentFilterPort.filterComment(content)` → `FilterResult`
    2. `Comment.create(postId, userId, content)` → PENDING
    3. 상태 분기:
       - `isFiltered == true` → `comment.block(reason)`
       - `filterType == "fallback"` → PENDING 유지
       - else → `comment.approve()`
    4. `CommentPort.save(comment)`
    5. `CommentFilterLogPort.save(log)` — FilterResult 기록
    6. Micrometer counter increment
    7. Return `CommentResponse`

- [ ] **Task 3B.4**: CreateCommentCommand DTO
  - File: `application/dto/comment/CommentDtos.kt` (신규)
  - `CreateCommentCommand(postId, userId, content, parentCommentId?)`
  - `CommentResponse(id, postId, userId, content, status, createdAt)`

- [ ] **Task 3B.5**: 상태 분기 로직 구현 (TODO(human) 포함)
  - CommentCommandServiceImpl 내부 `private fun resolveStatus(filterResult: FilterResult): CommentStatus`
  - **매직 문자열 제거**: FilterType enum 또는 서비스 내 private enum으로 when 분기
  - **트랜잭션 격리**: FilterLog 저장은 `runCatching`으로 감싸서 로그 실패가 댓글 저장에 영향 없도록

**REFACTOR**
- [ ] **Task 3B.6**: 상태 분기 로직 정리 + 로깅 + FilterType 상수 정리

#### Quality Gate

```bash
cd .worktrees/206/backend
./gradlew test --tests "*.CommentCommandServiceTest"
./gradlew test  # 전체 통과
```

- [ ] Tests written BEFORE production code
- [ ] 정상 → APPROVED, 금칙어 → BLOCKED, AI다운 → PENDING
- [ ] CommentFilterLog 저장 검증
- [ ] 기존 AI 테스트 깨지지 않음

---

### Phase 3C: CommentController REST API
**Goal**: POST/GET 엔드포인트로 댓글 생성/조회 API 노출
**Estimated Time**: 2시간
**Status**: ⏳ Pending
**Dependencies**: Phase 3B 완료

#### Tasks

**RED: Write Failing Tests First**
- [ ] **Test 3C.1**: CommentController MockMvc 통합 테스트
  - File: `infrastructure/web/comment/CommentControllerTest.kt` (신규)
  - Mock: `CommentCommandService`, `CommentQueryService`
  - 테스트 케이스:
    - POST /api/posts/{postId}/comments → 201 Created (APPROVED/BLOCKED)
    - POST /api/posts/{postId}/comments → 202 Accepted (PENDING)
    - POST 빈 content → 400 Bad Request
    - GET /api/posts/{postId}/comments → 200 + 페이지네이션
    - 미인증 요청 → 401 Unauthorized
  - Expected: FAIL — Controller 없음

**GREEN: Implement**
- [ ] **Task 3C.2**: CommentController 구현
  - File: `infrastructure/web/comment/CommentController.kt` (신규)
  - Endpoints:
    - `POST /api/posts/{postId}/comments` → 201/202
    - `GET /api/posts/{postId}/comments` → 200 + Page
  - 202 Accepted: status=PENDING일 때 (사용자에게 정상 응답)
  - Request: `CreateCommentRequest(content, parentCommentId?)`
  - Response: `CommentResponse(id, postId, content, status, createdAt)`
  - `@AuthenticationPrincipal`로 userId 추출

- [ ] **Task 3C.3**: CommentQueryService (조회 전용)
  - File: `application/service/comment/CommentQueryService.kt` (신규)
  - File: `application/service/comment/CommentQueryServiceImpl.kt` (신규)
  - `getByPostId(postId, pageable)` → PageResult (APPROVED만 반환)
  - **JPA 쿼리에서 status 필터**: `findByPostIdAndStatus(postId, APPROVED, pageable)` — 페이지네이션 정확도 보장
  - Pageable → PageRequest 변환은 NewsQueryService 패턴 따름

**REFACTOR**
- [ ] **Task 3C.4**: Request validation + Error response 정리

#### Quality Gate

```bash
cd .worktrees/206/backend
./gradlew test --tests "*.CommentControllerTest"
./gradlew test  # 전체 통과
```

- [ ] Tests written BEFORE production code
- [ ] POST → 201 (APPROVED) / 202 (PENDING) 분기
- [ ] GET → APPROVED만 반환
- [ ] 인증 필수 검증
- [ ] 기존 테스트 깨지지 않음

---

### Phase 3D: Micrometer 메트릭 + E2E 통합 테스트
**Goal**: 운영 모니터링 메트릭 + 전 레이어 관통 E2E 테스트
**Estimated Time**: 1.5시간
**Status**: ⏳ Pending
**Dependencies**: Phase 3C 완료

#### Tasks

**RED: Write Failing Tests First**
- [ ] **Test 3D.1**: 메트릭 검증 테스트
  - File: `application/service/comment/CommentCommandServiceTest.kt` (확장)
  - 테스트 케이스:
    - createComment() APPROVED → `comment.filter.approved` counter +1
    - createComment() BLOCKED → `comment.filter.blocked` counter +1
    - createComment() PENDING → `comment.filter.pending` counter +1
  - Mock: `SimpleMeterRegistry` (in-memory)
  - Expected: FAIL — 메트릭 코드 없음

- [ ] **Test 3D.2**: E2E 통합 테스트 (WireMock + Full Context)
  - File: `infrastructure/web/comment/CommentE2ETest.kt` (신규)
  - WireMock: Django AI Sidecar stub
  - 테스트 케이스:
    - 정상 댓글 POST → AI 호출 → APPROVED 저장 → 201 응답
    - 금칙어 댓글 POST → AI 호출 → BLOCKED 저장 → 201 응답
    - AI 서버 다운 POST → fallback → PENDING 저장 → 202 응답
    - **AI 응답 타임아웃** → WireMock `delayResponse(5000)` → fallback → PENDING → 202
    - BLOCKED 댓글 GET 시 미노출 확인
    - **Controller validation**: 빈 content → 400, 미인증 → 401
  - DB 검증: H2 + 실제 JPA 트랜잭션

**GREEN: Implement**
- [ ] **Task 3D.3**: Micrometer Counter 연결
  - File: `application/service/comment/CommentCommandServiceImpl.kt` (수정)
  - Counter names:
    - `comment.filter.approved`
    - `comment.filter.blocked`
    - `comment.filter.pending`
  - Tag: `filter_type` (LLM/rule/fallback/noop)
  - **메트릭 tag 검증**: SimpleMeterRegistry에서 `filter_type` tag 값까지 assertion

- [ ] **Task 3D.4**: E2E 테스트 구현
  - `@SpringBootTest` + `@AutoConfigureWebTestClient` 또는 `MockMvc`
  - WireMock 서버로 Django AI 응답 제어
  - 전체 플로우: HTTP → Controller → Service → AI Port → DB

**REFACTOR**
- [ ] **Task 3D.5**: 메트릭 이름/태그 정리 + 테스트 커버리지 확인

#### Quality Gate

```bash
cd .worktrees/206/backend
./gradlew test  # 전체 통과 (기존 + 새 테스트)
./gradlew jacocoTestReport  # 커버리지 확인 (가능한 경우)
```

- [ ] Tests written BEFORE production code
- [ ] 3개 메트릭 카운터 동작 확인
- [ ] E2E: 정상/금칙어/AI다운 3개 시나리오 통과
- [ ] BLOCKED 댓글 GET 미노출 확인
- [ ] 전체 테스트 스위트 통과

---

## Risk Assessment

| Risk | Probability | Impact | Mitigation Strategy |
|------|-------------|--------|---------------------|
| Flyway 번호 충돌 | Low | Medium | V117 사용 (기존 V1~V116 확인 완료) |
| Comment-User FK 문제 (테스트) | Medium | Low | H2 in-memory에 users 테이블 seed 또는 FK 없이 테스트 |
| PostId 정합성 (MongoDB 참조) | Low | Medium | VARCHAR(24) 제약 + 앱 레벨 검증 |
| CommentFilterPort fallback 해석 변경 | Low | High | 기존 AiServiceFallback 수정 없이 서비스 레이어에서 해석 |
| Actuator 메트릭 미노출 | Low | Low | `management.endpoints.web.exposure.include` 확인 |

---

## Rollback Strategy

### If Phase 3A Fails
- `git revert` — 엔티티/마이그레이션 되돌리기
- Flyway migration: `flyway clean` (개발 환경) 또는 V117 `DROP TABLE` 역마이그레이션

### If Phase 3B Fails
- CommentCommandService 삭제 → Phase 3A는 유지 (독립적)

### If Phase 3C Fails
- CommentController 삭제 → Phase 3A/3B는 유지

### If Phase 3D Fails
- 메트릭 코드 제거 → 핵심 기능은 유지

---

## Progress Tracking

### Completion Status
- **Phase 3A**: ⏳ 0%
- **Phase 3B**: ⏳ 0%
- **Phase 3C**: ⏳ 0%
- **Phase 3D**: ⏳ 0%

**Overall Progress**: 0% complete

### Time Tracking
| Phase | Estimated | Actual | Variance |
|-------|-----------|--------|----------|
| Phase 3A | 2h | - | - |
| Phase 3B | 2.5h | - | - |
| Phase 3C | 2h | - | - |
| Phase 3D | 1.5h | - | - |
| **Total** | 8h | - | - |

---

## Notes & Learnings

### Phase 2 교훈 (적용 사항)
- `rethrowIfUnauthorized()` DRY 원칙 — 공통 유틸 추출로 해결
- CB ignoreExceptions에 401 추가 → 인증 실패가 CB를 오염시키지 않음
- `filterType="fallback"` 이 Fail-Pending의 핵심 분기 포인트

### Fail-Pending vs Fail-Open 전략
- 기존 `AiServiceFallback`: Fail-Open (isFiltered=false, filterType="fallback")
- 이 기능: filterType=="fallback"을 감지하여 PENDING으로 분기
- AiServiceFallback 자체는 변경하지 않음 (기존 계약 유지)

---

## References

### Related Issues
- Issue #206: 댓글 AI 필터링 E2E 통합 (현재)
- Issue #25: 댓글 CRUD API (후속 — 전체 CRUD)
- Issue #24: 게시글 CRUD API (후속 — Post 도메인)
- Issue #88: AI 게시글/댓글 자동 필터링 (상위 에픽)
- Issue #203/#204: Django AI 클라이언트 (완료)
- Issue #205: Django API 표준화 (완료)

### Key Files (기존)
- `CommentFilterPort.kt` — AI 필터 도메인 포트
- `AiCommentFilterAdapter.kt` — Django HTTP 어댑터
- `AiServiceFallback.kt` — Fail-Open + rethrowIfUnauthorized
- `FilterResult.kt` — 도메인 값 객체

### Key Files (신규 — 이 플랜에서 생성)
- `domain/comment/Comment.kt` — Comment 엔티티 (@PreUpdate for updatedAt)
- `domain/comment/CommentStatus.kt` — 상태 enum
- `infrastructure/persistence/comment/CommentFilterLog.kt` — 필터링 감사 로그 (인프라 레이어)
- `domain/comment/port/CommentPort.kt` — 영속성 포트 (findByPostIdAndStatus)
- `application/port/out/CommentFilterLogPort.kt` — 감사 로그 포트 (application 레이어)
- `application/service/comment/CommentCommandServiceImpl.kt` — 핵심 비즈니스 로직 (runCatching 격리)
- `infrastructure/web/comment/CommentController.kt` — REST API
- `V117__create_comment_tables.sql` — DB 마이그레이션 (VARCHAR+CHECK, 3-column index)

---

## Final Checklist

- [ ] 전체 Phase 완료 + Quality Gate 통과
- [ ] `./gradlew test` 전체 통과
- [ ] SC-1~SC-6 모든 Success Criteria 충족
- [ ] /simplify 코드 리뷰 실행
- [ ] 커밋 (/commit-kr 컨벤션)

---

**Plan Status**: ⏳ Pending
**Next Action**: Phase 3A RED — Comment 엔티티 단위 테스트 작성
**Blocked By**: None (Phase 2 완료 상태)
