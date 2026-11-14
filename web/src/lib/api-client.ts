import axios, {
  AxiosError,
  AxiosInstance,
  InternalAxiosRequestConfig,
} from "axios";
import { resolveApiBaseUrl } from "./api-base-url";
import { buildLoginUrl } from "./login-redirect";

export { LocalApiTargetError } from "./api-base-url";

const pageHost = typeof window !== "undefined" ? window.location.hostname : null;
const apiTarget = resolveApiBaseUrl(process.env.NEXT_PUBLIC_API_BASE_URL, pageHost);

/**
 * Empty when the build configuration would send a local page to a remote API. Requests then
 * reject before reaching the network instead of leaking to production.
 */
export const API_BASE_URL = apiTarget.ok ? apiTarget.baseUrl : "";

export const API_TARGET_BLOCKED_REASON = apiTarget.ok ? null : apiTarget.reason;

if (!apiTarget.ok && typeof window !== "undefined") {
  // One visible hint in the browser console; pages keep rendering and surface their own error.
  console.error(apiTarget.reason.message);
}

const WEB_REFRESH_URL = `${API_BASE_URL.replace(/\/$/, "")}/auth/web/refresh`;
const AUTO_REFRESH_EXCLUDED_PATHS = [
  "/auth/google",
  "/auth/refresh",
  "/auth/web/refresh",
  "/auth/logout",
];

type RetriableRequestConfig = InternalAxiosRequestConfig & {
  _fanPulseRetried?: boolean;
  _fanPulseSessionGeneration?: string;
};

const SESSION_LOCK = "fanpulse:web-cookie-session";
const SESSION_GENERATION_KEY = "fanpulse:web-session-generation";
let localGeneration = "initial";
let useSharedGeneration = true;
let lifecycleTail: Promise<void> | null = null;
let refreshFlight: { generation: string; promise: Promise<void> } | null = null;

class WebSessionChangedError extends Error {}

/** Only an opaque lifecycle marker is shared, never credentials or user data. */
export function getWebSessionGeneration(): string {
  if (!useSharedGeneration) return localGeneration;
  try {
    return window.localStorage.getItem(SESSION_GENERATION_KEY) ?? localGeneration;
  } catch {
    return localGeneration;
  }
}

function advanceWebSessionGeneration(): void {
  localGeneration = `${Date.now()}-${Math.random()}`;
  try {
    window.localStorage.setItem(SESSION_GENERATION_KEY, localGeneration);
  } catch {
    // Reads may still return an old marker after a quota/write failure. Prefer
    // this realm's fence for its remaining lifetime; cross-tab publishing is unavailable.
    useSharedGeneration = false;
  }
}

function assertWebSessionGeneration(generation: string): void {
  if (generation !== getWebSessionGeneration()) {
    throw new WebSessionChangedError("The cookie session changed while this request was pending");
  }
}

function browserSessionLocks(): LockManager | undefined {
  return typeof window !== "undefined" ? window.navigator?.locks : undefined;
}

async function withWebSessionLock<T>(operation: () => Promise<T>): Promise<T> {
  const locks = browserSessionLocks();
  if (locks) return await locks.request(SESSION_LOCK, operation);

  // Older/insecure browsers have no origin-wide Web Locks. Still serialize this
  // realm's cookie writers; do not pretend a racy expiring storage lease is a lock.
  const result = lifecycleTail ? lifecycleTail.then(operation) : operation();
  const settled = result.then(() => undefined, () => undefined);
  lifecycleTail = settled;
  void settled.then(() => {
    if (lifecycleTail === settled) lifecycleTail = null;
  });
  return result;
}

/**
 * Login/logout are cookie writers too. Fence old recovery immediately, then wait
 * for any refresh response (including Set-Cookie) before sending the mutation.
 * Advance again on failure as well: callers may retry, but pre-outcome work may not.
 */
export function runWebSessionMutation<T>(operation: () => Promise<T>): Promise<T> {
  advanceWebSessionGeneration();
  return withWebSessionLock(async () => {
    try {
      return await operation();
    } finally {
      advanceWebSessionGeneration();
    }
  });
}

/**
 * 웹 httpOnly Refresh Cookie를 사용해 인증 쿠키를 회전한다.
 *
 * 같은 origin의 탭들은 쿠키 응답까지 잠금을 유지하고 인증 상태를 다시 확인한다.
 * 기본 axios 클라이언트를 사용해 apiClient 응답 인터셉터의 재귀 호출을 피한다.
 */
