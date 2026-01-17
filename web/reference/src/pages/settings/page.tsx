
import { useState } from 'react';
import { Link } from 'react-router-dom';

export default function Settings() {
  const [pushEnabled, setPushEnabled] = useState(true);
  const [darkMode, setDarkMode] = useState(false);
  const [language, setLanguage] = useState('ko');

  return (
    <div className="min-h-screen bg-white pb-20">
      {/* Header */}
      <header className="fixed top-0 left-0 right-0 bg-white border-b border-gray-200 z-50">
        <div className="px-4 py-3 flex items-center justify-between">
          <Link to="/mypage" className="w-9 h-9 flex items-center justify-center">
            <i className="ri-arrow-left-line text-xl text-gray-900"></i>
          </Link>
          <h1 className="text-base font-bold text-gray-900">설정</h1>
          <div className="w-9"></div>
        </div>
      </header>

      {/* Content */}
      <div className="pt-14">
        {/* Account Section */}
        <div className="px-4 py-4">
          <h2 className="text-sm font-bold text-gray-500 mb-3">계정</h2>
          
          <div className="bg-white rounded-xl border border-gray-200">
            <Link to="/mypage" className="flex items-center justify-between px-4 py-3.5 border-b border-gray-200">
              <div className="flex items-center gap-3">
                <div className="w-9 h-9 bg-purple-100 rounded-full flex items-center justify-center">
                  <i className="ri-user-line text-purple-600"></i>
                </div>
                <span className="text-sm text-gray-900">프로필 수정</span>
              </div>
              <i className="ri-arrow-right-s-line text-gray-400"></i>
            </Link>

            <button className="w-full flex items-center justify-between px-4 py-3.5 border-b border-gray-200">
              <div className="flex items-center gap-3">
                <div className="w-9 h-9 bg-blue-100 rounded-full flex items-center justify-center">
                  <i className="ri-lock-line text-blue-600"></i>
                </div>
                <span className="text-sm text-gray-900">비밀번호 변경</span>
              </div>
              <i className="ri-arrow-right-s-line text-gray-400"></i>
            </button>

            <button className="w-full flex items-center justify-between px-4 py-3.5">
              <div className="flex items-center gap-3">
                <div className="w-9 h-9 bg-green-100 rounded-full flex items-center justify-center">
                  <i className="ri-shield-check-line text-green-600"></i>
                </div>
                <span className="text-sm text-gray-900">개인정보 보호</span>
              </div>
              <i className="ri-arrow-right-s-line text-gray-400"></i>
            </button>
          </div>
        </div>

        {/* Notification Section */}
        <div className="px-4 py-4">
          <h2 className="text-sm font-bold text-gray-500 mb-3">알림</h2>
          
          <div className="bg-white rounded-xl border border-gray-200">
            <div className="flex items-center justify-between px-4 py-3.5 border-b border-gray-200">
              <div className="flex items-center gap-3">
                <div className="w-9 h-9 bg-pink-100 rounded-full flex items-center justify-center">
                  <i className="ri-notification-line text-pink-600"></i>
                </div>
                <span className="text-sm text-gray-900">푸시 알림</span>
              </div>
              <button 
                onClick={() => setPushEnabled(!pushEnabled)}
                className={`relative w-12 h-6 rounded-full transition-colors ${
                  pushEnabled ? 'bg-purple-600' : 'bg-gray-300'
                }`}
              >
                <div className={`absolute top-0.5 left-0.5 w-5 h-5 bg-white rounded-full transition-transform ${
                  pushEnabled ? 'translate-x-6' : 'translate-x-0'
                }`}></div>
              </button>
            </div>

            <Link to="/notifications" className="flex items-center justify-between px-4 py-3.5">
              <div className="flex items-center gap-3">
                <div className="w-9 h-9 bg-orange-100 rounded-full flex items-center justify-center">
                  <i className="ri-settings-3-line text-orange-600"></i>
                </div>
                <span className="text-sm text-gray-900">알림 설정</span>
              </div>
              <i className="ri-arrow-right-s-line text-gray-400"></i>
            </Link>
          </div>
        </div>

        {/* Display Section */}
        <div className="px-4 py-4">
          <h2 className="text-sm font-bold text-gray-500 mb-3">화면</h2>
          
          <div className="bg-white rounded-xl border border-gray-200">
            <div className="flex items-center justify-between px-4 py-3.5 border-b border-gray-200">
              <div className="flex items-center gap-3">
                <div className="w-9 h-9 bg-indigo-100 rounded-full flex items-center justify-center">
                  <i className="ri-moon-line text-indigo-600"></i>
                </div>
                <span className="text-sm text-gray-900">다크 모드</span>
              </div>
              <button 
                onClick={() => setDarkMode(!darkMode)}
                className={`relative w-12 h-6 rounded-full transition-colors ${
                  darkMode ? 'bg-purple-600' : 'bg-gray-300'
                }`}
              >
                <div className={`absolute top-0.5 left-0.5 w-5 h-5 bg-white rounded-full transition-transform ${
                  darkMode ? 'translate-x-6' : 'translate-x-0'
                }`}></div>
              </button>
            </div>

            <button
              onClick={() => setLanguage(language === 'ko' ? 'en' : 'ko')}
              className="w-full flex items-center justify-between px-4 py-3.5"
            >
              <div className="flex items-center gap-3">
                <div className="w-9 h-9 bg-teal-100 rounded-full flex items-center justify-center">
                  <i className="ri-global-line text-teal-600"></i>
                </div>
                <div className="flex-1 text-left">
                  <p className="text-sm text-gray-900">언어</p>
                  <p className="text-xs text-gray-500">{language === 'ko' ? '한국어' : 'English'}</p>
                </div>
              </div>
              <i className="ri-arrow-right-s-line text-gray-400"></i>
            </button>
          </div>
        </div>

        {/* Support Section */}
        <div className="px-4 py-4">
          <h2 className="text-sm font-bold text-gray-500 mb-3">지원</h2>
          
          <div className="bg-white rounded-xl border border-gray-200">
            <button className="w-full flex items-center justify-between px-4 py-3.5 border-b border-gray-200">
              <div className="flex items-center gap-3">
                <div className="w-9 h-9 bg-yellow-100 rounded-full flex items-center justify-center">
                  <i className="ri-question-line text-yellow-600"></i>
                </div>
                <span className="text-sm text-gray-900">도움말</span>
              </div>
              <i className="ri-arrow-right-s-line text-gray-400"></i>
            </button>

            <button className="w-full flex items-center justify-between px-4 py-3.5 border-b border-gray-200">
              <div className="flex items-center gap-3">
                <div className="w-9 h-9 bg-red-100 rounded-full flex items-center justify-center">
                  <i className="ri-customer-service-line text-red-600"></i>
                </div>
                <span className="text-sm text-gray-900">고객센터</span>
              </div>
              <i className="ri-arrow-right-s-line text-gray-400"></i>
            </button>

            <button className="w-full flex items-center justify-between px-4 py-3.5">
              <div className="flex items-center gap-3">
                <div className="w-9 h-9 bg-gray-100 rounded-full flex items-center justify-center">
                  <i className="ri-information-line text-gray-600"></i>
                </div>
                <div className="flex-1 text-left">
                  <p className="text-sm text-gray-900">앱 정보</p>
                  <p className="text-xs text-gray-500">버전 1.0.0</p>
                </div>
              </div>
              <i className="ri-arrow-right-s-line text-gray-400"></i>
            </button>
          </div>
        </div>

        {/* Logout Button */}
        <div className="px-4 py-4">
          <button className="w-full bg-gray-100 text-gray-700 py-3.5 rounded-xl font-medium text-sm">
            로그아웃
          </button>
        </div>

        {/* Delete Account */}
        <div className="px-4 pb-6">
          <button className="w-full text-red-500 text-sm underline">
            회원 탈퇴
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
          <Link to="/mypage" className="flex flex-col items-center justify-center text-purple-600">
            <i className="ri-user-fill text-xl"></i>
            <span className="text-xs mt-1 font-medium">My</span>
          </Link>
        </div>
      </nav>
    </div>
  );
}
