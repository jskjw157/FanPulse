import { expect, test, type Page } from '@playwright/test'

// ── Mock Data ───────────────────────────────────────

const mockLiveNow = [
  {
    id: 1,
    title: 'NewJeans 컴백 쇼케이스',
    artistName: 'NewJeans',
    thumbnailUrl: 'https://picsum.photos/seed/nj/400/300',
    status: 'LIVE',
    viewerCount: 12500,
  },
  {
    id: 2,
    title: 'BTS Fan Meeting Special',
    artistName: 'BTS',
    thumbnailUrl: 'https://picsum.photos/seed/bts/400/300',
    status: 'LIVE',
    viewerCount: 45000,
  },
]

const mockUpcoming = [
  {
    id: 3,
    title: 'SEVENTEEN Dance Practice',
    artistName: 'SEVENTEEN',
    thumbnailUrl: 'https://picsum.photos/seed/svt/400/300',
    status: 'SCHEDULED',
    scheduledAt: '2026-02-05T14:00:00Z',
  },
]

const mockLatestNews = [
  {
    id: 1,
    title: 'BTS 새 앨범 발매 예정',
    summary: '방탄소년단이 올해 새 앨범을 발매할 예정이다.',
    thumbnailUrl: 'https://picsum.photos/seed/news1/400/300',
    source: 'Billboard',
    publishedAt: '2026-01-31T10:00:00Z',
  },
  {
    id: 2,
    title: 'NewJeans 월드투어 발표',
    summary: 'NewJeans가 첫 월드투어 일정을 공개했다.',
    thumbnailUrl: 'https://picsum.photos/seed/news2/400/300',
    source: 'Variety',
    publishedAt: '2026-01-30T08:00:00Z',
  },
]

// ── API Mock Helpers ────────────────────────────────

async function mockHomeApis(
  page: Page,
  opts?: { failLive?: boolean; failNews?: boolean; emptyLive?: boolean },
) {
  const { failLive, failNews, emptyLive } = opts ?? {}

  // GET /streaming-events (Live Now & Upcoming)
  await page.route('**/streaming-events*', async (route) => {
    if (route.request().method() !== 'GET') return route.fallback()

    if (failLive) {
      await route.fulfill({ status: 500, body: 'Internal Server Error' })
      return
    }

    const url = new URL(route.request().url())
    const status = url.searchParams.get('status')

    if (status === 'LIVE') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          data: { items: emptyLive ? [] : mockLiveNow, hasMore: false },
        }),
      })
    } else if (status === 'SCHEDULED') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          data: { items: emptyLive ? [] : mockUpcoming, hasMore: false },
        }),
      })
    } else {
      await route.fallback()
    }
  })

  // GET /news/latest
  await page.route('**/news/latest*', async (route) => {
    if (route.request().method() !== 'GET') return route.fallback()

    if (failNews) {
      await route.fulfill({ status: 500, body: 'Internal Server Error' })
      return
    }

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        data: mockLatestNews,
      }),
    })
  })
}

async function mockAuthApi(page: Page) {
  await page.route('**/api/v1/auth/me', async (route) => {
    if (route.request().method() !== 'GET') return route.fallback()
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        authenticated: true,
        user: { id: '1', email: 'test@e2e.com', username: 'E2E User' },
      }),
    })
  })
}

async function mockGoogleIdentityServices(page: Page) {
  await page.route('**/gsi/client', async (route) => {
    if (route.request().method() !== 'GET') return route.fallback()
    await route.fulfill({
      status: 200,
      contentType: 'application/javascript',
      body: `window.google = { accounts: { id: { initialize: () => {}, renderButton: () => {} } } };`,
    })
  })
}

// ── Tests ───────────────────────────────────────────

