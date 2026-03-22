import { renderHook, act, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { useSearch } from './useSearch';
import { mockSearchResult } from '@/__mocks__/search';

vi.mock('@/lib/api/search', () => ({
  fetchSearchResults: vi.fn(),
}));

import { fetchSearchResults } from '@/lib/api/search';

const mockedFetch = vi.mocked(fetchSearchResults);

describe('useSearch', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('initializes with idle state', () => {
    const { result } = renderHook(() => useSearch());

    expect(result.current.query).toBe('');
    expect(result.current.results).toBeNull();
    expect(result.current.state).toBe('idle');
    expect(result.current.error).toBeNull();
  });

  it('updates query with setQuery', () => {
    const { result } = renderHook(() => useSearch());

    act(() => {
      result.current.setQuery('BTS');
    });

    expect(result.current.query).toBe('BTS');
  });

  it('searches and returns results', async () => {
    mockedFetch.mockResolvedValueOnce(mockSearchResult);

    const { result } = renderHook(() => useSearch());

    await act(async () => {
      await result.current.search('BTS');
    });

    await waitFor(() => {
      expect(result.current.state).toBe('success');
    });

    expect(result.current.results).toEqual(mockSearchResult);
    expect(mockedFetch).toHaveBeenCalledWith('BTS', expect.any(AbortSignal));
  });

  it('handles error', async () => {
    mockedFetch.mockRejectedValueOnce(new Error('Network error'));

    const { result } = renderHook(() => useSearch());

    await act(async () => {
      await result.current.search('BTS');
    });

    await waitFor(() => {
      expect(result.current.state).toBe('error');
    });

    expect(result.current.error).toBe('검색 중 오류가 발생했습니다');
  });

  it('sets loading state while searching', async () => {
    let resolvePromise: (value: unknown) => void;
    mockedFetch.mockImplementationOnce(
      () =>
        new Promise((resolve) => {
          resolvePromise = resolve;
        })
    );

    const { result } = renderHook(() => useSearch());

    act(() => {
      result.current.search('BTS');
    });

    expect(result.current.state).toBe('loading');

    await act(async () => {
      resolvePromise!(mockSearchResult);
    });

    await waitFor(() => {
      expect(result.current.state).toBe('success');
    });
  });
});
