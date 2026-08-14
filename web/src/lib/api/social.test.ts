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

const notification = {
  id: '22222222-2222-2222-2222-222222222222',
  type: 'NEWS',
  message: '실제 알림',
  isRead: false,
  createdAt: '2026-08-13T12:00:00',
};

describe('social API', () => {
  beforeEach(() => vi.clearAllMocks());

  it('loads real favorites from the authenticated user endpoint', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: [favorite] } });
    await expect(fetchFavorites()).resolves.toEqual([favorite]);
    expect(apiClient.get).toHaveBeenCalledWith('/users/me/favorites', { signal: undefined });
  });

  it('rejects an unsuccessful favorites response instead of treating the envelope as data', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: false, data: null } });
    await expect(fetchFavorites()).rejects.toThrow('즐겨찾기 API 응답이 올바르지 않습니다.');
  });

  it('rejects malformed favorite array elements', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({
      data: { success: true, data: [{ ...favorite, name: 42 }] },
    });
    await expect(fetchFavorites()).rejects.toThrow('즐겨찾기 API 응답이 올바르지 않습니다.');
  });

  it('removes a favorite through the API', async () => {
    vi.mocked(apiClient.delete).mockResolvedValue({});
    await removeFavorite(favorite.id);
    expect(apiClient.delete).toHaveBeenCalledWith(`/users/me/favorites/${favorite.id}`);
  });

  it('loads notifications using the unread filter', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: [notification] } });
    await expect(fetchNotifications(true)).resolves.toEqual([notification]);
    expect(apiClient.get).toHaveBeenCalledWith('/users/me/notifications', {
      params: { unreadOnly: true },
      signal: undefined,
    });
  });

  it('rejects an empty notifications response', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: null } });
    await expect(fetchNotifications(false)).rejects.toThrow(
      '알림 API 응답이 올바르지 않습니다.'
    );
  });

  it('rejects malformed notification array elements', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({
      data: { success: true, data: [{ ...notification, isRead: 'false' }] },
    });
    await expect(fetchNotifications(false)).rejects.toThrow(
      '알림 API 응답이 올바르지 않습니다.'
    );
  });

  it('marks all notifications read through the API', async () => {
    vi.mocked(apiClient.patch).mockResolvedValue({
      data: { success: true, data: { updated: 2 } },
    });
    await expect(markAllNotificationsRead()).resolves.toBe(2);
    expect(apiClient.patch).toHaveBeenCalledWith('/users/me/notifications/read-all');
  });

  it('rejects malformed mark-all payloads', async () => {
    vi.mocked(apiClient.patch).mockResolvedValue({
      data: { success: true, data: { updated: '2' } },
    });
    await expect(markAllNotificationsRead()).rejects.toThrow(
      '알림 API 응답이 올바르지 않습니다.'
    );
  });
});
