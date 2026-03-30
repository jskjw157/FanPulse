# Plan: Issue 135 - Unified Search API (Live + News)

## Context

### Original Request
- GitHub issue: https://github.com/jskjw157/FanPulse/issues/135
- Branch: feature/135-backend-검색-api-구현
- Goal: Implement unified search endpoint for Live (streaming events) and News.

### Key References (existing repo)
- Product/API spec: `doc/mvp/mvp_API_명세서.md` (Search Context section)
- Similar controller patterns:
  - `backend/src/main/kotlin/com/fanpulse/interfaces/rest/content/NewsController.kt`
  - `backend/src/main/kotlin/com/fanpulse/infrastructure/web/streaming/StreamingEventController.kt`
- Similar query-service patterns:
  - `backend/src/main/kotlin/com/fanpulse/application/service/content/NewsQueryServiceImpl.kt`
  - `backend/src/main/kotlin/com/fanpulse/application/service/streaming/StreamingEventQueryServiceImpl.kt`
- Persistence patterns:
  - `backend/src/main/kotlin/com/fanpulse/infrastructure/persistence/content/NewsJpaRepository.kt`
  - `backend/src/main/kotlin/com/fanpulse/infrastructure/persistence/streaming/StreamingEventJpaRepository.kt`
- Domain entities:
  - `backend/src/main/kotlin/com/fanpulse/domain/content/Artist.kt`
  - `backend/src/main/kotlin/com/fanpulse/domain/content/News.kt`
  - `backend/src/main/kotlin/com/fanpulse/domain/streaming/StreamingEvent.kt`

## Decisions (confirmed)
- Response style: follow existing backend style (no `{success,data}` wrapper). Return DTO directly.
- Endpoint prefix: follow existing controllers => `/api/v1/search`.
- Default per-category limit: 10.
- Live status scope: include `SCHEDULED`, `LIVE`, and `ENDED`.
- News source field name: use `sourceName` (align with existing `NewsSummary` / `NewsResponse`).
- Auth: make `/api/v1/search/**` public (add to `SecurityConfig` permitAll).
- Sorting:
  - Live: status group order `LIVE` -> `SCHEDULED` -> `ENDED`, then within group:
    - `LIVE`: `viewerCount desc`
    - `SCHEDULED`: `scheduledAt asc`
    - `ENDED`: `endedAt desc`
  - News: `publishedAt desc`
- Validation (q): if `q.trim().length < 2`, return 400 ProblemDetail with `errorCode=INVALID_REQUEST` (via `IllegalArgumentException`).
- News summary: generate from `content` (strip newlines), first 120 chars, append `...` when truncated.

## Scope Boundaries

### IN
- `GET /api/v1/search?q=...&limit=...`
- Live search target:
  - StreamingEvent title
  - Artist name
- News search target:
  - News title
  - News content/body
- Return:
  - Up to `limit` items per category
  - `totalCount` per category

### OUT (explicitly not in this issue)
- Search history (`search_history`) persistence
- Popular keywords
- Elasticsearch indexing/search context implementation
- Pagination beyond `limit` (no `page` param)
- Artists/community/posts/concerts unified search

## Proposed API Contract

### Endpoint
`GET /api/v1/search?q={string}&limit={int}`

### Query Params
- `q` (required): min length 2
- `limit` (optional): default 10, clamp to range 1..10

### Response 200 (DTO only)
```json
{
  "live": {
    "items": [
      {
        "id": "...",
        "title": "...",
        "artistId": "...",
        "artistName": "...",
        "thumbnailUrl": "...",
        "status": "SCHEDULED",
        "scheduledAt": "2025-01-20T14:00:00Z"
      }
    ],
    "totalCount": 0
  },
  "news": {
    "items": [
      {
        "id": "...",
        "title": "...",
        "summary": "...",
        "sourceName": "...",
        "publishedAt": "2025-01-14T10:30:00Z"
      }
    ],
    "totalCount": 0
  }
}
```

### Error Handling
- `q.length < 2` => 400 ProblemDetail
  - Confirmed: `IllegalArgumentException` -> current handler maps to `ErrorType.INVALID_REQUEST`
  - Note: Spec doc uses `VALIDATION_ERROR`, but backend currently standardizes on `INVALID_REQUEST` / `VALIDATION_FAILED`

## Architecture / Implementation Approach

### High-level approach
- Implement a dedicated Search controller + query service.
- Reuse existing domain ports and persistence adapters where possible.
- MVP search implementation uses Postgres LIKE queries (case-insensitive), not Elasticsearch.

