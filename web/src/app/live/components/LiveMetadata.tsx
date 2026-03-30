import type { LiveDetail } from '@/types/live';

interface LiveMetadataProps {
  live: LiveDetail;
}

function formatViewerCount(count: number): string {
  return count.toLocaleString('ko-KR');
}

export default function LiveMetadata({ live }: LiveMetadataProps) {
  return (
    <div className="px-4 py-4">
      <h1 className="text-xl font-bold text-gray-900">{live.title}</h1>
      <p className="text-sm text-gray-500 mt-1">{live.artistName}</p>

      {live.viewerCount !== undefined && live.status === 'LIVE' && (
        <p className="text-sm text-purple-600 mt-1">
          {formatViewerCount(live.viewerCount)}명 시청 중
        </p>
      )}

      {live.description && (
        <p className="text-sm text-gray-700 mt-4 whitespace-pre-wrap">
          {live.description}
        </p>
      )}
    </div>
  );
}
