"use client";

import PageHeader from "@/components/layout/PageHeader";
import PageWrapper from "@/components/layout/PageWrapper";
import Link from "next/link";
import { useState } from "react";

export default function ConcertDetailPage() {
  const [selectedDate, setSelectedDate] = useState('2024-12-20');
  const [selectedTicket, setSelectedTicket] = useState('');

  const concert = {
    title: 'BTS World Tour Seoul',
    artist: 'BTS',
    image: 'https://readdy.ai/api/search-image?query=BTS%20world%20tour%20concert%20poster%2C%20spectacular%20stage%20design%2C%20purple%20and%20blue%20lighting%2C%20professional%20concert%20photography%2C%20massive%20LED%20screens%2C%20dynamic%20atmosphere%2C%20high%20quality&width=800&height=600&seq=concert002&orientation=landscape',
    venue: '잠실 올림픽 주경기장',
    address: '서울특별시 송파구 올림픽로 25',
    dates: [
      { date: '2024-12-20', time: '19:00', status: 'available' },
      { date: '2024-12-21', time: '19:00', status: 'available' },
      { date: '2024-12-22', time: '18:00', status: 'soldout' }
    ],
    tickets: [
      { id: 'vip', name: 'VIP석', price: '220,000원', status: 'available' },
      { id: 'r', name: 'R석', price: '165,000원', status: 'available' },
      { id: 's', name: 'S석', price: '132,000원', status: 'available' },
      { id: 'a', name: 'A석', price: '99,000원', status: 'soldout' }
    ],
    description: 'BTS의 월드투어가 서울에서 개최됩니다. 최고의 무대와 퍼포먼스를 경험하세요!',
    notice: [
      '본 공연은 전석 지정석입니다',
      '7세 이상 입장 가능합니다',
      '공연 당일 신분증을 지참해주세요',
      '티켓 예매 후 취소/환불은 공연 7일 전까지 가능합니다'
    ]
  };

  return (
    <>
      <PageHeader 
        title="공연 상세" 
        rightAction={
          <button className="w-9 h-9 flex items-center justify-center rounded-full hover:bg-gray-100">
            <i className="ri-share-line text-xl text-gray-700"></i>
          </button>
        }
      />
      <PageWrapper className="pb-24">
        {/* Poster */}
        <div className="h-80">
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img 
            src={concert.image}
            alt={concert.title}
            className="w-full h-full object-cover object-top"
          />
        </div>

        {/* Concert Info */}
        <div className="px-4 py-5">
          <h1 className="text-2xl font-bold text-gray-900 mb-2">{concert.title}</h1>
          <p className="text-base text-purple-600 font-medium mb-4">{concert.artist}</p>

          <div className="space-y-3 text-sm">
            <div className="flex items-start gap-3">
              <i className="ri-map-pin-line text-purple-600 text-lg flex-shrink-0 mt-0.5"></i>
              <div>
                <p className="font-medium text-gray-900">{concert.venue}</p>
                <p className="text-gray-600 text-xs mt-0.5">{concert.address}</p>
              </div>
            </div>
          </div>
        </div>

        {/* Date Selection */}
        <div className="px-4 py-4 bg-gray-50">
          <h2 className="text-base font-bold text-gray-900 mb-3">공연 일정</h2>
          <div className="space-y-2">
            {concert.dates.map(item => (
              <button
                key={item.date}
                onClick={() => item.status === 'available' && setSelectedDate(item.date)}
                disabled={item.status === 'soldout'}
                className={`w-full p-4 rounded-xl text-left transition-all ${
                  selectedDate === item.date
                    ? 'bg-gradient-to-r from-purple-600 to-pink-600 text-white'
                    : item.status === 'soldout'
                    ? 'bg-gray-200 text-gray-400'
                    : 'bg-white text-gray-900'
                }`}
              >
                <div className="flex items-center justify-between">
                  <div>
                    <p className="font-bold">{item.date}</p>
                    <p className="text-sm mt-1">{item.time} 시작</p>
                  </div>
                  {item.status === 'soldout' && (
                    <span className="text-xs font-medium">매진</span>
                  )}
                </div>
              </button>
            ))}
          </div>
        </div>

        {/* Ticket Selection */}
        <div className="px-4 py-4">
          <h2 className="text-base font-bold text-gray-900 mb-3">좌석 선택</h2>
          <div className="grid grid-cols-2 gap-3">
            {concert.tickets.map(ticket => (
              <button
                key={ticket.id}
                onClick={() => ticket.status === 'available' && setSelectedTicket(ticket.id)}
                disabled={ticket.status === 'soldout'}
                className={`p-4 rounded-xl text-left transition-all ${
                  selectedTicket === ticket.id
                    ? 'bg-purple-100 border-2 border-purple-600'
                    : ticket.status === 'soldout'
                    ? 'bg-gray-100 border-2 border-gray-200'
                    : 'bg-white border-2 border-gray-200'
                }`}
              >
                <p className={`font-bold mb-1 ${
                  ticket.status === 'soldout' ? 'text-gray-400' : 'text-gray-900'
                }`}>
                  {ticket.name}
                </p>
                <p className={`text-sm ${
                  selectedTicket === ticket.id ? 'text-purple-600' : 
                  ticket.status === 'soldout' ? 'text-gray-400' : 'text-gray-600'
                }`}>
                  {ticket.status === 'soldout' ? '매진' : ticket.price}
                </p>
              </button>
            ))}
          </div>
        </div>

        {/* Description */}
        <div className="px-4 py-4 bg-gray-50">
          <h2 className="text-base font-bold text-gray-900 mb-3">공연 소개</h2>
          <p className="text-sm text-gray-700 leading-relaxed">
            {concert.description}
          </p>
        </div>

        {/* Notice */}
        <div className="px-4 py-4 mb-20">
          <h2 className="text-base font-bold text-gray-900 mb-3">유의사항</h2>
          <div className="space-y-2">
            {concert.notice.map((item, index) => (
              <div key={index} className="flex items-start gap-2">
                <i className="ri-checkbox-circle-line text-purple-600 flex-shrink-0 mt-0.5"></i>
                <p className="text-sm text-gray-700">{item}</p>
              </div>
            ))}
          </div>
        </div>

              {/* Bottom Action */}
              <div className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 px-4 py-4 z-40">
                <div className="max-w-4xl mx-auto">
                  <button
                    disabled={!selectedDate || !selectedTicket}              className={`w-full py-4 rounded-full font-bold text-base ${
                selectedDate && selectedTicket
                  ? 'bg-gradient-to-r from-purple-600 to-pink-600 text-white'
                  : 'bg-gray-200 text-gray-400'
              }`}
            >
              티켓 예매하기
            </button>
          </div>
        </div>
      </PageWrapper>
    </>
  );
}
