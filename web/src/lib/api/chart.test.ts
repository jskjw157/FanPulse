import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@/lib/api-client', () => ({
  apiClient: {
    get: vi.fn(),
  },
}));

import { apiClient } from '@/lib/api-client';
import { fetchLatestChart } from './chart';

const mockedGet = vi.mocked(apiClient.get);

const chartResponse = {
  id: 'chart-1',
  chartType: 'MELON',
  chartDate: '2026-08-13',
  createdAt: '2026-08-13T00:00:00Z',
  entries: [
    {
      id: 'entry-1',
      rank: 1,
      trackId: 'track-1',
      artistId: 'artist-1',
      trackTitle: 'API Track',
      artistName: 'API Artist',
      previousRank: 2,
      peakRank: 1,
      weeksOnChart: 4,
      rankChange: 1,
      isNew: false,
    },
  ],
};

describe('fetchLatestChart', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('requests the latest chart through the common client and unwraps ApiResponse data', async () => {
    mockedGet.mockResolvedValue({ data: { success: true, data: chartResponse } });

    await expect(fetchLatestChart('MELON')).resolves.toEqual(chartResponse);
    expect(mockedGet).toHaveBeenCalledWith('/charts/MELON/latest', {
      signal: undefined,
    });
  });
});
