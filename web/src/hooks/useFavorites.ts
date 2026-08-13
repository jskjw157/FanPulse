'use client';

import { useCallback, useEffect, useState } from 'react';
import type { AsyncState } from '@/types/common';
import { fetchFavorites, removeFavorite, type FavoriteArtist } from '@/lib/api/social';

export function useFavorites() {
  const [favorites, setFavorites] = useState<FavoriteArtist[]>([]);
  const [state, setState] = useState<AsyncState>('loading');
  const [error, setError] = useState<string | null>(null);
  const [mutatingId, setMutatingId] = useState<string | null>(null);

  const load = useCallback(async (signal?: AbortSignal) => {
    setState('loading');
    setError(null);
    try {
      const rows = await fetchFavorites(signal);
      if (signal?.aborted) return;
      setFavorites(rows);
      setState('success');
    } catch {
      if (signal?.aborted) return;
      setFavorites([]);
      setError('즐겨찾기를 불러올 수 없습니다');
      setState('error');
    }
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    void load(controller.signal);
    return () => controller.abort();
  }, [load]);

  const unfollow = useCallback(async (artistId: string) => {
    setMutatingId(artistId);
    try {
      await removeFavorite(artistId);
      setFavorites((current) => current.filter((artist) => artist.id !== artistId));
    } finally {
      setMutatingId(null);
    }
  }, []);

  return { favorites, state, error, retry: load, unfollow, mutatingId };
}
