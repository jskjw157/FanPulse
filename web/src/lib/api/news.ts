import type { News, NewsDetail } from '@/types/news';
import { apiClient } from '@/lib/api-client';
import { unwrapApiResponse } from '@/lib/api-response';
import {
  isNewsApiDto,
  isNewsApiDtoArray,
  mapNewsApiDto,
  mapNewsDetailApiDto,
} from '@/lib/api/news-contract';

export async function fetchNewsList(
  limit = 20,
  offset = 0,
  signal?: AbortSignal
): Promise<News[]> {
  const response = await apiClient.get('/news/latest', {
    params: { limit, offset },
    signal,
  });

  return unwrapApiResponse(
    response.data,
    '뉴스 API 응답이 올바르지 않습니다.',
    isNewsApiDtoArray
  ).map(mapNewsApiDto);
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
