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

export interface ArtistSummary {
  id: string;
  name: string;
  englishName: string | null;
  agency: string | null;
  profileImageUrl: string | null;
  isGroup: boolean;
}

interface ArtistListResponse {
  content: ArtistSummary[];
  totalElements: number;
  page: number;
  size: number;
  totalPages: number;
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

function isNonNegativeInteger(value: unknown): value is number {
  return Number.isInteger(value) && (value as number) >= 0;
}

function isArtistSummary(value: unknown): value is ArtistSummary {
  return isRecord(value) &&
    isUuid(value.id) &&
    isNonEmptyString(value.name) &&
    isNullableString(value.englishName) &&
    isNullableString(value.agency) &&
    isNullableString(value.profileImageUrl) &&
    typeof value.isGroup === 'boolean';
}

function isArtistListResponse(value: unknown): value is ArtistListResponse {
  if (!isRecord(value)) return false;
  if (
    !Array.isArray(value.content) ||
    !value.content.every(isArtistSummary) ||
    !isNonNegativeInteger(value.totalElements) ||
    !isNonNegativeInteger(value.page) ||
    !Number.isInteger(value.size) ||
    (value.size as number) <= 0 ||
    !isNonNegativeInteger(value.totalPages)
  ) return false;

  const expectedPages = value.totalElements === 0
    ? 0
    : Math.ceil((value.totalElements as number) / (value.size as number));
  return value.totalPages === expectedPages &&
    (value.totalPages === 0
      ? value.page === 0 && value.content.length === 0
      : (value.page as number) < (value.totalPages as number));
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

export async function fetchActiveArtists(signal?: AbortSignal): Promise<ArtistSummary[]> {
  const all: ArtistSummary[] = [];
  const seenIds = new Set<string>();
  let page = 0;
  let totalPages = 1;
  let expectedTotalElements: number | null = null;
  let expectedTotalPages: number | null = null;

  do {
    const response = await apiClient.get<ApiResponse<ArtistListResponse>>('/artists', {
      params: { activeOnly: true, page, size: 100, sortBy: 'name', sortDir: 'asc' },
      signal,
    });
    const data = unwrapApiResponse(
      response.data,
      '아티스트 목록 API 응답이 올바르지 않습니다.',
      isArtistListResponse,
    );
    if (data.page !== page || data.size !== 100) {
      throw new Error('아티스트 목록 API 응답이 올바르지 않습니다.');
    }
    if (expectedTotalElements === null) {
      expectedTotalElements = data.totalElements;
      expectedTotalPages = data.totalPages;
    } else if (
      data.totalElements !== expectedTotalElements ||
      data.totalPages !== expectedTotalPages
    ) {
      throw new Error('아티스트 목록 API 응답이 올바르지 않습니다.');
    }
    for (const artist of data.content) {
      if (seenIds.has(artist.id)) {
        throw new Error('아티스트 목록 API 응답이 올바르지 않습니다.');
      }
      seenIds.add(artist.id);
      all.push(artist);
    }
    totalPages = data.totalPages;
    page += 1;
  } while (page < totalPages);

  if (all.length !== expectedTotalElements) {
    throw new Error('아티스트 목록 API 응답이 올바르지 않습니다.');
  }

  return all;
}
