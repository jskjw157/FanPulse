"use client";

import PageHeader from "@/components/layout/PageHeader";
import PageWrapper from "@/components/layout/PageWrapper";
import { useState } from "react";
import { motion } from "framer-motion";
import Link from "next/link";

export default function CommunityPage() {
  const [activeTab, setActiveTab] = useState("all");

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
    }
  ];

  const filteredPosts = posts.filter(post => {
    if (activeTab === 'popular') return post.isPopular;
    return true;
  });

  return (
    <>
      <PageHeader 
        title="Community" 
        rightAction={
          <button className="p-2">
            <i className="ri-search-line text-xl text-gray-700"></i>
          </button>
        }
      />
      <PageWrapper>
        {/* Tab Navigation */}
        <div className="sticky top-16 bg-white border-b border-gray-200 z-30">
          <div className="flex max-w-7xl mx-auto">
            <button 
              onClick={() => setActiveTab("all")}
              className={`flex-1 py-4 text-sm font-medium transition-colors relative ${
                activeTab === "all" ? "text-purple-600" : "text-gray-500"
              }`}
            >
              전체
              {activeTab === "all" && (
                <motion.div 
                  layoutId="activeTab"
                  className="absolute bottom-0 left-0 right-0 h-0.5 bg-purple-600" 
                />
              )}
            </button>
            <button 
              onClick={() => setActiveTab("popular")}
              className={`flex-1 py-4 text-sm font-medium transition-colors relative ${
                activeTab === "popular" ? "text-purple-600" : "text-gray-500"
              }`}
            >
              인기
              {activeTab === "popular" && (
                <motion.div 
                  layoutId="activeTab"
                  className="absolute bottom-0 left-0 right-0 h-0.5 bg-purple-600" 
                />
              )}
            </button>
          </div>
        </div>

        <div className="max-w-3xl mx-auto px-4 py-6 space-y-4">
          {filteredPosts.map((post) => (
            <Link
              key={post.id}
              href={`/post-detail?id=${post.id}`}
              className="block bg-white rounded-2xl border border-gray-100 overflow-hidden hover:shadow-md transition-shadow"
            >
              {/* Post Header */}
              <div className="p-4 flex items-center justify-between">
                <div className="flex items-center gap-3">
                  {/* eslint-disable-next-line @next/next/no-img-element */}
                  <img
                    src={post.avatar}
                    alt={post.author}
                    className="w-10 h-10 rounded-full object-cover border border-gray-100"
                  />
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="text-sm font-bold text-gray-900">{post.author}</span>
                      {post.isVIP && (
                        <span className="px-2 py-0.5 bg-gradient-to-r from-yellow-400 to-orange-400 text-white text-[10px] rounded-full font-bold">
                          VIP
                        </span>
                      )}
                    </div>
                    <div className="flex items-center gap-2 mt-0.5">
                      <span className="text-xs font-bold text-purple-600">{post.artist}</span>
                      <span className="text-xs text-gray-400">· {post.time}</span>
                    </div>
                  </div>
                </div>
                <button className="w-8 h-8 flex items-center justify-center text-gray-400 hover:text-gray-600">
                  <i className="ri-more-2-fill"></i>
                </button>
              </div>

              {/* Post Content */}
              <div className="px-4 pb-3">
                <p className="text-sm text-gray-800 leading-relaxed whitespace-pre-wrap">{post.content}</p>
              </div>

              {/* Post Image */}
              {post.image && (
                // eslint-disable-next-line @next/next/no-img-element
                <img
                  src={post.image}
                  alt="Post content"
                  className="w-full h-64 object-cover"
                />
              )}

              {/* Post Actions */}
              <div className="p-3 flex items-center justify-between border-t border-gray-50">
                <div className="flex items-center gap-4">
                  <button className="flex items-center gap-1.5 text-gray-500 hover:text-pink-500 transition-colors">
                    <i className="ri-heart-line text-lg"></i>
                    <span className="text-xs font-medium">{post.likes.toLocaleString()}</span>
                  </button>
                  <button className="flex items-center gap-1.5 text-gray-500 hover:text-blue-500 transition-colors">
                    <i className="ri-chat-3-line text-lg"></i>
                    <span className="text-xs font-medium">{post.comments}</span>
                  </button>
                  <button className="flex items-center gap-1.5 text-gray-500 hover:text-green-500 transition-colors">
                    <i className="ri-share-forward-line text-lg"></i>
                    <span className="text-xs font-medium">{post.shares}</span>
                  </button>
                </div>
                <button className="text-gray-400 hover:text-purple-600 transition-colors">
                  <i className="ri-bookmark-line text-lg"></i>
                </button>
              </div>
            </Link>
          ))}
        </div>

        {/* Floating Action Button */}
        <Link 
          href="/post-create"
          className="fixed bottom-24 right-4 lg:right-8 lg:bottom-8 w-14 h-14 bg-gradient-to-r from-purple-600 to-pink-600 rounded-full flex items-center justify-center shadow-lg text-white hover:scale-105 transition-transform"
        >
          <i className="ri-add-line text-2xl"></i>
        </Link>
      </PageWrapper>
    </>
  );
}