import type { Live } from '@/types/live';
import type { News } from '@/types/news';
import type { PaginatedResponse } from '@/types/api';
import { apiClient } from '@/lib/api-client';

export async function fetchLiveNow(limit = 5): Promise<PaginatedResponse<Live>> {
  const { data } = await apiClient.get('/streaming-events', {
    params: { status: 'LIVE', limit },
  });
  return data.data;
}

export async function fetchUpcoming(limit = 5): Promise<PaginatedResponse<Live>> {
  const { data } = await apiClient.get('/streaming-events', {
    params: { status: 'SCHEDULED', limit },
  });
  return data.data;
}

export async function fetchLatestNews(limit = 10): Promise<News[]> {
  const { data } = await apiClient.get('/news/latest', {
    params: { limit },
  });
  return data.data;
}
