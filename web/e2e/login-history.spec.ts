import { expect, test, type Page } from '@playwright/test';

const originPath = '/__history_origin__';
const post = {
  id: '11111111-1111-4111-8111-111111111111',
  authorId: '22222222-2222-4222-8222-222222222222',
  authorName: 'History fixture author',
  artistId: null,
  artistName: null,
  artistProfileImageUrl: null,
  content: 'Deep link restored after synthetic login',
  imageUrl: null,
  likeCount: 0,
  commentCount: 0,
  createdAt: '2026-11-01T09:00:00Z',
};
const detailPath = `/post-detail?id=${post.id}`;
const loginPath = `/login?redirect=${encodeURIComponent(detailPath)}`;

// All API traffic and GIS are intercepted. No real OAuth, cookies or backend needed.
async function mockSession(page: Page, options: { authenticated?: boolean; expireOnDetail?: boolean } = {}) {
  let authenticated = options.authenticated ?? false;
  let expireOnDetail = options.expireOnDetail ?? false;
  const user = { id: post.authorId, email: 'history@example.com', username: 'History_User' };
  const emptyPage = { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 };

  await page.route('**/gsi/client', (route) => route.fulfill({
    contentType: 'application/javascript',
    body: `(() => {
      let callback;
      window.google = { accounts: { id: {
        initialize: (config) => { callback = config.callback; },
        renderButton: (container) => {
          const button = document.createElement('button');
          button.textContent = 'Synthetic Google sign-in';
          button.onclick = () => callback({ credential: 'synthetic-history-token' });
          container.replaceChildren(button);
        }
      } } };
    })();`,
  }));

  await page.route('**/api/v1/**', async (route) => {
    const endpoint = new URL(route.request().url()).pathname.replace('/api/v1', '');
    const json = (data: unknown) => route.fulfill({ json: { success: true, data } });
    if (endpoint === '/auth/me') return route.fulfill({ json: { authenticated, user: authenticated ? user : null } });
    if (endpoint === '/auth/web/refresh') return route.fulfill({ status: 401, json: { error: 'expired' } });
    if (endpoint === '/auth/google') {
      authenticated = true;
      return route.fulfill({ json: { userId: user.id, email: user.email, username: user.username } });
    }
    if (endpoint === '/auth/logout') {
      authenticated = false;
      return route.fulfill({ json: {} });
    }
    if (endpoint === '/me/settings') {
      return route.fulfill({ json: { theme: 'light', language: 'ko', pushEnabled: true, updatedAt: post.createdAt } });
    }
    if (endpoint === '/users/me/notifications/unread-count') return json({ unreadCount: 0 });
    if (endpoint === `/community/posts/${post.id}`) {
      if (expireOnDetail) {
        expireOnDetail = false;
        authenticated = false;
      }
      if (!authenticated) return route.fulfill({ status: 401, json: { error: 'expired' } });
      return json(post);
    }
    if (endpoint === `/community/posts/${post.id}/comments`) return json(emptyPage);
    if (endpoint === '/comments/mine') return route.fulfill({ json: emptyPage });
    if (endpoint === `/community/me/posts/${post.id}/state`) return json({ liked: false, saved: false });
    if (endpoint === '/streaming-events') return json({ items: [], hasMore: false });
    if (endpoint === '/news/latest') return json([]);
    return route.fulfill({ status: 501, json: { unexpectedEndpoint: endpoint } });
  });
}

// A real prior document makes Back observable without relying on a fabricated
// router.back mock or manufacturing extra entries inside the application.
async function openFromPriorDocument(page: Page, destination: string) {
  await page.route(`**${originPath}`, (route) => route.fulfill({
    contentType: 'text/html',
    body: `<h1>History origin</h1><a href="${destination}">Open app</a>`,
  }));
  await page.goto(originPath);
  const previousLength = await page.evaluate(() => window.history.length);
  await page.getByRole('link', { name: 'Open app' }).click();
  return previousLength + 1;
}

