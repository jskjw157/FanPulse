import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("./api-client", () => ({
  apiClient: {
    get: vi.fn(),
    post: vi.fn(),
  },
  getWebSessionGeneration: vi.fn(() => "test-session"),
  refreshWebSession: vi.fn(),
  runWebSessionMutation: vi.fn((operation: () => Promise<unknown>) => operation()),
  toFanPulseError: vi.fn((error: unknown) => error),
  FanPulseApiError: class FanPulseApiError extends Error {},
}));

import { apiClient, refreshWebSession } from "./api-client";
import { checkAuthStatus, loginWithGoogle, logout } from "./auth";
import { SESSION_CHANGED } from "./session-sync";

describe("auth client", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  it("같은 계정의 성공 login writer도 통지하지만 읽기와 실패 login은 통지하지 않는다", async () => {
    const user = { id: "same-account", email: "same@example.test", username: "Same" };
    vi.mocked(apiClient.post).mockResolvedValue({ data: { userId: user.id, ...user } } as never);
    await loginWithGoogle({ idToken: "synthetic-first" });
    const first = localStorage.getItem(SESSION_CHANGED);
    expect(first).not.toBeNull();
    await loginWithGoogle({ idToken: "synthetic-second" });
    const second = localStorage.getItem(SESSION_CHANGED);
    expect(second).not.toBe(first);

    vi.mocked(apiClient.get).mockResolvedValue({ data: { authenticated: true, user } } as never);
    await expect(checkAuthStatus()).resolves.toEqual({ authenticated: true, user });
    expect(localStorage.getItem(SESSION_CHANGED)).toBe(second);
    vi.mocked(apiClient.post).mockRejectedValueOnce(new Error("login failed"));
    await expect(loginWithGoogle({ idToken: "synthetic-failure" })).rejects.toThrow("login failed");
    expect(localStorage.getItem(SESSION_CHANGED)).toBe(second);
  });

  it("이미 인증된 상태면 Refresh Cookie를 사용하지 않는다", async () => {
    const authenticated = {
      authenticated: true,
      user: {
        id: "11111111-1111-1111-1111-111111111111",
        email: "user@example.com",
        username: "tester",
      },
    };
    vi.mocked(apiClient.get).mockResolvedValueOnce({
      data: authenticated,
    } as never);

    await expect(checkAuthStatus()).resolves.toEqual(authenticated);

    expect(refreshWebSession).not.toHaveBeenCalled();
    expect(apiClient.get).toHaveBeenCalledTimes(1);
  });

  it("Access Cookie가 만료되면 웹 세션을 갱신한 뒤 인증 상태를 다시 조회한다", async () => {
    const authenticated = {
      authenticated: true,
      user: {
        id: "22222222-2222-2222-2222-222222222222",
        email: "restored@example.com",
        username: "restored",
      },
    };
    vi.mocked(apiClient.get)
      .mockResolvedValueOnce({
        data: { authenticated: false },
      } as never)
      .mockResolvedValueOnce({
        data: authenticated,
      } as never);
    vi.mocked(refreshWebSession).mockResolvedValueOnce(undefined);

    await expect(checkAuthStatus()).resolves.toEqual(authenticated);

    expect(refreshWebSession).toHaveBeenCalledTimes(1);
    expect(apiClient.get).toHaveBeenCalledTimes(2);
  });

  it("Refresh Cookie도 유효하지 않으면 최초 비인증 상태를 유지한다", async () => {
    const unauthenticated = { authenticated: false };
    vi.mocked(apiClient.get).mockResolvedValueOnce({
      data: unauthenticated,
    } as never);
    vi.mocked(refreshWebSession).mockRejectedValueOnce(
      new Error("refresh expired")
    );

    await expect(checkAuthStatus()).resolves.toEqual(unauthenticated);

    expect(apiClient.get).toHaveBeenCalledTimes(1);
  });

  it("로그아웃 실패를 호출자에게 전달해 성공으로 표시하지 않는다", async () => {
    vi.mocked(apiClient.post).mockRejectedValueOnce(new Error("network failed"));
    await expect(logout()).rejects.toThrow("network failed");
  });
});
