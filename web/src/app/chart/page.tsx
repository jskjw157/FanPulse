'use client';

import Link from 'next/link';
import PageHeader from '@/components/layout/PageHeader';
import PageWrapper from '@/components/layout/PageWrapper';
import { useLatestChart } from '@/hooks/useLatestChart';
import type { ChartEntry } from '@/lib/api/chart';

function RankChange({ entry }: { entry: ChartEntry }) {
  if (entry.isNew) {
    return <span className="text-xs font-semibold text-purple-600">NEW</span>;
  }

  const change = entry.rankChange ?? 0;
  return (
    <span
      className={`text-sm font-medium ${
        change > 0 ? 'text-red-500' : change < 0 ? 'text-blue-500' : 'text-gray-400'
      }`}
    >
      {change > 0 ? `▲ ${change}` : change < 0 ? `▼ ${Math.abs(change)}` : '—'}
    </span>
  );
}

function Artwork({ entry }: { entry: ChartEntry }) {
  if (entry.artworkUrl) {
    return (
      // eslint-disable-next-line @next/next/no-img-element
      <img
        src={entry.artworkUrl}
        alt={entry.trackTitle}
        width={56}
        height={56}
        className="h-14 w-14 flex-shrink-0 rounded-xl object-cover bg-gray-100"
      />
    );
  }

  return (
    <div
      aria-hidden="true"
      className="flex h-14 w-14 flex-shrink-0 items-center justify-center rounded-xl bg-gray-100 text-gray-400"
    >
      <i className="ri-music-2-line text-xl" />
    </div>
  );
}

function ChartRow({ entry }: { entry: ChartEntry }) {
  const body = (
    <>
      <span className="w-10 text-center text-lg font-bold text-gray-700">
        {entry.rank}
      </span>
      <Artwork entry={entry} />
      <div className="min-w-0 flex-1">
        <h2 className="font-bold text-gray-900">{entry.trackTitle}</h2>
        <p className="mt-1 text-sm text-gray-600">{entry.artistName}</p>
        <p className="mt-1 text-xs text-gray-500">
          최고 {entry.peakRank}위 · {entry.weeksOnChart}주 차트인
        </p>
      </div>
      <RankChange entry={entry} />
    </>
  );

  if (entry.artistId) {
    return (
      <Link
        href={`/artists/${entry.artistId}`}
        className="flex items-center gap-4 rounded-2xl bg-white p-4 shadow-sm transition-shadow hover:shadow-md"
      >
        {body}
      </Link>
    );
  }

  return (
    <div className="flex items-center gap-4 rounded-2xl bg-white p-4 shadow-sm">
      {body}
    </div>
  );
}

export default function ChartPage() {
  const { chart, state, error, retry } = useLatestChart('APPLE_MUSIC');

  return (
    <>
      <PageHeader title="Music Chart" />
      <PageWrapper>
        <div className="mx-auto max-w-4xl px-4 py-6">
          <div className="mb-4 rounded-2xl border border-gray-200 bg-white p-4">
            <h1 className="font-bold text-gray-900">Apple Music Korea Top 100</h1>
            <p className="mt-1 text-sm text-gray-500">
              Apple Music Korea 실시간 Top 100입니다. 등록된 아티스트만 상세 페이지로 연결됩니다.
            </p>
            {chart && (
              <time className="mt-2 block text-xs text-gray-500" dateTime={chart.chartDate}>
                차트 기준일 {chart.chartDate}
              </time>
            )}
          </div>

          {state === 'loading' && (
            <p className="py-12 text-center text-gray-500">차트를 불러오는 중입니다</p>
          )}

          {state === 'error' && (
            <div className="py-12 text-center">
              <p className="text-gray-500">{error ?? '차트를 불러올 수 없습니다'}</p>
              <button
                type="button"
                onClick={retry}
                className="mt-4 rounded-full bg-purple-600 px-6 py-2 text-sm font-medium text-white hover:bg-purple-700"
              >
                다시 시도
              </button>
            </div>
          )}

          {state === 'success' && chart?.entries.length === 0 && (
            <p className="py-12 text-center text-gray-500">차트 항목이 없습니다</p>
          )}

          {state === 'success' && chart && chart.entries.length > 0 && (
            <div className="space-y-3">
              {chart.entries.map((entry) => (
                <ChartRow key={entry.id} entry={entry} />
              ))}
            </div>
          )}
        </div>
      </PageWrapper>
    </>
  );
}
