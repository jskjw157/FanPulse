import { expect, test } from '@playwright/test'
import { createHmac, randomUUID } from 'node:crypto'

const E2E_USER_ID = '22222222-2222-2222-2222-222222222222'

function base64Url(value: string | Buffer) {
  return Buffer.from(value).toString('base64url')
}

function createAccessToken() {
  const jwtSecret = process.env.COMMUNITY_E2E_JWT_SECRET
  if (!jwtSecret) throw new Error('COMMUNITY_E2E_JWT_SECRET이 필요합니다')
  const secretBytes = Buffer.from(jwtSecret)
  if (secretBytes.length < 32) {
    throw new Error('COMMUNITY_E2E_JWT_SECRET은 32바이트 이상이어야 합니다')
  }
  const { alg, digest } = secretBytes.length >= 64
    ? { alg: 'HS512', digest: 'sha512' }
    : secretBytes.length >= 48
      ? { alg: 'HS384', digest: 'sha384' }
      : { alg: 'HS256', digest: 'sha256' }
  const now = Math.floor(Date.now() / 1000)
  const header = base64Url(JSON.stringify({ alg, typ: 'JWT' }))
  const payload = base64Url(JSON.stringify({
    sub: E2E_USER_ID,
    type: 'access',
    jti: randomUUID(),
    iat: now,
    exp: now + 3600,
  }))
  const signature = createHmac(digest, secretBytes)
    .update(`${header}.${payload}`)
    .digest('base64url')
  return `${header}.${payload}.${signature}`
}

test.describe('Community real vertical slice', () => {
  test.skip(
    process.env.COMMUNITY_REAL_E2E !== 'true',
    '실제 PostgreSQL/Spring E2E는 명시적으로 실행한다',
  )

  test('PostgreSQL 게시글과 댓글이 Spring API를 거쳐 목록과 상세에 표시된다', async ({ page }) => {
    const consoleErrors: string[] = []
    page.on('console', (message) => {
      if (message.type() === 'error') consoleErrors.push(message.text())
    })
    page.on('pageerror', (error) => consoleErrors.push(error.message))

    await page.context().addCookies([{
      name: 'fanpulse_access_token',
      value: createAccessToken(),
      domain: '127.0.0.1',
      path: '/',
      httpOnly: true,
      sameSite: 'Lax',
    }])

    await page.route('**/gsi/client', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/javascript',
        body: 'window.google={accounts:{id:{initialize:()=>{},renderButton:()=>{}}}};',
      })
    })

    await page.goto('/community')
    await expect(page.getByText('PostgreSQL E2E 게시글')).toBeVisible({ timeout: 20_000 })
    await expect(page.getByText('e2e-author')).toBeVisible()
    await expect(page.getByText('E2E Artist')).toBeVisible()

    await page.getByRole('link', { name: /PostgreSQL E2E 게시글 상세 보기/ }).click()
    await expect(page).toHaveURL(/post-detail\?id=11111111-1111-1111-1111-111111111111/)
    await expect(page.getByText('PostgreSQL E2E 댓글')).toBeVisible({ timeout: 20_000 })
    await expect(page.getByText('e2e-author')).toHaveCount(2)
    expect(consoleErrors).toEqual([])
  })
})