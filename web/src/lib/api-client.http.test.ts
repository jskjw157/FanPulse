// @vitest-environment node

import { createServer, type Server } from "node:http";
import type { AddressInfo } from "node:net";
import type { AxiosInstance } from "axios";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

type Reply = {
  status: number;
  data?: unknown;
  headers?: Record<string, string>;
  disconnect?: boolean;
};

const expired: Reply = {
  status: 401,
  data: { error: "access-expired" },
  headers: { "WWW-Authenticate": "Bearer" },
};
const refreshPath = "/auth/web/refresh";
const initialPage = "/settings";

describe("401-only session recovery through the real Axios HTTP adapter", () => {
  let server: Server;
  let client: AxiosInstance;
  let location: { hostname: string; pathname: string; href: string };
  let replies: Map<string, Reply[]>;
  let requests: { method: string; path: string; body: string }[];
  let restoreDefaults: (() => void) | undefined;

  function plan(method: string, path: string, ...responses: Reply[]) {
    replies.set(`${method} /api/v1${path}`, responses);
  }

  function order() {
    return requests.map(({ method, path }) => `${method} ${path}`);
  }

  beforeEach(async () => {
    vi.resetModules();
    replies = new Map();
    requests = [];
    server = createServer(async (request, response) => {
      let body = "";
      for await (const chunk of request) body += chunk;
      const method = request.method ?? "GET";
      const path = request.url ?? "/";
      requests.push({ method, path, body });
      const reply = replies.get(`${method} ${path}`)?.shift();
      if (!reply) {
        response.writeHead(500, { "Content-Type": "application/json" });
        response.end(JSON.stringify({ error: `Unexpected request: ${method} ${path}` }));
        return;
      }
      if (reply.disconnect) {
        response.destroy();
        return;
      }
      response.writeHead(reply.status, { "Content-Type": "application/json", ...reply.headers });
      response.end(reply.data === undefined ? "" : JSON.stringify(reply.data));
    });
    await new Promise<void>((resolve, reject) => {
      server.once("error", reject);
      server.listen(0, "127.0.0.1", resolve);
    });
    const { port } = server.address() as AddressInfo;
    vi.stubEnv("NEXT_PUBLIC_API_BASE_URL", `http://127.0.0.1:${port}/api/v1`);
    location = { hostname: "127.0.0.1", pathname: initialPage, href: initialPage };
    // Only enable the browser-only interceptor/redirect branch; no browser or UI is launched.
    vi.stubGlobal("window", { location });
    // This is Axios's shipped Node adapter, not a custom adapter that manufactures AxiosErrors.
    // Cookie transport/rotation and genuine CORS rejection are covered by the backend HTTP suite.
    const axios = (await import("axios")).default;
    const originalAdapter = axios.defaults.adapter;
    const originalProxy = axios.defaults.proxy;
    restoreDefaults = () => {
      axios.defaults.adapter = originalAdapter;
      axios.defaults.proxy = originalProxy;
    };
    axios.defaults.adapter = "http";
    axios.defaults.proxy = false;
    client = (await import("./api-client")).apiClient;
  });

  afterEach(async () => {
    const closed = new Promise<void>((resolve, reject) => {
      server.close((error) => error ? reject(error) : resolve());
    });
    server.closeAllConnections();
    await closed;
    restoreDefaults?.();
    vi.unstubAllGlobals();
    vi.unstubAllEnvs();
    vi.resetModules();
  });

  it.each([
    "/me",
    "/me/settings",
    "/users/me/favorites",
    "/users/me/notifications",
    "/users/me/notifications/page",
    "/users/me/notifications/unread-count",
  ])("refreshes once then retries %s after a wire-level 401", async (path) => {
    const recovered = { recovered: path };
    plan("GET", path, expired, { status: 200, data: recovered });
    plan("POST", refreshPath, { status: 204 });

    await expect(client.get(path)).resolves.toMatchObject({ status: 200, data: recovered });

    expect(order()).toEqual([`GET /api/v1${path}`, `POST /api/v1${refreshPath}`, `GET /api/v1${path}`]);
    expect(location.href).toBe(initialPage);
  });

  it("preserves a settings mutation method and body across its single retry", async () => {
    const update = { language: "en" };
    plan("PATCH", "/me/settings", expired, { status: 200, data: update });
    plan("POST", refreshPath, { status: 204 });

    await expect(client.patch("/me/settings", update)).resolves.toMatchObject({ status: 200, data: update });

    expect(order()).toEqual(["PATCH /api/v1/me/settings", `POST /api/v1${refreshPath}`, "PATCH /api/v1/me/settings"]);
    expect(requests[0].body).toBe(JSON.stringify(update));
    expect(requests[2].body).toBe(requests[0].body);
    expect(location.href).toBe(initialPage);
  });

  it.each([401, 403, 500])("does not retry the protected request when refresh fails with %s", async (status) => {
    plan("GET", "/me", expired);
    plan("POST", refreshPath, { status, data: { error: "refresh-failed" } });

    await expect(client.get("/me")).rejects.toMatchObject({
      response: { status: 401, data: { error: "access-expired" } },
    });

    expect(order()).toEqual(["GET /api/v1/me", `POST /api/v1${refreshPath}`]);
    // Assert the boundary, not the redirect URL, which is owned by the login/navigation work.
    expect(location.href).not.toBe(initialPage);
  });

  it("does not retry the protected request after a refresh network failure", async () => {
    plan("GET", "/me", expired);
    plan("POST", refreshPath, { status: 0, disconnect: true });

    await expect(client.get("/me")).rejects.toMatchObject({
      response: { status: 401, data: { error: "access-expired" } },
    });

    expect(order()).toEqual(["GET /api/v1/me", `POST /api/v1${refreshPath}`]);
    expect(location.href).not.toBe(initialPage);
  });

  it("does not refresh or retry again when the retried request is still 401", async () => {
    plan("GET", "/me/settings", expired, { ...expired, data: { error: "still-unauthorized" } });
    plan("POST", refreshPath, { status: 204 });

    await expect(client.get("/me/settings")).rejects.toMatchObject({
      response: { status: 401, data: { error: "still-unauthorized" } },
    });

    expect(order()).toEqual(["GET /api/v1/me/settings", `POST /api/v1${refreshPath}`, "GET /api/v1/me/settings"]);
    expect(location.href).not.toBe(initialPage);
  });

  it("passes a retried 403 through without a second refresh or login redirect", async () => {
    plan("GET", "/me/settings", expired, { status: 403, data: { error: "forbidden" } });
    plan("POST", refreshPath, { status: 204 });

    await expect(client.get("/me/settings")).rejects.toMatchObject({ response: { status: 403 } });

    expect(order()).toEqual(["GET /api/v1/me/settings", `POST /api/v1${refreshPath}`, "GET /api/v1/me/settings"]);
    expect(location.href).toBe(initialPage);
  });

  it.each([
    { path: "/admin/comment-reviews", data: { error: "insufficient-permission" } },
    { path: "/me/settings", data: "Invalid CORS request" },
  ])("never refreshes a forbidden or CORS 403 from $path", async ({ path, data }) => {
    plan("GET", path, { status: 403, data });

    await expect(client.get(path)).rejects.toMatchObject({ response: { status: 403, data } });

    expect(order()).toEqual([`GET /api/v1${path}`]);
    expect(location.href).toBe(initialPage);
  });

  it.each(["/auth/google", "/auth/refresh", "/auth/web/refresh", "/auth/logout"])(
    "does not recursively refresh an excluded auth endpoint %s",
    async (path) => {
      plan("POST", path, expired);

      await expect(client.post(path)).rejects.toMatchObject({ response: { status: 401 } });

      expect(order()).toEqual([`POST /api/v1${path}`]);
      expect(location.href).toBe(initialPage);
    }
  );

  it("keeps the public auth-status 200 false response without trying refresh", async () => {
    plan("GET", "/auth/me", { status: 200, data: { authenticated: false } });

    await expect(client.get("/auth/me")).resolves.toMatchObject({ status: 200, data: { authenticated: false } });

    expect(order()).toEqual(["GET /api/v1/auth/me"]);
    expect(location.href).toBe(initialPage);
  });

  it("does not treat a network failure without an HTTP status as an expired session", async () => {
    plan("GET", "/me", { status: 0, disconnect: true });

    await expect(client.get("/me")).rejects.toMatchObject({ code: "ECONNRESET" });

    expect(order()).toEqual(["GET /api/v1/me"]);
    expect(location.href).toBe(initialPage);
  });
});
