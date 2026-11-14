import { act, cleanup, fireEvent, render, renderHook, screen, waitFor } from '@testing-library/react';
import { AxiosError, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthProvider, useAuth } from '@/contexts/AuthContext';
import { apiClient } from '@/lib/api-client';
import { updateMyLanguage } from '@/lib/api/user';
import { SESSION_CHANGED } from '@/lib/session-sync';
import { useNotifications } from '@/hooks/useNotifications';
import { useNotificationUnreadCount } from '@/hooks/useNotificationUnreadCount';
import i18n from '@/i18n';
import MyPage from './mypage/page';
import SettingsPage from './settings/page';

const { pushMock, replaceMock } = vi.hoisted(() => ({ pushMock: vi.fn(), replaceMock: vi.fn() }));
vi.mock('next/navigation', () => ({ useRouter: () => ({ push: pushMock, replace: replaceMock, back: vi.fn() }) }));

const alice = { id: '11111111-1111-4111-8111-111111111111', email: 'alice@example.com', username: 'Alice_1', hasPassword: false, createdAt: '2026-01-01T00:00:00Z' };
const bob = { ...alice, id: '22222222-2222-4222-8222-222222222222', email: 'bob@example.com', username: 'Bob_2' };
const originalAdapter = apiClient.defaults.adapter;
let current: typeof alice | null;
let storedName: string;
let languages: Record<string, 'ko' | 'en'>;
let failProfile: 'duplicate' | 'invalid' | 'server' | null;
let failSettingsGet: boolean;
let failSettingsSave: boolean;
let getGate: Promise<void> | null;
let saveGate: Promise<void> | null;
let logoutGate: Promise<void> | null;
let authGate: Promise<void> | null;
let profileGate: Promise<void> | null;
let writes: string[];
let failLogout: 'server' | 'network' | null;

function deferred() {
  let resolve!: () => void;
  const promise = new Promise<void>((done) => { resolve = done; });
  return { promise, resolve };
}

function reply(config: InternalAxiosRequestConfig, data: unknown): AxiosResponse {
  return { config, data, status: 200, statusText: 'OK', headers: {} };
}

function reject(config: InternalAxiosRequestConfig, status: number, errorCode: string): never {
  throw new AxiosError('Request failed', undefined, config, undefined, {
    ...reply(config, { title: 'Request failed', status, errorCode, detail: 'Rejected by server' }), status,
  });
}

function SessionControls() {
  const { user, refreshAuth, logout } = useAuth();
  return <>
    <output aria-label="auth username">{user?.username ?? 'signed out'}</output>
    <button onClick={() => void refreshAuth()}>Refresh session</button>
    <button onClick={() => void logout()}>End session</button>
  </>;
}

function NotificationProbe() {
  const { user, isLoading, sessionVersion } = useAuth();
  const id = isLoading ? undefined : user?.id;
  const { notifications, retry } = useNotifications(false, id, sessionVersion);
  const { count } = useNotificationUnreadCount(id, sessionVersion);
  return <>
    <output aria-label="notifications">{notifications.map((row) => row.message).join(',')}</output>
    <output aria-label="unread">{count ?? 'unknown'}</output>
    <button onClick={() => void retry()}>Reload notifications</button>
  </>;
}

function App({ page = 'settings' }: { page?: 'settings' | 'profile' | 'notifications' }) {
  return <AuthProvider><SessionControls />{page === 'profile' ? <MyPage /> : page === 'notifications' ? <NotificationProbe /> : <SettingsPage />}</AuthProvider>;
}

