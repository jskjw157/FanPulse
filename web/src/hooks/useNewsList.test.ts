import { act, renderHook, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { useNewsList } from './useNewsList';
import { fetchNewsList } from '@/lib/api/news';
import type { NewsPage } from '@/lib/api/news';

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

function pendingPage() {
  let resolve!: (page: NewsPage) => void;
  let reject!: (error: Error) => void;
  const promise = new Promise<NewsPage>((yes, no) => {
    resolve = yes;
    reject = no;
  });
  return { promise, resolve, reject };
}

describe('useNewsList', () => {
  beforeEach(() => vi.mocked(fetchNewsList).mockReset());

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

    await act(async () => {
      await result.current.loadMore();
    });
    expect(fetchNewsList).toHaveBeenCalledTimes(2);
  });

  it('locks a deferred second page and retries a 503 at the same offset without losing items', async () => {
    const pageTwo = pendingPage();
    const retry = pendingPage();
    vi.mocked(fetchNewsList)
      .mockResolvedValueOnce({ items: [first], nextOffset: 1, hasMore: true, totalElements: 2 })
      .mockReturnValueOnce(pageTwo.promise)
      .mockReturnValueOnce(retry.promise);

    const { result } = renderHook(() => useNewsList({ limit: 1 }));
    await waitFor(() => expect(result.current.state).toBe('success'));

    let request!: Promise<void>;
    act(() => {
      // Same-tick calls must be locked before React renders the loading state.
      request = result.current.loadMore();
      void result.current.loadMore();
      void result.current.loadMore();
    });
    expect(result.current.state).toBe('loading');
    expect(result.current.items).toEqual([first]);
    expect(result.current.hasMore).toBe(true);
    expect(fetchNewsList).toHaveBeenCalledTimes(2);
    expect(fetchNewsList).toHaveBeenNthCalledWith(2, 1, 1, expect.any(AbortSignal));
    expect(vi.mocked(fetchNewsList).mock.calls[1][2]?.aborted).toBe(false);

    await act(async () => { await result.current.loadMore(); });
    expect(fetchNewsList).toHaveBeenCalledTimes(2);

    await act(async () => {
      pageTwo.reject(Object.assign(new Error('Service Unavailable'), {
        isAxiosError: true,
        response: { status: 503 },
      }));
      await request;
    });
    expect(result.current.state).toBe('error');
    expect(result.current.error).toBe('서버에 문제가 발생했습니다');
    expect(result.current.items).toEqual([first]);
    expect(result.current.hasMore).toBe(true);

    act(() => {
      request = result.current.loadMore();
      void result.current.loadMore();
    });
    expect(result.current.state).toBe('loading');
    expect(result.current.error).toBeNull();
    expect(result.current.items).toEqual([first]);
    expect(fetchNewsList).toHaveBeenCalledTimes(3);
    expect(fetchNewsList).toHaveBeenNthCalledWith(3, 1, 1, expect.any(AbortSignal));

    await act(async () => {
      retry.resolve({ items: [second], nextOffset: 2, hasMore: false, totalElements: 2 });
      await request;
    });
    expect(result.current.state).toBe('success');
    expect(result.current.error).toBeNull();
    expect(result.current.items).toEqual([first, second]);
    expect(result.current.hasMore).toBe(false);
    expect(fetchNewsList).toHaveBeenCalledTimes(3);
  });

  it.each(['success', 'error'])('ignores a canceled page-two %s after a refresh', async (outcome) => {
    const pageTwo = pendingPage();
    vi.mocked(fetchNewsList)
      .mockResolvedValueOnce({ items: [first], nextOffset: 1, hasMore: true, totalElements: 2 })
      .mockReturnValueOnce(pageTwo.promise)
      .mockResolvedValueOnce({ items: [second], nextOffset: 1, hasMore: true, totalElements: 2 });

    const { result } = renderHook(() => useNewsList({ limit: 1 }));
    await waitFor(() => expect(result.current.state).toBe('success'));

    let request!: Promise<void>;
    act(() => { request = result.current.loadMore(); });
    const signal = vi.mocked(fetchNewsList).mock.calls[1][2];
    await act(async () => { await result.current.refresh(); });
    expect(signal?.aborted).toBe(true);
    expect(fetchNewsList).toHaveBeenNthCalledWith(3, 1, 0, expect.any(AbortSignal));

    await act(async () => {
      if (outcome === 'success') {
        pageTwo.resolve({ items: [first], nextOffset: 2, hasMore: false, totalElements: 2 });
      } else {
        pageTwo.reject(new Error('late failure'));
      }
      await request;
    });
    expect(result.current.items).toEqual([second]);
    expect(result.current.state).toBe('success');
    expect(result.current.error).toBeNull();
    expect(result.current.hasMore).toBe(true);
  });
});
