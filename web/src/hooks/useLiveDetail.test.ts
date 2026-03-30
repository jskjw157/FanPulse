import { renderHook, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { useLiveDetail } from './useLiveDetail';

vi.mock('@/lib/api/live', () => ({
  fetchLiveDetail: vi.fn(),
}));

import { fetchLiveDetail } from '@/lib/api/live';
import { mockLiveDetail } from '@/__mocks__/live';

const mockedFetchLiveDetail = vi.mocked(fetchLiveDetail);

describe('useLiveDetail', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('fetches live detail by id', async () => {
    mockedFetchLiveDetail.mockResolvedValue(mockLiveDetail);

    const { result } = renderHook(() => useLiveDetail('1'));

    await waitFor(() => {
      expect(result.current.state).toBe('success');
    });

    expect(result.current.live).toEqual(mockLiveDetail);
    expect(mockedFetchLiveDetail).toHaveBeenCalledWith('1', expect.any(AbortSignal));
  });

  it('sets error state on 404', async () => {
    mockedFetchLiveDetail.mockRejectedValue({
      response: { status: 404 },
    });

    const { result } = renderHook(() => useLiveDetail('999'));

    await waitFor(() => {
      expect(result.current.state).toBe('error');
    });

    expect(result.current.error).toBe('라이브를 찾을 수 없습니다');
  });

  it('sets generic error message on other errors', async () => {
    mockedFetchLiveDetail.mockRejectedValue(new Error('Network error'));

    const { result } = renderHook(() => useLiveDetail('1'));

    await waitFor(() => {
      expect(result.current.state).toBe('error');
    });

    expect(result.current.error).toBe('데이터를 불러올 수 없습니다');
  });
});