beforeEach(async () => {
  current = { ...alice };
  storedName = alice.username;
  languages = { [alice.id]: 'ko', [bob.id]: 'en' };
  failProfile = null;
  failSettingsGet = false;
  failSettingsSave = false;
  getGate = null;
  saveGate = null;
  logoutGate = null;
  authGate = null;
  profileGate = null;
  writes = [];
  failLogout = null;
  pushMock.mockClear();
  replaceMock.mockClear();
  await i18n.changeLanguage('ko');
  apiClient.defaults.adapter = async (config) => {
    const user = current;
    if (config.url === '/auth/me') {
      await authGate;
      return reply(config, { authenticated: !!user, user: user ? { ...user, username: user.id === alice.id ? storedName : user.username } : undefined });
    }
    if (config.url === '/auth/logout') {
      await logoutGate;
      if (failLogout === 'server') reject(config, 500, 'INTERNAL_ERROR');
      if (failLogout === 'network') throw new AxiosError('Network Error', 'ERR_NETWORK', config);
      current = null;
      return reply(config, {});
    }
    // Mirror the server's pre-service guard, including optional private-read assertions.
    const expected = config.headers.get('X-Expected-User-Id');
    if (user && (config.url === '/me' || config.url === '/me/settings' || config.url?.startsWith('/users/me/notifications'))) {
      if (!expected && config.method === 'patch' && !config.url?.startsWith('/users')) reject(config, 428, 'EXPECTED_USER_REQUIRED');
      if (expected && expected !== user.id) reject(config, 412, 'EXPECTED_USER_MISMATCH');
    }
    // #296 owns the GET path change; keep this suite usable before and after its integration.
    if ((config.url === '/users/me' || config.url === '/me') && config.method === 'get') {
      await profileGate;
      return reply(config, { ...user, username: user?.id === alice.id ? storedName : user?.username });
    }
    if (config.url === '/me' && config.method === 'patch') {
      await saveGate;
      if (failProfile) reject(config, failProfile === 'duplicate' ? 409 : failProfile === 'invalid' ? 400 : 500,
        failProfile === 'duplicate' ? 'USERNAME_ALREADY_EXISTS' : failProfile === 'invalid' ? 'VALIDATION_FAILED' : 'INTERNAL_ERROR');
      const { username } = JSON.parse(config.data);
      writes.push(`profile:${user?.id}`);
      storedName = username;
      return reply(config, { ...user, username });
    }
    if (config.url === '/me/settings' && user) {
      if (config.method === 'get') {
        const language = languages[user.id];
        await getGate;
        if (failSettingsGet) reject(config, 500, 'INTERNAL_ERROR');
        return reply(config, { theme: 'light', language, pushEnabled: true, updatedAt: '2026-01-01T00:00:00Z' });
      }
      await saveGate;
      if (failSettingsSave) reject(config, 500, 'INTERNAL_ERROR');
      const { language, ...other } = JSON.parse(config.data);
      if (!['ko', 'en'].includes(language) || Object.keys(other).length) reject(config, 400, 'INVALID_REQUEST');
      writes.push(`settings:${user.id}`);
      languages[user.id] = language;
      return reply(config, { theme: 'light', language, pushEnabled: true, updatedAt: '2026-01-02T00:00:00Z' });
    }
    if (config.url === '/users/me/notifications/unread-count') {
      return reply(config, { success: true, data: { unreadCount: user?.id === alice.id ? 17 : 3 } });
    }
    if (config.url === '/users/me/notifications/page') {
      return reply(config, { success: true, data: {
        content: [{ id: '33333333-3333-4333-8333-333333333333', type: 'NEWS', message: `${user?.username} private notification`, isRead: false, createdAt: '2026-01-01T00:00:00Z' }],
        page: 0, size: 20, totalElements: 1, totalPages: 1, last: true, unreadCount: user?.id === alice.id ? 17 : 3,
      } });
    }
    throw new Error(`Unexpected request: ${config.method} ${config.url}`);
  };
});

afterEach(() => {
  cleanup();
  apiClient.defaults.adapter = originalAdapter;
  expect(pushMock).not.toHaveBeenCalled();
});

async function editNickname(value: string) {
  fireEvent.click(await screen.findByRole('button', { name: '닉네임 편집' }));
  fireEvent.change(screen.getByLabelText('닉네임'), { target: { value } });
  fireEvent.click(screen.getByRole('button', { name: '저장' }));
}

