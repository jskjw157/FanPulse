"use client";

import { useState, useEffect } from 'react';
import axios from 'axios';
import { fetchLiveDetail } from '@/lib/api/live';
import type { LiveDetail } from '@/types/live';
import type { AsyncState } from '@/types/common';

interface UseLiveDetailReturn {
  live: LiveDetail | null;
  state: AsyncState;
  error: string | null;
}

export function useLiveDetail(id: string): UseLiveDetailReturn {
  const [live, setLive] = useState<LiveDetail | null>(null);
  const [state, setState] = useState<AsyncState>('loading');
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const abortController = new AbortController();

    async function fetch() {
      setState('loading');
      setError(null);

      try {
        const data = await fetchLiveDetail(id, abortController.signal);
        if (abortController.signal.aborted) return;

        setLive(data);
        setState('success');
      } catch (err) {
        if (abortController.signal.aborted) return;

        const errorObj = err as { response?: { status?: number } };
        if (errorObj.response?.status === 404) {
          setError('라이브를 찾을 수 없습니다');
        } else {
          setError('데이터를 불러올 수 없습니다');
        }
        setState('error');
      }
    }

    fetch();

    return () => {
      abortController.abort();
    };
  }, [id]);

  return { live, state, error };
}
