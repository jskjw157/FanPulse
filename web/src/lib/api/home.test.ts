import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@/lib/api-client', () => ({
  apiClient: {
    get: vi.fn(),
  },
}));

import { apiClient } from '@/lib/api-client';
import { fetchLatestNews } from './home';

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

const mappedNews = {
  id: apiNews.id,
  title: apiNews.title,
  summary: apiNews.content,
  thumbnailUrl: null,
  source: apiNews.sourceName,
  publishedAt: apiNews.publishedAt,
};

describe('fetchLatestNews', () => {
  beforeEach(() => vi.clearAllMocks());

  it('validates the API envelope and maps the production news DTO', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({
      data: { success: true, data: [apiNews] },
    });

    await expect(fetchLatestNews(5)).resolves.toEqual([mappedNews]);
    expect(apiClient.get).toHaveBeenCalledWith('/news/latest', {
      params: { limit: 5 },
      signal: undefined,
    });
  });

  it.each([
    { success: false, data: [apiNews] },
    { success: true, data: null },
    [apiNews],
  ])('rejects unsuccessful, empty, or legacy raw payloads: %o', async (payload) => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: payload });

    await expect(fetchLatestNews()).rejects.toThrow(
      '뉴스 API 응답이 올바르지 않습니다.'
    );
  });

  it.each([
    { ...apiNews, id: 1 },
    { ...apiNews, artistId: 'not-a-uuid' },
    { ...apiNews, sourceName: '' },
    { ...apiNews, thumbnailUrl: 42 },
    { ...apiNews, thumbnailUrl: 'http://images.example.com/news.jpg' },
    { ...apiNews, sourceUrl: 'javascript:alert(1)' },
    { ...apiNews, publishedAt: 'not-a-date' },
  ])('rejects malformed successful news payloads: %o', async (item) => {
    vi.mocked(apiClient.get).mockResolvedValue({
      data: { success: true, data: [item] },
    });

    await expect(fetchLatestNews()).rejects.toThrow(
      '뉴스 API 응답이 올바르지 않습니다.'
    );
  });
});
