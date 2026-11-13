import { describe, expect, it } from "vitest";
import {
  LocalApiTargetError,
  PRODUCTION_API_BASE_URL,
  isLoopbackHost,
  resolveApiBaseUrl,
} from "./api-base-url";

describe("resolveApiBaseUrl", () => {
  it("keeps an explicit same-origin path so the rewrite proxy answers", () => {
    expect(resolveApiBaseUrl("/api/v1", "localhost")).toEqual({
      ok: true,
      baseUrl: "/api/v1",
      source: "configured",
    });
  });

  it("accepts an explicit loopback API from a local page", () => {
    expect(resolveApiBaseUrl("http://127.0.0.1:8080/api/v1", "127.0.0.1")).toEqual({
      ok: true,
      baseUrl: "http://127.0.0.1:8080/api/v1",
      source: "configured",
    });
  });

  it("falls back to the same-origin proxy when nothing is configured locally", () => {
    expect(resolveApiBaseUrl(undefined, "localhost")).toEqual({
      ok: true,
      baseUrl: "/api/v1",
      source: "loopback-default",
    });
  });

  it("treats a blank value as unset", () => {
    expect(resolveApiBaseUrl("   ", "localhost")).toEqual({
      ok: true,
      baseUrl: "/api/v1",
      source: "loopback-default",
    });
  });

  it("keeps the production default when nothing is configured outside a browser", () => {
    expect(resolveApiBaseUrl(undefined, null)).toEqual({
      ok: true,
      baseUrl: PRODUCTION_API_BASE_URL,
      source: "production-default",
    });
  });

  it("keeps the production default for an unconfigured public host", () => {
    expect(resolveApiBaseUrl(undefined, "fanpulse.app")).toEqual({
      ok: true,
      baseUrl: PRODUCTION_API_BASE_URL,
      source: "production-default",
    });
  });

  it("keeps an explicit production API on a public host", () => {
    expect(resolveApiBaseUrl(PRODUCTION_API_BASE_URL, "fanpulse.app")).toEqual({
      ok: true,
      baseUrl: PRODUCTION_API_BASE_URL,
      source: "configured",
    });
  });

  it("refuses a remote API from a local page instead of leaking the request", () => {
    const result = resolveApiBaseUrl(PRODUCTION_API_BASE_URL, "localhost");
    expect(result.ok).toBe(false);
    if (result.ok) return;
    expect(result.reason).toBeInstanceOf(LocalApiTargetError);
    expect(result.reason.configured).toBe(PRODUCTION_API_BASE_URL);
    expect(result.reason.pageHost).toBe("localhost");
    expect(result.reason.message).toContain("NEXT_PUBLIC_API_BASE_URL=/api/v1");
  });

  it.each(["localhost", "127.0.0.1", "0.0.0.0", "::1", "[::1]", "app.localhost"])(
    "treats %s as the developer machine",
    (host) => {
      expect(isLoopbackHost(host)).toBe(true);
    }
  );

  it.each(["fanpulse.app", "api.fanpulse.app", "192.168.0.10"])(
    "treats %s as remote",
    (host) => {
      expect(isLoopbackHost(host)).toBe(false);
    }
  );
});
