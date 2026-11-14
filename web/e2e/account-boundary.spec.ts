import { expect, test as base, type Page } from '@playwright/test';
import {
  createAccountBoundary, events, ids, paths,
  type Account, type AccountBoundary, type Surface,
} from './fixtures/account-boundary';

const test = base.extend<{ boundary: AccountBoundary }>({
  boundary: async ({ context, baseURL }, runFixture, testInfo) => {
    if (!baseURL) throw new Error('A loopback baseURL is required');
    const boundary = await createAccountBoundary(context, baseURL);
    try {
      await runFixture(boundary);
    } finally {
      boundary.releaseAll();
      await testInfo.attach('synthetic-http-and-mutation-log', {
        contentType: 'application/json', body: JSON.stringify({
          calls: boundary.calls, mutations: boundary.mutations,
          blockedExternal: boundary.blockedExternal, unexpectedApi: boundary.unexpectedApi,
          browserEvents: await events(boundary.oldTab).catch(() => []),
        }, null, 2),
      });
    }
  },
});

test.use({ serviceWorkers: 'block', trace: 'on', video: 'on' });

const surfaces: Surface[] = ['settings', 'profile', 'notifications'];
const nickname = (page: Page) => page.locator('input[autocomplete="nickname"]');
const save = (page: Page) => page.getByRole('button', { name: /^(저장|Save)$/, exact: true });
const edit = (page: Page) => page.getByRole('button', { name: /^(닉네임 편집|Edit nickname)$/ });
const signal = (page: Page) => page.evaluate(() => localStorage.getItem('fanpulse:session-changed'));

async function ready(boundary: AccountBoundary, surface: Surface, account: Account) {
  const page = boundary.oldTab;
  if (surface === 'settings') {
    await expect(page.locator('#language')).toHaveValue(boundary.settings[account].language);
    await expect(save(page)).toBeDisabled();
  } else if (surface === 'profile') {
    await expect(page.getByRole('heading', { name: boundary.profiles[account].username, exact: true })).toBeVisible();
    await expect(edit(page)).toBeVisible();
    await expect(nickname(page)).toHaveCount(0);
  } else {
    for (const row of boundary.notifications[account]) await expect(page.getByText(row.message, { exact: true })).toBeVisible();
  }
  await expect(page.getByRole('link', { name: `알림, 읽지 않은 알림 ${boundary.notifications[account].length}개`, exact: true })).toBeVisible();
}

async function openWithDraft(boundary: AccountBoundary, surface: Surface) {
  const page = boundary.oldTab;
  await page.bringToFront();
  await page.goto(paths[surface]);
  await ready(boundary, surface, 'A');
  if (surface === 'settings') {
    await page.locator('#language').selectOption('en');
    await expect(save(page)).toBeEnabled();
  } else if (surface === 'profile') {
    await edit(page).click();
    await nickname(page).fill('Alice_Unsaved');
  }
}

async function privateStateHidden(boundary: AccountBoundary) {
  const page = boundary.oldTab;
  await expect(page.getByText('로딩 중...', { exact: true })).toBeVisible();
  await expect(page.locator('#language')).toHaveCount(0);
  await expect(nickname(page)).toHaveCount(0);
  await expect(page.getByText('alice@example.test', { exact: true })).toHaveCount(0);
  await expect(page.getByRole('heading', { name: /^(Alice_1|Bob_2)$/ })).toHaveCount(0);
  await expect(page.getByText(/Alice private notification/)).toHaveCount(0);
  await expect(page.getByText(/Bob private notification/)).toHaveCount(0);
  await expect(page.getByRole('link', { name: /알림, 읽지 않은 알림/ })).toHaveCount(0);
  await expect(page.getByText(/닉네임을 저장했습니다|언어를 저장했습니다|Language saved|Nickname saved/)).toHaveCount(0);
  await expect(page.locator('html')).toHaveAttribute('lang', 'ko');
}

function assertGuardedReads(boundary: AccountBoundary, account: Account, from = 0) {
  const reads = boundary.calls.slice(from).filter((call) => call.tab === 'old' && call.method === 'GET' && call.path !== '/auth/me');
  expect(reads.length).toBeGreaterThan(0);
  for (const call of reads) {
    expect(call.expectedUserId).toBe(ids[account]);
    expect(call.principal).toBe(account);
  }
  expect(boundary.unexpectedApi).toEqual([]);
}

