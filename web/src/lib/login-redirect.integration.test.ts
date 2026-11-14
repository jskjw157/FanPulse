import axios, { AxiosError, type AxiosAdapter, type AxiosInstance } from "axios";
import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from "vitest";
import { SAFE_LOGIN_RETURNS, UNSAFE_LOGIN_RETURNS } from "./login-redirect.fixtures";

// Redirect-only integration coverage lives separately from api-client.test.ts,
// which owns refresh/session behavior. No request here can reach the network.
let apiClient: AxiosInstance;
let originalAdapter: AxiosInstance["defaults"]["adapter"];

beforeAll(async () => {
  vi.stubEnv("NEXT_PUBLIC_API_BASE_URL", "/api/v1");
  ({ apiClient } = await import("./api-client"));
  originalAdapter = apiClient.defaults.adapter;
});

afterEach(() => {
  vi.restoreAllMocks();
  vi.unstubAllGlobals();
});

afterAll(() => {
  apiClient.defaults.adapter = originalAdapter;
  vi.unstubAllEnvs();
});

async function expireSession(returnTo: string, refreshSucceeds: boolean) {
  const queryIndex = returnTo.indexOf("?");
  const location = {
    hostname: "localhost",
    pathname: queryIndex < 0 ? returnTo : returnTo.slice(0, queryIndex),
    search: queryIndex < 0 ? "" : returnTo.slice(queryIndex),
    href: "unchanged",
    replace: vi.fn(),
  };
  // A writable location records real interceptor navigation without jsdom's
  // unimplemented navigation or the browser normalizing hostile input first.
  vi.stubGlobal("window", { location });
  const refresh = vi.spyOn(axios, "post");
  if (refreshSucceeds) refresh.mockResolvedValue({});
  else refresh.mockRejectedValue(new Error("Session expired"));
  const adapter = vi.fn<AxiosAdapter>(async (config) => {
    throw new AxiosError("Unauthorized", "ERR_BAD_REQUEST", config, undefined, {
      config, data: {}, status: 401, statusText: "Unauthorized", headers: {},
    });
  });
  apiClient.defaults.adapter = adapter;

  await expect(apiClient.get("/protected-resource")).rejects.toMatchObject({ response: { status: 401 } });
  expect(refresh).toHaveBeenCalledTimes(1);
  expect(adapter).toHaveBeenCalledTimes(refreshSucceeds ? 2 : 1);
  return location;
}

describe.each([
  ["refresh failure", false],
  ["401 after successful refresh", true],
] as const)("login return on %s", (_reason, refreshSucceeds) => {
  it.each(SAFE_LOGIN_RETURNS)("preserves the supported destination %s", async (input, expected) => {
    const location = await expireSession(input, refreshSucceeds);
    expect(location.replace).toHaveBeenCalledExactlyOnceWith(`/login?redirect=${encodeURIComponent(expected)}`);
    expect(location.href).toBe("unchanged");
  });

  it.each(UNSAFE_LOGIN_RETURNS)("never forwards the unsafe destination %j", async (input) => {
    const location = await expireSession(input, refreshSucceeds);
    const alreadyOnLogin = location.pathname === "/login" || location.pathname.startsWith("/login/");
    if (alreadyOnLogin) expect(location.replace).not.toHaveBeenCalled();
    else expect(location.replace).toHaveBeenCalledExactlyOnceWith("/login");
    expect(location.href).toBe("unchanged");
  });
});
