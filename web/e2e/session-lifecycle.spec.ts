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
const secondUser = {
  id: '33200000-0000-4000-8000-000000000002',
  email: 'session-332@example.test',
  username: 'Session_B',
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
  const expiredSettingsArrived = deferred();
  const releaseExpiredSettings = deferred();
  const logoutArrived = deferred();
  const releaseLogout = deferred();
  const releaseLogin = deferred();
  let nextLoginGate: ReturnType<typeof deferred> & { arrived: ReturnType<typeof deferred> } | undefined;
  const identityGates = new Map<Page, ReturnType<typeof deferred> & {
    arrived: ReturnType<typeof deferred>; once: boolean; access?: string;
  }>();
  let delayExpiredSettings = false;
  let delayLogout = false;
  let failLogout = false;
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
    if (url.href === 'https://accounts.google.com/gsi/client') return route.fulfill({
      contentType: 'application/javascript',
      body: `window.google = { accounts: { id: {
        initialize(config) { this.callback = config.callback; },
        renderButton(container) {
          const button = document.createElement('button');
          button.textContent = 'Synthetic Google sign-in';
          button.onclick = () => this.callback({ credential: 'synthetic-writer-order' });
          container.replaceChildren(button);
        }
      } } };`,
    });
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
    const user = access === 'B0' ? secondUser : /^A\d+$/.test(access) ? syntheticUser : null;
    const authenticated = !!user;
    events.push({ event: 'request', path, method: request.method(), refresh, access });

    if (path === '/auth/me' && request.method() === 'GET') {
      const gate = identityGates.get(request.frame().page());
      if (gate) {
        if (gate.once) identityGates.delete(request.frame().page());
        gate.access = access;
        gate.arrived.resolve();
        await gate.promise;
      }
      events.push({ event: 'identity-response', path, access });
      return route.fulfill({ headers: { 'cache-control': 'no-store' }, json: {
        authenticated, ...(user ? { user } : {}),
      } });
    }
    if (path === '/auth/google' && request.method() === 'POST') {
      const gate = nextLoginGate;
      nextLoginGate = undefined;
      if (gate) { gate.arrived.resolve(); await gate.promise; }
      await releaseLogin.promise;
      activeRefresh = 'RB0';
      events.push({ event: 'login-cookie-response', path, refresh: 'RB0' });
      return route.fulfill({ headers: authHeaders('B0', 'RB0'), json: {
        userId: secondUser.id, email: secondUser.email, username: secondUser.username,
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
      logoutArrived.resolve();
      if (delayLogout) await releaseLogout.promise;
      if (failLogout) return route.fulfill({ status: 500, json: { error: 'synthetic-logout-failed' } });
      if (refresh) revoked.add(refresh);
      if (activeRefresh === refresh) activeRefresh = null;
      events.push({ event: 'logout-clear-response', path, refresh });
      return route.fulfill({ status: 200, headers: authHeaders('', '', true), json: {} });
    }
    if (path === '/me' && request.method() === 'GET') {
      if (!user) return expired(route);
      return route.fulfill({ json: { ...user, hasPassword: false, createdAt: settings.updatedAt } });
    }
    if (path === '/me/settings') {
      if (!authenticated) {
        if (delayExpiredSettings && request.method() === 'PATCH') {
          expiredSettingsArrived.resolve();
          await releaseExpiredSettings.promise;
        }
        return expired(route);
      }
      if (request.method() === 'PATCH') {
        ++acceptedMutations;
        return route.fulfill({ json: { ...settings, ...request.postDataJSON() } });
      }
      return route.fulfill({ json: { ...settings, language: user?.id === secondUser.id ? 'en' : 'ko' } });
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
    holdExpiredSettings() { delayExpiredSettings = true; },
    expiredSettingsArrived: expiredSettingsArrived.promise,
    releaseExpiredSettings: releaseExpiredSettings.resolve,
    holdLogout(fail = false) { delayLogout = true; failLogout = fail; },
    logoutArrived: logoutArrived.promise,
    releaseLogout: releaseLogout.resolve,
    releaseLogin: releaseLogin.resolve,
    holdNextLogin() {
      const gate = { ...deferred(), arrived: deferred() };
      nextLoginGate = gate;
      return { arrived: gate.arrived.promise, release: gate.resolve };
    },
    holdIdentity(page: Page, once = false) {
      const gate = { ...deferred(), arrived: deferred(), once, access: undefined as string | undefined };
      identityGates.set(page, gate);
      return {
        arrived: gate.arrived.promise,
        access: () => gate.access,
        release() { if (identityGates.get(page) === gate) identityGates.delete(page); gate.resolve(); },
      };
    },
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
      releaseExpiredSettings.resolve();
      releaseLogout.resolve();
      releaseLogin.resolve();
      for (const gate of identityGates.values()) gate.resolve();
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

  for (const order of ['login-logout', 'logout-login', 'login-failed-logout', 'login-logout-login'] as const) {
    test(`cookie writers ${order} reconcile both surviving providers`, async ({ context, baseURL }, testInfo) => {
      await context.addInitScript(() => {
        const events: { type: string; key?: string | null; trusted: boolean }[] = [];
        Object.assign(window, { writerEvents: events, writerDocument: Math.random() });
        window.addEventListener('storage', (event) => events.push({ type: event.type, key: event.key, trusted: event.isTrusted }));
        window.addEventListener('focus', (event) => events.push({ type: event.type, trusted: event.isTrusted }));
        document.addEventListener('visibilitychange', (event) => events.push({ type: event.type, trusted: event.isTrusted }));
      });
      const events = (page: Page) => page.evaluate(() =>
        (window as typeof window & { writerEvents: { type: string; key?: string | null; trusted: boolean }[] }).writerEvents);
      const documentId = (page: Page) => page.evaluate(() =>
        (window as typeof window & { writerDocument: number }).writerDocument);
      const api = await mockCookieBackend(context, baseURL!);
      const a = await context.newPage();
      const b = await context.newPage();
      try {
        await api.seed('A0');
        await a.goto('/mypage');
        await expect(a.getByRole('heading', { name: syntheticUser.username, exact: true })).toBeVisible();
        await a.getByRole('link', { name: '설정', exact: true }).first().click();
        await b.goto('/login?redirect=%2Fsettings');
        await expect(b.getByRole('button', { name: 'Synthetic Google sign-in' })).toBeVisible();
        await expect(a.locator('#language')).toHaveValue('ko');
        await assertBrowserPrimitives(a, b);
        const documents = await Promise.all([a, b].map(documentId));
        api.holdLogout(order === 'login-failed-logout');
        if (order === 'logout-login') {
          await a.getByRole('button', { name: '로그아웃', exact: true }).click();
          await api.logoutArrived;
          await b.getByRole('button', { name: 'Synthetic Google sign-in' }).click();
          await expect.poll(async () => (await locks(b)).pending.length).toBe(1);
          expect(api.events.filter(({ path }) => path === '/auth/google')).toEqual([]);
          api.releaseLogout();
          await expect(a).toHaveURL(/\/login(?:\?|$)/);
          await expect.poll(() => api.events.filter(({ path }) => path === '/auth/google').length).toBe(1);
          expect(await api.cookies()).toEqual([]);
        } else {
          await b.getByRole('button', { name: 'Synthetic Google sign-in' }).click();
          await expect.poll(() => api.events.filter(({ path }) => path === '/auth/google').length).toBe(1);
          await a.getByRole('button', { name: '로그아웃', exact: true }).click();
          await expect.poll(async () => (await locks(a)).pending.length).toBe(1);
          expect(api.logoutPosts).toEqual([]);
        }
        const cursors = await Promise.all([a, b].map(async (page) => (await events(page)).length));
        api.releaseLogin();
        await expect(b.locator('#language')).toHaveValue('en');
        if (order !== 'logout-login') {
          await api.logoutArrived;
          expect(api.logoutPosts).toEqual(['RB0']);
          // B's real completion nonce refreshes A while logout still holds its cookie response.
          await expect(a.locator('#language')).toHaveValue('en');
          expect((await events(a)).slice(cursors[0])).toContainEqual({ type: 'storage', key: 'fanpulse:session-changed', trusted: true });
          const nonce = await a.evaluate(() => localStorage.getItem('fanpulse:session-changed'));
          const generation = await a.evaluate(() => localStorage.getItem('fanpulse:web-session-generation'));
          const identity = order === 'login-logout-login' ? api.holdIdentity(a) : null;
          api.releaseLogout();
          await expect.poll(() => a.evaluate(() => localStorage.getItem('fanpulse:web-session-generation'))).not.toBe(generation);
          if (order === 'login-logout') {
            expect(await api.cookies()).toEqual([]);
            await expect(b).toHaveURL(/\/login(?:\?|$)/);
            await expect(a).toHaveURL(/\/login(?:\?|$)/);
            for (const page of [a, b]) await expect(page.locator('#language')).toHaveCount(0);
          } else if (identity) {
            // Let a later real cookie writer overtake A's post-logout identity read.
            // Its stale anonymous result must not overwrite the new B session.
            await identity.arrived;
            await expect(b).toHaveURL(/\/login(?:\?|$)/);
            expect(await api.cookies()).toEqual([]);
            await b.getByRole('button', { name: 'Synthetic Google sign-in' }).click();
            await expect(b.locator('#language')).toHaveValue('en');
            identity.release();
            await expect(a.locator('#language')).toHaveValue('en');
          } else {
            await expect(a.locator('#language')).toHaveValue('en');
            await expect(a.getByRole('button', { name: 'Log out', exact: true })).toBeEnabled();
            expect(await a.evaluate(() => localStorage.getItem('fanpulse:session-changed'))).toBe(nonce);
          }
        } else {
          // Back is a same-document route, not a reload that could hide a stale provider.
          await expect.poll(async () => (await events(a)).slice(cursors[0]).some(({ key }) => key === 'fanpulse:session-changed')).toBe(true);
          const reads = api.events.filter(({ path }) => path === '/auth/me').length;
          await a.goBack();
          await expect(a.getByRole('heading', { name: secondUser.username, exact: true })).toBeVisible();
          expect(api.events.filter(({ path }) => path === '/auth/me')).toHaveLength(reads);
        }
        if (order !== 'login-logout') {
          expect(await api.cookies()).toEqual(expect.arrayContaining([
            expect.objectContaining({ name: accessCookie, value: 'B0', httpOnly: true }),
            expect.objectContaining({ name: refreshCookie, value: 'RB0', httpOnly: true }),
          ]));
          await expect(b.locator('#language')).toHaveValue('en');
        }
        expect(await Promise.all([a, b].map(documentId))).toEqual(documents);
        for (const [index, page] of [a, b].entries()) {
          expect((await events(page)).slice(cursors[index]).filter(({ type }) => type === 'focus' || type === 'visibilitychange')).toEqual([]);
        }
        expect(api.unexpected).toEqual([]);
        const cookieOrder = api.events.filter(({ event }) => event.endsWith('cookie-response') || event === 'logout-clear-response').map(({ event }) => event);
        expect(cookieOrder).toEqual(order === 'logout-login' ? ['logout-clear-response', 'login-cookie-response']
          : order === 'login-logout' ? ['login-cookie-response', 'logout-clear-response']
          : order === 'login-logout-login' ? ['login-cookie-response', 'logout-clear-response', 'login-cookie-response'] : ['login-cookie-response']);
      } finally {
        await testInfo.attach('writer-order-state', { body: JSON.stringify({ order, ...api.snapshot(), cookies: await api.cookies(), a: await events(a), b: await events(b) }, null, 2), contentType: 'application/json' });
        await api.dispose();
      }
    });
  }

  test('same-account queued login notifies a surviving provider before old anonymous reads finish', async ({ context, baseURL }, testInfo) => {
    await context.addInitScript(() => {
      const events: { type: string; key?: string | null; trusted: boolean }[] = [];
      Object.assign(window, { sameAccountEvents: events, sameAccountDocument: Math.random() });
      window.addEventListener('storage', (event) => events.push({ type: event.type, key: event.key, trusted: event.isTrusted }));
      window.addEventListener('focus', (event) => events.push({ type: event.type, trusted: event.isTrusted }));
      document.addEventListener('visibilitychange', (event) => events.push({ type: event.type, trusted: event.isTrusted }));
    });
    const browserState = (page: Page) => page.evaluate(() => {
      const observed = window as typeof window & { sameAccountEvents: { type: string; key?: string | null; trusted: boolean }[]; sameAccountDocument: number };
      return { events: [...observed.sameAccountEvents], document: observed.sameAccountDocument };
    });
    const api = await mockCookieBackend(context, baseURL!);
    const a = await context.newPage();
    const b = await context.newPage();
    let aRead: ReturnType<typeof api.holdIdentity> | undefined;
    let bRead: ReturnType<typeof api.holdIdentity> | undefined;
    let login: ReturnType<typeof api.holdNextLogin> | undefined;
    try {
      await api.seed('A0');
      await a.goto('/settings');
      await expect(a.locator('#language')).toHaveValue('ko');
      await b.goto('/login?redirect=%2Fsettings');
      await expect(b.getByRole('button', { name: 'Synthetic Google sign-in' })).toBeVisible();
      api.holdLogout();
      await b.getByRole('button', { name: 'Synthetic Google sign-in' }).click();
      await expect.poll(() => api.events.filter(({ path }) => path === '/auth/google').length).toBe(1);
      await a.getByRole('button', { name: '로그아웃', exact: true }).click();
      await expect.poll(async () => (await locks(a)).pending.length).toBe(1);
      api.releaseLogin();
      await api.logoutArrived;
      expect(api.logoutPosts).toEqual(['RB0']);
      await expect(b.locator('#language')).toHaveValue('en');
      await expect(a.locator('#language')).toHaveValue('en');

      // Establish a public login peer whose surviving provider already knows B.
      // All subsequent history/navigation is inside these same two documents.
      await b.goto('/login?redirect=%2Fsettings');
      await expect(b.getByRole('button', { name: 'Synthetic Google sign-in' })).toBeVisible();
      await b.waitForLoadState('networkidle');
      await expect(a.locator('#language')).toHaveValue('en');
      await assertBrowserPrimitives(a, b);
      const before = await Promise.all([a, b].map(browserState));
      const nonce = await a.evaluate(() => localStorage.getItem('fanpulse:session-changed'));
      aRead = api.holdIdentity(a, true);
      bRead = api.holdIdentity(b, true);
      login = api.holdNextLogin();
      await b.getByRole('button', { name: 'Synthetic Google sign-in' }).click();
      await expect.poll(async () => (await locks(b)).pending.length).toBe(1);
      expect(api.events.filter(({ event, path }) => event === 'request' && path === '/auth/google')).toHaveLength(1);
      api.releaseLogout();
      await login.arrived;
      await Promise.all([aRead.arrived, bRead.arrived]);
      expect(aRead.access()).toBe('');
      expect(bRead.access()).toBe('');
      expect(await api.cookies()).toEqual([]);
      expect(await a.evaluate(() => localStorage.getItem('fanpulse:session-changed'))).not.toBe(nonce);

      // Complete the writer and B's fresh confirmation FIRST. Each old anonymous
      // response is a separate gate and cannot accidentally release a new B read.
      const afterLogoutNonce = await a.evaluate(() => localStorage.getItem('fanpulse:session-changed'));
      login.release();
      await expect(b.locator('#language')).toHaveValue('en');
      expect(await api.cookies()).toEqual(expect.arrayContaining([
        expect.objectContaining({ name: accessCookie, value: 'B0', httpOnly: true }),
        expect.objectContaining({ name: refreshCookie, value: 'RB0', httpOnly: true }),
      ]));
      bRead.release();
      await b.waitForLoadState('networkidle');
      aRead.release();
      await a.waitForLoadState('networkidle');
      // Decisive assertion: baseline has B cookies/peer B but an anonymous sender.
      await expect(a.locator('#language')).toHaveValue('en');
      await expect(b.locator('#language')).toHaveValue('en');
      expect(await a.evaluate(() => localStorage.getItem('fanpulse:session-changed'))).not.toBe(afterLogoutNonce);
      const after = await Promise.all([a, b].map(browserState));
      expect(after.map(({ document }) => document)).toEqual(before.map(({ document }) => document));
      for (const [index, state] of after.entries()) {
        const received = state.events.slice(before[index].events.length);
        expect(received.filter(({ type }) => type === 'focus' || type === 'visibilitychange')).toEqual([]);
      }
      expect(after[0].events.slice(before[0].events.length).filter(({ type, key, trusted }) =>
        type === 'storage' && key === 'fanpulse:session-changed' && trusted)).toHaveLength(1);
      expect(after[1].events.slice(before[1].events.length).filter(({ type, key, trusted }) =>
        type === 'storage' && key === 'fanpulse:session-changed' && trusted)).toHaveLength(1);
      expect(api.events.filter(({ event }) => event === 'login-cookie-response' || event === 'logout-clear-response').map(({ event }) => event))
        .toEqual(['login-cookie-response', 'logout-clear-response', 'login-cookie-response']);
      expect(api.unexpected).toEqual([]);
    } finally {
      await testInfo.attach('same-account-queued-writer-state', {
        body: JSON.stringify({ ...api.snapshot(), cookies: await api.cookies(), a: await browserState(a), b: await browserState(b) }, null, 2),
        contentType: 'application/json',
      });
      aRead?.release();
      bRead?.release();
      login?.release();
      await api.dispose();
    }
  });

  test('confirmed logout notifies a new tab while the originating identity recheck is pending', async ({ context, baseURL }, testInfo) => {
    await context.addInitScript(() => {
      const events: { type: string; key?: string | null; trusted: boolean }[] = [];
      Object.assign(window, { logoutBoundaryEvents: events });
      window.addEventListener('storage', (event) => events.push({ type: event.type, key: event.key, trusted: event.isTrusted }));
      window.addEventListener('focus', (event) => events.push({ type: event.type, trusted: event.isTrusted }));
      document.addEventListener('visibilitychange', (event) => events.push({ type: event.type, trusted: event.isTrusted }));
    });
    const browserEvents = (page: Page) => page.evaluate(() =>
      (window as typeof window & { logoutBoundaryEvents: { type: string; key?: string | null; trusted: boolean }[] }).logoutBoundaryEvents);
    const api = await mockCookieBackend(context, baseURL!);
    const left = await context.newPage();
    const right = await context.newPage();
    try {
      await api.seed('A0');
      await left.goto('/settings');
      await expect(left.locator('#language')).toHaveValue('ko');
      api.holdLogout();
      await left.getByRole('button', { name: '로그아웃', exact: true }).click();
      await api.logoutArrived;
      const identity = api.holdIdentity(left);
      const leftCursor = (await browserEvents(left)).length;
      // The new real AuthProvider announces A; no StorageEvent is synthesized.
      await right.goto('/settings');
      await expect(right.locator('#language')).toHaveValue('ko');
      await identity.arrived;
      expect((await browserEvents(left)).slice(leftCursor)).toContainEqual({
        type: 'storage', key: 'fanpulse:session-changed', trusted: true,
      });
      await expect(left.locator('#language')).toHaveCount(0);
      const rightCursor = (await browserEvents(right)).length;
      const generation = await left.evaluate(() => localStorage.getItem('fanpulse:web-session-generation'));
      api.releaseLogout();
      // Wait for the actual response's lifecycle boundary before releasing the old A read.
      await expect.poll(() => left.evaluate(() => localStorage.getItem('fanpulse:web-session-generation'))).not.toBe(generation);
      expect(await api.cookies()).toEqual([]);
      identity.release();
      await expect(left).toHaveURL(/\/login(?:\?|$)/);
      await expect(right).toHaveURL(/\/login(?:\?|$)/);
      const received = (await browserEvents(right)).slice(rightCursor);
      expect(received).toContainEqual({ type: 'storage', key: 'fanpulse:session-changed', trusted: true });
      expect(received.filter(({ type }) => type === 'focus' || type === 'visibilitychange')).toEqual([]);
      expect(api.unexpected).toEqual([]);
    } finally {
      await testInfo.attach('held-logout-native-storage', {
        body: JSON.stringify({ ...api.snapshot(), left: await browserEvents(left).catch(() => []), right: await browserEvents(right).catch(() => []) }, null, 2),
        contentType: 'application/json',
      });
      await api.dispose();
    }
  });

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

  test('quota-exhausted storage cannot recover an old 401 after logout', async ({ context, baseURL }, testInfo) => {
    const api = await mockCookieBackend(context, baseURL!);
    const page = await context.newPage();
    try {
      await api.seed('A0');
      await page.goto('/settings');
      await expect(page.getByRole('combobox', { name: '언어', exact: true })).toHaveValue('ko');
      // Exhaust actual Chromium storage; do not replace Storage methods or app code.
      const quota = await page.evaluate(() => {
        const key = 'fanpulse:web-session-generation';
        localStorage.setItem(key, 's');
        let chunk = 'x'.repeat(64 * 1024);
        let index = 0;
        let filled = 0;
        while (chunk.length) {
          try {
            localStorage.setItem(`328:quota:${index++}`, chunk);
            filled += chunk.length;
          } catch {
            chunk = chunk.slice(0, Math.floor(chunk.length / 2));
          }
        }
        let writeError = '';
        try { localStorage.setItem(key, 'a-new-session-generation-that-cannot-fit'); }
        catch (error) { writeError = (error as DOMException).name; }
        return { filled, writeError, readableGeneration: localStorage.getItem(key) };
      });
      expect(quota.filled).toBeGreaterThan(1024 * 1024);
      expect(quota).toMatchObject({ writeError: 'QuotaExceededError', readableGeneration: 's' });
      await testInfo.attach('actual-chromium-storage-quota', {
        body: JSON.stringify(quota), contentType: 'application/json',
      });
      await context.addCookies([{ name: accessCookie, value: 'expired', url: api.origin, httpOnly: true, sameSite: 'Lax' }]);
      api.holdExpiredSettings();
      await page.getByRole('combobox', { name: '언어', exact: true }).selectOption('en');
      await page.getByRole('button', { name: '저장', exact: true }).click();
      await api.expiredSettingsArrived;
      // Keep logout pending so AuthContext has not aborted settings work yet.
      // The old 401 then reaches Axios while the real session lock is held.
      api.holdLogout();
      await page.getByRole('button', { name: '로그아웃', exact: true }).click();
      await api.logoutArrived;
      const staleResponse = page.waitForResponse((response) =>
        response.url().endsWith('/api/v1/me/settings') && response.request().method() === 'PATCH');
      api.releaseExpiredSettings();
      expect((await staleResponse).status()).toBe(401);
      api.releaseLogout();
      await expect(page).toHaveURL(/\/login(?:\?|$)/);
      expect(await api.cookies()).toEqual([]);
      await page.waitForLoadState('networkidle');
      expect(api.refreshPosts).toEqual([]);
      expect(api.events.filter(({ path, method }) => path === '/me/settings' && method === 'PATCH')).toHaveLength(1);
      expect(api.snapshot().acceptedMutations).toBe(0);
      expect(api.unexpected).toEqual([]);
    } finally {
      await testInfo.attach('quota-session-boundary', {
        body: JSON.stringify({ ...api.snapshot(), cookies: await api.cookies() }, null, 2),
        contentType: 'application/json',
      });
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