for (const surface of surfaces) {
  test(`native two-tab session signal hides ${surface} before A -> B identity resolves`, async ({ boundary }, testInfo) => {
    await openWithDraft(boundary, surface);
    assertGuardedReads(boundary, 'A');
    const gate = boundary.holdAuth();
    const eventCursor = (await events(boundary.oldTab)).length;
    const requestCursor = boundary.calls.length;
    await boundary.otherTab.bringToFront();
    await boundary.replaceCookie('B');
    // Mount the real AuthProvider for B. Its announcement must reach A via the browser.
    await boundary.otherTab.goto('/settings');
    await expect(boundary.otherTab.locator('#language')).toHaveValue('ko');
    await expect.poll(() => gate.entered).toBeGreaterThan(0);
    expect((await events(boundary.oldTab)).slice(eventCursor)).toContainEqual(expect.objectContaining({
      type: 'storage', key: 'fanpulse:session-changed', trusted: true,
    }));
    await privateStateHidden(boundary);
    expect(boundary.calls.slice(requestCursor).filter((call) => call.tab === 'old' && call.path !== '/auth/me')).toEqual([]);
    await testInfo.attach(`${surface}-pending-identity`, {
      body: await boundary.oldTab.screenshot(), contentType: 'image/png',
    });
    const announced = await signal(boundary.otherTab);
    gate.release();
    await ready(boundary, surface, 'B');
    await expect(boundary.oldTab.getByText(/Alice private notification|alice@example.test/)).toHaveCount(0);
    assertGuardedReads(boundary, 'B', requestCursor);
    // Receiving a signal must not echo another nonce or replay an abandoned draft.
    expect(await signal(boundary.otherTab)).toBe(announced);
    expect(boundary.calls.filter((call) => call.method === 'PATCH')).toEqual([]);
    expect(boundary.mutations).toEqual([]);
  });

  test(`native focus recovers a missed A -> B signal for ${surface}`, async ({ boundary }, testInfo) => {
    await openWithDraft(boundary, surface);
    await boundary.otherTab.bringToFront();
    await expect.poll(() => boundary.oldTab.evaluate(() => document.hasFocus())).toBe(false);
    const gate = boundary.holdAuth();
    const nonce = await signal(boundary.oldTab);
    const eventCursor = (await events(boundary.oldTab)).length;
    const requestCursor = boundary.calls.length;
    await boundary.replaceCookie('B');
    expect(await signal(boundary.oldTab)).toBe(nonce);
    expect(gate.entered).toBe(0);
    expect((await events(boundary.oldTab)).slice(eventCursor).filter((event) => event.type === 'storage')).toEqual([]);
    await boundary.oldTab.bringToFront();
    await expect.poll(() => gate.entered).toBeGreaterThan(0);
    expect((await events(boundary.oldTab)).slice(eventCursor).some((event) =>
      event.trusted && (event.type === 'focus' || (event.type === 'visibilitychange' && event.visibility === 'visible')),
    )).toBe(true);
    await privateStateHidden(boundary);
    expect(boundary.calls.slice(requestCursor).filter((call) => call.tab === 'old' && call.path !== '/auth/me')).toEqual([]);
    await testInfo.attach(`${surface}-focus-pending-identity`, { body: await boundary.oldTab.screenshot(), contentType: 'image/png' });
    gate.release();
    await ready(boundary, surface, 'B');
    assertGuardedReads(boundary, 'B', requestCursor);
    expect(boundary.mutations).toEqual([]);
    expect(boundary.calls.filter((call) => call.method === 'PATCH')).toEqual([]);
  });

  test(`same-account focus revalidates ${surface}, hides private state and discards drafts`, async ({ boundary }) => {
    await openWithDraft(boundary, surface);
    await boundary.otherTab.bringToFront();
    await expect.poll(() => boundary.oldTab.evaluate(() => document.hasFocus())).toBe(false);
    boundary.profiles.A.username = 'Alice_Revalidated';
    boundary.notifications.A[0].message = 'Alice refreshed notification';
    const nonce = await signal(boundary.oldTab);
    const gate = boundary.holdAuth();
    const requestCursor = boundary.calls.length;
    await boundary.oldTab.bringToFront();
    await expect.poll(() => gate.entered).toBeGreaterThan(0);
    await privateStateHidden(boundary);
    gate.release();
    await ready(boundary, surface, 'A');
    assertGuardedReads(boundary, 'A', requestCursor);
    expect(await signal(boundary.oldTab)).toBe(nonce);
    expect(boundary.calls.filter((call) => call.method === 'PATCH')).toEqual([]);
    expect(boundary.mutations).toEqual([]);
  });
}

