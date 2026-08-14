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
  id: '11111111-1111-1111-1111-111111111111',
  chartType: 'APPLE_MUSIC',
  chartDate: '2026-08-10',
  createdAt: '2026-08-13T00:00:00Z',
  entries: [
    {
      id: '22222222-2222-2222-2222-222222222222',
      rank: 1,
      trackId: '33333333-3333-3333-3333-333333333333',
      artistId: '44444444-4444-4444-4444-444444444444',
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

    await expect(fetchLatestChart('APPLE_MUSIC')).resolves.toEqual(chartResponse);
    expect(mockedGet).toHaveBeenCalledWith('/charts/APPLE_MUSIC/latest', {
      signal: undefined,
    });
  });

  it.each([
    { success: false, data: chartResponse },
    { success: true, data: null },
  ])('rejects an unsuccessful or empty API envelope: %o', async (payload) => {
    mockedGet.mockResolvedValue({ data: payload });

    await expect(fetchLatestChart('APPLE_MUSIC')).rejects.toThrow(
      '차트 API 응답이 올바르지 않습니다.'
    );
  });

  it.each([
    { ...chartResponse, chartDate: '2026-02-30' },
    { ...chartResponse, entries: [{ ...chartResponse.entries[0], rank: '1' }] },
    { ...chartResponse, entries: [{ ...chartResponse.entries[0], artistId: 'not-a-uuid' }] },
  ])('rejects malformed successful chart payload: %o', async (data) => {
    mockedGet.mockResolvedValue({ data: { success: true, data } });

    await expect(fetchLatestChart('APPLE_MUSIC')).rejects.toThrow(
      '차트 API 응답이 올바르지 않습니다.'
    );
  });
});
