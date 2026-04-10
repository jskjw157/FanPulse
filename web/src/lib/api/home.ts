import type { Live } from '@/types/live';
import type { News } from '@/types/news';
import type { PaginatedResponse } from '@/types/api';
import { apiClient } from '@/lib/api-client';

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
  const { data } = await apiClient.get('/news/latest', {
    params: { limit },
    signal,
  });

  const result = Array.isArray(data.data) ? data.data : Array.isArray(data) ? data : null;

  if (result === null) {
    console.error('[fetchLatestNews] 예상치 못한 응답 구조:', data);
    return [];
  }

  return result;
}
