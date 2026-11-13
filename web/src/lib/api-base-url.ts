/**
 * Resolves the API base URL for the web app.
 *
 * `NEXT_PUBLIC_API_BASE_URL` is inlined by Next.js at build time, so a local build that
 * forgets it used to call the production API from a developer machine. The rules below keep
 * production behaviour unchanged while making a local page either use the same-origin proxy
 * or fail before any request leaves the browser.
 */
export const PRODUCTION_API_BASE_URL = "https://api.fanpulse.app/api/v1";

export class LocalApiTargetError extends Error {
  readonly configured: string;
  readonly pageHost: string;

  constructor(configured: string, pageHost: string) {
    super(
      `로컬 페이지(${pageHost})에서 원격 API(${configured})로 요청할 수 없습니다. ` +
        "NEXT_PUBLIC_API_BASE_URL=/api/v1 로 다시 빌드하고 API_PROXY_TARGET을 로컬 백엔드로 지정하세요."
    );
    this.name = "LocalApiTargetError";
    this.configured = configured;
    this.pageHost = pageHost;
  }
}

export type ApiBaseUrlResolution =
  | {
      ok: true;
      baseUrl: string;
      source: "configured" | "loopback-default" | "production-default";
    }
  | { ok: false; reason: LocalApiTargetError };

const LOOPBACK_HOSTS = new Set(["localhost", "127.0.0.1", "0.0.0.0", "::1", "[::1]"]);

/** `localhost`, `127.0.0.1`, `::1`, and `*.localhost` are treated as the developer machine. */
export function isLoopbackHost(hostname: string): boolean {
  const host = hostname.trim().toLowerCase();
  return LOOPBACK_HOSTS.has(host) || host.endsWith(".localhost");
}

function isAbsoluteUrl(value: string): boolean {
  return /^[a-z][a-z\d+\-.]*:\/\//i.test(value);
}

function isLoopbackUrl(value: string): boolean {
  try {
    return isLoopbackHost(new URL(value).hostname);
  } catch {
    return false;
  }
}

/**
 * @param configured raw `NEXT_PUBLIC_API_BASE_URL` value (may be undefined or blank)
 * @param pageHost `window.location.hostname`, or null outside a browser (build, SSR)
 */
export function resolveApiBaseUrl(
  configured: string | undefined,
  pageHost: string | null
): ApiBaseUrlResolution {
  const value = configured?.trim();
  const onLoopback = pageHost !== null && isLoopbackHost(pageHost);

  if (value) {
    if (!isAbsoluteUrl(value)) {
      // A path such as `/api/v1` keeps the browser on the same origin and goes through the
      // Next.js rewrite proxy.
      return { ok: true, baseUrl: value, source: "configured" };
    }
    if (onLoopback && !isLoopbackUrl(value)) {
      return { ok: false, reason: new LocalApiTargetError(value, pageHost ?? "") };
    }
    return { ok: true, baseUrl: value, source: "configured" };
  }

  if (onLoopback) {
    // No configuration at all: stay same-origin so the bundled rewrite proxy answers.
    return { ok: true, baseUrl: "/api/v1", source: "loopback-default" };
  }

  // Unchanged legacy behaviour for production builds that rely on the default.
  return { ok: true, baseUrl: PRODUCTION_API_BASE_URL, source: "production-default" };
}
