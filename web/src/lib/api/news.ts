import type { NewsDetail } from '@/types/news';
import { apiClient } from '@/lib/api-client';

export async function fetchNewsDetail(
  id: string | number,
  signal?: AbortSignal
): Promise<NewsDetail> {
  const { data } = await apiClient.get(`/news/${id}`, { signal });
  return data.data;
}
