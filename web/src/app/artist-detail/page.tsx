"use client";

import PageHeader from "@/components/layout/PageHeader";
import PageWrapper from "@/components/layout/PageWrapper";
import { Suspense, useState } from "react";
import Link from "next/link";
import { useSearchParams } from "next/navigation";

function ArtistDetailContent() {
  const searchParams = useSearchParams();
  const artistName = searchParams.get('artist') || 'BTS';
  const [isFollowing, setIsFollowing] = useState(false);
  const [activeTab, setActiveTab] = useState('overview');

  const artist = {
    name: artistName,
    image: 'https://readdy.ai/api/search-image?query=BTS%20kpop%20group%20professional%20photo%2C%20vibrant%20stage%20lighting%2C%20dynamic%20performance%20shot%2C%20high%20quality%20photography%2C%20energetic%20atmosphere%2C%20purple%20and%20blue%20lighting%2C%20concert%20stage%20background&width=800&height=600&seq=artist001&orientation=landscape',
    followers: '2.5M',
    votes: '1.2M',
    rank: 1,
    description: `${artistName}은 대한민국의 대표 K-POP 그룹으로, 전 세계적으로 가장 성공한 아티스트 중 하나입니다.`,
    members: ['RM', 'Jin', 'Suga', 'J-Hope', 'Jimin', 'V', 'Jungkook'],
    achievements: [
      'Billboard Hot 100 1위',
      'Grammy Awards 후보',
      '유엔 연설',
      '글로벌 앨범 판매 3천만장 돌파'
    ]
  };

  const recentNews = [
    {
      id: 1,
      title: `${artistName} 새 앨범 발매 예정`,
      date: '2024.12.10',
      image: 'https://readdy.ai/api/search-image?query=BTS%20new%20album%20announcement%2C%20professional%20press%20photo%2C%20modern%20studio%20setting%2C%20album%20cover%20concept%2C%20high%20quality%20photography%2C%20purple%20and%20blue%20theme&width=400&height=300&seq=news003&orientation=landscape'
    },
    {
      id: 2,
      title: `${artistName} 월드투어 일정 공개`,
      date: '2024.12.08',
      image: 'https://readdy.ai/api/search-image?query=BTS%20world%20tour%20announcement%2C%20concert%20stage%20setup%2C%20spectacular%20lighting%2C%20professional%20photography%2C%20global%20tour%20concept&width=400&height=300&seq=news004&orientation=landscape'
    }
  ];

  const upcomingEvents = [
    {
      id: 1,
      title: `${artistName} World Tour Seoul`,
      date: '2024.12.20',
      venue: '잠실 올림픽 주경기장'
    },
    {
      id: 2,
      title: 'Fan Meeting 2024',
      date: '2024.12.25',
      venue: 'KSPO DOME'
    }
  ];

  return (
    <>
      <PageHeader title="Artist Profile" />
      <PageWrapper>
        {/* Hero Section */}
        <div className="relative">
          <div className="h-64">
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img 
              src={artist.image}
              alt={artist.name}
              className="w-full h-full object-cover object-top"
            />
            <div className="absolute inset-0 bg-gradient-to-t from-black/70 to-transparent"></div>
          </div>
          <div className="absolute bottom-0 left-0 right-0 px-4 pb-4">
            <div className="flex items-end justify-between">
              <div>
                <h1 className="text-3xl font-bold text-white mb-2">{artist.name}</h1>
                <div className="flex items-center gap-4 text-white/90 text-sm">
                  <span className="flex items-center gap-1">
                    <i className="ri-user-line"></i>
                    {artist.followers}
                  </span>
                  <span className="flex items-center gap-1">
                    <i className="ri-trophy-line"></i>
                    #{artist.rank}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="px-4 py-4 flex gap-3">
          <button
            onClick={() => setIsFollowing(!isFollowing)}
            className={`flex-1 py-3 rounded-full font-medium text-sm transition-colors ${
              isFollowing
                ? 'bg-gray-100 text-gray-700'
                : 'bg-gradient-to-r from-purple-600 to-pink-600 text-white shadow-md'
            }`}
          >
            {isFollowing ? '팔로잉' : '팔로우'}
          </button>
          <Link 
            href="/voting"
            className="flex-1 bg-purple-100 text-purple-700 py-3 rounded-full font-medium text-sm text-center hover:bg-purple-200 transition-colors"
          >
            투표하기
          </Link>
        </div>

        {/* Stats */}
        <div className="px-4 py-4 grid grid-cols-3 gap-3">
          <div className="bg-gradient-to-br from-purple-50 to-pink-50 rounded-2xl p-4 text-center">
            <p className="text-2xl font-bold text-purple-600">{artist.followers}</p>
            <p className="text-xs text-gray-600 mt-1">팔로워</p>
          </div>
          <div className="bg-gradient-to-br from-purple-50 to-pink-50 rounded-2xl p-4 text-center">
            <p className="text-2xl font-bold text-purple-600">{artist.votes}</p>
            <p className="text-xs text-gray-600 mt-1">투표수</p>
          </div>
          <div className="bg-gradient-to-br from-purple-50 to-pink-50 rounded-2xl p-4 text-center">
            <p className="text-2xl font-bold text-purple-600">#{artist.rank}</p>
            <p className="text-xs text-gray-600 mt-1">순위</p>
          </div>
        </div>

        {/* Tabs */}
        <div className="px-4 py-2 border-b border-gray-200 sticky top-16 bg-white z-10">
          <div className="flex gap-6">
            <button
              onClick={() => setActiveTab('overview')}
              className={`pb-3 text-sm font-medium border-b-2 transition-colors ${
                activeTab === 'overview'
                  ? 'border-purple-600 text-purple-600'
                  : 'border-transparent text-gray-500'
              }`}
            >
              개요
            </button>
            <button
              onClick={() => setActiveTab('news')}
              className={`pb-3 text-sm font-medium border-b-2 transition-colors ${
                activeTab === 'news'
                  ? 'border-purple-600 text-purple-600'
                  : 'border-transparent text-gray-500'
              }`}
            >
              뉴스
            </button>
            <button
              onClick={() => setActiveTab('events')}
              className={`pb-3 text-sm font-medium border-b-2 transition-colors ${
                activeTab === 'events'
                  ? 'border-purple-600 text-purple-600'
                  : 'border-transparent text-gray-500'
              }`}
            >
              일정
            </button>
          </div>
        </div>

        {/* Tab Content */}
        <div className="px-4 py-4 min-h-[300px]">
          {activeTab === 'overview' && (
            <div className="space-y-6">
              {/* Description */}
              <div>
                <h2 className="text-base font-bold text-gray-900 mb-3">소개</h2>
                <p className="text-sm text-gray-700 leading-relaxed">
                  {artist.description}
                </p>
              </div>

              {/* Members */}
              <div>
                <h2 className="text-base font-bold text-gray-900 mb-3">멤버</h2>
                <div className="flex flex-wrap gap-2">
                  {artist.members.map(member => (
                    <span
                      key={member}
                      className="bg-purple-50 text-purple-700 px-3 py-1.5 rounded-full text-sm font-medium"
                    >
                      {member}
                    </span>
                  ))}
                </div>
              </div>

              {/* Achievements */}
              <div>
                <h2 className="text-base font-bold text-gray-900 mb-3">주요 성과</h2>
                <div className="space-y-2">
                  {artist.achievements.map((achievement, index) => (
                    <div key={index} className="flex items-center gap-2">
                      <i className="ri-trophy-line text-purple-600"></i>
                      <span className="text-sm text-gray-700">{achievement}</span>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}

          {activeTab === 'news' && (
            <div className="space-y-3">
              {recentNews.map(news => (
                <Link
                  key={news.id}
                  href={`/news-detail?id=${news.id}`}
                  className="flex gap-3 bg-gray-50 rounded-2xl p-3 hover:bg-gray-100 transition-colors"
                >
                  {/* eslint-disable-next-line @next/next/no-img-element */}
                  <img 
                    src={news.image}
                    alt={news.title}
                    className="w-24 h-20 rounded-xl object-cover object-top flex-shrink-0"
                  />
                  <div className="flex-1">
                    <h3 className="text-sm font-bold text-gray-900 mb-1 line-clamp-2">
                      {news.title}
                    </h3>
                    <p className="text-xs text-gray-500">{news.date}</p>
                  </div>
                </Link>
              ))}
            </div>
          )}

          {activeTab === 'events' && (
            <div className="space-y-3">
              {upcomingEvents.map(event => (
                <div key={event.id} className="bg-gradient-to-br from-purple-50 to-pink-50 rounded-2xl p-4">
                  <h3 className="font-bold text-gray-900 mb-2">{event.title}</h3>
                  <div className="space-y-1 text-sm text-gray-700">
                    <p className="flex items-center gap-2">
                      <i className="ri-calendar-line text-purple-600"></i>
                      {event.date}
                    </p>
                    <p className="flex items-center gap-2">
                      <i className="ri-map-pin-line text-purple-600"></i>
                      {event.venue}
                    </p>
                  </div>
                  <Link
                    href="/concert"
                    className="block w-full mt-3 bg-gradient-to-r from-purple-600 to-pink-600 text-white text-sm font-medium py-2 rounded-full text-center hover:shadow-md transition-shadow"
                  >
                    티켓 예매
                  </Link>
                </div>
              ))}
            </div>
          )}
        </div>
      </PageWrapper>
    </>
  );
}

export default function ArtistDetailPage() {
  return (
    <Suspense fallback={null}>
      <ArtistDetailContent />
    </Suspense>
  );
}
