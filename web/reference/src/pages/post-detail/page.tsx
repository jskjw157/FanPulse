import { useState } from 'react';
import { Link } from 'react-router-dom';
import Image from 'next/image';

export default function PostDetail() {
  const [liked, setLiked] = useState(false);
  const [bookmarked, setBookmarked] = useState(false);
  const [comment, setComment] = useState('');

  const post = {
    id: 1,
    author: {
      name: 'ARMY_Forever',
      avatar: 'https://readdy.ai/api/search-image?query=icon%2C%20cute%20purple%20heart%20avatar%2C%202.5D%20illustration%20style%2C%20the%20icon%20should%20take%20up%2070%20percent%20of%20the%20frame%2C%20isolated%20on%20white%20background%2C%20centered%20composition%2C%20soft%20lighting%2C%20vibrant%20colors&width=100&height=100&seq=avatar001&orientation=squarish',
      badge: 'VIP'
    },
    content: 'BTS 새 앨범 티저 영상 보셨나요? 진짜 너무 기대돼요! 💜 컴백 준비하는 모습 보니까 벌써부터 설레네요. 이번 앨범도 대박날 것 같아요!',
    image: 'https://readdy.ai/api/search-image?query=BTS%20comeback%20teaser%20concept%2C%20professional%20photography%2C%20purple%20theme%2C%20modern%20aesthetic%2C%20high%20quality%2C%20artistic%20composition%2C%20elegant%20lighting&width=800&height=600&seq=post001&orientation=landscape',
    likes: 1234,
    comments: 89,
    shares: 45,
    time: '2시간 전',
    tags: ['BTS', '컴백', '새앨범']
  };

  const commentsList = [
    {
      id: 1,
      author: 'Blink_Girl',
      avatar: 'https://readdy.ai/api/search-image?query=icon%2C%20cute%20pink%20crown%20avatar%2C%202.5D%20illustration%20style%2C%20the%20icon%20should%20take%20up%2070%20percent%20of%20the%20frame%2C%20isolated%20on%20white%20background%2C%20centered%20composition%2C%20soft%20lighting%2C%20vibrant%20colors&width=100&height=100&seq=avatar002&orientation=squarish',
      content: '저도 티저 보고 소름 돋았어요! 이번 컨셉 진짜 좋은 것 같아요',
      time: '1시간 전',
      likes: 23
    },
    {
      id: 2,
      author: 'Kpop_Lover',
      avatar: 'https://readdy.ai/api/search-image?query=icon%2C%20cute%20blue%20star%20avatar%2C%202.5D%20illustration%20style%2C%20the%20icon%20should%20take%20up%2070%20percent%20of%20the%20frame%2C%20isolated%20on%20white%20background%2C%20centered%20composition%2C%20soft%20lighting%2C%20vibrant%20colors&width=100&height=100&seq=avatar003&orientation=squarish',
      content: '벌써부터 기대되네요 ㅠㅠ 빨리 발매일 공개됐으면 좋겠어요',
      time: '30분 전',
      likes: 15
    },
    {
      id: 3,
      author: 'Music_Fan',
      avatar: 'https://readdy.ai/api/search-image?query=icon%2C%20cute%20orange%20music%20note%20avatar%2C%202.5D%20illustration%20style%2C%20the%20icon%20should%20take%20up%2070%20percent%20of%20the%20frame%2C%20isolated%20on%20white%20background%2C%20centered%20composition%2C%20soft%20lighting%2C%20vibrant%20colors&width=100&height=100&seq=avatar004&orientation=squarish',
      content: '이번 앨범도 빌보드 1위 가즈아! 💪',
      time: '15분 전',
      likes: 8
    }
  ];

  return (
    <div className="min-h-screen bg-white pb-20">
      {/* Header */}
      <header className="fixed top-0 left-0 right-0 bg-white border-b border-gray-200 z-50">
        <div className="px-4 py-3 flex items-center justify-between">
          <Link to="/community" className="w-9 h-9 flex items-center justify-center">
            <i className="ri-arrow-left-line text-xl text-gray-900"></i>
          </Link>
          <h1 className="text-base font-bold text-gray-900">게시글</h1>
          <button className="w-9 h-9 flex items-center justify-center">
            <i className="ri-more-2-line text-xl text-gray-700"></i>
          </button>
        </div>
      </header>

      {/* Content */}
      <div className="pt-14">
        {/* Post */}
        <div className="px-4 py-4">
          {/* Author Info */}
          <div className="flex items-center gap-3 mb-4">
            <Image
              src={post.author.avatar}
              alt={post.author.name}
              width={48}
              height={48}
              className="w-12 h-12 rounded-full object-cover"
            />
            <div className="flex-1">
              <div className="flex items-center gap-2">
                <h3 className="font-bold text-gray-900">{post.author.name}</h3>
                <span className="bg-gradient-to-r from-purple-600 to-pink-600 text-white text-xs px-2 py-0.5 rounded-full">
                  {post.author.badge}
                </span>
              </div>
              <p className="text-xs text-gray-500">{post.time}</p>
            </div>
            <button className="px-4 py-1.5 border-2 border-purple-600 text-purple-600 rounded-full text-sm font-medium">
              팔로우
            </button>
          </div>

          {/* Post Content */}
          <p className="text-sm text-gray-900 leading-relaxed mb-3">
            {post.content}
          </p>

          {/* Tags */}
          <div className="flex flex-wrap gap-2 mb-4">
            {post.tags.map(tag => (
              <span key={tag} className="text-xs text-purple-600 bg-purple-50 px-2 py-1 rounded-full">
                #{tag}
              </span>
            ))}
          </div>

          {/* Post Image */}
          <div className="rounded-2xl overflow-hidden mb-4 relative h-64">
            <Image
              src={post.image}
              alt="Post"
              fill
              className="object-cover object-top"
            />
          </div>

          {/* Engagement Stats */}
          <div className="flex items-center justify-between py-3 border-y border-gray-200">
            <div className="flex items-center gap-4 text-sm text-gray-600">
              <span className="flex items-center gap-1">
                <i className="ri-heart-fill text-pink-500"></i>
                {post.likes}
              </span>
              <span className="flex items-center gap-1">
                <i className="ri-chat-3-line"></i>
                {post.comments}
              </span>
              <span className="flex items-center gap-1">
                <i className="ri-share-line"></i>
                {post.shares}
              </span>
            </div>
          </div>

          {/* Action Buttons */}
          <div className="flex items-center gap-2 py-3">
            <button 
              onClick={() => setLiked(!liked)}
              className={`flex-1 py-2.5 rounded-xl font-medium text-sm flex items-center justify-center gap-2 ${
                liked ? 'bg-pink-50 text-pink-600' : 'bg-gray-100 text-gray-700'
              }`}
            >
              <i className={liked ? 'ri-heart-fill' : 'ri-heart-line'}></i>
              좋아요
            </button>
            <button className="flex-1 bg-gray-100 text-gray-700 py-2.5 rounded-xl font-medium text-sm flex items-center justify-center gap-2">
              <i className="ri-chat-3-line"></i>
              댓글
            </button>
            <button className="flex-1 bg-gray-100 text-gray-700 py-2.5 rounded-xl font-medium text-sm flex items-center justify-center gap-2">
              <i className="ri-share-line"></i>
              공유
            </button>
            <button 
              onClick={() => setBookmarked(!bookmarked)}
              className="w-12 h-10 bg-gray-100 rounded-xl flex items-center justify-center"
            >
              <i className={`${bookmarked ? 'ri-bookmark-fill text-purple-600' : 'ri-bookmark-line text-gray-700'}`}></i>
            </button>
          </div>
        </div>

        {/* Comments Section */}
        <div className="bg-gray-50 px-4 py-4">
          <h2 className="text-base font-bold text-gray-900 mb-4">
            댓글 {commentsList.length}
          </h2>

          <div className="space-y-4">
            {commentsList.map(item => (
              <div key={item.id} className="bg-white rounded-xl p-3">
                <div className="flex items-start gap-3">
                  <Image
                    src={item.avatar}
                    alt={item.author}
                    width={40}
                    height={40}
                    className="w-10 h-10 rounded-full object-cover flex-shrink-0"
                  />
                  <div className="flex-1">
                    <div className="flex items-center justify-between mb-1">
                      <h4 className="font-bold text-sm text-gray-900">{item.author}</h4>
                      <span className="text-xs text-gray-400">{item.time}</span>
                    </div>
                    <p className="text-sm text-gray-700 leading-relaxed mb-2">
                      {item.content}
                    </p>
                    <div className="flex items-center gap-3">
                      <button className="text-xs text-gray-500 flex items-center gap-1">
                        <i className="ri-heart-line"></i>
                        {item.likes}
                      </button>
                      <button className="text-xs text-gray-500">답글</button>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Comment Input */}
      <div className="fixed bottom-16 left-0 right-0 bg-white border-t border-gray-200 px-4 py-3">
        <div className="flex items-center gap-2">
          <input 
            type="text"
            value={comment}
            onChange={(e) => setComment(e.target.value)}
            placeholder="댓글을 입력하세요..."
            className="flex-1 bg-gray-100 rounded-full px-4 py-2.5 text-sm border-none focus:outline-none focus:ring-2 focus:ring-purple-600"
          />
          <button className="w-10 h-10 bg-gradient-to-r from-purple-600 to-pink-600 rounded-full flex items-center justify-center">
            <i className="ri-send-plane-fill text-white"></i>
          </button>
        </div>
      </div>

      {/* Bottom Navigation */}
      <nav className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 z-50">
        <div className="grid grid-cols-5 h-16">
          <Link to="/" className="flex flex-col items-center justify-center text-gray-500">
            <i className="ri-home-5-line text-xl"></i>
            <span className="text-xs mt-1">Home</span>
          </Link>
          <Link to="/community" className="flex flex-col items-center justify-center text-purple-600">
            <i className="ri-chat-3-fill text-xl"></i>
            <span className="text-xs mt-1 font-medium">Community</span>
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
