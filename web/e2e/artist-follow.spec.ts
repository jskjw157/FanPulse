import { expect, test, type Page } from '@playwright/test'
import path from 'node:path'

const artistId = '11111111-1111-1111-1111-111111111111'
const artistPath = `/artists/${artistId}`
const artist = {
  id: artistId, name: 'aespa', englishName: 'aespa', agency: 'SM Entertainment',
  description: '서버에서 불러온 아티스트 소개입니다.', profileImageUrl: null,
  isGroup: true, members: ['Karina', 'Winter'], active: true, debutDate: '2020-11-17',
  createdAt: '2026-01-01T00:00:00Z',
}
const favorite = { ...artist, followedAt: '2026-11-06T12:00:00' }

// These tests exercise the real Next/React UI with a stateful API fixture, not PostgreSQL.
async function mockApi(page: Page, authenticated = true) {
  const state = {
    authenticated, following: false, posts: 0, deletes: 0,
    loadFails: false, mutationFails: false, artistMissing: false, expired: false,
    releasePost: null as (() => void) | null,
  }
  await page.route('**/gsi/client', (route) => route.fulfill({
    contentType: 'application/javascript',
    body: `let cb; window.google = { accounts: { id: {
      initialize: (config) => { cb = config.callback },
      renderButton: (container) => {
        const button = document.createElement('button'); button.textContent = 'Google Sign-In (E2E)';
        button.onclick = () => cb({ credential: 'fixture-token' }); container.appendChild(button);
      }
    } } };`,
  }))
  await page.route('**/api/v1/**', async (route) => {
    const url = new URL(route.request().url())
    const method = route.request().method()
    const fulfill = (data: unknown, status = 200) => route.fulfill({ status, json: data })
    if (url.pathname.endsWith('/auth/me')) {
      return fulfill({ authenticated: state.authenticated, user: state.authenticated
        ? { id: '22222222-2222-2222-2222-222222222222', email: 'follow@example.com', username: 'Fan' } : null })
    }
    if (url.pathname.endsWith('/auth/google')) {
      state.authenticated = true
      return fulfill({ userId: '22222222-2222-2222-2222-222222222222' })
    }
    if (url.pathname.endsWith('/auth/web/refresh')) return fulfill({}, 401)
    if (url.pathname.endsWith(artistPath)) {
      return state.artistMissing ? fulfill({}, 404) : fulfill({ success: true, data: artist })
    }
    if (url.pathname.endsWith('/users/me/favorites')) {
      return state.loadFails ? fulfill({}, 503) : fulfill({ success: true, data: state.following ? [favorite] : [] })
    }
    if (url.pathname.endsWith(`/users/me/favorites/${artistId}`)) {
      if (state.expired) return fulfill({}, 401)
      if (state.mutationFails) return fulfill({}, 503)
      if (method === 'POST') {
        state.posts++
        await new Promise<void>((resolve) => { state.releasePost = resolve })
        const created = !state.following
        state.following = true
        return fulfill({ success: true, data: favorite }, created ? 201 : 200)
      }
      if (method === 'DELETE') {
        state.deletes++
        state.following = false
        return route.fulfill({ status: 204 })
      }
    }
    throw new Error(`Unexpected API call: ${method} ${url.pathname}`)
  })
  return state
}

async function screenshot(page: Page, name: string) {
  if (process.env.FOLLOW_SCREENSHOTS === 'true') {
    await page.locator('article header').screenshot({ path: path.resolve(__dirname, '../../.amp/in/artifacts', `${name}.png`) })
  }
}