export function refreshWebSession(generation = getWebSessionGeneration()): Promise<void> {
  if (!apiTarget.ok) {
    return Promise.reject(apiTarget.reason);
  }

  if (generation !== getWebSessionGeneration()) {
    return Promise.reject(new WebSessionChangedError("The cookie session changed before recovery"));
  }
  if (refreshFlight?.generation === generation) return refreshFlight.promise;

  const promise = withWebSessionLock(async () => {
    assertWebSessionGeneration(generation);
    if (browserSessionLocks()) {
      // Another tab may have already rotated R0 while we waited. A mutex alone
      // prevents overlap but not redundant/stale recovery decisions.
      const status = await axios.get<{ authenticated: boolean }>(
        `${API_BASE_URL.replace(/\/$/, "")}/auth/me`,
        { withCredentials: true, timeout: 30000 },
      );
      assertWebSessionGeneration(generation);
      if (status.data.authenticated) return;
    }
    await axios.post<void>(WEB_REFRESH_URL, undefined, {
      withCredentials: true,
      timeout: 30000,
    });
    assertWebSessionGeneration(generation);
  }).finally(() => {
    if (refreshFlight?.promise === promise) refreshFlight = null;
  });
  refreshFlight = { generation, promise };
  return promise;
}

function stampSessionGeneration(config: InternalAxiosRequestConfig): InternalAxiosRequestConfig {
  const request = config as RetriableRequestConfig;
  request._fanPulseSessionGeneration ??= getWebSessionGeneration();
  if (request._fanPulseRetried) {
    // Another async interceptor can yield between scheduling a retry and
    // dispatching it. Recheck here too, before the adapter receives the config.
    assertWebSessionGeneration(request._fanPulseSessionGeneration);
  }
  return request;
}

/**
 * Axios 인스턴스
 * - withCredentials: true로 httpOnly 쿠키 자동 전송
 * - 토큰은 쿠키로 관리되므로 Authorization 헤더 불필요
 */
const client: AxiosInstance = axios.create({
  baseURL: API_BASE_URL || "/",
  timeout: 30000,
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true,
});

// Stamping/fencing is synchronous. Keep Axios's synchronous dispatch path: its
// async interceptor chain can discard a response recovered by an error handler.
client.interceptors.request.use(
  stampSessionGeneration,
  (error: unknown) => { throw error; },
  { synchronous: true },
);

if (!apiTarget.ok) {
  const blocked = apiTarget.reason;
  client.interceptors.request.use(() => {
    throw blocked;
  });
}

export const apiClient: AxiosInstance = client;

function shouldSkipAutoRefresh(url?: string): boolean {
  if (!url) return true;
  return AUTO_REFRESH_EXCLUDED_PATHS.some((path) => url.includes(path));
}

function redirectToLogin(): void {
  if (
    typeof window !== "undefined" &&
    window.location.pathname !== "/login" &&
    !window.location.pathname.startsWith("/login/")
  ) {
    window.location.replace(buildLoginUrl(`${window.location.pathname}${window.location.search}`));
  }
}

// Response 인터셉터: 401이면 웹 쿠키를 한 번 갱신한 후 원래 요청을 재시도한다.
apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const status = error.response?.status;
    const requestConfig = error.config as RetriableRequestConfig | undefined;

    if (
      status !== 401 ||
      !requestConfig ||
      typeof window === "undefined" ||
      shouldSkipAutoRefresh(requestConfig.url)
    ) {
      return Promise.reject(error);
    }

    const generation = requestConfig._fanPulseSessionGeneration;
    if (!generation || generation !== getWebSessionGeneration()) return Promise.reject(error);

    if (requestConfig._fanPulseRetried) {
      redirectToLogin();
      return Promise.reject(error);
    }

    requestConfig._fanPulseRetried = true;

    try {
      await refreshWebSession(generation);
      assertWebSessionGeneration(generation);
      return apiClient.request(requestConfig).catch((retryError: unknown) => {
        // Preserve the original 401 if the retry was fenced before dispatch;
        // actual retry responses (in particular 403) retain their own semantics.
        throw retryError instanceof WebSessionChangedError ? error : retryError;
      });
    } catch {
      if (generation === getWebSessionGeneration()) redirectToLogin();
      return Promise.reject(error);
    }
  }
);

/**
 * API 에러 타입
 */
export interface ApiErrorPayload {
  code: string;
  message: string;
}

export class FanPulseApiError extends Error {
  readonly code?: string;
  readonly status?: number;

  constructor(message: string, opts: { code?: string; status?: number } = {}) {
    super(message);
    this.name = "FanPulseApiError";
    this.code = opts.code;
    this.status = opts.status;
  }
}

/**
 * Axios 에러를 FanPulseApiError로 변환
 */
export function toFanPulseError(error: unknown): FanPulseApiError {
  if (error instanceof FanPulseApiError) {
    return error;
  }

  if (axios.isAxiosError(error)) {
    const data = error.response?.data;
    const status = error.response?.status;

    // API 실패 응답 형식
    if (data?.success === false && data?.error) {
      return new FanPulseApiError(data.error.message, {
        code: data.error.code,
        status,
      });
    }

    // Problem Detail 형식 (RFC 7807)
    if (data?.title && data?.status) {
      return new FanPulseApiError(data.detail || data.title, {
        code: data.errorCode,
        status: data.status,
      });
    }

    // 일반 에러
    return new FanPulseApiError(
      error.message || "요청에 실패했습니다.",
      { status }
    );
  }

  if (error instanceof Error) {
    return new FanPulseApiError(error.message);
  }

  return new FanPulseApiError("알 수 없는 오류가 발생했습니다.");
}
