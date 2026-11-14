import { expect, test, type BrowserContext, type Page, type Route } from '@playwright/test';

test.use({ serviceWorkers: 'block' });

const lockName = 'fanpulse:web-cookie-session';
const accessCookie = 'fanpulse_access_token';
const refreshCookie = 'fanpulse_refresh_token';
const syntheticUser = {
  id: '31700000-0000-4000-8000-000000000001',
  email: 'session-317@example.test',
  username: 'Session_317',
};
const settings = {
  theme: 'light', language: 'ko', pushEnabled: true, updatedAt: '2026-11-01T00:00:00Z',
};

function deferred() {
  let resolve!: () => void;
  const promise = new Promise<void>((done) => { resolve = done; });
  return { promise, resolve };
}

/**
 * Only the local API is fake. The built/dev Next app, Axios XHR transport, two
 * documents, navigator.locks, localStorage and HttpOnly browser cookies are real.
 * Never inject auth/lock/client implementations, and never contact external APIs.
 */
async function mockCookieBackend(context: BrowserContext, baseURL: string) {
  const origin = new URL(baseURL).origin;
  expect(['127.0.0.1', 'localhost', '[::1]']).toContain(new URL(origin).hostname);
  const releaseRefresh = deferred();
  const events: { event: string; path: string; method?: string; refresh?: string; access?: string }[] = [];
  const refreshPosts: string[] = [];
  const logoutPosts: string[] = [];
  const unexpected: string[] = [];
  const blockedExternal: string[] = [];
  const consumed = new Set<string>();
  const revoked = new Set<string>();
  let activeRefresh: string | null = 'R0';
  let rotations = 0;
  let reuseCount = 0;
  let acceptedMutations = 0;

  const authHeaders = (access: string, refresh: string, clear = false) => ({
    // Playwright splits newline-delimited Set-Cookie into separate Chromium
    // response headers. Comma-joining would not represent two real cookies.
    'set-cookie': [
      `${accessCookie}=${access}; Path=/; HttpOnly; SameSite=Lax${clear ? '; Max-Age=0' : ''}`,
      `${refreshCookie}=${refresh}; Path=/; HttpOnly; SameSite=Lax${clear ? '; Max-Age=0' : ''}`,
    ].join('\n'),
    'cache-control': 'no-store',
  });
  const expired = (route: Route) => route.fulfill({ status: 401, json: { error: 'synthetic-access-expired' } });

  await context.route('**/*', async (route) => {
    const request = route.request();
    const url = new URL(request.url());
    if (url.origin !== origin) {
      blockedExternal.push(request.url());
      return route.abort('blockedbyclient');
    }
    if (!url.pathname.startsWith('/api/')) return route.continue();
    const path = url.pathname.replace('/api/v1', '');
    const cookies = new Map((await request.headerValue('cookie') ?? '').split(/;\s*/).map((part) => {
      const separator = part.indexOf('=');
      return [part.slice(0, separator), part.slice(separator + 1)] as const;
    }));
    const refresh = cookies.get(refreshCookie) ?? '';
    const access = cookies.get(accessCookie) ?? '';
    const authenticated = /^A\d+$/.test(access);
    events.push({ event: 'request', path, method: request.method(), refresh, access });

    if (path === '/auth/me' && request.method() === 'GET') {
      return route.fulfill({ headers: { 'cache-control': 'no-store' }, json: {
        authenticated, ...(authenticated ? { user: syntheticUser } : {}),
      } });
    }
    if (path === '/auth/web/refresh' && request.method() === 'POST') {
      refreshPosts.push(refresh);
      if (refresh && consumed.has(refresh)) {
        // Preserve the backend's deliberate reuse/theft revocation semantics.
        ++reuseCount;
        if (activeRefresh) revoked.add(activeRefresh);
        activeRefresh = null;
        return route.fulfill({ status: 401, headers: authHeaders('', '', true) });
      }
      if (!refresh || refresh !== activeRefresh || revoked.has(refresh)) {
        return route.fulfill({ status: 401, headers: authHeaders('', '', true) });
      }
      consumed.add(refresh);
      const next = ++rotations;
      activeRefresh = `R${next}`;
      // Rotation has issued R1, but Chromium has not received its Set-Cookie.
      await releaseRefresh.promise;
      events.push({ event: 'refresh-cookie-response', path, refresh: `R${next}` });
      return route.fulfill({ status: 204, headers: authHeaders(`A${next}`, `R${next}`) });
    }
    if (path === '/auth/logout' && request.method() === 'POST') {
      logoutPosts.push(refresh);
      if (refresh) revoked.add(refresh);
      if (activeRefresh === refresh) activeRefresh = null;
      events.push({ event: 'logout-clear-response', path, refresh });
      return route.fulfill({ status: 200, headers: authHeaders('', '', true), json: {} });
    }
    if (path === '/me/settings') {
      if (!authenticated) return expired(route);
      if (request.method() === 'PATCH') {
        ++acceptedMutations;
        return route.fulfill({ json: { ...settings, ...request.postDataJSON() } });
      }
      return route.fulfill({ json: settings });
    }
    if (path === '/users/me/notifications/unread-count') {
      if (!authenticated) return expired(route);
      return route.fulfill({ json: { success: true, data: { unreadCount: 0 } } });
    }
    unexpected.push(`${request.method()} ${path}`);
    return route.fulfill({ status: 501, json: { unexpectedEndpoint: path } });
  });

  return {
    origin,
    events,
    refreshPosts,
    logoutPosts,
    unexpected,
    releaseRefresh: releaseRefresh.resolve,
    async seed(access: string) {
      await context.addCookies([
        { name: accessCookie, value: access, url: origin, httpOnly: true, sameSite: 'Lax' },
        { name: refreshCookie, value: 'R0', url: origin, httpOnly: true, sameSite: 'Lax' },
      ]);
    },
    async cookies() {
      return (await context.cookies(origin)).filter(({ name }) => name === accessCookie || name === refreshCookie);
    },
    snapshot() {
      return { events, refreshPosts, logoutPosts, reuseCount, activeRefresh, revoked: [...revoked], acceptedMutations, unexpected, blockedExternal };
    },
    async dispose() {
      releaseRefresh.resolve();
      await context.unrouteAll({ behavior: 'wait' });
    },
  };
}

