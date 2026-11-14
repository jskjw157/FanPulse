import { test, expect, type BrowserContext } from '@playwright/test';
import { createAccountBoundary, ids, type Account } from './fixtures/account-boundary';

test.use({ serviceWorkers: 'block' });
const postId = '33333333-3333-4333-8333-333333333333';
const artistId = '44444444-4444-4444-8444-444444444444';
const at = '2026-01-01T00:00:00Z';

type Call = { method: string; path: string; principal: Account; expected: string | null; status: number; body: unknown };

// Browser/cookies/app are real; only local HTTP is synthetic. Kotlin has separate JWT/MVC tests.
async function communityBoundary(context: BrowserContext, baseURL: string, failFirstSavedRead = false) {
  const boundary = await createAccountBoundary(context, baseURL);
  const post = { id: postId, authorId: ids.A, authorName: 'Alice_1', artistId: null, artistName: null,
    artistProfileImageUrl: null, content: 'Public community fixture', imageUrl: null, likeCount: 0,
    commentCount: 0, createdAt: at };
  const states = { A: { liked: false, saved: false }, B: { liked: false, saved: false } };
  const calls: Call[] = [];
  const writes: Call[] = [];
  let failedSavedRead = false;
  await context.route('**/api/v1/**', async route => {
    const request = route.request();
    const url = new URL(request.url());
    if (url.origin !== new URL(baseURL).origin) return route.fallback();
    const path = url.pathname.replace('/api/v1', '');
    if (!path.startsWith('/community/') && path !== '/comments/mine' && path !== '/artists') return route.fallback();
    const headers = await request.allHeaders();
    const principal = /(?:^|;\s*)fanpulse_access_token=fixture-([AB])(?:;|$)/.exec(headers.cookie ?? '')?.[1] as Account;
    if (!principal) return route.fulfill({ status: 401, json: {} });
    const call: Call = { method: request.method(), path, principal, expected: headers['x-expected-user-id'] ?? null,
      status: 0, body: request.postData() ? request.postDataJSON() : null };
    calls.push(call);
    const reply = async (data: unknown, status = 200, raw = false) => {
      call.status = status;
      await route.fulfill({ status, json: raw ? data : { success: true, data }, headers: { 'Cache-Control': 'no-store' } });
    };
    const publicRead = call.method === 'GET' && (path === '/artists' || /^\/community\/posts\/[^/]+(?:\/comments)?$/.test(path));
    // Optional-header compatibility permits baseline replay. Missing-write rejection is tested by real Kotlin MVC.
    if (!publicRead && call.expected !== null && call.expected !== ids[principal]) {
      return reply({ title: 'Expected user mismatch', status: 412, errorCode: 'EXPECTED_USER_MISMATCH', detail: 'Account changed' }, 412, true);
    }
    if (path === '/artists') return reply({ content: [{ id: artistId, name: 'Scoped Artist', englishName: null,
      agency: null, profileImageUrl: null, isGroup: true }], page: 0, size: 100, totalElements: 1, totalPages: 1 });
    if (path === '/comments/mine') return reply({ content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 }, 200, true);
    if (path === `/community/posts/${postId}` && call.method === 'GET') return reply(post);
    if (path === `/community/me/posts/${postId}/state`) return reply(states[principal]);
    if (path === '/community/me/saved') {
      if (failFirstSavedRead && principal === 'A' && !failedSavedRead) {
        failedSavedRead = true;
        return reply({ title: 'Temporarily unavailable', status: 503 }, 503, true);
      }
      const rows = principal === 'B' ? [{ ...post, content: 'B saved collection item' }] : [];
      return reply({ content: rows, page: 0, size: 20, totalElements: rows.length, totalPages: rows.length ? 1 : 0, last: true });
    }
    if (path.endsWith('/comments') && call.method === 'GET') return reply({ content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 });
    if (path === `/community/posts/${postId}/saved` || path === `/community/posts/${postId}/likes`) {
      const key = path.endsWith('/saved') ? 'saved' : 'liked';
      states[principal][key] = call.method === 'POST';
      writes.push(call);
      return reply(states[principal]);
    }
    if (path === `/community/posts/${postId}/comments` && call.method === 'POST') {
      writes.push(call);
      return reply({ id: '55555555-5555-4555-8555-555555555555', postId, userId: ids[principal],
        content: (call.body as { content: string }).content, status: 'PENDING', parentCommentId: null,
        createdAt: at, authorName: principal === 'A' ? 'Alice_1' : 'Bob_2' }, 202);
    }
    if (path === '/community/posts' && call.method === 'POST') {
      writes.push(call);
      return reply({ ...post, authorId: ids[principal], authorName: principal === 'A' ? 'Alice_1' : 'Bob_2',
        content: (call.body as { content: string }).content }, 201);
    }
    return reply({ unexpected: path }, 501, true);
  });
  return { ...boundary, calls, writes, states };
}

