"use client";

import { useState, useEffect, useCallback, useRef } from 'react';
import axios from 'axios';
import { searchAll } from '@/lib/api/search';
import type { Live } from '@/types/live';
import type { News } from '@/types/news';
import type { AsyncState } from '@/types/common';

const DEBOUNCE_MS = 300;

function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    if (error.code === 'ECONNABORTED') return '요청 시간이 초과되었습니다';
    if (error.request && !error.response) return '네트워크 연결을 확인해주세요';
    if (error.response && error.response.status >= 500) return '서버에 문제가 발생했습니다';
  }
  return '검색 결과를 불러올 수 없습니다';
}


interface UseSearchReturn {
  query: string;
  setQuery: (q: string) => void;
  lives: Live[];
  news: News[];
  state: AsyncState;
  error: string | null;
}

export function useSearch(): UseSearchReturn {
  const [query, setQuery] = useState('');
  const [lives, setLives] = useState<Live[]>([]);
  const [news, setNews] = useState<News[]>([]);
  const [state, setState] = useState<AsyncState>('idle');
  const [error, setError] = useState<string | null>(null);

  const abortRef = useRef<AbortController | null>(null);
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const executeSearch = useCallback(async (q: string) => {
    abortRef.current?.abort();
    const controller = new AbortController();
    abortRef.current = controller;

    setState('loading');
    setError(null);

    try {
      const result = await searchAll(q, 20, controller.signal);

      if (controller.signal.aborted) return;

      setLives(result.live?.items ?? []);
      setNews(result.news?.items ?? []);
      setState('success');
    } catch (err) {
      if (axios.isCancel(err) || (err instanceof DOMException && err.name === 'AbortError')) {
        return;
      }
      setError(getErrorMessage(err));
      setState('error');
    }
  }, []);

  useEffect(() => {
    if (timerRef.current) {
      clearTimeout(timerRef.current);
    }

    const trimmed = query.trim();

    if (!trimmed) {
      abortRef.current?.abort();
      timerRef.current = setTimeout(() => {
        setLives([]);
        setNews([]);
        setState('idle');
        setError(null);
      }, 0);
      return;
    }

    timerRef.current = setTimeout(() => {
      executeSearch(trimmed);
    }, DEBOUNCE_MS);

    return () => {
      if (timerRef.current) {
        clearTimeout(timerRef.current);
      }
    };
  }, [query, executeSearch]);

  useEffect(() => {
    return () => {
      abortRef.current?.abort();
    };
  }, []);

  return { query, setQuery, lives, news, state, error };
}
