# Comment review recovery (#300)

## Operations

- Apply Flyway `V20261106.300.1` before starting the updated backend. It adds retry/version columns and an append-only decision trail; no existing comment is made public by the migration.
- Automatic recovery is enabled by default: `COMMENT_REVIEW_ENABLED=true`, `COMMENT_REVIEW_INTERVAL_MS=60000`. Disable the scheduler to pause automatic review; operator decisions remain available.
- Provision trusted operator UUIDs through deployment configuration `COMMENT_REVIEW_ADMIN_USER_IDS` (comma-separated existing user UUIDs). The default is empty and denies every operator request. A normal signed access token is still required. User-submitted roles, headers and refresh tokens do not grant access. Do not expose this environment variable as a public web setting.
- Operator UI: `/admin/comment-reviews`. The backend enforces permissions on list, history and decision endpoints, regardless of how the page is reached.
- Each recovery pass selects at most 20 due PENDING comments. Short row-locked transactions claim attempts with a five-minute lease. AI calls occur outside database transactions. Fallback/noop or thrown AI failures keep comments private and back off 1, 2, 4, then 8 minutes. After five claimed attempts (including process crashes), only operator review can resolve the comment. A crashed claim is eligible again when its lease expires, within the same five-attempt budget.
- Authors query `/api/v1/comments/mine?postId=...` using their authenticated identity. There is no user selector. Responses are `Cache-Control: no-store`; public comment lists remain APPROVED-only.
- Operator decisions require `expectedVersion`, `status` (`APPROVED`/`BLOCKED` only), and a nonblank reason of at most 1000 characters. A stale/repeated/nonpending decision returns 409; refresh before retrying. Exhaustion is not an automatic rejection.
- Claim, unavailable AI, stale result and terminal decision records are stored in `comment_review_audits`. Operator records include their UUID and reason. No comment body is copied into this table. The last 100 records per comment are available to operators only. History intentionally survives deletion of the comment/user.

## Concurrency and integration

- #299 owns DELETED, tombstones, edit/delete APIs and published-post visibility checks on both generic and community comment endpoints. Both migrations use `ADD COLUMN IF NOT EXISTS` for `version BIGINT NOT NULL DEFAULT 0`, `review_attempts INTEGER NOT NULL DEFAULT 0`, and `next_review_at TIMESTAMP NULL`. Merge the identical entity fields once. Every edit resets `reviewAttempts=0` and `nextReviewAt=null`.
- A recovery result is applied only after reacquiring the comment row lock and verifying PENDING, the exact claimed version, and the exact body. JPA `@Version` also makes the final update conditional on the version. A missing/deleted/edited/operator-resolved row is never recreated or reapproved. No event is published for a discarded result.
- #302 owns `com.fanpulse.domain.comment.CommentApprovedEvent(val commentId: UUID) : AbstractDomainEvent()` and notification persistence. This branch includes the identical event shape for independent compilation; retain one class when merging. `CommentJpaRepository.findByIdForUpdate` is shared with the notification consumer.
- Recovery and operator approval call `DomainEventPublisher.publish` synchronously **after flush, inside the same transaction**. The #302 listener must not be async or AFTER_COMMIT: failures must roll back approval and its audit. Notification deduplication uses `COMMENT_APPROVED:{commentId}`: the first approval only, not one per edited revision. #302 owns immediate-create publication; #299 owns edited-comment publication.
- #299 also owns switching the legacy creation/edit filter-log adapter to REQUIRED and removing swallowed audit failures. The new recovery audit never uses REQUIRES_NEW or swallows persistence/event errors.

## Local verification (2026-11-06)

- Backend targeted comment/domain/persistence/HTTP and JWT-filter tests: 49 passed. H2 uses its own dialect explicitly; this is not PostgreSQL locking/Flyway evidence. Scenarios include outage creation, recovery, backoff boundaries, five-attempt exhaustion, active/expired claims, a late worker after reclamation, edits including edit-away-and-back, physical deletion, operator-versus-AI ordering, stale/duplicate decisions, and listener-failure rollback of approval/audit.
- HTTP tests use signed access/refresh tokens with the actual security chain. They check anonymous/forged/refresh rejection, ordinary-user denial despite a forged role header, authenticated-author filtering, private no-store responses, and stale decisions. Existing public comment tests also pass; unauthenticated endpoints outside the new review API retain their existing 403 behavior.
- Web suite: 368 passed; the final operator-test typing fix was then rechecked with all 14 operator tests passing. TypeScript, changed-file ESLint, and the production webpack build passed.
- A real Chrome/Next browser scenario with mocked API responses passed: persisted private state after reload, recovery refresh into the public list, mobile layout/approval, operator approve/block with version and reason, audit display, and clearing private data on 403. Eight desktop/mobile state captures were visually inspected. This does not establish a real browser-to-PostgreSQL-to-Django flow.
- All heavy commands held the shared exclusive validation handle and released it in finally. The test server used port 3300 and exited; no worktree Node/JVM or 3300 listener remained. Port 8080 was not touched, the lock file was retained, and validation ownership was handed to #302.
- The full backend suite, PostgreSQL migrations/locking, real Django AI outage/recovery and the separately implemented #302 durable notification listener were not run in this worktree. There is no full-stack or full-backend green claim.

## Rollback and validation limits

Pause `COMMENT_REVIEW_ENABLED` before rolling back the application. Retain the additive columns and audit table; do not erase history or edit applied migrations. Coordinate #299/#302 merge conflicts in the entity, repository, event and shared web post-detail page before combined deployment.

H2 transaction tests and HTTP authorization tests do not prove PostgreSQL Flyway, real Django AI outage/recovery, or notification persistence from the separate #302 branch. Run those on the combined deployment before public operation. Do not start multiple copies of the repository's fixed-name Compose stack. Local verification must hold the shared `validation.lock` with `FileShare.None` and release the handle in `finally`; do not delete the lock or disturb port 8080.
