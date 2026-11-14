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
