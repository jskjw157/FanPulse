import { expect, test } from '@playwright/test';

const profile = {
  id: '11111111-1111-1111-1111-111111111111',
  email: 'live@example.com',
  username: '실제 API 프로필',
  hasPassword: false,
  createdAt: '2026-01-02T03:04:05Z',
};
const authStatus = {
  authenticated: true,
  user: { id: profile.id, username: 'Cached Name', email: 'cached@example.com' },
};

test('profile failure remains visible until explicit retry succeeds', async ({ page }, testInfo) => {
  await page.setViewportSize({ width: 390, height: 844 });
  let profileRequests = 0;
  let recovering = false;
  let finishRetry!: () => void;
  const retryPending = new Promise<void>((resolve) => { finishRetry = resolve; });
  await page.route('**/api/v1/**', async (route) => {
    const path = new URL(route.request().url()).pathname;
    if (path === '/api/v1/auth/me') {
      return route.fulfill({ json: authStatus });
    }
    if (path === '/api/v1/me' && route.request().method() === 'GET') {
      profileRequests++;
      if (!recovering) {
        return route.fulfill({ status: 503, json: { detail: 'Service unavailable' } });
      }
      await retryPending;
      return route.fulfill({ json: profile });
    }
    return route.fulfill({ status: 404, json: { detail: 'Unexpected API request' } });
  });

  await page.goto('/mypage');
  const profileError = page.getByRole('main').getByRole('alert');
  await expect(profileError).toContainText('프로필을 불러올 수 없습니다');
  const failedRequests = profileRequests;
  await expect(page.getByText('Cached Name')).toHaveCount(0);
  await expect(page.getByText('cached@example.com')).toHaveCount(0);
  await page.screenshot({ path: testInfo.outputPath('profile-error-mobile.png') });
  await page.setViewportSize({ width: 1280, height: 900 });
  await page.screenshot({ path: testInfo.outputPath('profile-error-desktop.png') });
  expect(profileRequests).toBe(failedRequests);

  await page.setViewportSize({ width: 390, height: 844 });
  recovering = true;
  await page.getByRole('button', { name: '다시 시도' }).click();
  await expect(page.getByRole('status', { name: '프로필을 불러오는 중입니다' })).toBeVisible();
  await expect(page.getByRole('button', { name: '다시 시도' })).toHaveCount(0);
  await page.screenshot({ path: testInfo.outputPath('profile-loading-mobile.png') });
  finishRetry();

  await expect(page.getByRole('heading', { name: profile.username })).toBeVisible();
  await expect(page.getByText(profile.email)).toBeVisible();
  await expect(profileError).toHaveCount(0);
  await expect(page.getByText('Cached Name')).toHaveCount(0);
  await expect(page).toHaveURL('/mypage');
  expect(profileRequests).toBe(failedRequests + 1);
  await page.screenshot({ path: testInfo.outputPath('profile-success-mobile.png') });
});

test('profile 401 keeps the existing web session refresh and request retry', async ({ page }) => {
  let profileRequests = 0;
  let refreshRequests = 0;
  await page.route('**/api/v1/**', async (route) => {
    const path = new URL(route.request().url()).pathname;
    if (path === '/api/v1/auth/me') {
      return route.fulfill({ json: authStatus });
    }
    if (path === '/api/v1/me' && route.request().method() === 'GET') {
      profileRequests++;
      return refreshRequests === 0
        ? route.fulfill({ status: 401, json: { detail: 'Access cookie expired' } })
        : route.fulfill({ json: profile });
    }
    if (path === '/api/v1/auth/web/refresh' && route.request().method() === 'POST') {
      refreshRequests++;
      return route.fulfill({ status: 200, json: {} });
    }
    return route.fulfill({ status: 404, json: {} });
  });

  await page.goto('/mypage');
  await expect(page.getByRole('heading', { name: profile.username })).toBeVisible();
  await expect(page.getByText(profile.email)).toBeVisible();
  await expect(page.getByRole('main').getByRole('alert')).toHaveCount(0);
  await expect(page).toHaveURL('/mypage');
  expect(profileRequests).toBeGreaterThanOrEqual(2);
  expect(refreshRequests).toBe(1);
});
