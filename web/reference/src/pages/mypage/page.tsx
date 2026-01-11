
import { Link } from 'react-router-dom';

export default function MyPage() {
  const user = {
    name: 'Sarah Kim',
    email: 'sarah.kim@example.com',
    avatar: 'https://readdy.ai/api/search-image?query=young%20asian%20woman%20profile%20photo%2C%20friendly%20smile%2C%20casual%20style%2C%20professional%20portrait%20photography%2C%20natural%20lighting%2C%20clean%20background&width=300&height=300&seq=mypage001&orientation=squarish',
    membership: 'VIP',
    points: 12450,
    level: 15
  };

  const stats = [
    { label: '투표 참여', value: '247', icon: 'ri-trophy-line' },
    { label: '게시물', value: '89', icon: 'ri-file-text-line' },
    { label: '팔로워', value: '1.2K', icon: 'ri-user-follow-line' }
  ];

  const pointHistory = [
    { id: 1, type: '적립', desc: '광고 시청', points: '+500', date: '2024.12.15' },
    { id: 2, type: '사용', desc: '굿즈 구매', points: '-2000', date: '2024.12.14' },
    { id: 3, type: '적립', desc: '투표 참여', points: '+100', date: '2024.12.13' },
    { id: 4, type: '적립', desc: '출석 체크', points: '+50', date: '2024.12.12' }
  ];

  const menuItems = [
    { icon: 'ri-heart-line', label: '좋아요한 아티스트', link: '/favorites' },
    { icon: 'ri-bookmark-line', label: '저장한 게시물', link: '/saved' },
    { icon: 'ri-ticket-line', label: '예매 내역', link: '/tickets' },
    { icon: 'ri-settings-3-line', label: '설정', link: '/settings' },
    { icon: 'ri-customer-service-line', label: '고객센터', link: '/support' }
  ];

  return (
    <div className="min-h-screen bg-gray-50 pb-20">
      {/* Header */}
      <header className="fixed top-0 left-0 right-0 bg-white border-b border-gray-200 z-50">
        <div className="px-4 py-3 flex items-center justify-between">
          <h1 className="text-base font-bold text-gray-900">마이페이지</h1>
          <div className="flex items-center gap-2">
            <Link to="/notifications" className="w-9 h-9 flex items-center justify-center relative">
              <i className="ri-notification-line text-xl text-gray-700"></i>
              <span className="absolute top-1.5 right-1.5 w-2 h-2 bg-red-500 rounded-full"></span>
            </Link>
            <Link to="/settings" className="w-9 h-9 flex items-center justify-center">
              <i className="ri-settings-3-line text-xl text-gray-700"></i>
            </Link>
          </div>
        </div>
      </header>

      {/* Profile Section */}
      <div className="pt-16 bg-gradient-to-r from-purple-600 to-pink-600 pb-6">
        <div className="px-4">
          <div className="flex items-center gap-4 mb-4">
            <img
              src={user.avatar}
              alt={user.name}
              className="w-20 h-20 rounded-full object-cover border-4 border-white"
            />
            <div className="flex-1">
              <div className="flex items-center gap-2 mb-1">
                <h2 className="text-white text-xl font-bold">{user.name}</h2>
                <span className="bg-gradient-to-r from-yellow-400 to-orange-500 text-white text-xs px-2 py-1 rounded-full font-medium">
                  {user.membership}
                </span>
              </div>
              <p className="text-white/90 text-sm mb-2">{user.email}</p>
              <div className="flex items-center gap-2">
                <div className="flex-1 bg-white/20 rounded-full h-2">
                  <div className="bg-white rounded-full h-2" style={{ width: '60%' }}></div>
                </div>
                <span className="text-white text-xs font-medium">Lv.{user.level}</span>
              </div>
            </div>
          </div>

          {/* Stats */}
          <div className="grid grid-cols-3 gap-3">
            {stats.map((stat, index) => (
              <div key={index} className="bg-white/20 backdrop-blur-sm rounded-2xl p-3 text-center">
                <i className={`${stat.icon} text-2xl text-white mb-1`}></i>
                <p className="text-white text-lg font-bold">{stat.value}</p>
                <p className="text-white/90 text-xs">{stat.label}</p>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Points Card */}
      <div className="px-4 -mt-3">
        <div className="bg-white rounded-2xl p-4 shadow-lg">
          <div className="flex items-center justify-between mb-3">
            <div>
              <p className="text-gray-600 text-sm mb-1">보유 포인트</p>
              <p className="text-2xl font-bold text-purple-600">{user.points.toLocaleString()}P</p>
            </div>
            <Link
              to="/ads"
              className="bg-gradient-to-r from-purple-600 to-pink-600 text-white px-4 py-2 rounded-full text-sm font-medium"
            >
              포인트 적립
            </Link>
          </div>
          <div className="border-t border-gray-100 pt-3">
            <p className="text-xs text-gray-500 mb-2 font-medium">최근 포인트 내역</p>
            <div className="space-y-2">
              {pointHistory.slice(0, 3).map(item => (
                <div key={item.id} className="flex items-center justify-between text-sm">
                  <div className="flex items-center gap-2">
                    <span className={`w-1.5 h-1.5 rounded-full ${
                      item.type === '적립' ? 'bg-green-500' : 'bg-red-500'
                    }`}></span>
                    <span className="text-gray-700">{item.desc}</span>
                  </div>
                  <span className={`font-medium ${
                    item.type === '적립' ? 'text-green-600' : 'text-red-600'
                  }`}>
                    {item.points}
                  </span>
                </div>
              ))}
            </div>
            <button className="w-full mt-3 text-purple-600 text-sm font-medium">
              전체 내역 보기 <i className="ri-arrow-right-s-line"></i>
            </button>
          </div>
        </div>
      </div>

      {/* Menu List */}
      <div className="px-4 mt-4">
        <div className="bg-white rounded-2xl overflow-hidden shadow-sm">
          {menuItems.map((item, index) => (
            <Link
              key={index}
              to={item.link}
              className={`flex items-center justify-between px-4 py-4 ${
                index !== menuItems.length - 1 ? 'border-b border-gray-100' : ''
              }`}
            >
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-purple-50 rounded-full flex items-center justify-center">
                  <i className={`${item.icon} text-xl text-purple-600`}></i>
                </div>
                <span className="text-gray-900 font-medium">{item.label}</span>
              </div>
              <i className="ri-arrow-right-s-line text-xl text-gray-400"></i>
            </Link>
          ))}
        </div>
      </div>

      {/* Logout Button */}
      <div className="px-4 mt-4">
        <button className="w-full bg-white border border-gray-200 text-gray-700 py-3 rounded-2xl font-medium">
          로그아웃
        </button>
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
