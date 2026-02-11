"use client";

import { useState, useEffect, useCallback, useRef } from 'react';
import axios from 'axios';
import { fetchLiveList, type FetchLiveListParams } from '@/lib/api/live';
import type { Live } from '@/types/live';
import type { AsyncState } from '@/types/common';

function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    if (error.code === 'ECONNABORTED') return '요청 시간이 초과되었습니다';
    if (error.request && !error.response) return '네트워크 연결을 확인해주세요';
    if (error.response && error.response.status >= 500) return '서버에 문제가 발생했습니다';
  }
  return '데이터를 불러올 수 없습니다';
}

interface UseInfiniteLiveListOptions {
  status?: 'LIVE' | 'SCHEDULED' | 'ENDED';
  limit?: number;
}

interface UseInfiniteLiveListReturn {
  items: Live[];
  state: AsyncState;
  error: string | null;
  hasMore: boolean;
  loadMore: () => Promise<void>;
  refresh: () => Promise<void>;
}

export function useInfiniteLiveList(
  options: UseInfiniteLiveListOptions = {}
): UseInfiniteLiveListReturn {
  const { status, limit = 20 } = options;

  const [items, setItems] = useState<Live[]>([]);
  const [state, setState] = useState<AsyncState>('loading');
  const [error, setError] = useState<string | null>(null);
  const [hasMore, setHasMore] = useState(true);
  const cursorRef = useRef<string | undefined>(undefined);
  const abortControllerRef = useRef<AbortController | null>(null);

  const fetchData = useCallback(
    async (isRefresh = false) => {
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }
      abortControllerRef.current = new AbortController();

      if (isRefresh) {
        setState('loading');
        cursorRef.current = undefined;
      }
      setError(null);

      try {
        const params: FetchLiveListParams = {
          status,
          limit,
          cursor: isRefresh ? undefined : cursorRef.current,
          signal: abortControllerRef.current.signal,
        };

        const response = await fetchLiveList(params);

        if (abortControllerRef.current.signal.aborted) return;

        if (isRefresh) {
          setItems(response.items);
        } else {
          setItems((prev) => [...prev, ...response.items]);
        }

        cursorRef.current = response.nextCursor;
        setHasMore(response.hasMore);
        setState('success');
      } catch (err) {
        if (axios.isCancel(err)) return;
        if (abortControllerRef.current?.signal.aborted) return;

        setError(getErrorMessage(err));
        setState('error');
      }
    },
    [status, limit]
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
