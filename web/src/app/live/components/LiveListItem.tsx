"use client";

import Link from 'next/link';
import Image from 'next/image';
import StatusBadge from './StatusBadge';
import { formatViewerCount } from '@/lib/utils/format';
import type { Live } from '@/types/live';

interface LiveListItemProps {
  live: Live;
}

export default function LiveListItem({ live }: LiveListItemProps) {
  const { id, title, artistName, thumbnailUrl, status, viewerCount } = live;

  return (
    <Link href={`/live/${id}`} className="block group">
      <article className="bg-white rounded-2xl overflow-hidden shadow-sm hover:shadow-md transition-shadow">
        <div className="relative">
          <Image
            src={thumbnailUrl}
            alt={title}
            width={384}
            height={192}
            sizes="(max-width: 768px) 100vw, (max-width: 1024px) 50vw, 33vw"
            className="w-full h-48 object-cover group-hover:scale-105 transition-transform duration-300"
          />
          <div className="absolute top-3 left-3">
            <StatusBadge status={status} />
          </div>
        </div>
        <div className="p-4">
          <h3 className="font-bold text-gray-900 line-clamp-1 group-hover:text-purple-600 transition-colors">
            {title}
          </h3>
          <p className="text-sm text-gray-500 mt-1">{artistName}</p>
          {status === 'LIVE' && viewerCount != null && (
            <span className="text-xs text-gray-400 mt-1 block">
              {formatViewerCount(viewerCount)}명
            </span>
          )}
        </div>
      </article>
    </Link>
  );
}
