# Browser account-boundary preconditions (#318)

Cookies belong to the origin, not the tab. A tab displaying account A can send a
request authenticated as B after another tab logs in. Aborting a request or
checking its response identity cannot undo a write already made to B.

## HTTP contract

`X-Expected-User-Id: <UUID>` asserts the identity whose state the caller is editing
or displaying. It is **not** authentication, permission to select another user,
or an optimistic resource-version/ETag check. The authenticated `userId` remains
the only target. The assertion is checked before entering a mutation/query
service, so a mismatch cannot save data or publish mutation events.

| Request | Header policy |
| --- | --- |
| `PATCH /api/v1/me` and `PATCH /api/v1/me/settings`, cookie authentication | Required |
| Same PATCH requests, explicit `Authorization: Bearer ...` | Optional for existing native/API clients; enforced if supplied |
| `GET /api/v1/me`, `GET /api/v1/me/settings` | Optional; enforced if supplied |
| GET `/api/v1/users/me/notifications`, `/notifications/page`, `/notifications/unread-count` | Optional; enforced if supplied |
| PATCH `/api/v1/users/me/notifications/{id}/read`, `/notifications/read-all` | Optional; enforced if supplied |

The last two rows use the `/api/v1/users/me` prefix. Existing response bodies and
legacy notification array endpoints are unchanged. Other endpoints (including
password changes and favorites) are outside this contract.

* Missing required header: **428**, RFC 7807 `errorCode: EXPECTED_USER_REQUIRED`.
* Present but blank, malformed, combined, or different from the authenticated
  principal's canonical UUID (case-insensitive): **412**,
  `errorCode: EXPECTED_USER_MISMATCH`. The response does not disclose the actual
  account ID or private data.
* Authentication/authorization still runs first. A header cannot authenticate a
  request. Bearer-over-cookie credential precedence is unchanged.
* Neither failure may be retried automatically with a newly discovered user ID.
  Refresh identity, discard the draft, and require a new user action.

Browser profile/settings writes always send the captured displayed user ID.
Browser private profile/settings reads and notification page/count/read-state
requests also send it, preventing B's data from being labeled as A even if the
cookie switch has not yet produced an event. A retried 401 request must preserve
this header; if session refresh selects B, the original A operation fails 412.

## Compatibility and rollout

Existing bearer clients need no changes. Legacy private GETs and notification
clients may omit the assertion deliberately; they do not gain stale-tab
protection until upgraded. Old cookie clients that omit it on profile/settings
PATCH now fail closed with 428; this is intentional, not silent backward
compatibility. Reload/upgrade those browser clients. Deploy the updated web
client **before server enforcement**. Older tabs still running the previous web
bundle must reload before they can save again. Reverse proxies must
forward the header unchanged; trusted cross-origin deployments must permit it
in CORS preflights (the current backend allows request headers).

## Tab synchronization and private-state lifecycle

`AuthProvider` publishes a non-sensitive nonce at localStorage key
`fanpulse:session-changed` when a checked identity changes and after confirmed
logout. Other tabs listen for that key; incoming events do not echo. Focus and
visible-document transitions revalidate too, covering missed events and disabled
storage. No token or account data is stored in the signal. Precondition failures
emit the local `fanpulse:session-invalidated` event and refresh identity.

Before awaiting identity, the provider nulls the current user, marks auth loading,
clears/aborts settings, resets language, and increments `sessionVersion`.
Profile and notification snapshots, count requests, and editor drafts are scoped
to that version as well as user ID. They are hidden/invalidated even when a fast
refresh returns the same ID or the sequence is A -> B -> A. Language and nickname
drafts are discarded, never carried to the refreshed account. The notification
badge no longer launches its own focus request ahead of identity revalidation.

This is event/focus revalidation, not polling or instantaneous detection of
arbitrary HttpOnly-cookie writes. Without an event, previously rendered A data
can remain until focus/revalidation; guarded requests cannot fetch/mutate B under
A's scope. Storage-disabled tabs rely on focus/visibility or a failed assertion.
Automatic token refresh and login/logout cookie lifecycle serialization remain
owned by #317; these changes do not modify `api-client.ts` or `auth.ts`.

## Chromium regression scope

`web/e2e/account-boundary.spec.ts` uses two real pages in one browser context,
with synthetic HTTP provided by `web/e2e/fixtures/account-boundary.ts`:

* An HTTP response in the second tab replaces the shared HttpOnly session cookie
  A -> B. Mounting the real B `AuthProvider` announces the session; the receiving
  A tab must observe a **native trusted storage event**, not a dispatched test
  event. Incoming signals must not echo another nonce.
* Native `bringToFront()` focus/visibility transitions recover a cookie switch
  without a session announcement. The tests require native trusted browser
  events; they do not synthesize DOM focus or visibility events.
