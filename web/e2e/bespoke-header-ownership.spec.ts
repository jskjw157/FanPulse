import { expect, test, type Locator, type Page } from '@playwright/test';

const artistId = '22222222-2222-4222-8222-222222222222';
const detailId = '11111111-1111-4111-8111-111111111111';
const timestamp = '2026-11-01T09:00:00Z';
const news = {
  id: detailId, artistId, title: 'Header news fixture',
  content: '<p>Header ownership article.</p>'.repeat(100),
  sourceName: 'Fixture source', sourceUrl: 'https://example.test/header-news',
  thumbnailUrl: null, category: 'GENERAL', viewCount: 0,
  publishedAt: timestamp, createdAt: timestamp,
};
const live = {
  id: detailId, title: 'Header live fixture', artistName: 'Fixture artist',
  thumbnailUrl: null, status: 'LIVE', viewerCount: 12,
  description: 'Header ownership live description.\n'.repeat(100),
  streamUrl: 'https://www.youtube.com/watch?v=abcdefghijk',
};

async function mockBackend(page: Page, holdDetails = false) {
  let releaseDetails: () => void = () => undefined;
  const gate = holdDetails ? new Promise<void>((resolve) => { releaseDetails = resolve; }) : Promise.resolve();
  const state = { detailRequests: 0, searchQueries: [] as string[], posts: [] as unknown[], unexpected: [] as string[] };
  // Never fetch YouTube, remote icons, or any other external service.
  await page.context().route('**/*', (route) => {
    const url = new URL(route.request().url());
    if (url.hostname === 'cdn.jsdelivr.net' && url.pathname.endsWith('/fonts/remixicon.css')) {
      // Give icon-only buttons a deterministic glyph box without downloading fonts.
      return route.fulfill({ contentType: 'text/css', body: '[class^="ri-"]::before { content: "□"; }' });
    }
    return url.hostname === '127.0.0.1' || url.hostname === 'localhost' ? route.fallback() : route.abort();
  });
  await page.route('**/api/v1/**', async (route) => {
    const request = route.request();
    const url = new URL(request.url());
    const endpoint = url.pathname.replace('/api/v1', '');
    const json = (data: unknown) => route.fulfill({ json: { success: true, data } });
    if (endpoint === '/auth/me') return route.fulfill({ json: {
      authenticated: true, user: { id: artistId, email: 'header@example.test', username: 'Header fan' },
    } });
    if (endpoint === '/me/settings') return route.fulfill({ json: {
      theme: 'light', language: 'ko', pushEnabled: true, updatedAt: timestamp,
    } });
    if (endpoint === '/users/me/notifications/unread-count') return json({ unreadCount: 0 });
    if (endpoint === '/users/me/notifications/page') return json({
      content: [], page: 0, size: 20, totalElements: 0, totalPages: 0, last: true, unreadCount: 0,
    });
    if (endpoint === '/streaming-events') return json({ items: [], hasMore: false });
    if (endpoint === '/news/latest') return json([]);
    if (endpoint === '/search') {
      state.searchQueries.push(url.searchParams.get('q') ?? '');
      // Search deliberately has a raw response, unlike the detail endpoints.
      return route.fulfill({ json: {
        live: { items: [], totalCount: 0 },
        news: { items: [{
          id: detailId, title: 'Header search fixture', summary: 'Fixture summary',
          sourceName: 'Fixture source', publishedAt: timestamp,
        }], totalCount: 1 },
      } });
    }
    if (endpoint === '/artists') return json({
      content: [{ id: artistId, name: 'Fixture artist', englishName: null, agency: null, profileImageUrl: null, isGroup: true }],
      page: 0, size: 100, totalElements: 1, totalPages: 1,
    });
    if (endpoint === '/community/posts' && request.method() === 'POST') {
      state.posts.push(request.postDataJSON());
      // A visible, deterministic submit response without any real write.
      return route.fulfill({ status: 503, json: { success: false } });
    }
    if (endpoint === '/community/posts') return json({
      content: [], page: 0, size: 20, totalElements: 0, totalPages: 0, last: true,
    });
    if (endpoint === `/news/${detailId}` || endpoint === `/streaming-events/${detailId}`) {
      state.detailRequests += 1;
      await gate;
      return json(endpoint.startsWith('/news/') ? news : live);
    }
    state.unexpected.push(`${request.method()} ${endpoint}`);
    return route.fulfill({ status: 501, json: { unexpectedEndpoint: endpoint } });
  });
  return { state, releaseDetails };
}

