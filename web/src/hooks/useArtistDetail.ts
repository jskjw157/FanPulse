'use client';

import { useCallback, useEffect, useState } from 'react';
import { fetchArtistDetail, type ArtistDetail } from '@/lib/api/artist';
import type { AsyncState } from '@/types/common';

interface UseArtistDetailReturn {
  artist: ArtistDetail | null;
  state: AsyncState;
  error: string | null;
  retry: () => void;
}

export function useArtistDetail(id: string): UseArtistDetailReturn {
  const [artist, setArtist] = useState<ArtistDetail | null>(null);
  const [state, setState] = useState<AsyncState>('loading');
  const [error, setError] = useState<string | null>(null);
  const [retryCount, setRetryCount] = useState(0);

  useEffect(() => {
    const controller = new AbortController();

    async function loadArtist() {
      setState('loading');
      setError(null);
      try {
        const result = await fetchArtistDetail(id, controller.signal);
        if (controller.signal.aborted) return;
        setArtist(result);
        setState('success');
      } catch {
        if (controller.signal.aborted) return;
        setArtist(null);
        setError('아티스트 정보를 불러올 수 없습니다');
        setState('error');
      }
    }

    loadArtist();
    return () => controller.abort();
  }, [id, retryCount]);

  const retry = useCallback(() => setRetryCount((count) => count + 1), []);
  return { artist, state, error, retry };
}
