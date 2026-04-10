"use client";

import Link from 'next/link';
import Image from 'next/image';
import { formatRelativeTime } from '@/lib/utils/format';
import type { News } from '@/types/news';

interface NewsCardProps {
  news: News;
}

export default function NewsCard({ news }: NewsCardProps) {
  const { id, title, summary, thumbnailUrl, source, publishedAt } = news;

  return (
    <Link
      href={`/news/${id}`}
      className="flex gap-3 hover:bg-gray-50 p-2 rounded-xl transition-colors"
    >
      {thumbnailUrl ? (
        <Image
          src={thumbnailUrl}
          alt={title}
          width={96}
          height={80}
          className="w-24 h-20 rounded-lg object-cover object-top flex-shrink-0"
        />
      ) : (
        <div className="w-24 h-20 rounded-lg bg-gradient-to-br from-purple-100 to-pink-100 flex-shrink-0 flex items-center justify-center">
          <svg className="w-8 h-8 text-purple-300" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M12 7.5h1.5m-1.5 3h1.5m-7.5 3h7.5m-7.5 3h7.5m3-9h3.375c.621 0 1.125.504 1.125 1.125V18a2.25 2.25 0 0 1-2.25 2.25M16.5 7.5V18a2.25 2.25 0 0 0 2.25 2.25M16.5 7.5V4.875c0-.621-.504-1.125-1.125-1.125H4.125C3.504 3.75 3 4.254 3 4.875V18a2.25 2.25 0 0 0 2.25 2.25h13.5" />
          </svg>
        </div>
      )}
      <div className="flex-1 min-w-0">
        <h3 className="font-medium text-gray-900 line-clamp-1">{title}</h3>
        <p className="text-sm text-gray-600 line-clamp-2 mt-0.5">{summary}</p>
        <span className="text-xs text-gray-400 mt-1 block">
          {source} · {formatRelativeTime(publishedAt)}
        </span>
      </div>
    </Link>
  );
}