### Suggested new code structure
Create:
- `backend/src/main/kotlin/com/fanpulse/interfaces/rest/search/SearchController.kt`
- `backend/src/main/kotlin/com/fanpulse/application/service/search/SearchQueryService.kt`
- `backend/src/main/kotlin/com/fanpulse/application/service/search/SearchQueryServiceImpl.kt`
- `backend/src/main/kotlin/com/fanpulse/application/dto/search/SearchDtos.kt`

Modify:
- Public access:
  - `backend/src/main/kotlin/com/fanpulse/infrastructure/security/SecurityConfig.kt`
    - Add `/api/v1/search/**` to permitAll list
- News search (title + content):
  - `backend/src/main/kotlin/com/fanpulse/domain/content/port/NewsPort.kt`
  - `backend/src/main/kotlin/com/fanpulse/infrastructure/persistence/content/NewsJpaRepository.kt`
  - `backend/src/main/kotlin/com/fanpulse/infrastructure/persistence/content/NewsAdapter.kt`
- Live search (title + artist name):
  - `backend/src/main/kotlin/com/fanpulse/domain/streaming/port/StreamingEventPort.kt`
  - `backend/src/main/kotlin/com/fanpulse/infrastructure/persistence/streaming/StreamingEventJpaRepository.kt`
  - `backend/src/main/kotlin/com/fanpulse/infrastructure/persistence/streaming/StreamingEventAdapter.kt`

## Query Design Notes

### News
- Keep existing `/api/v1/news/search` behavior stable if desired.
  - Prefer adding a new repository/port method for integrated search:
    - `searchByTitleOrContent(query, pageRequest)`
- Filtering:
  - Always include `visible = true`.
- Matching:
- Matching (case-insensitive):
  - `LOWER(title) LIKE ... OR LOWER(content) LIKE ...`.

### News summary mapping
- `summary`: derived from `News.content` (strip newlines, take first 120 chars, append `...` if truncated)

### Live (StreamingEvent)
- Status filter: include `SCHEDULED`, `LIVE`, and `ENDED` (confirmed).
- Matching:
  - Title LIKE OR artist name LIKE.
  - Requires referencing Artist for matching (join or subquery).

### Live ordering
- Confirmed ordering:
  - Status group order `LIVE` -> `SCHEDULED` -> `ENDED`
  - Within group sort:
    - `LIVE`: `viewerCount desc`
    - `SCHEDULED`: `scheduledAt asc`
    - `ENDED`: `endedAt desc`

### artistName in response
- Simplest (acceptable for `limit <= 10`): fetch Artist names per event via `ArtistPort.findById`.
- If you want to avoid N+1 even for 10:
  - Add batch lookup capability to `ArtistPort` (e.g., `findByIds`) or
  - Use a projection query that returns `artistName` together with event fields.

## Test Strategy

### Existing test infra
- JUnit 5 + Mockk + WebMvcTest
- Integration tests exist (SpringBootTest)
- Command: `cd backend && ./gradlew test`

### Add tests for Search
1) Controller mapping tests (WebMvcTest)
- File: `backend/src/test/kotlin/com/fanpulse/interfaces/rest/search/SearchControllerTest.kt`
- Verify:
  - 200 and response JSON structure when service returns data
  - 400 when `q` is shorter than 2 (if validated at controller)

2) Service unit tests (Mockk)
- File: `backend/src/test/kotlin/com/fanpulse/application/service/search/SearchQueryServiceImplTest.kt`
- Verify:
  - `limit` defaulting/clamping logic
  - Live/news queries called with expected params
  - Mapping (summary, artistName resolution)

Optional (if query correctness must be locked):
- DataJpaTest for the new JPA repository queries.

## Work Breakdown (TODOs)

- [ ] 1. Confirm/implement Search DTO contract (no wrapper, two categories)
- [ ] 2. Implement news integrated-search query (title + content) via NewsPort/Adapter/Repository
- [ ] 3. Implement live integrated-search query (title + artist name) via StreamingEventPort/Adapter/Repository
- [ ] 4. Implement SearchQueryServiceImpl to orchestrate and assemble response
- [ ] 5. Implement SearchController (`GET /api/v1/search`) with validation + limit clamping
- [ ] 6. Update `SecurityConfig` to permitAll for `/api/v1/search/**`
- [ ] 7. Add tests (controller + service); run `./gradlew test`
- [ ] 8. Smoke test locally with curl after `./gradlew bootRun`

## Definition of Done
- `GET /api/v1/search` returns live/news results with per-category `items` and `totalCount`
- `q` is required and enforced (min 2 chars) with 400 ProblemDetail
- Default limit=10; limit is clamped to 1..10
- Full backend test suite passes: `cd backend && ./gradlew test`