describe('Nickname editing through the existing profile API', () => {
  it.each(['ab', 'Z'.repeat(50)])('saves valid boundary %s and restores profile/auth on reconnect', async (name) => {
    const app = render(<App page="profile" />);
    await editNickname(`  ${name}  `);
    expect(await screen.findByText('닉네임을 저장했습니다.')).toBeVisible();
    expect(await screen.findByRole('heading', { name })).toBeVisible();
    expect(screen.getByLabelText('auth username')).toHaveTextContent(name);
    app.unmount();
    render(<App page="profile" />);
    expect(await screen.findByRole('heading', { name })).toBeVisible();
    expect(screen.getByLabelText('auth username')).toHaveTextContent(name);
  });

  it.each(['', 'a', 'x'.repeat(51), 'name with space', '한글'])('rejects invalid nickname %j without changing the displayed identity', async (name) => {
    render(<App page="profile" />);
    await editNickname(name);
    expect(await screen.findByRole('alert')).toHaveTextContent('2–50자');
    expect(screen.getByLabelText('auth username')).toHaveTextContent(alice.username);
    expect(screen.getByRole('heading', { name: alice.username })).toBeVisible();
  });

  it.each(['duplicate', 'invalid', 'server'] as const)('shows %s failure and permits a successful retry', async (failure) => {
    failProfile = failure;
    render(<App page="profile" />);
    await editNickname('Changed_2');
    expect(await screen.findByRole('alert')).toHaveTextContent(failure === 'duplicate' ? '이미 사용 중' : failure === 'invalid' ? '2–50자' : '저장하지 못했습니다');
    expect(screen.getByLabelText('auth username')).toHaveTextContent(alice.username);
    failProfile = null;
    fireEvent.click(screen.getByRole('button', { name: '저장' }));
    expect(await screen.findByRole('heading', { name: 'Changed_2' })).toBeVisible();
    expect(screen.getByLabelText('auth username')).toHaveTextContent('Changed_2');
  });

  it('ignores a nickname save that finishes after logout', async () => {
    const gate = deferred();
    saveGate = gate.promise;
    render(<App page="profile" />);
    await editNickname('Late_Name');
    expect(screen.getByRole('button', { name: '저장 중…' })).toBeDisabled();
    fireEvent.click(screen.getByRole('button', { name: 'End session' }));
    await waitFor(() => expect(screen.getByLabelText('auth username')).toHaveTextContent('signed out'));
    await act(async () => gate.resolve());
    expect(screen.getByLabelText('auth username')).toHaveTextContent('signed out');
    expect(screen.queryByText('닉네임을 저장했습니다.')).not.toBeInTheDocument();
  });
});

describe('Logout session outcome', () => {
  it.each(['server', 'network'] as const)('keeps the session visible after %s failure and allows retry', async (failure) => {
    failLogout = failure;
    render(<App />);
    await screen.findByRole('combobox', { name: '언어' });
    fireEvent.click(screen.getByRole('button', { name: '로그아웃' }));
    expect(await screen.findByRole('alert')).toHaveTextContent('세션이 유지될 수 있습니다');
    expect(screen.getByLabelText('auth username')).toHaveTextContent(alice.username);
    expect(screen.getByRole('button', { name: '로그아웃' })).toBeEnabled();
    expect(replaceMock).not.toHaveBeenCalled();
    fireEvent.click(screen.getByRole('button', { name: 'Refresh session' }));
    await waitFor(() => expect(screen.getByLabelText('auth username')).toHaveTextContent(alice.username));
    failLogout = null;
    fireEvent.click(screen.getByRole('button', { name: '로그아웃' }));
    await waitFor(() => expect(screen.getByLabelText('auth username')).toHaveTextContent('signed out'));
    expect(current).toBeNull();
    expect(replaceMock).toHaveBeenCalledWith('/login');
  });

  it('does not claim logout while the server request is pending', async () => {
    const gate = deferred();
    logoutGate = gate.promise;
    render(<App />);
    await screen.findByRole('combobox', { name: '언어' });
    fireEvent.click(screen.getByRole('button', { name: '로그아웃' }));
    expect(screen.getByRole('button', { name: '로그아웃 중…' })).toBeDisabled();
    expect(screen.getByLabelText('auth username')).toHaveTextContent(alice.username);
    expect(replaceMock).not.toHaveBeenCalled();
    await act(async () => gate.resolve());
    await waitFor(() => expect(screen.getByLabelText('auth username')).toHaveTextContent('signed out'));
    expect(current).toBeNull();
    expect(replaceMock).toHaveBeenCalledWith('/login');
  });
});

