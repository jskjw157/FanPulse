import { expect, test, type Page } from '@playwright/test';

async function mockBackend(page: Page) {
  let unreadCount = 1;
  await page.route('**/api/v1/**', async (route) => {
    const request = route.request();
    const url = new URL(request.url());
    const endpoint = url.pathname.replace('/api/v1', '');
    const json = (data: unknown) => route.fulfill({ json: { success: true, data } });
    if (endpoint === '/auth/me') {
      return route.fulfill({ json: { authenticated: true, user: {
        id: '11111111-1111-4111-8111-111111111111', email: 'header@example.com', username: 'Header fan',
      } } });
    }
    if (endpoint === '/me/settings') {
      return route.fulfill({ json: {
        theme: 'light', language: 'ko', pushEnabled: true, updatedAt: '2026-11-01T09:00:00Z',
      } });
    }
    if (endpoint === '/streaming-events') return json({ items: [], hasMore: false });
    if (endpoint === '/news/latest') return json([]);
    if (endpoint === '/users/me/notifications/unread-count') return json({ unreadCount });
    if (endpoint === '/users/me/notifications/page') {
      return json({
        content: [{
          id: '22222222-2222-4222-8222-222222222222', type: 'NEWS', message: 'Header action fixture',
          isRead: unreadCount === 0, createdAt: '2026-11-01T09:00:00Z',
        }],
        page: 0, size: 20, totalElements: 1, totalPages: 1, last: true, unreadCount,
      });
    }
    if (endpoint === '/users/me/notifications/read-all' && request.method() === 'PATCH') {
      const updated = unreadCount;
      unreadCount = 0;
      return json({ updated });
    }
    // Keep this navigation contract test isolated from any real backend.
    return route.fulfill({ status: 501, json: { unexpectedEndpoint: endpoint } });
  });
}

test.describe('Mobile header ownership', () => {
  test.use({ viewport: { width: 390, height: 844 } });

  test('home and settings menus, page back/actions, search and notifications remain clickable', async ({ page }) => {
    await mockBackend(page);
    await page.goto('/');
    const menu = page.getByRole('button', { name: '메뉴 열기' });
    await menu.click();
    await page.getByRole('link', { name: /설정$/ }).click();
    await expect(page).toHaveURL('/settings');
    await expect(page.getByRole('heading', { name: '설정', exact: true })).toBeVisible();
    const back = page.getByRole('button', { name: '뒤로 가기' });
    const menuBox = await menu.boundingBox();
    const backBox = await back.boundingBox();
    expect(menuBox).not.toBeNull();
    expect(backBox).not.toBeNull();
    expect(menuBox!.y + menuBox!.height).toBeLessThanOrEqual(backBox!.y);

    // No force or dispatched events: this fails if PageHeader intercepts the menu.
    await menu.click();
    const drawerTitle = page.getByRole('heading', { name: '메뉴', exact: true });
    await expect(drawerTitle).toBeVisible();
    await drawerTitle.locator('..').getByRole('button').click();
    await expect(drawerTitle).toHaveCount(0);

    await page.getByRole('link', { name: /^알림/ }).click();
    await expect(page).toHaveURL('/notifications');
    await page.getByRole('button', { name: '모두 읽음 처리' }).click();
    await expect(page.getByText('읽지 않은 알림 0개', { exact: true })).toBeVisible();
    await back.click();
    await expect(page).toHaveURL('/settings');
    await page.getByRole('link', { name: '검색', exact: true }).click();
    await expect(page).toHaveURL('/search');
    await expect(page.getByPlaceholder('라이브, 뉴스 검색...')).toBeVisible();
    await page.goBack();
    await expect(page).toHaveURL('/settings');
    await back.click();
    await expect(page).toHaveURL('/');
    await expect(back).toHaveCount(0);
    await menu.click();
    await expect(page.getByRole('heading', { name: '메뉴', exact: true })).toBeVisible();
  });

  test('scrolled support tabs stay below the page back/title row', async ({ page }) => {
    await mockBackend(page);
    await page.goto('/support');
    await expect(page.getByRole('heading', { name: '고객센터', exact: true })).toBeVisible();
    await page.evaluate(() => window.scrollTo(0, document.documentElement.scrollHeight));
    const back = page.getByRole('button', { name: '뒤로 가기' });
    const faq = page.getByRole('button', { name: 'FAQ', exact: true });
    await expect.poll(async () => {
      const backBox = await back.boundingBox();
      const tabBox = await faq.boundingBox();
      return backBox !== null && tabBox !== null && tabBox.y >= backBox.y + backBox.height;
    }).toBe(true);
    await page.getByRole('button', { name: '공지사항', exact: true }).click();
    await expect(page.getByRole('heading', { name: '고객센터', exact: true })).toBeVisible();
    await page.getByRole('button', { name: '메뉴 열기' }).click();
    await expect(page.getByRole('heading', { name: '메뉴', exact: true })).toBeVisible();
  });
});

test('desktop keeps global navigation and the static page heading', async ({ page }) => {
  await page.setViewportSize({ width: 1280, height: 900 });
  await mockBackend(page);
  await page.goto('/settings');
  const heading = page.getByRole('heading', { name: '설정', exact: true });
  await expect(heading).toBeVisible();
  await expect(page.getByRole('button', { name: '뒤로 가기' })).toBeHidden();
  await expect(page.getByRole('button', { name: '메뉴 열기' })).toBeHidden();
  const pageHeader = page.locator('header').filter({ has: heading });
  expect(await pageHeader.evaluate((element) => getComputedStyle(element).position)).toBe('static');
  await expect(page.getByRole('link', { name: 'Home', exact: true })).toBeVisible();
  await expect(page.getByRole('link', { name: '검색', exact: true })).toBeVisible();
  await expect(page.getByRole('link', { name: /^알림/ })).toBeVisible();
  expect(await page.evaluate(() => document.documentElement.scrollWidth)).toBeLessThanOrEqual(1280);
});
