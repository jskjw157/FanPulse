import { apiClient } from '@/lib/api-client';

export interface ArtistDetail {
  id: string;
  name: string;
  englishName: string | null;
  agency: string | null;
  description: string | null;
  profileImageUrl: string | null;
  isGroup: boolean;
  members: string[];
  active: boolean;
  debutDate: string | null;
  createdAt: string;
}

interface ApiResponse<T> {
  data: T;
}

export async function fetchArtistDetail(
  id: string,
  signal?: AbortSignal
): Promise<ArtistDetail> {
  const response = await apiClient.get<ApiResponse<ArtistDetail>>(`/artists/${id}`, { signal });
  return response.data.data;
}
