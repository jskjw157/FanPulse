# #299 Community owner edit/delete integration contract

Issue: https://github.com/jskjw157/FanPulse/issues/299
Branch: `feat/community-edit-delete`; base: `4258d08686b9eee9fe35df4bc3d7488b786ac3f0`.

## API and visibility

- `PUT /api/v1/community/posts/{postId}` with `{content}` returns the existing post envelope (200).
- `DELETE /api/v1/community/posts/{postId}` returns 204, is owner-only and idempotent for that owner.
- `PUT /api/v1/community/posts/{postId}/comments/{commentId}` with `{content}` returns the existing comment envelope (200 APPROVED/BLOCKED, 202 PENDING).
- `DELETE /api/v1/community/posts/{postId}/comments/{commentId}` returns 204; the service checks both the owner and actual post relationship.
- Mobile `PUT/DELETE /api/v1/comments/{commentId}` share the same service; PUT preserves the raw comment DTO rather than wrapping it. Existing raw GET/POST and legacy ObjectId post IDs remain compatible.
- Authentication provides the author ID. Client-supplied author IDs are never accepted. Non-owners receive 403; optimistic concurrency conflicts receive 409.
- Post edits reuse creation's fail-closed moderation: only explicit, named-model allow replaces the body; rejection or unavailability leaves the prior approved version intact.
- Comment edits run the existing filter again. BLOCKED/PENDING edits disappear from public lists/counts; only APPROVED comments count (including replies).
- Posts use REMOVED, excluded from detail/latest/popular/saved rows and pagination totals. Existing saved rows can still be unsaved. UUID post checks apply to both generic and community comment routes; mutations lock the published post to serialize against post deletion.
- Deleted comments retain their row, ID and parent references but replace the body with a nonempty tombstone. Public lists omit the parent and retain approved replies, including across pages. New replies to deleted/nonapproved parents are rejected. Deletion is terminal; edit/approve/block cannot resurrect it.
- Web owner menus, prefilled content editor and explicit cancelable deletion dialog are on post detail. Comment mutations reload the public page/count from page zero; failed reloads hide stale content and retry reads only. Post deletion navigates to community; community/saved pages fetch on remount.

## Shared contract with #300 and #302

Agreed directly by the three workers; no other worktree's files were copied.

- `Comment.version: Long = 0`, `@Version`, `version BIGINT NOT NULL DEFAULT 0`.
- `reviewAttempts: Int = 0` / `review_attempts INTEGER NOT NULL DEFAULT 0`.
- `nextReviewAt: Instant? = null` / `next_review_at TIMESTAMP NULL`.
- #299 migration `V20261106.299.1__community_edit_delete.sql` adds the three columns with IF NOT EXISTS and updates `chk_comments_status` to include DELETED. #300 independently adds the same columns with IF NOT EXISTS; it must not replace the deletion check.
- Editing/deleting resets reviewAttempts and nextReviewAt. Any successful change increments the JPA version on flush. #300's recovery/admin application must still verify PENDING, expected version and exact content after its AI call; stale/nonpending/deleted work produces no event/audit decision.
- Shared query name: `CommentJpaRepository.findByIdForUpdate(id)` with PESSIMISTIC_WRITE. Mutations that also lock community posts use post-before-comment ordering; integration must preserve lock ordering and reread freshness.
- Shared event: `com.fanpulse.domain.comment.CommentApprovedEvent(commentId: UUID) : AbstractDomainEvent`. #302 is the final owner of this identical standalone event file; merge duplicates into one definition.
- CommentAdapter.save uses saveAndFlush. Successful APPROVED create/edit publishes through DomainEventPublisher inside that same transaction after flush; a stale write cannot publish. There is no asynchronous/after-commit replay contract.
- #302 synchronously rereads/locks the current comment and checks APPROVED, the actual user and the published post before storing a notification. Notification sourceKey `COMMENT_APPROVED:{commentId}` means only the first approval notifies, even when an edit is later reapproved.
- Filter logs use REQUIRED, not REQUIRES_NEW. Audit and listener failures propagate and roll back the whole approval/notification transaction. The former log-failure-swallowing unit expectation intentionally changes.

## Validation targets

- Backend service/persistence tests: ownership, cross-post IDs, AI edit outcomes, recovery lease reset, terminal deletion, preserved replies, exact visible counts, stale detached snapshots, listener/audit failure rollback, removed-post generic route rejection and legacy mobile IDs.
- Controller tests: anonymous rejection, authenticated identity, raw-vs-envelope DTOs, 200/202/204/403/409 contracts.
- Existing community tests: explicit moderation allow/fallback, list/saved pagination, idempotent likes/saves and native popular ordering.
- Web Vitest, TypeScript, production build and mocked Playwright desktop/mobile owner/edit/confirm/error/list/saved flows. Browser mocks validate UI contracts, not real PostgreSQL or AI behavior.
- PostgreSQL/Flyway/real AI end-to-end verification requires separately prepared services. H2 tests do not prove PostgreSQL locking/Flyway execution. Never reuse/stop the existing 8080 process or start duplicate Compose stacks.
- Heavy commands run only in #299's assigned turn, under the shared validation.lock FileShare.None process handle, released in finally without deleting the lock file.

