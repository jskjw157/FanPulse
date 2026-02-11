"use client";

import { useInfiniteLiveList } from '@/hooks/useInfiniteLiveList';
import LiveGrid from './components/LiveGrid';
import InfiniteScroll from './components/InfiniteScroll';
import PageWrapper from '@/components/layout/PageWrapper';

export default function LiveListPage() {
  const { items, state, error, hasMore, loadMore, refresh } = useInfiniteLiveList();

  return (
    <PageWrapper>
      <div className="max-w-7xl mx-auto px-4 lg:px-8 py-6">
        <header className="mb-6">
          <h1 className="text-2xl font-bold text-gray-900">Live Now</h1>
          <p className="text-gray-500 mt-1">실시간 라이브 방송을 만나보세요</p>
        </header>

        <InfiniteScroll
          hasMore={hasMore}
          loading={state === 'loading' && items.length > 0}
          onLoadMore={loadMore}
        >
          <LiveGrid
            lives={items}
            state={items.length === 0 ? state : 'success'}
            error={error ?? undefined}
            onRetry={refresh}
          />
        </InfiniteScroll>
      </div>
    </PageWrapper>
  );
}
