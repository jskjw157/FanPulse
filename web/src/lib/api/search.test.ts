import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@/lib/api-client', () => ({
  apiClient: {
    get: vi.fn(),
  },
}));

import { apiClient } from '@/lib/api-client';
import { searchAll } from './search';

const live = {
  id: '11111111-1111-1111-1111-111111111111',
  title: 'Live',
  artistId: '22222222-2222-2222-2222-222222222222',
  artistName: 'Artist',
  thumbnailUrl: null,
  status: 'LIVE',
  scheduledAt: '2026-08-14T02:00:00Z',
};

const searchNews = {
  id: '33333333-3333-3333-3333-333333333333',
  title: '검색된 실제 뉴스',
  summary: '검색 API가 제공한 실제 요약',
  sourceName: '실제 언론사',
  publishedAt: '2026-08-14T02:30:00Z',
};

describe('searchAll news contract', () => {
  beforeEach(() => vi.clearAllMocks());

  it('maps validated search news items to the shared card model', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({
      data: {
        live: { items: [live], totalCount: 1 },
        news: { items: [searchNews], totalCount: 1 },
      },
    });

    await expect(searchAll('검색')).resolves.toEqual({
      live: {
        items: [
          {
            id: live.id,
            title: live.title,
            artistName: live.artistName,
            thumbnailUrl: null,
            status: live.status,
            scheduledAt: live.scheduledAt,
          },
        ],
        totalCount: 1,
      },
      news: {
        items: [
          {
            id: searchNews.id,
            title: searchNews.title,
            summary: searchNews.summary,
            thumbnailUrl: null,
            source: searchNews.sourceName,
            publishedAt: searchNews.publishedAt,
          },
        ],
        totalCount: 1,
      },
    });
  });

  it('rejects malformed successful search news items', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({
      data: {
        live: { items: [], totalCount: 0 },
        news: { items: [{ ...searchNews, id: 7 }], totalCount: 1 },
      },
    });

    await expect(searchAll('검색')).rejects.toThrow(
      '검색 API 응답이 올바르지 않습니다.'
    );
  });

  it('rejects malformed successful search live items', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({
      data: {
        live: { items: [{ ...live, scheduledAt: 'not-a-date' }], totalCount: 1 },
        news: { items: [], totalCount: 0 },
      },
    });

    await expect(searchAll('검색')).rejects.toThrow(
      '검색 API 응답이 올바르지 않습니다.'
    );
  });

  it('rejects non-HTTPS search live thumbnails', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({
      data: {
        live: {
          items: [{ ...live, thumbnailUrl: 'http://images.example.com/live.jpg' }],
          totalCount: 1,
        },
        news: { items: [], totalCount: 0 },
      },
    });

    await expect(searchAll('검색')).rejects.toThrow(
      '검색 API 응답이 올바르지 않습니다.'
    );
  });
});
