import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@/lib/api-client', () => ({ apiClient: { get: vi.fn(), delete: vi.fn(), patch: vi.fn() } }));

import { apiClient } from '@/lib/api-client';
import { fetchFavorites, removeFavorite, fetchNotifications, markAllNotificationsRead } from './social';

const favorite = {
  id: '11111111-1111-1111-1111-111111111111',
  name: 'API Artist',
  englishName: null,
  agency: 'Agency',
  profileImageUrl: null,
  isGroup: true,
  followedAt: '2026-08-13T12:00:00',
};

describe('social API', () => {
  beforeEach(() => vi.clearAllMocks());

  it('loads real favorites from the authenticated user endpoint', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: [favorite] } });
    await expect(fetchFavorites()).resolves.toEqual([favorite]);
    expect(apiClient.get).toHaveBeenCalledWith('/users/me/favorites', { signal: undefined });
  });

  it('removes a favorite through the API', async () => {
    vi.mocked(apiClient.delete).mockResolvedValue({});
    await removeFavorite(favorite.id);
    expect(apiClient.delete).toHaveBeenCalledWith(`/users/me/favorites/${favorite.id}`);
  });

  it('loads notifications using the unread filter', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: [] } });
    await fetchNotifications(true);
    expect(apiClient.get).toHaveBeenCalledWith('/users/me/notifications', {
      params: { unreadOnly: true },
      signal: undefined,
    });
  });

  it('marks all notifications read through the API', async () => {
    vi.mocked(apiClient.patch).mockResolvedValue({ data: { data: { updated: 2 } } });
    await expect(markAllNotificationsRead()).resolves.toBe(2);
    expect(apiClient.patch).toHaveBeenCalledWith('/users/me/notifications/read-all');
  });
});
