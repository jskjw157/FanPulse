import type { Live } from '@/types/live';
import type { News } from '@/types/news';
import { apiClient } from '@/lib/api-client';

export interface SearchResultItem {
  type: 'LIVE' | 'NEWS';
  live?: Live;
  news?: News;
}

export interface SearchResponse {
  items: SearchResultItem[];
  hasMore: boolean;
  nextCursor?: string;
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
  return data.data;
}
