# Feature Plan: PostgreSQL Schema Migration with Flyway

## Issue Reference
- **Issue**: #19 - [Backend] PostgreSQL Schema Migration
- **Branch**: `master` (Sprint 1)
- **Priority**: high-priority
- **Sprint**: Sprint 1: Skeleton + Contract
- **Labels**: feature, backend, mvp, epic/infra

---

## 1. Overview

### 1.1 Problem Statement
현재 FanPulse 백엔드는 JPA의 `ddl-auto: validate` 설정을 사용하고 있어, 데이터베이스 스키마가 수동으로 관리되어야 합니다. 이는 다음 문제를 야기합니다:
- 스키마 버전 관리 부재
- 팀원 간 스키마 동기화 어려움
- 운영 환경 배포 시 수동 작업 필요
- 롤백 불가능

### 1.2 Solution
Flyway를 도입하여 데이터베이스 스키마를 코드로 관리하고, 버전 관리 및 자동 마이그레이션을 구현합니다.

### 1.3 Why Flyway?
| 도구 | 장점 | 단점 |
|------|------|------|
| **Flyway (권장)** | Spring Boot 네이티브 지원, SQL 기반, 간단한 설정, 널리 사용됨 | Undo 기능은 Teams/Enterprise 필요 |
| Liquibase | XML/YAML 지원, 롤백 기본 제공 | 설정 복잡, 학습 곡선 |
| JPA ddl-auto | 간단한 설정 | 운영 환경 부적합, 버전 관리 불가 |

---

## 2. Scope

### 2.1 In Scope
- [x] Flyway 의존성 추가 및 설정
- [x] 테이블 의존성 분석 및 마이그레이션 순서 결정
- [x] PostgreSQL 스키마 마이그레이션 스크립트 작성 (32개 테이블)
- [x] 인덱스 및 외래키 제약조건 설정
- [x] 마스터 데이터 시딩 (FAQ, Rewards)
- [x] 테스트 환경 구성 (Local PostgreSQL)
- [x] 마이그레이션 테스트 작성

### 2.2 Out of Scope (Next Phase)
- [ ] MongoDB 컬렉션 마이그레이션 (posts, comments)
- [ ] 결제 관련 테이블 (PG사 선정 후)
- [ ] Flyway Undo 스크립트 (Teams 에디션)
- [ ] 다중 데이터소스 마이그레이션

---

## 3. Technical Design

### 3.1 Architecture

```
+-------------------+     +-------------------+     +-------------------+
|   Application     | --> |   Flyway          | --> |   PostgreSQL      |
|   Startup         |     |   Migration       |     |   Database        |
+-------------------+     +-------------------+     +-------------------+
                                    |
                                    v
                          +-------------------+
                          | db/migration/     |
                          | V1__create_extension.sql |
                          | V2__create_core_tables.sql |
                          | ...               |
                          +-------------------+
```

### 3.2 Database Tables (32 Tables)

#### Core Tables
| 테이블 | Context | 설명 |
|--------|---------|------|
| users | Identity | 사용자 정보 |
| auth_tokens | Identity | 인증 토큰 |
| oauth_accounts | Identity | OAuth 연동 |
| user_settings | Identity | 사용자 설정 |

#### Voting Tables
| 테이블 | Context | 설명 |
|--------|---------|------|
| polls | Voting | 투표 |
| vote_options | Voting | 투표 옵션 |
| votes | Voting | 투표 기록 |
| voting_power | Voting | 투표권 |

#### Reward & Membership Tables
| 테이블 | Context | 설명 |
|--------|---------|------|
| points | Reward | 포인트 잔액 |
| point_transactions | Reward | 포인트 거래 |
| rewards | Reward | 교환 상품 |
| memberships | Membership | VIP 멤버십 |
| user_daily_missions | Reward | 일일 미션 |

#### Streaming Tables
| 테이블 | Context | 설명 |
|--------|---------|------|
| streaming_events | Streaming | 라이브 이벤트 |
| chat_messages | Streaming | 채팅 메시지 |
| live_hearts | Streaming | 하트 |

