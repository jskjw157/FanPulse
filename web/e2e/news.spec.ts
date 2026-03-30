import { expect, test, type Page } from '@playwright/test'

// ── Mock Data ───────────────────────────────────────

const mockNewsDetail = {
  id: 1,
  title: 'BTS 새 앨범 발매 예정',
  summary: 'BTS가 2026년 3월 새 앨범 발매를 예고했다.',
  thumbnailUrl: 'https://picsum.photos/seed/bts-news/800/450',
  source: '스포츠조선',
  publishedAt: '2026-02-01T09:00:00Z',
  content: `
    <p>BTS가 2026년 3월 새 앨범 발매를 예고했다. 멤버들의 솔로 활동 이후 첫 완전체 앨범으로 팬들의 기대를 모으고 있다.</p>
    <p>소속사 하이브에 따르면, 이번 앨범은 전 멤버가 참여한 자작곡으로 구성되며, 월드투어도 함께 진행될 예정이다.</p>
    <img src="https://picsum.photos/seed/bts-album/600/400" alt="BTS 앨범 커버" />
    <p>BTS는 "팬들에게 새로운 모습을 보여드리고 싶다"고 전했다.</p>
  `,
  sourceUrl: 'https://sports.chosun.com/article/bts-2026',
  author: '김기자',
}

const mockNewsDetail2 = {
  id: 2,
  title: 'BLACKPINK 월드투어 추가 공연',
  summary: 'BLACKPINK가 아시아 투어에 서울 추가 공연을 확정했다.',
  thumbnailUrl: 'https://picsum.photos/seed/bp-news/800/450',
  source: '엔터미디어',
  publishedAt: '2026-01-31T15:30:00Z',
  content: '<p>BLACKPINK가 아시아 투어에 서울 추가 공연을 확정했다.</p>',
  sourceUrl: 'https://enter.media.com/article/bp-tour',
}

// ── Setup Helper ────────────────────────────────

async function setupMocks(
  page: Page,
  opts?: { notFound?: boolean; failApi?: boolean; newsData?: typeof mockNewsDetail }
) {
  const { notFound, failApi, newsData = mockNewsDetail } = opts ?? {}

  // Mock Google Identity Services
  await page.route('**/gsi/client', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/javascript',
      body: 'window.google={accounts:{id:{initialize:()=>{},renderButton:()=>{}}}};',
    })
  })

  // Mock Auth API
  await page.route('**/api/v1/auth/me', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ authenticated: true, user: { id: '1' } }),
    })
  })

  // Mock News API
  await page.route('**/api/v1/news/*', async (route) => {
    if (route.request().method() !== 'GET') return route.fallback()

    if (failApi) {
      await route.fulfill({ status: 500, body: 'Internal Server Error' })
      return
    }

    if (notFound) {
      await route.fulfill({
        status: 404,
        contentType: 'application/json',
        body: JSON.stringify({
          error: { code: 'NOT_FOUND', message: '뉴스를 찾을 수 없습니다' },
        }),
      })
      return
    }

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ data: newsData }),
    })
  })
}

// ── Tests: News Detail ────────────────────────────────