for (const action of ['save', 'like', 'comment'] as const) {
  test(`stale ${action} cannot mutate a cookie-switched account; deliberate B retry can`, async ({ context, baseURL }, testInfo) => {
    const boundary = await communityBoundary(context, baseURL!);
    const path = `/community/posts/${postId}/${action === 'save' ? 'saved' : action === 'like' ? 'likes' : 'comments'}`;
    try {
      await boundary.oldTab.goto(`/post-detail?id=${postId}`);
      await expect(boundary.oldTab.getByRole('button', { name: '저장', exact: true })).toBeEnabled();
      if (action === 'comment') await boundary.oldTab.getByLabel('댓글 내용').fill('A draft before switch');
      await boundary.replaceCookie('B');
      const click = async () => {
        const name = action === 'save' ? '저장' : action === 'like' ? '좋아요' : '댓글 등록';
        await boundary.oldTab.getByRole('button', { name, exact: true }).click();
      };
      await click();
      await expect.poll(() => boundary.calls.filter(call => call.path === path && call.method === 'POST').length).toBe(1);
      const rejected = boundary.calls.find(call => call.path === path && call.method === 'POST');
      expect(rejected).toMatchObject({ principal: 'B', expected: ids.A, status: 412 });
      expect(boundary.writes).toEqual([]);
      expect(boundary.states.B).toEqual({ liked: false, saved: false });
      await expect.poll(() => boundary.calls.some(call => call.path.endsWith('/state') && call.expected === ids.B)).toBe(true);
      await expect(boundary.oldTab.getByRole('button', { name: '저장', exact: true })).toBeEnabled();
      if (action === 'comment') {
        await expect(boundary.oldTab.getByLabel('댓글 내용')).toHaveValue('');
        await boundary.oldTab.getByLabel('댓글 내용').fill('Fresh deliberate B comment');
      }
      await click();
      await expect.poll(() => boundary.writes.length).toBe(1);
      expect(boundary.writes[0]).toMatchObject({ path, principal: 'B', expected: ids.B });
      expect(boundary.calls.filter(call => call.path === path && call.method === 'POST')).toHaveLength(2);
      expect(boundary.states.A).toEqual({ liked: false, saved: false });
      if (action !== 'comment') expect(boundary.states.B[action === 'save' ? 'saved' : 'liked']).toBe(true);
    } finally {
      await testInfo.attach('community-account-journal', { body: JSON.stringify({ calls: boundary.calls, writes: boundary.writes, states: boundary.states }, null, 2), contentType: 'application/json' });
      boundary.releaseAll();
    }
  });
}

test('stale composer cannot publish as B and revalidation discards its A draft', async ({ context, baseURL }, testInfo) => {
  const boundary = await communityBoundary(context, baseURL!);
  try {
    await boundary.oldTab.goto('/post-create');
    await boundary.oldTab.getByRole('button', { name: 'Scoped Artist' }).click();
    await boundary.oldTab.getByLabel('내용 *').fill('A post before switch');
    await boundary.replaceCookie('B');
    await boundary.oldTab.getByRole('button', { name: '게시', exact: true }).click();
    await expect.poll(() => boundary.calls.filter(call => call.path === '/community/posts' && call.method === 'POST').length).toBe(1);
    expect(boundary.calls.find(call => call.path === '/community/posts' && call.method === 'POST')).toMatchObject({ principal: 'B', expected: ids.A, status: 412 });
    expect(boundary.writes).toEqual([]);
    await expect(boundary.oldTab.getByLabel('내용 *')).toHaveValue('');
    await boundary.oldTab.getByRole('button', { name: 'Scoped Artist' }).click();
    await boundary.oldTab.getByLabel('내용 *').fill('Fresh deliberate B post');
    await boundary.oldTab.getByRole('button', { name: '게시', exact: true }).click();
    await expect(boundary.oldTab).toHaveURL(new RegExp(`/post-detail\\?id=${postId}$`));
    expect(boundary.writes).toHaveLength(1);
    expect(boundary.writes[0]).toMatchObject({ principal: 'B', expected: ids.B, body: { content: 'Fresh deliberate B post' } });
  } finally {
    await testInfo.attach('composer-account-journal', { body: JSON.stringify({ calls: boundary.calls, writes: boundary.writes }, null, 2), contentType: 'application/json' });
    boundary.releaseAll();
  }
});

test('stale saved-list retry cannot display B collection before identity revalidation', async ({ context, baseURL }, testInfo) => {
  const boundary = await communityBoundary(context, baseURL!, true);
  try {
    await boundary.oldTab.goto('/saved');
    await expect(boundary.oldTab.getByText('저장한 게시글을 불러오지 못했습니다.')).toBeVisible();
    const gate = boundary.holdAuth();
    await boundary.replaceCookie('B');
    await boundary.oldTab.getByRole('button', { name: '다시 시도', exact: true }).click();
    await expect.poll(() => boundary.calls.filter(call => call.path === '/community/me/saved').length).toBe(2);
    expect(boundary.calls.filter(call => call.path === '/community/me/saved')[1]).toMatchObject({ principal: 'B', expected: ids.A, status: 412 });
    await expect.poll(() => gate.entered).toBeGreaterThan(0);
    await expect(boundary.oldTab.getByText('B saved collection item')).toHaveCount(0);
    await testInfo.attach('identity-pending-no-private-collection', { body: await boundary.oldTab.screenshot(), contentType: 'image/png' });
    gate.release();
    await expect(boundary.oldTab.getByText('B saved collection item')).toBeVisible();
    await testInfo.attach('revalidated-B-collection', { body: await boundary.oldTab.screenshot(), contentType: 'image/png' });
    expect(boundary.calls.filter(call => call.path === '/community/me/saved').at(-1)).toMatchObject({ principal: 'B', expected: ids.B, status: 200 });
    expect(boundary.writes).toEqual([]);
  } finally {
    await testInfo.attach('saved-read-account-journal', { body: JSON.stringify({ calls: boundary.calls, writes: boundary.writes }, null, 2), contentType: 'application/json' });
    boundary.releaseAll();
  }
});