#### Content Tables
| 테이블 | Context | 설명 |
|--------|---------|------|
| artists | Content | 아티스트 |
| crawled_news | Content | 크롤링 뉴스 |
| crawled_charts | Content | 차트 순위 |
| crawled_charts_history | Content | 차트 히스토리 |
| crawled_concerts | Content | 콘서트 정보 |
| crawled_ads | Content | 광고 상품 |

#### Social & Support Tables
| 테이블 | Context | 설명 |
|--------|---------|------|
| notifications | Notification | 알림 |
| media | Common | 미디어 |
| likes | Community | 좋아요 |
| user_favorites | Community | 팔로우 |
| saved_posts | Community | 저장 게시물 |
| ticket_reservations | Concert | 예매 |
| support_tickets | Support | 1:1 문의 |
| faq | Support | FAQ |
| notices | Support | 공지사항 |
| search_history | Search | 검색 기록 |

### 3.3 Migration File Structure

```
backend/src/main/resources/db/migration/
  V1__create_extension.sql           # uuid-ossp 확장
  V2__create_core_tables.sql         # users, artists, polls, rewards
  V3__create_identity_tables.sql     # auth_tokens, oauth_accounts, user_settings
  V4__create_voting_tables.sql       # vote_options, votes, voting_power
  V5__create_reward_tables.sql       # points, point_transactions, memberships, user_daily_missions
  V6__create_streaming_tables.sql    # streaming_events, chat_messages, live_hearts
  V7__create_content_tables.sql      # crawled_news, crawled_charts, crawled_concerts, crawled_ads
  V8__create_social_tables.sql       # notifications, media, likes, user_favorites, saved_posts
  V9__create_support_tables.sql      # support_tickets, faq, notices, search_history
  V10__create_reservation_tables.sql # ticket_reservations
  V11__create_indexes.sql            # 성능 최적화 인덱스
  V100__seed_faq.sql                 # FAQ 시딩
  V101__seed_rewards.sql             # 리워드 시딩
```

### 3.4 Key Components

#### 3.4.1 Flyway Configuration
```yaml
# application.yml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    baseline-version: 0
    locations: classpath:db/migration
    validate-on-migrate: true
    out-of-order: false
    clean-disabled: true  # 운영 환경 보호
```

#### 3.4.2 Gradle Dependency
```kotlin
// build.gradle.kts
implementation("org.flywaydb:flyway-core")
implementation("org.flywaydb:flyway-database-postgresql")
```

---

## 4. Implementation Plan

### Phase 1: Setup (Day 1)
| Task | Priority | Estimate |
|------|----------|----------|
| Flyway 의존성 추가 | P0 | 0.5h |
| application.yml 설정 | P0 | 0.5h |
| 테스트 환경 설정 (Local PostgreSQL) | P0 | 1h |

### Phase 2: Core Migration (Day 1-2)
| Task | Priority | Estimate |
|------|----------|----------|
| V1: UUID 확장 | P0 | 0.5h |
| V2: Core Tables (users, artists, polls, rewards) | P0 | 1h |
| V3: Identity Tables | P0 | 1h |
| V4: Voting Tables | P0 | 1h |

### Phase 3: Domain Migration (Day 2-3)
| Task | Priority | Estimate |
|------|----------|----------|
| V5: Reward Tables | P0 | 1h |
| V6: Streaming Tables | P0 | 1h |
| V7: Content Tables | P0 | 1h |
| V8: Social Tables | P0 | 1h |
| V9: Support Tables | P0 | 1h |
| V10: Reservation Tables | P0 | 0.5h |

### Phase 4: Optimization & Seeding (Day 3-4)
| Task | Priority | Estimate |
|------|----------|----------|
| V11: 인덱스 생성 | P0 | 1h |
| V100-V101: 시딩 스크립트 | P1 | 1h |
| 통합 테스트 작성 | P0 | 2h |

### Phase 5: Verification (Day 4)
| Task | Priority | Estimate |
|------|----------|----------|
| 전체 마이그레이션 테스트 | P0 | 1h |
| JPA Entity 정합성 검증 | P0 | 1h |
| 문서화 | P1 | 1h |

**Total Estimate**: 16 hours (4 days)

---

