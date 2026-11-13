# Approved-comment in-app notifications (#302)

Scope: persisted in-app notifications only. No browser/mobile push, FCM, Kafka,
background delivery, or historical notification backfill.

## Approval integration contract

Agreed with #299 (edit/delete) and #300 (review recovery):

- The canonical event is
  `com.fanpulse.domain.comment.CommentApprovedEvent(val commentId: UUID) : AbstractDomainEvent()`.
  #302 owns the definition. Keep one identical class when integrating the branches.
- Producers publish through `DomainEventPublisher` **synchronously in the same write
  transaction**, after the approval has been saved/flushed successfully. This is not
  an asynchronous event or an `AFTER_COMMIT` callback.
- #302 wires creation. #299 wires approved edits. #300 wires recovery/admin approval
  only after its expected id/version/PENDING/content validation and successful write.
  Deleted, stale, non-pending, rejected, and failed optimistic/CAS writes must not publish.
- `CommentAdapter.save` uses `saveAndFlush`. Merge this change with #299 rather than
  adding a second save/publish path. Retain exactly one event publication per successful
  producer transition; duplicates are tolerated but unnecessary.
- `CommentJpaRepository.findByIdForUpdate(id)` is a `PESSIMISTIC_WRITE` query shared
  with #300. Keep one method on merge.
- The consumer locks the comment, checks its current `APPROVED` status, resolves the
  actual published community post and owner, and excludes the comment's author.
  Legacy non-UUID post IDs and missing/removed posts produce no notification.
- `Notification.sourceKey = COMMENT_APPROVED:{commentId}` is globally unique. This is
  **one notification per comment's first approval**, not one per revision. An approved
  edit or recovery redelivery must not reset an existing notification's read state.
  Row locking serializes deliveries and the unique constraint is the database backstop.
- The listener requires an existing transaction and does not swallow errors. Comment
  and notification commit or roll back together. Notification persistence failure fails
  the approval; callers may retry, and no notification is visible from a failed commit.
- The agreed #299-owned audit fix is also reproduced here for standalone operation:
  `CommentFilterLogAdapter.save` participates with `REQUIRED`, and the producer no
  longer catches audit errors. Approval → synchronous event → audit share one transaction.
  Merge this identical fix once; do not restore the previous `REQUIRES_NEW`/catch path.

Do not add a post write lock inside this listener: #299/#300 own producer locking and
version validation. The listener's post read does not invert their post → comment write
lock order. A post deleted after the event remains a safely unavailable notification
target, rather than deleting the notification/deduplication record.

## Authenticated API contracts

All routes derive ownership exclusively from the authenticated `userId` request
attribute. No recipient ID is accepted from the client.

| Route under `/api/v1/users/me` | `ApiResponse.data` |
| --- | --- |
| `GET /notifications?unreadOnly=false` | Existing array contract, unchanged |
| `GET /notifications/page?unreadOnly=false&page=0&size=20` | `{content,page,size,totalElements,totalPages,last,unreadCount}` |
| `GET /notifications/unread-count` | `{unreadCount}` across all of this user's pages |
| `PATCH /notifications/{notificationId}/read` | Updated notification, or 404 for missing/foreign IDs |
| `PATCH /notifications/read-all` | `{updated}` count of this user's newly read rows |

Notification fields retain `id,type,message,isRead,createdAt` and add nullable
`targetType,targetId` plus `targetAvailable`. Comment notifications use `type=COMMENT`,
`targetType=POST` and the community post UUID. Availability is computed from the current
published post, including again when marking read. Existing notifications have no
target and remain readable without a fabricated destination.

Pages are zero-based, negative pages are rejected, sizes are clamped to 1–100, and
ordering is `createdAt DESC, id DESC`. Unread counts are not the current page length.
Unread-only clients must restart pagination after reading rows to avoid offset skips.

## Web and deletion integration

The web page marks an individual notification read before navigating to
`/post-detail?id={targetId}`. A failed write must not fake read state or navigate.
An unavailable target shows a deletion/unavailability notice. The detail page also
handles a 404 after navigation, covering deletion between the read response and fetch.
The header badge uses the authenticated account's unread count, not a fixed red dot.

#298 adds `addFavorite` to the favorites section of `web/src/lib/api/social.ts`;
its notifications section and production `SocialUserController` are unchanged.
Preserve that addition together with this branch's notification sections. Also retain
#298's `social.test.ts` POST mock/response tests and `SocialUserControllerTest.kt`
favorite authentication/404 tests when combining both branches' test changes.
#299/#300 also change the detail page and comment service: retain both features rather
than replacing whole files. Generic `/comments` publication/deletion checks and comment
`version`/`DELETED` belong to those branches, not a second implementation here.

## Migration and verification boundaries

`V20261106.302.1__notification_targets_and_deduplication.sql` adds nullable targets,
the unique source key, target-pair constraint, and user/page indexes. It does not
change the comment status/version schema owned by #299/#300. Target/source IDs are
not foreign keys so deletion cannot erase delivery deduplication or notification history.

The backend regression scenarios cover real H2 transactions: create → stored owner
notification → read, self/non-approved/missing targets, replay/reapproval/concurrent
delivery, a commit-boundary failure, notification/audit insert failures, equal-time page
ordering, unread totals, foreign reads, and removed targets. H2 uses generated schema
with Flyway disabled and **does not verify this PostgreSQL migration**. Production
PostgreSQL and real AI verification require an available isolated environment.

The targeted backend run passed 44 tests (zero failures, errors, or skipped tests),
including seven notification integration scenarios. It covered
`CommentNotificationIntegrationTest`, `CommentCommandServiceTest`, `CommentAdapterTest`,
`SocialUserServiceIntegrationTest`, `SocialUserControllerTest`,
`NotificationJpaRepositoryTest`, `CommentControllerTest`, and
`CommentQueryServiceIntegrationTest`. The first run exposed an inherited PostgreSQL
dialect in the new H2 row-lock test; explicitly selecting H2Dialect in that test fixed
the test environment, and the same targets then passed. The full backend suite and
combined #299/#300 producer paths have not been executed on this branch.

Heavy validation is serialized after #300's explicit handoff, always in a process
holding `C:\Users\jskjw\Downloads\FanPulse-worktrees\.amp\in\validation.lock`
with `FileShare.None`, released in `finally`. Do not delete the lock file, use the
existing 8080 service, or start the fixed-name default Compose stack in parallel.

The completed web checks were `npm ci`, the full Vitest suite (353 tests / 64 files),
TypeScript, ESLint on the 12 changed web files, and the production build. All passed.
Twelve desktop/mobile Playwright scenarios passed against the production web server
on port 3302 with every API request mocked and the proxy directed away from 8080.
They exercise pagination, legacy rows, read-before-navigation, unread badge updates,
read/list/read-all failures and retries, stale filter responses, deleted targets,
and navigation-time 404s. Eleven screenshots were inspected, including successful
navigation and mobile scroll-end access to the final card/pagination above the
existing fixed navigation. These are browser contract checks, not a combined
Spring/PostgreSQL/AI end-to-end run.

The automated browser server and children exited, ports 3302/3399 were released,
and exclusive lock reacquisition/release was verified with the original file retained.
Validation logs and screenshots are under `.amp/in/artifacts/`; the detailed web
command/settings record is `web-validation-report.md` there. npm reported 26 existing
dependency advisories (18 high, 2 critical); no dependency remediation or install-policy
changes were included. No commit, push, merge, deployment, or other-branch integration
was performed.
