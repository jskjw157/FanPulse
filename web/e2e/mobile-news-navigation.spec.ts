import { expect, test, type Page } from '@playwright/test';

const news = {
  id: '11111111-1111-4111-8111-111111111111',
  artistId: '22222222-2222-4222-8222-222222222222',
  title: 'Mobile navigation news fixture',
  content: '<p>News detail reached through its ID.</p>',
  sourceName: 'FanPulse test source',
  sourceUrl: 'https://example.com/news/mobile-navigation',
  thumbnailUrl: null,
  category: 'GENERAL',
  viewCount: 0,
  publishedAt: '2026-11-01T09:00:00Z',
  createdAt: '2026-11-01T09:00:00Z',
};

async function mockBackend(page: Page) {
  await page.route('**/api/v1/**', async (route) => {
    const url = new URL(route.request().url());
    const endpoint = url.pathname.replace('/api/v1', '');
    const json = (data: unknown) => route.fulfill({ json: { success: true, data } });
    if (endpoint === '/auth/me') {
      return route.fulfill({ json: { authenticated: true, user: { id: news.artistId, email: 'menu@example.com' } } });
    }
    if (endpoint === '/me/settings') {
      return route.fulfill({ json: { theme: 'light', language: 'ko', pushEnabled: true, updatedAt: news.createdAt } });
    }
    if (endpoint === '/users/me/notifications/unread-count') return json({ unreadCount: 0 });
    if (endpoint === '/streaming-events') return json({ items: [], hasMore: false });
    if (endpoint === '/news/latest') return json([]);
    if (endpoint === '/news') {
      return json({ content: [news], totalElements: 1, page: 0, size: 20, totalPages: 1 });
    }
    if (endpoint === `/news/${news.id}`) return json(news);
    // Do not forward an unexpected request to a real backend.
    return route.fulfill({ status: 501, json: { unexpectedEndpoint: endpoint } });
  });
}

test.use({ viewport: { width: 390, height: 844 } });

test('mobile News selection opens the list, closes the menu, and opens a selected article', async ({ page }) => {
  await mockBackend(page);
  // Home has no page header, so this regression is independent of #307.
  await page.goto('/');
  await page.locator('header').getByRole('button').click();
  const newsLink = page.getByRole('link', { name: /뉴스$/ });
  await expect(newsLink).toHaveAttribute('href', '/news');
  await newsLink.click();

  await expect(page).toHaveURL('/news');
  await expect(page.getByRole('heading', { name: '메뉴', exact: true })).toHaveCount(0);
  await expect(page.getByText('뉴스 ID가 필요합니다')).toHaveCount(0);
  await page.getByRole('link', { name: new RegExp(news.title) }).click();
  await expect(page).toHaveURL(`/news/${news.id}`);
  await expect(page.getByRole('heading', { name: news.title, exact: true })).toBeVisible();
});

test('an existing legacy news-detail URL with an ID still opens the canonical article', async ({ page }) => {
  await mockBackend(page);
  await page.goto(`/news-detail?id=${news.id}`);

  await expect(page).toHaveURL(`/news/${news.id}`);
  await expect(page.getByRole('heading', { name: news.title, exact: true })).toBeVisible();
  await expect(page.getByText('뉴스 ID가 필요합니다')).toHaveCount(0);
});