describe('Account language persistence and isolation', () => {
  it('applies server language, saves Korean, and restores it after reconnect and login', async () => {
    languages[alice.id] = 'en';
    const app = render(<App />);
    expect(await screen.findByRole('heading', { name: 'Settings' })).toBeVisible();
    expect(document.documentElement.lang).toBe('en');
    fireEvent.change(screen.getByRole('combobox', { name: 'Language' }), { target: { value: 'ko' } });
    fireEvent.click(screen.getByRole('button', { name: 'Save' }));
    expect(await screen.findByText('언어를 저장했습니다.')).toBeVisible();
    expect(document.documentElement.lang).toBe('ko');
    app.unmount();
    const reconnected = render(<App />);
    expect(await screen.findByRole('combobox', { name: '언어' })).toHaveValue('ko');
    fireEvent.click(screen.getByRole('button', { name: 'End session' }));
    await waitFor(() => expect(screen.getByLabelText('auth username')).toHaveTextContent('signed out'));
    current = { ...alice };
    fireEvent.click(screen.getByRole('button', { name: 'Refresh session' }));
    expect(await screen.findByRole('combobox', { name: '언어' })).toHaveValue('ko');
    reconnected.unmount();
  });

  it('retains the applied language on save failure and allows retry', async () => {
    failSettingsSave = true;
    render(<App />);
    fireEvent.change(await screen.findByRole('combobox', { name: '언어' }), { target: { value: 'en' } });
    fireEvent.click(screen.getByRole('button', { name: '저장' }));
    expect(await screen.findByRole('alert')).toHaveTextContent('기존 언어를 유지');
    expect(document.documentElement.lang).toBe('ko');
    expect(screen.getByRole('heading', { name: '설정' })).toBeVisible();
    failSettingsSave = false;
    fireEvent.click(screen.getByRole('button', { name: '저장' }));
    expect(await screen.findByText('Language saved.')).toBeVisible();
    expect(document.documentElement.lang).toBe('en');
  });

  it('shows settings restore failure and retries explicitly', async () => {
    failSettingsGet = true;
    render(<App />);
    expect(await screen.findByRole('alert')).toHaveTextContent('불러오지 못했습니다');
    failSettingsGet = false;
    fireEvent.click(screen.getByRole('button', { name: '다시 시도' }));
    expect(await screen.findByRole('combobox', { name: '언어' })).toHaveValue('ko');
  });

  it('does not let a late settings read from Alice overwrite Bob', async () => {
    const gate = deferred();
    getGate = gate.promise;
    render(<App />);
    await waitFor(() => expect(screen.getByLabelText('auth username')).toHaveTextContent(alice.username));
    current = { ...bob };
    getGate = null;
    fireEvent.click(screen.getByRole('button', { name: 'Refresh session' }));
    expect(await screen.findByRole('heading', { name: 'Settings' })).toBeVisible();
    await act(async () => gate.resolve());
    expect(screen.getByRole('combobox', { name: 'Language' })).toHaveValue('en');
    expect(document.documentElement.lang).toBe('en');
    expect(screen.getByLabelText('auth username')).toHaveTextContent(bob.username);
  });

  it('ignores a language save finishing after logout/account switch', async () => {
    languages[bob.id] = 'ko';
    const gate = deferred();
    saveGate = gate.promise;
    render(<App />);
    fireEvent.change(await screen.findByRole('combobox', { name: '언어' }), { target: { value: 'en' } });
    fireEvent.click(screen.getByRole('button', { name: '저장' }));
    expect(screen.getByRole('button', { name: '저장 중…' })).toBeDisabled();
    fireEvent.click(screen.getByRole('button', { name: 'End session' }));
    await waitFor(() => expect(screen.getByLabelText('auth username')).toHaveTextContent('signed out'));
    current = { ...bob };
    fireEvent.click(screen.getByRole('button', { name: 'Refresh session' }));
    await waitFor(() => expect(screen.getByLabelText('auth username')).toHaveTextContent(bob.username));
    await act(async () => gate.resolve());
    expect(await screen.findByRole('combobox', { name: '언어' })).toHaveValue('ko');
    expect(document.documentElement.lang).toBe('ko');
  });

  it('rejects unsupported language values instead of sending or applying them', async () => {
    await expect(updateMyLanguage('ja' as 'ko', alice.id)).rejects.toThrow('Unsupported language');
    apiClient.defaults.adapter = async (config) => config.url === '/auth/me'
      ? reply(config, { authenticated: true, user: alice })
      : reply(config, { language: 'ja' });
    render(<App />);
    expect(await screen.findByRole('alert')).toHaveTextContent('불러오지 못했습니다');
    expect(screen.queryByRole('combobox')).not.toBeInTheDocument();
    expect(i18n.language).toBe('ko');
  });
});

