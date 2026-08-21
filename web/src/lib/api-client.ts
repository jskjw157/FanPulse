import axios, {
  AxiosError,
  AxiosInstance,
  InternalAxiosRequestConfig,
} from "axios";

export const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "https://api.fanpulse.app/api/v1";

const WEB_REFRESH_URL = `${API_BASE_URL.replace(/\/$/, "")}/auth/web/refresh`;
const AUTO_REFRESH_EXCLUDED_PATHS = [
  "/auth/google",
  "/auth/refresh",
  "/auth/web/refresh",
  "/auth/logout",
];

type RetriableRequestConfig = InternalAxiosRequestConfig & {
  _fanPulseRetried?: boolean;
};

let refreshPromise: Promise<void> | null = null;

/**
 * 웹 httpOnly Refresh Cookie를 사용해 인증 쿠키를 회전한다.
 *
 * 동시에 여러 요청이 401을 반환해도 실제 갱신 요청은 하나만 실행한다.
 * 기본 axios 클라이언트를 사용해 apiClient 응답 인터셉터의 재귀 호출을 피한다.
 */
export function refreshWebSession(): Promise<void> {
  if (refreshPromise === null) {
    refreshPromise = axios
      .post<void>(WEB_REFRESH_URL, undefined, {
        withCredentials: true,
        timeout: 30000,
      })
      .then(() => undefined)
      .finally(() => {
        refreshPromise = null;
      });
  }

  return refreshPromise;
}

/**
 * Axios 인스턴스
 * - withCredentials: true로 httpOnly 쿠키 자동 전송
 * - 토큰은 쿠키로 관리되므로 Authorization 헤더 불필요
 */
export const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true,
});

function shouldSkipAutoRefresh(url?: string): boolean {
  if (!url) return true;
  return AUTO_REFRESH_EXCLUDED_PATHS.some((path) => url.includes(path));
}

function redirectToLogin(): void {
  if (
    typeof window !== "undefined" &&
    window.location.pathname !== "/login"
  ) {
    window.location.href = "/login";
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

    if (requestConfig._fanPulseRetried) {
      redirectToLogin();
      return Promise.reject(error);
    }

    requestConfig._fanPulseRetried = true;

    try {
      await refreshWebSession();
      return apiClient.request(requestConfig);
    } catch {
      redirectToLogin();
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
