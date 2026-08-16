import { expect, test } from '@playwright/test'
import { createHmac, randomUUID } from 'node:crypto'

const REAL_E2E = process.env.CONCERT_REAL_E2E === 'true'
const E2E_USER_ID = '22222222-2222-2222-2222-222222222222'
const jwtSecret = process.env.CONCERT_E2E_JWT_SECRET
const apiBaseUrl = process.env.CONCERT_E2E_API_BASE_URL

function base64Url(value: string | Buffer): string {
  return Buffer.from(value).toString('base64url')
}

function createAccessToken(): string {
  if (!jwtSecret) throw new Error('CONCERT_E2E_JWT_SECRET이 필요합니다')
  const secretBytes = Buffer.from(jwtSecret)
  if (secretBytes.length < 32) throw new Error('CONCERT_E2E_JWT_SECRET은 32바이트 이상이어야 합니다')
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
    exp: now + 600,
  }))
  const unsigned = `${header}.${payload}`
  const signature = createHmac(digest, secretBytes).update(unsigned).digest('base64url')
  return `${unsigned}.${signature}`
}

test.describe('Concert real vertical slice', () => {
  test.skip(!REAL_E2E, 'CONCERT_REAL_E2E=true일 때만 실행합니다')

  test('shows persisted KOPIS concerts in list, detail, and ticket handoff', async ({ page, request }) => {
    if (!apiBaseUrl) throw new Error('CONCERT_E2E_API_BASE_URL이 필요합니다')
    const apiResponse = await request.get(`${apiBaseUrl}/concerts?page=0&size=20`)
    expect(apiResponse.ok()).toBeTruthy()
    const envelope = await apiResponse.json()
    expect(envelope.success).toBe(true)
    expect(envelope.data.totalElements).toBeGreaterThan(0)
    const first = envelope.data.content[0]

    await page.context().addCookies([{
      name: 'fanpulse_access_token',
      value: createAccessToken(),
      domain: '127.0.0.1',
      path: '/',
      httpOnly: true,
      sameSite: 'Lax',
    }])

    const consoleErrors: string[] = []
    page.on('console', (message) => {
      if (message.type() === 'error') consoleErrors.push(message.text())
    })
    page.on('pageerror', (error) => consoleErrors.push(error.message))

    await page.goto('/concert')
    await expect(page.getByText(first.name, { exact: true }).first()).toBeVisible()
    await page.getByRole('link', { name: `${first.name} 상세 보기` }).click()
    await expect(page.getByRole('heading', { name: first.name })).toBeVisible()
    const detailLink = page.getByRole('link', { name: 'KOPIS 공식 정보 확인', exact: true })
    await expect(detailLink).toHaveAttribute('href', first.ticketUrl)

    await page.goto('/tickets')
    await expect(page.getByText(first.name, { exact: true }).first()).toBeVisible()
    const ticketLink = page.getByRole('link', { name: 'KOPIS 공식 정보 보기', exact: true }).first()
    await expect(ticketLink).toHaveAttribute('href', first.ticketUrl)
    expect(new URL(first.ticketUrl).hostname).toBe('kopis.or.kr')
    expect(consoleErrors).toEqual([])
  })
})
