import { renderHook, waitFor, act } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { useInfiniteLiveList } from './useInfiniteLiveList';

vi.mock('@/lib/api/live', () => ({
  fetchLiveList: vi.fn(),
}));

import { fetchLiveList } from '@/lib/api/live';
import { mockLiveList } from '@/__mocks__/live';

const mockedFetchLiveList = vi.mocked(fetchLiveList);

describe('useInfiniteLiveList', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('starts in loading state and fetches initial data', async () => {
    mockedFetchLiveList.mockResolvedValue({
      items: mockLiveList.slice(0, 3),
      hasMore: true,
      nextCursor: 'cursor-1',
    });

    const { result } = renderHook(() => useInfiniteLiveList());

    expect(result.current.state).toBe('loading');

    await waitFor(() => {
      expect(result.current.state).toBe('success');
    });

    expect(result.current.items).toHaveLength(3);
    expect(result.current.hasMore).toBe(true);
  });

  it('loads more items when loadMore is called', async () => {
    mockedFetchLiveList
      .mockResolvedValueOnce({
        items: mockLiveList.slice(0, 3),
        hasMore: true,
        nextCursor: 'cursor-1',
      })
      .mockResolvedValueOnce({
        items: mockLiveList.slice(3, 6),
        hasMore: false,
      });

    const { result } = renderHook(() => useInfiniteLiveList());

    await waitFor(() => {
      expect(result.current.state).toBe('success');
    });

    await act(async () => {
      await result.current.loadMore();
    });

    expect(result.current.items).toHaveLength(6);
    expect(result.current.hasMore).toBe(false);
  });

  it('resets list on refresh', async () => {
    mockedFetchLiveList.mockResolvedValue({
      items: mockLiveList.slice(0, 3),
      hasMore: true,
      nextCursor: 'cursor-1',
    });

    const { result } = renderHook(() => useInfiniteLiveList());

    await waitFor(() => {
      expect(result.current.state).toBe('success');
    });

    vi.clearAllMocks();
    mockedFetchLiveList.mockResolvedValue({
      items: mockLiveList.slice(0, 2),
      hasMore: false,
    });

    await act(async () => {
      await result.current.refresh();
    });

    expect(result.current.items).toHaveLength(2);
    expect(mockedFetchLiveList).toHaveBeenCalledWith(
      expect.objectContaining({ cursor: undefined })
    );
  });

  it('sets error state on API failure', async () => {
    mockedFetchLiveList.mockRejectedValue(new Error('Network error'));

    const { result } = renderHook(() => useInfiniteLiveList());

    await waitFor(() => {
      expect(result.current.state).toBe('error');
    });

    expect(result.current.error).toBe('데이터를 불러올 수 없습니다');
  });
});
