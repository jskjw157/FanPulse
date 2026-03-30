"use client";

import { useInfiniteLiveList } from '@/hooks/useInfiniteLiveList';
import LiveGrid from './components/LiveGrid';
import InfiniteScroll from './components/InfiniteScroll';
import PageWrapper from '@/components/layout/PageWrapper';
import PageHeader from '@/components/layout/PageHeader';

export default function LiveListPage() {
  const { items, state, error, hasMore, loadMore, refresh } = useInfiniteLiveList();

  return (
    <>
      <PageHeader title="Live Now" />
      <PageWrapper>
        <div className="max-w-7xl mx-auto px-4 lg:px-8 py-6">
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
    </>
  );
}
