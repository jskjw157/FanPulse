import { useState } from 'react';
import { Link } from 'react-router-dom';
import Image from 'next/image';

interface Candidate {
  id: number;
  name: string;
  image: string;
  votes: number;
  percentage: number;
}

interface Poll {
  id: number;
  title: string;
  category: string;
  endDate: string;
  totalVotes: string;
  candidates: Candidate[];
}

export default function Voting() {
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [showRankingsModal, setShowRankingsModal] = useState(false);
  const [selectedPoll, setSelectedPoll] = useState<Poll | null>(null);
  const [votedCandidates, setVotedCandidates] = useState<Set<string>>(new Set());
  const [votingPower, setVotingPower] = useState({
    daily: 10,
    used: 3,
    bonus: 5
  });
  const [showVoteAnimation, setShowVoteAnimation] = useState<string | null>(null);

  const votingCategories = [
    { id: 'all', name: 'All', icon: 'ri-trophy-line' },
    { id: 'artist', name: 'Artist', icon: 'ri-user-star-line' },
    { id: 'song', name: 'Song', icon: 'ri-music-line' },
    { id: 'mv', name: 'MV', icon: 'ri-film-line' }
  ];

  const activePolls = [
    {
      id: 1,
      title: 'Best Male Group 2024',
      category: 'artist',
      endDate: '2024-12-20',
      totalVotes: '2.5M',
      candidates: [
        {
          id: 1,
          name: 'BTS',
          image: 'https://readdy.ai/api/search-image?query=BTS%20kpop%20group%20professional%20photo%2C%20vibrant%20stage%20lighting%2C%20dynamic%20performance%20shot%2C%20high%20quality%20photography%2C%20energetic%20atmosphere%2C%20purple%20and%20blue%20lighting&width=400&height=400&seq=vote001&orientation=squarish',
          votes: 1250000,
          percentage: 50
        },
        {
          id: 2,
          name: 'SEVENTEEN',
          image: 'https://readdy.ai/api/search-image?query=SEVENTEEN%20kpop%20boy%20group%20professional%20photo%2C%20bright%20stage%20lighting%2C%20synchronized%20performance%20shot%2C%20high%20quality%20photography%2C%20energetic%20vibe%2C%20colorful%20stage&width=400&height=400&seq=vote002&orientation=squarish',
          votes: 750000,
          percentage: 30
        },
        {
          id: 3,
          name: 'Stray Kids',
          image: 'https://readdy.ai/api/search-image?query=Stray%20Kids%20kpop%20boy%20group%20professional%20photo%2C%20powerful%20stage%20lighting%2C%20intense%20performance%20shot%2C%20high%20quality%20photography%2C%20dark%20edgy%20atmosphere&width=400&height=400&seq=vote003&orientation=squarish',
          votes: 500000,
          percentage: 20
        }
      ]
    },
    {
      id: 2,
      title: 'Best Female Group 2024',
      category: 'artist',
      endDate: '2024-12-20',
      totalVotes: '2.1M',
      candidates: [
        {
          id: 1,
          name: 'BLACKPINK',
          image: 'https://readdy.ai/api/search-image?query=BLACKPINK%20kpop%20girl%20group%20professional%20photo%2C%20glamorous%20stage%20lighting%2C%20powerful%20performance%20shot%2C%20high%20quality%20photography%2C%20pink%20and%20black%20theme&width=400&height=400&seq=vote004&orientation=squarish',
          votes: 1050000,
          percentage: 50
        },
        {
          id: 2,
          name: 'NewJeans',
          image: 'https://readdy.ai/api/search-image?query=NewJeans%20kpop%20girl%20group%20professional%20photo%2C%20fresh%20youthful%20concept%2C%20modern%20stage%20lighting%2C%20high%20quality%20photography%2C%20pastel%20colors%2C%20trendy%20aesthetic&width=400&height=400&seq=vote005&orientation=squarish',
          votes: 630000,
          percentage: 30
        },
        {
          id: 3,
          name: 'aespa',
          image: 'https://readdy.ai/api/search-image?query=aespa%20kpop%20girl%20group%20professional%20photo%2C%20futuristic%20stage%20lighting%2C%20high%20tech%20concept%2C%20high%20quality%20photography%2C%20neon%20colors%2C%20modern%20aesthetic&width=400&height=400&seq=vote006&orientation=squarish',
          votes: 420000,
          percentage: 20
        }
      ]
    },
    {
      id: 3,
      title: 'Song of the Year',
      category: 'song',
      endDate: '2024-12-25',
      totalVotes: '1.8M',
      candidates: [
        {
          id: 1,
          name: 'Super Shy - NewJeans',
          image: 'https://readdy.ai/api/search-image?query=KPOP%20music%20album%20cover%2C%20fresh%20pastel%20colors%2C%20modern%20minimalist%20design%2C%20professional%20graphic%20design%2C%20youthful%20aesthetic%2C%20clean%20composition&width=400&height=400&seq=vote007&orientation=squarish',
          votes: 900000,
          percentage: 50
        },
        {
          id: 2,
          name: 'Seven - Jungkook',
          image: 'https://readdy.ai/api/search-image?query=KPOP%20music%20album%20cover%2C%20vibrant%20purple%20theme%2C%20modern%20design%2C%20professional%20graphic%20design%2C%20energetic%20vibe%2C%20bold%20typography&width=400&height=400&seq=vote008&orientation=squarish',
          votes: 540000,
          percentage: 30
        },
        {
          id: 3,
          name: 'Spicy - aespa',
          image: 'https://readdy.ai/api/search-image?query=KPOP%20music%20album%20cover%2C%20futuristic%20neon%20design%2C%20high%20tech%20aesthetic%2C%20professional%20graphic%20design%2C%20bold%20colors%2C%20modern%20composition&width=400&height=400&seq=vote009&orientation=squarish',
          votes: 360000,
          percentage: 20
        }
      ]
    }
  ];

  const handleVote = (pollId: number, candidateId: number) => {
    const remainingVotes = votingPower.daily - votingPower.used + votingPower.bonus;
    
    if (remainingVotes <= 0) {
      alert('투표권이 부족합니다. 광고 시청 또는 VIP 가입으로 추가 투표권을 획득하세요!');
      return;
    }

    const voteKey = `${pollId}-${candidateId}`;
    
    // 애니메이션 표시
    setShowVoteAnimation(voteKey);
    setTimeout(() => setShowVoteAnimation(null), 1000);

    // 투표 처리
    setVotedCandidates(prev => new Set(prev).add(voteKey));
    setVotingPower(prev => ({
      ...prev,
      used: prev.used + 1
    }));
  };

  const isVoted = (pollId: number, candidateId: number) => {
    return votedCandidates.has(`${pollId}-${candidateId}`);
  };

  const isAnimating = (pollId: number, candidateId: number) => {
    return showVoteAnimation === `${pollId}-${candidateId}`;
  };

  const filteredPolls = selectedCategory === 'all' 
    ? activePolls 
    : activePolls.filter(poll => poll.category === selectedCategory);

  const handleViewFullRankings = (poll: Poll) => {
    setSelectedPoll(poll);
    setShowRankingsModal(true);
  };

  const remainingVotes = votingPower.daily - votingPower.used + votingPower.bonus;

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
            className="flex items-center gap-3 p-3 rounded-xl hover:bg-purple-50 transition-colors"
          >
            <div className="w-10 h-10 flex items-center justify-center">
              <i className="ri-live-line text-xl text-gray-600"></i>
            </div>
            <span className="font-medium text-gray-900">Live</span>
          </Link>

          <Link 
            to="/voting" 
            className="flex items-center gap-3 p-3 rounded-xl bg-purple-50 text-purple-600 transition-colors"
          >
            <div className="w-10 h-10 flex items-center justify-center">
              <i className="ri-trophy-fill text-xl"></i>
            </div>
            <span className="font-medium">Voting</span>
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
          {/* Voting Power Card */}
          <div className="mt-4 bg-gradient-to-r from-purple-600 to-pink-600 rounded-2xl p-4 text-white shadow-lg">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm opacity-90">남은 투표권</p>
                <p className="text-3xl font-bold mt-1">{remainingVotes}</p>
              </div>
              <div className="text-right">
                <p className="text-xs opacity-75">일일 투표권: {votingPower.daily}</p>
                <p className="text-xs opacity-75 mt-1">사용: {votingPower.used}</p>
                <p className="text-xs opacity-75 mt-1">보너스: {votingPower.bonus}</p>
              </div>
            </div>
            <div className="mt-4 flex gap-2">
              <Link 
                to="/ads" 
                className="flex-1 bg-white/20 backdrop-blur-sm text-white text-sm font-medium py-2 rounded-full text-center"
              >
                광고 시청하고 투표권 받기
              </Link>
              <Link 
                to="/membership" 
                className="flex-1 bg-white text-purple-600 text-sm font-medium py-2 rounded-full text-center"
              >
                VIP 가입
              </Link>
            </div>
          </div>

          {/* Category Tabs */}
          <div className="mt-6 flex gap-2 overflow-x-auto pb-2">
            {votingCategories.map(category => (
              <button
                key={category.id}
                onClick={() => setSelectedCategory(category.id)}
                className={`flex items-center gap-2 px-4 py-2 rounded-full whitespace-nowrap transition-all ${
                  selectedCategory === category.id
                    ? 'bg-gradient-to-r from-purple-600 to-pink-600 text-white shadow-md'
                    : 'bg-white text-gray-600 border border-gray-200'
                }`}
              >
                <i className={category.icon}></i>
                <span className="text-sm font-medium">{category.name}</span>
              </button>
            ))}
          </div>

          {/* Active Polls */}
          <div className="mt-6 space-y-6 pb-6">
            {filteredPolls.map(poll => (
              <div key={poll.id} className="bg-white rounded-2xl shadow-sm overflow-hidden">
                {/* Poll Header */}
                <div className="p-4 border-b border-gray-100">
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <h3 className="font-bold text-gray-900 text-lg">{poll.title}</h3>
                      <div className="flex items-center gap-3 mt-2 text-sm text-gray-600">
                        <span><i className="ri-calendar-line mr-1"></i>마감: {poll.endDate}</span>
                        <span><i className="ri-user-line mr-1"></i>{poll.totalVotes} votes</span>
                      </div>
                    </div>
                    <button
                      onClick={() => handleViewFullRankings(poll)}
                      className="text-purple-600 text-sm font-medium"
                    >
                      전체 순위
                    </button>
                  </div>
                </div>

                {/* Candidates Grid */}
                <div className="p-4 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                  {poll.candidates.map(candidate => (
                    <div key={candidate.id} className="relative">
                      {/* Candidate Card */}
                      <div className="relative rounded-xl overflow-hidden h-48">
                        <Image
                          src={candidate.image}
                          alt={candidate.name}
                          fill
                          className="object-cover object-top"
                          unoptimized
                        />
                        <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/20 to-transparent"></div>
                        
                        {/* Candidate Info */}
                        <div className="absolute bottom-0 left-0 right-0 p-3">
                          <h4 className="text-white font-bold text-lg">{candidate.name}</h4>
                          <div className="mt-2 flex items-center justify-between">
                            <div className="flex-1">
                              <div className="flex items-center gap-2 text-white text-sm">
                                <span className="font-bold">{candidate.percentage}%</span>
                                <span className="opacity-75">({candidate.votes.toLocaleString()} votes)</span>
                              </div>
                              <div className="mt-1 h-1.5 bg-white/30 rounded-full overflow-hidden">
                                <div 
                                  className="h-full bg-white rounded-full transition-all duration-500"
                                  style={{ width: `${candidate.percentage}%` }}
                                ></div>
                              </div>
                            </div>
                          </div>
                        </div>

                        {/* Vote Animation */}
                        {isAnimating(poll.id, candidate.id) && (
                          <div className="absolute inset-0 flex items-center justify-center bg-purple-600/80 animate-fade-in">
                            <div className="text-center">
                              <i className="ri-heart-fill text-6xl text-white animate-bounce"></i>
                              <p className="text-white font-bold mt-2">투표 완료!</p>
                            </div>
                          </div>
                        )}

                        {/* Voted Badge */}
                        {isVoted(poll.id, candidate.id) && !isAnimating(poll.id, candidate.id) && (
                          <div className="absolute top-3 right-3 bg-purple-600 text-white text-xs px-3 py-1 rounded-full font-medium flex items-center gap-1">
                            <i className="ri-check-line"></i>
                            투표완료
                          </div>
                        )}
                      </div>

                      {/* Vote Button */}
                      <button
                        onClick={() => handleVote(poll.id, candidate.id)}
                        disabled={isVoted(poll.id, candidate.id)}
                        className={`w-full mt-3 font-medium py-2.5 rounded-full transition-all ${
                          isVoted(poll.id, candidate.id)
                            ? 'bg-gray-200 text-gray-500 cursor-not-allowed'
                            : 'bg-gradient-to-r from-purple-600 to-pink-600 text-white hover:shadow-lg'
                        }`}
                      >
                        {isVoted(poll.id, candidate.id) ? '투표 완료' : '투표하기'}
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Rankings Modal */}
      {showRankingsModal && selectedPoll && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl max-w-2xl w-full max-h-[80vh] overflow-hidden">
            {/* Modal Header */}
            <div className="p-4 border-b border-gray-200 flex items-center justify-between">
              <h3 className="text-lg font-bold text-gray-900">{selectedPoll.title} - 전체 순위</h3>
              <button
                onClick={() => setShowRankingsModal(false)}
                className="w-8 h-8 flex items-center justify-center"
              >
                <i className="ri-close-line text-2xl text-gray-600"></i>
              </button>
            </div>

            {/* Rankings List */}
            <div className="overflow-y-auto max-h-[calc(80vh-80px)] p-4">
              <div className="space-y-3">
                {selectedPoll.candidates.map((candidate: Candidate, index: number) => (
                  <div key={candidate.id} className="flex items-center gap-3 p-3 bg-gray-50 rounded-xl">
                    <div className={`w-8 h-8 flex items-center justify-center font-bold ${
                      index === 0 ? 'text-yellow-500' :
                      index === 1 ? 'text-gray-400' :
                      index === 2 ? 'text-orange-600' :
                      'text-gray-600'
                    }`}>
                      {index + 1}
                    </div>
                    <div className="relative w-12 h-12 rounded-lg overflow-hidden">
                      <Image
                        src={candidate.image}
                        alt={candidate.name}
                        fill
                        className="object-cover object-top"
                        unoptimized
                      />
                    </div>
                    <div className="flex-1">
                      <h4 className="font-bold text-gray-900">{candidate.name}</h4>
                      <div className="flex items-center gap-2 mt-1">
                        <div className="flex-1 h-2 bg-gray-200 rounded-full overflow-hidden">
                          <div 
                            className="h-full bg-gradient-to-r from-purple-600 to-pink-600 rounded-full"
                            style={{ width: `${candidate.percentage}%` }}
                          ></div>
                        </div>
                        <span className="text-sm font-medium text-gray-900">{candidate.percentage}%</span>
                      </div>
                    </div>
                    <div className="text-right">
                      <p className="text-sm font-medium text-gray-900">{candidate.votes.toLocaleString()}</p>
                      <p className="text-xs text-gray-500">votes</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      )}

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
          <Link to="/voting" className="flex flex-col items-center justify-center text-purple-600">
            <i className="ri-trophy-fill text-xl"></i>
            <span className="text-xs mt-1 font-medium">Voting</span>
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
