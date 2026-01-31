# Feature Plan: Issue #124 - Identity/Live/Content 컨텍스트 분리 및 OpenAPI 구현

**Issue**: [#124](https://github.com/jskjw157/FanPulse/issues/124)
**Created**: 2026-01-17
**Last Updated**: 2026-01-17
**Status**: In Progress
**Estimated Effort**: Medium (15-20 hours)

---

## CRITICAL INSTRUCTIONS

After completing each phase:
1. Check off completed task checkboxes
2. Run all quality gate validation commands
3. Verify ALL quality gate items pass
4. Update "Last Updated" date
5. Document learnings in Notes section
6. Only then proceed to next phase

**DO NOT skip quality gates or proceed with failing checks**

---

## Overview

### Objectives
1. **Identity Context 구현**: 사용자 인증/인가 시스템 (TDD)
2. **Streaming Context REST API**: 기존 도메인 모델에 REST 엔드포인트 추가
3. **Content Context 구현**: News, Chart, Artist 도메인 및 API
4. **OpenAPI 스펙 완성**: 모든 엔드포인트 문서화

### Architecture Decisions

| 결정 | 근거 |
|------|------|
| JWT 토큰 기반 인증 | 무상태(Stateless) API, 모바일 앱 지원 |
| BCrypt 비밀번호 해싱 | Spring Security 표준, 보안성 |
| Port/Adapter 패턴 | 도메인 격리, 테스트 용이성 |
| 크롤링 테이블 재사용 | 기존 V7 마이그레이션 활용, DB 변경 최소화 |

### Completed Work (Phase 1: Design)
- [x] Identity Context 도메인 모델 (User, AuthToken, UserSettings)
- [x] Streaming Context 도메인 모델 (StreamingEvent)
- [x] Content Context DDD 설계 (News, Chart, Artist)
- [x] OpenAPI v1 스펙 정의 (`doc/api/openapi-v1.yaml`)
- [x] REST Controller 스켈레톤 (AuthController, MeController, StreamingEventController)

---

## Phase Breakdown

### Phase 1: Identity - JWT Infrastructure (2-3 hours)

**Goal**: JWT 토큰 발급/검증 인프라 구축

**Test Strategy**:
- Unit Tests: JwtTokenProvider 메서드별 테스트
- Coverage Target: 90%

**Tasks (TDD)**:

#### RED Phase
- [ ] `JwtTokenProviderTest.kt` 작성
  - [ ] `should generate valid access token`
  - [ ] `should generate valid refresh token`
  - [ ] `should validate token and extract claims`
  - [ ] `should reject expired token`
  - [ ] `should reject invalid signature`

#### GREEN Phase
- [ ] `JwtTokenProvider.kt` 구현
  - [ ] Access Token 생성 (1시간 만료)
  - [ ] Refresh Token 생성 (7일 만료)
  - [ ] Token 검증 및 Claims 추출
- [ ] `application.yml` JWT 설정 추가
  - [ ] `jwt.secret`, `jwt.access-expiration`, `jwt.refresh-expiration`

#### REFACTOR Phase
- [ ] 상수 추출 및 코드 정리
- [ ] 예외 처리 개선

**Quality Gate**:
```bash
./gradlew test --tests "*JwtTokenProviderTest*"
./gradlew jacocoTestReport  # coverage >= 90%
```

- [ ] 모든 테스트 통과
- [ ] 커버리지 90% 이상
- [ ] 빌드 성공

**Dependencies**: None

---

### Phase 2: Identity - Repository Adapters (2 hours)

**Goal**: User, UserSettings, OAuthAccount JPA Repository 구현

**Test Strategy**:
- Integration Tests: TestContainers PostgreSQL
- Coverage Target: 80%

**Tasks (TDD)**:

#### RED Phase
- [ ] `UserRepositoryTest.kt` 작성
  - [ ] `should save and find user by id`
  - [ ] `should find user by email`
  - [ ] `should check email exists`
  - [ ] `should check username exists`
- [ ] `UserSettingsRepositoryTest.kt` 작성
  - [ ] `should save and find settings by user id`
- [ ] `OAuthAccountRepositoryTest.kt` 작성
  - [ ] `should find by provider and provider user id`

#### GREEN Phase
- [ ] `UserJpaRepository.kt` (extends JpaRepository, implements UserPort)
- [ ] `UserSettingsJpaRepository.kt` (implements UserSettingsPort)
- [ ] `OAuthAccountJpaRepository.kt` (implements OAuthAccountPort)

#### REFACTOR Phase
- [ ] Query 최적화 (@Query 어노테이션 활용)
- [ ] 인덱스 확인

**Quality Gate**:
```bash
./gradlew test --tests "*RepositoryTest*"
```

- [ ] 모든 통합 테스트 통과
- [ ] DB 연결 정상
- [ ] 빌드 성공

**Dependencies**: Phase 1 완료

---

### Phase 3: Identity - AuthService Implementation (3-4 hours)

**Goal**: 회원가입, 로그인, 토큰 갱신, 로그아웃 구현

**Test Strategy**:
- Unit Tests: Service 로직 (Mock Repository)
- Integration Tests: 전체 플로우
- Coverage Target: 85%

**Tasks (TDD)**:

#### RED Phase
- [ ] `AuthServiceTest.kt` 작성
  - [ ] `signup should create user and return tokens`
  - [ ] `signup should throw when email exists`
  - [ ] `signup should throw when username exists`
  - [ ] `signup should validate password strength`
  - [ ] `login should return tokens for valid credentials`
  - [ ] `login should throw for invalid email`
  - [ ] `login should throw for wrong password`
  - [ ] `refreshToken should return new tokens`
  - [ ] `refreshToken should throw for expired token`
  - [ ] `logout should invalidate token`

#### GREEN Phase
- [ ] `AuthServiceImpl.kt` 구현
  - [ ] `signup()`: 사용자 생성 + 토큰 발급
  - [ ] `login()`: 비밀번호 검증 + 토큰 발급
  - [ ] `googleLogin()`: OAuth 처리 (기본 구조)
  - [ ] `refreshToken()`: 토큰 갱신
  - [ ] `logout()`: 토큰 무효화
- [ ] `PasswordEncoder` Bean 설정

#### REFACTOR Phase
- [ ] 도메인 이벤트 발행 (UserRegistered, UserLoggedIn)
- [ ] 예외 처리 통일

**Quality Gate**:
```bash
./gradlew test --tests "*AuthServiceTest*"
./gradlew jacocoTestReport
```

- [ ] 모든 테스트 통과
- [ ] 커버리지 85% 이상
- [ ] 도메인 이벤트 발행 확인

**Dependencies**: Phase 1, 2 완료

---

### Phase 4: Identity - Security Configuration (2 hours)

**Goal**: Spring Security 설정 및 인증 필터 구현

**Test Strategy**:
- Integration Tests: 엔드포인트 접근 제어 테스트
- Coverage Target: 75%

**Tasks (TDD)**:

#### RED Phase
- [ ] `SecurityConfigTest.kt` 작성
  - [ ] `public endpoints should be accessible without auth`
  - [ ] `protected endpoints should return 401 without token`
  - [ ] `protected endpoints should be accessible with valid token`

#### GREEN Phase
- [ ] `SecurityConfig.kt` 구현
  - [ ] CORS 설정
  - [ ] CSRF 비활성화 (JWT 사용)
  - [ ] 공개 엔드포인트 설정 (`/api/v1/auth/**`)
  - [ ] 인증 필요 엔드포인트 설정
- [ ] `JwtAuthenticationFilter.kt` 구현
  - [ ] Authorization 헤더에서 토큰 추출
  - [ ] 토큰 검증 및 SecurityContext 설정

#### REFACTOR Phase
- [ ] 예외 응답 포맷 통일 (401, 403)

**Quality Gate**:
```bash
./gradlew test --tests "*SecurityConfigTest*"
```

- [ ] 공개 API 접근 가능
- [ ] 보호 API 인증 필요
- [ ] 빌드 성공

**Dependencies**: Phase 3 완료

---

### Phase 5: Identity - REST Controllers Integration (2 hours)

**Goal**: AuthController, MeController 통합 테스트

**Test Strategy**:
- E2E Tests: 전체 인증 플로우
- Coverage Target: 80%

**Tasks (TDD)**:

#### RED Phase
- [ ] `AuthControllerIntegrationTest.kt` 작성
  - [ ] `POST /api/v1/auth/signup should return 201 with tokens`
  - [ ] `POST /api/v1/auth/login should return 200 with tokens`
  - [ ] `POST /api/v1/auth/refresh should return 200 with new tokens`
  - [ ] `POST /api/v1/auth/logout should return 200`
- [ ] `MeControllerIntegrationTest.kt` 작성
  - [ ] `GET /api/v1/me should return user profile`
  - [ ] `PATCH /api/v1/me should update profile`
  - [ ] `GET /api/v1/me/settings should return settings`
  - [ ] `PATCH /api/v1/me/settings should update settings`

#### GREEN Phase
- [ ] `AuthController.kt` 완성 (현재 스켈레톤 → 실제 구현)
- [ ] `MeController.kt` 완성
- [ ] `UserService.kt` 구현 (프로필/설정 CRUD)

#### REFACTOR Phase
- [ ] 응답 DTO 정리
- [ ] Validation 메시지 한글화

**Quality Gate**:
```bash
./gradlew test --tests "*ControllerIntegrationTest*"
curl -X POST http://localhost:8080/api/v1/auth/signup -d '{"email":"test@test.com","username":"testuser","password":"Test1234"}'
```

- [ ] 모든 통합 테스트 통과
- [ ] Swagger UI에서 API 테스트 가능
- [ ] 빌드 성공

**Dependencies**: Phase 4 완료

---

### Phase 6: Streaming Context - Query Service & Controller (2 hours)

**Goal**: 스트리밍 이벤트 조회 API 구현

**Test Strategy**:
- Unit Tests: QueryService
- Integration Tests: Controller
- Coverage Target: 80%

**Tasks (TDD)**:

#### RED Phase
- [ ] `StreamingEventQueryServiceTest.kt` 작성
  - [ ] `should return paginated events`
  - [ ] `should filter by status`
  - [ ] `should filter by artist`
  - [ ] `should return live events ordered by viewer count`
- [ ] `StreamingEventControllerIntegrationTest.kt` 작성
  - [ ] `GET /api/v1/streaming-events should return list`
  - [ ] `GET /api/v1/streaming-events/{id} should return detail`
  - [ ] `GET /api/v1/streaming-events/live should return live events`

#### GREEN Phase
- [ ] `StreamingEventQueryServiceImpl.kt` 구현
- [ ] `StreamingEventController.kt` 완성
- [ ] `StreamingEventJpaRepository.kt` 페이지네이션 메서드 추가

#### REFACTOR Phase
- [ ] DTO 매핑 최적화
- [ ] N+1 쿼리 방지

**Quality Gate**:
```bash
./gradlew test --tests "*StreamingEvent*Test*"
```

- [ ] 모든 테스트 통과
- [ ] 페이지네이션 동작 확인
- [ ] 빌드 성공

**Dependencies**: Phase 5 완료 (인증 시스템)

---

### Phase 7: Content Context - Domain & Repository (3 hours)

**Goal**: News, Chart, Artist 도메인 모델 및 Repository 구현

**Test Strategy**:
- Unit Tests: 도메인 로직
- Integration Tests: Repository
- Coverage Target: 80%

**Tasks (TDD)**:

#### RED Phase
- [ ] `NewsTest.kt` 도메인 테스트
- [ ] `ChartEntryTest.kt` 도메인 테스트
- [ ] `ArtistTest.kt` 도메인 테스트
- [ ] `NewsRepositoryTest.kt` 통합 테스트
- [ ] `ChartRepositoryTest.kt` 통합 테스트
- [ ] `ArtistRepositoryTest.kt` 통합 테스트

#### GREEN Phase
- [ ] `News.kt` Entity (crawled_news 테이블 매핑)
- [ ] `ChartEntry.kt` Entity (crawled_charts 테이블 매핑)
- [ ] `Artist.kt` Entity (artists 테이블 매핑 - 기존 테이블)
- [ ] `NewsJpaRepository.kt`
- [ ] `ChartJpaRepository.kt`
- [ ] `ArtistJpaRepository.kt`

#### REFACTOR Phase
- [ ] 쿼리 최적화
- [ ] 인덱스 확인

**Quality Gate**:
```bash
./gradlew test --tests "*ContentContext*" --tests "*NewsRepository*" --tests "*ChartRepository*"
```

- [ ] 모든 테스트 통과
- [ ] DB 스키마 호환 확인
- [ ] 빌드 성공

**Dependencies**: Phase 6 완료

---

### Phase 8: Content Context - Services & Controllers (3 hours)

**Goal**: News, Chart, Artist 조회 API 구현

**Test Strategy**:
- Unit Tests: Service 로직
- Integration Tests: Controller
- Coverage Target: 80%

**Tasks (TDD)**:

#### RED Phase
- [ ] `NewsQueryServiceTest.kt`
- [ ] `ChartQueryServiceTest.kt`
- [ ] `ArtistQueryServiceTest.kt`
- [ ] `NewsControllerIntegrationTest.kt`
- [ ] `ChartControllerIntegrationTest.kt`
- [ ] `ArtistControllerIntegrationTest.kt`

#### GREEN Phase
- [ ] `NewsQueryService.kt` 인터페이스 + `NewsQueryServiceImpl.kt`
- [ ] `ChartQueryService.kt` 인터페이스 + `ChartQueryServiceImpl.kt`
- [ ] `ArtistQueryService.kt` 인터페이스 + `ArtistQueryServiceImpl.kt`
- [ ] `NewsController.kt`
- [ ] `ChartController.kt`
- [ ] `ArtistController.kt`
- [ ] Content DTOs (`NewsDto.kt`, `ChartDto.kt`, `ArtistDto.kt`)

#### REFACTOR Phase
- [ ] DTO 매핑 최적화
- [ ] 응답 포맷 통일

**Quality Gate**:
```bash
./gradlew test --tests "*Content*Test*"
./gradlew bootRun  # Swagger UI에서 전체 API 확인
```

- [ ] 모든 테스트 통과
- [ ] OpenAPI 스펙과 일치 확인
- [ ] 빌드 성공

**Dependencies**: Phase 7 완료

---

## Risk Assessment

| 리스크 | 확률 | 영향 | 완화 전략 |
|--------|------|------|----------|
| JWT 라이브러리 호환성 | Low | Medium | jjwt 0.12.x 사용, Spring Boot 3.2 호환 확인 |
| 크롤링 테이블 스키마 불일치 | Low | High | V7 마이그레이션 먼저 확인, Entity 매핑 테스트 |
| Google OAuth 통합 복잡성 | Medium | Medium | Phase 1에서 기본 구조만, 추후 별도 이슈로 분리 |
| 테스트 실행 시간 증가 | Medium | Low | TestContainers 캐싱, 병렬 실행 설정 |

---

## Rollback Strategy

| Phase | 롤백 방법 |
|-------|----------|
| Phase 1-5 (Identity) | 새로 생성된 파일 삭제, application.yml JWT 설정 제거 |
| Phase 6 (Streaming) | Controller/Service 파일 삭제 (도메인 유지) |
| Phase 7-8 (Content) | 새로 생성된 파일 삭제 (DB 테이블 유지) |

---

## Progress Tracking

### Completed Phases
- [ ] Phase 1: JWT Infrastructure
- [ ] Phase 2: Repository Adapters
- [ ] Phase 3: AuthService Implementation
- [ ] Phase 4: Security Configuration
- [ ] Phase 5: REST Controllers Integration
- [ ] Phase 6: Streaming Query Service
- [ ] Phase 7: Content Domain & Repository
- [ ] Phase 8: Content Services & Controllers

### Overall Progress
- Total Tasks: 80+
- Completed: 0
- In Progress: Phase 1
- Blocked: None

---

## Notes & Learnings

### Phase 1

(To be filled during implementation)

### Phase 2

(To be filled during implementation)

---

## References

- [OpenAPI Spec](/doc/api/openapi-v1.yaml)
- [DDD Context Map](/doc/ddd/context-map.md)
- [Identity Context Design](/doc/ddd/bounded-contexts/identity.md)
- [Content Context Design](/doc/ddd/bounded-contexts/content.md)
- [MVP API 명세서](/doc/mvp/mvp_API_명세서.md)
