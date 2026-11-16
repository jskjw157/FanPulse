// Keep this list in sync with app routes. Login, error pages, API paths and
// arbitrary descendants are deliberately not return destinations.
const PAGE_PATHS = new Set([
  "/",
  "/ads",
  "/admin/comment-reviews",
  "/chart",
  "/community",
  "/concert",
  "/favorites",
  "/live",
  "/membership",
  "/mypage",
  "/news",
  "/notifications",
  "/post-create",
  "/saved",
  "/search",
  "/settings",
  "/support",
  "/tickets",
  "/voting",
]);

// These pages consume only `id`; the legacy aliases redirect to canonical pages.
const QUERY_DETAIL_PATHS = new Set([
  "/artist-detail",
  "/concert-detail",
  "/live-detail",
  "/news-detail",
  "/notice-detail",
  "/post-detail",
]);

const ARTIST_ID = /^[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}$/i;

function isSafeId(id: string): boolean {
  // IDs are opaque, not paths. Reject even once/twice-encoded separators,
  // traversal, controls and URL syntax after URLSearchParams decodes them.
  return id.length > 0 && !/[^a-z0-9_-]/i.test(id);
}

/** Validate an already outer-decoded login `redirect`, without URL normalization. */
export function getSafeLoginReturnTo(value: string | null | undefined): string | null {
  if (!value || !value.startsWith("/") || value.includes("\\") || value.includes("#")) {
    return null;
  }

  const queryIndex = value.indexOf("?");
  const pathname = queryIndex < 0 ? value : value.slice(0, queryIndex);
  const query = queryIndex < 0 ? "" : value.slice(queryIndex + 1);

  if (QUERY_DETAIL_PATHS.has(pathname)) {
    if (!query) return pathname;
    const entries = [...new URLSearchParams(query)];
    if (entries.length !== 1) return null;
    const [key, id] = entries[0];
    if (key !== "id" || !isSafeId(id)) return null;
    if (pathname === "/artist-detail" && !ARTIST_ID.test(id)) return null;
    return `${pathname}?id=${id}`;
  }

  // No other current page reads query state. In particular, never forward nested
  // redirect/next URLs or credentials to an otherwise allowed destination.
  if (query) return null;
  if (PAGE_PATHS.has(pathname)) return pathname;

  const segments = pathname.split("/");
  if (segments.length !== 3) return null;
  const [, kind, id] = segments;
  if (!isSafeId(id)) return null;
  if (kind === "artists" && ARTIST_ID.test(id)) return pathname;
  if (kind === "news" || kind === "live") return pathname;
  return null;
}

/** Use the same policy at every producer of the login URL and at its consumer. */
export function buildLoginUrl(returnTo: string | null | undefined): string {
  const safeReturnTo = getSafeLoginReturnTo(returnTo);
  return safeReturnTo ? `/login?redirect=${encodeURIComponent(safeReturnTo)}` : "/login";
}
