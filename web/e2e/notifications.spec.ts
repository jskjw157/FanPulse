import { expect, test, type Page } from '@playwright/test';
import { mkdirSync } from 'node:fs';
import path from 'node:path';

// Browser contract tests only: all backend responses are mocked, not DB/AI evidence.
const userId = '11111111-1111-1111-1111-111111111111';
const targetId = '33333333-3333-3333-3333-333333333333';
const deletedMessage = '삭제되었거나 사용할 수 없는 게시글입니다.';

async function mockBackend(page: Page) {
  const rows = Array.from({ length: 22 }, (_, index) => ({
    id: `22222222-2222-2222-2222-${String(index + 1).padStart(12, '0')}`,
    type: index === 1 ? 'NEWS' : 'POST_LIKED',
    message: index === 1 ? '이전 형식의 알림' : `게시글 반응 알림 ${index + 1}`,
    isRead: false,
    createdAt: '2026-11-06T10:30:00Z',
    ...(index === 1 ? {} : { targetType: 'POST', targetId, targetAvailable: true }),
  }));
  const api = { rows, failList: false, failRead: false, failAll: false, deleted: false, detailDeleted: false,
    readGate: null as Promise<void> | null };
  await page.route('**/api/v1/**', async (route) => {
    const request = route.request();
    const url = new URL(request.url());
    const endpoint = url.pathname.replace('/api/v1', '');
    const json = (data: unknown, status = 200) => route.fulfill({ status, json: { success: status < 400, data } });
    if (endpoint === '/auth/me') return route.fulfill({ json: { authenticated: true, user: { id: userId, email: 'notification@example.com' } } });
    if (endpoint === '/users/me/notifications/unread-count') return json({ unreadCount: rows.filter((row) => !row.isRead).length });
    if (endpoint === '/users/me/notifications/page') {
      if (api.failList) return json(null, 503);
      const pageNumber = Number(url.searchParams.get('page'));
      const size = Number(url.searchParams.get('size'));
      const filtered = url.searchParams.get('unreadOnly') === 'true' ? rows.filter((row) => !row.isRead) : rows;
      return json({
        content: filtered.slice(pageNumber * size, (pageNumber + 1) * size), page: pageNumber, size,
        totalElements: filtered.length, totalPages: Math.ceil(filtered.length / size),
        last: (pageNumber + 1) * size >= filtered.length, unreadCount: rows.filter((row) => !row.isRead).length,
      });
    }
    if (endpoint === '/users/me/notifications/read-all' && request.method() === 'PATCH') {
      if (api.failAll) return json(null, 503);
      const updated = rows.filter((row) => !row.isRead).length;
      rows.forEach((row) => { row.isRead = true; });
      return json({ updated });
    }
    if (endpoint.startsWith('/users/me/notifications/') && endpoint.endsWith('/read') && request.method() === 'PATCH') {
      if (api.failRead) return json(null, 503);
      if (api.readGate) await api.readGate;
      const row = rows.find((item) => endpoint.includes(item.id));
      if (!row) return json(null, 404);
      row.isRead = true;
      if (row.targetType === 'POST') row.targetAvailable = !api.deleted;
      return json(row);
    }
    if (endpoint === `/community/posts/${targetId}`) {
      if (api.detailDeleted) return json(null, 404);
      return json({ id: targetId, authorId: userId, authorName: '알림 테스트 작성자', artistId: null,
        artistName: null, artistProfileImageUrl: null, content: '알림에서 연 실제 게시글 화면', imageUrl: null,
        likeCount: 1, commentCount: 0, createdAt: '2026-11-06T10:00:00Z' });
    }
    if (endpoint === `/community/posts/${targetId}/comments`) return json({ content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 });
    if (endpoint === `/community/me/posts/${targetId}/state`) return json({ liked: false, saved: false });
    // Never forward an unhandled request to a real backend (including port 8080).
    return json({ unexpectedEndpoint: endpoint }, 501);
  });
  return api;
}

async function screenshot(page: Page, name: string, fullPage = (page.viewportSize()?.width ?? 1440) >= 1024) {
  const directory = path.resolve(__dirname, '../../.amp/in/artifacts');
  mkdirSync(directory, { recursive: true });
  // Mobile evidence captures the real viewport; scroll-end is checked separately.
  await page.screenshot({ path: path.join(directory, `notifications-${name}.png`), fullPage });
}

function firstNotification(page: Page) {
  return page.getByRole('button').filter({ has: page.getByText('게시글 반응 알림 1', { exact: true }) });
}