## Executed validation (2026-11-06)

- `npm ci`: success, 488 packages. No npm build-script approvals or permanent configuration changes.
- `backend\gradlew.bat -p backend test --no-daemon --max-workers=1 -Dorg.gradle.jvmargs="-Xmx512m -XX:MaxMetaspaceSize=256m" -Pkotlin.compiler.execution.strategy=in-process --tests '*comment.*' --tests '*community.*'`: 67 passed, 1 PostgreSQL opt-in suite skipped; no failures. Production and all test sources compiled. The seven new JPA integration cases passed, including optimistic stale-write rejection and actual audit database failure rollback.
- `npm --prefix web test -- --run --maxWorkers=1`: 361 passed in 63 files. An initial asynchronous editor assertion was corrected to wait for editor dismissal; this final run is green.
- `web\node_modules\.bin\tsc --noEmit -p web\tsconfig.json`: passed.
- `npm --prefix web run lint -- src/lib/api/community.ts src/lib/api/community.test.ts src/app/post-detail/page.tsx src/app/post-detail/page.test.tsx e2e/community-owner.spec.ts`: passed.
- `npm --prefix web run build`: passed (Next.js production build).
- Playwright owner flows: 10 desktop/mobile Chromium scenarios passed using browser-intercepted API responses on dedicated port 3299. Initial failures were test-only assumptions about Next's route-announcer alert and Strict Mode request counts; assertions now check the public UI instead. Installed Chromium was selected by an ignored temporary config; no browser download or permanent config edit was needed.
- Browser captures cover default ownership, owner menu, both editors, both confirmations, failed edit/delete, retained replies, and PENDING/BLOCKED removal. Screenshots are review artifacts under `.amp/in/artifacts/community-owner/`, not product fixtures. UI execution uses mocked APIs, not a real AI/database end-to-end run.
- Unrestricted `backend\gradlew.bat -p backend test --no-daemon --max-workers=1` with 512 MiB Gradle heap / 384 MiB metaspace and the default 512 MiB test-worker heap: **FAILED**, 791 tests, 626 passed / 159 failed / 6 skipped, 13m03s. Full XML results are preserved under `.amp/in/artifacts/validation/backend-full-results/` before rerunning the targeted suite.
- Full-suite failure evidence: four unchanged concert assertions use September 2026 fixtures although the upcoming query filters against the current Seoul date; 16 subprocess tests cannot execute Windows shell built-in `echo` via ProcessBuilder; initial AI contexts reject a missing enabled-service API key; Flyway cannot connect to localhost:5432. Later controller contexts (including the same controllers that passed in the targeted run) exhaust Java heap space, followed by cached-context-threshold failures. These are observed failure categories, not a claim that a baseline full-suite rerun passed. No unrelated code/configuration was changed to hide them.
- A fresh targeted backend JVM was run after the full-suite failure: again 67 passed / 1 PostgreSQL suite skipped / 0 failures (1m47s); its XML results are separately preserved in `backend-targeted-results/`. Final TypeScript, changed-file lint and the 10 Playwright cases also passed again. Mobile comment editing additionally checks that the surviving reply is reachable in the viewport when scrolled.
- All heavy processes exited normally. The temporary browser config was removed; the worktree has no remaining Node/JVM process and port 3299 has no listener. The shared lock was exclusively reacquired and released as a cleanup check, without deleting its file. Validation turn was handed directly to #300; no further heavy command will start here.

## Mobile compatibility and integration limits

- Android `CommentsApiService` currently consumes raw `Comment`/`CommentListResponse` on `/comments`, not the web envelope; these existing responses and legacy string/ObjectId IDs remain unchanged. The new generic PUT also returns the raw DTO. Android status is a string, and only APPROVED rows are publicly returned.
- The inspected iOS community detail still uses `addMockComments()` and a local `Post` model, rather than this backend comment contract. No native app UI/build was changed or executed.
- PostgreSQL/Flyway migration execution, actual PostgreSQL lock interleavings, real AI, and the combined #300 recovery/#302 notification consumer are unverified in this isolated branch. Docker/Podman/psql were not available on PATH. No Compose stack was started and the existing 8080 process was left alone.
- During integration, merge the identical event/repository method/three Comment fields once, retain #299's DELETED check and #300's PENDING/version/content terminal guard together, preserve the REQUIRED audit transaction, and keep #302's synchronous listener/first-approval dedupe. The REQUIRES_NEW/FK-lock concern is a code-based risk, not a reproduced PostgreSQL deadlock.
- Delivery is local, uncommitted and unpushed on the existing branch; no issue, branch, AGENTS file or permanent setting was created/changed.
