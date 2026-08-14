import type { News, NewsDetail } from '@/types/news';
import { apiClient } from '@/lib/api-client';
import { isRecord, unwrapApiResponse } from '@/lib/api-response';
import {
  isNewsApiDto,
  isNewsSummaryApiDto,
  mapNewsDetailApiDto,
  mapNewsSummaryApiDto,
  type NewsSummaryApiDto,
} from '@/lib/api/news-contract';

interface NewsListApiDto {
  content: NewsSummaryApiDto[];
  totalElements: number;
  page: number;
  size: number;
  totalPages: number;
}

export interface NewsPage {
  items: News[];
  nextOffset: number;
  hasMore: boolean;
  totalElements: number;
}

function isNonNegativeInteger(value: unknown): value is number {
  return Number.isSafeInteger(value) && (value as number) >= 0;
}

function isPositiveInteger(value: unknown): value is number {
  return Number.isSafeInteger(value) && (value as number) > 0;
}

function isNewsListApiDto(value: unknown): value is NewsListApiDto {
  if (!isRecord(value)) return false;
  if (!Array.isArray(value.content) || !value.content.every(isNewsSummaryApiDto)) return false;
  if (!isNonNegativeInteger(value.totalElements)) return false;
  if (!isNonNegativeInteger(value.page)) return false;
  if (!isPositiveInteger(value.size) || value.size > 100) return false;
  if (!isNonNegativeInteger(value.totalPages)) return false;
  if (value.content.length > value.size) return false;

  return value.totalPages === Math.ceil(value.totalElements / value.size);
}

export async function fetchNewsList(
  limit = 20,
  offset = 0,
  signal?: AbortSignal
): Promise<NewsPage> {
  const size = Math.min(100, Math.max(1, Math.trunc(limit)));
  const safeOffset = Math.max(0, Math.trunc(offset));
  const page = Math.floor(safeOffset / size);
  const response = await apiClient.get('/news', {
    params: { page, size, sortBy: 'publishedAt', sortDir: 'desc' },
    signal,
  });
  const data = unwrapApiResponse(
    response.data,
    '뉴스 API 응답이 올바르지 않습니다.',
    isNewsListApiDto
  );
  if (data.page !== page || data.size !== size) {
    throw new Error('뉴스 API 응답이 올바르지 않습니다.');
  }

  return {
    items: data.content.map(mapNewsSummaryApiDto),
    nextOffset: (data.page + 1) * data.size,
    hasMore: data.page + 1 < data.totalPages,
    totalElements: data.totalElements,
  };
}

export async function fetchNewsDetail(
  id: string | number,
  signal?: AbortSignal
): Promise<NewsDetail> {
  const response = await apiClient.get(`/news/${id}`, { signal });
  return mapNewsDetailApiDto(
    unwrapApiResponse(
      response.data,
      '뉴스 상세 API 응답이 올바르지 않습니다.',
      isNewsApiDto
    )
  );
}