for (const [device, viewport] of Object.entries({ desktop: { width: 1440, height: 1000 }, mobile: { width: 390, height: 844 } })) {
  test.describe(`Notifications mocked backend: ${device}`, () => {
    test.use({ viewport });

    test('paginates with account-wide badge, supports legacy rows, then reads before navigating', async ({ page }) => {
      await mockBackend(page);
      await page.goto('/notifications');
      await expect(page.getByText('읽지 않은 알림 22개')).toBeVisible();
      await expect(page.getByRole('link', { name: '알림, 읽지 않은 알림 22개' })).toHaveText('22');
      await screenshot(page, `${device}-listing`);
      if (device === 'mobile') {
        // Full-page captures paint a fixed nav partway down the long document.
        // Verify actual scroll-end access separately rather than hiding the nav.
        await page.evaluate(() => window.scrollTo(0, document.documentElement.scrollHeight));
        const bottomNav = await page.getByRole('navigation').filter({ has: page.getByText('My', { exact: true }) }).boundingBox();
        const pagination = await page.getByRole('navigation', { name: '알림 페이지' }).boundingBox();
        const finalCard = await page.getByRole('button').filter({ has: page.getByText('게시글 반응 알림 20', { exact: true }) }).boundingBox();
        expect(bottomNav).not.toBeNull();
        expect(pagination).not.toBeNull();
        expect(finalCard).not.toBeNull();
        expect(pagination!.y + pagination!.height).toBeLessThanOrEqual(bottomNav!.y);
        expect(finalCard!.y + finalCard!.height).toBeLessThanOrEqual(bottomNav!.y);
        expect(await page.evaluate(() => document.documentElement.scrollWidth)).toBeLessThanOrEqual(viewport.width);
        await screenshot(page, `${device}-listing-scroll-end`, false);
      }
      await page.getByRole('button', { name: '다음', exact: true }).click();
      await expect(page.getByText('2 / 2')).toBeVisible();
      await expect(page.getByText('게시글 반응 알림 22', { exact: true })).toBeVisible();
      await expect(page.getByRole('button', { name: '다음', exact: true })).toBeDisabled();
      await expect(page.getByText('읽지 않은 알림 22개')).toBeVisible();
      await page.getByRole('button', { name: '이전', exact: true }).click();
      await page.getByRole('button', { name: /이전 형식의 알림/ }).click();
      await expect(page.getByText('읽지 않은 알림 21개')).toBeVisible();
      await expect(page).toHaveURL(/\/notifications$/);
      await firstNotification(page).click();
      await expect(page).toHaveURL(new RegExp(`/post-detail\\?id=${targetId}`));
      await expect(page.getByText('알림에서 연 실제 게시글 화면')).toBeVisible();
      await expect(page.getByRole('link', { name: '알림, 읽지 않은 알림 20개' })).toHaveText('20');
      await screenshot(page, `${device}-navigation-success`);
    });

    test('keeps unread data on individual read failure and allows a successful retry', async ({ page }) => {
      const api = await mockBackend(page);
      api.failRead = true;
      await page.goto('/notifications');
      await firstNotification(page).click();
      await expect(page.getByRole('alert').filter({ hasText: '알림 읽음 처리에 실패했습니다' })).toHaveText('알림 읽음 처리에 실패했습니다');
      await expect(page.getByText('읽지 않은 알림 22개')).toBeVisible();
      await expect(page).toHaveURL(/\/notifications$/);
      await screenshot(page, `${device}-read-failure`);
      api.failRead = false;
      await firstNotification(page).click();
      await expect(page.getByText('알림에서 연 실제 게시글 화면')).toBeVisible();
    });

    test('uses fresh deleted availability without navigating to a known deleted post', async ({ page }) => {
      const api = await mockBackend(page);
      await page.goto('/notifications');
      await expect(page.getByText('읽지 않은 알림 22개')).toBeVisible();
      api.deleted = true;
      await firstNotification(page).click();
      await expect(page.getByRole('status')).toHaveText(deletedMessage);
      await expect(page).toHaveURL(/\/notifications$/);
      await expect(page.getByText('읽지 않은 알림 21개')).toBeVisible();
      await screenshot(page, `${device}-deleted-target`);
    });

    test('explains a post 404 after a successful read-and-navigate race', async ({ page }) => {
      const api = await mockBackend(page);
      api.detailDeleted = true;
      await page.goto('/notifications');
      await firstNotification(page).click();
      await expect(page).toHaveURL(new RegExp(`/post-detail\\?id=${targetId}`));
      await expect(page.getByText(deletedMessage, { exact: true })).toBeVisible();
      await expect(page.getByRole('button', { name: '다시 시도' })).toBeVisible();
      await screenshot(page, `${device}-detail-404`);
    });

    test('does not navigate from a read completed after a filter change and refreshes the active list', async ({ page }) => {
      const api = await mockBackend(page);
      let release!: () => void;
      api.readGate = new Promise<void>((resolve) => { release = resolve; });
      await page.goto('/notifications');
      await firstNotification(page).click();
      await expect(firstNotification(page)).toHaveAttribute('aria-busy', 'true');
      await page.getByRole('button', { name: '읽지 않음', exact: true }).click();
      await expect(page.getByText('읽지 않은 알림 22개')).toBeVisible();
      release();
      await expect(page.getByText('읽지 않은 알림 21개')).toBeVisible();
      await expect(firstNotification(page)).toHaveCount(0);
      await expect(page).toHaveURL(/\/notifications$/);
    });

    test('handles listing/read-all failure and clears every unread page after read-all succeeds', async ({ page }) => {
      const api = await mockBackend(page);
      api.failList = true;
      await page.goto('/notifications');
      await expect(page.getByText('알림을 불러올 수 없습니다')).toBeVisible();
      api.failList = false;
      await page.getByRole('button', { name: '다시 시도' }).click();
      await page.getByRole('button', { name: '읽지 않음', exact: true }).click();
      await expect(page.getByText('1 / 2')).toBeVisible();
      await page.getByRole('button', { name: '다음', exact: true }).click();
      await expect(page.getByText('2 / 2')).toBeVisible();
      api.failAll = true;
      await page.getByRole('button', { name: '모두 읽음 처리' }).click();
      await expect(page.getByRole('alert').filter({ hasText: '알림 읽음 처리에 실패했습니다' })).toHaveText('알림 읽음 처리에 실패했습니다');
      api.failAll = false;
      await page.getByRole('button', { name: '모두 읽음 처리' }).click();
      await expect(page.getByText('읽지 않은 알림이 없습니다')).toBeVisible();
      await expect(page.getByRole('link', { name: '알림, 읽지 않은 알림 0개' })).toHaveText('');
      await expect(page.getByRole('navigation', { name: '알림 페이지' })).toHaveCount(0);
    });
  });
}
