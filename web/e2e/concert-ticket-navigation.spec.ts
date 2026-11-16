import { expect, test, type Locator, type Page } from '@playwright/test';

const concert = {
  id: '11111111-1111-4111-8111-111111111111', externalId: 'PF298563',
  name: 'Concert navigation fixture', artist: 'Fixture artist',
  venueName: 'Fixture venue', venueHall: 'Fixture hall',
  startDate: '2026-12-12', endDate: '2026-12-12', status: '공연예정',
  posterUrl: null,
  performanceTime: Array.from({ length: 20 }, (_, i) => `Fixture performance ${i + 1}: 18:00`).join('\n'),
  priceText: '전석 110,000원', performers: 'Fixture performer', runtime: '100분',
  ageRating: '만 7세 이상', venueAddress: 'Fixture venue address',
  ticketUrl: 'https://kopis.or.kr/por/db/pblprfr/pblprfrView.do?menuId=MNU_00020&mt20Id=PF298563',
};
const handoffTitle = 'Isolated official information handoff';

async function mockBackend(page: Page) {
  const state = { handoffs: [] as string[], unexpected: [] as string[] };
  // Context routing also intercepts the initial request of a target=_blank popup.
  // The real official/ticket site is NEVER contacted, including during the click.
  await page.context().route('**/*', (route) => {
    const url = new URL(route.request().url());
    if (url.href === concert.ticketUrl) {
      state.handoffs.push(url.href);
      return route.fulfill({ contentType: 'text/html', body: `<h1>${handoffTitle}</h1>` });
    }
    if (url.hostname === '127.0.0.1' || url.hostname === 'localhost') return route.fallback();
    return route.abort();
  });
  await page.route('**/api/v1/**', async (route) => {
    const endpoint = new URL(route.request().url()).pathname.replace('/api/v1', '');
    const json = (data: unknown) => route.fulfill({ json: { success: true, data } });
    if (endpoint === '/auth/me') return route.fulfill({ json: {
      authenticated: true, user: {
        id: '22222222-2222-4222-8222-222222222222', email: 'concert@example.test', username: 'Concert fan',
      },
    } });
    if (endpoint === '/me/settings') return route.fulfill({ json: {
      theme: 'light', language: 'ko', pushEnabled: true, updatedAt: '2026-11-01T09:00:00Z',
    } });
    if (endpoint === '/users/me/notifications/unread-count') return json({ unreadCount: 0 });
    if (endpoint === '/concerts') return json({
      content: [concert], page: 0, size: 20, totalElements: 1, totalPages: 1, last: true,
    });
    if (endpoint === `/concerts/${concert.id}`) return json(concert);
    if (endpoint === '/streaming-events') return json({ items: [], hasMore: false });
    if (endpoint === '/news/latest') return json([]);
    state.unexpected.push(`${route.request().method()} ${endpoint}`);
    return route.fulfill({ status: 501, json: { unexpectedEndpoint: endpoint } });
  });
  return state;
}

async function expectHitTarget(link: Locator) {
  await expect(link).toBeVisible();
  await expect.poll(() => link.evaluate((element) => {
    const box = element.getBoundingClientRect();
    // Checking more than visibility catches the BottomNav intercepting the CTA.
    return [[0.1, 0.2], [0.5, 0.5], [0.9, 0.8]].every(([x, y]) => {
      const hit = document.elementFromPoint(box.x + box.width * x, box.y + box.height * y);
      return hit !== null && element.contains(hit);
    });
  }), { message: 'The link itself must receive normal pointer input across its visible area' }).toBe(true);
}

async function clickOfficialInfo(page: Page, link: Locator) {
  await expect(link).toHaveAttribute('href', concert.ticketUrl);
  await expect(link).toHaveAttribute('target', '_blank');
  await expect(link).toHaveAttribute('rel', 'noopener noreferrer');
  // No force click, dispatched event, or real remote navigation.
  const [popup] = await Promise.all([page.waitForEvent('popup'), link.click()]);
  await expect(popup.getByRole('heading', { name: handoffTitle, exact: true })).toBeVisible();
  await expect(popup).toHaveURL(concert.ticketUrl);
  await popup.close();
}

