import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@/lib/api-client', async (importOriginal) => ({
  ...await importOriginal<typeof import('@/lib/api-client')>(),
  apiClient: { get: vi.fn(), patch: vi.fn() },
}));

import { apiClient } from '@/lib/api-client';
import { fetchMyProfile, updateMyLanguage, updateMyProfile } from './user';

const profile = {
  id: '11111111-1111-1111-1111-111111111111',
  email: 'profile@example.com',
  username: 'API Profile',
  hasPassword: false,
  createdAt: '2026-01-02T03:04:05.123456Z',
};

describe('fetchMyProfile', () => {
  beforeEach(() => vi.resetAllMocks());

  it.each([false, true])('loads the direct MeController response (hasPassword=%s)', async (hasPassword) => {
    const response = { ...profile, hasPassword };
    vi.mocked(apiClient.get).mockResolvedValue({ data: response });
    const controller = new AbortController();

    await expect(fetchMyProfile(controller.signal)).resolves.toEqual(response);
    expect(apiClient.get).toHaveBeenCalledWith('/me', { signal: controller.signal });
  });

  it.each([
    null,
    [],
    { success: true, data: profile },
    { success: false, data: profile },
    { authenticated: true, user: profile },
    { ...profile, id: 'not-a-uuid' },
    { ...profile, email: null },
    { ...profile, username: undefined },
    { ...profile, username: ' ' },
    { ...profile, username: 42 },
    { ...profile, hasPassword: 'false' },
    { ...profile, createdAt: '2026-02-30T03:04:05Z' },
    { ...profile, createdAt: '2026-01-02T03:04:05' },
  ])('rejects a mismatched or malformed profile response: %o', async (data) => {
    vi.mocked(apiClient.get).mockResolvedValue({ data });

    await expect(fetchMyProfile()).rejects.toThrow('프로필 API 응답이 올바르지 않습니다.');
  });

  it('propagates transport failures instead of returning fallback profile data', async () => {
    vi.mocked(apiClient.get).mockRejectedValue(new Error('network failure'));

    await expect(fetchMyProfile()).rejects.toThrow('network failure');
  });

  it('asserts the displayed identity on reads and both browser writes', async () => {
    const signal = new AbortController().signal;
    const headers = { 'X-Expected-User-Id': profile.id };
    vi.mocked(apiClient.get).mockResolvedValue({ data: profile });
    vi.mocked(apiClient.patch).mockResolvedValueOnce({ data: profile }).mockResolvedValueOnce({ data: { language: 'ko' } });
    await fetchMyProfile(signal, profile.id);
    expect(apiClient.get).toHaveBeenLastCalledWith('/me', { signal, headers });
    await updateMyProfile('New_name', profile.id, signal);
    expect(apiClient.patch).toHaveBeenLastCalledWith('/me', { username: 'New_name' }, { signal, headers });
    await updateMyLanguage('ko', profile.id, signal);
    expect(apiClient.patch).toHaveBeenLastCalledWith('/me/settings', { language: 'ko' }, { signal, headers });
  });

  it('does not send a browser write without an expected identity', async () => {
    await expect(updateMyProfile('New_name', '')).rejects.toThrow('Expected user is required');
    await expect(updateMyLanguage('en', '')).rejects.toThrow('Expected user is required');
    expect(apiClient.patch).not.toHaveBeenCalled();
  });

  it('rejects a profile belonging to another cookie principal', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { ...profile, id: '22222222-2222-4222-8222-222222222222' } });
    await expect(fetchMyProfile(undefined, profile.id)).rejects.toThrow('Profile user mismatch');
  });
});
