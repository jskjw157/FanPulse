import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("./api-client", () => ({
  apiClient: {
    get: vi.fn(),
    post: vi.fn(),
  },
  refreshWebSession: vi.fn(),
  toFanPulseError: vi.fn((error: unknown) => error),
  FanPulseApiError: class FanPulseApiError extends Error {},
}));

import { apiClient, refreshWebSession } from "./api-client";
import { checkAuthStatus, logout } from "./auth";

describe("auth client", () => {
  beforeEach(() => {
    vi.clearAllMocks();
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

  it("로그아웃 API가 실패해도 클라이언트 로그아웃 흐름은 완료한다", async () => {
    const consoleSpy = vi
      .spyOn(console, "error")
      .mockImplementation(() => undefined);
    vi.mocked(apiClient.post).mockRejectedValueOnce(
      new Error("network failed")
    );

    await expect(logout()).resolves.toBeUndefined();

    expect(apiClient.post).toHaveBeenCalledWith("/auth/logout");
    expect(consoleSpy).toHaveBeenCalledTimes(1);
    consoleSpy.mockRestore();
  });
});
