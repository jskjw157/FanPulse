import type { BrowserContext, Page } from '@playwright/test';

export type Account = 'A' | 'B';
export type Surface = 'settings' | 'profile' | 'notifications';
export const paths: Record<Surface, string> = {
  settings: '/settings', profile: '/mypage', notifications: '/notifications',
};
export const ids = {
  A: '11111111-1111-4111-8111-111111111111',
  B: '22222222-2222-4222-8222-222222222222',
};
const timestamp = '2026-01-01T00:00:00Z';

export interface BrowserEvent {
  type: string;
  key?: string | null;
  trusted: boolean;
  visibility: string;
}

type ObservedWindow = typeof window & { accountBoundaryEvents: BrowserEvent[] };

export async function events(page: Page) {
  return page.evaluate(() => (window as ObservedWindow).accountBoundaryEvents);
}

interface HttpCall {
  tab: string;
  method: string;
  path: string;
  principal: Account | null;
  expectedUserId: string | null;
  status: number | null;
  body: unknown;
}

/**
 * Real pages/cookies/storage/focus and real application JS; only HTTP is synthetic.
 * This is NOT proof of the Kotlin guard: ExpectedUserPreconditionTest covers that.
 */
export async function createAccountBoundary(context: BrowserContext, baseURL: string) {
  const base = new URL(baseURL);
  if (!['127.0.0.1', 'localhost', '[::1]'].includes(base.hostname)) {
    throw new Error('Account-boundary fixtures require an isolated loopback app');
  }
  const profiles = {
    A: { id: ids.A, email: 'alice@example.test', username: 'Alice_1', hasPassword: false, createdAt: timestamp },
    B: { id: ids.B, email: 'bob@example.test', username: 'Bob_2', hasPassword: false, createdAt: timestamp },
  };
  // Both start ko so an A draft of en cannot masquerade as B's saved value.
  const settings = {
    A: { theme: 'light', language: 'ko', pushEnabled: true, updatedAt: timestamp },
    B: { theme: 'light', language: 'ko', pushEnabled: true, updatedAt: timestamp },
  };
  const notifications = {
    A: [
      { id: 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa1', message: 'Alice private notification 1', type: null, isRead: false, createdAt: timestamp },
      { id: 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa2', message: 'Alice private notification 2', type: null, isRead: false, createdAt: timestamp },
    ],
    B: [{ id: 'bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbb1', message: 'Bob private notification 1', type: null, isRead: false, createdAt: timestamp }],
  };
  const calls: HttpCall[] = [];
  const mutations: HttpCall[] = [];
  const unexpectedApi: string[] = [];
  const blockedExternal: string[] = [];
  const tabs = new Map<Page, string>();
  const gates = new Map<Page, { entered: number; wait: Promise<void>; release: () => void }>();

  await context.addInitScript(() => {
    const log: BrowserEvent[] = [];
    (window as ObservedWindow).accountBoundaryEvents = log;
    window.addEventListener('storage', (event) => log.push({
      type: event.type, key: event.key, trusted: event.isTrusted, visibility: document.visibilityState,
    }));
    window.addEventListener('focus', (event) => log.push({
      type: event.type, trusted: event.isTrusted, visibility: document.visibilityState,
    }));
    document.addEventListener('visibilitychange', (event) => log.push({
      type: event.type, trusted: event.isTrusted, visibility: document.visibilityState,
    }));
  });

  // No third-party HTTP, Google SDK, production API, or unhandled API proxy escapes.
  await context.route('**/*', async (route) => {
    const request = route.request();
    const url = new URL(request.url());
    if (url.origin !== base.origin) {
      blockedExternal.push(request.url());
      return route.abort('blockedbyclient');
    }
    const session = /^\/__e2e__\/session\/([AB])$/.exec(url.pathname);
    if (session) {
      return route.fulfill({
        contentType: 'text/html', body: '<!doctype html><title>Synthetic session fixture</title>',
        headers: {
          'Set-Cookie': `fanpulse_access_token=fixture-${session[1]}; Path=/; HttpOnly; SameSite=Lax`,
          'Cache-Control': 'no-store',
        },
      });
    }
    if (!url.pathname.startsWith('/api/')) {
      // Never let the local Next image optimizer fetch a third-party image either.
      if (url.pathname === '/_next/image') return route.abort('blockedbyclient');
      return route.continue();
    }
    const headers = await request.allHeaders();
    const cookie = /(?:^|;\s*)fanpulse_access_token=fixture-([AB])(?:;|$)/.exec(headers.cookie ?? '');
    const principal = cookie ? cookie[1] as Account : null;
    const page = request.frame().page();
    const path = url.pathname.replace(/^\/api\/v1/, '');
    const call: HttpCall = {
      tab: tabs.get(page) ?? 'unknown', method: request.method(), path, principal,
      expectedUserId: headers['x-expected-user-id'] ?? null, status: null,
      body: request.postData() ? request.postDataJSON() : null,
    };
    calls.push(call);
    const respond = async (json: unknown, status = 200) => {
      call.status = status;
      await route.fulfill({ status, json, headers: { 'Cache-Control': 'no-store' } });
    };
    if (path === '/auth/me' && call.method === 'GET') {
      const gate = gates.get(page);
      if (gate) { gate.entered++; await gate.wait; }
      return respond({ authenticated: !!principal, user: principal ? profiles[principal] : null });
    }
    const knownRead = call.method === 'GET' && ['/me', '/users/me', '/me/settings', '/users/me/notifications/page', '/users/me/notifications/unread-count'].includes(path);
    const knownWrite = call.method === 'PATCH' && ['/me', '/me/settings'].includes(path);
    if (!knownRead && !knownWrite) {
      unexpectedApi.push(`${call.method} ${url.pathname}`);
      return respond({ error: 'Unconfigured synthetic API' }, 501);
    }
    if (!principal) return respond({ title: 'Unauthorized', status: 401 }, 401);
    // Browser subset of the contract: required write assertions, optional legacy reads.
    if ((knownWrite && call.expectedUserId === null) ||
        (call.expectedUserId !== null && call.expectedUserId !== ids[principal])) {
      const missing = call.expectedUserId === null;
      return respond({
        title: 'Expected User Precondition', status: missing ? 428 : 412,
        errorCode: missing ? 'EXPECTED_USER_REQUIRED' : 'EXPECTED_USER_MISMATCH',
      }, missing ? 428 : 412);
    }
    if (path === '/me' || path === '/users/me') {
      if (call.method === 'PATCH' && path === '/me') {
        const body = request.postDataJSON();
        profiles[principal].username = body.username;
        mutations.push(call);
      }
      return respond(profiles[principal]);
    }
    if (path === '/me/settings') {
      if (call.method === 'PATCH') {
        settings[principal].language = request.postDataJSON().language;
        mutations.push(call);
      }
      return respond(settings[principal]);
    }
    const rows = notifications[principal];
    if (path.endsWith('/unread-count')) return respond({ success: true, data: { unreadCount: rows.length } });
    const size = Number(url.searchParams.get('size') ?? 20);
    return respond({ success: true, data: {
      content: rows, page: 0, size, totalElements: rows.length,
      totalPages: 1, last: true, unreadCount: rows.length,
    } });
  });
  await context.routeWebSocket('**/*', (socket) => {
    const url = new URL(socket.url());
    if (url.host === base.host) socket.connectToServer();
    else { blockedExternal.push(socket.url()); socket.close(); }
  });

  const otherTab = await context.newPage();
  // Playwright forces every document focused by default, even after bringToFront.
  // Disable that emulation so these tests observe native Chromium tab transitions.
  await (await context.newCDPSession(otherTab)).send('Emulation.setFocusEmulationEnabled', { enabled: false });
  tabs.set(otherTab, 'other');
  await otherTab.goto('/__e2e__/session/A');
  const oldTab = await context.newPage();
  await (await context.newCDPSession(oldTab)).send('Emulation.setFocusEmulationEnabled', { enabled: false });
  tabs.set(oldTab, 'old');

  function holdAuth() {
    let resolve!: () => void;
    const wait = new Promise<void>((done) => { resolve = done; });
    const gate = {
      entered: 0, wait,
      release: () => { if (gates.get(oldTab) === gate) gates.delete(oldTab); resolve(); },
    };
    if (gates.has(oldTab)) throw new Error('Previous auth gate was not released');
    gates.set(oldTab, gate);
    return gate;
  }

  return {
    oldTab, otherTab, profiles, settings, notifications, calls, mutations, unexpectedApi, blockedExternal, holdAuth,
    // HTTP Set-Cookie in a second real tab, without synthesizing any focus/storage event.
    async replaceCookie(account: Account) {
      await otherTab.evaluate(async (account) => {
        const response = await fetch(`/__e2e__/session/${account}`, { method: 'POST' });
        if (!response.ok) throw new Error('Synthetic session replacement failed');
      }, account);
    },
    releaseAll() { for (const gate of gates.values()) gate.release(); },
  };
}

export type AccountBoundary = Awaited<ReturnType<typeof createAccountBoundary>>;
