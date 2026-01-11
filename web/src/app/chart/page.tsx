"use client";

import PageHeader from "@/components/layout/PageHeader";
import PageWrapper from "@/components/layout/PageWrapper";
import { useState } from "react";
import Link from "next/link";

export default function ChartPage() {
  const [activeChart, setActiveChart] = useState('melon');

  const charts = [
    { id: 'melon', name: 'Melon', icon: 'ri-music-2-fill' },
    { id: 'billboard', name: 'Billboard', icon: 'ri-global-line' },
    { id: 'bugs', name: 'Bugs', icon: 'ri-bug-fill' }
  ];

  const rankings = [
    {
      rank: 1,
      prevRank: 1,
      change: 0,
      title: 'Super Shy',
      artist: 'NewJeans',
      album: 'Get Up',
      image: 'https://readdy.ai/api/search-image?query=KPOP%20album%20cover%20NewJeans%20Super%20Shy%2C%20modern%20minimalist%20design%2C%20pastel%20colors%2C%20professional%20album%20artwork%2C%20clean%20aesthetic%2C%20youthful%20vibe&width=200&height=200&seq=chart001&orientation=squarish'
    },
    {
      rank: 2,
      prevRank: 3,
      change: 1,
      title: 'Seven',
      artist: 'Jungkook (BTS)',
      album: 'Seven',
      image: 'https://readdy.ai/api/search-image?query=KPOP%20album%20cover%20Jungkook%20Seven%2C%20bold%20typography%2C%20vibrant%20colors%2C%20professional%20album%20artwork%2C%20modern%20design%2C%20energetic%20feel&width=200&height=200&seq=chart002&orientation=squarish'
    },
    {
      rank: 3,
      prevRank: 2,
      change: -1,
      title: 'Queencard',
      artist: '(G)I-DLE',
      album: 'I feel',
      image: 'https://readdy.ai/api/search-image?query=KPOP%20album%20cover%20GIDLE%20Queencard%2C%20fierce%20concept%2C%20bold%20colors%2C%20professional%20album%20artwork%2C%20powerful%20aesthetic%2C%20glamorous%20design&width=200&height=200&seq=chart003&orientation=squarish'
    },
    {
      rank: 4,
      prevRank: 5,
      change: 1,
      title: 'Spicy',
      artist: 'aespa',
      album: 'MY WORLD',
      image: 'https://readdy.ai/api/search-image?query=KPOP%20album%20cover%20aespa%20Spicy%2C%20futuristic%20design%2C%20vibrant%20red%20colors%2C%20professional%20album%20artwork%2C%20cyber%20aesthetic%2C%20bold%20typography&width=200&height=200&seq=chart004&orientation=squarish'
    },
    {
      rank: 5,
      prevRank: 4,
      change: -1,
      title: 'Kitsch',
      artist: 'IVE',
      album: 'I\'ve IVE',
      image: 'https://readdy.ai/api/search-image?query=KPOP%20album%20cover%20IVE%20Kitsch%2C%20retro%20pop%20art%20style%2C%20colorful%20design%2C%20professional%20album%20artwork%2C%20playful%20aesthetic%2C%20vintage%20vibe&width=200&height=200&seq=chart005&orientation=squarish'
    }
  ];

  return (
    <>
      <PageHeader title="Real-time Chart" />
      <PageWrapper>
        <div className="max-w-4xl mx-auto px-4 py-6">
          {/* Chart Tabs */}
          <div className="flex gap-2 overflow-x-auto pb-4 scrollbar-hide">
            {charts.map(chart => (
              <button
                key={chart.id}
                onClick={() => setActiveChart(chart.id)}
                className={`flex items-center gap-2 px-4 py-2 rounded-full whitespace-nowrap transition-all ${activeChart === chart.id
                    ? 'bg-gradient-to-r from-purple-600 to-pink-600 text-white shadow-md'
                    : 'bg-white text-gray-600 border border-gray-200'
                }`}
              >
                <i className={chart.icon}></i>
                <span className="text-sm font-medium">{chart.name}</span>
              </button>
            ))}
          </div>

          <div className="space-y-3">
            {rankings.map((item, index) => (
              <Link 
                key={item.rank} 
                href={`/artist-detail?artist=${encodeURIComponent(item.artist)}`}
                className="flex items-center gap-3 bg-white rounded-2xl p-4 shadow-sm hover:shadow-md transition-shadow"
              >
                {/* Rank */}
                <div className={`w-10 h-10 flex items-center justify-center font-bold text-lg flex-shrink-0 ${index === 0 ? 'text-yellow-500' :
                  index === 1 ? 'text-gray-400' :
                  index === 2 ? 'text-orange-600' :
                  'text-gray-600'
                }`}>
                  {item.rank}
                </div>

                {/* Artist Image */}
                {/* eslint-disable-next-line @next/next/no-img-element */}
                <img 
                  src={item.image}
                  alt={item.artist}
                  className="w-16 h-16 rounded-xl object-cover object-top flex-shrink-0"
                />

                {/* Info */}
                <div className="flex-1 min-w-0">
                  <h3 className="font-bold text-gray-900">{item.title}</h3>
                  <p className="text-sm text-gray-600 mt-0.5">{item.artist}</p>
                  <p className="text-xs text-gray-500 mt-1">{item.album}</p>
                </div>

                {/* Change */}
                <div className={`flex items-center gap-1 text-sm font-medium flex-shrink-0 ${item.change > 0 ? 'text-red-500' :
                  item.change < 0 ? 'text-blue-500' :
                  'text-gray-400'
                }`}>
                  {item.change > 0 && <i className="ri-arrow-up-line text-lg"></i>}
                  {item.change < 0 && <i className="ri-arrow-down-line text-lg"></i>}
                  {item.change === 0 && <i className="ri-subtract-line text-lg"></i>}
                  <span className="w-6 text-right">{Math.abs(item.change) || '-'}</span>
                </div>
              </Link>
            ))}
          </div>
        </div>
      </PageWrapper>
    </>
  );
}