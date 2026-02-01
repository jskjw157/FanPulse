import type { Live } from '@/types/live';
import type { News } from '@/types/news';
import type { PaginatedResponse } from '@/types/api';
import { apiClient } from './client';

export async function fetchLiveNow(limit = 5): Promise<PaginatedResponse<Live>> {
  const { data } = await apiClient.get('/api/v1/live', {
    params: { status: 'LIVE', limit },
  });
  return data.data;
}

export async function fetchUpcoming(limit = 5): Promise<PaginatedResponse<Live>> {
  const { data } = await apiClient.get('/api/v1/live', {
    params: { status: 'SCHEDULED', limit },
  });
  return data.data;
}

export async function fetchLatestNews(limit = 10): Promise<PaginatedResponse<News>> {
  const { data } = await apiClient.get('/api/v1/news', {
    params: { limit },
  });
  return data.data;
}
