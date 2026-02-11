"use client";

import Link from 'next/link';
import Image from 'next/image';
import Badge from '@/components/ui/Badge';
import { formatViewerCount, formatScheduledDate } from '@/lib/utils/format';
import type { Live } from '@/types/live';

interface LiveCardProps {
  live: Live;
}

export default function LiveCard({ live }: LiveCardProps) {
  const { id, title, artistName, thumbnailUrl, status, viewerCount, scheduledAt } = live;

  return (
    <Link href={`/live/${id}`} className="block flex-shrink-0 w-[280px] group">
      <div className="relative rounded-2xl overflow-hidden bg-white shadow-sm hover:shadow-md transition-shadow">
        <Image
          src={thumbnailUrl}
          alt={title}
          width={280}
          height={160}
          className="w-full h-40 object-cover group-hover:scale-105 transition-transform duration-300"
        />

        {status === 'LIVE' && (
          <div className="absolute top-3 left-3">
            <Badge variant="danger">
              <span className="flex items-center gap-1">
                <span className="w-1.5 h-1.5 bg-white rounded-full animate-pulse" />
                LIVE
              </span>
            </Badge>
          </div>
        )}

        {status === 'SCHEDULED' && (
          <div className="absolute top-3 left-3">
            <Badge variant="default">예정</Badge>
          </div>
        )}

        {status === 'ENDED' && (
          <div className="absolute top-3 left-3">
            <Badge>종료</Badge>
          </div>
        )}
      </div>

      <div className="p-3">
        <h3 className="font-bold text-gray-900 line-clamp-1 group-hover:text-purple-600 transition-colors">
          {title}
        </h3>
        <p className="text-sm text-gray-500 mt-0.5">{artistName}</p>
        {status === 'LIVE' && viewerCount != null && (
          <span className="text-xs text-gray-400 mt-1 block">
            {formatViewerCount(viewerCount)}명
          </span>
        )}
        {status === 'SCHEDULED' && scheduledAt && (
          <span className="text-xs text-gray-400 mt-1 block">
            {formatScheduledDate(scheduledAt)}
          </span>
        )}
      </div>
    </Link>
  );
}
