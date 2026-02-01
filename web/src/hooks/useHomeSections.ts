"use client";

import { useState, useEffect, useCallback } from 'react';
import axios from 'axios';
import { fetchLiveNow, fetchUpcoming, fetchLatestNews } from '@/lib/api/home';
import type { Live } from '@/types/live';
import type { News } from '@/types/news';
import type { AsyncState } from '@/types/common';

function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    if (error.code === 'ECONNABORTED') return '요청 시간이 초과되었습니다';
    if (error.request && !error.response) return '네트워크 연결을 확인해주세요';
    if (error.response && error.response.status >= 500) return '서버에 문제가 발생했습니다';
  }
  return '데이터를 불러올 수 없습니다';
}

interface UseHomeSectionsReturn {
  liveNow: Live[];
  upcoming: Live[];
  latestNews: News[];
  state: AsyncState;
  error: string | null;
  refresh: () => Promise<void>;
}

export function useHomeSections(): UseHomeSectionsReturn {
  const [liveNow, setLiveNow] = useState<Live[]>([]);
  const [upcoming, setUpcoming] = useState<Live[]>([]);
  const [latestNews, setLatestNews] = useState<News[]>([]);
  const [state, setState] = useState<AsyncState>('loading');
  const [error, setError] = useState<string | null>(null);

  const fetchAll = useCallback(async (signal?: AbortSignal) => {
    setState('loading');
    setError(null);

    try {
      const [liveRes, upcomingRes, newsRes] = await Promise.all([
        fetchLiveNow(),
        fetchUpcoming(),
        fetchLatestNews(),
      ]);

      if (signal?.aborted) return;

      setLiveNow(liveRes.items);
      setUpcoming(upcomingRes.items);
      setLatestNews(newsRes.items);
      setState('success');
    } catch (err) {
      if (signal?.aborted) return;
      setError(getErrorMessage(err));
      setState('error');
    }
  }, []);

  useEffect(() => {
    const abortController = new AbortController();
    fetchAll(abortController.signal);

    return () => {
      abortController.abort();
    };
  }, [fetchAll]);

  return {
    liveNow,
    upcoming,
    latestNews,
    state,
    error,
    refresh: fetchAll,
  };
}
