import type { Live } from '@/types/live';
import type { News } from '@/types/news';
import type { PaginatedResponse } from '@/types/api';
import { apiClient } from '@/lib/api-client';
import { unwrapApiResponse } from '@/lib/api-response';
import { isNewsApiDtoArray, mapNewsApiDto } from '@/lib/api/news-contract';

export async function fetchLiveNow(limit = 5, signal?: AbortSignal): Promise<PaginatedResponse<Live>> {
  const { data } = await apiClient.get('/streaming-events', {
    params: { status: 'LIVE', limit },
    signal,
  });
  return data.data;
}

export async function fetchUpcoming(limit = 5, signal?: AbortSignal): Promise<PaginatedResponse<Live>> {
  const { data } = await apiClient.get('/streaming-events', {
    params: { status: 'SCHEDULED', limit },
    signal,
  });
  return data.data;
}

export async function fetchRecentLives(limit = 10, signal?: AbortSignal): Promise<PaginatedResponse<Live>> {
  const { data } = await apiClient.get('/streaming-events', {
    params: { status: 'ENDED', limit },
    signal,
  });
  return data.data;
}

export async function fetchLatestNews(limit = 10, signal?: AbortSignal): Promise<News[]> {
  const response = await apiClient.get('/news/latest', {
    params: { limit },
    signal,
  });

  return unwrapApiResponse(
    response.data,
    '뉴스 API 응답이 올바르지 않습니다.',
    isNewsApiDtoArray
  ).map(mapNewsApiDto);
}
