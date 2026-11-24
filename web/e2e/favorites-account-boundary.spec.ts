import { test, expect } from '@playwright/test';
import { createAccountBoundary, ids, type Account } from './fixtures/account-boundary';

test.use({ serviceWorkers: 'block' });

test('a stale favorites screen cannot remove the newly signed-in account favorite', async ({ context, baseURL }, testInfo) => {
  const boundary = await createAccountBoundary(context, baseURL!);
  const artistId = '33333333-3333-4333-8333-333333333333';
  const artist = { id: artistId, name: 'Shared Artist', englishName: null, agency: null, profileImageUrl: null, isGroup: true, followedAt: '2026-01-01T00:00:00Z' };
  const favorites = { A: [artist], B: [artist] };
  const calls: Array<{ method: string; principal: Account; expected: string | null; status: number }> = [];
  const writes: Array<{ method: string; principal: Account }> = [];
  await context.route('**/api/v1/users/me/favorites**', async (route) => {
    const request = route.request();
    const headers = await request.allHeaders();
    const principal = /fanpulse_access_token=fixture-([AB])/.exec(headers.cookie ?? '')?.[1] as Account;
    if (!principal) return route.fulfill({ status: 401, json: {} });
    const expected = headers['x-expected-user-id'] ?? null;
    // Optional assertion contract; missing-header enforcement is covered separately by Kotlin MVC.
    if (expected !== null && expected !== ids[principal]) {
      calls.push({ method: request.method(), principal, expected, status: 412 });
      return route.fulfill({ status: 412, json: { title: 'Expected User Precondition', status: 412, errorCode: 'EXPECTED_USER_MISMATCH' } });
    }
    const isDelete = request.method() === 'DELETE';
    calls.push({ method: request.method(), principal, expected, status: isDelete ? 204 : 200 });
    if (isDelete) {
      favorites[principal] = favorites[principal].filter((row) => row.id !== artistId);
      writes.push({ method: request.method(), principal });
      return route.fulfill({ status: 204 });
    }
    return route.fulfill({ json: { success: true, data: favorites[principal] } });
  });
  try {
    await boundary.oldTab.goto('/favorites');
    await expect(boundary.oldTab.getByRole('button', { name: 'Shared Artist 좋아요 취소' })).toBeVisible();
    await boundary.replaceCookie('B');
    await boundary.oldTab.getByRole('button', { name: 'Shared Artist 좋아요 취소' }).click();
    await expect.poll(() => calls.filter((call) => call.method === 'DELETE').length).toBe(1);
    expect(favorites.B).toEqual([artist]);
    expect(writes).toEqual([]);
    expect(calls.find((call) => call.method === 'DELETE')).toEqual({ method: 'DELETE', principal: 'B', expected: ids.A, status: 412 });
    await expect(boundary.oldTab.getByRole('button', { name: 'Shared Artist 좋아요 취소' })).toBeVisible();
    await expect.poll(() => calls.filter((call) => call.method === 'GET' && call.principal === 'B').length).toBeGreaterThan(0);
    await boundary.oldTab.getByRole('button', { name: 'Shared Artist 좋아요 취소' }).click();
    await expect.poll(() => favorites.B.length).toBe(0);
    expect(favorites.A).toEqual([artist]);
    expect(writes).toEqual([{ method: 'DELETE', principal: 'B' }]);
  } finally {
    await testInfo.attach('favorites-account-journal', { body: JSON.stringify({ calls, writes, favorites }, null, 2), contentType: 'application/json' });
    boundary.releaseAll();
  }
});
