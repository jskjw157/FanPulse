import { useState } from 'react';
import { Link } from 'react-router-dom';

interface Artist {
  id: number;
  name: string;
  category: string;
  image: string;
  followers: string;
  isNotificationOn: boolean;
  latestNews: string;
}

export default function FavoritesPage() {
  const [artists, setArtists] = useState<Artist[]>([
    {
      id: 1,
      name: 'BTS',
      category: 'Boy Group',
      image: 'https://readdy.ai/api/search-image?query=professional%20kpop%20boy%20group%20seven%20members%2C%20dynamic%20group%20pose%2C%20modern%20stylish%20outfits%2C%20studio%20photography%2C%20vibrant%20colors%2C%20high%20fashion%20aesthetic%2C%20clean%20white%20background%2C%20centered%20composition&width=400&height=400&seq=fav001&orientation=squarish',
      followers: '2.5M',
      isNotificationOn: true,
      latestNews: '새 앨범 발매 예정'
    },
    {
      id: 2,
      name: 'BLACKPINK',
      category: 'Girl Group',
      image: 'https://readdy.ai/api/search-image?query=professional%20kpop%20girl%20group%20four%20members%2C%20elegant%20group%20pose%2C%20chic%20fashionable%20outfits%2C%20studio%20photography%2C%20sophisticated%20colors%2C%20high%20fashion%20aesthetic%2C%20clean%20white%20background%2C%20centered%20composition&width=400&height=400&seq=fav002&orientation=squarish',
      followers: '2.3M',
      isNotificationOn: true,
      latestNews: '월드투어 티켓 오픈'
    },
    {
      id: 3,
      name: 'IU',
      category: 'Solo Artist',
      image: 'https://readdy.ai/api/search-image?query=professional%20kpop%20female%20solo%20artist%20portrait%2C%20elegant%20pose%2C%20sophisticated%20outfit%2C%20studio%20photography%2C%20soft%20lighting%2C%20clean%20white%20background%2C%20centered%20composition%2C%20high%20fashion%20aesthetic&width=400&height=400&seq=fav003&orientation=squarish',
      followers: '1.8M',
      isNotificationOn: false,
      latestNews: '드라마 OST 발매'
    },
    {
      id: 4,
      name: 'SEVENTEEN',
      category: 'Boy Group',
      image: 'https://readdy.ai/api/search-image?query=professional%20kpop%20boy%20group%20thirteen%20members%2C%20energetic%20group%20pose%2C%20colorful%20stylish%20outfits%2C%20studio%20photography%2C%20vibrant%20aesthetic%2C%20clean%20white%20background%2C%20centered%20composition&width=400&height=400&seq=fav004&orientation=squarish',
      followers: '1.5M',
      isNotificationOn: true,
      latestNews: '콘서트 일정 공개'
    },
    {
      id: 5,
      name: 'NewJeans',
      category: 'Girl Group',
      image: 'https://readdy.ai/api/search-image?query=professional%20kpop%20girl%20group%20five%20members%2C%20fresh%20youthful%20pose%2C%20trendy%20casual%20outfits%2C%20studio%20photography%2C%20bright%20colors%2C%20modern%20aesthetic%2C%20clean%20white%20background%2C%20centered%20composition&width=400&height=400&seq=fav005&orientation=squarish',
      followers: '1.2M',
      isNotificationOn: true,
      latestNews: '신곡 티저 공개'
    },
    {
      id: 6,
      name: 'Stray Kids',
      category: 'Boy Group',
      image: 'https://readdy.ai/api/search-image?query=professional%20kpop%20boy%20group%20eight%20members%2C%20powerful%20group%20pose%2C%20edgy%20street%20fashion%20outfits%2C%20studio%20photography%2C%20bold%20colors%2C%20urban%20aesthetic%2C%20clean%20white%20background%2C%20centered%20composition&width=400&height=400&seq=fav006&orientation=squarish',
      followers: '1.1M',
      isNotificationOn: false,
      latestNews: '해외 공연 확정'
    }
  ]);

  const handleUnfollow = (id: number) => {
    setArtists(artists.filter(artist => artist.id !== id));
  };

  const toggleNotification = (id: number) => {
    setArtists(artists.map(artist => 
      artist.id === id 
        ? { ...artist, isNotificationOn: !artist.isNotificationOn }
        : artist
    ));
  };

  return (
    <div className="min-h-screen bg-gray-50 pb-20">
      {/* Header */}
      <header className="fixed top-0 left-0 right-0 bg-white border-b border-gray-200 z-50">
        <div className="px-4 py-3 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <Link to="/mypage" className="w-9 h-9 flex items-center justify-center">
              <i className="ri-arrow-left-line text-xl text-gray-900"></i>
            </Link>
            <h1 className="text-base font-bold text-gray-900">좋아요한 아티스트</h1>
          </div>
          <Link to="/search" className="w-9 h-9 flex items-center justify-center">
            <i className="ri-search-line text-xl text-gray-700"></i>
          </Link>
        </div>
      </header>

      {/* Content */}
      <div className="pt-16 px-4">
        {/* Stats */}
        <div className="bg-gradient-to-r from-purple-600 to-pink-600 rounded-2xl p-4 mb-4">
          <div className="flex items-center justify-between text-white">
            <div>
              <p className="text-white/90 text-sm mb-1">팔로우 중인 아티스트</p>
              <p className="text-3xl font-bold">{artists.length}</p>
            </div>
            <div className="w-16 h-16 bg-white/20 rounded-full flex items-center justify-center">
              <i className="ri-heart-fill text-3xl text-white"></i>
            </div>
          </div>
        </div>

        {/* Filter Tabs */}
        <div className="flex gap-2 mb-4 overflow-x-auto pb-2">
          <button className="px-4 py-2 bg-purple-600 text-white rounded-full text-sm font-medium whitespace-nowrap">
            전체 ({artists.length})
          </button>
          <button className="px-4 py-2 bg-white text-gray-700 rounded-full text-sm font-medium whitespace-nowrap border border-gray-200">
            Boy Group
          </button>
          <button className="px-4 py-2 bg-white text-gray-700 rounded-full text-sm font-medium whitespace-nowrap border border-gray-200">
            Girl Group
          </button>
          <button className="px-4 py-2 bg-white text-gray-700 rounded-full text-sm font-medium whitespace-nowrap border border-gray-200">
            Solo
          </button>
        </div>

        {/* Artist List */}
        {artists.length > 0 ? (
          <div className="space-y-3">
            {artists.map((artist) => (
              <div key={artist.id} className="bg-white rounded-2xl overflow-hidden shadow-sm">
                <div className="flex gap-3 p-3">
                  <Link to="/artist-detail" className="flex-shrink-0">
                    <img
                      src={artist.image}
                      alt={artist.name}
                      className="w-20 h-20 rounded-xl object-cover"
                    />
                  </Link>
                  <div className="flex-1 min-w-0">
                    <Link to="/artist-detail">
                      <h3 className="text-base font-bold text-gray-900 mb-1">{artist.name}</h3>
                      <p className="text-xs text-gray-500 mb-2">{artist.category}</p>
                    </Link>
                    <div className="flex items-center gap-2 mb-2">
                      <i className="ri-user-follow-line text-sm text-gray-400"></i>
                      <span className="text-xs text-gray-600">{artist.followers} 팔로워</span>
                    </div>
                    <div className="flex items-center gap-1">
                      <span className="w-1.5 h-1.5 bg-green-500 rounded-full"></span>
                      <p className="text-xs text-gray-700 truncate">{artist.latestNews}</p>
                    </div>
                  </div>
                  <div className="flex flex-col gap-2">
                    <button
                      onClick={() => toggleNotification(artist.id)}
                      className={`w-9 h-9 rounded-full flex items-center justify-center ${
                        artist.isNotificationOn 
                          ? 'bg-purple-100 text-purple-600' 
                          : 'bg-gray-100 text-gray-400'
                      }`}
                    >
                      <i className={`${
                        artist.isNotificationOn 
                          ? 'ri-notification-fill' 
                          : 'ri-notification-off-line'
                      } text-lg`}></i>
                    </button>
                    <button
                      onClick={() => handleUnfollow(artist.id)}
                      className="w-9 h-9 bg-red-50 text-red-500 rounded-full flex items-center justify-center"
                    >
                      <i className="ri-heart-fill text-lg"></i>
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="flex flex-col items-center justify-center py-20">
            <div className="w-24 h-24 bg-gray-100 rounded-full flex items-center justify-center mb-4">
              <i className="ri-heart-line text-5xl text-gray-300"></i>
            </div>
            <p className="text-gray-500 text-base mb-2">좋아요한 아티스트가 없습니다</p>
            <p className="text-gray-400 text-sm mb-6">관심있는 아티스트를 팔로우해보세요</p>
            <Link
              to="/"
              className="bg-gradient-to-r from-purple-600 to-pink-600 text-white px-6 py-3 rounded-full font-medium"
            >
              아티스트 둘러보기
            </Link>
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
