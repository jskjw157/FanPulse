import axios from "axios";
import { afterEach, describe, expect, it, vi } from "vitest";
import { API_BASE_URL, refreshWebSession } from "./api-client";

describe("local API target guard", () => {
  afterEach(() => {
    vi.unstubAllEnvs();
    vi.resetModules();
    vi.restoreAllMocks();
  });

  it("defaults to the same-origin proxy when the build had no API base URL", async () => {
    vi.resetModules();
    vi.stubEnv("NEXT_PUBLIC_API_BASE_URL", "");
    const mod = await import("./api-client");

    expect(mod.API_BASE_URL).toBe("/api/v1");
    expect(mod.API_TARGET_BLOCKED_REASON).toBeNull();
  });

  it("blocks a remote API target on a local page before touching the network", async () => {
    vi.resetModules();
    vi.stubEnv("NEXT_PUBLIC_API_BASE_URL", "https://api.fanpulse.app/api/v1");
    const consoleError = vi.spyOn(console, "error").mockImplementation(() => {});
    const openSpy = vi.spyOn(XMLHttpRequest.prototype, "open");
    const mod = await import("./api-client");

    expect(mod.API_TARGET_BLOCKED_REASON).toBeInstanceOf(mod.LocalApiTargetError);
    await expect(mod.apiClient.get("/me")).rejects.toBeInstanceOf(mod.LocalApiTargetError);
    await expect(mod.refreshWebSession()).rejects.toBeInstanceOf(mod.LocalApiTargetError);
    expect(openSpy).not.toHaveBeenCalled();
    expect(consoleError).toHaveBeenCalledWith(
      expect.stringContaining("NEXT_PUBLIC_API_BASE_URL=/api/v1")
    );
  });
});

describe("refreshWebSession", () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("동시에 호출돼도 웹 갱신 요청을 한 번만 실행한다", async () => {
    let resolveRequest: (() => void) | undefined;
    const pendingRequest = new Promise<void>((resolve) => {
      resolveRequest = resolve;
    });
    const postSpy = vi
      .spyOn(axios, "post")
      .mockReturnValue(pendingRequest as never);

    const first = refreshWebSession();
    const second = refreshWebSession();

    expect(first).toBe(second);
    expect(postSpy).toHaveBeenCalledTimes(1);
    expect(postSpy).toHaveBeenCalledWith(
      `${API_BASE_URL.replace(/\/$/, "")}/auth/web/refresh`,
      undefined,
      expect.objectContaining({
        withCredentials: true,
        timeout: 30000,
      })
    );

    resolveRequest?.();
    await Promise.all([first, second]);
  });

  it("갱신 실패 후에는 다음 호출에서 새 요청을 실행한다", async () => {
    const postSpy = vi
      .spyOn(axios, "post")
      .mockRejectedValueOnce(new Error("refresh failed"))
      .mockResolvedValueOnce({} as never);

    await expect(refreshWebSession()).rejects.toThrow("refresh failed");
    await expect(refreshWebSession()).resolves.toBeUndefined();

    expect(postSpy).toHaveBeenCalledTimes(2);
  });
});
