import { renderHook, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@/lib/api/chart', () => ({
  fetchLatestChart: vi.fn(),
}));

import { fetchLatestChart } from '@/lib/api/chart';
import { useLatestChart } from './useLatestChart';

const mockedFetchLatestChart = vi.mocked(fetchLatestChart);

const chart = {
  id: 'chart-1',
  chartType: 'MELON' as const,
  chartDate: '2026-08-13',
  createdAt: '2026-08-13T00:00:00Z',
  entries: [],
};

describe('useLatestChart', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('loads the selected chart type', async () => {
    mockedFetchLatestChart.mockResolvedValue(chart);

    const { result } = renderHook(() => useLatestChart('MELON'));

    expect(result.current.state).toBe('loading');

    await waitFor(() => {
      expect(result.current.state).toBe('success');
    });

    expect(result.current.chart).toEqual(chart);
    expect(mockedFetchLatestChart).toHaveBeenCalledWith('MELON', expect.any(AbortSignal));
  });
});
