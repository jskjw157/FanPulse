"use client";

import LiveCard from './LiveCard';
import SkeletonCard from '@/components/ui/SkeletonCard';
import type { Live } from '@/types/live';
import type { AsyncState } from '@/types/common';

interface LiveSectionProps {
  title: string;
  lives: Live[];
  state: AsyncState;
  error?: string;
  onRetry?: () => void;
}

export default function LiveSection({ title, lives, state, error, onRetry }: LiveSectionProps) {
  return (
    <section aria-label={title}>
      <div className="flex items-center justify-between px-4 py-3">
        <h2 className="text-lg font-bold text-gray-900">{title}</h2>
      </div>

      {state === 'loading' && (
        <div className="flex overflow-x-auto gap-4 px-4 scrollbar-hide">
          {Array.from({ length: 3 }).map((_, i) => (
            <SkeletonCard key={i} />
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

      {state === 'success' && lives.length === 0 && (
        <div className="text-center py-8 px-4">
          <p className="text-gray-400">현재 라이브 방송이 없습니다</p>
        </div>
      )}

      {state === 'success' && lives.length > 0 && (
        <div className="flex overflow-x-auto gap-4 px-4 pb-2 scrollbar-hide">
          {lives.map((live) => (
            <LiveCard key={live.id} live={live} />
          ))}
        </div>
      )}
    </section>
  );
}