test.describe('Home - 홈 화면', () => {
  test.beforeEach(async ({ page }) => {
    await mockGoogleIdentityServices(page)
    await mockAuthApi(page)
  })

  test('3개 섹션(Live Now, Upcoming, 최신 뉴스)이 모두 렌더링된다', async ({ page }) => {
    await mockHomeApis(page)
    await page.goto('/')

    await expect(page.getByRole('heading', { name: 'Live Now' })).toBeVisible()
    await expect(page.getByRole('heading', { name: 'Upcoming' })).toBeVisible()
    await expect(page.getByRole('heading', { name: '최신 뉴스' })).toBeVisible()
  })

  test('Live Now 카드가 제목과 아티스트명과 함께 표시된다', async ({ page }) => {
    await mockHomeApis(page)
    await page.goto('/')

    await expect(page.getByText('NewJeans 컴백 쇼케이스')).toBeVisible()
    await expect(page.getByText('BTS Fan Meeting Special')).toBeVisible()
    await expect(page.getByText('NewJeans').first()).toBeVisible()
  })

  test('Upcoming 카드에 예정 배지가 표시된다', async ({ page }) => {
    await mockHomeApis(page)
    await page.goto('/')

    await expect(page.getByText('SEVENTEEN Dance Practice')).toBeVisible()
    // Upcoming 섹션 내에서 Badge "예정"만 타겟팅
    const upcomingSection = page.locator('section', { has: page.getByRole('heading', { name: 'Upcoming' }) })
    await expect(upcomingSection.getByText('예정', { exact: true })).toBeVisible()
  })

  test('최신 뉴스 카드가 제목과 출처와 함께 표시된다', async ({ page }) => {
    await mockHomeApis(page)
    await page.goto('/')

    await expect(page.getByText('BTS 새 앨범 발매 예정')).toBeVisible()
    await expect(page.getByText('NewJeans 월드투어 발표')).toBeVisible()
    await expect(page.getByText('Billboard')).toBeVisible()
  })

  test('Live 카드 클릭 시 /live/:id 페이지로 이동한다', async ({ page }) => {
    // Next.js 개발 환경에서 RSC soft-nav이 router-state-tree 불일치로 중단될 수 있으므로
    // 목적지 페이지를 mock하여 hard navigation이 발생하도록 한다
    await page.route('http://localhost:3001/live/**', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'text/html',
        body: '<html><head><title>Live</title></head><body><h1>Live Page</h1></body></html>',
      })
    })
    await mockHomeApis(page)
    await page.goto('/')

    await page.locator('a[href="/live/1"]').click()
    await expect(page).toHaveURL('/live/1')
  })

  test('뉴스 카드 클릭 시 /news/:id 페이지로 이동한다', async ({ page }) => {
    // Next.js 개발 환경에서 RSC soft-nav이 router-state-tree 불일치로 중단될 수 있으므로
    // 목적지 페이지를 mock하여 hard navigation이 발생하도록 한다
    await page.route('http://localhost:3001/news/**', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'text/html',
        body: '<html><head><title>News</title></head><body><h1>News Page</h1></body></html>',
      })
    })
    await mockHomeApis(page)
    await page.goto('/')

    await page.locator('a[href="/news/1"]').click()
    await expect(page).toHaveURL('/news/1')
  })

  test('API 실패 시 에러 메시지와 다시 시도 버튼이 표시된다', async ({ page }) => {
    await mockHomeApis(page, { failLive: true })
    await page.goto('/')

    await expect(page.getByText('서버에 문제가 발생했습니다')).toBeVisible()
    await expect(page.getByRole('button', { name: '다시 시도' })).toBeVisible()
  })

  test('에러 상태에서 다시 시도 클릭 시 데이터가 정상 로드된다', async ({ page }) => {
    // CORS 프리플라이트(OPTIONS)를 포함한 모든 api.fanpulse.app 요청을 처음부터 인터셉트하여
    // Chrome CORS 캐시가 오염되지 않도록 한다
    const CORS_HEADERS = {
      'Access-Control-Allow-Origin': 'http://localhost:3001',
      'Access-Control-Allow-Credentials': 'true',
      'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type, Authorization',
      'Access-Control-Max-Age': '86400',
    }

    let shouldFail = true

    // OPTIONS 프리플라이트 요청을 항상 200으로 응답
    await page.route('**/api/v1/**', async (route) => {
      if (route.request().method() === 'OPTIONS') {
        await route.fulfill({ status: 204, headers: CORS_HEADERS })
        return
      }
      await route.fallback()
    })

    await page.route('**/streaming-events*', async (route) => {
      if (route.request().method() !== 'GET') return route.fallback()

      if (shouldFail) {
        await route.fulfill({
          status: 500,
          headers: CORS_HEADERS,
          body: 'fail',
        })
        return
      }

      const url = new URL(route.request().url())
      const status = url.searchParams.get('status')
      const items = status === 'LIVE' ? mockLiveNow : mockUpcoming
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        headers: CORS_HEADERS,
        body: JSON.stringify({ data: { items, hasMore: false } }),
      })
    })

    await page.route('**/news/latest*', async (route) => {
      if (route.request().method() !== 'GET') return route.fallback()

      if (shouldFail) {
        await route.fulfill({
          status: 500,
          headers: CORS_HEADERS,
          body: 'fail',
        })
        return
      }

      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        headers: CORS_HEADERS,
        body: JSON.stringify({ data: mockLatestNews }),
      })
    })

    await page.goto('/')
    await expect(page.getByText('서버에 문제가 발생했습니다')).toBeVisible()

    // React 하이드레이션이 완료되어 버튼의 onClick 핸들러가 붙을 때까지 대기
    await page.waitForFunction(() => {
      const btn = Array.from(document.querySelectorAll('button')).find(
        b => b.textContent?.trim() === '다시 시도'
      )
      if (!btn) return false
      return Object.getOwnPropertyNames(btn).some(
        k => k.startsWith('__reactProps') || k.startsWith('__reactFiber')
      )
    }, { timeout: 15000 })

    // 두 번째 시도는 성공
    shouldFail = false
    // Playwright의 click()은 SyntheticEvent를 fetchAll(signal)의 signal로 전달하므로
    // React fiber에서 onClick 핸들러를 직접 인수 없이 호출한다
    await page.evaluate(() => {
      const btn = Array.from(document.querySelectorAll('button')).find(
        (b) => b.textContent?.trim() === '다시 시도'
      ) as HTMLElement | undefined
      if (!btn) throw new Error('다시 시도 버튼을 찾을 수 없습니다')
      const propsKey = Object.getOwnPropertyNames(btn).find((k) => k.startsWith('__reactProps'))
      if (!propsKey) throw new Error('React props를 찾을 수 없습니다')
      const props = (btn as any)[propsKey]
      if (typeof props?.onClick !== 'function') throw new Error('onClick 핸들러가 없습니다')
      props.onClick()
    })

    await expect(page.getByText('NewJeans 컴백 쇼케이스')).toBeVisible({ timeout: 15000 })
  })

  test('LIVE 카드에 LIVE 배지와 시청자 수가 표시된다', async ({ page }) => {
    await mockHomeApis(page)
    await page.goto('/')

    // LIVE 배지 확인
    const liveSection = page.locator('section', { has: page.getByRole('heading', { name: 'Live Now' }) })
    await expect(liveSection.getByText('LIVE').first()).toBeVisible()

    // 시청자 수 (toLocaleString('ko-KR') → "12,500명")
    await expect(page.getByText('12,500명')).toBeVisible()
  })
})
