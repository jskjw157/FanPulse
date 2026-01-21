import { test, expect } from '@playwright/test';

test.describe('FanPulse Navigation Tests', () => {
  test('홈페이지 렌더링', async ({ page }) => {
    await page.goto('/');

    // 헤더 확인
    await expect(page.locator('header')).toBeVisible();
    await expect(page.locator('header h1')).toContainText('FanPulse');

    // 메인 섹션 확인
    await expect(page.getByRole('heading', { name: 'Welcome to FanPulse' })).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Live Now' })).toBeVisible();
    await expect(page.getByRole('heading', { name: '인기 게시글' })).toBeVisible();
    await expect(page.getByRole('heading', { name: '실시간 차트' })).toBeVisible();
  });

test('데스크탑 헤더 네비게이션 동작', async ({ page, isMobile }) => {
    // 모바일에서는 헤더 네비게이션이 숨겨지므로 스킵
    test.skip(isMobile === true, '헤더 네비게이션은 데스크탑 전용');

    await page.goto('/');

    // 헤더 내 네비게이션 링크 클릭
    const headerNav = page.locator('header nav');

    // Community 링크 클릭
    await headerNav.getByRole('link', { name: 'Community' }).click();
    await expect(page).toHaveURL('/community');

    // Live 링크 클릭
    await headerNav.getByRole('link', { name: 'Live' }).click();
    await expect(page).toHaveURL('/live');

    // Voting 링크 클릭
    await headerNav.getByRole('link', { name: 'Voting' }).click();
    await expect(page).toHaveURL('/voting');

    // Chart 링크 클릭
    await headerNav.getByRole('link', { name: 'Chart' }).click();
    await expect(page).toHaveURL('/chart');

    // Concert 링크 클릭
    await headerNav.getByRole('link', { name: 'Concert' }).click();
    await expect(page).toHaveURL('/concert');

    // Home으로 돌아가기
    await headerNav.getByRole('link', { name: 'Home' }).click();
    await expect(page).toHaveURL('/');
  });

  test('검색 페이지 접근', async ({ page }) => {
    await page.goto('/');

    // 검색 아이콘 클릭
    await page.locator('a[href="/search"]').first().click();
    await expect(page).toHaveURL('/search');
  });

  test('알림 페이지 접근', async ({ page }) => {
    await page.goto('/');

    // 알림 아이콘 클릭
    await page.locator('a[href="/notifications"]').first().click();
    await expect(page).toHaveURL('/notifications');
  });
});

test.describe('FanPulse Page Rendering Tests', () => {
  test('Community 페이지 렌더링', async ({ page }) => {
    await page.goto('/community');

    // 페이지 로드 확인 - first()로 첫 번째 요소 선택
    await expect(page.locator('header').first()).toBeVisible();
    await expect(page.locator('main')).toBeVisible();
  });

  test('Live 페이지 렌더링', async ({ page }) => {
    await page.goto('/live');

    await expect(page.locator('header').first()).toBeVisible();
    await expect(page.locator('main')).toBeVisible();
  });

  test('Voting 페이지 렌더링', async ({ page }) => {
    await page.goto('/voting');

    await expect(page.locator('header').first()).toBeVisible();
    await expect(page.locator('main')).toBeVisible();
  });

  test('Chart 페이지 렌더링', async ({ page }) => {
    await page.goto('/chart');

    await expect(page.locator('header').first()).toBeVisible();
    await expect(page.locator('main')).toBeVisible();
  });

  test('Concert 페이지 렌더링', async ({ page }) => {
    await page.goto('/concert');

    await expect(page.locator('header').first()).toBeVisible();
    await expect(page.locator('main')).toBeVisible();
  });

  test('Search 페이지 렌더링', async ({ page }) => {
    await page.goto('/search');

    await expect(page.locator('header').first()).toBeVisible();
    await expect(page.locator('main')).toBeVisible();
  });

  test('MyPage 페이지 렌더링', async ({ page }) => {
    await page.goto('/mypage');

    await expect(page.locator('header').first()).toBeVisible();
    await expect(page.locator('main')).toBeVisible();
  });

  test('Login 페이지 렌더링', async ({ page }) => {
    await page.goto('/login');

    // 로그인 페이지는 풀스크린 - 로고 확인
    await expect(page.locator('h1').first()).toBeVisible();
  });
});

test.describe('Mobile Navigation Tests', () => {
  test.use({ viewport: { width: 375, height: 667 } });

  test('모바일 하단 네비게이션 표시', async ({ page }) => {
    await page.goto('/');

    // 하단 네비게이션 바 확인
    const bottomNav = page.locator('nav.fixed.bottom-0');
    await expect(bottomNav).toBeVisible();

    // 5개 탭 확인
    await expect(bottomNav.getByText('Home')).toBeVisible();
    await expect(bottomNav.getByText('Community')).toBeVisible();
    await expect(bottomNav.getByText('Live')).toBeVisible();
    await expect(bottomNav.getByText('Voting')).toBeVisible();
    await expect(bottomNav.getByText('My')).toBeVisible();
  });

  test('모바일 하단 네비게이션 동작', async ({ page }) => {
    await page.goto('/');

    const bottomNav = page.locator('nav.fixed.bottom-0');

    // Community 탭 클릭
    await bottomNav.getByText('Community').click();
    await expect(page).toHaveURL('/community');

    // Live 탭 클릭
    await bottomNav.getByText('Live').click();
    await expect(page).toHaveURL('/live');

    // Home 탭 클릭
    await bottomNav.getByText('Home').click();
    await expect(page).toHaveURL('/');
  });
});
