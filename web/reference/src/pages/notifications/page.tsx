import { useState } from 'react';
import { Link } from 'react-router-dom';
import Image from 'next/image';

export default function Notifications() {
  const [filter, setFilter] = useState('all');

  const notifications = [
    {
      id: 1,
      type: 'like',
      user: 'ARMY_Forever',
      avatar: 'https://readdy.ai/api/search-image?query=icon%2C%20cute%20purple%20heart%20avatar%2C%202.5D%20illustration%20style%2C%20the%20icon%20should%20take%20up%2070%20percent%20of%20the%20frame%2C%20isolated%20on%20white%20background%2C%20centered%20composition%2C%20soft%20lighting%2C%20vibrant%20colors&width=100&height=100&seq=noti001&orientation=squarish',
      content: '님이 회원님의 게시글을 좋아합니다.',
      time: '5분 전',
      isRead: false,
      link: '/post-detail?id=1'
    },
    {
      id: 2,
      type: 'comment',
      user: 'Blink_Girl',
      avatar: 'https://readdy.ai/api/search-image?query=icon%2C%20cute%20pink%20crown%20avatar%2C%202.5D%20illustration%20style%2C%20the%20icon%20should%20take%20up%2070%20percent%20of%20the%20frame%2C%20isolated%20on%20white%20background%2C%20centered%20composition%2C%20soft%20lighting%2C%20vibrant%20colors&width=100&height=100&seq=noti002&orientation=squarish',
      content: '님이 회원님의 게시글에 댓글을 남겼습니다: "정말 멋진 글이네요!"',
      time: '1시간 전',
      isRead: false,
      link: '/post-detail?id=1'
    },
    {
      id: 3,
      type: 'vote',
      user: 'FanPulse',
      avatar: 'https://readdy.ai/api/search-image?query=icon%2C%20purple%20trophy%20award%2C%202.5D%20illustration%20style%2C%20the%20icon%20should%20take%20up%2070%20percent%20of%20the%20frame%2C%20isolated%20on%20white%20background%2C%20centered%20composition%2C%20soft%20lighting%2C%20vibrant%20colors&width=100&height=100&seq=noti003&orientation=squarish',
      content: 'BTS 투표가 곧 마감됩니다! 지금 참여하세요.',
      time: '2시간 전',
      isRead: true,
      link: '/voting'
    },
    {
      id: 4,
      type: 'event',
      user: 'FanPulse',
      avatar: 'https://readdy.ai/api/search-image?query=icon%2C%20colorful%20gift%20box%2C%202.5D%20illustration%20style%2C%20the%20icon%20should%20take%20up%2070%20percent%20of%20the%20frame%2C%20isolated%20on%20white%20background%2C%20centered%20composition%2C%20soft%20lighting%2C%20vibrant%20colors&width=100&height=100&seq=noti004&orientation=squarish',
      content: '새로운 이벤트가 시작되었습니다! 포인트 2배 적립 찬스!',
      time: '3시간 전',
      isRead: true,
      link: '/ads'
    },
    {
      id: 5,
      type: 'follow',
      user: 'Kpop_Lover',
      avatar: 'https://readdy.ai/api/search-image?query=icon%2C%20cute%20blue%20star%20avatar%2C%202.5D%20illustration%20style%2C%20the%20icon%20should%20take%20up%2070%20percent%20of%20the%20frame%2C%20isolated%20on%20white%20background%2C%20centered%20composition%2C%20soft%20lighting%2C%20vibrant%20colors&width=100&height=100&seq=noti005&orientation=squarish',
      content: '님이 회원님을 팔로우하기 시작했습니다.',
      time: '5시간 전',
      isRead: true,
      link: '/mypage'
    },
    {
      id: 6,
      type: 'concert',
      user: 'FanPulse',
      avatar: 'https://readdy.ai/api/search-image?query=icon%2C%20concert%20ticket%2C%202.5D%20illustration%20style%2C%20the%20icon%20should%20take%20up%2070%20percent%20of%20the%20frame%2C%20isolated%20on%20white%20background%2C%20centered%20composition%2C%20soft%20lighting%2C%20vibrant%20colors&width=100&height=100&seq=noti006&orientation=squarish',
      content: 'BLACKPINK 콘서트 티켓 예매가 시작되었습니다!',
      time: '1일 전',
      isRead: true,
      link: '/concert'
    },
    {
      id: 7,
      type: 'news',
      user: 'FanPulse',
      avatar: 'https://readdy.ai/api/search-image?query=icon%2C%20newspaper%2C%202.5D%20illustration%20style%2C%20the%20icon%20should%20take%20up%2070%20percent%20of%20the%20frame%2C%20isolated%20on%20white%20background%2C%20centered%20composition%2C%20soft%20lighting%2C%20vibrant%20colors&width=100&height=100&seq=noti007&orientation=squarish',
      content: 'BTS 새 앨범 발매 소식이 업데이트되었습니다.',
      time: '2일 전',
      isRead: true,
      link: '/news-detail?id=1'
    }
  ];

  const getTypeIcon = (type: string) => {
    switch (type) {
      case 'like': return 'ri-heart-fill text-pink-500';
      case 'comment': return 'ri-chat-3-fill text-blue-500';
      case 'vote': return 'ri-trophy-fill text-yellow-500';
      case 'event': return 'ri-gift-fill text-purple-500';
      case 'follow': return 'ri-user-add-fill text-green-500';
      case 'concert': return 'ri-ticket-fill text-orange-500';
      case 'news': return 'ri-newspaper-fill text-indigo-500';
      default: return 'ri-notification-fill text-gray-500';
    }
  };

  const filteredNotifications = filter === 'all' 
    ? notifications 
    : notifications.filter(n => !n.isRead);

  return (
    <div className="min-h-screen bg-white pb-20">
      {/* Header */}
      <header className="fixed top-0 left-0 right-0 bg-white border-b border-gray-200 z-50">
        <div className="px-4 py-3 flex items-center justify-between">
          <Link to="/" className="w-9 h-9 flex items-center justify-center">
            <i className="ri-arrow-left-line text-xl text-gray-900"></i>
          </Link>
          <h1 className="text-base font-bold text-gray-900">알림</h1>
          <Link to="/settings" className="w-9 h-9 flex items-center justify-center">
            <i className="ri-settings-3-line text-xl text-gray-700"></i>
          </Link>
        </div>

        {/* Filter Tabs */}
        <div className="px-4 py-2 flex gap-2">
          <button 
            onClick={() => setFilter('all')}
            className={`flex-1 py-2 rounded-full text-sm font-medium transition-colors ${
              filter === 'all' 
                ? 'bg-purple-600 text-white' 
                : 'bg-gray-100 text-gray-600'
            }`}
          >
            전체
          </button>
          <button 
            onClick={() => setFilter('unread')}
            className={`flex-1 py-2 rounded-full text-sm font-medium transition-colors ${
              filter === 'unread' 
                ? 'bg-purple-600 text-white' 
                : 'bg-gray-100 text-gray-600'
            }`}
          >
            읽지 않음
          </button>
        </div>
      </header>

      {/* Content */}
      <div className="pt-32">
        {/* Mark All as Read */}
        {filteredNotifications.some(n => !n.isRead) && (
          <div className="px-4 py-3 border-b border-gray-200">
            <button className="text-sm text-purple-600 font-medium">
              모두 읽음 처리
            </button>
          </div>
        )}

        {/* Notifications List */}
        <div className="divide-y divide-gray-200">
          {filteredNotifications.map(notification => (
            <Link 
              key={notification.id}
              to={notification.link}
              className={`flex items-start gap-3 px-4 py-4 ${
                !notification.isRead ? 'bg-purple-50' : 'bg-white'
              }`}
            >
              <div className="relative flex-shrink-0">
                <Image
                  src={notification.avatar}
                  alt={notification.user}
                  width={48}
                  height={48}
                  className="w-12 h-12 rounded-full object-cover"
                />
                <div className="absolute -bottom-1 -right-1 w-5 h-5 bg-white rounded-full flex items-center justify-center">
                  <i className={`${getTypeIcon(notification.type)} text-xs`}></i>
                </div>
              </div>

              <div className="flex-1 min-w-0">
                <p className="text-sm text-gray-900 leading-relaxed">
                  <span className="font-bold">{notification.user}</span>
                  {notification.content}
                </p>
                <p className="text-xs text-gray-500 mt-1">{notification.time}</p>
              </div>

              {!notification.isRead && (
                <div className="w-2 h-2 bg-purple-600 rounded-full flex-shrink-0 mt-2"></div>
              )}
            </Link>
          ))}
        </div>

        {/* Empty State */}
        {filteredNotifications.length === 0 && (
          <div className="flex flex-col items-center justify-center py-16 px-4">
            <div className="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mb-4">
              <i className="ri-notification-off-line text-4xl text-gray-400"></i>
            </div>
            <p className="text-gray-500 text-sm">읽지 않은 알림이 없습니다</p>
          </div>
        )}
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
          <Link to="/mypage" className="flex flex-col items-center justify-center text-gray-500">
            <i className="ri-user-line text-xl"></i>
            <span className="text-xs mt-1">My</span>
          </Link>
        </div>
      </nav>
    </div>
  );
}
