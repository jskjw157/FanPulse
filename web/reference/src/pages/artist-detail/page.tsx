import { useState } from 'react';
import { Link } from 'react-router-dom';

export default function ArtistDetail() {
  const [isFollowing, setIsFollowing] = useState(false);
  const [activeTab, setActiveTab] = useState('overview');

  const artist = {
    name: 'BTS',
    image: 'https://readdy.ai/api/search-image?query=BTS%20kpop%20group%20professional%20photo%2C%20vibrant%20stage%20lighting%2C%20dynamic%20performance%20shot%2C%20high%20quality%20photography%2C%20energetic%20atmosphere%2C%20purple%20and%20blue%20lighting%2C%20concert%20stage%20background&width=800&height=600&seq=artist001&orientation=landscape',
    followers: '2.5M',
    votes: '1.2M',
    rank: 1,
    description: '방탄소년단(BTS)은 대한민국의 보이 그룹으로, 2013년 6월 13일에 데뷔했습니다. 빅히트 뮤직 소속이며, 전 세계적으로 가장 성공한 K-POP 그룹 중 하나입니다.',
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
      title: 'BTS 새 앨범 발매 예정',
      date: '2024.12.10',
      image: 'https://readdy.ai/api/search-image?query=BTS%20new%20album%20announcement%2C%20professional%20press%20photo%2C%20modern%20studio%20setting%2C%20album%20cover%20concept%2C%20high%20quality%20photography%2C%20purple%20and%20blue%20theme&width=400&height=300&seq=news003&orientation=landscape'
    },
    {
      id: 2,
      title: 'BTS 월드투어 일정 공개',
      date: '2024.12.08',
      image: 'https://readdy.ai/api/search-image?query=BTS%20world%20tour%20announcement%2C%20concert%20stage%20setup%2C%20spectacular%20lighting%2C%20professional%20photography%2C%20global%20tour%20concept&width=400&height=300&seq=news004&orientation=landscape'
    }
  ];

  const upcomingEvents = [
    {
      id: 1,
      title: 'BTS World Tour Seoul',
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
    <div className="min-h-screen bg-white pb-20">
      {/* Header */}
      <header className="fixed top-0 left-0 right-0 bg-white/95 backdrop-blur-sm border-b border-gray-200 z-50">
        <div className="px-4 py-3 flex items-center justify-between">
          <Link to="/chart" className="w-9 h-9 flex items-center justify-center">
            <i className="ri-arrow-left-line text-xl text-gray-900"></i>
          </Link>
          <h1 className="text-base font-bold text-gray-900">아티스트</h1>
          <button className="w-9 h-9 flex items-center justify-center">
            <i className="ri-share-line text-xl text-gray-700"></i>
          </button>
        </div>
      </header>

      {/* Content */}
      <div className="pt-14">
        {/* Hero Section */}
        <div className="relative">
          <div className="h-64">
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
            className={`flex-1 py-3 rounded-full font-medium text-sm ${
              isFollowing
                ? 'bg-gray-100 text-gray-700'
                : 'bg-gradient-to-r from-purple-600 to-pink-600 text-white'
            }`}
          >
            {isFollowing ? '팔로잉' : '팔로우'}
          </button>
          <Link 
            to="/voting"
            className="flex-1 bg-purple-100 text-purple-700 py-3 rounded-full font-medium text-sm text-center"
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
        <div className="px-4 py-2 border-b border-gray-200">
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
        <div className="px-4 py-4">
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
                  to={`/news-detail?id=${news.id}`}
                  className="flex gap-3 bg-gray-50 rounded-2xl p-3"
                >
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
                    to="/concert"
                    className="block w-full mt-3 bg-gradient-to-r from-purple-600 to-pink-600 text-white text-sm font-medium py-2 rounded-full text-center"
                  >
                    티켓 예매
                  </Link>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Bottom Navigation */}
      <nav className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 z-50">
        <div className="grid grid-cols-5 h-16">
          <Link to="/" className="flex flex-col items-center justify-center text-gray-500">
            <i className="ri-home-5-line text-xl"></i>
            <span className="text-xs mt-1">Home</span>
          </Link>
          <Link to="/community" className="flex flex-col items-center justify-center text-gray-500">
            <i className="ri-chat-3-line text-xl"></i>
            <span className="text-xs mt-1">Community</span>
          </Link>
          <Link to="/live" className="flex flex-col items-center justify-center text-gray-500">
            <i className="ri-live-line text-xl"></i>
            <span className="text-xs mt-1">Live</span>
          </Link>
          <Link to="/voting" className="flex flex-col items-center justify-center text-gray-500">
            <i className="ri-trophy-line text-xl"></i>
            <span className="text-xs mt-1">Voting</span>
          </Link>
          <Link to="/mypage" className="flex flex-col items-center justify-center text-gray-500">
            <i className="ri-user-line text-xl"></i>
            <span className="text-xs mt-1">My</span>
          </Link>
        </div>
      </nav>
    </div>
  );
}