## 5. Database Schema Details

### 5.1 users Table
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
```

### 5.2 votes Table (복합 외래키 예시)
```sql
CREATE TABLE votes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    poll_id UUID NOT NULL REFERENCES polls(id) ON DELETE CASCADE,
    vote_option_id UUID NOT NULL REFERENCES vote_options(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_votes_user_poll UNIQUE (user_id, poll_id)
);

CREATE INDEX idx_votes_poll_created ON votes(poll_id, created_at DESC);
```

### 5.3 Foreign Key Strategy
| 관계 유형 | ON DELETE | ON UPDATE | 예시 |
|----------|-----------|-----------|------|
| 강한 의존 | CASCADE | CASCADE | votes -> users |
| 약한 의존 | SET NULL | CASCADE | streaming_events -> artists |
| 참조만 | RESTRICT | CASCADE | ticket_reservations -> crawled_concerts |

---

## 6. Testing Strategy

### 6.1 Unit Tests
- 각 마이그레이션 스크립트 문법 검증

### 6.2 Integration Tests
```kotlin
@SpringBootTest
@ActiveProfiles("integration-test")
class FlywayMigrationTest {

    @Test
    fun `should run all migrations successfully`() {
        // Given: 로컬 PostgreSQL (application-integration-test.yml)
        // When: Application starts
        // Then: All migrations applied
        val result = flyway.migrate()
        assertThat(result.migrationsExecuted).isGreaterThan(0)
        assertThat(result.success).isTrue()
    }

    @Test
    fun `should validate schema with JPA entities`() {
        // JPA validate 모드에서 예외 없이 시작되어야 함
    }
}
```

### 6.3 Data Integrity Tests
- FK 제약조건 테스트
- UNIQUE 제약조건 테스트
- NOT NULL 제약조건 테스트

---

## 7. Risks & Mitigations

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| 마이그레이션 순서 오류 | High | Medium | 테스트 환경에서 충분한 검증 |
| FK 의존성 문제 | High | Medium | 의존성 그래프 분석 철저히 |
| 운영 환경 데이터 손실 | Critical | Low | clean-disabled: true 설정 |
| JPA Entity 불일치 | Medium | Medium | validate 모드로 검증 |

---

## 8. Rollback Strategy

### 8.1 Flyway Community Edition
- Undo 기능 없음
- 수동 롤백 스크립트 별도 관리
- `V1_1__rollback_xxx.sql` 형태로 작성

### 8.2 Emergency Rollback
```sql
-- 각 마이그레이션에 대응하는 롤백 스크립트 예시
-- rollback/U2__undo_core_tables.sql
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS artists CASCADE;
DROP TABLE IF EXISTS polls CASCADE;
DROP TABLE IF EXISTS rewards CASCADE;
```

---

## 9. Acceptance Criteria

- [x] Flyway 설정이 완료되어 애플리케이션 시작 시 자동 마이그레이션 실행
- [x] 32개 테이블이 정의서에 맞게 생성됨
- [x] 모든 FK 제약조건이 올바르게 설정됨
- [x] 필수 인덱스가 생성됨
- [x] FAQ, Rewards 마스터 데이터가 시딩됨
- [x] 로컬 PostgreSQL 기반 통합 테스트 통과
- [x] JPA validate 모드에서 애플리케이션 정상 시작

---

## 10. Documentation

### 10.1 Files to Create/Update
- [ ] `backend/src/main/resources/db/migration/` - 마이그레이션 스크립트
- [ ] `backend/build.gradle.kts` - Flyway 의존성
- [ ] `backend/src/main/resources/application.yml` - Flyway 설정
- [ ] `doc/데이터베이스_정의서.md` - 변경 이력 업데이트

### 10.2 References
- [Flyway Documentation](https://documentation.red-gate.com/fd)
- [Spring Boot + Flyway](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.migration-tool.flyway)
- [PostgreSQL 14 Documentation](https://www.postgresql.org/docs/14/)

---

## 변경 이력

| 버전 | 날짜 | 변경 내용 | 작성자 |
|------|------|----------|--------|
| 1.0.0 | 2026-01-07 | 최초 작성 | Claude |
