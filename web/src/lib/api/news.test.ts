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

const apiNewsSummary = {
  id: apiNews.id,
  artistId: apiNews.artistId,
  title: apiNews.title,
  thumbnailUrl: apiNews.thumbnailUrl,
  sourceName: apiNews.sourceName,
  category: apiNews.category,
  publishedAt: apiNews.publishedAt,
};

const mappedSummary = {
  id: apiNews.id,
  title: apiNews.title,
  summary: null,
  thumbnailUrl: null,
  source: apiNews.sourceName,
  publishedAt: apiNews.publishedAt,
};

const mappedDetail = {
  ...mappedSummary,
  summary: apiNews.content,
  artistId: apiNews.artistId,
  content: apiNews.content,
  sourceUrl: apiNews.sourceUrl,
  category: apiNews.category,
  viewCount: apiNews.viewCount,
  createdAt: apiNews.createdAt,
};

const apiPage = {
  content: [apiNewsSummary],
  totalElements: 41,
  page: 1,
  size: 20,
  totalPages: 3,
};

describe('news API', () => {
  beforeEach(() => vi.clearAllMocks());

  it('maps the canonical paginated news endpoint without fabricating a summary', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({
      data: { success: true, data: apiPage },
    });

    await expect(fetchNewsList(20, 20)).resolves.toEqual({
      items: [mappedSummary],
      nextOffset: 40,
      hasMore: true,
      totalElements: 41,
    });
    expect(apiClient.get).toHaveBeenCalledWith('/news', {
      params: { page: 1, size: 20, sortBy: 'publishedAt', sortDir: 'desc' },
      signal: undefined,
    });
  });

  it('uses totalPages rather than page length to close pagination', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({
      data: {
        success: true,
        data: { ...apiPage, totalElements: 40, page: 1, totalPages: 2 },
      },
    });

    await expect(fetchNewsList(20, 20)).resolves.toMatchObject({
      nextOffset: 40,
      hasMore: false,
      totalElements: 40,
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

  it.each([
    { ...apiPage, content: [{ ...apiNewsSummary, artistId: 'not-a-uuid' }] },
    { ...apiPage, totalElements: -1 },
    { ...apiPage, page: '1' },
    { ...apiPage, size: 0 },
    { ...apiPage, totalPages: -1 },
    { ...apiPage, totalElements: 100, totalPages: 3 },
    [apiNews],
  ])('rejects malformed successful paginated payloads: %o', async (data) => {
    vi.mocked(apiClient.get).mockResolvedValue({
      data: { success: true, data },
    });

    await expect(fetchNewsList()).rejects.toThrow(
      '뉴스 API 응답이 올바르지 않습니다.'
    );
  });

  it('rejects a valid-shaped page that does not match the requested page', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({
      data: { success: true, data: { ...apiPage, page: 0 } },
    });

    await expect(fetchNewsList(20, 20)).rejects.toThrow(
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
