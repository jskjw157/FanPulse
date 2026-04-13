"use client";

import { useState, useCallback } from "react";
import { useTranslation } from "react-i18next";
import Link from "next/link";
import PageWrapper from "@/components/layout/PageWrapper";
import LiveCard from "@/app/components/home/LiveCard";
import NewsCard from "@/app/components/home/NewsCard";
import SkeletonCard from "@/components/ui/SkeletonCard";
import { useSearch } from "@/hooks/useSearch";

const RECENT_SEARCHES_KEY = "fanpulse_recent_searches";
const MAX_RECENT_SEARCHES = 10;

function loadRecentSearches(): string[] {
  if (typeof window === "undefined") return [];
  try {
    const raw = localStorage.getItem(RECENT_SEARCHES_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}

function saveRecentSearches(searches: string[]) {
  try {
    localStorage.setItem(RECENT_SEARCHES_KEY, JSON.stringify(searches));
  } catch {
    // localStorage 용량 초과 등 무시
  }
}

function addRecentSearch(term: string, current: string[]): string[] {
  const filtered = current.filter((s) => s !== term);
  const updated = [term, ...filtered].slice(0, MAX_RECENT_SEARCHES);
  saveRecentSearches(updated);
  return updated;
}

export default function SearchPage() {
  const { t } = useTranslation();
  const { query, setQuery, lives, news, state, error } = useSearch();
  const [recentSearches, setRecentSearches] = useState<string[]>(loadRecentSearches);

  const handleSearch = useCallback(
    (term: string) => {
      setQuery(term);
      if (term.trim()) {
        setRecentSearches((prev) => addRecentSearch(term.trim(), prev));
      }
    },
    [setQuery]
  );

  const handleInputChange = useCallback(
    (value: string) => {
      setQuery(value);
    },
    [setQuery]
  );

  const handleInputKeyDown = useCallback(
    (e: React.KeyboardEvent<HTMLInputElement>) => {
      if (e.key === "Enter" && query.trim()) {
        setRecentSearches((prev) => addRecentSearch(query.trim(), prev));
      }
    },
    [query]
  );

  const handleClearInput = useCallback(() => {
    setQuery("");
  }, [setQuery]);

  const handleClearRecentSearches = useCallback(() => {
    setRecentSearches([]);
    saveRecentSearches([]);
  }, []);

  const handleRemoveRecentSearch = useCallback((term: string) => {
    setRecentSearches((prev) => {
      const updated = prev.filter((s) => s !== term);
      saveRecentSearches(updated);
      return updated;
    });
  }, []);

  const hasQuery = query.trim().length > 0;
  const hasResults = lives.length > 0 || news.length > 0;

  return (
    <>
      {/* 검색 헤더 */}
      <header className="fixed top-0 left-0 right-0 bg-white border-b border-gray-200 z-50 lg:static lg:z-auto lg:border-none lg:bg-transparent lg:pt-8 lg:pb-4">
        <div className="px-4 py-3 lg:px-0 lg:max-w-4xl lg:mx-auto">
          <div className="flex items-center gap-2">
            <Link
              href="/"
              className="w-9 h-9 flex items-center justify-center lg:hidden"
            >
              <i className="ri-arrow-left-line text-xl text-gray-900" />
            </Link>
            <div className="flex-1 relative">
              <input
                type="text"
                value={query}
                onChange={(e) => handleInputChange(e.target.value)}
                onKeyDown={handleInputKeyDown}
                placeholder={t("search.placeholder")}
                className="w-full bg-gray-100 rounded-full pl-10 pr-10 py-2.5 text-sm border-none focus:outline-none focus:ring-2 focus:ring-purple-600 lg:py-3 lg:text-base"
              />
              <i className="ri-search-line absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 lg:left-4" />
              {query && (
                <button
                  onClick={handleClearInput}
                  className="absolute right-3 top-1/2 -translate-y-1/2"
                  aria-label="Clear search"
                >
                  <i className="ri-close-circle-fill text-gray-400" />
                </button>
              )}
            </div>
          </div>
        </div>
      </header>

      <PageWrapper className="pt-16">
        {/* 검색어 없을 때: 최근 검색어 */}
        {!hasQuery && (
          <RecentSearchesSection
            searches={recentSearches}
            onSelect={handleSearch}
            onRemove={handleRemoveRecentSearch}
            onClearAll={handleClearRecentSearches}
          />
        )}

        {/* 로딩 상태 */}
        {hasQuery && state === "loading" && <SearchSkeleton />}

        {/* 에러 상태 */}
        {hasQuery && state === "error" && (
          <div className="text-center py-16 px-4">
            <div className="w-16 h-16 mx-auto mb-4 bg-red-50 rounded-full flex items-center justify-center">
              <i className="ri-error-warning-line text-2xl text-red-400" />
            </div>
            <p className="text-gray-500 mb-4">{error}</p>
            <button
              onClick={() => handleSearch(query)}
              className="px-6 py-2 bg-purple-600 text-white rounded-full text-sm font-medium hover:bg-purple-700 transition-colors"
            >
              {t("search.errorRetry")}
            </button>
          </div>
        )}

        {/* 결과 없음 */}
        {hasQuery && state === "success" && !hasResults && (
          <div className="text-center py-16 px-4">
            <div className="w-16 h-16 mx-auto mb-4 bg-gray-100 rounded-full flex items-center justify-center">
              <i className="ri-search-line text-2xl text-gray-400" />
            </div>
            <p className="text-gray-500">
              {t("search.noResults", { query: query.trim() })}
            </p>
            <p className="text-sm text-gray-400 mt-1">
              {t("search.noResultsHint")}
            </p>
          </div>
        )}

        {/* 검색 결과 */}
        {hasQuery && state === "success" && hasResults && (
          <div className="space-y-6 pb-4">
            {/* Live 결과 */}
            {lives.length > 0 && (
              <section aria-label={t("search.liveResults")}>
                <div className="flex items-center justify-between px-4 py-3">
                  <h2 className="text-lg font-bold text-gray-900">
                    {t("search.liveResults")}
                  </h2>
                </div>
                <div className="flex overflow-x-auto gap-4 px-4 pb-2 scrollbar-hide">
                  {lives.map((live) => (
                    <LiveCard key={live.id} live={live} />
                  ))}
                </div>
              </section>
            )}

            {/* News 결과 */}
            {news.length > 0 && (
              <section aria-label={t("search.newsResults")}>
                <div className="flex items-center justify-between px-4 py-3">
                  <h2 className="text-lg font-bold text-gray-900">
                    {t("search.newsResults")}
                  </h2>
                </div>
                <div className="space-y-1 px-4">
                  {news.map((item) => (
                    <NewsCard key={item.id} news={item} />
                  ))}
                </div>
              </section>
            )}
          </div>
        )}
      </PageWrapper>
    </>
  );
}

/* ─── 최근 검색어 ─── */

interface RecentSearchesSectionProps {
  searches: string[];
  onSelect: (term: string) => void;
  onRemove: (term: string) => void;
  onClearAll: () => void;
}

function RecentSearchesSection({
  searches,
  onSelect,
  onRemove,
  onClearAll,
}: RecentSearchesSectionProps) {
  const { t } = useTranslation();

  if (searches.length === 0) {
    return null;
  }

  return (
    <div className="px-4 py-4">
      <div className="flex items-center justify-between mb-3">
        <h2 className="text-sm font-bold text-gray-900">
          {t("search.recentSearches")}
        </h2>
        <button
          onClick={onClearAll}
          className="text-xs text-gray-500 hover:text-purple-600 transition-colors"
        >
          {t("search.clearAll")}
        </button>
      </div>
      <div className="flex flex-wrap gap-2">
        {searches.map((term) => (
          <div
            key={term}
            className="flex items-center gap-1.5 bg-gray-100 rounded-full text-sm text-gray-700 hover:bg-gray-200 transition-colors"
          >
            <button
              onClick={() => onSelect(term)}
              className="flex items-center gap-2 pl-3 py-2"
            >
              <i className="ri-time-line text-gray-400" />
              {term}
            </button>
            <button
              onClick={() => onRemove(term)}
              className="pr-3 py-2"
              aria-label={t("search.removeRecent")}
            >
              <i className="ri-close-line text-gray-400 hover:text-gray-600" />
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}

/* ─── 로딩 스켈레톤 ─── */

function SearchSkeleton() {
  return (
    <div className="space-y-6 px-4 py-4">
      {/* Live 스켈레톤 */}
      <div>
        <div className="h-5 w-20 bg-gray-200 rounded animate-pulse mb-3" />
        <div className="flex overflow-x-auto gap-4 scrollbar-hide">
          {Array.from({ length: 3 }).map((_, i) => (
            <SkeletonCard key={`live-${i}`} />
          ))}
        </div>
      </div>

      {/* News 스켈레톤 */}
      <div>
        <div className="h-5 w-16 bg-gray-200 rounded animate-pulse mb-3" />
        <div className="space-y-2">
          {Array.from({ length: 4 }).map((_, i) => (
            <SkeletonCard key={`news-${i}`} layout="horizontal" />
          ))}
        </div>
      </div>
    </div>
  );
}
