'use client';

import { useState, useEffect, useCallback } from 'react';
import { fetchNewsDetail } from '@/lib/api/news';
import type { NewsDetail } from '@/types/news';
import type { AsyncState } from '@/types/common';

interface UseNewsDetailReturn {
  news: NewsDetail | null;
  state: AsyncState;
  error: string | null;
  retry: () => void;
}

export function useNewsDetail(id: string): UseNewsDetailReturn {
  const [news, setNews] = useState<NewsDetail | null>(null);
  const [state, setState] = useState<AsyncState>('loading');
  const [error, setError] = useState<string | null>(null);
  const [retryCount, setRetryCount] = useState(0);

  const fetchData = useCallback(async (signal: AbortSignal) => {
    setState('loading');
    setError(null);

    try {
      const data = await fetchNewsDetail(id, signal);
      if (signal.aborted) return;

      setNews(data);
      setState('success');
    } catch (err) {
      if (signal.aborted) return;

      const errorObj = err as { response?: { status?: number } };
      if (errorObj.response?.status === 404) {
        setError('뉴스를 찾을 수 없습니다');
      } else {
        setError('뉴스를 불러올 수 없습니다');
      }
      setState('error');
    }
  }, [id]);

  useEffect(() => {
    const abortController = new AbortController();
    fetchData(abortController.signal);

    return () => {
      abortController.abort();
    };
  }, [id, retryCount, fetchData]);

  const retry = useCallback(() => {
    setRetryCount((prev) => prev + 1);
  }, []);

  return { news, state, error, retry };
}