function globalHeader(page: Page) {
  return page.locator('header').filter({ has: page.getByRole('link', { name: 'FanPulse', exact: true }) });
}

async function expectCenterHit(control: Locator) {
  await expect(control).toBeVisible();
  await expect.poll(() => control.evaluate((element) => {
    const box = element.getBoundingClientRect();
    const hit = document.elementFromPoint(box.x + box.width / 2, box.y + box.height / 2);
    return hit !== null && element.contains(hit);
  }), { message: 'The visible control, not an overlapping header, must own its center point' }).toBe(true);
}

async function expectSeparateRows(page: Page, localHeader: Locator) {
  await expect.poll(async () => {
    const globalBox = await globalHeader(page).boundingBox();
    const localBox = await localHeader.boundingBox();
    // top-16 meets the global 4rem row; its 1px bottom border may be shared.
    return globalBox !== null && localBox !== null && localBox.y >= globalBox.y + globalBox.height - 1;
  }).toBe(true);
}

async function openAndCloseMenu(page: Page) {
  const menu = page.getByRole('button', { name: '메뉴 열기' });
  await expectCenterHit(menu);
  await expectCenterHit(globalHeader(page).getByRole('link', { name: '검색', exact: true }));
  await expectCenterHit(globalHeader(page).getByRole('link', { name: /^알림/ }));
  // Normal Playwright click: no force, dispatchEvent, or script-triggered action.
  await menu.click();
  const title = page.getByRole('heading', { name: '메뉴', exact: true });
  await expect(title).toBeVisible();
  await title.locator('..').getByRole('button').click();
  await expect(title).toHaveCount(0);
}

