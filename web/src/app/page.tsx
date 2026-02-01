"use client";

import { useHomeSections } from '@/hooks/useHomeSections';
import LiveSection from './components/home/LiveSection';
import LatestNewsSection from './components/home/LatestNewsSection';

export default function Home() {
  const { liveNow, upcoming, latestNews, state, error, refresh } = useHomeSections();

  if (state === 'error') {
    return (
      <main className="max-w-7xl mx-auto pb-20">
        <div className="text-center py-16 px-4">
          <p className="text-gray-500">{error}</p>
          <button
            onClick={refresh}
            className="mt-4 px-6 py-2 bg-purple-600 text-white rounded-full text-sm font-medium hover:bg-purple-700 transition-colors"
          >
            다시 시도
          </button>
        </div>
      </main>
    );
  }

  return (
    <main className="max-w-7xl mx-auto pb-20">
      <LiveSection
        title="Live Now"
        lives={liveNow}
        state={state}
        onRetry={refresh}
      />

      <LiveSection
        title="Upcoming"
        lives={upcoming}
        state={state}
        onRetry={refresh}
      />

      <LatestNewsSection
        news={latestNews}
        state={state}
        onRetry={refresh}
      />
    </main>
  );
}
