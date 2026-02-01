"use client";

import NewsCard from './NewsCard';
import SkeletonCard from '@/components/ui/SkeletonCard';
import type { News } from '@/types/news';
import type { AsyncState } from '@/types/common';

interface LatestNewsSectionProps {
  news: News[];
  state: AsyncState;
  error?: string;
  onRetry?: () => void;
}

export default function LatestNewsSection({ news, state, error, onRetry }: LatestNewsSectionProps) {
  return (
    <section aria-label="최신 뉴스">
      <div className="flex items-center justify-between px-4 py-3">
        <h2 className="text-lg font-bold text-gray-900">최신 뉴스</h2>
      </div>

      {state === 'loading' && (
        <div className="space-y-3 px-4">
          {Array.from({ length: 3 }).map((_, i) => (
            <SkeletonCard key={i} layout="horizontal" />
          ))}
        </div>
      )}

      {state === 'error' && (
        <div className="text-center py-8 px-4">
          <p className="text-gray-500">{error}</p>
          {onRetry && (
            <button
              onClick={onRetry}
              className="mt-3 px-4 py-2 bg-purple-600 text-white rounded-full text-sm font-medium hover:bg-purple-700 transition-colors"
            >
              다시 시도
            </button>
          )}
        </div>
      )}

      {state === 'success' && news.length === 0 && (
        <div className="text-center py-8 px-4">
          <p className="text-gray-400">최신 뉴스가 없습니다</p>
        </div>
      )}

      {state === 'success' && news.length > 0 && (
        <div className="space-y-1 px-4">
          {news.map((item) => (
            <NewsCard key={item.id} news={item} />
          ))}
        </div>
      )}
    </section>
  );
}