test.describe('News Detail - 뉴스 상세', () => {
  test('뉴스 상세 페이지에서 제목이 h1 태그로 표시된다', async ({ page }) => {
    await setupMocks(page)
    await page.goto('/news/1')

    await expect(
      page.getByRole('heading', { name: 'BTS 새 앨범 발매 예정', level: 1 })
    ).toBeVisible({ timeout: 10000 })
  })

  test('썸네일 이미지가 표시된다', async ({ page }) => {
    await setupMocks(page)
    await page.goto('/news/1')

    await expect(page.getByText('스포츠조선', { exact: true })).toBeVisible({ timeout: 10000 })
    await expect(page.locator('img[alt="BTS 새 앨범 발매 예정"]')).toBeVisible()
  })

  test('메타데이터(출처, 작성자, 날짜)가 표시된다', async ({ page }) => {
    await setupMocks(page)
    await page.goto('/news/1')

    await expect(page.getByText('스포츠조선', { exact: true })).toBeVisible({ timeout: 10000 })
    await expect(page.getByText('김기자')).toBeVisible()
  })

  test('HTML 본문 콘텐츠가 렌더링된다', async ({ page }) => {
    await setupMocks(page)
    await page.goto('/news/1')

    await expect(
      page.getByText(/멤버들의 솔로 활동 이후 첫 완전체 앨범/)
    ).toBeVisible({ timeout: 10000 })
  })

  test('본문 내 이미지가 렌더링된다', async ({ page }) => {
    await setupMocks(page)
    await page.goto('/news/1')

    await expect(page.getByText('스포츠조선', { exact: true })).toBeVisible({ timeout: 10000 })
    await expect(page.locator('img[alt="BTS 앨범 커버"]')).toBeVisible()
  })

  test('원문 링크 버튼이 표시되고 올바른 URL을 가진다', async ({ page }) => {
    await setupMocks(page)
    await page.goto('/news/1')

    await expect(page.getByText('스포츠조선', { exact: true })).toBeVisible({ timeout: 10000 })

    const sourceLink = page.getByRole('link', { name: /스포츠조선에서 원문 보기/ })
    await expect(sourceLink).toBeVisible()
    await expect(sourceLink).toHaveAttribute(
      'href',
      'https://sports.chosun.com/article/bts-2026'
    )
    await expect(sourceLink).toHaveAttribute('target', '_blank')
    await expect(sourceLink).toHaveAttribute('rel', 'noopener noreferrer')
  })

  test('뒤로가기 버튼이 표시된다', async ({ page }) => {
    await setupMocks(page)
    await page.goto('/news/1')

    await expect(page.getByRole('button', { name: /뒤로/i })).toBeVisible({
      timeout: 10000,
    })
  })

  test('작성자가 없는 뉴스도 정상 렌더링된다', async ({ page }) => {
    await setupMocks(page, { newsData: mockNewsDetail2 })
    await page.goto('/news/2')

    await expect(page.getByText('엔터미디어', { exact: true })).toBeVisible({ timeout: 10000 })
    await expect(
      page.getByRole('heading', { name: 'BLACKPINK 월드투어 추가 공연', level: 1 })
    ).toBeVisible()
    await expect(page.getByText('김기자')).not.toBeVisible()
  })

  test('존재하지 않는 뉴스 ID로 접근 시 404 에러 메시지가 표시된다', async ({
    page,
  }) => {
    await setupMocks(page, { notFound: true })
    await page.goto('/news/999')

    await expect(page.getByText('뉴스를 찾을 수 없습니다')).toBeVisible({
      timeout: 10000,
    })
    await expect(page.getByRole('link', { name: '홈으로 이동' })).toBeVisible()
  })

  test('404 에러 상태에서 "홈으로 이동" 클릭 시 홈으로 이동한다', async ({
    page,
  }) => {
    await setupMocks(page, { notFound: true })
    await page.goto('/news/999')

    await expect(page.getByText('뉴스를 찾을 수 없습니다')).toBeVisible({
      timeout: 10000,
    })
    await page.getByRole('link', { name: '홈으로 이동' }).click()
    await expect(page).toHaveURL('/')
  })

  test('네트워크 에러 시 에러 메시지와 다시 시도 버튼이 표시된다', async ({
    page,
  }) => {
    await setupMocks(page, { failApi: true })
    await page.goto('/news/1')

    await expect(page.getByText('뉴스를 불러올 수 없습니다')).toBeVisible({
      timeout: 10000,
    })
    await expect(page.getByRole('button', { name: '다시 시도' })).toBeVisible()
  })

  test('다시 시도 버튼 클릭 시 API를 재호출한다', async ({ page }) => {
    // 기존 라우트 모두 제거하고 새로 설정
    await page.unrouteAll()

    // Mock Google Identity Services
    await page.route('**/gsi/client', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/javascript',
        body: 'window.google={accounts:{id:{initialize:()=>{},renderButton:()=>{}}}};',
      })
    })

    // Mock Auth API
    await page.route('**/api/v1/auth/me', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ authenticated: true, user: { id: '1' } }),
      })
    })

    // 첫 번째 요청은 실패, 이후 성공
    let requestCount = 0
    await page.route('**/api/v1/news/*', async (route) => {
      if (route.request().method() !== 'GET') return route.fallback()

      requestCount++
      if (requestCount <= 2) {
        // 첫 번째, 두 번째 요청 실패 (Next.js가 2번 호출할 수 있음)
        await route.fulfill({ status: 500, body: 'Internal Server Error' })
      } else {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ data: mockNewsDetail }),
        })
      }
    })

    await page.goto('/news/1')

    await expect(page.getByText('뉴스를 불러올 수 없습니다')).toBeVisible({
      timeout: 10000,
    })

    await page.getByRole('button', { name: '다시 시도' }).click()

    await expect(
      page.getByRole('heading', { name: 'BTS 새 앨범 발매 예정', level: 1 })
    ).toBeVisible({ timeout: 10000 })
  })
})
