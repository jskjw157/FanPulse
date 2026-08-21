import {
  apiClient,
  FanPulseApiError,
  refreshWebSession,
  toFanPulseError,
} from "./api-client";

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
 * - 백엔드에서 현재 Refresh Token을 무효화하고 인증 쿠키를 삭제
 */
export async function logout(): Promise<void> {
  try {
    await apiClient.post("/auth/logout");
  } catch (error) {
    // 서버 요청이 실패해도 클라이언트 인증 상태는 제거한다.
    console.error("Logout failed:", error);
  }
}

async function requestAuthStatus(): Promise<AuthStatusResponse> {
  const response = await apiClient.get<AuthStatusResponse>("/auth/me");
  return response.data;
}

/**
 * 인증 상태 확인
 *
 * Access Cookie가 만료되어 `/auth/me`가 비인증 상태를 반환하면
 * httpOnly Refresh Cookie로 세션을 한 번 갱신한 뒤 상태를 다시 확인한다.
 */
export async function checkAuthStatus(): Promise<AuthStatusResponse> {
  try {
    const current = await requestAuthStatus();
    if (current.authenticated) {
      return current;
    }

    try {
      await refreshWebSession();
      return await requestAuthStatus();
    } catch {
      return current;
    }
  } catch {
    return { authenticated: false };
  }
}
