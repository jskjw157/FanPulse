"use client";

import PageWrapper from "@/components/layout/PageWrapper";
import Link from "next/link";
import { useState } from "react";

export default function SearchPage() {
  const [searchQuery, setSearchQuery] = useState('');
  const [activeTab, setActiveTab] = useState('all');

  const recentSearches = ['BTS', 'BLACKPINK', '콘서트', 'NewJeans'];
  const popularSearches = ['BTS 새 앨범', 'BLACKPINK 투어', 'SEVENTEEN', 'NewJeans 뮤비'];

  const searchResults = {
    artists: [
      {
        id: 1,
        name: 'BTS',
        image: 'https://readdy.ai/api/search-image?query=BTS%20K-pop%20group%20professional%20photo%2C%20seven%20members%2C%20modern%20style%2C%20high%20quality%20photography%2C%20purple%20theme%2C%20elegant%20composition&width=200&height=200&seq=search001&orientation=squarish',
        followers: '1.2M'
      },
      {
        id: 2,
        name: 'BLACKPINK',
        image: 'https://readdy.ai/api/search-image?query=BLACKPINK%20K-pop%20girl%20group%20professional%20photo%2C%20four%20members%2C%20glamorous%20style%2C%20high%20quality%20photography%2C%20pink%20and%20black%20theme&width=200&height=200&seq=search002&orientation=squarish',
        followers: '980K'
      }
    ],
    posts: [
      {
        id: 1,
        author: 'ARMY_Forever',
        content: 'BTS 새 앨범 티저 영상 보셨나요? 진짜 너무 기대돼요! 💜',
        image: 'https://readdy.ai/api/search-image?query=BTS%20comeback%20teaser%20concept%2C%20professional%20photography%2C%20purple%20theme%2C%20modern%20aesthetic%2C%20high%20quality%2C%20artistic%20composition&width=300&height=200&seq=search003&orientation=landscape',
        likes: 1234,
        time: '2시간 전'
      },
      {
        id: 2,
        author: 'Blink_Girl',
        content: 'BLACKPINK 월드투어 티켓 예매 성공했어요! 너무 설레요 ㅠㅠ',
        image: 'https://readdy.ai/api/search-image?query=BLACKPINK%20world%20tour%20concert%20stage%2C%20spectacular%20lighting%2C%20pink%20and%20black%20theme%2C%20professional%20concert%20photography&width=300&height=200&seq=search004&orientation=landscape',
        likes: 856,
        time: '5시간 전'
      }
    ],
    news: [
      {
        id: 1,
        title: 'BTS 새 앨범 발매 예정',
        category: '뉴스',
        date: '2024.12.10',
        image: 'https://readdy.ai/api/search-image?query=BTS%20new%20album%20announcement%2C%20professional%20press%20photo%2C%20modern%20studio%20setting%2C%20album%20cover%20concept%2C%20high%20quality%20photography%2C%20purple%20theme&width=300&height=200&seq=search005&orientation=landscape'
      },
      {
        id: 2,
        title: 'BLACKPINK 월드투어 추가 공연',
        category: '공연',
        date: '2024.12.09',
        image: 'https://readdy.ai/api/search-image?query=BLACKPINK%20world%20tour%20concert%20announcement%2C%20glamorous%20stage%20setup%2C%20pink%20and%20black%20theme%2C%20professional%20concert%20photography&width=300&height=200&seq=search006&orientation=landscape'
      }
    ],
    concerts: [
      {
        id: 1,
        title: 'BTS World Tour 2025',
        date: '2025.03.15',
        venue: '잠실 올림픽 주경기장',
        image: 'https://readdy.ai/api/search-image?query=BTS%20world%20tour%20concert%20poster%20design%2C%20professional%20layout%2C%20purple%20and%20blue%20gradient%2C%20modern%20typography%2C%20high%20quality%20graphics&width=300&height=400&seq=search007&orientation=portrait'
      },
      {
        id: 2,
        title: 'BLACKPINK BORN PINK TOUR',
        date: '2025.04.20',
        venue: '고척 스카이돔',
        image: 'https://readdy.ai/api/search-image?query=BLACKPINK%20concert%20poster%20design%2C%20glamorous%20style%2C%20pink%20and%20black%20gradient%2C%20modern%20typography%2C%20high%20quality%20graphics&width=300&height=400&seq=search008&orientation=portrait'
      }
    ]
  };

  const hasSearchQuery = searchQuery.length > 0;

  return (
    <>
      <header className="fixed top-0 left-0 right-0 bg-white border-b border-gray-200 z-50 lg:static lg:z-auto lg:border-none lg:bg-transparent lg:pt-8 lg:pb-4">
        <div className="px-4 py-3 lg:px-0 lg:max-w-4xl lg:mx-auto">
          <div className="flex items-center gap-2">
            <Link href="/" className="w-9 h-9 flex items-center justify-center lg:hidden">
              <i className="ri-arrow-left-line text-xl text-gray-900"></i>
            </Link>
            <div className="flex-1 relative">
              <input 
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="아티스트, 게시글, 뉴스 검색..."
                className="w-full bg-gray-100 rounded-full pl-10 pr-10 py-2.5 text-sm border-none focus:outline-none focus:ring-2 focus:ring-purple-600 lg:py-3 lg:text-base"
              />
              <i className="ri-search-line absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 lg:left-4"></i>
              {searchQuery && (
                <button 
                  onClick={() => setSearchQuery('')}
                  className="absolute right-3 top-1/2 -translate-y-1/2"
                >
                  <i className="ri-close-circle-fill text-gray-400"></i>
                </button>
              )}
            </div>
          </div>
        </div>

        {/* Tabs */}
        {hasSearchQuery && (
          <div className="px-4 pb-2 flex gap-2 overflow-x-auto scrollbar-hide lg:max-w-4xl lg:mx-auto lg:px-0 lg:pt-4">
            {['all', 'artists', 'posts', 'news', 'concerts'].map(tab => (
              <button
                key={tab}
                onClick={() => setActiveTab(tab)}
                className={`px-4 py-1.5 rounded-full text-sm font-medium whitespace-nowrap transition-colors ${
                  activeTab === tab
                    ? 'bg-purple-600 text-white'
                    : 'bg-gray-100 text-gray-600'
                }`}
              >
                {tab === 'all' && '전체'}
                {tab === 'artists' && '아티스트'}
                {tab === 'posts' && '게시글'}
                {tab === 'news' && '뉴스'}
                {tab === 'concerts' && '콘서트'}
              </button>
            ))}
          </div>
        )}
      </header>

      <PageWrapper className={hasSearchQuery ? 'pt-32' : 'pt-16'}>
        {!hasSearchQuery ? (
          <>
            {/* Recent Searches */}
            <div className="px-4 py-4">
              <div className="flex items-center justify-between mb-3">
                <h2 className="text-sm font-bold text-gray-900">최근 검색어</h2>
                <button className="text-xs text-gray-500 hover:text-purple-600">전체 삭제</button>
              </div>
              <div className="flex flex-wrap gap-2">
                {recentSearches.map((term, index) => (
                  <button
                    key={index}
                    onClick={() => setSearchQuery(term)}
                    className="flex items-center gap-2 bg-gray-100 px-3 py-2 rounded-full text-sm text-gray-700 hover:bg-gray-200 transition-colors"
                  >
                    <i className="ri-time-line text-gray-400"></i>
                    {term}
                    <i className="ri-close-line text-gray-400"></i>
                  </button>
                ))}
              </div>
            </div>

            {/* Popular Searches */}
            <div className="px-4 py-4 bg-gray-50">
              <h2 className="text-sm font-bold text-gray-900 mb-3">인기 검색어</h2>
              <div className="space-y-2">
                {popularSearches.map((term, index) => (
                  <button
                    key={index}
                    onClick={() => setSearchQuery(term)}
                    className="flex items-center gap-3 w-full text-left py-2 hover:bg-gray-100 rounded-lg px-2 transition-colors"
                  >
                    <span className="text-purple-600 font-bold text-sm w-5">{index + 1}</span>
                    <span className="text-sm text-gray-900">{term}</span>
                  </button>
                ))}
              </div>
            </div>
          </>
        ) : (
          <>
            {/* Artists Results */}
            {(activeTab === 'all' || activeTab === 'artists') && (
              <div className="px-4 py-4">
                <h2 className="text-sm font-bold text-gray-900 mb-3">아티스트</h2>
                <div className="space-y-3">
                  {searchResults.artists.map(artist => (
                    <Link
                      key={artist.id}
                      href={`/artist-detail?id=${artist.id}`}
                      className="flex items-center gap-3 hover:bg-gray-50 p-2 rounded-xl transition-colors"
                    >
                      {/* eslint-disable-next-line @next/next/no-img-element */}
                      <img
                        src={artist.image}
                        alt={artist.name}
                        className="w-14 h-14 rounded-full object-cover"
                      />
                      <div className="flex-1">
                        <h3 className="font-bold text-gray-900">{artist.name}</h3>
                        <p className="text-xs text-gray-500">팔로워 {artist.followers}</p>
                      </div>
                      <button className="px-4 py-1.5 border-2 border-purple-600 text-purple-600 rounded-full text-sm font-medium hover:bg-purple-50 transition-colors">
                        팔로우
                      </button>
                    </Link>
                  ))}
                </div>
              </div>
            )}

            {/* Posts Results */}
            {(activeTab === 'all' || activeTab === 'posts') && (
              <div className="px-4 py-4 bg-gray-50">
                <h2 className="text-sm font-bold text-gray-900 mb-3">게시글</h2>
                <div className="space-y-3">
                  {searchResults.posts.map(post => (
                    <Link
                      key={post.id}
                      href={`/post-detail?id=${post.id}`}
                      className="flex gap-3 bg-white rounded-xl p-3 shadow-sm hover:shadow-md transition-shadow"
                    >
                      {/* eslint-disable-next-line @next/next/no-img-element */}
                      <img
                        src={post.image}
                        alt={post.content}
                        className="w-24 h-20 rounded-lg object-cover object-top flex-shrink-0"
                      />
                      <div className="flex-1 min-w-0">
                        <p className="text-xs text-purple-600 font-medium mb-1">{post.author}</p>
                        <p className="text-sm text-gray-900 line-clamp-2 mb-2">{post.content}</p>
                        <div className="flex items-center gap-3 text-xs text-gray-500">
                          <span className="flex items-center gap-1">
                            <i className="ri-heart-line"></i>
                            {post.likes}
                          </span>
                          <span>{post.time}</span>
                        </div>
                      </div>
                    </Link>
                  ))}
                </div>
              </div>
            )}

            {/* News Results */}
            {(activeTab === 'all' || activeTab === 'news') && (
              <div className="px-4 py-4">
                <h2 className="text-sm font-bold text-gray-900 mb-3">뉴스</h2>
                <div className="space-y-3">
                  {searchResults.news.map(news => (
                    <Link
                      key={news.id}
                      href={`/news-detail?id=${news.id}`}
                      className="flex gap-3 hover:bg-gray-50 p-2 rounded-xl transition-colors"
                    >
                      {/* eslint-disable-next-line @next/next/no-img-element */}
                      <img
                        src={news.image}
                        alt={news.title}
                        className="w-24 h-20 rounded-lg object-cover object-top flex-shrink-0"
                      />
                      <div className="flex-1">
                        <span className="text-xs text-purple-600 font-medium">{news.category}</span>
                        <h3 className="text-sm font-medium text-gray-900 mt-1 line-clamp-2">
                          {news.title}
                        </h3>
                        <p className="text-xs text-gray-500 mt-1">{news.date}</p>
                      </div>
                    </Link>
                  ))}
                </div>
              </div>
            )}

            {/* Concerts Results */}
            {(activeTab === 'all' || activeTab === 'concerts') && (
              <div className="px-4 py-4 bg-gray-50">
                <h2 className="text-sm font-bold text-gray-900 mb-3">콘서트</h2>
                <div className="grid grid-cols-2 gap-3">
                  {searchResults.concerts.map(concert => (
                    <Link
                      key={concert.id}
                      href={`/concert-detail?id=${concert.id}`}
                      className="bg-white rounded-xl overflow-hidden shadow-sm hover:shadow-md transition-shadow"
                    >
                      {/* eslint-disable-next-line @next/next/no-img-element */}
                      <img
                        src={concert.image}
                        alt={concert.title}
                        className="w-full h-48 object-cover object-top"
                      />
                      <div className="p-3">
                        <h3 className="text-sm font-bold text-gray-900 line-clamp-1 mb-1">
                          {concert.title}
                        </h3>
                        <p className="text-xs text-gray-600 mb-1">{concert.date}</p>
                        <p className="text-xs text-gray-500 line-clamp-1">{concert.venue}</p>
                      </div>
                    </Link>
                  ))}
                </div>
              </div>
            )}
          </>
        )}
      </PageWrapper>
    </>
  );
}
