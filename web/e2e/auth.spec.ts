import { expect, test, type Page } from '@playwright/test'

/**
 * Google Identity Services 모킹
 * - 실제 Google OAuth 대신 E2E용 모의 로그인 버튼 렌더링
 */
async function mockGoogleIdentityServices(page: Page, opts?: { credential?: string }) {
  const credential = opts?.credential ?? 'e2e-id-token'

  await page.route('**/gsi/client', async (route) => {
    if (route.request().method() !== 'GET') return route.fallback()

    await route.fulfill({
      status: 200,
      contentType: 'application/javascript',
      body: `(() => {
  const callbackHolder = { cb: null };

  window.google = {
    accounts: {
      id: {
        initialize: (opts) => {
          callbackHolder.cb = opts && opts.callback ? opts.callback : null;
        },
        renderButton: (container) => {
          if (!container) return;
          container.innerHTML = '<button id="gis-btn" type="button">Google Sign-In (E2E)</button>';
          const btn = container.querySelector('#gis-btn');
          if (!btn) return;
          btn.addEventListener('click', () => {
            if (typeof callbackHolder.cb === 'function') {
              callbackHolder.cb({ credential: ${JSON.stringify(credential)} });
            }
          });
        },
      },
    },
  };
})();`,
    })
  })
}

/**
 * 인증 API 모킹
 * - /auth/google: 로그인 성공 시 쿠키 설정 (헤더로)
 * - /auth/me: 인증 상태 확인
 * - /auth/logout: 로그아웃
 */
async function mockAuthApi(page: Page, opts?: { authenticated?: boolean }) {
  const isAuthenticated = opts?.authenticated ?? false

  // POST /auth/google - 로그인
  await page.route('**/api/v1/auth/google', async (route) => {
    if (route.request().method() !== 'POST') return route.fallback()

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      headers: {
        'Set-Cookie': [
          'fanpulse_access_token=e2e-access-token; Path=/; HttpOnly; SameSite=Lax',
          'fanpulse_refresh_token=e2e-refresh-token; Path=/; HttpOnly; SameSite=Lax',
        ].join(', '),
      },
      body: JSON.stringify({
        user: {
          id: '1',
          email: 'test@example.com',
          username: 'E2E User',
        },
      }),
    })
  })

  // GET /auth/me - 인증 상태 확인
  await page.route('**/api/v1/auth/me', async (route) => {
    if (route.request().method() !== 'GET') return route.fallback()

    if (isAuthenticated) {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          authenticated: true,
          user: {
            id: '1',
            email: 'test@example.com',
            username: 'E2E User',
          },
        }),
      })
    } else {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          authenticated: false,
          user: null,
        }),
      })
    }
  })

  // POST /auth/logout - 로그아웃
  await page.route('**/api/v1/auth/logout', async (route) => {
    if (route.request().method() !== 'POST') return route.fallback()

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      headers: {
        'Set-Cookie': [
          'fanpulse_access_token=; Path=/; HttpOnly; Max-Age=0',
          'fanpulse_refresh_token=; Path=/; HttpOnly; Max-Age=0',
        ].join(', '),
      },
      body: JSON.stringify({ success: true }),
    })
  })
}

test.describe('Auth - 로그인 페이지', () => {
  test('로그인 페이지에 Google 로그인 버튼이 표시된다', async ({ page }) => {
    await mockGoogleIdentityServices(page)
    await mockAuthApi(page)
    await page.goto('/login')

    await expect(
      page.getByRole('heading', { name: 'Welcome to FanPulse' }),
    ).toBeVisible()
    await expect(page.locator('#gis-btn')).toBeVisible()
  })

  test('Google 로그인 성공 시 홈으로 리다이렉트된다', async ({ page }) => {
    await mockGoogleIdentityServices(page)
    await mockAuthApi(page)

    // 로그인 후에는 인증된 상태로 변경
    let loggedIn = false
    await page.route('**/api/v1/auth/me', async (route) => {
      if (route.request().method() !== 'GET') return route.fallback()

      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          authenticated: loggedIn,
          user: loggedIn
            ? { id: '1', email: 'test@example.com', username: 'E2E User' }
            : null,
        }),
      })
    })

    await page.route('**/api/v1/auth/google', async (route) => {
      if (route.request().method() !== 'POST') return route.fallback()

      loggedIn = true
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          user: { id: '1', email: 'test@example.com', username: 'E2E User' },
        }),
      })
    })

    await page.goto('/login')
    await page.locator('#gis-btn').click()

    await expect(page).toHaveURL('/')
  })
})

test.describe('Auth - ProtectedRoute', () => {
  test('비로그인 상태에서 마이페이지 접근 시 로그인 페이지로 리다이렉트된다', async ({
    page,
  }) => {
    await mockGoogleIdentityServices(page)
    await mockAuthApi(page, { authenticated: false })

    await page.goto('/mypage')

    await expect(page).toHaveURL('/login')
  })

  test('비로그인 상태에서 알림 페이지 접근 시 로그인 페이지로 리다이렉트된다', async ({
    page,
  }) => {
    await mockGoogleIdentityServices(page)
    await mockAuthApi(page, { authenticated: false })

    await page.goto('/notifications')

    await expect(page).toHaveURL('/login')
  })

  test('비로그인 상태에서 게시글 작성 접근 시 로그인 페이지로 리다이렉트된다', async ({
    page,
  }) => {
    await mockGoogleIdentityServices(page)
    await mockAuthApi(page, { authenticated: false })

    await page.goto('/post-create')

    await expect(page).toHaveURL('/login')
  })

  test('로그인 상태에서 마이페이지 접근 시 정상 표시된다', async ({ page }) => {
    await mockGoogleIdentityServices(page)
    await mockAuthApi(page, { authenticated: true })

    await page.goto('/mypage')

    await expect(page).toHaveURL('/mypage')
    // 마이페이지 헤더, 사용자 정보, 로그아웃 버튼이 표시되는지 확인
    await expect(page.getByRole('heading', { name: '마이페이지' })).toBeVisible()
    await expect(page.getByText('E2E User')).toBeVisible() // AuthContext에서 가져온 사용자 이름
    await expect(page.getByText('test@example.com')).toBeVisible() // 이메일
    await expect(page.getByRole('button', { name: '로그아웃' })).toBeVisible()
  })
})

test.describe('Auth - 로그아웃', () => {
  test('마이페이지에서 로그아웃 시 로그인 페이지로 이동한다', async ({ page }) => {
    await mockGoogleIdentityServices(page)

    // 초기에는 로그인 상태
    let isLoggedIn = true
    await page.route('**/api/v1/auth/me', async (route) => {
      if (route.request().method() !== 'GET') return route.fallback()

      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          authenticated: isLoggedIn,
          user: isLoggedIn
            ? { id: '1', email: 'test@example.com', username: 'E2E User' }
            : null,
        }),
      })
    })

    await page.route('**/api/v1/auth/logout', async (route) => {
      if (route.request().method() !== 'POST') return route.fallback()

      isLoggedIn = false
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ success: true }),
      })
    })

    await page.goto('/mypage')
    await expect(page).toHaveURL('/mypage')

    // 로그아웃 버튼 클릭
    await page.getByRole('button', { name: '로그아웃' }).click()

    await expect(page).toHaveURL('/login')
  })
})
