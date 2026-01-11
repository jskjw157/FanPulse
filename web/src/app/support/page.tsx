"use client";

import PageHeader from "@/components/layout/PageHeader";
import PageWrapper from "@/components/layout/PageWrapper";
import Link from "next/link";
import { useState } from "react";

export default function SupportPage() {
  const [activeTab, setActiveTab] = useState<'faq' | 'inquiry' | 'notice'>('faq');
  const [expandedFaq, setExpandedFaq] = useState<number | null>(null);
  const [showInquiryForm, setShowInquiryForm] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState('all');

  const tabs = [
    { id: 'faq' as const, name: 'FAQ' },
    { id: 'inquiry' as const, name: '1:1 문의' },
    { id: 'notice' as const, name: '공지사항' }
  ];

  const faqCategories = [
    { id: 'all', label: '전체', icon: 'ri-list-check' },
    { id: 'ticket', label: '예매', icon: 'ri-ticket-line' },
    { id: 'payment', label: '결제', icon: 'ri-bank-card-line' },
    { id: 'account', label: '계정', icon: 'ri-user-line' },
    { id: 'point', label: '포인트', icon: 'ri-coin-line' }
  ];

  const faqs = [
    {
      id: 1,
      category: 'ticket',
      question: '티켓 예매는 어떻게 하나요?',
      answer: '콘서트 페이지에서 원하는 공연을 선택한 후, 좌석과 날짜를 선택하여 예매할 수 있습니다. 결제는 카드, 계좌이체, 포인트 사용이 가능합니다.'
    },
    {
      id: 2,
      category: 'ticket',
      question: '예매 취소 및 환불은 어떻게 하나요?',
      answer: '공연 7일 전까지는 100% 환불이 가능하며, 3일 전까지는 50% 환불됩니다. 예매 내역 페이지에서 취소 요청을 할 수 있습니다.'
    },
    {
      id: 3,
      category: 'payment',
      question: '결제 수단은 무엇이 있나요?',
      answer: '신용카드, 체크카드, 계좌이체, 간편결제(카카오페이, 네이버페이), 포인트 결제가 가능합니다.'
    },
    {
      id: 4,
      category: 'payment',
      question: '결제 오류가 발생했어요',
      answer: '결제 오류 시 카드사 승인 상태를 먼저 확인해주세요. 승인이 되었다면 1:1 문의를 통해 주문번호와 함께 문의해주시면 빠르게 도와드리겠습니다.'
    },
    {
      id: 5,
      category: 'account',
      question: '회원가입은 어떻게 하나요?',
      answer: '이메일, 카카오톡, 네이버 계정으로 간편하게 가입할 수 있습니다. 로그인 페이지에서 회원가입 버튼을 눌러주세요.'
    },
    {
      id: 6,
      category: 'account',
      question: '비밀번호를 잊어버렸어요',
      answer: '로그인 페이지에서 "비밀번호 찾기"를 클릭하시면 가입하신 이메일로 재설정 링크를 보내드립니다.'
    },
    {
      id: 7,
      category: 'point',
      question: '포인트는 어떻게 적립하나요?',
      answer: '광고 시청, 투표 참여, 출석 체크, 게시물 작성 등 다양한 활동으로 포인트를 적립할 수 있습니다.'
    },
    {
      id: 8,
      category: 'point',
      question: '포인트 유효기간이 있나요?',
      answer: '포인트는 적립일로부터 1년간 유효합니다. 유효기간이 지나면 자동으로 소멸되니 주의해주세요.'
    }
  ];

  const inquiries = [
    {
      id: 1,
      title: '티켓 좌석 변경 문의',
      category: '예매',
      status: '답변완료',
      date: '2024.12.15',
      statusColor: 'text-green-600 bg-green-50'
    },
    {
      id: 2,
      title: '결제 오류 문의',
      category: '결제',
      status: '처리중',
      date: '2024.12.14',
      statusColor: 'text-blue-600 bg-blue-50'
    },
    {
      id: 3,
      title: '포인트 적립 문의',
      category: '포인트',
      status: '답변완료',
      date: '2024.12.10',
      statusColor: 'text-green-600 bg-green-50'
    }
  ];

  const notices = [
    {
      id: 1,
      title: '2024년 설날 연휴 고객센터 운영 안내',
      date: '2024.12.20',
      isNew: true
    },
    {
      id: 2,
      title: '앱 업데이트 안내 (v2.5.0)',
      date: '2024.12.18',
      isNew: true
    },
    {
      id: 3,
      title: '개인정보 처리방침 변경 안내',
      date: '2024.12.15',
      isNew: false
    },
    {
      id: 4,
      title: '서비스 점검 안내 (12/25)',
      date: '2024.12.10',
      isNew: false
    }
  ];

  const filteredFaqs = selectedCategory === 'all' 
    ? faqs 
    : faqs.filter(faq => faq.category === selectedCategory);

  return (
    <>
      <PageHeader title="고객센터" />
      <PageWrapper>
        {/* Stats Card - 헤더 바로 아래 */}
        <div className="px-4">
          <div className="bg-gradient-to-r from-purple-600 to-pink-600 rounded-2xl p-4 mt-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="text-center">
                <p className="text-white/90 text-xs">평균 응답 시간</p>
                <p className="text-white text-2xl font-bold mt-1">2시간</p>
              </div>
              <div className="text-center">
                <p className="text-white/90 text-xs">문의 해결률</p>
                <p className="text-white text-2xl font-bold mt-1">98%</p>
              </div>
            </div>
          </div>
        </div>

        {/* Tab Menu - 통계 카드 아래 고정 */}
        <div className="sticky top-16 bg-white/95 backdrop-blur-sm z-40 border-b border-gray-200 mt-4">
          <div className="flex px-4">
            {tabs.map(tab => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`flex-1 py-3 text-sm font-medium border-b-2 transition-colors ${
                  activeTab === tab.id
                    ? 'border-purple-600 text-purple-600'
                    : 'border-transparent text-gray-500'
                }`}
              >
                {tab.name}
              </button>
            ))}
          </div>
        </div>

        {/* Main Content - 탭 아래 시작 */}
        <div className="pt-4">
          {/* FAQ Tab */}
          {activeTab === 'faq' && (
            <div className="py-4">
              {/* Category Filter */}
              <div className="flex gap-2 overflow-x-auto pb-3 mb-4 scrollbar-hide px-4">
                {faqCategories.map(category => (
                  <button
                    key={category.id}
                    onClick={() => setSelectedCategory(category.id)}
                    className={`flex items-center gap-2 px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap transition-colors flex-shrink-0 ${
                      selectedCategory === category.id
                        ? 'bg-purple-600 text-white'
                        : 'bg-white text-gray-700 border border-gray-200'
                    }`}
                  >
                    <i className={`${category.icon} text-base`}></i>
                    {category.label}
                  </button>
                ))}
              </div>

              {/* FAQ List */}
              <div className="space-y-3 px-4">
                {filteredFaqs.map(faq => (
                  <div key={faq.id} className="bg-white rounded-2xl overflow-hidden shadow-sm">
                    <button
                      onClick={() => setExpandedFaq(expandedFaq === faq.id ? null : faq.id)}
                      className="w-full px-4 py-4 flex items-start justify-between text-left"
                    >
                      <div className="flex-1 pr-3">
                        <div className="flex items-center gap-2 mb-1">
                          <span className="w-5 h-5 bg-purple-100 rounded-full flex items-center justify-center">
                            <i className="ri-question-line text-xs text-purple-600"></i>
                          </span>
                          <span className="text-xs text-purple-600 font-medium">Q</span>
                        </div>
                        <p className="text-gray-900 font-medium">{faq.question}</p>
                      </div>
                      <i className={`ri-arrow-${expandedFaq === faq.id ? 'up' : 'down'}-s-line text-xl text-gray-400 flex-shrink-0`}></i>
                    </button>
                    {expandedFaq === faq.id && (
                      <div className="px-4 pb-4 border-t border-gray-100">
                        <div className="flex items-start gap-2 pt-3">
                          <span className="w-5 h-5 bg-pink-100 rounded-full flex items-center justify-center flex-shrink-0 mt-0.5">
                            <i className="ri-chat-smile-3-line text-xs text-pink-600"></i>
                          </span>
                          <div className="flex-1">
                            <span className="text-xs text-pink-600 font-medium mb-1 block">A</span>
                            <p className="text-sm text-gray-600 leading-relaxed">{faq.answer}</p>
                          </div>
                        </div>
                      </div>
                    )}
                  </div>
                ))}
              </div>

              {/* Help Button */}
              <div className="mt-6 bg-gradient-to-r from-purple-50 to-pink-50 rounded-2xl p-4 text-center mx-4">
                <p className="text-gray-700 text-sm mb-3">원하는 답변을 찾지 못하셨나요?</p>
                <button
                  onClick={() => {
                    setActiveTab('inquiry');
                    setShowInquiryForm(true);
                  }}
                  className="bg-gradient-to-r from-purple-600 to-pink-600 text-white px-6 py-2.5 rounded-full text-sm font-medium"
                >
                  1:1 문의하기
                </button>
              </div>
            </div>
          )}

          {/* Inquiry Tab */}
          {activeTab === 'inquiry' && (
            <div className="px-4 py-4">
              {!showInquiryForm ? (
                <>
                  {/* New Inquiry Button */}
                  <button
                    onClick={() => setShowInquiryForm(true)}
                    className="w-full bg-gradient-to-r from-purple-600 to-pink-600 text-white py-3.5 rounded-2xl font-medium mb-4 flex items-center justify-center gap-2"
                  >
                    <i className="ri-edit-line text-lg"></i>
                    새 문의 작성하기
                  </button>

                  {/* Inquiry History */}
                  <div>
                    <h3 className="text-sm font-bold text-gray-900 mb-3">문의 내역</h3>
                    <div className="space-y-3">
                      {inquiries.map(inquiry => (
                        <div key={inquiry.id} className="bg-white rounded-2xl p-4 shadow-sm">
                          <div className="flex items-start justify-between mb-2">
                            <h4 className="text-gray-900 font-medium flex-1 pr-2">{inquiry.title}</h4>
                            <span className={`text-xs px-2 py-1 rounded-full font-medium ${inquiry.statusColor}`}>
                              {inquiry.status}
                            </span>
                          </div>
                          <div className="flex items-center justify-between text-xs text-gray-500">
                            <span className="flex items-center gap-1">
                              <i className="ri-folder-line"></i>
                              {inquiry.category}
                            </span>
                            <span>{inquiry.date}</span>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                </>
              ) : (
                /* Inquiry Form */
                <div className="bg-white rounded-2xl p-4 shadow-sm">
                  <div className="flex items-center justify-between mb-4">
                    <h3 className="text-base font-bold text-gray-900">1:1 문의하기</h3>
                    <button
                      onClick={() => setShowInquiryForm(false)}
                      className="w-8 h-8 flex items-center justify-center"
                    >
                      <i className="ri-close-line text-xl text-gray-500"></i>
                    </button>
                  </div>

                  <form className="space-y-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">문의 유형</label>
                      <select className="w-full px-4 py-3 border border-gray-200 rounded-xl text-gray-900 bg-white">
                        <option>예매 관련</option>
                        <option>결제 관련</option>
                        <option>계정 관련</option>
                        <option>포인트 관련</option>
                        <option>기타</option>
                      </select>
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">제목</label>
                      <input
                        type="text"
                        placeholder="문의 제목을 입력해주세요"
                        className="w-full px-4 py-3 border border-gray-200 rounded-xl text-gray-900"
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">문의 내용</label>
                      <textarea
                        placeholder="문의 내용을 상세히 입력해주세요"
                        rows={6}
                        maxLength={500}
                        className="w-full px-4 py-3 border border-gray-200 rounded-xl text-gray-900 resize-none"
                      ></textarea>
                      <p className="text-xs text-gray-500 mt-1 text-right">최대 500자</p>
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">이메일</label>
                      <input
                        type="email"
                        placeholder="답변 받으실 이메일"
                        className="w-full px-4 py-3 border border-gray-200 rounded-xl text-gray-900"
                      />
                    </div>

                    <button
                      type="submit"
                      className="w-full bg-gradient-to-r from-purple-600 to-pink-600 text-white py-3.5 rounded-xl font-medium"
                    >
                      문의 제출하기
                    </button>
                  </form>
                </div>
              )}
            </div>
          )}

          {/* Notice Tab */}
          {activeTab === 'notice' && (
            <div className="px-4 py-4">
              {/* Notice List */}
              <div className="space-y-3">
                {notices.map((notice) => (
                  <Link
                    key={notice.id}
                    href={`/notice-detail?id=${notice.id}`}
                    className="block bg-white rounded-xl p-4 border border-gray-200"
                  >
                    <div className="flex items-start justify-between gap-3">
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-2">
                          <span className="text-xs font-medium text-purple-600 bg-purple-50 px-2 py-0.5 rounded">
                            {/* Dummy category */}
                            공지
                          </span>
                          {notice.isNew && (
                            <span className="text-xs font-bold text-red-500">NEW</span>
                          )}
                        </div>
                        <h3 className="text-sm font-medium text-gray-900 mb-1 line-clamp-1">
                          {notice.title}
                        </h3>
                        <p className="text-xs text-gray-500">{notice.date}</p>
                      </div>
                      <i className="ri-arrow-right-s-line text-gray-400 text-lg flex-shrink-0"></i>
                    </div>
                  </Link>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Quick Contact */}
        <div className="fixed bottom-24 right-4 lg:right-8 lg:bottom-8 z-40">
          <button className="w-14 h-14 bg-gradient-to-r from-purple-600 to-pink-600 text-white rounded-full shadow-lg flex items-center justify-center">
            <i className="ri-customer-service-fill text-2xl"></i>
          </button>
        </div>
      </PageWrapper>
    </>
  );
}
