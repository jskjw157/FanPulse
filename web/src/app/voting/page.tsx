"use client";

import PageHeader from "@/components/layout/PageHeader";
import PageWrapper from "@/components/layout/PageWrapper";
import { useState } from "react";
import Link from "next/link";

export default function VotingPage() {
  const [selectedCategory, setSelectedCategory] = useState('all');
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
    
    setShowVoteAnimation(voteKey);
    setTimeout(() => setShowVoteAnimation(null), 1000);

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

  const remainingVotes = votingPower.daily - votingPower.used + votingPower.bonus;

  return (
    <>
      <PageHeader title="Voting" />
      <PageWrapper>
        <div className="max-w-6xl mx-auto px-4 py-6">
          {/* Voting Power Card */}
          <div className="bg-gradient-to-r from-purple-600 to-pink-600 rounded-2xl p-4 text-white shadow-lg mb-6">
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
                href="/ads" 
                className="flex-1 bg-white/20 backdrop-blur-sm text-white text-sm font-medium py-2 rounded-full text-center hover:bg-white/30 transition-colors"
              >
                광고 시청하고 투표권 받기
              </Link>
              <Link 
                href="/membership" 
                className="flex-1 bg-white text-purple-600 text-sm font-medium py-2 rounded-full text-center hover:bg-gray-50 transition-colors"
              >
                VIP 가입
              </Link>
            </div>
          </div>

          {/* Category Tabs */}
          <div className="flex gap-2 overflow-x-auto pb-4 scrollbar-hide mb-2">
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
          <div className="space-y-6" data-testid="voting-list-skeleton">
            {filteredPolls.map(poll => (
              <div key={poll.id} className="bg-white rounded-2xl shadow-sm overflow-hidden border border-gray-100">
                {/* Poll Header */}
                <div className="p-4 border-b border-gray-100 bg-gray-50/50">
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <h3 className="font-bold text-gray-900 text-lg">{poll.title}</h3>
                      <div className="flex items-center gap-3 mt-2 text-sm text-gray-600">
                        <span><i className="ri-calendar-line mr-1"></i>마감: {poll.endDate}</span>
                        <span><i className="ri-user-line mr-1"></i>{poll.totalVotes} votes</span>
                      </div>
                    </div>
                  </div>
                </div>

                {/* Candidates Grid */}
                <div className="p-4 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                  {poll.candidates.map(candidate => (
                    <div key={candidate.id} className="relative group">
                      {/* Candidate Card */}
                      <div className="relative rounded-xl overflow-hidden shadow-sm group-hover:shadow-md transition-shadow">
                        {/* eslint-disable-next-line @next/next/no-img-element */}
                        <img
                          src={candidate.image}
                          alt={candidate.name}
                          className="w-full h-48 object-cover object-top"
                        />
                        <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-black/20 to-transparent"></div>
                        
                        {/* Candidate Info */}
                        <div className="absolute bottom-0 left-0 right-0 p-4">
                          <h4 className="text-white font-bold text-lg">{candidate.name}</h4>
                          <div className="mt-2 flex items-center justify-between">
                            <div className="flex-1">
                              <div className="flex items-center gap-2 text-white text-sm mb-1">
                                <span className="font-bold text-purple-300">{candidate.percentage}%</span>
                                <span className="opacity-75 text-xs">({candidate.votes.toLocaleString()} votes)</span>
                              </div>
                              <div className="h-1.5 bg-white/20 rounded-full overflow-hidden backdrop-blur-sm">
                                <div 
                                  className="h-full bg-gradient-to-r from-purple-400 to-pink-400 rounded-full transition-all duration-1000 ease-out"
                                  style={{ width: `${candidate.percentage}%` }}
                                ></div>
                              </div>
                            </div>
                          </div>
                        </div>

                        {/* Vote Animation overlay */}
                        {isAnimating(poll.id, candidate.id) && (
                          <div className="absolute inset-0 flex items-center justify-center bg-purple-600/80 animate-fade-in z-10">
                            <div className="text-center">
                              <i className="ri-heart-fill text-6xl text-white animate-bounce"></i>
                              <p className="text-white font-bold mt-2">투표 완료!</p>
                            </div>
                          </div>
                        )}

                        {/* Voted Badge */}
                        {isVoted(poll.id, candidate.id) && !isAnimating(poll.id, candidate.id) && (
                          <div className="absolute top-3 right-3 bg-purple-600 text-white text-xs px-3 py-1 rounded-full font-medium flex items-center gap-1 shadow-lg">
                            <i className="ri-check-line"></i>
                            투표완료
                          </div>
                        )}
                      </div>

                      {/* Vote Button */}
                      <button
                        onClick={() => handleVote(poll.id, candidate.id)}
                        disabled={isVoted(poll.id, candidate.id)}
                        className={`w-full mt-3 font-medium py-3 rounded-xl transition-all flex items-center justify-center gap-2 ${
                          isVoted(poll.id, candidate.id)
                            ? 'bg-gray-100 text-gray-400 cursor-not-allowed border border-gray-200'
                            : 'bg-white border-2 border-purple-600 text-purple-600 hover:bg-purple-50'
                        }`}
                      >
                        {isVoted(poll.id, candidate.id) ? (
                          <>
                            <i className="ri-check-line"></i>
                            투표 완료
                          </>
                        ) : (
                          <>
                            <i className="ri-heart-line"></i>
                            투표하기
                          </>
                        )}
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </div>
      </PageWrapper>
    </>
  );
}