async function finishLogin(page: Page, expectedPath = detailPath) {
  await page.getByRole('button', { name: 'Synthetic Google sign-in' }).click();
  await expect(page).toHaveURL(expectedPath);
  await expect(page.getByRole('heading', { name: 'Welcome to FanPulse' })).toHaveCount(0);
  if (expectedPath === detailPath) await expect(page.getByText(post.content, { exact: true })).toBeVisible();
}

test.use({ viewport: { width: 390, height: 844 } });

for (const entry of ['guard', 'expired session'] as const) {
  for (const back of ['page', 'browser'] as const) {
    test(`${entry}: deep-link login replaces transient entries and ${back} Back reaches the prior document`, async ({ page }) => {
      await mockSession(page, { authenticated: entry === 'expired session', expireOnDetail: entry === 'expired session' });
      const expectedLength = await openFromPriorDocument(page, detailPath);
      await expect(page).toHaveURL(loginPath);
      await expect(page.getByRole('button', { name: 'Synthetic Google sign-in' })).toBeVisible();
      expect(await page.evaluate(() => window.history.length)).toBe(expectedLength);

      await finishLogin(page);
      expect(await page.evaluate(() => window.history.length)).toBe(expectedLength);
      if (back === 'page') await page.getByRole('button', { name: '뒤로 가기', exact: true }).click();
      else await page.goBack();
      await expect(page).toHaveURL(originPath);
      await expect(page.getByRole('heading', { name: 'History origin' })).toBeVisible();

      // Forward returns to the detail, not to a blocked copy or completed login.
      await page.goForward();
      await expect(page).toHaveURL(detailPath);
      await expect(page.getByText(post.content, { exact: true })).toBeVisible();
    });
  }
}

test('Back before submitting login still reaches the prior document', async ({ page }) => {
  await mockSession(page);
  await openFromPriorDocument(page, detailPath);
  await expect(page).toHaveURL(loginPath);
  await page.goBack();
  await expect(page).toHaveURL(originPath);
});

for (const query of ['', `?redirect=${encodeURIComponent('//evil.example')}`]) {
  test(`login without a safe return destination replaces the form with home (${query || 'absent'})`, async ({ page }) => {
    await mockSession(page);
    const expectedLength = await openFromPriorDocument(page, `/login${query}`);
    await finishLogin(page, '/');
    expect(await page.evaluate(() => window.history.length)).toBe(expectedLength);
    await page.goBack();
    await expect(page).toHaveURL(originPath);
  });
}

test('a fresh-tab deep link adds no synthetic predecessor to browser history', async ({ page }) => {
  await mockSession(page);
  await page.goto(detailPath);
  await expect(page).toHaveURL(loginPath);
  await finishLogin(page);
  const session = await page.context().newCDPSession(page);
  const history = await session.send('Page.getNavigationHistory');
  // Headed Chromium may retain its initial about:blank. It is not an app entry.
  expect(history.entries.filter((entry) => entry.url !== 'about:blank').map((entry) => entry.url))
    .toEqual([new URL(detailPath, page.url()).href]);
  expect(history.currentIndex).toBe(history.entries.length - 1);
  if (history.currentIndex > 0) {
    expect(history.entries[history.currentIndex - 1].url).toBe('about:blank');
    await page.goBack();
    await expect(page).toHaveURL('about:blank');
    await page.goForward();
  } else {
    expect(await page.goBack()).toBeNull();
  }
  await expect(page).toHaveURL(detailPath);
  await expect(page.getByText(post.content, { exact: true })).toBeVisible();
  await session.detach();
});

test('confirmed logout replaces the current entry without discarding prior history', async ({ page }) => {
  await mockSession(page, { authenticated: true });
  const expectedLength = await openFromPriorDocument(page, '/settings');
  await page.getByRole('button', { name: /로그아웃/ }).click();
  // The root guard may also attach a safe return destination after auth clears.
  await expect(page).toHaveURL(/\/login(?:\?.*)?$/);
  await expect(page.getByRole('button', { name: 'Synthetic Google sign-in' })).toBeVisible();
  expect(await page.evaluate(() => window.history.length)).toBe(expectedLength);
  await page.goBack();
  await expect(page).toHaveURL(originPath);
});

