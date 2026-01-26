import axios, { AxiosError, AxiosInstance } from "axios";

export const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "https://api.fanpulse.app/api/v1";

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
  withCredentials: true, // httpOnly 쿠키 자동 전송
});

// Response 인터셉터: 401 에러 처리
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      // 인증 만료 시 로그인 페이지로 리다이렉트
      if (typeof window !== "undefined") {
        window.location.href = "/login";
      }
    }
    return Promise.reject(error);
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
