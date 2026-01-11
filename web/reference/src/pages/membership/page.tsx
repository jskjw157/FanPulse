import { useState } from 'react';
import { Link } from 'react-router-dom';

export default function Membership() {
  const [isVIP, setIsVIP] = useState(false);

  const vipBenefits = [
    {
      icon: 'ri-trophy-line',
      title: 'Extra Voting Power',
      description: '+5 daily votes for all polls'
    },
    {
      icon: 'ri-vip-crown-line',
      title: 'Exclusive Content',
      description: 'Behind-the-scenes videos & photos'
    },
    {
      icon: 'ri-discount-percent-line',
      title: 'Special Discounts',
      description: '20% off on all merchandise'
    },
    {
      icon: 'ri-chat-private-line',
      title: 'Priority Support',
      description: 'Fast response from our team'
    },
    {
      icon: 'ri-gift-line',
      title: 'Monthly Rewards',
      description: 'Free points & exclusive gifts'
    },
    {
      icon: 'ri-star-line',
      title: 'VIP Badge',
      description: 'Stand out in the community'
    }
  ];

  const plans = [
    {
      id: 1,
      name: 'Monthly',
      price: 9.99,
      period: 'month',
      popular: false,
      savings: null
    },
    {
      id: 2,
      name: 'Quarterly',
      price: 24.99,
      period: '3 months',
      popular: true,
      savings: 'Save 17%'
    },
    {
      id: 3,
      name: 'Yearly',
      price: 79.99,
      period: 'year',
      popular: false,
      savings: 'Save 33%'
    }
  ];

  const stats = [
    { label: 'Total Votes', value: '1,247', icon: 'ri-trophy-line' },
    { label: 'Posts', value: '89', icon: 'ri-chat-3-line' },
    { label: 'Points', value: '2,450', icon: 'ri-coin-line' },
    { label: 'Following', value: '12', icon: 'ri-user-star-line' }
  ];

  const activities = [
    {
      id: 1,
      type: 'vote',
      title: 'Voted for BTS',
      subtitle: 'Best Male Group 2024',
      time: '2 hours ago',
      icon: 'ri-trophy-line',
      color: 'bg-purple-100 text-purple-600'
    },
    {
      id: 2,
      type: 'post',
      title: 'Posted in Community',
      subtitle: 'Just watched the new MV...',
      time: '5 hours ago',
      icon: 'ri-chat-3-line',
      color: 'bg-blue-100 text-blue-600'
    },
    {
      id: 3,
      type: 'reward',
      title: 'Earned 50 Points',
      subtitle: 'Completed survey',
      time: '1 day ago',
      icon: 'ri-coin-line',
      color: 'bg-orange-100 text-orange-600'
    }
  ];

  return (
    <div className="min-h-screen bg-gray-50 pb-20">
      {/* Header */}
      <header className="fixed top-0 left-0 right-0 bg-white shadow-sm z-50">
        <div className="px-4 py-3 flex items-center justify-between">
          <h1 className="text-xl font-bold text-gray-900">My Profile</h1>
          <button className="w-9 h-9 flex items-center justify-center">
            <i className="ri-settings-3-line text-xl text-gray-700"></i>
          </button>
        </div>
      </header>

      {/* Main Content */}
      <div className="pt-16">
        {/* Profile Section */}
        <div className="bg-gradient-to-r from-purple-600 to-pink-600 px-4 pt-6 pb-8">
          <div className="flex items-center gap-4 mb-6">
            <img
              src="https://readdy.ai/api/search-image?query=young%20person%20profile%20photo%2C%20friendly%20smile%2C%20casual%20style%2C%20professional%20portrait%20photography%2C%20natural%20lighting%2C%20clean%20background&width=200&height=200&seq=profile001&orientation=squarish"
              alt="Profile"
              className="w-20 h-20 rounded-full object-cover border-4 border-white"
            />
            <div className="flex-1">
              <div className="flex items-center gap-2 mb-1">
                <h2 className="text-white text-xl font-bold">Alex Kim</h2>
                {isVIP && (
                  <span className="bg-yellow-400 text-yellow-900 text-xs px-2 py-0.5 rounded-full font-medium">
                    VIP
                  </span>
                )}
              </div>
              <p className="text-white/90 text-sm">@alexkim_fanpulse</p>
              <p className="text-white/80 text-xs mt-1">Member since Dec 2023</p>
            </div>
          </div>

          {/* Stats */}
          <div className="grid grid-cols-4 gap-2">
            {stats.map((stat, index) => (
              <div key={index} className="bg-white/10 backdrop-blur-sm rounded-xl p-3 text-center">
                <i className={`${stat.icon} text-white text-xl mb-1`}></i>
                <p className="text-white font-bold text-sm">{stat.value}</p>
                <p className="text-white/80 text-xs mt-0.5">{stat.label}</p>
              </div>
            ))}
          </div>
        </div>

        {/* VIP Section */}
        {!isVIP && (
          <div className="px-4 -mt-4">
            <div className="bg-gradient-to-br from-yellow-400 to-orange-500 rounded-2xl p-5 shadow-lg">
              <div className="flex items-center gap-3 mb-4">
                <div className="w-12 h-12 bg-white/20 rounded-full flex items-center justify-center">
                  <i className="ri-vip-crown-fill text-3xl text-white"></i>
                </div>
                <div>
                  <h3 className="text-white font-bold text-lg">Upgrade to VIP</h3>
                  <p className="text-white/90 text-sm">Unlock exclusive benefits</p>
                </div>
              </div>
              <button className="w-full bg-white text-orange-600 font-bold py-3 rounded-full">
                See Plans
              </button>
            </div>
          </div>
        )}

        {/* VIP Benefits */}
        <div className="px-4 mt-6">
          <h3 className="text-lg font-bold text-gray-900 mb-3">VIP Benefits</h3>
          <div className="grid grid-cols-2 gap-3">
            {vipBenefits.map((benefit, index) => (
              <div key={index} className="bg-white rounded-xl p-4 shadow-sm">
                <div className="w-10 h-10 bg-purple-100 rounded-full flex items-center justify-center mb-3">
                  <i className={`${benefit.icon} text-xl text-purple-600`}></i>
                </div>
                <h4 className="font-medium text-gray-900 text-sm mb-1">{benefit.title}</h4>
                <p className="text-xs text-gray-500 line-clamp-2">{benefit.description}</p>
              </div>
            ))}
          </div>
        </div>

        {/* Pricing Plans */}
        <div className="px-4 mt-6">
          <h3 className="text-lg font-bold text-gray-900 mb-3">Choose Your Plan</h3>
          <div className="space-y-3">
            {plans.map(plan => (
              <div
                key={plan.id}
                className={`bg-white rounded-2xl p-4 shadow-sm relative ${
                  plan.popular ? 'border-2 border-purple-600' : ''
                }`}
              >
                {plan.popular && (
                  <div className="absolute -top-2 left-1/2 -translate-x-1/2 bg-purple-600 text-white text-xs px-3 py-1 rounded-full font-medium">
                    Most Popular
                  </div>
                )}
                <div className="flex items-center justify-between">
                  <div>
                    <h4 className="font-bold text-gray-900">{plan.name}</h4>
                    <p className="text-sm text-gray-500 mt-1">{plan.period}</p>
                    {plan.savings && (
                      <span className="inline-block bg-green-100 text-green-700 text-xs px-2 py-0.5 rounded-full mt-2 font-medium">
                        {plan.savings}
                      </span>
                    )}
                  </div>
                  <div className="text-right">
                    <p className="text-3xl font-bold text-gray-900">${plan.price}</p>
                    <button className="mt-2 bg-gradient-to-r from-purple-600 to-pink-600 text-white px-6 py-2 rounded-full text-sm font-medium">
                      Subscribe
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Recent Activity */}
        <div className="px-4 mt-6 mb-6">
          <h3 className="text-lg font-bold text-gray-900 mb-3">Recent Activity</h3>
          <div className="bg-white rounded-2xl overflow-hidden shadow-sm">
            {activities.map((activity, index) => (
              <div
                key={activity.id}
                className={`flex items-center gap-3 p-4 ${
                  index !== activities.length - 1 ? 'border-b border-gray-100' : ''
                }`}
              >
                <div className={`w-10 h-10 rounded-full flex items-center justify-center ${activity.color}`}>
                  <i className={`${activity.icon} text-lg`}></i>
                </div>
                <div className="flex-1">
                  <p className="text-sm font-medium text-gray-900">{activity.title}</p>
                  <p className="text-xs text-gray-500 mt-0.5">{activity.subtitle}</p>
                </div>
                <span className="text-xs text-gray-400">{activity.time}</span>
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
