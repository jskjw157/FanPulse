"use client";

import { useState, useEffect, useCallback, useRef } from 'react';
import axios from 'axios';
import { fetchNewsList } from '@/lib/api/news';
import type { News } from '@/types/news';
import type { AsyncState } from '@/types/common';

function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    if (error.code === 'ECONNABORTED') return '요청 시간이 초과되었습니다';
    if (error.request && !error.response) return '네트워크 연결을 확인해주세요';
    if (error.response && error.response.status >= 500) return '서버에 문제가 발생했습니다';
  }
  return '뉴스를 불러올 수 없습니다';
}

interface UseNewsListOptions {
  limit?: number;
}

interface UseNewsListReturn {
  items: News[];
  state: AsyncState;
  error: string | null;
  hasMore: boolean;
  loadMore: () => Promise<void>;
  refresh: () => Promise<void>;
}

export function useNewsList(
  options: UseNewsListOptions = {}
): UseNewsListReturn {
  const { limit = 20 } = options;

  const [items, setItems] = useState<News[]>([]);
  const [state, setState] = useState<AsyncState>('loading');
  const [error, setError] = useState<string | null>(null);
  const [hasMore, setHasMore] = useState(true);
  const offsetRef = useRef(0);
  const abortControllerRef = useRef<AbortController | null>(null);

  const fetchData = useCallback(
    async (isRefresh = false) => {
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }
      abortControllerRef.current = new AbortController();

      if (isRefresh) {
        setState('loading');
        offsetRef.current = 0;
      }
      setError(null);

      try {
        const offset = isRefresh ? 0 : offsetRef.current;
        const result = await fetchNewsList(limit, offset, abortControllerRef.current.signal);

        if (abortControllerRef.current.signal.aborted) return;

        if (isRefresh) {
          setItems(result);
        } else {
          setItems((prev) => [...prev, ...result]);
        }

        offsetRef.current = (isRefresh ? 0 : offsetRef.current) + result.length;
        setHasMore(result.length >= limit);
        setState('success');
      } catch (err) {
        if (axios.isCancel(err)) return;
        if (abortControllerRef.current?.signal.aborted) return;

        setError(getErrorMessage(err));
        setState('error');
      }
    },
    [limit]
  );

  useEffect(() => {
    fetchData(true);

    return () => {
      abortControllerRef.current?.abort();
    };
  }, [fetchData]);

  const loadMore = useCallback(async () => {
    if (state === 'loading' || !hasMore) return;
    await fetchData(false);
  }, [fetchData, state, hasMore]);

  const refresh = useCallback(async () => {
    await fetchData(true);
  }, [fetchData]);

  return {
    items,
    state,
    error,
    hasMore,
    loadMore,
    refresh,
  };
}
