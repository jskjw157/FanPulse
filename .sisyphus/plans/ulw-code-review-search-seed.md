# ULTRAWORK Code Review: Search API (#135) + Seed Loader (#136)

## Scope

- Reviewed backend unified search implementation (issue #135) at commit `69ca184`.
- Reviewed current working tree changes related to seed loading for search verification (issue #136):
  - `backend/src/main/kotlin/com/fanpulse/infrastructure/seed/**`
  - `backend/seed/**`
  - `backend/src/main/kotlin/com/fanpulse/domain/content/port/NewsPort.kt`
  - `backend/src/main/kotlin/com/fanpulse/infrastructure/persistence/content/NewsAdapter.kt`
  - `backend/src/main/kotlin/com/fanpulse/infrastructure/persistence/content/NewsJpaRepository.kt`

## Evidence Gathered

- Code inspection:
  - `backend/src/main/kotlin/com/fanpulse/application/service/search/SearchQueryServiceImpl.kt`
  - `backend/src/main/kotlin/com/fanpulse/interfaces/rest/search/SearchController.kt`
  - `backend/src/main/kotlin/com/fanpulse/infrastructure/persistence/streaming/StreamingEventJpaRepository.kt`
  - `backend/src/main/kotlin/com/fanpulse/infrastructure/persistence/content/NewsJpaRepository.kt`
  - `backend/src/main/kotlin/com/fanpulse/infrastructure/seed/SeedLoaderRunner.kt`
- Runtime/schema inspection (read-only):
  - `\d artists`, `\d news`, `\d streaming_events` on local Postgres.

## Executive Verdict

- Search API behavior is correct for MVP and verified with tests + local smoke.
- Largest risk is not the search logic itself, but DB schema management: current Postgres schema contains tables/columns/types not represented in Flyway migrations.
  - This can break new environments where DB is created from migrations only (since app uses `ddl-auto: validate`).

## Findings

### Critical

1) Flyway migration drift vs JPA schema expectations

- Observation:
  - `backend/src/main/resources/db/migration` does NOT define the `news` table.
  - Migrations define `artists.debut_date` as `DATE`, but actual DB has `timestamp with time zone`.
  - Migrations do NOT add `artists.english_name`, but actual DB has it.
- Why this matters:
  - `backend/src/main/resources/application.yml` sets `spring.jpa.hibernate.ddl-auto: validate`.
  - A clean DB created from migrations would likely fail JPA validation and the app would not boot.
- Impact on #135:
  - Search code references `Artist.englishName` in JPQL (`StreamingEventJpaRepository.searchByTitleOrArtistName`) and assumes `news` table exists for `NewsJpaRepository`.
- Recommendation:
  - Create missing Flyway migrations to fully describe the canonical schema (news table, artists english_name, debut_date type, and any other drift).
  - Treat as a separate hardening issue if out-of-scope for #135/#136.

### High

2) Potential N+1 query in unified search

- Where: `backend/src/main/kotlin/com/fanpulse/application/service/search/SearchQueryServiceImpl.kt`
- Behavior:
  - For each returned streaming event, `artistPort.findById(event.artistId)` is called.
  - Worst case per request: 3 event queries (LIVE/SCHEDULED/ENDED) + 1 news query + up to `limit` artist lookups.
- Impact:
  - MVP acceptable at `limit<=10`, but it scales poorly and adds DB round-trips.
- Recommendation:
  - Add a batch lookup to `ArtistPort` (e.g., `findByIds`) or change the streaming search query to return `artistName` via projection.

3) Seed loader safety: `exitProcess(0)` + always-on component

- Where: `backend/src/main/kotlin/com/fanpulse/infrastructure/seed/SeedLoaderRunner.kt`
- Behavior:
  - Runner is always part of the application context.
  - If `fanpulse.seed.enabled=true` is ever enabled in a non-seed runtime, it will terminate the JVM.
- Recommendation:
  - Gate runner with `@ConditionalOnProperty("fanpulse.seed.enabled", havingValue = "true")`.
  - Consider a dedicated Spring profile (e.g., `seed`) and disable in production configs.
  - Prefer graceful shutdown (`SpringApplication.exit(ctx)`) over `exitProcess`.

### Medium

4) Streaming search query uses cross join style

- Where: `backend/src/main/kotlin/com/fanpulse/infrastructure/persistence/streaming/StreamingEventJpaRepository.kt`
- Observation:
  - Uses `FROM StreamingEvent e, Artist a` with join predicate in WHERE.
- Recommendation:
  - Prefer explicit join for readability and query planner hints (`JOIN ... ON ...`).
  - Add indexes for text search if query volume increases (or switch to full-text search / ES later).

5) Logging contains raw query string

- Where: `SearchController` / `SearchQueryServiceImpl`
- Impact:
  - Search terms can contain user-entered strings; generally ok, but avoid logging PII in higher environments.
- Recommendation:
  - Keep debug-level only (current), or log length/hash instead.

### Low

6) Minor cleanup in seed loader

- `SeedLoaderRunner.kt` has an unused import (`PageRequest`).

## Recommended Follow-up Work Plan (No implementation here)

1) Schema alignment (highest priority)
- Create Flyway migrations to represent actual production schema for:
  - `news` table (create + constraints)
  - `artists.english_name` column
  - `artists.debut_date` column type
  - any other drift discovered in `\d` outputs
- Add CI check that a fresh DB created from migrations boots with `ddl-auto=validate`.

2) Search performance hardening
- Add batch artist lookup or projection join.
- Re-check query plans for LIKE queries (optional).

3) Seed loader hardening
- Make seed runner opt-in (`@ConditionalOnProperty` + profile).
- Decide whether seed files live under `backend/seed/` or `backend/src/main/resources/seed/`.
- Decide whether seed runner should be a separate executable/main class.