for (const outcome of ['success', 'failure'] as const) {
  test(`held ${outcome} after Back cleans up login without losing the surviving cookie session`, async ({ page, context }, testInfo) => {
    let releaseLogin!: () => void;
    const loginGate = new Promise<void>((resolve) => { releaseLogin = resolve; });
    const users = {
      A: { id: post.authorId, email: 'alice@example.test', username: 'Alice_1', hasPassword: false, createdAt: post.createdAt },
      B: { id: '33333333-3333-4333-8333-333333333333', email: 'bob@example.test', username: 'Bob_2', hasPassword: false, createdAt: post.createdAt },
    };
    const calls: { path: string; principal: string | null }[] = [];
    const external: string[] = [];
    const unexpected: string[] = [];
    const base = testInfo.project.use.baseURL!;
    await context.addCookies([{ name: 'fanpulse_access_token', value: 'fixture-A', url: base, httpOnly: true, sameSite: 'Lax' }]);
    await page.addInitScript(() => {
      const events: string[] = [];
      Object.assign(window, { loginHistoryEvents: events });
      window.addEventListener('focus', () => events.push('focus'));
      window.addEventListener('storage', () => events.push('storage'));
      document.addEventListener('visibilitychange', () => events.push('visibilitychange'));
    });
    await page.route('**/*', async (route) => {
      const url = new URL(route.request().url());
      if (url.href === 'https://accounts.google.com/gsi/client') return route.fulfill({
        contentType: 'application/javascript',
        body: `(() => {
          let callback;
          window.google = { accounts: { id: {
            initialize: (config) => { callback = config.callback; },
            renderButton: (container) => {
              const button = document.createElement('button');
              button.textContent = 'Synthetic Google sign-in';
              button.onclick = () => callback({ credential: 'synthetic-history-token' });
              window.loginButtons = [...(window.loginButtons || []), button];
              container.replaceChildren(button);
            }
          } } };
        })();`,
      });
      if (url.origin !== new URL(base).origin) {
        external.push(url.origin);
        return route.abort('blockedbyclient');
      }
      if (!url.pathname.startsWith('/api/')) return route.continue();
      const path = url.pathname.replace('/api/v1', '');
      const cookie = (await route.request().allHeaders()).cookie ?? '';
      const principal = /fanpulse_access_token=fixture-([AB])(?:;|$)/.exec(cookie)?.[1] as keyof typeof users | undefined;
      calls.push({ path, principal: principal ?? null });
      if (path === '/auth/google') {
        await loginGate;
        return route.fulfill(outcome === 'success' ? {
          headers: { 'Set-Cookie': 'fanpulse_access_token=fixture-B; Path=/; HttpOnly; SameSite=Lax' },
          json: { userId: users.B.id, email: users.B.email, username: users.B.username },
        } : { status: 401, json: { error: 'Synthetic rejected login' } });
      }
      if (path === '/auth/logout') return route.fulfill({
        headers: { 'Set-Cookie': 'fanpulse_access_token=; Path=/; HttpOnly; SameSite=Lax; Max-Age=0' }, json: {},
      });
      if (path === '/auth/me') return route.fulfill({ json: { authenticated: !!principal, user: principal ? users[principal] : null } });
      if (path === '/auth/web/refresh') return route.fulfill({ status: 401, json: {} });
      if (path === '/me') return route.fulfill({ json: principal ? users[principal] : {} });
      if (path === '/me/settings') return route.fulfill({ json: { theme: 'light', language: 'ko', pushEnabled: true, updatedAt: post.createdAt } });
      if (!['/users/me/notifications/unread-count', '/streaming-events', '/news/latest'].includes(path)) {
        unexpected.push(path);
        return route.fulfill({ status: 501, json: {} });
      }
      const data = path === '/users/me/notifications/unread-count' ? { unreadCount: 0 }
        : path === '/streaming-events' ? { items: [], hasMore: false } : [];
      return route.fulfill({ json: { success: true, data } });
    });
    const eventLog = () => page.evaluate(() =>
      (window as typeof window & { loginHistoryEvents: string[] }).loginHistoryEvents);
    try {
      await page.goto('/');
      await page.getByRole('link', { name: 'My', exact: true }).click();
      await expect(page.getByRole('heading', { name: users.A.username, exact: true })).toBeVisible();
      await page.getByRole('link', { name: '설정', exact: true }).first().click();
      await expect(page).toHaveURL('/settings');
      const initialAuthReads = calls.filter(({ path }) => path === '/auth/me').length;
      await page.getByRole('button', { name: '로그아웃', exact: true }).click();
      await expect(page).toHaveURL(/\/login(?:\?.*)?$/);
      await page.getByRole('button', { name: 'Synthetic Google sign-in' }).click();
      await expect.poll(() => calls.filter(({ path }) => path === '/auth/google').length).toBe(1);
      await page.goBack();
      await expect(page).toHaveURL('/login?redirect=%2Fmypage');
      await expect(page.getByRole('button', { name: 'Synthetic Google sign-in' })).toBeVisible();
      // Cleanup detached the first SDK button; the root provider did not remount/recheck.
      expect(await page.evaluate(() => {
        const buttons = (window as typeof window & { loginButtons: HTMLButtonElement[] }).loginButtons;
        return buttons.length >= 2 && !buttons[0].isConnected && buttons.at(-1)!.isConnected;
      })).toBe(true);
      expect(calls.filter(({ path }) => path === '/auth/me')).toHaveLength(initialAuthReads);
      const cursor = (await eventLog()).length;
      const generation = await page.evaluate(() => localStorage.getItem('fanpulse:web-session-generation'));
      releaseLogin();
      await expect.poll(() => page.evaluate(() => localStorage.getItem('fanpulse:web-session-generation'))).not.toBe(generation);
      if (outcome === 'success') {
        await expect.poll(() => calls.filter(({ path, principal }) => path === '/auth/me' && principal === 'B').length).toBe(1);
        await expect.poll(() => calls.filter(({ path, principal }) => path === '/me/settings' && principal === 'B').length).toBe(1);
      } else {
        expect(calls.filter(({ path }) => path === '/auth/me')).toHaveLength(initialAuthReads);
      }
      // The old form must neither hijack navigation nor surface its late state in the new form.
      await expect(page).toHaveURL('/login?redirect=%2Fmypage');
      await expect(page.getByText('처리 중...', { exact: true })).toHaveCount(0);
      await expect(page.getByRole('alert').filter({ hasText: /\S/ })).toHaveCount(0);
      expect((await eventLog()).slice(cursor)).toEqual([]);
      const cookies = (await context.cookies(base)).filter(({ name }) => name === 'fanpulse_access_token');
      expect(cookies.map(({ value, httpOnly }) => ({ value, httpOnly }))).toEqual(outcome === 'success' ? [{ value: 'fixture-B', httpOnly: true }] : []);
      // Back stays within this document and does not itself trigger identity revalidation.
      await page.goBack();
      if (outcome === 'success') {
        await expect(page).toHaveURL('/');
        await page.getByRole('link', { name: 'My', exact: true }).click();
        await expect(page.getByRole('heading', { name: users.B.username, exact: true })).toBeVisible();
        expect(calls.filter(({ path }) => path === '/auth/me')).toHaveLength(initialAuthReads + 1);
      } else {
        await expect(page).toHaveURL('/login?redirect=%2F');
        expect(calls.filter(({ path }) => path === '/auth/me')).toHaveLength(initialAuthReads);
      }
      expect(unexpected).toEqual([]);
    } finally {
      releaseLogin();
      await testInfo.attach('cancelled-login-history', { body: JSON.stringify({ outcome, calls, events: await eventLog(), external }, null, 2), contentType: 'application/json' });
    }
  });
}
