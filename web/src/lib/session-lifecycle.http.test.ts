// @vitest-environment node

import { execFileSync } from "node:child_process";
import { readFileSync } from "node:fs";
import { createServer, type Server } from "node:http";
import type { AddressInfo } from "node:net";
import { fileURLToPath } from "node:url";
import { createContext, runInContext } from "node:vm";
import axios, { AxiosHeaders, type AxiosAdapter } from "axios";
import ts from "typescript";
import { afterEach, beforeEach, describe, expect, it } from "vitest";

function deferred() {
  let resolve!: () => void;
  const promise = new Promise<void>((done) => { resolve = done; });
  return { promise, resolve };
}

// FIFO origin-wide Web Locks stand-in, shared by otherwise separate JS realms.
// No storage lease, token grace period, timers, browser or network service is used.
class BrowserLocks {
  private tail: Promise<unknown> = Promise.resolve();
  private pending = 0;
  readonly queued = deferred();

  request<T>(_name: string, callback: () => Promise<T>): Promise<T> {
    if (this.pending++) this.queued.resolve();
    const result = this.tail.then(callback);
    this.tail = result.then(
      () => { --this.pending; },
      () => { --this.pending; },
    );
    return result;
  }
}

type Realm = {
  api: typeof import("./api-client");
  auth: typeof import("./auth");
  location: { hostname: string; pathname: string; search: string; href: string };
};