describe('Stale-tab account boundary', () => {
  it('does not let an old rendered language action borrow the newly refreshed account ID', async () => {
    const { result } = renderHook(() => useAuth(), { wrapper: AuthProvider });
    await waitFor(() => expect(result.current.settingsState).toBe('success'));
    const saveAsAlice = result.current.saveLanguage;
    current = { ...bob };
    await act(() => result.current.refreshAuth());
    await waitFor(() => expect(result.current.user?.id).toBe(bob.id));
    await act(async () => { expect(await saveAsAlice('ko')).toBe(false); });
    expect(writes).toEqual([]);
    expect(languages[bob.id]).toBe('en');
  });

  it.each(['settings', 'profile'] as const)('rejects a stale %s write before either account is mutated', async (page) => {
    render(<App page={page} />);
    if (page === 'settings') {
      fireEvent.change(await screen.findByRole('combobox', { name: '언어' }), { target: { value: 'en' } });
    } else {
      fireEvent.click(await screen.findByRole('button', { name: '닉네임 편집' }));
      fireEvent.change(screen.getByLabelText('닉네임'), { target: { value: 'Stale_Draft' } });
    }
    // No refresh event: only the HttpOnly cookie principal changes in the other tab.
    current = { ...bob };
    const gate = deferred();
    authGate = gate.promise;
    fireEvent.click(screen.getByRole('button', { name: '저장' }));
    await waitFor(() => expect(screen.getByLabelText('auth username')).toHaveTextContent('signed out'));
    expect(writes).toEqual([]);
    expect(storedName).toBe(alice.username);
    expect(languages).toEqual({ [alice.id]: 'ko', [bob.id]: 'en' });
    expect(screen.queryByDisplayValue('Stale_Draft')).not.toBeInTheDocument();
    await act(async () => gate.resolve());
    await waitFor(() => expect(screen.getByLabelText('auth username')).toHaveTextContent(bob.username));
    expect(writes).toEqual([]); // Never replay a rejected mutation as Bob.
  });

  it.each(['storage', 'focus', 'visibility'] as const)('hides private profile and resets drafts during %s identity refresh', async (event) => {
    render(<App page="profile" />);
    fireEvent.click(await screen.findByRole('button', { name: '닉네임 편집' }));
    fireEvent.change(screen.getByLabelText('닉네임'), { target: { value: 'Alice_Draft' } });
    current = { ...bob };
    const gate = deferred();
    authGate = gate.promise;
    const visibility = vi.spyOn(document, 'visibilityState', 'get').mockReturnValue('visible');
    act(() => {
      if (event === 'storage') window.dispatchEvent(new StorageEvent('storage', { key: SESSION_CHANGED, newValue: 'changed' }));
      else if (event === 'focus') window.dispatchEvent(new Event('focus'));
      else document.dispatchEvent(new Event('visibilitychange'));
    });
    visibility.mockRestore();
    expect(screen.queryByText(alice.email)).not.toBeInTheDocument();
    expect(screen.queryByDisplayValue('Alice_Draft')).not.toBeInTheDocument();
    expect(screen.getByLabelText('auth username')).toHaveTextContent('signed out');
    await act(async () => gate.resolve());
    await screen.findByRole('heading', { name: bob.username });
    fireEvent.click(await screen.findByRole('button', { name: 'Edit nickname' }));
    expect(screen.getByLabelText('Nickname')).toHaveValue(bob.username);
    expect(writes).toEqual([]);
  });

  it('resets an unsaved language draft even when fast revalidation returns the same account', async () => {
    render(<App />);
    fireEvent.change(await screen.findByRole('combobox', { name: '언어' }), { target: { value: 'en' } });
    fireEvent.click(screen.getByRole('button', { name: 'Refresh session' }));
    await waitFor(() => expect(screen.getByRole('combobox', { name: '언어' })).toHaveValue('ko'));
    expect(screen.getByRole('button', { name: '저장' })).toBeDisabled();
  });

  it('discards a profile response started before revalidation', async () => {
    const gate = deferred();
    profileGate = gate.promise;
    render(<App page="profile" />);
    await waitFor(() => expect(screen.getByLabelText('auth username')).toHaveTextContent(alice.username));
    current = { ...bob };
    profileGate = null;
    fireEvent.click(screen.getByRole('button', { name: 'Refresh session' }));
    await screen.findByRole('heading', { name: bob.username });
    await act(async () => gate.resolve());
    expect(screen.queryByText(alice.email)).not.toBeInTheDocument();
    expect(screen.getByRole('heading', { name: bob.username })).toBeVisible();
  });

  it('hides old notifications and count before a focus identity check resolves', async () => {
    render(<App page="notifications" />);
    await waitFor(() => expect(screen.getByLabelText('notifications')).toHaveTextContent(`${alice.username} private notification`));
    await waitFor(() => expect(screen.getByLabelText('unread')).toHaveTextContent('17'));
    current = { ...bob };
    const gate = deferred();
    authGate = gate.promise;
    act(() => { window.dispatchEvent(new Event('focus')); });
    expect(screen.getByLabelText('notifications')).toBeEmptyDOMElement();
    expect(screen.getByLabelText('unread')).toHaveTextContent('unknown');
    await act(async () => gate.resolve());
    await waitFor(() => expect(screen.getByLabelText('notifications')).toHaveTextContent(`${bob.username} private notification`));
    await waitFor(() => expect(screen.getByLabelText('unread')).toHaveTextContent('3'));
  });

  it('rejects notification reads for an unnoticed cookie switch rather than labeling Bob rows as Alice', async () => {
    render(<App page="notifications" />);
    await waitFor(() => expect(screen.getByLabelText('notifications')).toHaveTextContent(`${alice.username} private notification`));
    current = { ...bob };
    const gate = deferred();
    authGate = gate.promise;
    fireEvent.click(screen.getByRole('button', { name: 'Reload notifications' }));
    await waitFor(() => expect(screen.getByLabelText('auth username')).toHaveTextContent('signed out'));
    expect(screen.getByLabelText('notifications')).toBeEmptyDOMElement();
    expect(screen.getByLabelText('unread')).toHaveTextContent('unknown');
    await act(async () => gate.resolve());
    await waitFor(() => expect(screen.getByLabelText('notifications')).toHaveTextContent(`${bob.username} private notification`));
  });
});
