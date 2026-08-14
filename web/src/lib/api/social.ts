import { apiClient } from '@/lib/api-client';
import {
  isIsoDateTime,
  isNonEmptyString,
  isNullableString,
  isRecord,
  isUuid,
  unwrapApiResponse,
} from '@/lib/api-response';
import type { ApiResponse } from '@/types/api';

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

function isFavoriteArtist(value: unknown): value is FavoriteArtist {
  if (!isRecord(value)) return false;
  return (
    isUuid(value.id) &&
    isNonEmptyString(value.name) &&
    isNullableString(value.englishName) &&
    isNullableString(value.agency) &&
    isNullableString(value.profileImageUrl) &&
    typeof value.isGroup === 'boolean' &&
    isIsoDateTime(value.followedAt)
  );
}

function isUserNotification(value: unknown): value is UserNotification {
  if (!isRecord(value)) return false;
  return (
    isUuid(value.id) &&
    isNullableString(value.type) &&
    isNonEmptyString(value.message) &&
    typeof value.isRead === 'boolean' &&
    isIsoDateTime(value.createdAt)
  );
}

function isFavoriteArray(value: unknown): value is FavoriteArtist[] {
  return Array.isArray(value) && value.every(isFavoriteArtist);
}

function isNotificationArray(value: unknown): value is UserNotification[] {
  return Array.isArray(value) && value.every(isUserNotification);
}

function isUpdatedResult(value: unknown): value is { updated: number } {
  return isRecord(value) && Number.isInteger(value.updated) && (value.updated as number) >= 0;
}

export async function fetchFavorites(signal?: AbortSignal): Promise<FavoriteArtist[]> {
  const response = await apiClient.get<ApiResponse<FavoriteArtist[]>>('/users/me/favorites', { signal });
  return unwrapApiResponse(
    response.data,
    '즐겨찾기 API 응답이 올바르지 않습니다.',
    isFavoriteArray
  );
}

export async function removeFavorite(artistId: string): Promise<void> {
  await apiClient.delete(`/users/me/favorites/${artistId}`);
}

export async function fetchNotifications(
  unreadOnly: boolean,
  signal?: AbortSignal,
): Promise<UserNotification[]> {
  const response = await apiClient.get<ApiResponse<UserNotification[]>>('/users/me/notifications', {
    params: { unreadOnly },
    signal,
  });
  return unwrapApiResponse(
    response.data,
    '알림 API 응답이 올바르지 않습니다.',
    isNotificationArray
  );
}

export async function markAllNotificationsRead(): Promise<number> {
  const response = await apiClient.patch<ApiResponse<{ updated: number }>>(
    '/users/me/notifications/read-all'
  );
  const result = unwrapApiResponse(
    response.data,
    '알림 API 응답이 올바르지 않습니다.',
    isUpdatedResult
  );
  return result.updated;
}
