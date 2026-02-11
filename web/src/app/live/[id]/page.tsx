"use client";

import { useParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import { useLiveDetail } from '@/hooks/useLiveDetail';
import YouTubePlayer from '@/app/live/components/YouTubePlayer';
import LiveMetadata from '@/app/live/components/LiveMetadata';
import SkeletonCard from '@/components/ui/SkeletonCard';

export default function LiveDetailPage() {
  const params = useParams();
  const router = useRouter();
  const id = params.id as string;

  const { live, state, error } = useLiveDetail(id);

  if (state === 'loading') {
    return (
      <div className="min-h-screen bg-white">
        <header className="sticky top-0 z-50 bg-white border-b px-4 py-3">
          <button
            onClick={() => router.back()}
            className="flex items-center gap-1 text-gray-600 hover:text-gray-900"
          >
            <span>←</span>
            <span>뒤로</span>
          </button>
        </header>
        <div className="w-full aspect-video bg-gray-200 animate-pulse" />
        <div className="p-4 space-y-3">
          <SkeletonCard variant="horizontal" />
        </div>
      </div>
    );
  }

  if (state === 'error' || !live) {
    return (
      <div className="min-h-screen bg-white flex flex-col items-center justify-center px-4">
        <p className="text-gray-500 text-lg mb-4">{error ?? '라이브를 찾을 수 없습니다'}</p>
        <Link
          href="/"
          className="px-6 py-2 bg-purple-600 text-white rounded-full hover:bg-purple-700 transition-colors"
        >
          홈으로 이동
        </Link>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-white">
      <header className="sticky top-0 z-50 bg-white border-b px-4 py-3">
        <button
          onClick={() => router.back()}
          className="flex items-center gap-1 text-gray-600 hover:text-gray-900"
        >
          <span>←</span>
          <span>뒤로</span>
        </button>
      </header>

      <YouTubePlayer streamUrl={live.streamUrl} title={live.title} />

      <LiveMetadata live={live} />
    </div>
  );
}
