'use client';

import { useParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import { useNewsDetail } from '@/hooks/useNewsDetail';
import NewsHeader from './components/NewsHeader';
import NewsMetadata from './components/NewsMetadata';
import NewsContent from './components/NewsContent';
import SourceLink from './components/SourceLink';
import SkeletonCard from '@/components/ui/SkeletonCard';

export default function NewsDetailPage() {
  const params = useParams();
  const router = useRouter();

  const rawId = params.id;
  const id = Array.isArray(rawId) ? rawId[0] : rawId;

  const { news, state, error, retry } = useNewsDetail(id ?? '');

  if (state === 'loading') {
    return (
      <article className="min-h-screen bg-white pb-20">
        <header className="sticky top-0 z-50 bg-white border-b px-4 py-3">
          <button
            onClick={() => router.back()}
            className="flex items-center gap-1 text-gray-600 hover:text-gray-900"
          >
            <span>←</span>
            <span>뒤로</span>
          </button>
        </header>
        <div className="w-full h-56 bg-gray-200 animate-pulse" />
        <div className="p-4 space-y-3">
          <div className="h-8 w-3/4 bg-gray-200 rounded animate-pulse" />
          <div className="h-4 w-1/2 bg-gray-200 rounded animate-pulse" />
          <div className="space-y-2 mt-6">
            {[...Array(8)].map((_, i) => (
              <div key={i} className="h-4 w-full bg-gray-200 rounded animate-pulse" />
            ))}
          </div>
        </div>
      </article>
    );
  }

  if (state === 'error' || !news) {
    const isNotFound = error === '뉴스를 찾을 수 없습니다';

    return (
      <div className="min-h-screen bg-white flex flex-col items-center justify-center px-4">
        <p className="text-gray-500 text-lg mb-4">
          {error ?? '뉴스를 찾을 수 없습니다'}
        </p>
        {isNotFound ? (
          <Link
            href="/"
            className="px-6 py-2 bg-purple-600 text-white rounded-full hover:bg-purple-700 transition-colors"
          >
            홈으로 이동
          </Link>
        ) : (
          <button
            onClick={retry}
            className="px-6 py-2 bg-purple-600 text-white rounded-full hover:bg-purple-700 transition-colors"
          >
            다시 시도
          </button>
        )}
      </div>
    );
  }

  return (
    <article className="min-h-screen bg-white pb-20">
      <header className="sticky top-0 z-50 bg-white border-b px-4 py-3">
        <button
          onClick={() => router.back()}
          className="flex items-center gap-1 text-gray-600 hover:text-gray-900"
        >
          <span>←</span>
          <span>뒤로</span>
        </button>
      </header>

      <NewsHeader title={news.title} thumbnailUrl={news.thumbnailUrl} />

      <NewsMetadata
        source={news.source}
        publishedAt={news.publishedAt}
        author={news.author}
      />

      <hr className="my-4 mx-4 border-gray-100" />

      <NewsContent content={news.content} />

      <hr className="my-4 mx-4 border-gray-100" />

      <SourceLink url={news.sourceUrl} sourceName={news.source} />
    </article>
  );
}