async function locks(page: Page) {
  return page.evaluate(async (name) => {
    const snapshot = await navigator.locks.query();
    return {
      held: snapshot.held?.filter((lock) => lock.name === name) ?? [],
      pending: snapshot.pending?.filter((lock) => lock.name === name) ?? [],
    };
  }, lockName);
}

async function assertBrowserPrimitives(a: Page, b: Page) {
  for (const page of [a, b]) {
    expect(await page.evaluate(() => ({
      secure: isSecureContext,
      lockRequest: typeof navigator.locks?.request,
      storage: localStorage instanceof Storage,
    }))).toEqual({ secure: true, lockRequest: 'function', storage: true });
  }
  await a.evaluate(() => localStorage.setItem('317:origin-probe', 'same-browser-origin'));
  expect(await b.evaluate(() => localStorage.getItem('317:origin-probe'))).toBe('same-browser-origin');
}

test.describe('Cookie session coordination in two real Chromium tabs', () => {
  test.setTimeout(60_000);

  test('expired R0 in two tabs rotates once, without backend reuse revocation', async ({ context, baseURL }, testInfo) => {
    const api = await mockCookieBackend(context, baseURL!);
    const a = await context.newPage();
    const b = await context.newPage();
    try {
      await api.seed('expired');
      await a.goto('/settings', { waitUntil: 'domcontentloaded' });
      await expect.poll(() => api.refreshPosts.length, { timeout: 15_000 }).toBe(1);
      await b.goto('/settings', { waitUntil: 'domcontentloaded' });
      await assertBrowserPrimitives(a, b);
      // Fail promptly on baseline's second POST rather than depending on sleeps.
      await expect.poll(async () => (await locks(b)).pending.length > 0 || api.refreshPosts.length > 1).toBe(true);
      const waiting = await locks(b);
      expect(api.refreshPosts).toEqual(['R0']);
      expect(waiting.held).toHaveLength(1);
      expect(waiting.pending).toHaveLength(1);
      expect((await api.cookies()).find(({ name }) => name === refreshCookie)?.value).toBe('R0');
      api.releaseRefresh();

      for (const page of [a, b]) {
        await expect(page.getByRole('combobox', { name: '언어', exact: true })).toHaveValue('ko');
        expect(await page.evaluate(() => document.cookie)).not.toContain('fanpulse_');
      }
      expect(api.refreshPosts).toEqual(['R0']);
      expect(api.snapshot()).toMatchObject({ reuseCount: 0, activeRefresh: 'R1', revoked: [] });
      expect(await api.cookies()).toEqual(expect.arrayContaining([
        expect.objectContaining({ name: accessCookie, value: 'A1', httpOnly: true }),
        expect.objectContaining({ name: refreshCookie, value: 'R1', httpOnly: true }),
      ]));
      await b.reload();
      await expect(b.getByRole('combobox', { name: '언어', exact: true })).toHaveValue('ko');
      expect(api.refreshPosts).toEqual(['R0']);
      expect(api.unexpected).toEqual([]);
      await testInfo.attach('two-tab-rotation-state', {
        body: JSON.stringify({ ...api.snapshot(), waiting, cookies: await api.cookies() }, null, 2),
        contentType: 'application/json',
      });
    } finally {
      await api.dispose();
    }
  });

  test('logout waits for delayed refresh Set-Cookie, revokes R1 and clears cookies last', async ({ context, baseURL }, testInfo) => {
    const api = await mockCookieBackend(context, baseURL!);
    const a = await context.newPage();
    const b = await context.newPage();
    try {
      await api.seed('A0');
      await Promise.all([a.goto('/settings'), b.goto('/settings')]);
      for (const page of [a, b]) {
        await expect(page.getByRole('combobox', { name: '언어', exact: true })).toHaveValue('ko');
      }
      await assertBrowserPrimitives(a, b);
      // Access expires after both UIs are signed in; no app/auth mocks are injected.
      await context.addCookies([{ name: accessCookie, value: 'expired', url: api.origin, httpOnly: true, sameSite: 'Lax' }]);
      await a.getByRole('combobox', { name: '언어', exact: true }).selectOption('en');
      await a.getByRole('button', { name: '저장', exact: true }).click();
      await expect.poll(() => api.refreshPosts.length, { timeout: 15_000 }).toBe(1);
      await b.getByRole('button', { name: '로그아웃', exact: true }).click();
      await expect.poll(async () => (await locks(b)).pending.length > 0 || api.logoutPosts.length > 0).toBe(true);
      const waiting = await locks(b);
      expect(api.logoutPosts).toEqual([]);
      expect(waiting.held).toHaveLength(1);
      // A real tab-focus unread-count request can also queue recovery. It must
      // not weaken the ordering: logout still cannot reach HTTP before refresh.
      expect(waiting.pending.length).toBeGreaterThanOrEqual(1);
      await expect(b.getByRole('button', { name: '로그아웃 중…', exact: true })).toBeDisabled();
      expect((await api.cookies()).find(({ name }) => name === refreshCookie)?.value).toBe('R0');
      api.releaseRefresh();

      await expect(b).toHaveURL(/\/login(?:\?|$)/);
      expect(api.logoutPosts).toEqual(['R1']);
      expect(api.snapshot()).toMatchObject({ reuseCount: 0, activeRefresh: null, revoked: ['R1'], acceptedMutations: 0 });
      expect(await api.cookies()).toEqual([]);
      const cookieOrder = api.events.filter(({ event }) => event === 'refresh-cookie-response' || event === 'logout-clear-response');
      expect(cookieOrder.map(({ event }) => event)).toEqual(['refresh-cookie-response', 'logout-clear-response']);
      expect(api.events.filter(({ path, method }) => path === '/me/settings' && method === 'PATCH')).toHaveLength(1);
      const generations = await Promise.all([a, b].map((page) => page.evaluate(() => localStorage.getItem('fanpulse:web-session-generation'))));
      expect(generations[0]).toBeTruthy();
      expect(generations[1]).toBe(generations[0]);
      // Reload the other tab: a genuine fresh auth check must not restore session.
      await a.reload();
      await expect(a).toHaveURL(/\/login(?:\?|$)/);
      expect(await api.cookies()).toEqual([]);
      expect(api.snapshot().activeRefresh).toBeNull();
      expect(api.unexpected).toEqual([]);
      await testInfo.attach('two-tab-logout-state', {
        body: JSON.stringify({ ...api.snapshot(), waiting, generations, cookies: await api.cookies() }, null, 2),
        contentType: 'application/json',
      });
    } finally {
      await api.dispose();
    }
  });
});
