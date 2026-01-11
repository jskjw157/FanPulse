# Implementation Plan: Live Discovery (yt-dlp) -> streaming_events

**Status**: 📝 Planned
**Started**: 2026-01-07
**Last Updated**: 2026-01-07
**Estimated Completion**: 2026-01-14

---

**⚠️ CRITICAL INSTRUCTIONS**: After completing each phase:
1. ✅ Check off completed task checkboxes
2. 🧪 Run all quality gate validation commands
3. ⚠️ Verify ALL quality gate items pass
4. 📅 Update "Last Updated" date above
5. 📝 Document learnings in Notes section
6. ➡️ Only then proceed to next phase

⛔ **DO NOT skip quality gates or proceed with failing checks**

---

## 📋 Overview

### Feature Description
YouTube 채널에서 yt-dlp로 라이브/과거 스트리밍 영상을 주기적으로 발견하고,
`streaming_events`에 upsert하여 H006(라이브 목록) / H019(라이브 상세) 화면에
외부 임베드 기반 스트리밍 정보를 제공한다. Weverse는 이번 범위에서 제외한다.

### Success Criteria
- [ ] `artist_channels` 테이블로 아티스트-채널 매핑을 관리한다.
- [ ] `streaming_events`에 `platform`, `external_id`, `source_url` 컬럼과
      `(platform, external_id)` 유니크 인덱스를 추가한다.
- [ ] 1시간 주기로 발견 작업이 실행되고 중복 없이 upsert된다.
- [ ] `GET /live` 응답에서 신규 이벤트가 조회된다.

### User Impact
팬들이 라이브/과거 스트리밍 정보를 안정적으로 확인할 수 있으며,
외부 플랫폼 임베드를 통해 앱 내 시청 경험이 개선된다.

---

## 🏗️ Architecture Decisions

| Decision | Rationale | Trade-offs |
|----------|-----------|------------|
| yt-dlp 기반 발견(YouTube만) | API 키 없이 영상 메타데이터 확보 | yt-dlp/웹 구조 변경에 취약 |
| `artist_channels` 테이블 추가 | 채널 매핑을 DB로 일원화 | 마이그레이션 필요 |
| `streaming_events`에 `platform/external_id/source_url` 추가 | 안정적 중복 제거 | 기존 스키마 변경 |
| `(platform, external_id)` 유니크 | 플랫폼 확장 대비 | 기존 데이터 정리 필요 |
| Weverse 제외 | 법적/약관 리스크 최소화 | 플랫폼 범위 축소 |

---

## 📦 Dependencies

### Required Before Starting
- [ ] DB 마이그레이션 전략 확정 (DDL + 롤백)
- [ ] `artist_channels` 초기 seed 데이터 확보

### External Dependencies
- yt-dlp (Python 실행 환경 포함)

---

## 🧪 Test Strategy

### Testing Approach
**TDD Principle**: Write tests FIRST, then implement to make them pass

### Test Pyramid for This Feature
| Test Type | Coverage Target | Purpose |
|-----------|-----------------|---------|
| **Unit Tests** | ≥80% | 매핑/파싱/URL 생성 |
| **Integration Tests** | Critical paths | DB upsert, 스케줄러 실행 |
| **E2E Tests** | 1 critical flow | `GET /live` 조회 흐름 |

### Test File Organization
```
backend/src/test/kotlin/
├── com/fanpulse/domain/streaming/
│   └── StreamingEventMappingTest.kt
├── com/fanpulse/infrastructure/external/
│   └── YtDlpParserTest.kt
└── com/fanpulse/integration/
    └── LiveDiscoveryIntegrationTest.kt
```

---

## 🚀 Implementation Phases

### Phase 1: Schema + Mapping Foundation
**Goal**: 스키마 확장과 매핑 규칙 확정
**Estimated Time**: 3 hours
**Status**: ⏳ Pending

#### Tasks

**🔴 RED: Write Failing Tests First**
- [ ] **Test 1.1**: `platform/external_id` 기반 중복 규칙 테스트
  - File(s): `backend/src/test/kotlin/com/fanpulse/domain/streaming/StreamingEventMappingTest.kt`
  - Expected: Tests FAIL (red)

**🟢 GREEN: Implement to Make Tests Pass**
- [ ] **Task 1.2**: DB 마이그레이션 설계
  - `artist_channels` 테이블 추가
  - `streaming_events` 컬럼 추가 + 유니크 인덱스
- [ ] **Task 1.3**: 매핑 규칙(플랫폼/외부 ID/임베드 URL) 정의

**🔵 REFACTOR: Clean Up Code**
- [ ] **Task 1.4**: 매핑 유틸/VO 정리 및 네이밍 개선

