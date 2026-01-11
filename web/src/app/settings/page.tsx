"use client";

import PageHeader from "@/components/layout/PageHeader";
import PageWrapper from "@/components/layout/PageWrapper";
import Link from "next/link";
import { useState } from "react";

export default function SettingsPage() {
  const [pushEnabled, setPushEnabled] = useState(true);
  const [darkMode, setDarkMode] = useState(false);

  return (
    <>
      <PageHeader title="설정" />
      <PageWrapper>
        <div className="max-w-3xl mx-auto px-4 py-4">
          {/* Account Section */}
          <div className="py-4">
            <h2 className="text-sm font-bold text-gray-500 mb-3 px-1">계정</h2>
            
            <div className="bg-white rounded-2xl border border-gray-100 overflow-hidden shadow-sm">
              <Link href="/mypage" className="flex items-center justify-between px-4 py-4 border-b border-gray-100 hover:bg-gray-50 transition-colors">
                <div className="flex items-center gap-3">
                  <div className="w-9 h-9 bg-purple-100 rounded-full flex items-center justify-center">
                    <i className="ri-user-line text-purple-600"></i>
                  </div>
                  <span className="text-sm text-gray-900 font-medium">프로필 수정</span>
                </div>
                <i className="ri-arrow-right-s-line text-gray-400 text-lg"></i>
              </Link>

              <button className="w-full flex items-center justify-between px-4 py-4 border-b border-gray-100 hover:bg-gray-50 transition-colors">
                <div className="flex items-center gap-3">
                  <div className="w-9 h-9 bg-blue-100 rounded-full flex items-center justify-center">
                    <i className="ri-lock-line text-blue-600"></i>
                  </div>
                  <span className="text-sm text-gray-900 font-medium">비밀번호 변경</span>
                </div>
                <i className="ri-arrow-right-s-line text-gray-400 text-lg"></i>
              </button>

              <button className="w-full flex items-center justify-between px-4 py-4 hover:bg-gray-50 transition-colors">
                <div className="flex items-center gap-3">
                  <div className="w-9 h-9 bg-green-100 rounded-full flex items-center justify-center">
                    <i className="ri-shield-check-line text-green-600"></i>
                  </div>
                  <span className="text-sm text-gray-900 font-medium">개인정보 보호</span>
                </div>
                <i className="ri-arrow-right-s-line text-gray-400 text-lg"></i>
              </button>
            </div>
          </div>

          {/* Notification Section */}
          <div className="py-4">
            <h2 className="text-sm font-bold text-gray-500 mb-3 px-1">알림</h2>
            
            <div className="bg-white rounded-2xl border border-gray-100 overflow-hidden shadow-sm">
              <div className="flex items-center justify-between px-4 py-4 border-b border-gray-100">
                <div className="flex items-center gap-3">
                  <div className="w-9 h-9 bg-pink-100 rounded-full flex items-center justify-center">
                    <i className="ri-notification-line text-pink-600"></i>
                  </div>
                  <span className="text-sm text-gray-900 font-medium">푸시 알림</span>
                </div>
                <button 
                  onClick={() => setPushEnabled(!pushEnabled)}
                  className={`relative w-12 h-7 rounded-full transition-colors ${
                    pushEnabled ? 'bg-purple-600' : 'bg-gray-200'
                  }`}
                >
                  <div className={`absolute top-1 left-1 w-5 h-5 bg-white rounded-full transition-transform shadow-sm ${
                    pushEnabled ? 'translate-x-5' : 'translate-x-0'
                  }`}></div>
                </button>
              </div>

              <Link href="/notifications" className="flex items-center justify-between px-4 py-4 hover:bg-gray-50 transition-colors">
                <div className="flex items-center gap-3">
                  <div className="w-9 h-9 bg-orange-100 rounded-full flex items-center justify-center">
                    <i className="ri-settings-3-line text-orange-600"></i>
                  </div>
                  <span className="text-sm text-gray-900 font-medium">알림 설정</span>
                </div>
                <i className="ri-arrow-right-s-line text-gray-400 text-lg"></i>
              </Link>
            </div>
          </div>

          {/* Display Section */}
          <div className="py-4">
            <h2 className="text-sm font-bold text-gray-500 mb-3 px-1">화면</h2>
            
            <div className="bg-white rounded-2xl border border-gray-100 overflow-hidden shadow-sm">
              <div className="flex items-center justify-between px-4 py-4 border-b border-gray-100">
                <div className="flex items-center gap-3">
                  <div className="w-9 h-9 bg-indigo-100 rounded-full flex items-center justify-center">
                    <i className="ri-moon-line text-indigo-600"></i>
                  </div>
                  <span className="text-sm text-gray-900 font-medium">다크 모드</span>
                </div>
                <button 
                  onClick={() => setDarkMode(!darkMode)}
                  className={`relative w-12 h-7 rounded-full transition-colors ${
                    darkMode ? 'bg-purple-600' : 'bg-gray-200'
                  }`}
                >
                  <div className={`absolute top-1 left-1 w-5 h-5 bg-white rounded-full transition-transform shadow-sm ${
                    darkMode ? 'translate-x-5' : 'translate-x-0'
                  }`}></div>
                </button>
              </div>

              <button className="w-full flex items-center justify-between px-4 py-4 hover:bg-gray-50 transition-colors">
                <div className="flex items-center gap-3">
                  <div className="w-9 h-9 bg-teal-100 rounded-full flex items-center justify-center">
                    <i className="ri-global-line text-teal-600"></i>
                  </div>
                  <div className="flex-1 text-left">
                    <p className="text-sm text-gray-900 font-medium">언어</p>
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-xs text-gray-500">한국어</span>
                  <i className="ri-arrow-right-s-line text-gray-400 text-lg"></i>
                </div>
              </button>
            </div>
          </div>

          {/* Support Section */}
          <div className="py-4">
            <h2 className="text-sm font-bold text-gray-500 mb-3 px-1">지원</h2>
            
            <div className="bg-white rounded-2xl border border-gray-100 overflow-hidden shadow-sm">
              <button className="w-full flex items-center justify-between px-4 py-4 border-b border-gray-100 hover:bg-gray-50 transition-colors">
                <div className="flex items-center gap-3">
                  <div className="w-9 h-9 bg-yellow-100 rounded-full flex items-center justify-center">
                    <i className="ri-question-line text-yellow-600"></i>
                  </div>
                  <span className="text-sm text-gray-900 font-medium">도움말</span>
                </div>
                <i className="ri-arrow-right-s-line text-gray-400 text-lg"></i>
              </button>

              <Link href="/support" className="w-full flex items-center justify-between px-4 py-4 border-b border-gray-100 hover:bg-gray-50 transition-colors">
                <div className="flex items-center gap-3">
                  <div className="w-9 h-9 bg-red-100 rounded-full flex items-center justify-center">
                    <i className="ri-customer-service-line text-red-600"></i>
                  </div>
                  <span className="text-sm text-gray-900 font-medium">고객센터</span>
                </div>
                <i className="ri-arrow-right-s-line text-gray-400 text-lg"></i>
              </Link>

              <button className="w-full flex items-center justify-between px-4 py-4 hover:bg-gray-50 transition-colors">
                <div className="flex items-center gap-3">
                  <div className="w-9 h-9 bg-gray-100 rounded-full flex items-center justify-center">
                    <i className="ri-information-line text-gray-600"></i>
                  </div>
                  <div className="flex-1 text-left">
                    <p className="text-sm text-gray-900 font-medium">앱 정보</p>
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-xs text-gray-500">v1.0.0</span>
                  <i className="ri-arrow-right-s-line text-gray-400 text-lg"></i>
                </div>
              </button>
            </div>
          </div>

          {/* Logout Button */}
          <div className="py-4">
            <button className="w-full bg-white border border-gray-200 text-gray-700 py-3.5 rounded-2xl font-medium hover:bg-gray-50 transition-colors shadow-sm">
              로그아웃
            </button>
          </div>

          {/* Delete Account */}
          <div className="pb-8 text-center">
            <button className="text-red-500 text-sm underline hover:text-red-600 transition-colors">
              회원 탈퇴
            </button>
          </div>
        </div>
      </PageWrapper>
    </>
  );
}
