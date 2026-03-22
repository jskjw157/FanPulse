"use client";

import LiveListItem from './LiveListItem';
import SkeletonCard from '@/components/ui/SkeletonCard';
import type { Live } from '@/types/live';
import type { AsyncState } from '@/types/common';

interface LiveGridProps {
  lives: Live[];
  state: AsyncState;
  error?: string;
  onRetry?: () => void;
}

export default function LiveGrid({ lives, state, error, onRetry }: LiveGridProps) {
  if (state === 'loading') {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {Array.from({ length: 6 }).map((_, i) => (
          <SkeletonCard key={`skeleton-${i}`} variant="vertical" />
        ))}
      </div>
    );
  }

  if (state === 'error') {
    return (
      <div className="flex flex-col items-center justify-center py-16 text-center">
        <p className="text-gray-500 mb-4">{error}</p>
        {onRetry && (
          <button
            onClick={onRetry}
            className="px-6 py-2 bg-purple-600 text-white rounded-full hover:bg-purple-700 transition-colors"
          >
            다시 시도
          </button>
        )}
      </div>
    );
  }

  if (lives.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-16 text-center">
        <p className="text-gray-500">라이브가 없습니다</p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      {lives.map((live) => (
        <LiveListItem key={live.id} live={live} />
      ))}
    </div>
  );
}
