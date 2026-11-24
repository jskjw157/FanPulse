'use client';

import { useCallback, useEffect, useRef, useState } from 'react';
import type { AsyncState } from '@/types/common';
import { addFavorite, fetchFavorites, removeFavorite, type FavoriteArtist } from '@/lib/api/social';

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
  const pending = useRef<{ userId: string; artistId: string } | null>(null);

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
    try {
      const rows = await fetchFavorites(signal, userId);
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

  const mutate = useCallback(async (artistId: string, following: boolean) => {
    if (!userId || snapshot.userId !== userId || snapshot.state !== 'success' || pending.current?.userId === userId) return;
    const operation = { userId, artistId };
    pending.current = operation;
    setMutation({ userId, mutatingId: artistId, error: null });
    try {
      let favorite: FavoriteArtist | null = null;
      if (following) favorite = await addFavorite(artistId, userId);
      else await removeFavorite(artistId, userId);
      setSnapshot((current) => current.userId === userId
        ? {
          ...current,
          favorites: favorite
            ? [favorite, ...current.favorites.filter((artist) => artist.id !== artistId)]
            : current.favorites.filter((artist) => artist.id !== artistId),
        }
        : current);
      if (pending.current === operation) setMutation({ userId, mutatingId: null, error: null });
    } catch {
      if (pending.current === operation) {
        setMutation({ userId, mutatingId: null, error: following ? '팔로우에 실패했습니다' : '좋아요 취소에 실패했습니다' });
      }
    } finally {
      if (pending.current === operation) pending.current = null;
    }
  }, [userId, snapshot.userId, snapshot.state]);

  const follow = useCallback((artistId: string) => mutate(artistId, true), [mutate]);
  const unfollow = useCallback((artistId: string) => mutate(artistId, false), [mutate]);

  const isCurrentSnapshot = snapshot.userId === userId;
  const isCurrentMutation = mutation.userId === userId;

  return {
    favorites: isCurrentSnapshot ? snapshot.favorites : [],
    state: isCurrentSnapshot ? snapshot.state : 'loading' as AsyncState,
    error: isCurrentSnapshot ? snapshot.error : null,
    retry: load,
    follow,
    unfollow,
    mutatingId: isCurrentMutation ? mutation.mutatingId : null,
    mutationError: isCurrentMutation ? mutation.error : null,
  };
}
