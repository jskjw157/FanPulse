import { useState } from 'react';
import { Link } from 'react-router-dom';

export default function Ads() {
  const [points, setPoints] = useState(2450);

  const adOffers = [
    {
      id: 1,
      title: 'Watch 30s Video Ad',
      description: 'Earn points by watching short videos',
      reward: 10,
      icon: 'ri-play-circle-line',
      color: 'from-blue-500 to-cyan-500',
      available: 5
    },
    {
      id: 2,
      title: 'Complete Survey',
      description: 'Share your opinion and earn more',
      reward: 50,
      icon: 'ri-questionnaire-line',
      color: 'from-green-500 to-emerald-500',
      available: 3
    },
    {
      id: 3,
      title: 'Install Partner App',
      description: 'Download and try new apps',
      reward: 100,
      icon: 'ri-download-cloud-line',
      color: 'from-purple-500 to-pink-500',
      available: 2
    },
    {
      id: 4,
      title: 'Daily Check-in',
      description: 'Login daily to earn bonus points',
      reward: 20,
      icon: 'ri-calendar-check-line',
      color: 'from-orange-500 to-red-500',
      available: 1
    }
  ];

  const rewards = [
    {
      id: 1,
      name: 'BTS Official Lightstick',
      points: 5000,
      image: 'https://readdy.ai/api/search-image?query=KPOP%20official%20lightstick%2C%20BTS%20army%20bomb%2C%20purple%20glow%2C%20professional%20product%20photography%2C%20clean%20white%20background%2C%20centered%20composition%2C%20high%20quality%20details&width=400&height=400&seq=reward001&orientation=squarish',
      stock: 'Limited'
    },
    {
      id: 2,
      name: 'BLACKPINK Photo Card Set',
      points: 2000,
      image: 'https://readdy.ai/api/search-image?query=KPOP%20photo%20card%20collection%20set%2C%20premium%20quality%20cards%2C%20pink%20and%20black%20theme%2C%20professional%20product%20photography%2C%20clean%20white%20background%2C%20elegant%20display&width=400&height=400&seq=reward002&orientation=squarish',
      stock: 'In Stock'
    },
    {
      id: 3,
      name: 'NewJeans Album (Signed)',
      points: 8000,
      image: 'https://readdy.ai/api/search-image?query=KPOP%20signed%20album%2C%20autographed%20CD%20case%2C%20pastel%20colors%2C%20professional%20product%20photography%2C%20clean%20white%20background%2C%20premium%20quality&width=400&height=400&seq=reward003&orientation=squarish',
      stock: 'Limited'
    },
    {
      id: 4,
      name: 'Concert Ticket Discount 20%',
      points: 1500,
      image: 'https://readdy.ai/api/search-image?query=concert%20ticket%20voucher%2C%20golden%20ticket%20design%2C%20professional%20graphic%20design%2C%20clean%20white%20background%2C%20elegant%20composition%2C%20premium%20quality&width=400&height=400&seq=reward004&orientation=squarish',
      stock: 'In Stock'
    },
    {
      id: 5,
      name: 'Official T-Shirt',
      points: 3000,
      image: 'https://readdy.ai/api/search-image?query=KPOP%20official%20merchandise%20t-shirt%2C%20folded%20shirt%20display%2C%20professional%20product%20photography%2C%20clean%20white%20background%2C%20high%20quality%20fabric&width=400&height=400&seq=reward005&orientation=squarish',
      stock: 'In Stock'
    },
    {
      id: 6,
      name: 'VIP Membership 1 Month',
      points: 1000,
      image: 'https://readdy.ai/api/search-image?query=VIP%20membership%20card%2C%20golden%20premium%20card%2C%20luxury%20design%2C%20professional%20product%20photography%2C%20clean%20white%20background%2C%20elegant%20composition&width=400&height=400&seq=reward006&orientation=squarish',
      stock: 'In Stock'
    }
  ];

  const recentEarnings = [
    { id: 1, activity: 'Watched Video Ad', points: 10, time: '5 min ago' },
    { id: 2, activity: 'Daily Check-in', points: 20, time: '2 hours ago' },
    { id: 3, activity: 'Completed Survey', points: 50, time: '1 day ago' }
  ];

  return (
    <div className="min-h-screen bg-gray-50 pb-20">
      {/* Header */}
      <header className="fixed top-0 left-0 right-0 bg-white shadow-sm z-50">
        <div className="px-4 py-3">
          <div className="flex items-center justify-between mb-3">
            <h1 className="text-xl font-bold text-gray-900">Ads & Rewards</h1>
            <button className="w-9 h-9 flex items-center justify-center">
              <i className="ri-history-line text-xl text-gray-700"></i>
            </button>
          </div>

          {/* Points Card */}
          <div className="bg-gradient-to-r from-yellow-400 to-orange-500 rounded-2xl p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-white/90 text-sm">My Points</p>
                <p className="text-white text-3xl font-bold mt-1">{points.toLocaleString()}</p>
              </div>
              <div className="w-16 h-16 bg-white/20 rounded-full flex items-center justify-center">
                <i className="ri-coin-line text-4xl text-white"></i>
              </div>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <div className="pt-40 px-4">
        {/* Earn Points Section */}
        <div className="mb-6">
          <h2 className="text-lg font-bold text-gray-900 mb-3">Earn Points</h2>
          <div className="grid grid-cols-2 gap-3">
            {adOffers.map(offer => (
              <div key={offer.id} className={`bg-gradient-to-br ${offer.color} rounded-2xl p-4 text-white relative overflow-hidden`}>
                <div className="absolute top-2 right-2 bg-white/20 text-white text-xs px-2 py-1 rounded-full">
                  {offer.available} left
                </div>
                <div className="w-12 h-12 flex items-center justify-center mb-3">
                  <i className={`${offer.icon} text-4xl`}></i>
                </div>
                <h3 className="font-bold text-sm mb-1">{offer.title}</h3>
                <p className="text-xs text-white/90 mb-3 line-clamp-2">{offer.description}</p>
                <div className="flex items-center justify-between">
                  <span className="text-lg font-bold">+{offer.reward}</span>
                  <button
                    onClick={() => setPoints(prev => prev + offer.reward)}
                    className="bg-white/20 backdrop-blur-sm px-3 py-1 rounded-full text-xs font-medium"
                  >
                    Start
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Recent Earnings */}
        <div className="mb-6">
          <h2 className="text-lg font-bold text-gray-900 mb-3">Recent Earnings</h2>
          <div className="bg-white rounded-2xl overflow-hidden shadow-sm">
            {recentEarnings.map((earning, index) => (
              <div
                key={earning.id}
                className={`flex items-center justify-between p-4 ${
                  index !== recentEarnings.length - 1 ? 'border-b border-gray-100' : ''
                }`}
              >
                <div>
                  <p className="text-sm font-medium text-gray-900">{earning.activity}</p>
                  <p className="text-xs text-gray-500 mt-1">{earning.time}</p>
                </div>
                <span className="text-green-600 font-bold">+{earning.points}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Rewards Shop */}
        <div>
          <div className="flex items-center justify-between mb-3">
            <h2 className="text-lg font-bold text-gray-900">Redeem Rewards</h2>
            <button className="text-sm text-purple-600 font-medium">View All</button>
          </div>
          <div className="grid grid-cols-2 gap-3">
            {rewards.map(reward => (
              <div key={reward.id} className="bg-white rounded-2xl overflow-hidden shadow-sm">
                <div className="relative">
                  <img
                    src={reward.image}
                    alt={reward.name}
                    className="w-full h-40 object-cover object-top"
                  />
                  <div className={`absolute top-2 right-2 text-white text-xs px-2 py-1 rounded-full font-medium ${
                    reward.stock === 'Limited' ? 'bg-red-600' : 'bg-green-600'
                  }`}>
                    {reward.stock}
                  </div>
                </div>
                <div className="p-3">
                  <h3 className="font-medium text-gray-900 text-sm line-clamp-2 h-10">{reward.name}</h3>
                  <div className="flex items-center justify-between mt-3">
                    <div className="flex items-center gap-1 text-orange-600">
                      <i className="ri-coin-line text-lg"></i>
                      <span className="font-bold text-sm">{reward.points.toLocaleString()}</span>
                    </div>
                    <button className="bg-purple-600 text-white px-3 py-1 rounded-full text-xs font-medium">
                      Redeem
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
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
          <Link to="/mypage" className="flex flex-col items-center justify-center text-gray-500">
            <i className="ri-user-line text-xl"></i>
            <span className="text-xs mt-1">My</span>
          </Link>
        </div>
      </nav>
    </div>
  );
}