#### Quality Gate ✋
- [ ] 마이그레이션 설계 문서 검증
- [ ] 매핑 테스트 통과

---

### Phase 2: yt-dlp Discovery Adapter
**Goal**: yt-dlp 결과 파싱 및 후보 리스트 생성
**Estimated Time**: 3 hours
**Status**: ⏳ Pending

#### Tasks

**🔴 RED: Write Failing Tests First**
- [ ] **Test 2.1**: yt-dlp JSON 파싱 테스트
  - File(s): `backend/src/test/kotlin/com/fanpulse/infrastructure/external/YtDlpParserTest.kt`
  - Expected: Tests FAIL (red)

**🟢 GREEN: Implement to Make Tests Pass**
- [ ] **Task 2.2**: yt-dlp 실행 어댑터 구현
- [ ] **Task 2.3**: `platform/external_id/source_url` 매핑 반영

**🔵 REFACTOR: Clean Up Code**
- [ ] **Task 2.4**: 파서/DTO 분리 및 예외 처리 정리

#### Quality Gate ✋
- [ ] 파서 테스트 통과
- [ ] yt-dlp 실행 실패 시 복구 전략 확인

---

### Phase 3: Upsert Service + Repository Path
**Goal**: 발견된 이벤트를 `streaming_events`에 upsert
**Estimated Time**: 4 hours
**Status**: ⏳ Pending

#### Tasks

**🔴 RED: Write Failing Tests First**
- [ ] **Test 3.1**: upsert 동작 통합 테스트
  - File(s): `backend/src/test/kotlin/com/fanpulse/integration/LiveDiscoveryIntegrationTest.kt`
  - Expected: Tests FAIL (red)

**🟢 GREEN: Implement to Make Tests Pass**
- [ ] **Task 3.2**: `StreamingDiscoveryService` 구현
- [ ] **Task 3.3**: `(platform, external_id)` 기준 upsert 로직 구현

**🔵 REFACTOR: Clean Up Code**
- [ ] **Task 3.4**: 도메인 서비스/포트 경계 정리

#### Quality Gate ✋
- [ ] 통합 테스트 통과
- [ ] 기존 `GET /live` 응답 스키마 유지 확인

---

### Phase 4: Scheduler + Operational Readiness
**Goal**: 1시간 주기 실행과 운영 준비
**Estimated Time**: 2 hours
**Status**: ⏳ Pending

#### Tasks

**🔴 RED: Write Failing Tests First**
- [ ] **Test 4.1**: 스케줄러 실행 트리거 테스트

**🟢 GREEN: Implement to Make Tests Pass**
- [ ] **Task 4.2**: `LiveDiscoveryScheduler` 추가 (1시간 크론)
- [ ] **Task 4.3**: 로그/메트릭 최소화 추가

**🔵 REFACTOR: Clean Up Code**
- [ ] **Task 4.4**: 설정 키 정리 및 문서화

#### Quality Gate ✋
- [ ] 스케줄러 수동 실행 검증
- [ ] 장애 시 재시도/로그 확인

---

## ⚠️ Risk Assessment

| Risk | Probability | Impact | Mitigation Strategy |
|------|-------------|--------|---------------------|
| yt-dlp 구조 변경 | Med | Med | 파서 테스트/샘플 고정, 롤백 플랜 |
| DB 마이그레이션 실패 | Low | High | 단계별 마이그레이션 + 롤백 스크립트 |
| 채널 매핑 누락 | Med | Med | seed 검증 + 모니터링 로그 |
| 법적/약관 리스크 | Low | High | Weverse 제외, YouTube 범위 고정 |

---

## 🔄 Rollback Strategy

### If Phase 1 Fails
- 스키마 변경 롤백 (DDL revert)
- 스케줄러 비활성화 설정 유지

### If Phase 2-3 Fails
- 신규 서비스/어댑터 제거
- 기존 `streaming_events` 데이터 보존

### If Phase 4 Fails
- 스케줄러 비활성화
- 수동 실행 스크립트로 대체

---

## 📊 Progress Tracking

### Completion Status
- **Phase 1**: ⏳ 0%
- **Phase 2**: ⏳ 0%
- **Phase 3**: ⏳ 0%
- **Phase 4**: ⏳ 0%

**Overall Progress**: 0% complete

---

## 📝 Notes & Learnings

### Implementation Notes
- Weverse 제외 확정
- `(platform, external_id)` 유니크 기준 확정

---

## 📚 References

### Documentation
- `doc/프로젝트_기획서.md`
- `doc/화면_정의서.md`
- `doc/데이터베이스_정의서.md`
- `doc/크롤링.md`
- `doc/mvp/mvp_API_명세서.md`
- `script/live_concert_collector.py`

### Related Issues
- Issue #160: Live discovery (yt-dlp 기반)

