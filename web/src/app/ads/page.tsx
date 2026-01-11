"use client";

import PageHeader from "@/components/layout/PageHeader";
import PageWrapper from "@/components/layout/PageWrapper";
import Card from "@/components/ui/Card";
import Button from "@/components/ui/Button";
import { useState } from "react";

export default function AdsPage() {
  const [points] = useState(2450);

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
    <>
      <PageHeader 
        title="Ads & Rewards" 
        rightAction={
          <button className="w-9 h-9 flex items-center justify-center rounded-full hover:bg-gray-100 transition-colors">
            <i className="ri-history-line text-xl text-gray-700"></i>
          </button>
        }
      />
      <PageWrapper>
        <div className="max-w-6xl mx-auto px-4 py-6">
          {/* Points Banner */}
          <div className="bg-gradient-to-r from-yellow-400 to-orange-500 rounded-2xl p-6 shadow-lg mb-8 text-white relative overflow-hidden">
            <div className="relative z-10 flex items-center justify-between">
              <div>
                <p className="text-white/90 text-sm font-medium mb-1">My Points</p>
                <p className="text-4xl font-bold">{points.toLocaleString()}</p>
              </div>
              <div className="w-16 h-16 bg-white/20 rounded-full flex items-center justify-center backdrop-blur-sm">
                <i className="ri-coin-line text-4xl text-white"></i>
              </div>
            </div>
            <div className="absolute -top-10 -right-10 w-40 h-40 bg-white/10 rounded-full blur-2xl"></div>
          </div>

          <div className="grid lg:grid-cols-3 gap-8">
            <div className="lg:col-span-2 space-y-8">
              {/* Earn Points Section */}
              <section>
                <h2 className="text-lg font-bold text-gray-900 mb-4 px-1">Earn Points</h2>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4" data-testid="reward-missions-skeleton">
                  {adOffers.map(offer => (
                    <div key={offer.id} className={`bg-gradient-to-br ${offer.color} rounded-2xl p-5 text-white relative overflow-hidden group hover:shadow-lg transition-all cursor-pointer`}>
                      <div className="absolute top-3 right-3 bg-black/20 backdrop-blur-sm text-white text-xs px-2 py-1 rounded-full font-medium">
                        {offer.available} left
                      </div>
                      <div className="w-12 h-12 flex items-center justify-center mb-4 bg-white/20 rounded-xl backdrop-blur-sm">
                        <i className={`${offer.icon} text-2xl`}></i>
                      </div>
                      <h3 className="font-bold text-lg mb-1">{offer.title}</h3>
                      <p className="text-xs text-white/90 mb-4 line-clamp-2 min-h-[2.5em]">{offer.description}</p>
                      <div className="flex items-center justify-between mt-auto">
                        <span className="text-xl font-bold flex items-center gap-1">
                          <i className="ri-add-line text-sm"></i>{offer.reward} P
                        </span>
                        <button className="bg-white/20 hover:bg-white/30 backdrop-blur-sm px-4 py-1.5 rounded-full text-xs font-bold transition-colors">
                          Start
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              </section>

              {/* Rewards Shop */}
              <section>
                <div className="flex items-center justify-between mb-4 px-1">
                  <h2 className="text-lg font-bold text-gray-900">Redeem Rewards</h2>
                  <button className="text-sm text-purple-600 font-medium hover:text-purple-700">View All</button>
                </div>
                <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
                  {rewards.map(reward => (
                    <Card key={reward.id} className="overflow-hidden border border-gray-100 hover:shadow-md transition-all group">
                      <div className="relative aspect-square">
                        {/* eslint-disable-next-line @next/next/no-img-element */}
                        <img
                          src={reward.image}
                          alt={reward.name}
                          className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                        />
                        <div className={`absolute top-2 right-2 text-white text-[10px] px-2 py-1 rounded-full font-bold shadow-sm ${
                          reward.stock === 'Limited' ? 'bg-red-500' : 'bg-green-500'
                        }`}>
                          {reward.stock}
                        </div>
                      </div>
                      <div className="p-3">
                        <h3 className="font-bold text-gray-900 text-sm line-clamp-2 h-10 mb-2 leading-tight">{reward.name}</h3>
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-1 text-orange-500">
                            <i className="ri-coin-fill text-sm"></i>
                            <span className="font-bold text-sm">{reward.points.toLocaleString()}</span>
                          </div>
                        </div>
                        <Button size="sm" variant="secondary" className="w-full mt-3 h-8 text-xs">
                          Redeem
                        </Button>
                      </div>
                    </Card>
                  ))}
                </div>
              </section>
            </div>

            {/* Sidebar (Recent Earnings) */}
            <div className="lg:col-span-1">
              <div className="sticky top-24">
                <h2 className="text-lg font-bold text-gray-900 mb-4 px-1">Recent Earnings</h2>
                <Card className="divide-y divide-gray-100 border border-gray-100">
                  {recentEarnings.map((earning) => (
                    <div
                      key={earning.id}
                      className="flex items-center justify-between p-4 hover:bg-gray-50 transition-colors"
                    >
                      <div>
                        <p className="text-sm font-bold text-gray-900">{earning.activity}</p>
                        <p className="text-xs text-gray-500 mt-1 flex items-center gap-1">
                          <i className="ri-time-line"></i> {earning.time}
                        </p>
                      </div>
                      <span className="text-green-600 font-bold bg-green-50 px-2 py-1 rounded-lg text-sm">+{earning.points}</span>
                    </div>
                  ))}
                </Card>

                <div className="mt-6 bg-purple-50 rounded-2xl p-5 text-center">
                  <div className="w-12 h-12 bg-white rounded-full flex items-center justify-center mx-auto mb-3 text-purple-600 shadow-sm">
                    <i className="ri-gift-2-fill text-2xl"></i>
                  </div>
                  <h3 className="font-bold text-gray-900 mb-1">Invite Friends</h3>
                  <p className="text-sm text-gray-600 mb-4">Get 500 points for every friend you invite!</p>
                  <Button className="w-full shadow-md">Invite Now</Button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </PageWrapper>
    </>
  );
}