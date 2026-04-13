"use client";

import { useTranslation } from 'react-i18next';
import { useNewsList } from '@/hooks/useNewsList';
import NewsCard from '@/app/components/home/NewsCard';
import SkeletonCard from '@/components/ui/SkeletonCard';
import PageWrapper from '@/components/layout/PageWrapper';
import PageHeader from '@/components/layout/PageHeader';

export default function NewsListPage() {
  const { t } = useTranslation();
  const { items, state, error, hasMore, loadMore, refresh } = useNewsList();

  return (
    <>
      <PageHeader title={t('news.title')} />
      <PageWrapper>
        <div className="max-w-7xl mx-auto px-4 lg:px-8 py-6">
          {/* 초기 로딩 */}
          {state === 'loading' && items.length === 0 && (
            <div className="space-y-3">
              {Array.from({ length: 6 }).map((_, i) => (
                <SkeletonCard key={i} layout="horizontal" />
              ))}
            </div>
          )}

          {/* 에러 상태 */}
          {state === 'error' && items.length === 0 && (
            <div className="flex flex-col items-center justify-center py-16 text-center">
              <p className="text-gray-500 mb-4">{error}</p>
              <button
                onClick={refresh}
                className="px-6 py-2 bg-purple-600 text-white rounded-full hover:bg-purple-700 transition-colors"
              >
                {t('news.error.retry')}
              </button>
            </div>
          )}

          {/* 빈 상태 */}
          {state === 'success' && items.length === 0 && (
            <div className="flex flex-col items-center justify-center py-16 text-center">
              <p className="text-gray-500">{t('news.empty')}</p>
            </div>
          )}

          {/* 뉴스 목록 */}
          {items.length > 0 && (
            <div className="space-y-1">
              {items.map((news) => (
                <NewsCard key={news.id} news={news} />
              ))}
            </div>
          )}

          {/* 더보기 버튼 */}
          {items.length > 0 && hasMore && (
            <div className="flex justify-center py-8">
              <button
                onClick={loadMore}
                disabled={state === 'loading'}
                className="px-6 py-2 bg-purple-600 text-white rounded-full text-sm font-medium hover:bg-purple-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {state === 'loading' ? t('news.loading') : t('news.loadMore')}
              </button>
            </div>
          )}

          {/* 추가 로딩 스피너 */}
          {state === 'loading' && items.length > 0 && (
            <div className="flex justify-center py-8" role="status">
              <div className="w-8 h-8 border-4 border-purple-200 border-t-purple-600 rounded-full animate-spin" />
              <span className="sr-only">{t('news.loading')}</span>
            </div>
          )}

          {/* 모두 로드 완료 */}
          {!hasMore && items.length > 0 && state !== 'loading' && (
            <p className="text-center text-gray-400 py-8">
              {t('news.allLoaded')}
            </p>
          )}
        </div>
      </PageWrapper>
    </>
  );
}
