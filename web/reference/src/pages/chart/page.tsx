import { useState } from 'react';
import { Link } from 'react-router-dom';

export default function Chart() {
  const [activeChart, setActiveChart] = useState('melon');
  const [isMenuOpen, setIsMenuOpen] = useState(false);

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
    },
    {
      rank: 6,
      prevRank: 6,
      change: 0,
      title: 'Ditto',
      artist: 'NewJeans',
      album: 'OMG',
      image: 'https://readdy.ai/api/search-image?query=KPOP%20album%20cover%20NewJeans%20Ditto%2C%20nostalgic%20film%20aesthetic%2C%20soft%20colors%2C%20professional%20album%20artwork%2C%20dreamy%20vibe%2C%20vintage%20camera%20feel&width=200&height=200&seq=chart006&orientation=squarish'
    },
    {
      rank: 7,
      prevRank: 8,
      change: 1,
      title: 'Hype Boy',
      artist: 'NewJeans',
      album: 'NewJeans',
      image: 'https://readdy.ai/api/search-image?query=KPOP%20album%20cover%20NewJeans%20Hype%20Boy%2C%20fresh%20youthful%20design%2C%20bright%20colors%2C%20professional%20album%20artwork%2C%20trendy%20aesthetic%2C%20modern%20feel&width=200&height=200&seq=chart007&orientation=squarish'
    },
    {
      rank: 8,
      prevRank: 7,
      change: -1,
      title: 'Teddy Bear',
      artist: 'STAYC',
      album: 'TEDDY BEAR',
      image: 'https://readdy.ai/api/search-image?query=KPOP%20album%20cover%20STAYC%20Teddy%20Bear%2C%20cute%20concept%2C%20soft%20pink%20colors%2C%20professional%20album%20artwork%2C%20sweet%20aesthetic%2C%20adorable%20design&width=200&height=200&seq=chart008&orientation=squarish'
    },
    {
      rank: 9,
      prevRank: 10,
      change: 1,
      title: 'OMG',
      artist: 'NewJeans',
      album: 'OMG',
      image: 'https://readdy.ai/api/search-image?query=KPOP%20album%20cover%20NewJeans%20OMG%2C%20playful%20design%2C%20vibrant%20colors%2C%20professional%20album%20artwork%2C%20fun%20aesthetic%2C%20energetic%20vibe&width=200&height=200&seq=chart009&orientation=squarish'
    },
    {
      rank: 10,
      prevRank: 9,
      change: -1,
      title: 'Unforgiven',
      artist: 'LE SSERAFIM',
      album: 'UNFORGIVEN',
      image: 'https://readdy.ai/api/search-image?query=KPOP%20album%20cover%20LE%20SSERAFIM%20Unforgiven%2C%20dark%20powerful%20concept%2C%20dramatic%20colors%2C%20professional%20album%20artwork%2C%20fierce%20aesthetic%2C%20bold%20design&width=200&height=200&seq=chart010&orientation=squarish'
    }
  ];

  const getChangeIcon = (change: number) => {
    if (change > 0) return <i className="ri-arrow-up-fill text-red-500"></i>;
    if (change < 0) return <i className="ri-arrow-down-fill text-blue-500"></i>;
    return <i className="ri-subtract-line text-gray-400"></i>;
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
            className="flex items-center gap-3 p-3 rounded-xl bg-purple-50 text-purple-600 transition-colors"
          >
            <div className="w-10 h-10 flex items-center justify-center bg-purple-100 rounded-full">
              <i className="ri-bar-chart-box-fill text-xl text-purple-600"></i>
            </div>
            <span className="font-medium">차트</span>
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
        <div className="max-w-4xl mx-auto">
          {/* Chart Tabs */}
          <div className="mt-4 flex gap-2 overflow-x-auto pb-2">
            {charts.map(chart => (
              <button
                key={chart.id}
                onClick={() => setActiveChart(chart.id)}
                className={`flex items-center gap-2 px-4 py-2 rounded-full whitespace-nowrap transition-all ${
                  activeChart === chart.id
                    ? 'bg-gradient-to-r from-purple-600 to-pink-600 text-white shadow-md'
                    : 'bg-white text-gray-600 border border-gray-200'
                }`}
              >
                <i className={chart.icon}></i>
                <span className="text-sm font-medium">{chart.name}</span>
              </button>
            ))}
          </div>

          {/* Chart List */}
          <div className="mt-4 space-y-2 pb-6">
            {rankings.map((item, index) => (
              <Link 
                key={item.rank} 
                to="/artist-detail"
                className="flex items-center gap-3 bg-white rounded-2xl p-4 shadow-sm hover:shadow-md transition-shadow"
              >
                {/* Rank */}
                <div className={`w-10 h-10 flex items-center justify-center font-bold text-lg flex-shrink-0 ${
                  index === 0 ? 'text-yellow-500' :
                  index === 1 ? 'text-gray-400' :
                  index === 2 ? 'text-orange-600' :
                  'text-gray-600'
                }`}>
                  {item.rank}
                </div>

                {/* Artist Image */}
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
                <div className={`flex items-center gap-1 text-sm font-medium flex-shrink-0 ${
                  item.change > 0 ? 'text-red-500' :
                  item.change < 0 ? 'text-blue-500' :
                  'text-gray-400'
                }`}>
                  {getChangeIcon(item.change)}
                  <span className="w-6 text-right">{Math.abs(item.change) || '-'}</span>
                </div>
              </Link>
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
