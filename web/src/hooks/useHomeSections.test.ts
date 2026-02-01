import { renderHook, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { useHomeSections } from './useHomeSections';

vi.mock('@/lib/api/home', () => ({
  fetchLiveNow: vi.fn(),
  fetchUpcoming: vi.fn(),
  fetchLatestNews: vi.fn(),
}));

import { fetchLiveNow, fetchUpcoming, fetchLatestNews } from '@/lib/api/home';
import { mockLiveNow, mockUpcoming } from '@/__mocks__/live';
import { mockLatestNews } from '@/__mocks__/news';

const mockedFetchLiveNow = vi.mocked(fetchLiveNow);
const mockedFetchUpcoming = vi.mocked(fetchUpcoming);
const mockedFetchLatestNews = vi.mocked(fetchLatestNews);

describe('useHomeSections', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('starts in loading state', () => {
    mockedFetchLiveNow.mockReturnValue(new Promise(() => {}));
    mockedFetchUpcoming.mockReturnValue(new Promise(() => {}));
    mockedFetchLatestNews.mockReturnValue(new Promise(() => {}));

    const { result } = renderHook(() => useHomeSections());
    expect(result.current.state).toBe('loading');
  });

  it('fetches all data in parallel and returns success state', async () => {
    mockedFetchLiveNow.mockResolvedValue({ items: mockLiveNow, hasMore: false });
    mockedFetchUpcoming.mockResolvedValue({ items: mockUpcoming, hasMore: false });
    mockedFetchLatestNews.mockResolvedValue({ items: mockLatestNews, hasMore: true });

    const { result } = renderHook(() => useHomeSections());

    await waitFor(() => {
      expect(result.current.state).toBe('success');
    });

    expect(result.current.liveNow).toEqual(mockLiveNow);
    expect(result.current.upcoming).toEqual(mockUpcoming);
    expect(result.current.latestNews).toEqual(mockLatestNews);
    expect(result.current.error).toBeNull();
  });

  it('sets error state when any API fails', async () => {
    mockedFetchLiveNow.mockRejectedValue(new Error('네트워크 오류'));
    mockedFetchUpcoming.mockResolvedValue({ items: mockUpcoming, hasMore: false });
    mockedFetchLatestNews.mockResolvedValue({ items: mockLatestNews, hasMore: true });

    const { result } = renderHook(() => useHomeSections());

    await waitFor(() => {
      expect(result.current.state).toBe('error');
    });

    expect(result.current.error).toBe('데이터를 불러올 수 없습니다');
  });

  it('calls all three APIs', async () => {
    mockedFetchLiveNow.mockResolvedValue({ items: [], hasMore: false });
    mockedFetchUpcoming.mockResolvedValue({ items: [], hasMore: false });
    mockedFetchLatestNews.mockResolvedValue({ items: [], hasMore: false });

    renderHook(() => useHomeSections());

    await waitFor(() => {
      expect(mockedFetchLiveNow).toHaveBeenCalledOnce();
      expect(mockedFetchUpcoming).toHaveBeenCalledOnce();
      expect(mockedFetchLatestNews).toHaveBeenCalledOnce();
    });
  });

  it('refresh re-fetches all data', async () => {
    mockedFetchLiveNow.mockResolvedValue({ items: mockLiveNow, hasMore: false });
    mockedFetchUpcoming.mockResolvedValue({ items: mockUpcoming, hasMore: false });
    mockedFetchLatestNews.mockResolvedValue({ items: mockLatestNews, hasMore: true });

    const { result } = renderHook(() => useHomeSections());

    await waitFor(() => {
      expect(result.current.state).toBe('success');
    });

    vi.clearAllMocks();
    mockedFetchLiveNow.mockResolvedValue({ items: [], hasMore: false });
    mockedFetchUpcoming.mockResolvedValue({ items: [], hasMore: false });
    mockedFetchLatestNews.mockResolvedValue({ items: [], hasMore: false });

    await result.current.refresh();

    await waitFor(() => {
      expect(mockedFetchLiveNow).toHaveBeenCalledOnce();
      expect(result.current.liveNow).toEqual([]);
    });
  });
});