test.describe('Bespoke mobile headers', () => {
  test.use({ viewport: { width: 390, height: 844 }, serviceWorkers: 'block' });

  test('search keeps global menu/notifications usable and its input/clear action usable', async ({ page }) => {
    const { state } = await mockBackend(page);
    await page.goto('/');
    await globalHeader(page).getByRole('link', { name: '검색', exact: true }).click();
    await expect(page).toHaveURL('/search');
    const input = page.getByRole('textbox');
    await expect(input).toBeVisible();
    await openAndCloseMenu(page);
    await expectSeparateRows(page, page.locator('header').filter({ has: input }));
    await expectCenterHit(input);
    await input.click();
    await input.fill('header');
    await expect(page.getByRole('heading', { name: 'Header search fixture', exact: true })).toBeVisible();
    expect(state.searchQueries).toContain('header');
    await page.getByRole('button', { name: 'Clear search' }).click();
    await expect(input).toHaveValue('');
    await globalHeader(page).getByRole('link', { name: /^알림/ }).click();
    await expect(page).toHaveURL('/notifications');
    await expect(page.getByRole('heading', { name: '알림', exact: true })).toBeVisible();
    expect(state.unexpected).toEqual([]);
  });

  test('post-create keeps global actions separate from enabled publish and cancel', async ({ page }) => {
    const { state } = await mockBackend(page);
    await page.goto('/post-create');
    const title = page.getByRole('heading', { name: '게시글 작성', exact: true });
    await expect(title).toBeVisible();
    await openAndCloseMenu(page);
    await expectSeparateRows(page, page.locator('header').filter({ has: title }));
    await page.getByRole('button', { name: 'Fixture artist', exact: true }).click();
    await page.getByLabel('내용 *', { exact: true }).fill('Header ownership post');
    const publish = page.getByRole('button', { name: '게시', exact: true });
    await expect(publish).toBeEnabled();
    await expectCenterHit(publish);
    await publish.click();
    await expect(page.getByRole('alert').filter({ hasText: '게시글을 등록하지 못했습니다.' })).toHaveText('게시글을 등록하지 못했습니다. 내용을 확인해 주세요.');
    expect(state.posts).toEqual([{ artistId, content: 'Header ownership post' }]);
    const cancel = page.getByRole('link', { name: '취소', exact: true });
    await expectCenterHit(cancel);
    await cancel.click();
    await expect(page).toHaveURL('/community');
    await expect(page.getByText('아직 작성된 게시글이 없습니다.', { exact: true })).toBeVisible();
    expect(state.unexpected).toEqual([]);
  });

  for (const kind of ['news', 'live'] as const) {
    for (const stateName of ['loading', 'ready'] as const) {
      test(`${kind} ${stateName} back row cannot cover global actions after scrolling`, async ({ page }) => {
        const { state, releaseDetails } = await mockBackend(page, stateName === 'loading');
        try {
          await page.goto('/');
          await page.goto(`/${kind}/${detailId}`);
          await expect.poll(() => state.detailRequests).toBeGreaterThan(0);
          const heading = page.getByRole('heading', { name: kind === 'news' ? news.title : live.title, exact: true });
          if (stateName === 'ready') await expect(heading).toBeVisible();
          else await expect(heading).toHaveCount(0);
          const back = page.getByRole('button', { name: /뒤로/ });
          await expect(back).toBeVisible();
          await page.evaluate(() => window.scrollTo(0, 320));
          await expect.poll(() => page.evaluate(() => window.scrollY)).toBeGreaterThan(64);
          await openAndCloseMenu(page);
          await expectSeparateRows(page, page.locator('header').filter({ has: back }));
          await expectCenterHit(back);
          releaseDetails();
          await expect(heading).toBeVisible();
          await back.click();
          await expect(page).toHaveURL('/');
          expect(state.unexpected).toEqual([]);
        } finally {
          releaseDetails();
        }
      });
    }
  }
});

test('desktop keeps global navigation clickable above static and scrolled detail headers', async ({ page }) => {
  await page.setViewportSize({ width: 1280, height: 900 });
  const { state } = await mockBackend(page);
  for (const path of ['/search', '/post-create']) {
    await page.goto(path);
    const local = path === '/search'
      ? page.locator('header').filter({ has: page.getByRole('textbox') })
      : page.locator('header').filter({ has: page.getByRole('heading', { name: '게시글 작성', exact: true }) });
    await expect(local).toBeVisible();
    expect(await local.evaluate((element) => getComputedStyle(element).position)).toBe('static');
    await expect(globalHeader(page).getByRole('link', { name: 'Home', exact: true })).toBeVisible();
    await expect(page.getByRole('button', { name: '메뉴 열기' })).toBeHidden();
  }
  for (const kind of ['news', 'live'] as const) {
    await page.goto(`/${kind}/${detailId}`);
    await expect(page.getByRole('heading', { name: kind === 'news' ? news.title : live.title, exact: true })).toBeVisible();
    const row = page.locator('header').filter({ has: page.getByRole('button', { name: /뒤로/ }) });
    await page.evaluate(() => window.scrollTo(0, 320));
    await expectSeparateRows(page, row);
    const home = globalHeader(page).getByRole('link', { name: 'Home', exact: true });
    await expectCenterHit(home);
    await home.click();
    await expect(page).toHaveURL('/');
  }
  expect(await page.evaluate(() => document.documentElement.scrollWidth)).toBeLessThanOrEqual(1280);
  expect(state.unexpected).toEqual([]);
});
