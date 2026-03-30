---
name: verify-flyway-entity-sync
description: JPA @Entity ↔ Flyway 마이그레이션 동기화 검증. 엔티티 추가/수정 또는 마이그레이션 변경 후 사용.
---

# verify-flyway-entity-sync

## Purpose

JPA 엔티티와 Flyway SQL 마이그레이션 간 불일치를 탐지합니다:

1. **테이블 누락** — `@Table(name = "X")` 엔티티에 대응하는 `CREATE TABLE X`가 없는 경우
2. **컬럼 불일치** — 엔티티 필드와 마이그레이션 컬럼 정의 불일치
3. **마이그레이션 버전 충돌** — 동일 버전 번호의 중복 마이그레이션
4. **IF NOT EXISTS 패턴** — Django와 공유하는 테이블은 `CREATE TABLE IF NOT EXISTS` 필수

## When to Run

- `@Entity` 어노테이션이 있는 Kotlin 파일을 추가/수정한 후
- 새 Flyway 마이그레이션 파일을 추가한 후
- `docker-compose up`에서 Hibernate schema-validation 에러 발생 시
- PR 전 백엔드 스키마 정합성 검증 시

## Related Files

| File | Purpose |
|------|---------|
| `backend/src/main/kotlin/com/fanpulse/domain/content/News.kt` | News 엔티티 (@Table: news) |
| `backend/src/main/kotlin/com/fanpulse/domain/content/Artist.kt` | Artist 엔티티 (@Table: artists) |
| `backend/src/main/kotlin/com/fanpulse/domain/content/Chart.kt` | Chart + ChartEntry 엔티티 (@Table: charts, chart_entries) |
| `backend/src/main/kotlin/com/fanpulse/domain/identity/User.kt` | User 엔티티 (@Table: users) |
| `backend/src/main/kotlin/com/fanpulse/domain/identity/UserSettings.kt` | UserSettings 엔티티 (@Table: user_settings) |
| `backend/src/main/kotlin/com/fanpulse/domain/identity/OAuthAccount.kt` | OAuthAccount 엔티티 (@Table: oauth_accounts) |
| `backend/src/main/kotlin/com/fanpulse/domain/streaming/StreamingEvent.kt` | StreamingEvent 엔티티 (@Table: streaming_events) |
| `backend/src/main/kotlin/com/fanpulse/domain/discovery/ArtistChannel.kt` | ArtistChannel 엔티티 (@Table: artist_channels) |
| `backend/src/main/kotlin/com/fanpulse/infrastructure/persistence/identity/RefreshTokenJpaRepository.kt` | RefreshToken 엔티티 (@Table: refresh_tokens) |
| `backend/src/main/resources/db/migration/` | Flyway 마이그레이션 디렉토리 |

## Workflow

### Step 1: 모든 JPA 엔티티의 테이블명 수집

**도구:** Grep

```bash
# @Table(name = "X") 패턴에서 테이블명 추출
grep -rn '@Table' backend/src/main/kotlin/ --include='*.kt'
```

모든 `name = "..."` 값을 목록으로 수집합니다.

### Step 2: 모든 Flyway 마이그레이션의 CREATE TABLE 수집

**도구:** Grep

```bash
# CREATE TABLE [IF NOT EXISTS] X 에서 테이블명 추출
grep -rn 'CREATE TABLE' backend/src/main/resources/db/migration/ --include='*.sql'
```

### Step 3: 테이블 매핑 검증

**검사:** Step 1의 각 테이블명이 Step 2에 존재하는지 확인합니다.

| 엔티티 테이블 | 마이그레이션 | 상태 |
|------------|------------|------|
| users | V2 | PASS |
| artists | V2 | PASS |
| user_settings | V3 | PASS |
| oauth_accounts | V3 | PASS |
| streaming_events | V6 | PASS |
| artist_channels | V102 | PASS |
| refresh_tokens | V109 | PASS |
| charts | V115 | PASS |
| chart_entries | V115 | PASS |
| news | V117 | PASS |

**PASS 기준:** 모든 엔티티 테이블이 하나 이상의 마이그레이션에서 `CREATE TABLE`로 정의됨.
**FAIL 기준:** `@Table(name = "X")`가 있지만 어떤 마이그레이션에서도 `CREATE TABLE X`를 찾을 수 없음.

**수정:** 누락된 테이블에 대해 새 Flyway 마이그레이션 파일을 생성합니다. News.kt의 V117__create_news_table.sql을 참고.

### Step 4: 마이그레이션 버전 중복 검사

**도구:** Glob + Bash

```bash
# 마이그레이션 파일명에서 버전 번호만 추출하여 중복 확인
ls backend/src/main/resources/db/migration/V*.sql | sed 's/.*V\([0-9]*\)__.*/\1/' | sort -n | uniq -d
```

**PASS 기준:** 출력이 비어있음 (중복 없음).
**FAIL 기준:** 중복 버전 번호가 출력됨.

### Step 5: Django 공유 테이블 IF NOT EXISTS 검사

**도구:** Grep

Django AI sidecar와 공유하는 테이블(crawled_*)은 `IF NOT EXISTS`를 사용해야 합니다:

```bash
grep -n 'CREATE TABLE crawled_' backend/src/main/resources/db/migration/V7__create_content_tables.sql
```

**PASS 기준:** 모든 `crawled_*` 테이블이 `CREATE TABLE IF NOT EXISTS`를 사용.
**FAIL 기준:** `CREATE TABLE crawled_*` (IF NOT EXISTS 없이) 사용.

**수정:** `CREATE TABLE`을 `CREATE TABLE IF NOT EXISTS`로 변경.

## Output Format

```markdown
## Flyway-Entity Sync 검증 결과

| # | 검사 | 상태 | 상세 |
|---|------|------|------|
| 1 | 테이블 매핑 | PASS/FAIL | N개 엔티티, M개 누락 |
| 2 | 버전 중복 | PASS/FAIL | 중복 버전: [목록] |
| 3 | IF NOT EXISTS | PASS/FAIL | crawled_* 테이블 N개 확인 |

### 누락된 테이블 (있는 경우)
| 엔티티 | @Table | 필요한 조치 |
|--------|--------|------------|
| News.kt | news | V{N}__create_news_table.sql 생성 필요 |
```

## Exceptions

다음은 **위반이 아닙니다**:

1. **`crawled_*` 테이블** — 이 테이블들은 Django 모델에서도 정의되며, Spring의 JPA 엔티티가 아닙니다. Flyway 마이그레이션에는 존재하지만 `@Entity`가 없어도 정상입니다.
2. **`@Embeddable` 클래스** — `@Entity`가 아닌 `@Embeddable`은 독립 테이블이 필요하지 않습니다.
3. **`shedlock` 테이블** — 라이브러리가 관리하는 테이블로, 엔티티 없이 마이그레이션만 존재합니다.
4. **`flyway_schema_history`** — Flyway 내부 테이블로, 검증 대상이 아닙니다.
