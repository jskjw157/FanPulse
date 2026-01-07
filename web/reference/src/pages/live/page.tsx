import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

export default function Live() {
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const navigate = useNavigate();

  const liveStreams = [
    {
      id: 1,
      title: 'NewJeans 컴백 쇼케이스',
      artist: 'NewJeans Official',
      viewers: '24.5K',
      thumbnail: 'https://readdy.ai/api/search-image?query=K-pop%20girl%20group%20NewJeans%20performing%20live%20on%20stage%20with%20dynamic%20lighting%2C%20professional%20concert%20photography%2C%20energetic%20performance%2C%20vibrant%20stage%20lights%2C%20multiple%20members%20singing%20and%20dancing%2C%20high-quality%20broadcast%20camera%20angle%2C%20modern%20stage%20design%2C%20purple%20and%20pink%20lighting%20effects%2C%20professional%20live%20streaming%20quality%2C%204K%20resolution%2C%20cinematic%20composition&width=343&height=193&seq=live001&orientation=landscape',
      isLive: true
    },
    {
      id: 2,
      title: 'BTS Fan Meeting Special',
      artist: 'BTS',
      viewers: '89.2K',
      thumbnail: 'https://readdy.ai/api/search-image?query=KPOP%20fan%20meeting%20event%2C%20intimate%20stage%20setup%2C%20warm%20purple%20lighting%2C%20cozy%20atmosphere%2C%20professional%20event%20photography%2C%20fans%20interaction%20space&width=600&height=400&seq=live102&orientation=landscape',
      duration: '1:45:20'
    },
    {
      id: 3,
      title: 'BLACKPINK Behind The Scenes',
      artist: 'BLACKPINK',
      viewers: '67.8K',
      thumbnail: 'https://readdy.ai/api/search-image?query=KPOP%20behind%20the%20scenes%20photoshoot%2C%20backstage%20atmosphere%2C%20pink%20and%20black%20theme%2C%20professional%20photography%2C%20glamorous%20setting%2C%20makeup%20room&width=600&height=400&seq=live103&orientation=landscape',
      duration: '0:58:12'
    }
  ];

  const concerts = [
    {
      id: 1,
      title: 'NewJeans 1st World Tour',
      date: 'Jan 10, 2025',
      location: 'Gocheok Sky Dome',
      image: 'https://readdy.ai/api/search-image?query=KPOP%20girl%20group%20concert%20stage%2C%20fresh%20pastel%20lighting%2C%20modern%20stage%20design%2C%20professional%20concert%20photography%2C%20youthful%20vibrant%20atmosphere%2C%20trendy%20aesthetic&width=400&height=300&seq=concert101&orientation=landscape',
      status: 'On Sale'
    },
    {
      id: 2,
      title: 'TWICE Encore Concert',
      date: 'Jan 18, 2025',
      location: 'KSPO Dome',
      image: 'https://readdy.ai/api/search-image?query=KPOP%20concert%20stage%2C%20colorful%20vibrant%20lighting%2C%20energetic%20atmosphere%2C%20professional%20concert%20photography%2C%20spectacular%20stage%20effects%2C%20LED%20displays&width=400&height=300&seq=concert102&orientation=landscape',
      status: 'Sold Out'
    }
  ];

  const handleGetTickets = (concert: any) => {
    navigate('/concert-detail', { 
      state: { 
        concert: {
          id: concert.id,
          title: concert.title,
          artist: concert.artist || 'K-POP Artist',
          date: concert.date,
          time: '19:00 KST',
          venue: concert.location,
          image: concert.image,
          price: '₩50,000 - ₩150,000',
          description: `${concert.title}의 특별한 라이브 공연에 여러분을 초대합니다. 최고의 무대와 음향으로 잊지 못할 추억을 만들어보세요.`,
          status: concert.status
        }
      } 
    });
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-purple-50 to-pink-50 pb-20 lg:pb-0">
      {/* Header */}
      <header className="fixed top-0 left-0 right-0 bg-gradient-to-r from-purple-600 to-pink-600 z-50">
        <div className="max-w-7xl mx-auto px-4 py-3 flex items-center justify-between">
          <h1 className="text-xl md:text-2xl font-bold text-white" style={{ fontFamily: '"Pacifico", serif' }}>FanPulse</h1>
          <div className="flex items-center gap-2">
            <Link to="/search" className="w-9 h-9 flex items-center justify-center">
              <i className="ri-search-line text-xl text-white"></i>
            </Link>
            <Link to="/notifications" className="w-9 h-9 flex items-center justify-center relative">
              <i className="ri-notification-line text-xl text-white"></i>
              <span className="absolute top-1.5 right-1.5 w-2 h-2 bg-red-500 rounded-full"></span>
            </Link>
            <button 
              onClick={() => setIsMenuOpen(true)}
              className="w-9 h-9 flex items-center justify-center lg:hidden"
            >
              <i className="ri-menu-line text-xl text-white"></i>
            </button>
          </div>
        </div>
      </header>

      {/* Side Menu Modal */}
      {isMenuOpen && (
        <>
          {/* Backdrop */}
          <div 
            className="fixed inset-0 bg-black/50 z-[60]"
            onClick={() => setIsMenuOpen(false)}
          ></div>
          
          {/* Menu Panel */}
          <div className="fixed top-0 right-0 bottom-0 w-72 bg-white z-[70] shadow-2xl animate-slide-in-right">
            {/* Menu Header */}
            <div className="bg-gradient-to-r from-purple-600 to-pink-600 p-4 flex items-center justify-between">
              <h2 className="text-white font-bold text-lg">메뉴</h2>
              <button 
                onClick={() => setIsMenuOpen(false)}
                className="w-8 h-8 flex items-center justify-center"
              >
                <i className="ri-close-line text-2xl text-white"></i>
              </button>
            </div>

            {/* Menu Items */}
            <div className="overflow-y-auto h-[calc(100vh-64px)]">
              <div className="p-4 space-y-1">
                <Link 
                  to="/chart" 
                  onClick={() => setIsMenuOpen(false)}
                  className="flex items-center gap-3 p-3 rounded-xl hover:bg-purple-50 transition-colors"
                >
                  <div className="w-10 h-10 flex items-center justify-center bg-purple-100 rounded-full">
                    <i className="ri-bar-chart-box-line text-xl text-purple-600"></i>
                  </div>
                  <span className="font-medium text-gray-900">차트</span>
                </Link>

                <Link 
                  to="/news-detail" 
                  onClick={() => setIsMenuOpen(false)}
                  className="flex items-center gap-3 p-3 rounded-xl hover:bg-purple-50 transition-colors"
                >
                  <div className="w-10 h-10 flex items-center justify-center bg-indigo-100 rounded-full">
                    <i className="ri-newspaper-line text-xl text-indigo-600"></i>
                  </div>
                  <span className="font-medium text-gray-900">뉴스</span>
                </Link>

                <Link 
                  to="/concert" 
                  onClick={() => setIsMenuOpen(false)}
                  className="flex items-center gap-3 p-3 rounded-xl hover:bg-purple-50 transition-colors"
                >
                  <div className="w-10 h-10 flex items-center justify-center bg-pink-100 rounded-full">
                    <i className="ri-music-line text-xl text-pink-600"></i>
                  </div>
                  <span className="font-medium text-gray-900">콘서트</span>
                </Link>

                <Link 
                  to="/tickets" 
                  onClick={() => setIsMenuOpen(false)}
                  className="flex items-center gap-3 p-3 rounded-xl hover:bg-purple-50 transition-colors"
                >
                  <div className="w-10 h-10 flex items-center justify-center bg-blue-100 rounded-full">
                    <i className="ri-ticket-line text-xl text-blue-600"></i>
                  </div>
                  <span className="font-medium text-gray-900">티켓</span>
                </Link>

                <Link 
                  to="/membership" 
                  onClick={() => setIsMenuOpen(false)}
                  className="flex items-center gap-3 p-3 rounded-xl hover:bg-purple-50 transition-colors"
                >
                  <div className="w-10 h-10 flex items-center justify-center bg-yellow-100 rounded-full">
                    <i className="ri-vip-crown-line text-xl text-yellow-600"></i>
                  </div>
                  <span className="font-medium text-gray-900">멤버십</span>
                </Link>

                <Link 
                  to="/ads" 
                  onClick={() => setIsMenuOpen(false)}
                  className="flex items-center gap-3 p-3 rounded-xl hover:bg-purple-50 transition-colors"
                >
                  <div className="w-10 h-10 flex items-center justify-center bg-orange-100 rounded-full">
                    <i className="ri-gift-line text-xl text-orange-600"></i>
                  </div>
                  <span className="font-medium text-gray-900">리워드</span>
                </Link>

                <Link 
                  to="/favorites" 
                  onClick={() => setIsMenuOpen(false)}
                  className="flex items-center gap-3 p-3 rounded-xl hover:bg-purple-50 transition-colors"
                >
                  <div className="w-10 h-10 flex items-center justify-center bg-red-100 rounded-full">
                    <i className="ri-heart-line text-xl text-red-600"></i>
                  </div>
                  <span className="font-medium text-gray-900">즐겨찾기</span>
                </Link>

                <Link 
                  to="/saved" 
                  onClick={() => setIsMenuOpen(false)}
                  className="flex items-center gap-3 p-3 rounded-xl hover:bg-purple-50 transition-colors"
                >
                  <div className="w-10 h-10 flex items-center justify-center bg-green-100 rounded-full">
                    <i className="ri-bookmark-line text-xl text-green-600"></i>
                  </div>
                  <span className="font-medium text-gray-900">저장됨</span>
                </Link>

                <div className="h-px bg-gray-200 my-3"></div>

                <Link 
                  to="/settings" 
                  onClick={() => setIsMenuOpen(false)}
                  className="flex items-center gap-3 p-3 rounded-xl hover:bg-purple-50 transition-colors"
                >
                  <div className="w-10 h-10 flex items-center justify-center bg-gray-100 rounded-full">
                    <i className="ri-settings-3-line text-xl text-gray-600"></i>
                  </div>
                  <span className="font-medium text-gray-900">설정</span>
                </Link>

                <Link 
                  to="/support" 
                  onClick={() => setIsMenuOpen(false)}
                  className="flex items-center gap-3 p-3 rounded-xl hover:bg-purple-50 transition-colors"
                >
                  <div className="w-10 h-10 flex items-center justify-center bg-cyan-100 rounded-full">
                    <i className="ri-customer-service-line text-xl text-cyan-600"></i>
                  </div>
                  <span className="font-medium text-gray-900">고객센터</span>
                </Link>
              </div>
            </div>
          </div>
        </>
      )}

      {/* Desktop Sidebar */}
      <aside className="hidden lg:block fixed left-0 top-16 bottom-0 w-64 bg-white border-r border-gray-200 overflow-y-auto z-40">
        <div className="p-4 space-y-1">
          <Link 
            to="/" 
            className="flex items-center gap-3 p-3 rounded-xl hover:bg-purple-50 transition-colors"
          >
            <div className="w-10 h-10 flex items-center justify-center">
              <i className="ri-home-5-line text-xl text-gray-600"></i>
            </div>
            <span className="font-medium text-gray-900">Home</span>
          </Link>

          <Link 
            to="/community" 
            className="flex items-center gap-3 p-3 rounded-xl hover:bg-purple-50 transition-colors"
          >
            <div className="w-10 h-10 flex items-center justify-center">
              <i className="ri-chat-3-line text-xl text-gray-600"></i>
            </div>
            <span className="font-medium text-gray-900">Community</span>
          </Link>

          <Link 
            to="/live" 
            className="flex items-center gap-3 p-3 rounded-xl bg-purple-50 text-purple-600 transition-colors"
          >
            <div className="w-10 h-10 flex items-center justify-center">
              <i className="ri-live-fill text-xl"></i>
            </div>
            <span className="font-medium">Live</span>
          </Link>

          <Link 
            to="/voting" 
            className="flex items-center gap-3 p-3 rounded-xl hover:bg-purple-50 transition-colors"
          >
            <div className="w-10 h-10 flex items-center justify-center">
              <i className="ri-trophy-line text-xl text-gray-600"></i>
            </div>
            <span className="font-medium text-gray-900">Voting</span>
          </Link>

          <Link 
            to="/mypage" 
            className="flex items-center gap-3 p-3 rounded-xl hover:bg-purple-50 transition-colors"
          >
            <div className="w-10 h-10 flex items-center justify-center">
              <i className="ri-user-line text-xl text-gray-600"></i>
            </div>
            <span className="font-medium text-gray-900">My Page</span>
          </Link>

          <div className="h-px bg-gray-200 my-3"></div>

          <Link 
            to="/chart" 
            className="flex items-center gap-3 p-3 rounded-xl hover:bg-purple-50 transition-colors"
          >
            <div className="w-10 h-10 flex items-center justify-center bg-purple-100 rounded-full">
              <i className="ri-bar-chart-box-line text-xl text-purple-600"></i>
            </div>
            <span className="font-medium text-gray-900">차트</span>
          </Link>

          <Link 
            to="/news-detail" 
            className="flex items-center gap-3 p-3 rounded-xl hover:bg-purple-50 transition-colors"
          >
            <div className="w-10 h-10 flex items-center justify-center bg-indigo-100 rounded-full">
              <i className="ri-newspaper-line text-xl text-indigo-600"></i>
            </div>
            <span className="font-medium text-gray-900">뉴스</span>
          </Link>

          <Link 
            to="/concert" 
            className="flex items-center gap-3 p-3 rounded-xl hover:bg-purple-50 transition-colors"
          >
            <div className="w-10 h-10 flex items-center justify-center bg-pink-100 rounded-full">
              <i className="ri-music-line text-xl text-pink-600"></i>
            </div>
            <span className="font-medium text-gray-900">콘서트</span>
          </Link>

          <Link 
            to="/tickets" 
            className="flex items-center gap-3 p-3 rounded-xl hover:bg-purple-50 transition-colors"
          >
            <div className="w-10 h-10 flex items-center justify-center bg-blue-100 rounded-full">
              <i className="ri-ticket-line text-xl text-blue-600"></i>
            </div>
            <span className="font-medium text-gray-900">티켓</span>
          </Link>

          <Link 
            to="/membership" 
            className="flex items-center gap-3 p-3 rounded-xl hover:bg-purple-50 transition-colors"
          >
            <div className="w-10 h-10 flex items-center justify-center bg-yellow-100 rounded-full">
              <i className="ri-vip-crown-line text-xl text-yellow-600"></i>
            </div>
            <span className="font-medium text-gray-900">멤버십</span>
          </Link>

          <Link 
            to="/ads" 
            className="flex items-center gap-3 p-3 rounded-xl hover:bg-purple-50 transition-colors"
          >
            <div className="w-10 h-10 flex items-center justify-center bg-orange-100 rounded-full">
              <i className="ri-gift-line text-xl text-orange-600"></i>
            </div>
            <span className="font-medium text-gray-900">리워드</span>
          </Link>

          <Link 
            to="/favorites" 
            className="flex items-center gap-3 p-3 rounded-xl hover:bg-purple-50 transition-colors"
          >
            <div className="w-10 h-10 flex items-center justify-center bg-red-100 rounded-full">
              <i className="ri-heart-line text-xl text-red-600"></i>
            </div>
            <span className="font-medium text-gray-900">즐겨찾기</span>
          </Link>

          <Link 
            to="/saved" 
            className="flex items-center gap-3 p-3 rounded-xl hover:bg-purple-50 transition-colors"
          >
            <div className="w-10 h-10 flex items-center justify-center bg-green-100 rounded-full">
              <i className="ri-bookmark-line text-xl text-green-600"></i>
            </div>
            <span className="font-medium text-gray-900">저장됨</span>
          </Link>

          <div className="h-px bg-gray-200 my-3"></div>

          <Link 
            to="/settings" 
            className="flex items-center gap-3 p-3 rounded-xl hover:bg-purple-50 transition-colors"
          >
            <div className="w-10 h-10 flex items-center justify-center bg-gray-100 rounded-full">
              <i className="ri-settings-3-line text-xl text-gray-600"></i>
            </div>
            <span className="font-medium text-gray-900">설정</span>
          </Link>

          <Link 
            to="/support" 
            className="flex items-center gap-3 p-3 rounded-xl hover:bg-purple-50 transition-colors"
          >
            <div className="w-10 h-10 flex items-center justify-center bg-cyan-100 rounded-full">
              <i className="ri-customer-service-line text-xl text-cyan-600"></i>
            </div>
            <span className="font-medium text-gray-900">고객센터</span>
          </Link>
        </div>
      </aside>

      {/* Main Content */}
      <div className="pt-16 px-4 lg:ml-64 lg:px-8">
        <div className="max-w-6xl mx-auto">
          {/* Featured Live */}
          <div className="mt-4 mb-6">
            <div className="relative rounded-2xl overflow-hidden">
              <img
                src={liveStreams[0].thumbnail}
                alt={liveStreams[0].title}
                className="w-full h-64 md:h-80 lg:h-96 object-cover object-top"
              />
              <div className="absolute top-3 left-3 bg-red-600 text-white text-xs px-3 py-1 rounded-full font-medium flex items-center gap-1.5">
                <span className="w-2 h-2 bg-white rounded-full animate-pulse"></span>
                LIVE
              </div>
              <div className="absolute top-3 right-3 bg-black/70 text-white text-xs px-2 py-1 rounded-full">
                <i className="ri-eye-line mr-1"></i>{liveStreams[0].viewers}
              </div>
              <div className="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/80 to-transparent p-4 md:p-6">
                <h3 className="text-white font-bold text-xl md:text-2xl">{liveStreams[0].title}</h3>
                <p className="text-white/90 text-sm md:text-base mt-1">{liveStreams[0].artist}</p>
              </div>
            </div>
          </div>

          {/* Other Live Streams */}
          <h3 className="font-bold text-gray-900 mb-3 text-lg">More Live Streams</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-6">
            {liveStreams.slice(1).map(stream => (
              <div key={stream.id} className="bg-white rounded-xl overflow-hidden shadow-sm">
                <div className="relative">
                  <img
                    src={stream.thumbnail}
                    alt={stream.title}
                    className="w-full h-40 object-cover object-top"
                  />
                  <div className="absolute top-2 left-2 bg-red-600 text-white text-xs px-2 py-0.5 rounded-full font-medium flex items-center gap-1">
                    <span className="w-1 h-1 bg-white rounded-full"></span>
                    LIVE
                  </div>
                </div>
                <div className="p-3">
                  <h4 className="font-medium text-gray-900 text-sm line-clamp-1">{stream.title}</h4>
                  <p className="text-xs text-gray-500 mt-1">{stream.artist}</p>
                  <div className="flex items-center justify-between text-xs text-gray-600 mt-2">
                    <span><i className="ri-eye-line mr-1"></i>{stream.viewers}</span>
                    <span>{stream.duration}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>

          {/* Concerts Section */}
          <h3 className="font-bold text-gray-900 mb-3 text-lg">Upcoming Concerts</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 pb-6">
            {concerts.map(concert => (
              <div 
                key={concert.id} 
                className="bg-white rounded-xl overflow-hidden shadow-sm cursor-pointer hover:shadow-md transition-shadow"
                onClick={() => handleGetTickets(concert)}
              >
                <div className="relative">
                  <img
                    src={concert.image}
                    alt={concert.title}
                    className="w-full h-40 object-cover object-top"
                  />
                  <div className={`absolute top-2 right-2 text-white text-xs px-2 py-1 rounded-full font-medium ${
                    concert.status === 'Sold Out' ? 'bg-gray-800' : 'bg-green-600'
                  }`}>
                    {concert.status}
                  </div>
                </div>
                <div className="p-3">
                  <h4 className="font-medium text-gray-900 text-sm line-clamp-2">{concert.title}</h4>
                  <p className="text-xs text-gray-500 mt-2">
                    <i className="ri-calendar-line mr-1"></i>{concert.date}
                  </p>
                  <p className="text-xs text-gray-500 mt-1">
                    <i className="ri-map-pin-line mr-1"></i>{concert.location}
                  </p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Bottom Navigation */}
      <nav className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 z-50 lg:hidden">
        <div className="grid grid-cols-5 h-16">
          <Link to="/" className="flex flex-col items-center justify-center text-gray-500">
            <i className="ri-home-5-line text-xl"></i>
            <span className="text-xs mt-1">Home</span>
          </Link>
          <Link to="/community" className="flex flex-col items-center justify-center text-gray-500">
            <i className="ri-chat-3-line text-xl"></i>
            <span className="text-xs mt-1">Community</span>
          </Link>
          <Link to="/live" className="flex flex-col items-center justify-center text-purple-600">
            <i className="ri-live-fill text-xl"></i>
            <span className="text-xs mt-1 font-medium">Live</span>
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
