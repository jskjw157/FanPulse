import { apiClient, FanPulseApiError, toFanPulseError } from "./api-client";

// Re-export for backward compatibility
export { FanPulseApiError } from "./api-client";

/**
 * 인증 관련 타입
 */
export interface AuthUser {
  id: string;
  email: string;
  username?: string;
}

export interface GoogleLoginResponse {
  userId: string;
  email: string;
  username: string;
}

export interface AuthStatusResponse {
  authenticated: boolean;
  user?: AuthUser;
}

/**
 * Google OAuth 로그인
 * - 백엔드에서 httpOnly 쿠키로 토큰 설정
 */
export async function loginWithGoogle(params: {
  idToken: string;
}): Promise<GoogleLoginResponse> {
  try {
    const response = await apiClient.post<GoogleLoginResponse>(
      "/auth/google",
      params
    );
    return response.data;
  } catch (error) {
    throw toFanPulseError(error);
  }
}

/**
 * 로그아웃
 * - 백엔드에서 쿠키 삭제
 */
export async function logout(): Promise<void> {
  try {
    await apiClient.post("/auth/logout");
  } catch (error) {
    // 로그아웃 실패해도 클라이언트에서는 로그아웃 처리
    console.error("Logout failed:", error);
  }
}

/**
 * 인증 상태 확인
 * - 서버에서 쿠키 기반으로 인증 상태 반환
 */
export async function checkAuthStatus(): Promise<AuthStatusResponse> {
  try {
    const response = await apiClient.get<AuthStatusResponse>("/auth/me");
    return response.data;
  } catch (error) {
    // 401이면 인증되지 않은 상태
    return { authenticated: false };
  }
}
