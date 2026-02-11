'use client';

import { useCallback } from 'react';
import Link from 'next/link';
import { SearchBar } from './components/SearchBar';
import { RecentSearches } from './components/RecentSearches';
import { LiveResults } from './components/LiveResults';
import { NewsResults } from './components/NewsResults';
import { EmptyState } from './components/EmptyState';
import { useSearch } from '@/hooks/useSearch';
import { useRecentSearches } from '@/hooks/useRecentSearches';
import PageWrapper from '@/components/layout/PageWrapper';

export default function SearchPage() {
  const { query, setQuery, results, state, error, search } = useSearch();
  const { searches, addSearch, removeSearch, clearAll } = useRecentSearches();

  const handleSearch = useCallback(
    async (q: string) => {
      await search(q);
      addSearch(q);
    },
    [search, addSearch]
  );

  const handleSelectRecent = useCallback(
    (q: string) => {
      setQuery(q);
      handleSearch(q);
    },
    [setQuery, handleSearch]
  );

  const handleClear = useCallback(() => {
    setQuery('');
  }, [setQuery]);

  const hasResults =
    results && (results.live.length > 0 || results.news.length > 0);
  const showEmptyState = state === 'success' && results && !hasResults;
  const showRecentSearches = state === 'idle' && !results;

  return (
    <div className="min-h-screen bg-white">
      {/* Header */}
      <header className="sticky top-0 bg-white border-b z-50">
        <div className="px-4 py-3 flex items-center gap-2">
          <Link
            href="/"
            className="lg:hidden p-2 -ml-2 text-gray-600 hover:text-gray-900"
            aria-label="뒤로"
          >
            <i className="ri-arrow-left-line text-xl" />
          </Link>
          <div className="flex-1">
            <SearchBar
              value={query}
              onChange={setQuery}
              onSearch={handleSearch}
              onClear={handleClear}
              autoFocus
            />
          </div>
        </div>
      </header>

      <PageWrapper>
        {/* Loading */}
        {state === 'loading' && (
          <div className="flex justify-center py-16">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-pink-500" />
          </div>
        )}

        {/* Error */}
        {state === 'error' && (
          <div className="text-center py-16">
            <i className="ri-error-warning-line text-4xl text-red-400 mb-4 block" />
            <p className="text-gray-600">{error || '오류가 발생했습니다'}</p>
            <button
              type="button"
              onClick={() => handleSearch(query)}
              className="mt-4 px-4 py-2 bg-pink-500 text-white rounded-full text-sm hover:bg-pink-600"
            >
              다시 시도
            </button>
          </div>
        )}

        {/* Recent Searches */}
        {showRecentSearches && (
          <RecentSearches
            searches={searches}
            onSelect={handleSelectRecent}
            onRemove={removeSearch}
            onClearAll={clearAll}
          />
        )}

        {/* Empty State */}
        {showEmptyState && <EmptyState />}

        {/* Results */}
        {hasResults && (
          <>
            <LiveResults lives={results.live} />
            <NewsResults newsList={results.news} />
          </>
        )}
      </PageWrapper>
    </div>
  );
}
