'use client';

import { useState, useEffect, useCallback } from 'react';
import {
  getRecentSearches,
  addRecentSearch as addToStorage,
  removeRecentSearch as removeFromStorage,
  clearRecentSearches as clearStorage,
} from '@/lib/storage/recentSearches';

interface UseRecentSearchesReturn {
  searches: string[];
  addSearch: (query: string) => void;
  removeSearch: (query: string) => void;
  clearAll: () => void;
}

export function useRecentSearches(): UseRecentSearchesReturn {
  const [searches, setSearches] = useState<string[]>([]);

  useEffect(() => {
    setSearches(getRecentSearches());
  }, []);

  const addSearch = useCallback((query: string) => {
    addToStorage(query);
    setSearches(getRecentSearches());
  }, []);

  const removeSearch = useCallback((query: string) => {
    removeFromStorage(query);
    setSearches(getRecentSearches());
  }, []);

  const clearAll = useCallback(() => {
    clearStorage();
    setSearches([]);
  }, []);

  return {
    searches,
    addSearch,
    removeSearch,
    clearAll,
  };
}
