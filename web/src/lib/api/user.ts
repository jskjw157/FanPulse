import { apiClient, toFanPulseError } from '@/lib/api-client';
import { isIsoInstant, isNonEmptyString, isRecord, isUuid } from '@/lib/api-response';

export interface UserProfile {
  id: string;
  email: string;
  username: string;
  hasPassword: boolean;
  createdAt: string;
}

// Shared by GET /me and PATCH /me, which both return UserResponse directly.
export function parseUserProfile(data: unknown): UserProfile {
  if (
    !isRecord(data) ||
    !isUuid(data.id) ||
    !isNonEmptyString(data.email) ||
    !isNonEmptyString(data.username) ||
    typeof data.hasPassword !== 'boolean' ||
    !isIsoInstant(data.createdAt)
  ) {
    throw new Error('프로필 API 응답이 올바르지 않습니다.');
  }

  return {
    id: data.id,
    email: data.email,
    username: data.username,
    hasPassword: data.hasPassword,
    createdAt: data.createdAt,
  };
}

export async function fetchMyProfile(signal?: AbortSignal): Promise<UserProfile> {
  const { data } = await apiClient.get<unknown>('/me', { signal });
  return parseUserProfile(data);
}

export type UserLanguage = 'ko' | 'en';

export interface UserSettings {
  theme: string;
  language: UserLanguage;
  pushEnabled: boolean;
  updatedAt: string;
}

function readSettings(settings: UserSettings): UserSettings {
  if (settings.language !== 'ko' && settings.language !== 'en') {
    throw new Error('Unsupported language');
  }
  return settings;
}

export async function updateMyProfile(username: string, signal?: AbortSignal): Promise<UserProfile> {
  try {
    const { data } = await apiClient.patch<unknown>('/me', { username }, { signal });
    return parseUserProfile(data);
  } catch (error) {
    throw toFanPulseError(error);
  }
}

export async function fetchMySettings(signal?: AbortSignal): Promise<UserSettings> {
  try {
    const { data } = await apiClient.get<UserSettings>('/me/settings', { signal });
    return readSettings(data);
  } catch (error) {
    throw toFanPulseError(error);
  }
}

export async function updateMyLanguage(language: UserLanguage, signal?: AbortSignal): Promise<UserSettings> {
  try {
    if (language !== 'ko' && language !== 'en') throw new Error('Unsupported language');
    const { data } = await apiClient.patch<UserSettings>('/me/settings', { language }, { signal });
    return readSettings(data);
  } catch (error) {
    throw toFanPulseError(error);
  }
}
