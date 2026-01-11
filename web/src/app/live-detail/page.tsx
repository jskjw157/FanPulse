"use client";

import { useRouter } from 'next/navigation';
import { useState } from 'react';

export default function LiveDetailPage() {
  const router = useRouter();
  const [isLiked, setIsLiked] = useState(false);
  const [message, setMessage] = useState('');
  const [chatMessages, setChatMessages] = useState([
    { id: 1, user: '민지팬123', message: '오늘 무대 최고예요! 🔥', time: '2분 전' },
    { id: 2, user: '하니러버', message: '라이브 음색 미쳤다 ㅠㅠ', time: '1분 전' },
    { id: 3, user: '뉴진스사랑', message: '다들 너무 예뻐요 💕', time: '방금' },
  ]);

  const handleSendMessage = () => {
    if (message.trim()) {
      setChatMessages([
        ...chatMessages,
        { id: Date.now(), user: '나', message: message.trim(), time: '방금' }
      ]);
      setMessage('');
    }
  };

  return (
    <div className="min-h-screen bg-white pb-20">
      {/* 헤더 */}
      <div className="fixed top-0 left-0 right-0 z-50 bg-gradient-to-b from-black/80 to-transparent px-4 py-3">
        <div className="flex items-center justify-between">
          <button onClick={() => router.back()} className="w-9 h-9 flex items-center justify-center">
            <i className="ri-arrow-left-line text-white text-xl"></i>
          </button>
          <div className="flex items-center gap-2">
            <button className="w-9 h-9 flex items-center justify-center">
              <i className="ri-share-line text-white text-xl"></i>
            </button>
            <button className="w-9 h-9 flex items-center justify-center">
              <i className="ri-more-2-fill text-white text-xl"></i>
            </button>
          </div>
        </div>
      </div>

      {/* 라이브 영상 영역 */}
      <div className="relative w-full h-[450px] bg-black">
        {/* eslint-disable-next-line @next/next/no-img-element */}
        <img 
          src="https://readdy.ai/api/search-image?query=K-pop%20girl%20group%20NewJeans%20performing%20live%20on%20stage%20with%20dynamic%20lighting%2C%20professional%20concert%20photography%2C%20energetic%20performance%2C%20vibrant%20stage%20lights%2C%20multiple%20members%20singing%20and%20dancing%2C%20high-quality%20broadcast%20camera%20angle%2C%20modern%20stage%20design%2C%20purple%20and%20pink%20lighting%20effects%2C%20professional%20live%20streaming%20quality%2C%204K%20resolution%2C%20cinematic%20composition&width=375&height=450&seq=live001&orientation=portrait"
          alt="Live Stream"
          className="w-full h-full object-cover"
        />
        
        {/* LIVE 배지 */}
        <div className="absolute top-20 left-4 bg-red-600 px-3 py-1 rounded-full flex items-center gap-1.5">
          <div className="w-2 h-2 bg-white rounded-full animate-pulse"></div>
          <span className="text-white text-xs font-bold">LIVE</span>
        </div>

        {/* 시청자 수 */}
        <div className="absolute top-20 right-4 bg-black/60 backdrop-blur-sm px-3 py-1.5 rounded-full flex items-center gap-1.5">
          <i className="ri-eye-line text-white text-sm"></i>
          <span className="text-white text-xs font-semibold">24,583</span>
        </div>

        {/* 하단 정보 */}
        <div className="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/80 to-transparent p-4">
          <div className="flex items-start gap-3">
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img 
              src="https://readdy.ai/api/search-image?query=K-pop%20idol%20profile%20photo%2C%20professional%20headshot%2C%20beautiful%20young%20woman%2C%20soft%20lighting%2C%20clean%20background%2C%20high%20quality%20portrait%20photography%2C%20friendly%20smile%2C%20modern%20styling&width=48&height=48&seq=profile001&orientation=squarish"
              alt="Artist"
              className="w-12 h-12 rounded-full border-2 border-white"
            />
            <div className="flex-1">
              <h2 className="text-white font-bold text-base">NewJeans 컴백 쇼케이스</h2>
              <p className="text-white/80 text-sm mt-0.5">NewJeans Official</p>
            </div>
            <button className="bg-[#FF2D55] hover:bg-[#FF2D55]/90 text-white px-5 py-2 rounded-full text-sm font-semibold">
              팔로우
            </button>
          </div>
        </div>
      </div>

      {/* 액션 버튼 */}
      <div className="flex items-center justify-around py-4 px-4 border-b border-gray-100">
        <button 
          onClick={() => setIsLiked(!isLiked)}
          className="flex flex-col items-center gap-1"
        >
          <div className="w-11 h-11 flex items-center justify-center">
            <i className={`${isLiked ? 'ri-heart-fill text-[#FF2D55]' : 'ri-heart-line text-gray-700'} text-2xl`}></i>
          </div>
          <span className="text-xs text-gray-600">15.2K</span>
        </button>
        <button className="flex-1 flex flex-col items-center gap-1">
          <div className="w-11 h-11 flex items-center justify-center">
            <i className="ri-chat-3-line text-gray-700 text-2xl"></i>
          </div>
          <span className="text-xs text-gray-600">8.5K</span>
        </button>
        <button className="flex-1 flex flex-col items-center gap-1">
          <div className="w-11 h-11 flex items-center justify-center">
            <i className="ri-gift-line text-gray-700 text-2xl"></i>
          </div>
          <span className="text-xs text-gray-600">선물</span>
        </button>
        <button className="flex-1 flex flex-col items-center gap-1">
          <div className="w-11 h-11 flex items-center justify-center">
            <i className="ri-share-forward-line text-gray-700 text-2xl"></i>
          </div>
          <span className="text-xs text-gray-600">공유</span>
        </button>
      </div>

      {/* 실시간 채팅 */}
      <div className="px-4 py-4 pb-6">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-base font-bold text-gray-900">실시간 채팅</h3>
          <span className="text-sm text-gray-500">8,542명 참여중</span>
        </div>

        {/* 채팅 메시지 */}
        <div className="space-y-3 mb-4 max-h-[300px] overflow-y-auto">
          {chatMessages.map((chat) => (
            <div key={chat.id} className="flex items-start gap-2.5">
              <div className="w-8 h-8 rounded-full bg-gradient-to-br from-purple-400 to-pink-400 flex items-center justify-center flex-shrink-0">
                <span className="text-white text-xs font-semibold">{chat.user[0]}</span>
              </div>
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 mb-0.5">
                  <span className="text-sm font-semibold text-gray-900">{chat.user}</span>
                  <span className="text-xs text-gray-400">{chat.time}</span>
                </div>
                <p className="text-sm text-gray-700 break-words">{chat.message}</p>
              </div>
            </div>
          ))}
        </div>

        {/* 채팅 입력 */}
        <div className="flex items-center gap-2">
          <div className="flex-1 relative">
            <input
              type="text"
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && handleSendMessage()}
              placeholder="메시지를 입력하세요..."
              className="w-full bg-gray-50 border-none rounded-full px-4 py-3 text-sm text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-[#FF2D55]"
            />
          </div>
          <button 
            onClick={handleSendMessage}
            className="w-10 h-10 bg-[#FF2D55] rounded-full flex items-center justify-center flex-shrink-0"
          >
            <i className="ri-send-plane-fill text-white text-lg"></i>
          </button>
        </div>
      </div>
    </div>
  );
}