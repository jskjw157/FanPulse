import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@/lib/api-client', () => ({
  apiClient: { get: vi.fn() },
}));

import { apiClient } from '@/lib/api-client';
import { fetchArtistDetail } from './artist';

const mockedGet = vi.mocked(apiClient.get);

const artist = {
  id: '11111111-1111-1111-1111-111111111111',
  name: 'aespa',
  englishName: 'aespa',
  agency: 'SM Entertainment',
  description: 'Artist description',
  profileImageUrl: 'https://example.com/aespa.jpg',
  isGroup: true,
  members: ['Karina', 'Winter'],
  active: true,
  debutDate: '2020-11-17',
  createdAt: '2026-01-01T00:00:00Z',
};

describe('fetchArtistDetail', () => {
  beforeEach(() => vi.clearAllMocks());

  it('loads an artist from the Spring artist detail endpoint', async () => {
    mockedGet.mockResolvedValue({ data: { success: true, data: artist } });

    await expect(fetchArtistDetail(artist.id)).resolves.toEqual(artist);
    expect(mockedGet).toHaveBeenCalledWith(`/artists/${artist.id}`, { signal: undefined });
  });
});
