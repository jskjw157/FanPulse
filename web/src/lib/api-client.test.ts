import axios from "axios";
import { afterEach, describe, expect, it, vi } from "vitest";
import { API_BASE_URL, refreshWebSession } from "./api-client";

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
