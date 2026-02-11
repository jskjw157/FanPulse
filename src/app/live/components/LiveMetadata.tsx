import StatusBadge from './StatusBadge';
import { formatViewerCount } from '@/lib/utils/format';
import type { LiveDetail } from '@/types/live';

interface LiveMetadataProps {
  live: LiveDetail;
}

export default function LiveMetadata({ live }: LiveMetadataProps) {
  const { title, artistName, description, status, viewerCount } = live;

  return (
    <div className="px-4 py-4">
      <div className="flex items-center gap-2 mb-2">
        <StatusBadge status={status} />
        {status === 'LIVE' && viewerCount != null && (
          <span className="text-sm text-purple-600 font-medium">
            {formatViewerCount(viewerCount)}명 시청 중
          </span>
        )}
      </div>

      <h1 className="text-xl font-bold text-gray-900">{title}</h1>
      <p className="text-sm text-gray-500 mt-1">{artistName}</p>

      {description && (
        <p className="text-sm text-gray-700 mt-4 whitespace-pre-wrap">{description}</p>
      )}
    </div>
  );
}
