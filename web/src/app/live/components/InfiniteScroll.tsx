"use client";

import { useEffect, useRef, type ReactNode } from 'react';

interface InfiniteScrollProps {
  children: ReactNode;
  hasMore: boolean;
  loading: boolean;
  onLoadMore: () => void;
}

export default function InfiniteScroll({
  children,
  hasMore,
  loading,
  onLoadMore,
}: InfiniteScrollProps) {
  const sentinelRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const sentinel = sentinelRef.current;
    if (!sentinel) return;

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && hasMore && !loading) {
          onLoadMore();
        }
      },
      { rootMargin: '100px' }
    );

    observer.observe(sentinel);

    return () => {
      observer.disconnect();
    };
  }, [hasMore, loading, onLoadMore]);

  return (
    <>
      {children}

      <div ref={sentinelRef} className="h-4" />

      {loading && (
        <div className="flex justify-center py-8" role="status">
          <div className="w-8 h-8 border-4 border-purple-200 border-t-purple-600 rounded-full animate-spin" />
          <span className="sr-only">로딩 중...</span>
        </div>
      )}

      {!hasMore && !loading && (
        <p className="text-center text-gray-400 py-8">
          모든 라이브를 확인했습니다
        </p>
      )}
    </>
  );
}
