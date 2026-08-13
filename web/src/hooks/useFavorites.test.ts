import { act, renderHook, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@/lib/api/social', () => ({ fetchFavorites: vi.fn(), removeFavorite: vi.fn() }));

import { fetchFavorites, removeFavorite } from '@/lib/api/social';
import { useFavorites } from './useFavorites';

const favorite = {
  id: '11111111-1111-1111-1111-111111111111', name: 'API Artist', englishName: null,
  agency: null, profileImageUrl: null, isGroup: true, followedAt: '2026-08-13T12:00:00',
};

describe('useFavorites', () => {
  beforeEach(() => vi.clearAllMocks());

  it('loads API favorites and removes only after API success', async () => {
    vi.mocked(fetchFavorites).mockResolvedValue([favorite]);
    vi.mocked(removeFavorite).mockResolvedValue();
    const { result } = renderHook(() => useFavorites());
    await waitFor(() => expect(result.current.state).toBe('success'));
    expect(result.current.favorites).toEqual([favorite]);

    await act(() => result.current.unfollow(favorite.id));
    expect(removeFavorite).toHaveBeenCalledWith(favorite.id);
    expect(result.current.favorites).toEqual([]);
  });

  it('shows an explicit load error without fallback artists', async () => {
    vi.mocked(fetchFavorites).mockRejectedValue(new Error('network'));
    const { result } = renderHook(() => useFavorites());
    await waitFor(() => expect(result.current.state).toBe('error'));
    expect(result.current.favorites).toEqual([]);
    expect(result.current.error).toBe('즐겨찾기를 불러올 수 없습니다');
  });
});
