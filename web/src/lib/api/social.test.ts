import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@/lib/api-client', async (importOriginal) => ({
  ...await importOriginal<typeof import('@/lib/api-client')>(),
  apiClient: { get: vi.fn(), post: vi.fn(), delete: vi.fn(), patch: vi.fn() },
}));

import { apiClient } from '@/lib/api-client';
import { addFavorite, fetchFavorites, removeFavorite, fetchNotifications, markAllNotificationsRead,
  fetchNotificationPage, fetchNotificationUnreadCount, markNotificationRead } from './social';

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

  it.each([201, 200])('accepts a favorite from new or duplicate POST (%s)', async (status) => {
    vi.mocked(apiClient.post).mockResolvedValue({ status, data: { success: true, data: favorite } });
    await expect(addFavorite(favorite.id)).resolves.toEqual(favorite);
    expect(apiClient.post).toHaveBeenCalledWith(`/users/me/favorites/${favorite.id}`);
  });

  it('rejects malformed POST results rather than claiming follow succeeded', async () => {
    vi.mocked(apiClient.post).mockResolvedValue({ data: { success: true, data: { ...favorite, followedAt: null } } });
    await expect(addFavorite(favorite.id)).rejects.toThrow('즐겨찾기 API 응답이 올바르지 않습니다.');
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
    expect(apiClient.patch).toHaveBeenCalledWith('/users/me/notifications/read-all', undefined, {});
  });

  it('rejects malformed mark-all payloads', async () => {
    vi.mocked(apiClient.patch).mockResolvedValue({
      data: { success: true, data: { updated: '2' } },
    });
    await expect(markAllNotificationsRead()).rejects.toThrow(
      '알림 API 응답이 올바르지 않습니다.'
    );
  });

  it('loads the new paginated endpoint without changing the legacy endpoint contract', async () => {
    const page = { content: [notification], page: 1, size: 20, totalElements: 21, totalPages: 2, last: true, unreadCount: 19 };
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: page } });
    await expect(fetchNotificationPage(true, 1)).resolves.toEqual(page);
    expect(apiClient.get).toHaveBeenCalledWith('/users/me/notifications/page', {
      params: { unreadOnly: true, page: 1, size: 20 }, signal: undefined,
    });
  });

  it.each([{ targetId: '/malicious' }, { targetType: 1 }, { targetAvailable: 'false' }])(
    'rejects malformed target metadata', async (fields) => {
      vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: [{ ...notification, ...fields }] } });
      await expect(fetchNotifications(false)).rejects.toThrow('알림 API 응답이 올바르지 않습니다.');
    },
  );

  it('accepts legacy rows and nullable target metadata', async () => {
    const rows = [notification, { ...notification, targetType: null, targetId: null, targetAvailable: false }];
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: rows } });
    await expect(fetchNotifications(false)).resolves.toEqual(rows);
  });

  it.each([-1, '3', 1.5])('rejects an invalid unread count %s', async (unreadCount) => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: { unreadCount } } });
    await expect(fetchNotificationUnreadCount()).rejects.toThrow();
  });

  it('loads an account-wide unread count', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: { unreadCount: 0 } } });
    await expect(fetchNotificationUnreadCount()).resolves.toBe(0);
    expect(apiClient.get).toHaveBeenCalledWith('/users/me/notifications/unread-count', { signal: undefined });
  });

  it('returns fresh availability from the individual read response', async () => {
    const read = { ...notification, isRead: true, targetType: 'POST', targetId: favorite.id, targetAvailable: false };
    vi.mocked(apiClient.patch).mockResolvedValue({ data: { success: true, data: read } });
    await expect(markNotificationRead(notification.id)).resolves.toEqual(read);
    expect(apiClient.patch).toHaveBeenCalledWith(`/users/me/notifications/${notification.id}/read`, undefined, {});
  });

  it('binds notification pages counts and writes to the displayed account', async () => {
    const headers = { 'X-Expected-User-Id': favorite.id };
    const page = { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0, last: true, unreadCount: 0 };
    vi.mocked(apiClient.get).mockResolvedValueOnce({ data: { success: true, data: page } })
      .mockResolvedValueOnce({ data: { success: true, data: { unreadCount: 0 } } });
    await fetchNotificationPage(false, 0, 20, undefined, favorite.id);
    expect(apiClient.get).toHaveBeenLastCalledWith('/users/me/notifications/page', {
      params: { unreadOnly: false, page: 0, size: 20 }, signal: undefined, headers,
    });
    await fetchNotificationUnreadCount(undefined, favorite.id);
    expect(apiClient.get).toHaveBeenLastCalledWith('/users/me/notifications/unread-count', { signal: undefined, headers });
    vi.mocked(apiClient.patch).mockResolvedValueOnce({ data: { success: true, data: { updated: 0 } } })
      .mockResolvedValueOnce({ data: { success: true, data: { ...notification, isRead: true } } });
    await markAllNotificationsRead(favorite.id);
    expect(apiClient.patch).toHaveBeenLastCalledWith('/users/me/notifications/read-all', undefined, { headers });
    await markNotificationRead(notification.id, favorite.id);
    expect(apiClient.patch).toHaveBeenLastCalledWith(`/users/me/notifications/${notification.id}/read`, undefined, { headers });
  });

  it.each([{ ...notification }, { ...notification, id: favorite.id, isRead: true }])(
    'rejects mismatched or unread mutation responses', async (read) => {
      vi.mocked(apiClient.patch).mockResolvedValue({ data: { success: true, data: read } });
      await expect(markNotificationRead(notification.id)).rejects.toThrow();
    },
  );

  it.each([{ page: 1 }, { size: 0 }, { totalPages: 9 }, { content: null }, { unreadCount: -1 }])(
    'rejects malformed or mismatched page metadata', async (fields) => {
      const page = { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0, last: true, unreadCount: 0, ...fields };
      vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: page } });
      await expect(fetchNotificationPage(false)).rejects.toThrow();
    },
  );
});