for (const surface of ['settings', 'profile'] as const) {
  test(`no-event stale ${surface} write sends A assertion, fails once, never replays against B`, async ({ boundary, context }, testInfo) => {
    await openWithDraft(boundary, surface);
    await expect.poll(() => boundary.oldTab.evaluate(() => document.hasFocus())).toBe(true);
    const before = JSON.stringify({ profiles: boundary.profiles, settings: boundary.settings });
    const authCount = boundary.calls.filter((call) => call.tab === 'old' && call.path === '/auth/me').length;
    const eventCursor = (await events(boundary.oldTab)).length;
    const nonce = await signal(boundary.oldTab);
    // Fetch executes in the background tab without activating it; Set-Cookie is shared.
    await boundary.replaceCookie('B');
    expect((await context.cookies()).find((cookie) => cookie.name === 'fanpulse_access_token')).toMatchObject({ value: 'fixture-B', httpOnly: true });
    expect(await boundary.oldTab.evaluate(() => document.cookie)).not.toContain('fanpulse_access_token');
    expect(await signal(boundary.oldTab)).toBe(nonce);
    expect((await events(boundary.oldTab)).slice(eventCursor).filter((event) =>
      event.type === 'storage' || event.type === 'focus' || (event.type === 'visibilitychange' && event.visibility === 'visible'),
    )).toEqual([]);
    expect(boundary.calls.filter((call) => call.tab === 'old' && call.path === '/auth/me')).toHaveLength(authCount);
    if (surface === 'settings') await expect(boundary.oldTab.locator('#language')).toHaveValue('en');
    else await expect(nickname(boundary.oldTab)).toHaveValue('Alice_Unsaved');
    const gate = boundary.holdAuth();
    const [response] = await Promise.all([
      boundary.oldTab.waitForResponse((response) => response.request().method() === 'PATCH'),
      save(boundary.oldTab).click(),
    ]);
    expect(response.status()).toBe(412);
    expect(await response.json()).toMatchObject({ errorCode: 'EXPECTED_USER_MISMATCH' });
    const attempts = boundary.calls.filter((call) => call.method === 'PATCH');
    expect(attempts).toHaveLength(1);
    expect(attempts[0]).toMatchObject({
      principal: 'B', expectedUserId: ids.A, status: 412,
      path: surface === 'settings' ? '/me/settings' : '/me',
      body: surface === 'settings' ? { language: 'en' } : { username: 'Alice_Unsaved' },
    });
    expect(boundary.mutations).toEqual([]);
    expect(JSON.stringify({ profiles: boundary.profiles, settings: boundary.settings })).toBe(before);
    await expect.poll(() => gate.entered).toBeGreaterThan(0);
    await privateStateHidden(boundary);
    await testInfo.attach(`${surface}-rejected-without-mutation`, { body: await boundary.oldTab.screenshot(), contentType: 'image/png' });
    const requestCursor = boundary.calls.length;
    gate.release();
    await ready(boundary, surface, 'B');
    assertGuardedReads(boundary, 'B', requestCursor);
    expect(boundary.calls.filter((call) => call.method === 'PATCH')).toHaveLength(1);
    expect(boundary.mutations).toEqual([]);
    // Positive control: only a fresh, deliberate B action is allowed to mutate B.
    if (surface === 'settings') await boundary.oldTab.locator('#language').selectOption('en');
    else {
      await edit(boundary.oldTab).click();
      await expect(nickname(boundary.oldTab)).toHaveValue('Bob_2');
      await nickname(boundary.oldTab).fill('Bob_Intentional');
    }
    await save(boundary.oldTab).click();
    if (surface === 'settings') await expect(boundary.oldTab.getByRole('status')).toHaveText('Language saved.');
    else await expect(boundary.oldTab.getByRole('heading', { name: 'Bob_Intentional', exact: true })).toBeVisible();
    expect(boundary.calls.filter((call) => call.method === 'PATCH')).toHaveLength(2);
    expect(boundary.mutations).toHaveLength(1);
    expect(boundary.mutations[0]).toMatchObject({
      principal: 'B', expectedUserId: ids.B, status: 200,
      body: surface === 'settings' ? { language: 'en' } : { username: 'Bob_Intentional' },
    });
    expect(boundary.profiles.A.username).toBe('Alice_1');
    expect(boundary.settings.A.language).toBe('ko');
    expect(boundary.unexpectedApi).toEqual([]);
  });
}

test('no-event notification reload cannot put B rows into the stale A scope', async ({ boundary }, testInfo) => {
  await openWithDraft(boundary, 'notifications');
  await expect.poll(() => boundary.oldTab.evaluate(() => document.hasFocus())).toBe(true);
  const gate = boundary.holdAuth();
  await boundary.replaceCookie('B');
  expect(gate.entered).toBe(0);
  const [response] = await Promise.all([
    boundary.oldTab.waitForResponse((response) => new URL(response.url()).pathname.endsWith('/notifications/page')),
    boundary.oldTab.getByRole('button', { name: '읽지 않음', exact: true }).click(),
  ]);
  expect(response.status()).toBe(412);
  const rejectedRead = boundary.calls.filter((call) => call.path === '/users/me/notifications/page').at(-1);
  expect(rejectedRead).toMatchObject({ principal: 'B', expectedUserId: ids.A, status: 412 });
  expect(await response.json()).toMatchObject({ errorCode: 'EXPECTED_USER_MISMATCH' });
  await expect.poll(() => gate.entered).toBeGreaterThan(0);
  await privateStateHidden(boundary);
  await testInfo.attach('notifications-guarded-read-rejected', { body: await boundary.oldTab.screenshot(), contentType: 'image/png' });
  const requestCursor = boundary.calls.length;
  gate.release();
  await ready(boundary, 'notifications', 'B');
  assertGuardedReads(boundary, 'B', requestCursor);
  await expect(boundary.oldTab.getByText(/Alice private notification/)).toHaveCount(0);
  expect(boundary.calls.filter((call) => call.method === 'PATCH')).toEqual([]);
  expect(boundary.mutations).toEqual([]);
});
