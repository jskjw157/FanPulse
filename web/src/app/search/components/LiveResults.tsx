import Link from 'next/link';
import Image from 'next/image';
import type { Live } from '@/types/live';
import StatusBadge from '@/app/live/components/StatusBadge';
import { formatViewerCount } from '@/lib/utils/format';

interface LiveResultsProps {
  lives: Live[];
}

export function LiveResults({ lives }: LiveResultsProps) {
  if (lives.length === 0) {
    return null;
  }

  return (
    <section className="px-4 py-4">
      <h2 className="text-sm font-bold text-gray-900 mb-3">라이브</h2>
      <div className="space-y-3">
        {lives.map((live) => (
          <Link
            key={live.id}
            href={`/live/${live.id}`}
            className="flex gap-3 hover:bg-gray-50 p-2 rounded-xl transition-colors"
          >
            <div className="relative w-28 h-20 flex-shrink-0">
              <Image
                src={live.thumbnailUrl}
                alt={live.title}
                fill
                className="rounded-lg object-cover"
              />
              <div className="absolute top-1 left-1">
                <StatusBadge status={live.status} />
              </div>
            </div>
            <div className="flex-1 min-w-0">
              <h3 className="text-sm font-medium text-gray-900 line-clamp-1">
                {live.title}
              </h3>
              <p className="text-xs text-gray-500 mt-0.5">{live.artistName}</p>
              <span className="text-xs text-gray-400 mt-1 block">
                {formatViewerCount(live.viewerCount)}명 시청
              </span>
            </div>
          </Link>
        ))}
      </div>
    </section>
  );
}
