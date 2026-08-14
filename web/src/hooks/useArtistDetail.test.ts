import { renderHook, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@/lib/api/artist', () => ({ fetchArtistDetail: vi.fn() }));

import { fetchArtistDetail } from '@/lib/api/artist';
import { useArtistDetail } from './useArtistDetail';

const mockedFetch = vi.mocked(fetchArtistDetail);
const artist = {
  id: '11111111-1111-1111-1111-111111111111',
  name: 'aespa',
  englishName: 'aespa',
  agency: 'SM Entertainment',
  description: 'Artist description',
  profileImageUrl: null,
  isGroup: true,
  members: ['Karina'],
  active: true,
  debutDate: '2020-11-17',
  createdAt: '2026-01-01T00:00:00Z',
};

describe('useArtistDetail', () => {
  beforeEach(() => vi.clearAllMocks());

  it('exposes loading and then the API artist', async () => {
    mockedFetch.mockResolvedValue(artist);
    const { result } = renderHook(() => useArtistDetail(artist.id));

    expect(result.current.state).toBe('loading');
    await waitFor(() => expect(result.current.state).toBe('success'));
    expect(result.current.artist).toEqual(artist);
  });

  it('exposes an explicit API error', async () => {
    mockedFetch.mockRejectedValue(new Error('network'));
    const { result } = renderHook(() => useArtistDetail(artist.id));

    await waitFor(() => expect(result.current.state).toBe('error'));
    expect(result.current.artist).toBeNull();
    expect(result.current.error).toBe('아티스트 정보를 불러올 수 없습니다');
  });
});
