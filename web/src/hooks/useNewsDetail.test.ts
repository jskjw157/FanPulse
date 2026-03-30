import { renderHook, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { useNewsDetail } from './useNewsDetail';

vi.mock('@/lib/api/news', () => ({
  fetchNewsDetail: vi.fn(),
}));

import { fetchNewsDetail } from '@/lib/api/news';
import { mockNewsDetail } from '@/__mocks__/news';

const mockedFetchNewsDetail = vi.mocked(fetchNewsDetail);

describe('useNewsDetail', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('fetches news detail by id', async () => {
    mockedFetchNewsDetail.mockResolvedValue(mockNewsDetail);

    const { result } = renderHook(() => useNewsDetail('1'));

    await waitFor(() => {
      expect(result.current.state).toBe('success');
    });

    expect(result.current.news).toEqual(mockNewsDetail);
    expect(mockedFetchNewsDetail).toHaveBeenCalledWith('1', expect.any(AbortSignal));
  });

  it('sets error state on 404', async () => {
    mockedFetchNewsDetail.mockRejectedValue({
      response: { status: 404 },
    });

    const { result } = renderHook(() => useNewsDetail('999'));

    await waitFor(() => {
      expect(result.current.state).toBe('error');
    });

    expect(result.current.error).toBe('뉴스를 찾을 수 없습니다');
  });

  it('sets generic error message on other errors', async () => {
    mockedFetchNewsDetail.mockRejectedValue(new Error('Network error'));

    const { result } = renderHook(() => useNewsDetail('1'));

    await waitFor(() => {
      expect(result.current.state).toBe('error');
    });

    expect(result.current.error).toBe('뉴스를 불러올 수 없습니다');
  });

  it('provides retry function', async () => {
    mockedFetchNewsDetail.mockRejectedValueOnce(new Error('Network error'));
    mockedFetchNewsDetail.mockResolvedValueOnce(mockNewsDetail);

    const { result } = renderHook(() => useNewsDetail('1'));

    await waitFor(() => {
      expect(result.current.state).toBe('error');
    });

    result.current.retry();

    await waitFor(() => {
      expect(result.current.state).toBe('success');
    });

    expect(result.current.news).toEqual(mockNewsDetail);
  });
});
