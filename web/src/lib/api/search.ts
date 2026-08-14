import type { Live } from '@/types/live';
import type { News } from '@/types/news';
import { apiClient } from '@/lib/api-client';
import {
  isIsoDateTime,
  isNonEmptyString,
  isRecord,
  isUuid,
} from '@/lib/api-response';
import { isSearchNewsApiDto, mapSearchNewsApiDto } from '@/lib/api/news-contract';

interface SearchLiveApiDto {
  id: string;
  title: string;
  artistId: string;
  artistName: string;
  thumbnailUrl: string | null;
  status: Live['status'];
  scheduledAt: string;
}

function isNullableHttpsUrl(value: unknown): value is string | null {
  if (value === null) return true;
  if (!isNonEmptyString(value)) return false;

  try {
    return new URL(value).protocol === 'https:';
  } catch {
    return false;
  }
}

function isSearchLiveApiDto(value: unknown): value is SearchLiveApiDto {
  if (!isRecord(value)) return false;

  return (
    isUuid(value.id) &&
    isUuid(value.artistId) &&
    isNonEmptyString(value.title) &&
    isNonEmptyString(value.artistName) &&
    isNullableHttpsUrl(value.thumbnailUrl) &&
    (value.status === 'LIVE' || value.status === 'SCHEDULED' || value.status === 'ENDED') &&
    isIsoDateTime(value.scheduledAt)
  );
}

function mapSearchLiveApiDto(value: SearchLiveApiDto): Live {
  return {
    id: value.id,
    title: value.title,
    artistName: value.artistName,
    thumbnailUrl: value.thumbnailUrl,
    status: value.status,
    scheduledAt: value.scheduledAt,
  };
}

export interface SearchResponse {
  live: { items: Live[]; totalCount: number };
  news: { items: News[]; totalCount: number };
}

export async function searchAll(
  query: string,
  limit = 20,
  signal?: AbortSignal
): Promise<SearchResponse> {
  const response = await apiClient.get('/search', {
    params: { q: query, limit },
    signal,
  });

  const data: unknown = response.data;
  if (!isRecord(data) || !isRecord(data.live) || !isRecord(data.news)) {
    throw new Error('검색 API 응답이 올바르지 않습니다.');
  }

  const liveItems = data.live.items;
  const newsItems = data.news.items;
  const liveTotal = data.live.totalCount;
  const newsTotal = data.news.totalCount;
  if (
    !Array.isArray(liveItems) ||
    !liveItems.every(isSearchLiveApiDto) ||
    !Array.isArray(newsItems) ||
    !newsItems.every(isSearchNewsApiDto) ||
    !Number.isInteger(liveTotal) ||
    (liveTotal as number) < 0 ||
    !Number.isInteger(newsTotal) ||
    (newsTotal as number) < 0
  ) {
    throw new Error('검색 API 응답이 올바르지 않습니다.');
  }

  return {
    live: {
      items: liveItems.map(mapSearchLiveApiDto),
      totalCount: liveTotal as number,
    },
    news: {
      items: newsItems.map(mapSearchNewsApiDto),
      totalCount: newsTotal as number,
    },
  };
}
