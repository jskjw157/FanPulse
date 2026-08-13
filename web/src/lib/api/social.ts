import { apiClient } from '@/lib/api-client';

export interface FavoriteArtist {
  id: string;
  name: string;
  englishName: string | null;
  agency: string | null;
  profileImageUrl: string | null;
  isGroup: boolean;
  followedAt: string;
}

export interface UserNotification {
  id: string;
  type: string | null;
  message: string;
  isRead: boolean;
  createdAt: string;
}

export async function fetchFavorites(signal?: AbortSignal): Promise<FavoriteArtist[]> {
  const response = await apiClient.get('/users/me/favorites', { signal });
  return response.data.data ?? response.data;
}

export async function removeFavorite(artistId: string): Promise<void> {
  await apiClient.delete(`/users/me/favorites/${artistId}`);
}

export async function fetchNotifications(
  unreadOnly: boolean,
  signal?: AbortSignal,
): Promise<UserNotification[]> {
  const response = await apiClient.get('/users/me/notifications', {
    params: { unreadOnly },
    signal,
  });
  return response.data.data ?? response.data;
}

export async function markAllNotificationsRead(): Promise<number> {
  const response = await apiClient.patch('/users/me/notifications/read-all');
  return response.data.data?.updated ?? response.data.updated ?? 0;
}
