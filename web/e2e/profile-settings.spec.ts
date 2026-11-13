import { expect, test, type Page } from '@playwright/test';
import path from 'node:path';

const artifacts = path.resolve(__dirname, '../../.amp/in/artifacts');

const profile = {
  id: '11111111-1111-4111-8111-111111111111', email: 'fan@example.com', username: 'Fan_01',
  hasPassword: false, createdAt: '2026-01-01T00:00:00Z',
};

async function mockAccount(page: Page) {
  const state = { username: profile.username, language: 'ko', failSave: false, failLoad: false };
  await page.route('**/api/v1/**', async (route) => {
    const request = route.request();
    const path = new URL(request.url()).pathname.replace('/api/v1', '');
    const user = { ...profile, username: state.username };
    const settings = { theme: 'light', language: state.language, pushEnabled: true, updatedAt: '2026-01-01T00:00:00Z' };
    if (path === '/auth/me') return route.fulfill({ json: { authenticated: true, user } });
    // #296 independently changes the GET path; mutations always use the existing /me.
    if ((path === '/me' || path === '/users/me') && request.method() === 'GET') return route.fulfill({ json: user });
    if (path === '/me' && request.method() === 'PATCH') {
      const { username } = request.postDataJSON();
      if (username === 'Taken_Name') return route.fulfill({ status: 409, json: {
        title: 'Username Already Exists', status: 409, errorCode: 'USERNAME_ALREADY_EXISTS',
      } });
      state.username = username;
      return route.fulfill({ json: { ...user, username } });
    }
    if (path === '/me/settings') {
      if (state.failLoad || (request.method() === 'PATCH' && state.failSave)) {
        return route.fulfill({ status: 500, json: { title: 'Server error', status: 500, errorCode: 'INTERNAL_ERROR' } });
      }
      if (request.method() === 'PATCH') {
        const payload = request.postDataJSON();
        expect(Object.keys(payload)).toEqual(['language']);
        expect(['ko', 'en']).toContain(payload.language);
        state.language = payload.language;
      }
      return route.fulfill({ json: { ...settings, language: state.language } });
    }
    return route.abort();
  });
  return state;
}

test('nickname edit shows duplicate errors, saves, and survives reload', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await mockAccount(page);
  await page.goto('/mypage');
  await expect(page.getByRole('heading', { name: 'Fan_01' })).toBeVisible();
  await page.screenshot({ path: path.join(artifacts, 'profile-default.png') });
  await page.getByRole('button', { name: '닉네임 편집' }).click();
  await page.getByLabel('닉네임', { exact: true }).fill('Taken_Name');
  await page.getByRole('button', { name: '저장', exact: true }).click();
  await expect(page.getByRole('alert').filter({ hasText: '이미 사용 중' })).toBeVisible();
  await page.screenshot({ path: path.join(artifacts, 'profile-duplicate.png') });
  await page.getByLabel('닉네임', { exact: true }).fill('Updated_Fan');
  await page.getByRole('button', { name: '저장', exact: true }).click();
  await expect(page.getByRole('heading', { name: 'Updated_Fan' })).toBeVisible();
  await expect(page.getByRole('status')).toContainText('닉네임을 저장했습니다');
  await page.reload();
  await expect(page.getByRole('heading', { name: 'Updated_Fan' })).toBeVisible();
  const longestName = 'x'.repeat(50);
  await page.getByRole('button', { name: '닉네임 편집' }).click();
  await page.getByLabel('닉네임', { exact: true }).fill(longestName);
  await page.getByRole('button', { name: '저장', exact: true }).click();
  await expect(page.getByRole('heading', { name: longestName })).toBeVisible();
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true);
  await page.screenshot({ path: path.join(artifacts, 'profile-max-length.png') });
});

test('language save, failure, reload restoration, and load retry render correctly', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  const account = await mockAccount(page);
  await page.goto('/settings');
  await expect(page.getByRole('combobox', { name: '언어', exact: true })).toHaveValue('ko');
  await page.screenshot({ path: path.join(artifacts, 'settings-ko.png') });
  account.failSave = true;
  await page.getByRole('combobox', { name: '언어', exact: true }).selectOption('en');
  await page.getByRole('button', { name: '저장', exact: true }).click();
  await expect(page.getByRole('alert').filter({ hasText: '기존 언어를 유지' })).toBeVisible();
  await expect(page.locator('html')).toHaveAttribute('lang', 'ko');
  await page.screenshot({ path: path.join(artifacts, 'settings-save-failure.png') });
  account.failSave = false;
  await page.getByRole('button', { name: '저장', exact: true }).click();
  await expect(page.getByRole('heading', { name: 'Settings', exact: true })).toBeVisible();
  await expect(page.getByRole('status')).toContainText('Language saved.');
  await expect(page.locator('html')).toHaveAttribute('lang', 'en');
  await page.screenshot({ path: path.join(artifacts, 'settings-en-mobile.png') });
  await page.setViewportSize({ width: 1280, height: 900 });
  await page.screenshot({ path: path.join(artifacts, 'settings-en-desktop.png') });
  await page.reload();
  await expect(page.getByRole('combobox', { name: 'Language', exact: true })).toHaveValue('en');
  await expect(page.getByRole('heading', { name: 'Settings', exact: true })).toBeVisible();
  account.failLoad = true;
  await page.reload();
  await expect(page.getByRole('alert').filter({ hasText: '불러오지 못했습니다' })).toBeVisible();
  await page.screenshot({ path: path.join(artifacts, 'settings-load-failure.png') });
  account.failLoad = false;
  await page.getByRole('button', { name: '다시 시도' }).click();
  await expect(page.getByRole('combobox', { name: 'Language', exact: true })).toHaveValue('en');
});
