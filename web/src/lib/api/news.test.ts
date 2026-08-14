import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@/lib/api-client', () => ({
  apiClient: {
    get: vi.fn(),
  },
}));

import { apiClient } from '@/lib/api-client';
import { fetchNewsDetail, fetchNewsList } from './news';

const apiNews = {
  id: '11111111-1111-1111-1111-111111111111',
  artistId: '22222222-2222-2222-2222-222222222222',
  title: '실제 API 뉴스',
  content: '실제 수집 기사 요약 내용',
  sourceUrl: 'https://news.google.com/articles/real',
  sourceName: '실제 언론사',
  thumbnailUrl: null,
  category: 'GENERAL',
  viewCount: 0,
  publishedAt: '2026-08-14T02:30:00Z',
  createdAt: '2026-08-14T02:50:00Z',
};

const mappedSummary = {
  id: apiNews.id,
  title: apiNews.title,
  summary: apiNews.content,
  thumbnailUrl: null,
  source: apiNews.sourceName,
  publishedAt: apiNews.publishedAt,
};

const mappedDetail = {
  ...mappedSummary,
  artistId: apiNews.artistId,
  content: apiNews.content,
  sourceUrl: apiNews.sourceUrl,
  category: apiNews.category,
  viewCount: apiNews.viewCount,
  createdAt: apiNews.createdAt,
};

describe('news API', () => {
  beforeEach(() => vi.clearAllMocks());

  it('maps validated latest news list responses', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({
      data: { success: true, data: [apiNews] },
    });

    await expect(fetchNewsList(20, 5)).resolves.toEqual([mappedSummary]);
    expect(apiClient.get).toHaveBeenCalledWith('/news/latest', {
      params: { limit: 20, offset: 5 },
      signal: undefined,
    });
  });

  it('maps validated news detail responses', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({
      data: { success: true, data: apiNews },
    });

    await expect(fetchNewsDetail(apiNews.id)).resolves.toEqual(mappedDetail);
    expect(apiClient.get).toHaveBeenCalledWith(`/news/${apiNews.id}`, {
      signal: undefined,
    });
  });

  it('rejects malformed list elements instead of returning them as News', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({
      data: { success: true, data: [{ ...apiNews, content: null }] },
    });

    await expect(fetchNewsList()).rejects.toThrow(
      '뉴스 API 응답이 올바르지 않습니다.'
    );
  });

  it('rejects malformed detail payloads', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({
      data: { success: true, data: { ...apiNews, sourceUrl: '' } },
    });

    await expect(fetchNewsDetail(apiNews.id)).rejects.toThrow(
      '뉴스 상세 API 응답이 올바르지 않습니다.'
    );
  });
});
