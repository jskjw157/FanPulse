import type { Live } from '@/types/live';
import type { News } from '@/types/news';
import { apiClient } from '@/lib/api-client';

export interface SearchResponse {
  live: { items: Live[]; totalCount: number };
  news: { items: News[]; totalCount: number };
}

export async function searchAll(
  query: string,
  limit = 20,
  signal?: AbortSignal
): Promise<SearchResponse> {
  const { data } = await apiClient.get('/search', {
    params: { q: query, limit },
    signal,
  });
  return data;
}
