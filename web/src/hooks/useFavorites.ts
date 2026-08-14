'use client';

import { useCallback, useEffect, useState } from 'react';
import type { AsyncState } from '@/types/common';
import { fetchFavorites, removeFavorite, type FavoriteArtist } from '@/lib/api/social';

interface FavoritesSnapshot {
  userId: string | undefined;
  favorites: FavoriteArtist[];
  state: AsyncState;
  error: string | null;
}

interface FavoriteMutationState {
  userId: string | undefined;
  mutatingId: string | null;
  error: string | null;
}

export function useFavorites(userId: string | undefined) {
  const [snapshot, setSnapshot] = useState<FavoritesSnapshot>({
    userId,
    favorites: [],
    state: 'loading',
    error: null,
  });
  const [mutation, setMutation] = useState<FavoriteMutationState>({
    userId,
    mutatingId: null,
    error: null,
  });

  const load = useCallback(async (signal?: AbortSignal) => {
    if (!userId) {
      setSnapshot({
        userId,
        favorites: [],
        state: 'error',
        error: '인증된 사용자 정보를 확인할 수 없습니다',
      });
      return;
    }

    setSnapshot({ userId, favorites: [], state: 'loading', error: null });
    setMutation({ userId, mutatingId: null, error: null });
    try {
      const rows = await fetchFavorites(signal);
      if (signal?.aborted) return;
      setSnapshot({ userId, favorites: rows, state: 'success', error: null });
    } catch {
      if (signal?.aborted) return;
      setSnapshot({
        userId,
        favorites: [],
        state: 'error',
        error: '즐겨찾기를 불러올 수 없습니다',
      });
    }
  }, [userId]);

  useEffect(() => {
    const controller = new AbortController();
    void load(controller.signal);
    return () => controller.abort();
  }, [load]);

  const unfollow = useCallback(async (artistId: string) => {
    setMutation({ userId, mutatingId: artistId, error: null });
    try {
      await removeFavorite(artistId);
      setSnapshot((current) => current.userId === userId
        ? { ...current, favorites: current.favorites.filter((artist) => artist.id !== artistId) }
        : current);
      setMutation({ userId, mutatingId: null, error: null });
    } catch {
      setMutation({ userId, mutatingId: null, error: '좋아요 취소에 실패했습니다' });
    }
  }, [userId]);

  const isCurrentSnapshot = snapshot.userId === userId;
  const isCurrentMutation = mutation.userId === userId;

  return {
    favorites: isCurrentSnapshot ? snapshot.favorites : [],
    state: isCurrentSnapshot ? snapshot.state : 'loading' as AsyncState,
    error: isCurrentSnapshot ? snapshot.error : null,
    retry: load,
    unfollow,
    mutatingId: isCurrentMutation ? mutation.mutatingId : null,
    mutationError: isCurrentMutation ? mutation.error : null,
  };
}
