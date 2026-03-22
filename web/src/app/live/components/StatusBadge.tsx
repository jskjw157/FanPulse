import type { LiveStatus } from '@/types/live';

interface StatusBadgeProps {
  status: LiveStatus;
}

export default function StatusBadge({ status }: StatusBadgeProps) {
  if (status === 'LIVE') {
    return (
      <span className="inline-flex items-center gap-1 px-2 py-1 bg-red-500 text-white text-xs font-bold rounded-md">
        <span className="w-1.5 h-1.5 bg-white rounded-full animate-pulse" />
        LIVE
      </span>
    );
  }

  if (status === 'SCHEDULED') {
    return (
      <span className="inline-flex items-center px-2 py-1 bg-gray-500 text-white text-xs font-medium rounded-md">
        예정
      </span>
    );
  }

  return (
    <span className="inline-flex items-center px-2 py-1 bg-gray-800 text-white text-xs font-medium rounded-md">
      종료
    </span>
  );
}