* Auth-response gates hold identity unresolved while assertions check that old
  nickname/language drafts, profile identity, notification rows and unread badge
  are absent. Releasing the gate shows only freshly fetched B data and resets
  drafts. No settings/profile mutation is sent or replayed during revalidation.
* A no-event cookie switch leaves A's draft in the foreground tab. Submitting it
  sends A's expected ID with the real shared B cookie, receives one 412, triggers
  identity revalidation, and leaves both fixture accounts unchanged. Resolving
  identity must not replay that operation. A subsequent **explicit** B edit is
  the positive control and performs exactly one successful mutation.
* Same-account focus also hides private state, refetches profile/notification
  data, discards drafts, and does not announce an identity change or replay a
  write. Retaining a draft merely because the UUID stayed the same is not valid.
* A no-event notification filter/reload sends the still-displayed A assertion,
  fails 412 instead of exposing B rows under A, and hides private rows/counts
  while identity is revalidated.

There are 12 Chromium cases. All browser API traffic is fulfilled in memory;
unknown API requests fail 501 instead of reaching the Next backend proxy.
External browser HTTP/WebSockets and image-optimizer requests are blocked, and
service workers are disabled. The fixture does not use Google, a production
server, a database or a browser cookie mutation API. Request logs derive the
principal from Chromium's actual Cookie header, not a mutable test principal.
The denied write is enforced by a **synthetic** HTTP precondition in this suite;
the independent real-JWT/filter/MVC `ExpectedUserPreconditionTest` is what checks
the Kotlin controller denies before calling mutation services. Neither suite
alone is full-stack browser-to-Kotlin proof.

Each browser case records a trace/video and attaches the request/mutation/event
journal; pending-identity cases also attach a screenshot. These are only evidence
after central execution and inspection, not evidence from this coding task.

## Serialized regression commands

Execution is reserved for the central executor. These commands have **not** been
run as part of this coding task. No observed red/green claim is made.

From `web` (existing dependencies only):

```powershell
.\node_modules\.bin\vitest.cmd run src/app/profile-settings.test.tsx src/hooks/useMyProfile.test.ts src/hooks/useNotifications.test.ts src/components/layout/Header.test.tsx src/app/notifications/page.test.tsx src/lib/api/user.test.ts src/lib/api/social.test.ts --maxWorkers=1 --no-file-parallelism
```

Focused lint, including the queued/cancelable profile effect and new browser files
(no rule suppressions; separately tracked existing hook findings are not fixed):

```powershell
.\node_modules\.bin\eslint.cmd src/hooks/useMyProfile.ts src/hooks/useMyProfile.test.ts e2e/account-boundary.spec.ts e2e/fixtures/account-boundary.ts
```

Chromium, also from `web`, with dependencies/browser already provisioned centrally:

```powershell
$env:NEXT_PUBLIC_API_BASE_URL = '/api/v1'
$env:API_PROXY_TARGET = 'http://127.0.0.1:9'
$env:NEXT_TELEMETRY_DISABLED = '1'
$env:PLAYWRIGHT_PORT = '3018'
$env:PLAYWRIGHT_HTML_OPEN = 'never'
$env:PLAYWRIGHT_HTML_OUTPUT_DIR = '../.amp/in/artifacts/318-chromium-report'
.\node_modules\.bin\playwright.cmd test e2e/account-boundary.spec.ts --project=chromium --workers=1 --retries=0 --reporter=line,html --output=../.amp/in/artifacts/318-chromium
```

Before writing artifacts, ensure `/.amp/in/` is present in the repository-local
exclude file located by `git rev-parse --git-path info/exclude`; preserve its other
rules. Do not reuse a dev server compiled from different code for red/green runs.
No install or browser download is part of these commands. Browser routing does
not intercept server-process networking: the existing `next/font/google` setup
may download fonts while compiling. Central must use its prewarmed/offline-font
setup and deny process egress as well for a completely offline run. No compiler,
browser, server or font fetch was launched by this worker.

From `backend` (no Docker/database needed for the new standalone MVC suite):

```powershell
.\gradlew.bat test --tests com.fanpulse.interfaces.rest.identity.ExpectedUserPreconditionTest --tests com.fanpulse.application.identity.command.UpdateUserProfileHandlerTest --no-daemon --max-workers=1
```

Central's broader backend compatibility run may need to add matching expected-user
assertions to old cookie PATCH fixtures. Their unrelated authorization/403 and
mutation expectations must remain unchanged; no compatibility pass is claimed
here.

For a red comparison, retain the new regression tests with the pre-change
production files from `995701a` in an isolated central-executor workspace and run
the same focused commands. Expected red: missing/stale browser assertions are not
enforced, stale notifications can load the cookie principal's rows, private
state/drafts persist during refresh. Reapply this implementation and run the
identical commands for green. Do not overwrite an active worker checkout to
stage the comparison.
