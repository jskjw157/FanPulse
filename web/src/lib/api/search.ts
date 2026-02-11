import type { SearchResult } from '@/types/search';
import { apiClient } from '@/lib/api-client';

export async function fetchSearchResults(
  query: string,
  signal?: AbortSignal
): Promise<SearchResult> {
  const { data } = await apiClient.get(`/search`, {
    params: { q: query },
    signal,
  });
  return data.data;
}
