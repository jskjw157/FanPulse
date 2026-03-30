"use client";

import PageHeader from "@/components/layout/PageHeader";
import PageWrapper from "@/components/layout/PageWrapper";
import Card from "@/components/ui/Card";
import Button from "@/components/ui/Button";
import { useState } from "react";

export default function MembershipPage() {
  const [isVIP] = useState(false);

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
    <>
      <PageHeader title="My Profile" />
      <PageWrapper>
        {/* Profile Section */}
        <div className="bg-gradient-to-r from-purple-600 to-pink-600 px-4 pt-6 pb-8 lg:rounded-b-3xl">
          <div className="max-w-4xl mx-auto">
            <div className="flex items-center gap-4 mb-6">
              {/* eslint-disable-next-line @next/next/no-img-element */}
              <img
                src="https://readdy.ai/api/search-image?query=young%20person%20profile%20photo%2C%20friendly%20smile%2C%20casual%20style%2C%20professional%20portrait%20photography%2C%20natural%20lighting%2C%20clean%20background&width=200&height=200&seq=profile001&orientation=squarish"
                alt="Profile"
                className="w-20 h-20 rounded-full object-cover border-4 border-white shadow-md"
              />
              <div className="flex-1">
                <div className="flex items-center gap-2 mb-1">
                  <h2 className="text-white text-xl font-bold">Alex Kim</h2>
                  {isVIP && (
                    <span className="bg-yellow-400 text-yellow-900 text-xs px-2 py-0.5 rounded-full font-medium shadow-sm">
                      VIP
                    </span>
                  )}
                </div>
                <p className="text-white/90 text-sm">@alexkim_fanpulse</p>
                <p className="text-white/80 text-xs mt-1">Member since Dec 2023</p>
              </div>
            </div>

            {/* Stats */}
            <div className="grid grid-cols-4 gap-3">
              {stats.map((stat, index) => (
                <div key={index} className="bg-white/10 backdrop-blur-md rounded-xl p-3 text-center border border-white/10">
                  <i className={`${stat.icon} text-white text-xl mb-1`}></i>
                  <p className="text-white font-bold text-sm">{stat.value}</p>
                  <p className="text-white/70 text-xs mt-0.5 font-medium">{stat.label}</p>
                </div>
              ))}
            </div>
          </div>
        </div>

        <div className="max-w-4xl mx-auto px-4 -mt-6">
          {/* VIP Upgrade Card */}
          {!isVIP && (
            <Card className="bg-gradient-to-br from-yellow-400 to-orange-500 text-white border-none shadow-xl mb-8 relative overflow-hidden">
              <div className="relative z-10 p-6">
                <div className="flex items-center gap-4 mb-4">
                  <div className="w-12 h-12 bg-white/20 rounded-full flex items-center justify-center backdrop-blur-sm">
                    <i className="ri-vip-crown-fill text-2xl"></i>
                  </div>
                  <div>
                    <h3 className="font-bold text-lg">Upgrade to VIP</h3>
                    <p className="text-white/90 text-sm">Unlock exclusive benefits & rewards</p>
                  </div>
                </div>
                <Button className="w-full bg-white text-orange-600 hover:bg-orange-50 border-none font-bold">
                  See Plans
                </Button>
              </div>
              <div className="absolute top-0 right-0 w-32 h-32 bg-white/10 rounded-full -mr-10 -mt-10 blur-xl"></div>
            </Card>
          )}

          {/* VIP Benefits */}
          <div className="mb-8" data-testid="membership-benefits-skeleton">
            <h3 className="text-lg font-bold text-gray-900 mb-4 px-1">VIP Benefits</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {vipBenefits.map((benefit, index) => (
                <Card key={index} className="p-4 flex gap-4 items-start hover:shadow-md transition-all border border-gray-100">
                  <div className="w-10 h-10 bg-purple-50 rounded-full flex items-center justify-center flex-shrink-0">
                    <i className={`${benefit.icon} text-xl text-purple-600`}></i>
                  </div>
                  <div>
                    <h4 className="font-bold text-gray-900 text-sm mb-1">{benefit.title}</h4>
                    <p className="text-xs text-gray-500 leading-relaxed">{benefit.description}</p>
                  </div>
                </Card>
              ))}
            </div>
          </div>

          {/* Pricing Plans */}
          <div className="mb-8">
            <h3 className="text-lg font-bold text-gray-900 mb-4 px-1">Choose Your Plan</h3>
            <div className="space-y-4">
              {plans.map(plan => (
                <Card
                  key={plan.id}
                  className={`p-5 relative overflow-hidden transition-all hover:shadow-md ${
                    plan.popular ? 'border-2 border-purple-600 shadow-md' : 'border border-gray-100'
                  }`}
                >
                  {plan.popular && (
                    <div className="absolute top-0 right-0 bg-purple-600 text-white text-xs px-3 py-1 rounded-bl-xl font-medium">
                      Most Popular
                    </div>
                  )}
                  <div className="flex items-center justify-between">
                    <div>
                      <h4 className="font-bold text-gray-900 text-lg">{plan.name}</h4>
                      <p className="text-sm text-gray-500 mt-1">{plan.period}</p>
                      {plan.savings && (
                        <span className="inline-block bg-green-100 text-green-700 text-xs px-2 py-0.5 rounded-full mt-2 font-bold">
                          {plan.savings}
                        </span>
                      )}
                    </div>
                    <div className="text-right">
                      <p className="text-2xl font-bold text-gray-900">${plan.price}</p>
                      <Button 
                        size="sm" 
                        variant={plan.popular ? 'primary' : 'outline'}
                        className="mt-2 px-6"
                      >
                        Subscribe
                      </Button>
                    </div>
                  </div>
                </Card>
              ))}
            </div>
          </div>

          {/* Recent Activity */}
          <div className="mb-8">
            <h3 className="text-lg font-bold text-gray-900 mb-4 px-1">Recent Activity</h3>
            <Card className="divide-y divide-gray-100 border border-gray-100">
              {activities.map((activity) => (
                <div key={activity.id} className="flex items-center gap-4 p-4 hover:bg-gray-50 transition-colors">
                  <div className={`w-10 h-10 rounded-full flex items-center justify-center flex-shrink-0 ${activity.color}`}>
                    <i className={`${activity.icon} text-lg`}></i>
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-bold text-gray-900 truncate">{activity.title}</p>
                    <p className="text-xs text-gray-500 mt-0.5 truncate">{activity.subtitle}</p>
                  </div>
                  <span className="text-xs text-gray-400 whitespace-nowrap">{activity.time}</span>
                </div>
              ))}
            </Card>
          </div>
        </div>
      </PageWrapper>
    </>
  );
}