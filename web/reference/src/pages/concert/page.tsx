import { useState } from 'react';
import { Link } from 'react-router-dom';

export default function Concert() {
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const concerts = [
    {
      id: 1,
      title: 'BTS World Tour Seoul',
      artist: 'BTS',
      date: 'Dec 20, 2024',
      time: '19:00 KST',
      venue: 'Jamsil Olympic Stadium',
      location: 'Seoul, Korea',
      price: '₩150,000 - ₩300,000',
      status: 'available',
      image: 'https://readdy.ai/api/search-image?query=BTS%20concert%20poster%20design%2C%20purple%20theme%2C%20professional%20concert%20photography%2C%20stadium%20stage%20setup%2C%20massive%20LED%20screens%2C%20spectacular%20lighting%2C%20energetic%20atmosphere&width=600&height=400&seq=concert001&orientation=landscape'
    },
    {
      id: 2,
      title: 'BLACKPINK World Tour',
      artist: 'BLACKPINK',
      date: 'Dec 25, 2024',
      time: '18:00 KST',
      venue: 'KSPO Dome',
      location: 'Seoul, Korea',
      price: '₩180,000 - ₩350,000',
      status: 'available',
      image: 'https://readdy.ai/api/search-image?query=BLACKPINK%20concert%20poster%20design%2C%20pink%20and%20black%20theme%2C%20professional%20concert%20photography%2C%20glamorous%20stage%20setup%2C%20powerful%20lighting%2C%20fierce%20atmosphere&width=600&height=400&seq=concert002&orientation=landscape'
    },
    {
      id: 3,
      title: 'SEVENTEEN Be The Sun',
      artist: 'SEVENTEEN',
      date: 'Jan 5, 2025',
      time: '19:00 KST',
      venue: 'Gocheok Sky Dome',
      location: 'Seoul, Korea',
      price: '₩140,000 - ₩280,000',
      status: 'available',
      image: 'https://readdy.ai/api/search-image?query=SEVENTEEN%20concert%20poster%20design%2C%20bright%20colorful%20theme%2C%20professional%20concert%20photography%2C%20synchronized%20performance%2C%20vibrant%20stage%20lighting%2C%20energetic%20vibe&width=600&height=400&seq=concert003&orientation=landscape'
    },
    {
      id: 4,
      title: 'NewJeans Fan Meeting',
      artist: 'NewJeans',
      date: 'Jan 10, 2025',
      time: '17:00 KST',
      venue: 'Olympic Hall',
      location: 'Seoul, Korea',
      price: '₩120,000 - ₩250,000',
      status: 'soldout',
      image: 'https://readdy.ai/api/search-image?query=NewJeans%20fan%20meeting%20poster%20design%2C%20fresh%20pastel%20theme%2C%20professional%20event%20photography%2C%20intimate%20stage%20setup%2C%20youthful%20aesthetic%2C%20trendy%20atmosphere&width=600&height=400&seq=concert004&orientation=landscape'
    },
    {
      id: 5,
      title: 'TWICE Encore Concert',
      artist: 'TWICE',
      date: 'Jan 18, 2025',
      time: '19:00 KST',
      venue: 'KSPO Dome',
      location: 'Seoul, Korea',
      price: '₩160,000 - ₩320,000',
      status: 'available',
      image: 'https://readdy.ai/api/search-image?query=TWICE%20concert%20poster%20design%2C%20colorful%20vibrant%20theme%2C%20professional%20concert%20photography%2C%20energetic%20stage%20setup%2C%20spectacular%20lighting%20effects%2C%20joyful%20atmosphere&width=600&height=400&seq=concert005&orientation=landscape'
    }
  ];

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
          <div 
            className="fixed inset-0 bg-black/50 z-[60]"
            onClick={() => setIsMenuOpen(false)}
          ></div>
          
          <div className="fixed top-0 right-0 bottom-0 w-72 bg-white z-[70] shadow-2xl animate-slide-in-right">
            <div className="bg-gradient-to-r from-purple-600 to-pink-600 p-4 flex items-center justify-between">
              <h2 className="text-white font-bold text-lg">메뉴</h2>
              <button 
                onClick={() => setIsMenuOpen(false)}
                className="w-8 h-8 flex items-center justify-center"
              >
                <i className="ri-close-line text-2xl text-white"></i>
              </button>
            </div>

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
            className="flex items-center gap-3 p-3 rounded-xl hover:bg-purple-50 transition-colors"
          >
            <div className="w-10 h-10 flex items-center justify-center">
              <i className="ri-live-line text-xl text-gray-600"></i>
            </div>
            <span className="font-medium text-gray-900">Live</span>
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
            className="flex items-center gap-3 p-3 rounded-xl bg-purple-50 text-purple-600 transition-colors"
          >
            <div className="w-10 h-10 flex items-center justify-center bg-pink-100 rounded-full">
              <i className="ri-music-fill text-xl text-pink-600"></i>
            </div>
            <span className="font-medium">콘서트</span>
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
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mt-4 pb-6">
            {concerts.map(concert => (
              <div key={concert.id} className="bg-white rounded-2xl overflow-hidden shadow-sm hover:shadow-md transition-shadow">
                <img
                  src={concert.image}
                  alt={concert.title}
                  className="w-full h-48 object-cover object-top"
                />
                <div className="p-4">
                  <h3 className="font-bold text-gray-900 text-lg">{concert.title}</h3>
                  <p className="text-sm text-gray-600 mt-1">{concert.artist}</p>
                  <div className="mt-3 space-y-2">
                    <div className="flex items-center text-sm text-gray-600">
                      <i className="ri-calendar-line w-5"></i>
                      <span>{concert.date} at {concert.time}</span>
                    </div>
                    <div className="flex items-center text-sm text-gray-600">
                      <i className="ri-map-pin-line w-5"></i>
                      <span>{concert.location}</span>
                    </div>
                    <div className="flex items-center text-sm text-gray-600">
                      <i className="ri-ticket-line w-5"></i>
                      <span>{concert.price}</span>
                    </div>
                  </div>
                  <Link
                    to="/concert-detail"
                    state={{
                      concert: {
                        id: concert.id,
                        title: concert.title,
                        artist: concert.artist,
                        date: concert.date,
                        time: concert.time,
                        venue: concert.venue,
                        location: concert.location,
                        image: concert.image,
                        price: concert.price,
                        description: `${concert.artist}의 특별한 콘서트에 여러분을 초대합니다. 최고의 무대와 음향으로 잊지 못할 추억을 만들어보세요.`,
                        status: concert.status
                      }
                    }}
                    className="w-full mt-4 bg-gradient-to-r from-purple-600 to-pink-600 text-white font-medium py-3 rounded-full block text-center hover:shadow-lg transition-shadow"
                  >
                    Get Tickets
                  </Link>
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