describe("browser-wide cookie session lifecycle through public APIs and deferred HTTP", () => {
  let server: Server;
  let baseURL: string;
  let locks: BrowserLocks;
  let storage: Map<string, string>;
  let cookies: Map<string, string>;
  let consumed: Set<string>;
  let revoked: Set<string>;
  let requests: { path: string; refresh: string | undefined }[];
  let refreshArrived: ReturnType<typeof deferred>;
  let releaseRefresh: ReturnType<typeof deferred>;
  let statusArrived: ReturnType<typeof deferred>;
  let releaseStatus: ReturnType<typeof deferred>;
  let protectedArrived: ReturnType<typeof deferred>;
  let releaseProtected: ReturnType<typeof deferred>;
  let logoutArrived: ReturnType<typeof deferred>;
  let releaseLogout: ReturnType<typeof deferred>;
  let delayStatus: boolean;
  let delayProtected: boolean;
  let delayLogout: boolean;
  let refreshFailure: boolean;
  let logoutFailure: boolean;
  let reuseCount: number;
  let restoreAdapter: () => void;

  function count(path: string) {
    return requests.filter((request) => request.path === path).length;
  }

  // Evaluate the actual TS sources in independent VM contexts: separate module
  // single-flights/globals, shared origin storage, Web Locks and browser cookie jar.
  // Only Axios's cookie transport is supplied; the real Node HTTP adapter handles
  // status codes, requests, response ordering and all deferred server responses.
  function realm(): Realm {
    const location = { hostname: "127.0.0.1", pathname: "/settings", search: "", href: "/settings" };
    const context = createContext({
      window: {
        location,
        navigator: { locks },
        localStorage: {
          getItem: (key: string) => storage.get(key) ?? null,
          setItem: (key: string, value: string) => { storage.set(key, value); },
        },
      },
      navigator: { locks },
      process: { env: { NEXT_PUBLIC_API_BASE_URL: baseURL } },
      console,
      URL,
      URLSearchParams,
    });
    const modules = new Map<string, { exports: unknown }>();
    function load(name: string): unknown {
      if (name === "axios") return axios;
      const cached = modules.get(name);
      if (cached) return cached.exports;
      // Optional read-only baseline replay: same tests, HTTP and realm harness,
      // but load source from a Git revision without checking out or editing files.
      const sourceRef = process.env.FANPULSE_SESSION_SOURCE_REF;
      const source = sourceRef
        ? execFileSync("git", ["show", `${sourceRef}:web/src/lib/${name.slice(2)}.ts`], {
          cwd: fileURLToPath(new URL("../../../", import.meta.url)), encoding: "utf8",
        })
        : readFileSync(new URL(`${name}.ts`, import.meta.url), "utf8");
      const { outputText } = ts.transpileModule(source, {
        compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022, esModuleInterop: true },
      });
      const loadedModule = { exports: {} };
      modules.set(name, loadedModule);
      const evaluate = runInContext(`(function(require, module, exports) { ${outputText}\n})`, context);
      evaluate(load, loadedModule, loadedModule.exports);
      return loadedModule.exports;
    }
    return {
      api: load("./api-client") as Realm["api"],
      auth: load("./auth") as Realm["auth"],
      location,
    };
  }

  beforeEach(async () => {
    locks = new BrowserLocks();
    storage = new Map();
    cookies = new Map([["fanpulse_access_token", "expired"], ["fanpulse_refresh_token", "R0"]]);
    consumed = new Set();
    revoked = new Set();
    requests = [];
    refreshArrived = deferred();
    releaseRefresh = deferred();
    statusArrived = deferred();
    releaseStatus = deferred();
    protectedArrived = deferred();
    releaseProtected = deferred();
    logoutArrived = deferred();
    releaseLogout = deferred();
    delayStatus = delayProtected = delayLogout = refreshFailure = logoutFailure = false;
    reuseCount = 0;

    server = createServer(async (request, response) => {
      for await (const chunk of request) void chunk;
      const path = (request.url ?? "").replace("/api/v1", "");
      const incoming = new Map((request.headers.cookie ?? "").split("; ").map((item) => item.split("=") as [string, string]));
      const refresh = incoming.get("fanpulse_refresh_token");
      const authenticated = incoming.get("fanpulse_access_token") === "A1";
      requests.push({ path, refresh });
      const reply = (status: number, data?: unknown, setCookies?: string[]) => {
        response.writeHead(status, { "Content-Type": "application/json", ...(setCookies ? { "Set-Cookie": setCookies } : {}) });
        response.end(data === undefined ? "" : JSON.stringify(data));
      };
      if (path === "/auth/me") {
        statusArrived.resolve();
        if (delayStatus) await releaseStatus.promise;
        reply(200, { authenticated, ...(authenticated ? { user: { id: "user-1", email: "user@example.com" } } : {}) });
      } else if (path === "/auth/web/refresh") {
        if (consumed.has(refresh ?? "")) {
          // Match the backend's deliberate theft/reuse response, never weaken it.
          ++reuseCount;
          revoked.add("R1");
          reply(401);
        } else if (!refresh || revoked.has(refresh) || refreshFailure) {
          reply(refreshFailure ? 503 : 401);
        } else {
          consumed.add(refresh);
          refreshArrived.resolve();
          await releaseRefresh.promise; // R1 issued, Set-Cookie not delivered yet.
          reply(204, undefined, ["fanpulse_access_token=A1; HttpOnly; Path=/", "fanpulse_refresh_token=R1; HttpOnly; Path=/"]);
        }
      } else if (path === "/auth/logout") {
        logoutArrived.resolve();
        if (delayLogout) await releaseLogout.promise;
        if (logoutFailure) reply(503);
        else {
          if (refresh) revoked.add(refresh);
          reply(200, undefined, ["fanpulse_access_token=; Max-Age=0; Path=/", "fanpulse_refresh_token=; Max-Age=0; Path=/"]);
        }
      } else if (path === "/auth/google") {
        reply(200, { userId: "user-1", email: "user@example.com", username: "user" }, ["fanpulse_access_token=A1; Path=/", "fanpulse_refresh_token=R1; Path=/"]);
      } else if (path === "/me/settings") {
        protectedArrived.resolve();
        if (delayProtected) await releaseProtected.promise;
        reply(authenticated ? 200 : 401, authenticated ? { language: "ko" } : { error: "expired" });
      } else reply(500, { error: `Unexpected path ${path}` });
    });
    await new Promise<void>((resolve) => { server.listen(0, "127.0.0.1", resolve); });
    baseURL = `http://127.0.0.1:${(server.address() as AddressInfo).port}/api/v1`;
    const originalAdapter = axios.defaults.adapter;
    const originalProxy = axios.defaults.proxy;
    const http = axios.getAdapter("http");
    const browserTransport: AxiosAdapter = async (config) => {
      config.headers = AxiosHeaders.from(config.headers);
      config.headers.set("Cookie", [...cookies].map(([key, value]) => `${key}=${value}`).join("; "));
      // Failure responses in this fixture intentionally do not set cookies.
      const result = await http(config);
      for (const cookie of result.headers["set-cookie"] ?? []) {
        const [key, value] = cookie.split(";", 1)[0].split("=");
        if (value) cookies.set(key, value);
        else cookies.delete(key);
      }
      return result;
    };
    axios.defaults.adapter = browserTransport;
    axios.defaults.proxy = false;
    restoreAdapter = () => {
      axios.defaults.adapter = originalAdapter;
      axios.defaults.proxy = originalProxy;
    };
  });

  afterEach(async () => {
    // Unblock and disconnect even on assertion failure; never leave a held socket.
    releaseRefresh.resolve();
    releaseStatus.resolve();
    releaseProtected.resolve();
    releaseLogout.resolve();
    const closed = new Promise<void>((resolve, reject) => {
      server.close((error) => error ? reject(error) : resolve());
    });
    server.closeAllConnections();
    await closed;
    restoreAdapter();
  });

  it("coalesces R0 recovery across independent JS realms without triggering backend reuse revocation", async () => {
    const a = realm();
    const b = realm();
    const first = a.api.refreshWebSession();
    await refreshArrived.promise;
    const second = b.api.refreshWebSession();
    const completed = Promise.allSettled([first, second]);
    releaseRefresh.resolve();

    expect((await completed).map((result) => result.status)).toEqual(["fulfilled", "fulfilled"]);
    expect(count("/auth/web/refresh")).toBe(1);
    expect(reuseCount).toBe(0);
    expect(revoked.has("R1")).toBe(false);
    expect((await b.auth.checkAuthStatus()).authenticated).toBe(true);
  });

  it("drains delayed refresh Set-Cookie before successful logout clears and revokes R1", async () => {
    const a = realm();
    const b = realm();
    const refreshing = a.api.refreshWebSession().catch(() => undefined);
    await refreshArrived.promise;
    const loggingOut = b.auth.logout();
    // On baseline, logout finishes while refresh is held; with the fix it queues.
    await Promise.race([loggingOut, locks.queued.promise]);
    releaseRefresh.resolve();
    await Promise.all([refreshing, loggingOut]);

    expect(requests.find((request) => request.path === "/auth/logout")?.refresh).toBe("R1");
    expect(revoked.has("R1")).toBe(true);
    expect(cookies.size).toBe(0);
  });

  it.each([false, true])("fences a delayed auth/me authenticated=%s response from before logout", async (authenticated) => {
    if (authenticated) cookies.set("fanpulse_access_token", "A1");
    const a = realm();
    const b = realm();
    delayStatus = true;
    const checking = a.auth.checkAuthStatus();
    await statusArrived.promise;
    await b.auth.logout();
    releaseStatus.resolve();

    expect(await checking).toEqual({ authenticated: false });
    expect(count("/auth/web/refresh")).toBe(0);
  });

  it("does not refresh, replay or redirect a late old-session 401 after logout and a new login", async () => {
    const a = realm();
    const b = realm();
    delayProtected = true;
    const failed = a.api.apiClient.get("/me/settings").catch((error) => error);
    await protectedArrived.promise;
    await b.auth.logout();
    await b.auth.loginWithGoogle({ idToken: "test-id-token" });
    releaseProtected.resolve();
    releaseRefresh.resolve();

    expect(await failed).toMatchObject({ response: { status: 401 } });
    expect(count("/auth/web/refresh")).toBe(0);
    expect(count("/me/settings")).toBe(1);
    expect(a.location.href).toBe("/settings");
  });

  it("fences protected retries and redirects when logout begins during their refresh", async () => {
    const a = realm();
    const b = realm();
    const failed = a.api.apiClient.get("/me/settings").catch((error) => error);
    await refreshArrived.promise;
    const loggingOut = b.auth.logout();
    await Promise.race([loggingOut, locks.queued.promise]);
    releaseRefresh.resolve();
    await loggingOut;

    expect(await failed).toMatchObject({ response: { status: 401 } });
    expect(count("/me/settings")).toBe(1);
    expect(a.location.href).toBe("/settings");
    expect(cookies.size).toBe(0);
  });

  it("does not publish authenticated recovery after logout starts during its refresh", async () => {
    const a = realm();
    const b = realm();
    const checking = a.auth.checkAuthStatus();
    await refreshArrived.promise;
    const loggingOut = b.auth.logout();
    await Promise.race([loggingOut, locks.queued.promise]);
    releaseRefresh.resolve();
    await loggingOut;

    expect(await checking).toEqual({ authenticated: false });
    expect(cookies.size).toBe(0);
  });

  it("fences a retry when logout occurs between scheduling and request-interceptor dispatch", async () => {
    const a = realm();
    const b = realm();
    let dispatches = 0;
    a.api.apiClient.interceptors.request.use(async (config) => {
      if (++dispatches === 2) await b.auth.logout();
      return config;
    });
    releaseRefresh.resolve();

    await expect(a.api.apiClient.get("/me/settings")).rejects.toMatchObject({ response: { status: 401 } });
    expect(count("/me/settings")).toBe(1);
    expect(a.location.href).toBe("/settings");
    expect(cookies.size).toBe(0);
  });

  it("rechecks the generation after waiting for an in-progress logout", async () => {
    const a = realm();
    const b = realm();
    delayLogout = true;
    const loggingOut = b.auth.logout();
    await logoutArrived.promise;
    const refreshing = a.api.refreshWebSession();
    const completed = Promise.allSettled([loggingOut, refreshing]);
    releaseLogout.resolve();
    releaseRefresh.resolve();

    const results = await completed;
    expect(results[0].status).toBe("fulfilled");
    expect(results[1].status).toBe("rejected");
    expect(count("/auth/web/refresh")).toBe(0);
    expect(cookies.size).toBe(0);
  });

  it("releases failed refresh/logout work so later recovery and explicit logout remain retryable", async () => {
    const a = realm();
    const b = realm();
    refreshFailure = true;
    await expect(a.api.refreshWebSession()).rejects.toMatchObject({ response: { status: 503 } });
    logoutFailure = true;
    await expect(b.auth.logout()).rejects.toMatchObject({ status: 503 });
    expect(cookies.get("fanpulse_refresh_token")).toBe("R0");
    refreshFailure = logoutFailure = false;
    releaseRefresh.resolve();

    expect((await a.auth.checkAuthStatus()).authenticated).toBe(true);
    await b.auth.logout();
    expect(count("/auth/web/refresh")).toBe(2);
    expect(count("/auth/logout")).toBe(2);
    expect(cookies.size).toBe(0);
  });
});
