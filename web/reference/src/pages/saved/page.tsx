import { useState } from 'react';
import { Link } from 'react-router-dom';
import Image from 'next/image';

export default function SavedPage() {
  const [sortBy, setSortBy] = useState<'recent' | 'saved'>('recent');
  const [bookmarkedPosts, setBookmarkedPosts] = useState([
    {
      id: 1,
      author: {
        name: 'ARMY_Forever',
        avatar: 'https://readdy.ai/api/search-image?query=icon%2C%20cute%20purple%20heart%20avatar%2C%202.5D%20illustration%20style%2C%20the%20icon%20should%20take%20up%2070%20percent%20of%20the%20frame%2C%20isolated%20on%20white%20background%2C%20centered%20composition%2C%20soft%20lighting%2C%20vibrant%20colors&width=100&height=100&seq=saved001&orientation=squarish',
        badge: 'VIP'
      },
      content: 'BTS 새 앨범 티저 영상 보셨나요? 진짜 너무 기대돼요! 💜 컴백 준비하는 모습 보니까 벌써부터 설레네요.',
      image: 'https://readdy.ai/api/search-image?query=BTS%20comeback%20teaser%20concept%2C%20professional%20photography%2C%20purple%20theme%2C%20modern%20aesthetic%2C%20high%20quality%2C%20artistic%20composition%2C%20elegant%20lighting%2C%20vibrant%20colors%2C%20isolated%20on%20simple%20background&width=800&height=600&seq=saved002&orientation=landscape',
      likes: 1234,
      comments: 89,
      time: '2시간 전',
      savedTime: '오늘',
      tags: ['BTS', '컴백']
    },
    {
      id: 2,
      author: {
        name: 'Blink_Girl',
        avatar: 'https://readdy.ai/api/search-image?query=icon%2C%20cute%20pink%20crown%20avatar%2C%202.5D%20illustration%20style%2C%20the%20icon%20should%20take%20up%2070%20percent%20of%20the%20frame%2C%20isolated%20on%20white%20background%2C%20centered%20composition%2C%20soft%20lighting%2C%20vibrant%20colors&width=100&height=100&seq=saved003&orientation=squarish',
        badge: 'PRO'
      },
      content: 'BLACKPINK 월드투어 서울 공연 티켓팅 성공했어요! 🎉 드디어 직관할 수 있게 됐어요 ㅠㅠ',
      image: 'https://readdy.ai/api/search-image?query=BLACKPINK%20concert%20stage%20performance%2C%20professional%20photography%2C%20pink%20and%20black%20theme%2C%20dynamic%20lighting%2C%20high%20energy%2C%20artistic%20composition%2C%20vibrant%20colors%2C%20isolated%20on%20simple%20background&width=800&height=600&seq=saved004&orientation=landscape',
      likes: 892,
      comments: 67,
      time: '5시간 전',
      savedTime: '오늘',
      tags: ['BLACKPINK', '콘서트']
    },
    {
      id: 3,
      author: {
        name: 'Kpop_Lover',
        avatar: 'https://readdy.ai/api/search-image?query=icon%2C%20cute%20blue%20star%20avatar%2C%202.5D%20illustration%20style%2C%20the%20icon%20should%20take%20up%2070%20percent%20of%20the%20frame%2C%20isolated%20on%20white%20background%2C%20centered%20composition%2C%20soft%20lighting%2C%20vibrant%20colors&width=100&height=100&seq=saved005&orientation=squarish',
        badge: 'VIP'
      },
      content: '오늘 음악방송 무대 레전드였어요! 직캠 보고 또 보고 있어요 👀',
      image: 'https://readdy.ai/api/search-image?query=K-pop%20music%20show%20stage%20performance%2C%20professional%20photography%2C%20colorful%20lighting%2C%20energetic%20atmosphere%2C%20high%20quality%2C%20artistic%20composition%2C%20vibrant%20colors%2C%20isolated%20on%20simple%20background&width=800&height=600&seq=saved006&orientation=landscape',
      likes: 567,
      comments: 45,
      time: '1일 전',
      savedTime: '어제',
      tags: ['음악방송', '무대']
    },
    {
      id: 4,
      author: {
        name: 'Music_Fan',
        avatar: 'https://readdy.ai/api/search-image?query=icon%2C%20cute%20orange%20music%20note%20avatar%2C%202.5D%20illustration%20style%2C%20the%20icon%20should%20take%20up%2070%20percent%20of%20the%20frame%2C%20isolated%20on%20white%20background%2C%20centered%20composition%2C%20soft%20lighting%2C%20vibrant%20colors&width=100&height=100&seq=saved007&orientation=squarish',
        badge: 'PRO'
      },
      content: '이번 주 차트 순위 업데이트! 우리 아티스트 1위 유지 중 🏆',
      image: 'https://readdy.ai/api/search-image?query=music%20chart%20ranking%20display%2C%20modern%20digital%20interface%2C%20colorful%20graphics%2C%20professional%20design%2C%20clean%20composition%2C%20vibrant%20colors%2C%20isolated%20on%20simple%20background&width=800&height=600&seq=saved008&orientation=landscape',
      likes: 423,
      comments: 34,
      time: '2일 전',
      savedTime: '2일 전',
      tags: ['차트', '순위']
    },
    {
      id: 5,
      author: {
        name: 'Fan_Club',
        avatar: 'https://readdy.ai/api/search-image?query=icon%2C%20cute%20green%20clover%20avatar%2C%202.5D%20illustration%20style%2C%20the%20icon%20should%20take%20up%2070%20percent%20of%20the%20frame%2C%20isolated%20on%20white%20background%2C%20centered%20composition%2C%20soft%20lighting%2C%20vibrant%20colors&width=100&height=100&seq=saved009&orientation=squarish',
        badge: 'VIP'
      },
      content: '팬미팅 현장 분위기 미쳤어요! 평생 잊지 못할 추억 💚',
      image: 'https://readdy.ai/api/search-image?query=K-pop%20fan%20meeting%20event%2C%20happy%20fans%20and%20artists%20interaction%2C%20professional%20photography%2C%20warm%20atmosphere%2C%20joyful%20moment%2C%20vibrant%20colors%2C%20isolated%20on%20simple%20background&width=800&height=600&seq=saved010&orientation=landscape',
      likes: 756,
      comments: 52,
      time: '3일 전',
      savedTime: '3일 전',
      tags: ['팬미팅', '이벤트']
    }
  ]);

  const handleRemoveBookmark = (postId: number) => {
    setBookmarkedPosts(bookmarkedPosts.filter(post => post.id !== postId));
  };

  const sortedPosts = [...bookmarkedPosts].sort((a, b) => {
    if (sortBy === 'recent') {
      return a.id - b.id;
    }
    return b.id - a.id;
  });

  return (
    <div className="min-h-screen bg-gray-50 pb-20">
      {/* Header */}
      <header className="fixed top-0 left-0 right-0 bg-white border-b border-gray-200 z-50">
        <div className="px-4 py-3 flex items-center justify-between">
          <Link to="/mypage" className="w-9 h-9 flex items-center justify-center">
            <i className="ri-arrow-left-line text-xl text-gray-900"></i>
          </Link>
          <h1 className="text-base font-bold text-gray-900">저장한 게시물</h1>
          <Link to="/search" className="w-9 h-9 flex items-center justify-center">
            <i className="ri-search-line text-xl text-gray-700"></i>
          </Link>
        </div>
      </header>

      {/* Stats Card */}
      <div className="pt-16 px-4 py-4">
        <div className="bg-gradient-to-r from-purple-600 to-pink-600 rounded-2xl p-4 text-white">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-white/90 text-sm mb-1">저장한 게시물</p>
              <p className="text-3xl font-bold">{bookmarkedPosts.length}</p>
            </div>
            <div className="w-16 h-16 bg-white/20 rounded-full flex items-center justify-center">
              <i className="ri-bookmark-fill text-3xl"></i>
            </div>
          </div>
        </div>
      </div>

      {/* Sort Tabs */}
      <div className="px-4 py-3">
        <div className="bg-white rounded-full p-1 inline-flex">
          <button
            onClick={() => setSortBy('recent')}
            className={`px-4 py-1.5 rounded-full text-sm font-medium transition-all ${
              sortBy === 'recent'
                ? 'bg-purple-600 text-white'
                : 'text-gray-600'
            }`}
          >
            최신순
          </button>
          <button
            onClick={() => setSortBy('saved')}
            className={`px-4 py-1.5 rounded-full text-sm font-medium transition-all ${
              sortBy === 'saved'
                ? 'bg-purple-600 text-white'
                : 'text-gray-600'
            }`}
          >
            저장순
          </button>
        </div>
      </div>

      {/* Posts List */}
      {sortedPosts.length > 0 ? (
        <div className="px-4 space-y-3">
          {sortedPosts.map(post => (
            <div key={post.id} className="bg-white rounded-2xl overflow-hidden shadow-sm">
              {/* Author Info */}
              <div className="p-4 pb-3">
                <div className="flex items-center gap-3 mb-3">
                  <Image
                    src={post.author.avatar}
                    alt={post.author.name}
                    width={40}
                    height={40}
                    className="w-10 h-10 rounded-full object-cover"
                  />
                  <div className="flex-1">
                    <div className="flex items-center gap-2">
                      <h3 className="font-bold text-sm text-gray-900">{post.author.name}</h3>
                      <span className="bg-gradient-to-r from-purple-600 to-pink-600 text-white text-xs px-2 py-0.5 rounded-full">
                        {post.author.badge}
                      </span>
                    </div>
                    <p className="text-xs text-gray-500">{post.time}</p>
                  </div>
                  <button
                    onClick={() => handleRemoveBookmark(post.id)}
                    className="w-9 h-9 flex items-center justify-center"
                  >
                    <i className="ri-bookmark-fill text-xl text-purple-600"></i>
                  </button>
                </div>

                {/* Content */}
                <Link to="/post-detail">
                  <p className="text-sm text-gray-900 leading-relaxed mb-3">
                    {post.content}
                  </p>

                  {/* Tags */}
                  <div className="flex flex-wrap gap-2 mb-3">
                    {post.tags.map(tag => (
                      <span key={tag} className="text-xs text-purple-600 bg-purple-50 px-2 py-1 rounded-full">
                        #{tag}
                      </span>
                    ))}
                  </div>

                  {/* Image */}
                  {post.image && (
                    <div className="rounded-xl overflow-hidden mb-3 relative h-48">
                      <Image
                        src={post.image}
                        alt="Post"
                        fill
                        className="object-cover object-top"
                      />
                    </div>
                  )}
                </Link>

                {/* Stats */}
                <div className="flex items-center justify-between pt-3 border-t border-gray-100">
                  <div className="flex items-center gap-4 text-sm text-gray-600">
                    <span className="flex items-center gap-1">
                      <i className="ri-heart-line"></i>
                      {post.likes}
                    </span>
                    <span className="flex items-center gap-1">
                      <i className="ri-chat-3-line"></i>
                      {post.comments}
                    </span>
                  </div>
                  <span className="text-xs text-gray-400">
                    저장: {post.savedTime}
                  </span>
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : (
        /* Empty State */
        <div className="px-4 py-16 text-center">
          <div className="w-24 h-24 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <i className="ri-bookmark-line text-5xl text-gray-400"></i>
          </div>
          <h3 className="text-lg font-bold text-gray-900 mb-2">저장한 게시물이 없어요</h3>
          <p className="text-sm text-gray-500 mb-6">
            마음에 드는 게시물을 저장해보세요
          </p>
          <Link
            to="/community"
            className="inline-block bg-gradient-to-r from-purple-600 to-pink-600 text-white px-6 py-3 rounded-full font-medium"
          >
            게시물 둘러보기
          </Link>
        </div>
      )}

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
