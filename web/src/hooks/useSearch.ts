'use client';

import { useState, useCallback, useRef } from 'react';
import { fetchSearchResults } from '@/lib/api/search';
import type { SearchResult } from '@/types/search';
import type { AsyncState } from '@/types/common';

interface UseSearchReturn {
  query: string;
  setQuery: (q: string) => void;
  results: SearchResult | null;
  state: AsyncState | 'idle';
  error: string | null;
  search: (q: string) => Promise<void>;
}

export function useSearch(): UseSearchReturn {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<SearchResult | null>(null);
  const [state, setState] = useState<AsyncState | 'idle'>('idle');
  const [error, setError] = useState<string | null>(null);
  const abortControllerRef = useRef<AbortController | null>(null);

  const search = useCallback(async (q: string) => {
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }

    abortControllerRef.current = new AbortController();
    const { signal } = abortControllerRef.current;

    setState('loading');
    setError(null);

    try {
      const data = await fetchSearchResults(q, signal);
      if (signal.aborted) return;

      setResults(data);
      setState('success');
    } catch (err) {
      if (signal.aborted) return;

      setError('검색 중 오류가 발생했습니다');
      setState('error');
    }
  }, []);

  return {
    query,
    setQuery,
    results,
    state,
    error,
    search,
  };
}
