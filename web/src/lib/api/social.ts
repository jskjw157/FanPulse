import { apiClient } from '@/lib/api-client';
import { expectedUserHeaders, privateRequestError } from '@/lib/session-sync';
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
  targetType?: string | null;
  targetId?: string | null;
  targetAvailable?: boolean;
}

export interface NotificationPage {
  content: UserNotification[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
  unreadCount: number;
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
    isIsoDateTime(value.createdAt) &&
    (value.targetType === undefined || isNullableString(value.targetType)) &&
    (value.targetId === undefined || value.targetId === null || isUuid(value.targetId)) &&
    (value.targetAvailable === undefined || typeof value.targetAvailable === 'boolean')
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

export async function fetchFavorites(signal?: AbortSignal, expectedUserId?: string): Promise<FavoriteArtist[]> {
  const response = await apiClient.get<ApiResponse<FavoriteArtist[]>>('/users/me/favorites', {
    signal, ...expectedUserHeaders(expectedUserId),
  }).catch((error) => { throw privateRequestError(error); });
  return unwrapApiResponse(
    response.data,
    '즐겨찾기 API 응답이 올바르지 않습니다.',
    isFavoriteArray
  );
}

export async function addFavorite(artistId: string, expectedUserId: string): Promise<FavoriteArtist> {
  const response = await apiClient.post<ApiResponse<FavoriteArtist>>(
    `/users/me/favorites/${artistId}`, undefined, expectedUserHeaders(expectedUserId),
  ).catch((error) => { throw privateRequestError(error); });
  return unwrapApiResponse(
    response.data,
    '즐겨찾기 API 응답이 올바르지 않습니다.',
    isFavoriteArtist
  );
}

export async function removeFavorite(artistId: string, expectedUserId: string): Promise<void> {
  await apiClient.delete(`/users/me/favorites/${artistId}`, expectedUserHeaders(expectedUserId))
    .catch((error) => { throw privateRequestError(error); });
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

export async function markAllNotificationsRead(expectedUserId?: string): Promise<number> {
  const response = await apiClient.patch<ApiResponse<{ updated: number }>>(
    '/users/me/notifications/read-all', undefined, expectedUserHeaders(expectedUserId)
  ).catch((error) => { throw privateRequestError(error); });
  const result = unwrapApiResponse(
    response.data,
    '알림 API 응답이 올바르지 않습니다.',
    isUpdatedResult
  );
  return result.updated;
}

function isCount(value: unknown): value is number {
  return Number.isSafeInteger(value) && (value as number) >= 0;
}

function isNotificationPage(value: unknown): value is NotificationPage {
  if (!isRecord(value) || !isNotificationArray(value.content) ||
      !isCount(value.page) || !isCount(value.size) || value.size === 0 ||
      !isCount(value.totalElements) || !isCount(value.totalPages) ||
      !isCount(value.unreadCount) || typeof value.last !== 'boolean') return false;
  return value.content.length <= value.size &&
    value.totalPages === Math.ceil(value.totalElements / value.size) &&
    value.last === (value.page + 1 >= value.totalPages);
}

export async function fetchNotificationPage(
  unreadOnly: boolean,
  page = 0,
  size = 20,
  signal?: AbortSignal,
  expectedUserId?: string,
): Promise<NotificationPage> {
  const response = await apiClient.get<ApiResponse<NotificationPage>>('/users/me/notifications/page', {
    params: { unreadOnly, page, size }, signal, ...expectedUserHeaders(expectedUserId),
  }).catch((error) => { throw privateRequestError(error); });
  const data = unwrapApiResponse(response.data, '알림 API 응답이 올바르지 않습니다.', isNotificationPage);
  if (data.page !== page || data.size !== size) throw new Error('알림 페이지 응답이 올바르지 않습니다.');
  return data;
}

export async function fetchNotificationUnreadCount(signal?: AbortSignal, expectedUserId?: string): Promise<number> {
  const response = await apiClient.get<ApiResponse<{ unreadCount: number }>>(
    '/users/me/notifications/unread-count', { signal, ...expectedUserHeaders(expectedUserId) },
  ).catch((error) => { throw privateRequestError(error); });
  const data = unwrapApiResponse(response.data, '알림 API 응답이 올바르지 않습니다.',
    (value): value is { unreadCount: number } => isRecord(value) && isCount(value.unreadCount));
  return data.unreadCount;
}

export async function markNotificationRead(id: string, expectedUserId?: string): Promise<UserNotification> {
  if (!isUuid(id)) throw new Error('알림 ID가 올바르지 않습니다.');
  const response = await apiClient.patch<ApiResponse<UserNotification>>(
    `/users/me/notifications/${id}/read`, undefined, expectedUserHeaders(expectedUserId),
  ).catch((error) => { throw privateRequestError(error); });
  const data = unwrapApiResponse(response.data, '알림 API 응답이 올바르지 않습니다.', isUserNotification);
  if (data.id !== id || !data.isRead) throw new Error('알림 읽음 응답이 올바르지 않습니다.');
  return data;
}
