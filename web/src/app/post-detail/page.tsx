"use client";

import PageHeader from "@/components/layout/PageHeader";
import PageWrapper from "@/components/layout/PageWrapper";
import { useState } from "react";
import Link from "next/link";

export default function PostDetailPage() {
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
    <>
      <PageHeader title="Post Detail" />
      <PageWrapper>
        {/* Post */}
        <div className="max-w-3xl mx-auto px-4 py-6 bg-white shadow-sm rounded-b-3xl">
          {/* Author Info */}
          <div className="flex items-center gap-3 mb-4">
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img 
              src={post.author.avatar}
              alt={post.author.name}
              className="w-12 h-12 rounded-full object-cover border border-gray-100"
            />
            <div className="flex-1">
              <div className="flex items-center gap-2">
                <h3 className="font-bold text-gray-900">{post.author.name}</h3>
                <span className="bg-gradient-to-r from-purple-600 to-pink-600 text-white text-[10px] px-2 py-0.5 rounded-full font-bold">
                  {post.author.badge}
                </span>
              </div>
              <p className="text-xs text-gray-500">{post.time}</p>
            </div>
            <button className="px-4 py-1.5 border-2 border-purple-600 text-purple-600 rounded-full text-sm font-medium hover:bg-purple-50 transition-colors">
              팔로우
            </button>
          </div>

          {/* Post Content */}
          <p className="text-gray-800 leading-relaxed mb-4 text-base">
            {post.content}
          </p>

          {/* Tags */}
          <div className="flex flex-wrap gap-2 mb-4">
            {post.tags.map(tag => (
              <Link
                key={tag}
                href={`/search?tag=${encodeURIComponent(tag)}`}
                className="text-xs text-purple-600 bg-purple-50 px-3 py-1 rounded-full font-medium hover:bg-purple-100 transition-colors"
              >
                #{tag}
              </Link>
            ))}
          </div>

          {/* Post Image */}
          <div className="rounded-2xl overflow-hidden mb-6 shadow-sm">
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img 
              src={post.image}
              alt="Post"
              className="w-full h-auto object-cover"
            />
          </div>

          {/* Engagement Stats */}
          <div className="flex items-center justify-between py-3 border-y border-gray-100">
            <div className="flex items-center gap-6 text-sm text-gray-600">
              <span className="flex items-center gap-1.5">
                <i className="ri-heart-fill text-pink-500"></i>
                <span className="font-medium">{post.likes.toLocaleString()}</span>
              </span>
              <span className="flex items-center gap-1.5">
                <i className="ri-chat-3-fill text-blue-500"></i>
                <span className="font-medium">{post.comments}</span>
              </span>
              <span className="flex items-center gap-1.5">
                <i className="ri-share-forward-fill text-green-500"></i>
                <span className="font-medium">{post.shares}</span>
              </span>
            </div>
          </div>

          {/* Action Buttons */}
          <div className="flex items-center gap-2 py-4">
            <button 
              onClick={() => setLiked(!liked)}
              className={`flex-1 py-3 rounded-xl font-medium text-sm flex items-center justify-center gap-2 transition-all ${
                liked 
                  ? 'bg-pink-50 text-pink-600 scale-95' 
                  : 'bg-gray-50 text-gray-600 hover:bg-gray-100'
              }`}
            >
              <i className={liked ? 'ri-heart-fill text-lg' : 'ri-heart-line text-lg'}></i>
              좋아요
            </button>
            <button className="flex-1 bg-gray-50 text-gray-600 hover:bg-gray-100 transition-colors py-3 rounded-xl font-medium text-sm flex items-center justify-center gap-2">
              <i className="ri-chat-3-line text-lg"></i>
              댓글
            </button>
            <button className="flex-1 bg-gray-50 text-gray-600 hover:bg-gray-100 transition-colors py-3 rounded-xl font-medium text-sm flex items-center justify-center gap-2">
              <i className="ri-share-line text-lg"></i>
              공유
            </button>
            <button 
              onClick={() => setBookmarked(!bookmarked)}
              className={`w-12 h-12 rounded-xl flex items-center justify-center transition-colors ${
                bookmarked 
                  ? 'bg-purple-50 text-purple-600' 
                  : 'bg-gray-50 text-gray-600 hover:bg-gray-100'
              }`}
            >
              <i className={`${bookmarked ? 'ri-bookmark-fill' : 'ri-bookmark-line'} text-xl`}></i>
            </button>
          </div>
        </div>

        {/* Comments Section */}
        <div className="max-w-3xl mx-auto px-4 py-8 bg-gray-50/50 min-h-[300px]">
          <h2 className="text-lg font-bold text-gray-900 mb-4 px-1">
            댓글 {commentsList.length}
          </h2>

          <div className="space-y-4">
            {commentsList.map(item => (
              <div key={item.id} className="bg-white rounded-2xl p-4 shadow-sm border border-gray-100">
                <div className="flex items-start gap-3">
                  {/* eslint-disable-next-line @next/next/no-img-element */}
                  <img 
                    src={item.avatar}
                    alt={item.author}
                    className="w-10 h-10 rounded-full object-cover flex-shrink-0 border border-gray-100"
                  />
                  <div className="flex-1">
                    <div className="flex items-center justify-between mb-1">
                      <h4 className="font-bold text-sm text-gray-900">{item.author}</h4>
                      <span className="text-xs text-gray-400">{item.time}</span>
                    </div>
                    <p className="text-sm text-gray-700 leading-relaxed mb-3">
                      {item.content}
                    </p>
                    <div className="flex items-center gap-4">
                      <button className="text-xs text-gray-500 flex items-center gap-1 hover:text-pink-500 transition-colors">
                        <i className="ri-heart-line"></i>
                        {item.likes}
                      </button>
                      <button className="text-xs text-gray-500 hover:text-purple-600 transition-colors">답글</button>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Comment Input Placeholder */}
        <div className="sticky bottom-0 left-0 right-0 bg-white border-t border-gray-200 px-4 py-3">
          <div className="max-w-3xl mx-auto flex items-center gap-3">
            <input 
              type="text"
              value={comment}
              onChange={(e) => setComment(e.target.value)}
              placeholder="댓글을 입력하세요..."
              className="flex-1 bg-gray-100 rounded-full px-5 py-3 text-sm border-none focus:outline-none focus:ring-2 focus:ring-purple-600 transition-all"
            />
            <button className="w-11 h-11 bg-gradient-to-r from-purple-600 to-pink-600 rounded-full flex items-center justify-center text-white shadow-md hover:scale-105 transition-transform">
              <i className="ri-send-plane-fill text-lg"></i>
            </button>
          </div>
        </div>
      </PageWrapper>
    </>
  );
}