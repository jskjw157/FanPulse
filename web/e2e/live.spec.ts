import { expect, test, type Page } from '@playwright/test'

// ── Mock Data ───────────────────────────────────────

const mockLiveList = [
  {
    id: 1,
    title: 'NewJeans 컴백 쇼케이스',
    artistName: 'NewJeans Official',
    thumbnailUrl: 'https://picsum.photos/seed/nj/400/300',
    status: 'LIVE',
    viewerCount: 24583,
  },
  {
    id: 2,
    title: 'BTS Fan Meeting Special',
    artistName: 'BTS',
    thumbnailUrl: 'https://picsum.photos/seed/bts/400/300',
    status: 'LIVE',
    viewerCount: 89200,
  },
  {
    id: 3,
    title: 'BLACKPINK Behind The Scenes',
    artistName: 'BLACKPINK',
    thumbnailUrl: 'https://picsum.photos/seed/bp/400/300',
    status: 'SCHEDULED',
    scheduledAt: '2026-02-15T14:00:00Z',
  },
  {
    id: 4,
    title: 'SEVENTEEN Dance Practice',
    artistName: 'SEVENTEEN',
    thumbnailUrl: 'https://picsum.photos/seed/svt/400/300',
    status: 'ENDED',
  },
]

const mockLiveListPage2 = [
  {
    id: 5,
    title: 'Stray Kids World Tour Highlights',
    artistName: 'Stray Kids',
    thumbnailUrl: 'https://picsum.photos/seed/skz/400/300',
    status: 'ENDED',
  },
  {
    id: 6,
    title: 'TWICE Concert Replay',
    artistName: 'TWICE',
    thumbnailUrl: 'https://picsum.photos/seed/twice/400/300',
    status: 'ENDED',
  },
]

const mockLiveDetail = {
  id: 1,
  title: 'NewJeans 컴백 쇼케이스',
  artistName: 'NewJeans Official',
  thumbnailUrl: 'https://picsum.photos/seed/nj/400/300',
  status: 'LIVE',
  description:
    'NewJeans의 새 앨범 "How Sweet" 컴백 쇼케이스 라이브 방송입니다. 새로운 타이틀곡 무대와 멤버들의 이야기를 만나보세요.',
  streamUrl: 'https://www.youtube.com/embed/dQw4w9WgXcQ',
  scheduledAt: '2026-02-01T14:00:00Z',
  startedAt: '2026-02-01T14:00:00Z',
  viewerCount: 24583,
}

// ── API Mock Helpers ────────────────────────────────

async function mockLiveListApi(
  page: Page,
  opts?: { failApi?: boolean; emptyList?: boolean; withPagination?: boolean }
) {
  const { failApi, emptyList, withPagination } = opts ?? {}

  await page.route('**/streaming-events*', async (route) => {
    if (route.request().method() !== 'GET') return route.fallback()

    if (failApi) {
      await route.fulfill({ status: 500, body: 'Internal Server Error' })
      return
    }

    const url = new URL(route.request().url())
    const cursor = url.searchParams.get('cursor')

    // Pagination: 첫 페이지 vs 두 번째 페이지
    if (withPagination && cursor === 'page2') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          data: {
            items: mockLiveListPage2,
            hasMore: false,
          },
        }),
      })
      return
    }

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        data: {
          items: emptyList ? [] : mockLiveList,
          nextCursor: withPagination ? 'page2' : undefined,
          hasMore: withPagination ? true : false,
        },
      }),
    })
  })
}

