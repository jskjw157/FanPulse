import { act, renderHook, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { useNewsList } from './useNewsList';
import { fetchNewsList } from '@/lib/api/news';

vi.mock('@/lib/api/news', () => ({
  fetchNewsList: vi.fn(),
}));

const first = {
  id: '11111111-1111-1111-1111-111111111111',
  title: '첫 기사',
  summary: null,
  thumbnailUrl: null,
  source: '언론사',
  publishedAt: '2026-08-14T03:00:00Z',
};

const second = {
  ...first,
  id: '22222222-2222-2222-2222-222222222222',
  title: '둘째 기사',
};

describe('useNewsList', () => {
  beforeEach(() => vi.clearAllMocks());

  it('advances by nextOffset and stops from server pagination metadata', async () => {
    vi.mocked(fetchNewsList)
      .mockResolvedValueOnce({
        items: [first],
        nextOffset: 1,
        hasMore: true,
        totalElements: 2,
      })
      .mockResolvedValueOnce({
        items: [second],
        nextOffset: 2,
        hasMore: false,
        totalElements: 2,
      });

    const { result } = renderHook(() => useNewsList({ limit: 1 }));

    await waitFor(() => expect(result.current.state).toBe('success'));
    expect(result.current.items).toEqual([first]);
    expect(result.current.hasMore).toBe(true);
    expect(fetchNewsList).toHaveBeenNthCalledWith(1, 1, 0, expect.any(AbortSignal));

    await act(async () => {
      await result.current.loadMore();
    });

    expect(result.current.items).toEqual([first, second]);
    expect(result.current.hasMore).toBe(false);
    expect(fetchNewsList).toHaveBeenNthCalledWith(2, 1, 1, expect.any(AbortSignal));
  });
});
