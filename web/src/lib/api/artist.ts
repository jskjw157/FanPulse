import { apiClient } from '@/lib/api-client';
import {
  isIsoDate,
  isIsoInstant,
  isNonEmptyString,
  isNullableString,
  isRecord,
  isUuid,
  unwrapApiResponse,
} from '@/lib/api-response';
import type { ApiResponse } from '@/types/api';

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

function isArtistDetail(value: unknown): value is ArtistDetail {
  if (!isRecord(value)) return false;
  return (
    isUuid(value.id) &&
    isNonEmptyString(value.name) &&
    isNullableString(value.englishName) &&
    isNullableString(value.agency) &&
    isNullableString(value.description) &&
    isNullableString(value.profileImageUrl) &&
    typeof value.isGroup === 'boolean' &&
    Array.isArray(value.members) &&
    value.members.every(isNonEmptyString) &&
    typeof value.active === 'boolean' &&
    (value.debutDate === null || isIsoDate(value.debutDate)) &&
    isIsoInstant(value.createdAt)
  );
}

export async function fetchArtistDetail(
  id: string,
  signal?: AbortSignal
): Promise<ArtistDetail> {
  const response = await apiClient.get<ApiResponse<ArtistDetail>>(`/artists/${id}`, { signal });
  return unwrapApiResponse(
    response.data,
    '아티스트 API 응답이 올바르지 않습니다.',
    isArtistDetail
  );
}