async function mockLiveDetailApi(
  page: Page,
  opts?: { notFound?: boolean; failApi?: boolean }
) {
  const { notFound, failApi } = opts ?? {}

  await page.route('**/streaming-events/*', async (route) => {
    if (route.request().method() !== 'GET') return route.fallback()

    // 목록 API는 패스
    if (route.request().url().includes('?')) {
      return route.fallback()
    }

    if (failApi) {
      await route.fulfill({ status: 500, body: 'Internal Server Error' })
      return
    }

    if (notFound) {
      await route.fulfill({
        status: 404,
        contentType: 'application/json',
        body: JSON.stringify({
          error: { code: 'NOT_FOUND', message: '라이브를 찾을 수 없습니다' },
        }),
      })
      return
    }

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        data: mockLiveDetail,
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

// ── Tests: Live List ────────────────────────────────

test.describe('Live List - 라이브 목록', () => {
  test.beforeEach(async ({ page }) => {
    await mockGoogleIdentityServices(page)
    await mockAuthApi(page)
  })

  test('라이브 목록 페이지 진입 시 헤더와 아이템이 표시된다', async ({
    page,
  }) => {
    await mockLiveListApi(page)
    await page.goto('/live')

    await expect(page.getByRole('heading', { name: 'Live Now' })).toBeVisible()
    await expect(page.getByText('NewJeans 컴백 쇼케이스')).toBeVisible()
    await expect(page.getByText('BTS Fan Meeting Special')).toBeVisible()
  })

  test('LIVE 상태 카드에 LIVE 배지가 표시된다', async ({ page }) => {
    await mockLiveListApi(page)
    await page.goto('/live')

    // LIVE 배지 확인 (여러 개 있을 수 있음)
    const liveBadges = page.getByText('LIVE', { exact: true })
    await expect(liveBadges.first()).toBeVisible()
  })

  test('SCHEDULED 상태 카드에 예정 배지가 표시된다', async ({ page }) => {
    await mockLiveListApi(page)
    await page.goto('/live')

    await expect(page.getByText('BLACKPINK Behind The Scenes')).toBeVisible()
    await expect(page.getByText('예정')).toBeVisible()
  })

  test('ENDED 상태 카드에 종료 배지가 표시된다', async ({ page }) => {
    await mockLiveListApi(page)
    await page.goto('/live')

    await expect(page.getByText('SEVENTEEN Dance Practice')).toBeVisible()
    await expect(page.getByText('종료')).toBeVisible()
  })

  test('라이브 카드 클릭 시 /live/:id 상세 페이지로 이동한다', async ({
    page,
  }) => {
    await mockLiveListApi(page)
    await mockLiveDetailApi(page)
    await page.goto('/live')

    // 카드 텍스트를 찾아 클릭 (Link가 전체 카드를 감싸므로 텍스트로 찾기)
    await page.getByText('NewJeans 컴백 쇼케이스').click()
    await expect(page).toHaveURL('/live/1')
  })

  test('API 실패 시 에러 메시지가 표시된다', async ({ page }) => {
    await mockLiveListApi(page, { failApi: true })
    await page.goto('/live')

    // 에러 메시지 확인 (서버 오류 또는 일반 에러 메시지)
    // 500 에러 감지 시 "서버에 문제가 발생했습니다" 또는 기본 메시지
    await expect(
      page.getByText(/(서버에 문제가 발생했습니다|데이터를 불러올 수 없습니다|네트워크 연결을 확인해주세요)/)
    ).toBeVisible({ timeout: 10000 })
  })

  test('라이브가 없을 때 빈 상태 메시지가 표시된다', async ({ page }) => {
    await mockLiveListApi(page, { emptyList: true })
    await page.goto('/live')

    await expect(page.getByText('라이브가 없습니다')).toBeVisible()
  })

  test('무한 스크롤: 스크롤 끝 도달 시 추가 데이터가 로드된다', async ({
    page,
  }) => {
    await mockLiveListApi(page, { withPagination: true })
    await page.goto('/live')

    // 첫 페이지 데이터 확인
    await expect(page.getByText('NewJeans 컴백 쇼케이스')).toBeVisible()

    // 스크롤을 맨 아래로 이동하여 무한 스크롤 트리거
    await page.evaluate(() => window.scrollTo(0, document.body.scrollHeight))

    // 두 번째 페이지 데이터 로드 확인
    await expect(
      page.getByText('Stray Kids World Tour Highlights')
    ).toBeVisible({ timeout: 5000 })
    await expect(page.getByText('TWICE Concert Replay')).toBeVisible()
  })

  test('모든 데이터 로드 후 "모든 라이브를 확인했습니다" 메시지 표시', async ({
    page,
  }) => {
    await mockLiveListApi(page) // hasMore: false
    await page.goto('/live')

    // 스크롤 끝으로 이동
    await page.evaluate(() => window.scrollTo(0, document.body.scrollHeight))

    await expect(page.getByText('모든 라이브를 확인했습니다')).toBeVisible({
      timeout: 3000,
    })
  })
})

// ── Tests: Live Detail ──────────────────────────────

test.describe('Live Detail - 라이브 상세', () => {
  test.beforeEach(async ({ page }) => {
    await mockGoogleIdentityServices(page)
    await mockAuthApi(page)
  })

  test('라이브 상세 페이지에서 YouTube 플레이어가 렌더링된다', async ({
    page,
  }) => {
    await mockLiveDetailApi(page)
    await page.goto('/live/1')

    // YouTube iframe 확인
    const iframe = page.locator('iframe[src*="youtube.com/embed"]')
    await expect(iframe).toBeVisible()
    await expect(iframe).toHaveAttribute('allowfullscreen', '')
  })

  test('라이브 메타데이터(제목, 아티스트, 설명)가 표시된다', async ({
    page,
  }) => {
    await mockLiveDetailApi(page)
    await page.goto('/live/1')

    await expect(page.getByText('NewJeans 컴백 쇼케이스')).toBeVisible()
    await expect(page.getByText('NewJeans Official')).toBeVisible()
    await expect(page.getByText(/컴백 쇼케이스 라이브 방송/)).toBeVisible()
  })

  test('LIVE 상태에서 시청자 수가 "N명 시청 중" 형식으로 표시된다', async ({
    page,
  }) => {
    await mockLiveDetailApi(page)
    await page.goto('/live/1')

    // 24,583명 시청 중
    await expect(page.getByText(/24,583.*시청 중/)).toBeVisible()
  })

  test('뒤로가기 버튼이 표시되고 클릭 시 이전 페이지로 이동한다', async ({
    page,
  }) => {
    await mockLiveListApi(page)
    await mockLiveDetailApi(page)

    // 목록 → 상세 이동
    await page.goto('/live')
    await page.getByText('NewJeans 컴백 쇼케이스').click()
    await expect(page).toHaveURL('/live/1')

    // 뒤로가기 버튼 클릭
    await page.getByRole('button', { name: /뒤로/i }).click()
    await expect(page).toHaveURL('/live')
  })

  test('존재하지 않는 라이브 ID로 접근 시 404 에러 메시지가 표시된다', async ({
    page,
  }) => {
    await mockLiveDetailApi(page, { notFound: true })
    await page.goto('/live/999')

    await expect(page.getByText('라이브를 찾을 수 없습니다')).toBeVisible()
    await expect(
      page.getByRole('link', { name: '홈으로 이동' })
    ).toBeVisible()
  })

  test('404 에러 상태에서 "홈으로 이동" 클릭 시 홈으로 이동한다', async ({
    page,
  }) => {
    await mockLiveDetailApi(page, { notFound: true })
    await page.goto('/live/999')

    await page.getByRole('link', { name: '홈으로 이동' }).click()
    await expect(page).toHaveURL('/')
  })
})