test('login returns to the artist, follow survives reload, and both unfollow entry points agree', async ({ page }) => {
  const state = await mockApi(page, false)
  await page.goto(artistPath)
  await expect(page).toHaveURL(`/login?redirect=${encodeURIComponent(artistPath)}`)
  await page.getByRole('button', { name: 'Google Sign-In (E2E)' }).click()
  await expect(page).toHaveURL(artistPath)
  const follow = page.getByRole('button', { name: '팔로우', exact: true })
  await expect(follow).toBeEnabled()
  await screenshot(page, 'artist-follow-default')
  await follow.click()
  await expect(page.getByRole('button', { name: '처리 중' })).toBeDisabled()
  await screenshot(page, 'artist-follow-pending')
  await page.getByRole('button', { name: '처리 중' }).evaluate((button: HTMLButtonElement) => { button.click(); button.click() })
  await expect.poll(() => state.posts).toBe(1)
  state.releasePost!()
  await expect(page.getByRole('button', { name: '팔로우 해제' })).toHaveAttribute('aria-pressed', 'true')
  await screenshot(page, 'artist-follow-followed')
  await page.reload()
  await expect(page.getByRole('button', { name: '팔로우 해제' })).toBeEnabled()
  await page.getByRole('link', { name: '좋아요한 아티스트', exact: true }).click()
  await expect(page.getByRole('link', { name: 'aespa', exact: true })).toBeVisible()
  await page.getByRole('button', { name: 'aespa 좋아요 취소' }).click()
  await expect(page.getByText('좋아요한 아티스트가 없습니다')).toBeVisible()
  expect(state.deletes).toBe(1)
  await page.goBack()
  await expect(follow).toBeEnabled()
  await follow.click()
  await expect.poll(() => state.posts).toBe(2)
  state.releasePost!()
  await page.getByRole('button', { name: '팔로우 해제' }).click()
  await expect(follow).toBeEnabled()
  await page.getByRole('link', { name: '좋아요한 아티스트', exact: true }).click()
  await expect(page.getByText('좋아요한 아티스트가 없습니다')).toBeVisible()
  expect(state.deletes).toBe(2)
})

test('mobile renders retryable load and mutation failures without changing follow state', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 })
  const state = await mockApi(page)
  state.loadFails = true
  await page.goto(artistPath)
  await expect(page.getByRole('alert').filter({ hasText: '즐겨찾기를 불러올 수 없습니다' })).toBeVisible()
  await expect(page.getByRole('button', { name: '팔로우', exact: true })).toBeDisabled()
  await screenshot(page, 'artist-follow-load-error-mobile')
  state.loadFails = false
  await page.getByRole('button', { name: '팔로우 상태 다시 확인' }).click()
  await expect(page.getByRole('button', { name: '팔로우', exact: true })).toBeEnabled()
  state.mutationFails = true
  await page.getByRole('button', { name: '팔로우', exact: true }).click()
  await expect(page.getByRole('alert').filter({ hasText: '팔로우에 실패했습니다' })).toBeVisible()
  await expect(page.getByRole('button', { name: '팔로우', exact: true })).toHaveAttribute('aria-pressed', 'false')
  await screenshot(page, 'artist-follow-mutation-error-mobile')
  state.mutationFails = false
  await page.getByRole('button', { name: '팔로우', exact: true }).click()
  await expect.poll(() => state.posts).toBe(1)
  state.releasePost!()
  await expect(page.getByRole('button', { name: '팔로우 해제' })).toBeEnabled()
  await screenshot(page, 'artist-follow-followed-mobile')
})

test('a missing artist has no follow action and an expired session preserves the return path', async ({ page }) => {
  const state = await mockApi(page)
  state.artistMissing = true
  await page.goto(artistPath)
  await expect(page.getByText('아티스트 정보를 불러올 수 없습니다')).toBeVisible()
  await expect(page.getByRole('button', { name: '팔로우', exact: true })).toHaveCount(0)
  state.artistMissing = false
  await page.getByRole('button', { name: '다시 시도' }).click()
  await expect(page.getByRole('button', { name: '팔로우', exact: true })).toBeEnabled()
  state.expired = true
  state.authenticated = false
  await page.getByRole('button', { name: '팔로우', exact: true }).click()
  await expect(page).toHaveURL(`/login?redirect=${encodeURIComponent(artistPath)}`)
})
