"use client";

import { useState } from 'react';
import { useRouter } from 'next/navigation';

export default function LoginPage() {
  const router = useRouter();
  const [isLogin, setIsLogin] = useState(true);

  const handleSocialLogin = (provider: string) => {
    // OAuth2 로그인 시뮬레이션
    console.log(`${provider} 로그인 시도`);
    setTimeout(() => {
      router.push('/');
    }, 500);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-purple-600 via-pink-500 to-purple-700 flex items-center justify-center px-4">
      <div className="w-full max-w-md">
        {/* Logo */}
        <div className="text-center mb-8">
          <h1 className="text-white text-3xl font-bold mb-2 font-pacifico">Welcome to FanPulse</h1>
          <p className="text-white/90 text-sm">글로벌 K-POP 팬들의 인터랙티브 플랫폼</p>
        </div>

        {/* Login Card */}
        <div className="bg-white rounded-3xl p-6 shadow-2xl">
          {/* Tab Switcher */}
          <div className="flex gap-2 mb-6 bg-gray-100 rounded-full p-1">
            <button
              onClick={() => setIsLogin(true)}
              className={`flex-1 py-2 rounded-full text-sm font-medium transition-all ${
                isLogin ? 'bg-purple-600 text-white' : 'text-gray-600'
              }`}
            >
              로그인
            </button>
            <button
              onClick={() => setIsLogin(false)}
              className={`flex-1 py-2 rounded-full text-sm font-medium transition-all ${
                !isLogin ? 'bg-purple-600 text-white' : 'text-gray-600'
              }`}
            >
              회원가입
            </button>
          </div>

          {/* Social Login Buttons */}
          <div className="space-y-3">
            <button
              onClick={() => handleSocialLogin('Google')}
              className="w-full flex items-center justify-center gap-3 bg-white border-2 border-gray-200 rounded-full py-3 font-medium text-gray-700 hover:border-purple-300 transition-colors"
            >
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M19.8055 10.2292C19.8055 9.55056 19.7501 8.86667 19.6306 8.19861H10.2002V12.0492H15.6014C15.3773 13.2911 14.6571 14.3898 13.6025 15.0875V17.5866H16.8251C18.7173 15.8449 19.8055 13.2728 19.8055 10.2292Z" fill="#4285F4"/>
                <path d="M10.2002 20.0006C12.9516 20.0006 15.2727 19.1151 16.8296 17.5865L13.607 15.0874C12.7096 15.6972 11.5521 16.0428 10.2047 16.0428C7.54356 16.0428 5.28217 14.2828 4.48642 11.9165H1.16699V14.4923C2.76077 17.8695 6.31545 20.0006 10.2002 20.0006Z" fill="#34A853"/>
                <path d="M4.48174 11.9163C4.06649 10.6744 4.06649 9.33009 4.48174 8.08813V5.51233H1.16708C-0.389027 8.66929 -0.389027 12.3352 1.16708 15.4921L4.48174 11.9163Z" fill="#FBBC04"/>
                <path d="M10.2002 3.95805C11.6241 3.936 13.0007 4.47247 14.0409 5.45722L16.8911 2.60218C15.1826 0.990831 12.9335 0.0808353 10.2002 0.104384C6.31545 0.104384 2.76077 2.23549 1.16699 5.61261L4.48165 8.18841C5.27263 5.81691 7.53879 3.95805 10.2002 3.95805Z" fill="#EA4335"/>
              </svg>
              Google로 {isLogin ? '로그인' : '가입하기'}
            </button>
          </div>

          {/* Divider */}
          <div className="flex items-center gap-3 my-6">
            <div className="flex-1 h-px bg-gray-200"></div>
            <span className="text-xs text-gray-400">또는</span>
            <div className="flex-1 h-px bg-gray-200"></div>
          </div>

          {/* Email Login Form */}
          <div className="space-y-3">
            <input
              type="email"
              placeholder="이메일"
              className="w-full px-4 py-3 bg-gray-50 border-none rounded-full text-sm focus:outline-none focus:ring-2 focus:ring-purple-500"
            />
            <input
              type="password"
              placeholder="비밀번호"
              className="w-full px-4 py-3 bg-gray-50 border-none rounded-full text-sm focus:outline-none focus:ring-2 focus:ring-purple-500"
            />
            {!isLogin && (
              <input
                type="password"
                placeholder="비밀번호 확인"
                className="w-full px-4 py-3 bg-gray-50 border-none rounded-full text-sm focus:outline-none focus:ring-2 focus:ring-purple-500"
              />
            )}
            <button
              onClick={() => router.push('/')}
              className="w-full bg-gradient-to-r from-purple-600 to-pink-600 text-white rounded-full py-3 font-medium hover:shadow-lg transition-shadow"
            >
              {isLogin ? '로그인' : '회원가입'}
            </button>
          </div>

          {/* Footer Links */}
          {isLogin && (
            <div className="mt-4 text-center">
              <button className="text-xs text-gray-500 hover:text-purple-600">
                비밀번호를 잊으셨나요?
              </button>
            </div>
          )}
        </div>

        {/* Skip Login */}
        <button
          onClick={() => router.push('/')}
          className="w-full mt-4 text-white text-sm font-medium py-3"
        >
          둘러보기
        </button>
      </div>
    </div>
  );
}