'use client';

import { useState } from 'react';
import Link from 'next/link';
import PageHeader from '@/components/layout/PageHeader';
import PageWrapper from '@/components/layout/PageWrapper';
import { useLatestChart } from '@/hooks/useLatestChart';
import type { ChartEntry, ChartType } from '@/lib/api/chart';

const CHARTS: Array<{ id: ChartType; name: string }> = [
  { id: 'MELON', name: 'Melon' },
  { id: 'BILLBOARD_US', name: 'Billboard' },
  { id: 'BUGS', name: 'Bugs' },
];

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

export default function ChartPage() {
  const [activeChart, setActiveChart] = useState<ChartType>('MELON');
  const { chart, state, error, retry } = useLatestChart(activeChart);

  return (
    <>
      <PageHeader title="Real-time Chart" />
      <PageWrapper>
        <div className="max-w-4xl mx-auto px-4 py-6">
          <div className="flex gap-2 overflow-x-auto pb-4 scrollbar-hide">
            {CHARTS.map((item) => (
              <button
                key={item.id}
                type="button"
                onClick={() => setActiveChart(item.id)}
                className={`px-4 py-2 rounded-full whitespace-nowrap transition-all ${
                  activeChart === item.id
                    ? 'bg-gradient-to-r from-purple-600 to-pink-600 text-white shadow-md'
                    : 'bg-white text-gray-600 border border-gray-200'
                }`}
              >
                {item.name}
              </button>
            ))}
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
            <p className="py-12 text-center text-gray-500">등록된 차트 항목이 없습니다</p>
          )}

          {state === 'success' && chart && chart.entries.length > 0 && (
            <div className="space-y-3">
              {chart.entries.map((entry) => (
                <Link
                  key={entry.id}
                  href={`/artists/${entry.artistId}`}
                  className="flex items-center gap-4 bg-white rounded-2xl p-4 shadow-sm hover:shadow-md transition-shadow"
                >
                  <span className="w-10 text-center text-lg font-bold text-gray-700">
                    {entry.rank}
                  </span>
                  <div className="min-w-0 flex-1">
                    <h3 className="font-bold text-gray-900">{entry.trackTitle}</h3>
                    <p className="mt-1 text-sm text-gray-600">{entry.artistName}</p>
                    <p className="mt-1 text-xs text-gray-500">
                      최고 {entry.peakRank}위 · {entry.weeksOnChart}주 차트인
                    </p>
                  </div>
                  <RankChange entry={entry} />
                </Link>
              ))}
            </div>
          )}
        </div>
      </PageWrapper>
    </>
  );
}
