import Link from 'next/link';
import Image from 'next/image';
import type { News } from '@/types/news';
import { formatRelativeTime } from '@/lib/utils/format';

interface NewsResultsProps {
  newsList: News[];
}

export function NewsResults({ newsList }: NewsResultsProps) {
  if (newsList.length === 0) {
    return null;
  }

  return (
    <section className="px-4 py-4 bg-gray-50">
      <h2 className="text-sm font-bold text-gray-900 mb-3">뉴스</h2>
      <div className="space-y-3">
        {newsList.map((news) => (
          <Link
            key={news.id}
            href={`/news/${news.id}`}
            className="flex gap-3 p-2 rounded-xl hover:bg-gray-100 transition-colors"
          >
            <div className="relative w-24 h-20 flex-shrink-0">
              <Image
                src={news.thumbnailUrl}
                alt={news.title}
                fill
                className="rounded-lg object-cover"
              />
            </div>
            <div className="flex-1 min-w-0">
              <h3 className="text-sm font-medium text-gray-900 line-clamp-2">
                {news.title}
              </h3>
              <span className="text-xs text-gray-500 mt-1 block">
                {news.source} · {formatRelativeTime(news.publishedAt)}
              </span>
            </div>
          </Link>
        ))}
      </div>
    </section>
  );
}
