import { useState } from 'react';
import { Link } from 'react-router-dom';

export default function Community() {
  const [activeTab, setActiveTab] = useState<'all' | 'popular'>('all');
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [selectedArtist, setSelectedArtist] = useState('all');
  const [showArtistModal, setShowArtistModal] = useState(false);
  const [artistSearchQuery, setArtistSearchQuery] = useState('');

  const artists = [
    { id: 'all', name: 'All', posts: 1234 },
    { id: 'bts', name: 'BTS', posts: 456 },
    { id: 'blackpink', name: 'BLACKPINK', posts: 389 },
    { id: 'seventeen', name: 'SEVENTEEN', posts: 267 },
    { id: 'newjeans', name: 'NewJeans', posts: 198 },
    { id: 'straykids', name: 'Stray Kids', posts: 156 },
    { id: 'twice', name: 'TWICE', posts: 234 },
    { id: 'txt', name: 'TXT', posts: 145 },
    { id: 'enhypen', name: 'ENHYPEN', posts: 178 },
    { id: 'itzy', name: 'ITZY', posts: 123 },
    { id: 'lesserafim', name: 'LE SSERAFIM', posts: 167 },
  ];

  const currentArtist = artists.find(a => a.id === selectedArtist);

  const filteredArtists = artists.filter(artist =>
    artist.name.toLowerCase().includes(artistSearchQuery.toLowerCase())
  );

  const posts = [
    {
      id: 1,
      artist: 'BTS',
      artistId: 'bts',
      author: 'ARMY_Forever',
      avatar: 'https://readdy.ai/api/search-image?query=young%20asian%20woman%20profile%20photo%2C%20friendly%20smile%2C%20casual%20style%2C%20high%20quality%20portrait%20photography%2C%20natural%20lighting&width=100&height=100&seq=comm001&orientation=squarish',
      content: 'BTS 새 앨범 티저 영상 보셨나요? 진짜 너무 기대돼요! 💜 컴백 준비하는 모습 보니까 벌써부터 설레네요',
      image: 'https://readdy.ai/api/search-image?query=BTS%20comeback%20teaser%20concept%2C%20professional%20photography%2C%20purple%20theme%2C%20modern%20aesthetic%2C%20high%20quality%2C%20artistic%20composition&width=400&height=300&seq=comm002&orientation=landscape',
      likes: 1234,
      comments: 89,
      shares: 45,
      time: '2시간 전',
      isVIP: true,
      isPopular: true
    },
    {
      id: 2,
      artist: 'BLACKPINK',
      artistId: 'blackpink',
      author: 'Blink_Girl',
      avatar: 'https://readdy.ai/api/search-image?query=young%20asian%20woman%20profile%20photo%2C%20stylish%20look%2C%20modern%20fashion%2C%20high%20quality%20portrait%20photography%2C%20studio%20lighting&width=100&height=100&seq=comm003&orientation=squarish',
      content: 'BLACKPINK 월드투어 티켓 예매 성공했어요! 너무 설레요 ㅠㅠ 같이 가실 분 계신가요?',
      image: 'https://readdy.ai/api/search-image?query=BLACKPINK%20world%20tour%20concert%20stage%2C%20spectacular%20lighting%2C%20pink%20and%20black%20theme%2C%20professional%20concert%20photography&width=400&height=300&seq=comm004&orientation=landscape',
      likes: 856,
      comments: 67,
      shares: 34,
      time: '5시간 전',
      isVIP: false,
      isPopular: true
    },
    {
      id: 3,
      artist: 'SEVENTEEN',
      artistId: 'seventeen',
      author: 'Carat_17',
      avatar: 'https://readdy.ai/api/search-image?query=young%20asian%20woman%20profile%20photo%2C%20cheerful%20expression%2C%20bright%20style%2C%20high%20quality%20portrait%20photography&width=100&height=100&seq=comm005&orientation=squarish',
      content: 'SEVENTEEN 안무 연습 영상 떴어요! 칼군무 진짜 미쳤다... 13명이 한 명처럼 움직이는 게 신기해요',
      likes: 645,
      comments: 52,
      shares: 28,
      time: '1일 전',
      isVIP: true,
      isPopular: false
    },
    {
      id: 4,
      artist: 'NewJeans',
      artistId: 'newjeans',
      author: 'Bunny_Fan',
      avatar: 'https://readdy.ai/api/search-image?query=young%20asian%20woman%20profile%20photo%2C%20cute%20style%2C%20pastel%20colors%2C%20high%20quality%20portrait%20photography&width=100&height=100&seq=comm006&orientation=squarish',
      content: 'NewJeans 신곡 뮤비 1억뷰 돌파! 🎉 우리 토끼들 최고야 ㅠㅠ',
      image: 'https://readdy.ai/api/search-image?query=NewJeans%20music%20video%20concept%2C%20fresh%20and%20youthful%20style%2C%20pastel%20colors%2C%20modern%20aesthetic%2C%20high%20quality%20photography&width=400&height=300&seq=comm007&orientation=landscape',
      likes: 523,
      comments: 41,
      shares: 19,
      time: '3시간 전',
      isVIP: false,
      isPopular: true
    },
    {
      id: 5,
      artist: 'Stray Kids',
      artistId: 'straykids',
      author: 'Stay_Forever',
      avatar: 'https://readdy.ai/api/search-image?query=young%20asian%20woman%20profile%20photo%2C%20energetic%20look%2C%20urban%20style%2C%20high%20quality%20portrait%20photography&width=100&height=100&seq=comm008&orientation=squarish',
      content: 'Stray Kids 자작곡 진짜 천재들... 이번 앨범도 명곡만 가득하네요',
      likes: 412,
      comments: 35,
      shares: 15,
      time: '6시간 전',
      isVIP: true,
      isPopular: false
    },
    {
      id: 6,
      artist: 'TWICE',
      artistId: 'twice',
      author: 'Once_Love',
      avatar: 'https://readdy.ai/api/search-image?query=young%20asian%20woman%20profile%20photo%2C%20sweet%20smile%2C%20lovely%20style%2C%20high%20quality%20portrait%20photography&width=100&height=100&seq=comm009&orientation=squarish',
      content: 'TWICE 콘서트 셋리스트 예상해봐요! 어떤 곡들 나올까요? 💕',
      likes: 389,
      comments: 48,
      shares: 12,
      time: '8시간 전',
      isVIP: false,
      isPopular: false
    }
  ];

  const filteredPosts = posts.filter(post => {
    if (selectedArtist === 'all') return true;
    return post.artistId === selectedArtist;
  }).filter(post => {
    if (activeTab === 'popular') return post.isPopular;
    return true;
  });

  const handleArtistSelect = (artistId: string) => {
    setSelectedArtist(artistId);
    setShowArtistModal(false);
    setArtistSearchQuery('');
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

      {/* Desktop Sidebar Navigation */}
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
            className="flex items-center gap-3 p-3 rounded-xl bg-purple-50 text-purple-600 transition-colors"
          >
            <div className="w-10 h-10 flex items-center justify-center">
              <i className="ri-chat-3-fill text-xl"></i>
            </div>
            <span className="font-medium">Community</span>
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

      {/* Artist Grid Modal */}
      {showArtistModal && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-end">
          <div className="w-full bg-white rounded-t-3xl max-h-[80vh] overflow-hidden flex flex-col">
            {/* Modal Header */}
            <div className="px-4 py-4 border-b border-gray-200 flex items-center justify-between flex-shrink-0">
              <h3 className="text-lg font-bold text-gray-900">Select Artist</h3>
              <button
                onClick={() => {
                  setShowArtistModal(false);
                  setArtistSearchQuery('');
                }}
                className="w-8 h-8 flex items-center justify-center"
              >
                <i className="ri-close-line text-2xl text-gray-600"></i>
              </button>
            </div>

            {/* Search Bar */}
            <div className="px-4 py-3 border-b border-gray-200 flex-shrink-0">
              <div className="relative">
                <input
                  type="text"
                  value={artistSearchQuery}
                  onChange={(e) => setArtistSearchQuery(e.target.value)}
                  placeholder="아티스트 검색..."
                  className="w-full bg-gray-100 rounded-full pl-10 pr-4 py-2.5 text-sm border-none focus:outline-none focus:ring-2 focus:ring-purple-600"
                />
                <i className="ri-search-line absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"></i>
              </div>
            </div>

            {/* Artist Grid */}
            <div className="overflow-y-auto p-4">
              <div className="grid grid-cols-3 gap-3">
                {filteredArtists.map(artist => (
                  <button
                    key={artist.id}
                    onClick={() => handleArtistSelect(artist.id)}
                    className={`relative rounded-2xl overflow-hidden border-2 transition-all ${
                      selectedArtist === artist.id
                        ? 'border-purple-600 shadow-lg'
                        : 'border-gray-200'
                    }`}
                  >
                    <div className="aspect-square bg-gradient-to-br from-purple-100 to-pink-100 flex flex-col items-center justify-center p-3">
                      <i className="ri-user-star-fill text-3xl text-purple-600 mb-2"></i>
                      <h4 className="text-xs font-bold text-gray-900 text-center leading-tight">
                        {artist.name}
                      </h4>
                      <p className="text-xs text-gray-600 mt-1">{artist.posts}</p>
                    </div>
                    {selectedArtist === artist.id && (
                      <div className="absolute top-2 right-2 w-6 h-6 bg-purple-600 rounded-full flex items-center justify-center">
                        <i className="ri-check-line text-sm text-white"></i>
                      </div>
                    )}
                  </button>
                ))}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Main Content */}
      <div className="pt-16 lg:ml-64 max-w-7xl lg:max-w-5xl mx-auto">
        {/* Tab Filter */}
        <div className="pt-4 px-4 sticky top-16 bg-gradient-to-b from-purple-50 to-pink-50 z-30">
          <div className="flex items-center justify-between mb-3">
            <div className="flex gap-2">
              <button
                onClick={() => setActiveTab('all')}
                className={`px-4 py-2 rounded-full text-sm font-medium transition-colors ${
                  activeTab === 'all'
                    ? 'bg-purple-600 text-white'
                    : 'bg-white text-gray-600 border border-gray-200'
                }`}
              >
                All Posts
              </button>
              <button
                onClick={() => setActiveTab('popular')}
                className={`px-4 py-2 rounded-full text-sm font-medium transition-colors ${
                  activeTab === 'popular'
                    ? 'bg-purple-600 text-white'
                    : 'bg-white text-gray-600 border border-gray-200'
                }`}
              >
                🔥 Popular
              </button>
            </div>
            <button
              onClick={() => setShowArtistModal(true)}
              className="flex items-center gap-2 px-3 py-2 bg-white rounded-full border border-gray-200 text-sm"
            >
              <i className="ri-filter-line text-purple-600"></i>
              <span className="text-gray-700">{currentArtist?.name || 'All'}</span>
            </button>
          </div>
        </div>
        {/* Content */}
        <div className={selectedArtist !== 'all' ? 'pt-12' : 'pt-4'}>
          <div className="space-y-3 px-4">
            {filteredPosts.map(post => (
              <Link
                key={post.id}
                to={`/post-detail?id=${post.id}`}
                className="block bg-white rounded-xl border border-gray-200 overflow-hidden"
              >
                {/* Post Header */}
                <div className="p-3 flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <img
                      src={post.avatar}
                      alt={post.author}
                      className="w-10 h-10 rounded-full object-cover"
                    />
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="text-sm font-bold text-gray-900">{post.author}</span>
                        {post.isVIP && (
                          <span className="px-2 py-0.5 bg-gradient-to-r from-yellow-400 to-orange-400 text-white text-xs rounded-full font-medium">
                            VIP
                          </span>
                        )}
                      </div>
                      <div className="flex items-center gap-2">
                        <span className="text-xs text-purple-600 font-medium">{post.artist}</span>
                        <span className="text-xs text-gray-500">· {post.time}</span>
                      </div>
                    </div>
                  </div>
                  <button className="w-8 h-8 flex items-center justify-center">
                    <i className="ri-more-2-fill text-gray-400"></i>
                  </button>
                </div>

                {/* Post Content */}
                <div className="px-3 pb-3">
                  <p className="text-sm text-gray-900 leading-relaxed">{post.content}</p>
                </div>

                {/* Post Image */}
                {post.image && (
                  <img
                    src={post.image}
                    alt={post.content}
                    className="w-full h-64 object-cover object-top"
                  />
                )}

                {/* Post Actions */}
                <div className="p-3 flex items-center justify-between border-t border-gray-100">
                  <button className="flex items-center gap-1.5 text-gray-600">
                    <i className="ri-heart-line text-lg"></i>
                    <span className="text-sm">{post.likes}</span>
                  </button>
                  <button className="flex items-center gap-1.5 text-gray-600">
                    <i className="ri-chat-3-line text-lg"></i>
                    <span className="text-sm">{post.comments}</span>
                  </button>
                  <button className="flex items-center gap-1.5 text-gray-600">
                    <i className="ri-share-forward-line text-lg"></i>
                    <span className="text-sm">{post.shares}</span>
                  </button>
                  <button className="flex items-center gap-1.5 text-gray-600">
                    <i className="ri-bookmark-line text-lg"></i>
                  </button>
                </div>
              </Link>
            ))}
          </div>
        </div>
      </div>

      {/* Floating Action Button */}
      <Link
        to="/post-create"
        className="fixed bottom-24 right-4 w-14 h-14 bg-purple-600 rounded-full flex items-center justify-center shadow-lg z-40"
      >
        <i className="ri-add-line text-2xl text-white"></i>
      </Link>

      {/* Bottom Navigation */}
      <nav className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 z-50 lg:hidden">
        <div className="grid grid-cols-5 h-16">
          <Link to="/" className="flex flex-col items-center justify-center text-gray-500">
            <i className="ri-home-5-line text-xl"></i>
            <span className="text-xs mt-1">Home</span>
          </Link>
          <Link to="/community" className="flex flex-col items-center justify-center text-purple-600">
            <i className="ri-chat-3-fill text-xl"></i>
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
