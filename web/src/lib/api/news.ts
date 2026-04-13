import type { News, NewsDetail } from '@/types/news';
import { apiClient } from '@/lib/api-client';

export async function fetchNewsList(
  limit = 20,
  offset = 0,
  signal?: AbortSignal
): Promise<News[]> {
  const { data } = await apiClient.get('/news/latest', {
    params: { limit, offset },
    signal,
  });

  const result = Array.isArray(data.data) ? data.data : Array.isArray(data) ? data : null;

  if (result === null) {
    console.error('[fetchNewsList] 예상치 못한 응답 구조:', data);
    return [];
  }

  return result;
}

export async function fetchNewsDetail(
  id: string | number,
  signal?: AbortSignal
): Promise<NewsDetail> {
  const { data } = await apiClient.get(`/news/${id}`, { signal });
  return data.data;
}
