import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@/lib/api-client', () => ({ apiClient: { get: vi.fn() } }));

import { apiClient } from '@/lib/api-client';
import { fetchMyProfile } from './user';

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
});