test.describe('Mobile concert official-info navigation', () => {
  test.use({ viewport: { width: 390, height: 844 }, serviceWorkers: 'block' });

  test('list to detail keeps the official CTA above navigation before and after scrolling', async ({ page }) => {
    const state = await mockBackend(page);
    await page.goto('/concert');
    await page.getByRole('link', { name: `${concert.name} 상세 보기`, exact: true }).click();
    await expect(page).toHaveURL(`/concert-detail?id=${concert.id}`);
    await expect(page.getByRole('heading', { name: concert.name, exact: true })).toBeVisible();
    const link = page.getByRole('link', { name: 'KOPIS 공식 정보 확인', exact: true });
    const bottomNav = page.getByRole('navigation').filter({ has: page.getByRole('link', { name: 'My', exact: true }) });
    const ctaRow = link.locator('..').locator('..');

    for (const scrolled of [false, true]) {
      if (scrolled) {
        await page.evaluate(() => window.scrollTo(0, document.documentElement.scrollHeight));
        await expect.poll(() => page.evaluate(() => window.scrollY)).toBeGreaterThan(0);
      }
      await expectHitTarget(link);
      await expectHitTarget(bottomNav.getByRole('link', { name: 'Home', exact: true }));
      await expect.poll(async () => {
        const cta = await ctaRow.boundingBox();
        const nav = await bottomNav.boundingBox();
        // The measured nav includes its border AND any actual safe-area padding.
        return cta !== null && nav !== null && cta.y + cta.height <= nav.y + 0.5;
      }).toBe(true);
      if (scrolled) {
        const note = page.getByText('공연 정보와 예매처는 KOPIS 공식 상세 페이지에서 다시 확인해 주세요.', { exact: true });
        await expect.poll(async () => {
          const content = await note.boundingBox();
          const cta = await ctaRow.boundingBox();
          return content !== null && cta !== null && content.y >= 128 && content.y + content.height <= cta.y;
        }, { message: 'The last content remains readable above both fixed bottom rows at maximum scroll' }).toBe(true);
      }
    }

    await clickOfficialInfo(page, link);
    expect(state.handoffs).toEqual([concert.ticketUrl]);
    // The fix must not merely raise the CTA over navigation and steal its taps.
    await bottomNav.getByRole('link', { name: 'Home', exact: true }).click();
    await expect(page).toHaveURL('/');
    expect(state.unexpected).toEqual([]);
  });
});

test('desktop keeps the bottom-zero CTA and official-info handoff without mobile navigation', async ({ page }) => {
  await page.setViewportSize({ width: 1280, height: 900 });
  const state = await mockBackend(page);
  await page.goto(`/concert-detail?id=${concert.id}`);
  await expect(page.getByRole('heading', { name: concert.name, exact: true })).toBeVisible();
  await expect(page.getByRole('link', { name: 'My', exact: true })).toBeHidden();
  const link = page.getByRole('link', { name: 'KOPIS 공식 정보 확인', exact: true });
  const ctaRow = link.locator('..').locator('..');
  await expect.poll(async () => {
    const box = await ctaRow.boundingBox();
    return box !== null && Math.abs(box.y + box.height - 900) <= 0.5;
  }).toBe(true);
  expect(await ctaRow.evaluate((element) => getComputedStyle(element).bottom)).toBe('0px');
  await expectHitTarget(link);
  await clickOfficialInfo(page, link);
  expect(state.handoffs).toEqual([concert.ticketUrl]);
  expect(await page.evaluate(() => document.documentElement.scrollWidth)).toBeLessThanOrEqual(1280);
  expect(state.unexpected).toEqual([]);
});
