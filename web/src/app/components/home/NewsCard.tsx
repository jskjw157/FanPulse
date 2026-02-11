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
      <Image
        src={thumbnailUrl}
        alt={title}
        width={96}
        height={80}
        className="w-24 h-20 rounded-lg object-cover object-top flex-shrink-0"
      />
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
