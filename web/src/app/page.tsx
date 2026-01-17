"use client";

import Image from 'next/image';
import Link from 'next/link';

export default function Home() {
  const trendingArtists = [
    {
      id: 1,
      name: 'BTS',
      image: 'https://readdy.ai/api/search-image?query=BTS%20kpop%20group%20professional%20photo%2C%20vibrant%20stage%20lighting%2C%20dynamic%20performance%20shot%2C%20high%20quality%20photography%2C%20energetic%20atmosphere%2C%20purple%20and%20blue%20lighting%2C%20concert%20stage%20background&width=400&height=400&seq=bts001&orientation=squarish',
      fans: '2.5M',
      votes: '1.2M'
    },
    {
      id: 2,
      name: 'BLACKPINK',
      image: 'https://readdy.ai/api/search-image?query=BLACKPINK%20kpop%20girl%20group%20professional%20photo%2C%20glamorous%20stage%20lighting%2C%20powerful%20performance%20shot%2C%20high%20quality%20photography%2C%20pink%20and%20black%20theme%2C%20concert%20stage%20background&width=400&height=400&seq=bp001&orientation=squarish',
      fans: '2.1M',
      votes: '980K'
    },
    {
      id: 3,
      name: 'SEVENTEEN',
      image: 'https://readdy.ai/api/search-image?query=SEVENTEEN%20kpop%20boy%20group%20professional%20photo%2C%20bright%20stage%20lighting%2C%20synchronized%20performance%20shot%2C%20high%20quality%20photography%2C%20energetic%20vibe%2C%20colorful%20stage%20background&width=400&height=400&seq=svt001&orientation=squarish',
      fans: '1.8M',
      votes: '850K'
    },
    {
      id: 4,
      name: 'NewJeans',
      image: 'https://readdy.ai/api/search-image?query=NewJeans%20kpop%20girl%20group%20professional%20photo%2C%20fresh%20youthful%20concept%2C%20modern%20stage%20lighting%2C%20high%20quality%20photography%2C%20pastel%20colors%2C%20trendy%20aesthetic%20background&width=400&height=400&seq=nj001&orientation=squarish',
      fans: '1.5M',
      votes: '720K'
    }
  ];

  const upcomingEvents = [
    {
      id: 1,
      title: 'MAMA Awards 2024',
      date: '2024.12.15',
      type: 'Award Show',
      image: 'https://readdy.ai/api/search-image?query=KPOP%20award%20show%20stage%20setup%2C%20grand%20ceremony%20hall%2C%20golden%20trophy%20display%2C%20professional%20event%20photography%2C%20luxurious%20red%20carpet%2C%20spotlights%20and%20stage%20lights%2C%20elegant%20atmosphere&width=600&height=300&seq=mama001&orientation=landscape'
    },
    {
      id: 2,
      title: 'BTS World Tour Seoul',
      date: '2024.12.20',
      type: 'Concert',
      image: 'https://readdy.ai/api/search-image?query=KPOP%20concert%20stadium%20stage%2C%20massive%20LED%20screens%2C%20purple%20lighting%20effects%2C%20professional%20concert%20photography%2C%20huge%20crowd%20atmosphere%2C%20spectacular%20stage%20design&width=600&height=300&seq=concert001&orientation=landscape'
    }
  ];

  const liveNow = [
    {
      id: 1,
      title: 'Music Bank Live',
      viewers: '125K',
      thumbnail: 'https://readdy.ai/api/search-image?query=KPOP%20music%20show%20broadcast%20stage%2C%20colorful%20LED%20background%2C%20professional%20TV%20studio%20lighting%2C%20modern%20stage%20design%2C%20vibrant%20atmosphere%2C%20live%20broadcast%20setup&width=400&height=300&seq=live001&orientation=landscape'
    },
    {
      id: 2,
      title: 'Fan Meeting Live',
      viewers: '89K',
      thumbnail: 'https://readdy.ai/api/search-image?query=KPOP%20fan%20meeting%20event%2C%20intimate%20stage%20setup%2C%20warm%20lighting%2C%20cozy%20atmosphere%2C%20professional%20event%20photography%2C%20fans%20interaction%20space&width=400&height=300&seq=live002&orientation=landscape'
    }
  ];

  const newsItems = [
    {
      id: 1,
      title: 'BTS 새 앨범 발매 예정',
      category: '뉴스',
      time: '1시간 전'
    },
    {
      id: 2,
      title: 'BLACKPINK 월드투어 추가 공연',
      category: '공연',
      time: '3시간 전'
    }
  ];

  const popularPosts = [
    {
      id: 1,
      title: 'BTS 콘서트 후기 - 정말 최고였어요!',
      author: '아미4ever',
      likes: 2845,
      comments: 342,
      image: 'https://readdy.ai/api/search-image?query=KPOP%20concert%20crowd%20with%20purple%20ocean%20light%20sticks%2C%20enthusiastic%20fans%2C%20energetic%20atmosphere%2C%20professional%20concert%20photography%2C%20BTS%20concert%20vibes%2C%20spectacular%20stage%20in%20background&width=300&height=200&seq=post001&orientation=landscape',
      time: '2시간 전'
    },
    {
      id: 2,
      title: 'NewJeans 신곡 뮤비 분석',
      author: '토끼덕후',
      likes: 1923,
      comments: 218,
      image: 'https://readdy.ai/api/search-image?query=KPOP%20music%20video%20screenshot%20analysis%2C%20modern%20aesthetic%2C%20pastel%20colors%2C%20NewJeans%20style%2C%20professional%20photography%2C%20trendy%20youthful%20concept&width=300&height=200&seq=post002&orientation=landscape',
      time: '5시간 전'
    },
    {
      id: 3,
      title: '이번주 음악방송 1위 예측',
      author: 'K팝매니아',
      likes: 1567,
      comments: 189,
      image: 'https://readdy.ai/api/search-image?query=KPOP%20music%20show%20trophy%20and%20stage%2C%20golden%20award%20trophy%2C%20professional%20broadcast%20studio%2C%20colorful%20LED%20screens%2C%20winner%20announcement%20moment&width=300&height=200&seq=post003&orientation=landscape',
      time: '8시간 전'
    }
  ];

  const chartRankings = [
    {
      rank: 1,
      prevRank: 1,
      change: 0,
      title: 'Super Shy',
      artist: 'NewJeans',
      image: 'https://readdy.ai/api/search-image?query=KPOP%20album%20cover%20NewJeans%20Super%20Shy%2C%20modern%20minimalist%20design%2C%20pastel%20colors%2C%20professional%20album%20artwork%2C%20clean%20aesthetic%2C%20youthful%20vibe&width=200&height=200&seq=chart001&orientation=squarish'
    },
    {
      rank: 2,
      prevRank: 3,
      change: 1,
      title: 'Seven',
      artist: 'Jungkook (BTS)',
      image: 'https://readdy.ai/api/search-image?query=KPOP%20album%20cover%20Jungkook%20Seven%2C%20bold%20typography%2C%20vibrant%20colors%2C%20professional%20album%20artwork%2C%20modern%20design%2C%20energetic%20feel&width=200&height=200&seq=chart002&orientation=squarish'
    },
    {
      rank: 3,
      prevRank: 2,
      change: -1,
      title: 'Queencard',
      artist: '(G)I-DLE',
      image: 'https://readdy.ai/api/search-image?query=KPOP%20album%20cover%20GIDLE%20Queencard%2C%20fierce%20concept%2C%20bold%20colors%2C%20professional%20album%20artwork%2C%20powerful%20aesthetic%2C%20glamorous%20design&width=200&height=200&seq=chart003&orientation=squarish'
    },
    {
      rank: 4,
      prevRank: 5,
      change: 1,
      title: 'Spicy',
      artist: 'aespa',
      image: 'https://readdy.ai/api/search-image?query=KPOP%20album%20cover%20aespa%20Spicy%2C%20futuristic%20design%2C%20vibrant%20red%20colors%2C%20professional%20album%20artwork%2C%20cyber%20aesthetic%2C%20bold%20typography&width=200&height=200&seq=chart004&orientation=squarish'
    },
    {
      rank: 5,
      prevRank: 4,
      change: -1,
      title: 'Kitsch',
      artist: 'IVE',
      image: 'https://readdy.ai/api/search-image?query=KPOP%20album%20cover%20IVE%20Kitsch%2C%20retro%20pop%20art%20style%2C%20colorful%20design%2C%20professional%20album%20artwork%2C%20playful%20aesthetic%2C%20vintage%20vibe&width=200&height=200&seq=chart005&orientation=squarish'
    }
  ];

  return (
    <div className="max-w-7xl mx-auto px-4 lg:px-8 py-4">
      {/* Hero Banner */}
      <div className="relative rounded-2xl overflow-hidden h-48 md:h-64 lg:h-96">
        <Image
          src="https://readdy.ai/api/search-image?query=KPOP%20festival%20main%20stage%2C%20spectacular%20light%20show%2C%20vibrant%20purple%20and%20pink%20gradient%20lighting%2C%20professional%20concert%20photography%2C%20energetic%20atmosphere%2C%20massive%20LED%20screens%2C%20dynamic%20stage%20effects&width=800&height=400&seq=hero001&orientation=landscape"
          alt="Hero Banner"
          fill
          className="object-cover object-top"
          unoptimized
        />
        <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent flex flex-col justify-end p-5 md:p-8 lg:p-12">
          <h1 className="text-white text-2xl md:text-3xl lg:text-5xl font-bold">Welcome to FanPulse</h1>
          <p className="text-white/90 text-sm md:text-base lg:text-xl mt-2">글로벌 K-POP 팬들의 인터랙티브 플랫폼</p>
        </div>
      </div>

      {/* News Ticker */}
      <div className="mt-6 bg-white rounded-2xl p-4 lg:p-6 shadow-sm">
        <div className="flex items-center gap-2 mb-3">
          <i className="ri-newspaper-line text-purple-600 text-xl"></i>
          <h3 className="font-bold text-gray-900 text-lg">최신 뉴스</h3>
        </div>
        <div className="space-y-2">
          {newsItems.map(news => (
            <Link 
              key={news.id} 
              href={`/news-detail?id=${news.id}`}
              className="flex items-center justify-between py-2 border-b border-gray-100 last:border-0 hover:bg-gray-50 px-2 rounded-lg transition-colors"
            >
              <div className="flex-1">
                <span className="text-xs text-purple-600 font-medium mr-2">{news.category}</span>
                <span className="text-sm text-gray-900">{news.title}</span>
              </div>
              <span className="text-xs text-gray-400">{news.time}</span>
            </Link>
          ))}
        </div>
      </div>

      {/* Live Now Section */}
      <div className="mt-8">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl lg:text-2xl font-bold text-gray-900">🔴 Live Now</h2>
          <Link href="/live" className="text-sm lg:text-base text-purple-600 font-medium hover:text-purple-700">View All</Link>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-2 gap-4 lg:gap-6">
          {liveNow.map(live => (
            <Link key={live.id} href="/live-detail" className="block group">
              <div className="relative rounded-xl overflow-hidden h-48 lg:h-64">
                <Image
                  src={live.thumbnail}
                  alt={live.title}
                  fill
                  className="object-cover object-top group-hover:scale-105 transition-transform duration-300"
                  unoptimized
                />
                <div className="absolute top-3 left-3 bg-red-600 text-white text-xs lg:text-sm px-3 py-1 rounded-full font-medium flex items-center gap-1">
                  <span className="w-2 h-2 bg-white rounded-full animate-pulse"></span>
                  LIVE
                </div>
                <div className="absolute bottom-3 right-3 bg-black/70 text-white text-xs lg:text-sm px-3 py-1 rounded-full">
                  <i className="ri-eye-line text-xs mr-1"></i>{live.viewers}
                </div>
              </div>
              <p className="text-sm lg:text-base font-medium text-gray-900 mt-3">{live.title}</p>
            </Link>
          ))}
        </div>
      </div>

      {/* Popular Posts Section */}
      <div className="mt-8">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl lg:text-2xl font-bold text-gray-900">🔥 인기 게시글</h2>
          <Link href="/community" className="text-sm lg:text-base text-purple-600 font-medium hover:text-purple-700">더보기</Link>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {popularPosts.map(post => (
            <Link 
              key={post.id} 
              href={`/post-detail?id=${post.id}`}
              className="block bg-white rounded-2xl overflow-hidden shadow-sm hover:shadow-md transition-shadow"
            >
              <div className="flex gap-3 p-4">
                <div className="relative w-24 h-20 rounded-xl overflow-hidden flex-shrink-0">
                  <Image
                    src={post.image}
                    alt={post.title}
                    fill
                    className="object-cover object-top"
                    unoptimized
                  />
                </div>
                <div className="flex-1 min-w-0">
                  <h3 className="font-bold text-gray-900 text-sm line-clamp-2">{post.title}</h3>
                  <div className="flex items-center gap-3 mt-2 text-xs text-gray-500">
                    <span>{post.author}</span>
                    <span className="flex items-center gap-1">
                      <i className="ri-heart-fill text-pink-500"></i>
                      {post.likes.toLocaleString()}
                    </span>
                    <span className="flex items-center gap-1">
                      <i className="ri-chat-3-line"></i>
                      {post.comments}
                    </span>
                  </div>
                </div>
              </div>
            </Link>
          ))}
        </div>
      </div>

      {/* Chart Rankings Section */}
      <div className="mt-8">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl lg:text-2xl font-bold text-gray-900">📊 실시간 차트</h2>
          <Link href="/chart" className="text-sm lg:text-base text-purple-600 font-medium hover:text-purple-700">전체보기</Link>
        </div>
        <div className="bg-white rounded-2xl p-4 lg:p-6 shadow-sm">
          <div className="space-y-3">
            {chartRankings.map((item, index) => (
              <Link 
                key={item.rank} 
                href={`/artist-detail?artist=${item.artist}`}
                className="flex items-center gap-3 hover:bg-gray-50 p-2 rounded-lg transition-colors"
              >
                <div className={`w-8 h-8 flex items-center justify-center font-bold text-base flex-shrink-0 ${
                  index === 0 ? 'text-yellow-500' :
                  index === 1 ? 'text-gray-400' :
                  index === 2 ? 'text-orange-600' :
                  'text-gray-600'
                }`}>
                  {item.rank}
                </div>

                <div className="relative w-14 h-14 rounded-lg overflow-hidden flex-shrink-0">
                  <Image
                    src={item.image}
                    alt={item.artist}
                    fill
                    className="object-cover object-top"
                    unoptimized
                  />
                </div>

                <div className="flex-1 min-w-0">
                  <h3 className="font-bold text-gray-900 text-sm lg:text-base truncate">{item.title}</h3>
                  <p className="text-xs lg:text-sm text-gray-600 truncate">{item.artist}</p>
                </div>

                <div className={`flex items-center gap-1 text-sm font-medium flex-shrink-0 ${
                  item.change > 0 ? 'text-red-500' :
                  item.change < 0 ? 'text-blue-500' :
                  'text-gray-400'
                }`}>
                  {item.change > 0 && <i className="ri-arrow-up-line"></i>}
                  {item.change < 0 && <i className="ri-arrow-down-line"></i>}
                  {item.change === 0 && <i className="ri-subtract-line"></i>}
                  <span className="w-5 text-right">{Math.abs(item.change) || '-'}</span>
                </div>
              </Link>
            ))}
          </div>
        </div>
      </div>

      {/* Trending Artists */}
      <div className="mt-8">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl lg:text-2xl font-bold text-gray-900">Best Male Group 2024</h2>
          <Link href="/voting" className="text-sm lg:text-base text-purple-600 font-medium hover:text-purple-700">Vote Now</Link>
        </div>
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 lg:gap-6">
          {trendingArtists.map(artist => (
            <div key={artist.id} className="bg-white rounded-2xl overflow-hidden shadow-sm hover:shadow-md transition-shadow">
              <div className="relative h-48 lg:h-56">
                <Image
                  src={artist.image}
                  alt={artist.name}
                  fill
                  className="object-cover object-top"
                  unoptimized
                />
                <div className="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent"></div>
              </div>
              <div className="p-4">
                <h3 className="font-bold text-gray-900 text-base lg:text-lg">{artist.name}</h3>
                <div className="flex items-center justify-between mt-2 text-xs lg:text-sm text-gray-600">
                  <span><i className="ri-user-line mr-1"></i>{artist.fans}</span>
                  <span><i className="ri-heart-fill text-pink-500 mr-1"></i>{artist.votes}</span>
                </div>
                <button className="w-full mt-3 bg-gradient-to-r from-purple-600 to-pink-600 text-white text-sm lg:text-base font-medium py-2.5 rounded-full hover:shadow-lg transition-shadow">
                  Vote
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Upcoming Events */}
      <div className="mt-8">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl lg:text-2xl font-bold text-gray-900">📅 Upcoming Events</h2>
          <Link href="/concert" className="text-sm lg:text-base text-purple-600 font-medium hover:text-purple-700">See All</Link>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 lg:gap-6">
          {upcomingEvents.map(event => (
            <Link key={event.id} href="/concert" className="block bg-white rounded-2xl overflow-hidden shadow-sm hover:shadow-md transition-shadow">
              <div className="relative h-40 lg:h-48">
                <Image
                  src={event.image}
                  alt={event.title}
                  fill
                  className="object-cover object-top"
                  unoptimized
                />
              </div>
              <div className="p-4 lg:p-6">
                <div className="flex items-center gap-2 mb-2">
                  <span className="bg-purple-100 text-purple-700 text-xs lg:text-sm px-3 py-1 rounded-full font-medium">{event.type}</span>
                  <span className="text-xs lg:text-sm text-gray-500">{event.date}</span>
                </div>
                <h3 className="font-bold text-gray-900 text-base lg:text-lg">{event.title}</h3>
                <button className="w-full mt-4 border-2 border-purple-600 text-purple-600 text-sm lg:text-base font-medium py-2.5 rounded-full hover:bg-purple-50 transition-colors">
                  Get Tickets
                </button>
              </div>
            </Link>
          ))}
        </div>
      </div>

      {/* Quick Actions */}
      <div className="mt-8 mb-6 grid grid-cols-3 gap-4 lg:gap-6">
        <Link href="/ads" className="bg-gradient-to-br from-yellow-400 to-orange-500 rounded-2xl p-4 lg:p-6 text-center shadow-sm hover:shadow-md transition-shadow">
          <div className="w-10 h-10 lg:w-14 lg:h-14 mx-auto flex items-center justify-center">
            <i className="ri-gift-line text-2xl lg:text-3xl text-white"></i>
          </div>
          <p className="text-white text-sm lg:text-base font-medium mt-2">Earn Rewards</p>
        </Link>
        <Link href="/membership" className="bg-gradient-to-br from-purple-500 to-pink-500 rounded-2xl p-4 lg:p-6 text-center shadow-sm hover:shadow-md transition-shadow">
          <div className="w-10 h-10 lg:w-14 lg:h-14 mx-auto flex items-center justify-center">
            <i className="ri-vip-crown-line text-2xl lg:text-3xl text-white"></i>
          </div>
          <p className="text-white text-sm lg:text-base font-medium mt-2">VIP Club</p>
        </Link>
        <Link href="/community" className="bg-gradient-to-br from-blue-500 to-cyan-500 rounded-2xl p-4 lg:p-6 text-center shadow-sm hover:shadow-md transition-shadow">
          <div className="w-10 h-10 lg:w-14 lg:h-14 mx-auto flex items-center justify-center">
            <i className="ri-chat-3-line text-2xl lg:text-3xl text-white"></i>
          </div>
          <p className="text-white text-sm lg:text-base font-medium mt-2">Community</p>
        </Link>
      </div>
    </div>
  );
}