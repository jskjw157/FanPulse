import { apiClient } from '@/lib/api-client';

export interface UserProfile {
  id: string;
  email: string;
  username?: string;
  profileImageUrl?: string;
}

export async function fetchMyProfile(signal?: AbortSignal): Promise<UserProfile> {
  const { data } = await apiClient.get('/users/me', { signal });
  return data.data ?? data;
}
