import { useState } from 'react';
import { Link } from 'react-router-dom';

interface Ticket {
  id: number;
  concertName: string;
  artist: string;
  venue: string;
  date: string;
  time: string;
  seat: string;
  price: number;
  status: 'confirmed' | 'cancelled' | 'refunded';
  bookingDate: string;
  bookingNumber: string;
  poster: string;
  qrCode: string;
}

export default function TicketsPage() {
  const [selectedTab, setSelectedTab] = useState<'all' | 'confirmed' | 'cancelled'>('all');
  const [selectedTicket, setSelectedTicket] = useState<Ticket | null>(null);

  const tickets: Ticket[] = [
    {
      id: 1,
      concertName: 'BTS World Tour 2024',
      artist: 'BTS',
      venue: '잠실 올림픽 주경기장',
      date: '2024.12.25',
      time: '19:00',
      seat: 'A구역 12열 15번',
      price: 150000,
      status: 'confirmed',
      bookingDate: '2024.11.15',
      bookingNumber: 'TK20241115001',
      poster: 'https://readdy.ai/api/search-image?query=BTS%20concert%20poster%20design%2C%20vibrant%20purple%20and%20pink%20gradient%20background%2C%20modern%20K-pop%20aesthetic%2C%20dynamic%20stage%20lighting%20effects%2C%20professional%20concert%20photography%2C%20high%20energy%20performance%20atmosphere&width=400&height=500&seq=ticket001&orientation=portrait',
      qrCode: 'https://readdy.ai/api/search-image?query=QR%20code%20sample%2C%20black%20and%20white%2C%20square%20format%2C%20clean%20design%2C%20scannable%20barcode%2C%20centered%20composition%2C%20isolated%20on%20white%20background&width=300&height=300&seq=qr001&orientation=squarish'
    },
    {
      id: 2,
      concertName: 'BLACKPINK Born Pink Tour',
      artist: 'BLACKPINK',
      venue: '고척 스카이돔',
      date: '2024.12.31',
      time: '18:00',
      seat: 'VIP석 5열 8번',
      price: 200000,
      status: 'confirmed',
      bookingDate: '2024.11.20',
      bookingNumber: 'TK20241120002',
      poster: 'https://readdy.ai/api/search-image?query=BLACKPINK%20concert%20poster%2C%20bold%20pink%20and%20black%20color%20scheme%2C%20fierce%20and%20stylish%20K-pop%20girl%20group%20aesthetic%2C%20glamorous%20stage%20setup%2C%20professional%20concert%20photography%2C%20powerful%20performance%20energy&width=400&height=500&seq=ticket002&orientation=portrait',
      qrCode: 'https://readdy.ai/api/search-image?query=QR%20code%20sample%2C%20black%20and%20white%2C%20square%20format%2C%20clean%20design%2C%20scannable%20barcode%2C%20centered%20composition%2C%20isolated%20on%20white%20background&width=300&height=300&seq=qr002&orientation=squarish'
    },
    {
      id: 3,
      concertName: 'Seventeen Be The Sun',
      artist: 'Seventeen',
      venue: 'KSPO DOME',
      date: '2024.11.10',
      time: '19:00',
      seat: 'B구역 8열 20번',
      price: 120000,
      status: 'cancelled',
      bookingDate: '2024.10.05',
      bookingNumber: 'TK20241005003',
      poster: 'https://readdy.ai/api/search-image?query=Seventeen%20concert%20poster%2C%20bright%20orange%20and%20blue%20gradient%2C%20energetic%20K-pop%20boy%20group%20concept%2C%20synchronized%20choreography%2C%20professional%20concert%20photography%2C%20youthful%20and%20vibrant%20atmosphere&width=400&height=500&seq=ticket003&orientation=portrait',
      qrCode: 'https://readdy.ai/api/search-image?query=QR%20code%20sample%2C%20black%20and%20white%2C%20square%20format%2C%20clean%20design%2C%20scannable%20barcode%2C%20centered%20composition%2C%20isolated%20on%20white%20background&width=300&height=300&seq=qr003&orientation=squarish'
    },
    {
      id: 4,
      concertName: 'IU The Golden Hour',
      artist: 'IU',
      venue: '올림픽공원 체조경기장',
      date: '2024.10.20',
      time: '18:30',
      seat: 'R석 15열 12번',
      price: 99000,
      status: 'refunded',
      bookingDate: '2024.09.10',
      bookingNumber: 'TK20240910004',
      poster: 'https://readdy.ai/api/search-image?query=IU%20concert%20poster%2C%20soft%20golden%20and%20pastel%20purple%20gradient%2C%20elegant%20and%20dreamy%20K-pop%20solo%20artist%20aesthetic%2C%20intimate%20stage%20lighting%2C%20professional%20concert%20photography%2C%20warm%20and%20emotional%20atmosphere&width=400&height=500&seq=ticket004&orientation=portrait',
      qrCode: 'https://readdy.ai/api/search-image?query=QR%20code%20sample%2C%20black%20and%20white%2C%20square%20format%2C%20clean%20design%2C%20scannable%20barcode%2C%20centered%20composition%2C%20isolated%20on%20white%20background&width=300&height=300&seq=qr004&orientation=squarish'
    }
  ];

  const filteredTickets = tickets.filter(ticket => {
    if (selectedTab === 'all') return true;
    if (selectedTab === 'confirmed') return ticket.status === 'confirmed';
    if (selectedTab === 'cancelled') return ticket.status === 'cancelled' || ticket.status === 'refunded';
    return true;
  });

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'confirmed':
        return <span className="bg-green-100 text-green-700 text-xs px-2 py-1 rounded-full font-medium">예매완료</span>;
      case 'cancelled':
        return <span className="bg-gray-100 text-gray-700 text-xs px-2 py-1 rounded-full font-medium">취소</span>;
      case 'refunded':
        return <span className="bg-blue-100 text-blue-700 text-xs px-2 py-1 rounded-full font-medium">환불완료</span>;
      default:
        return null;
    }
  };

  const handleCancelRequest = (ticket: Ticket) => {
    if (window.confirm(`${ticket.concertName} 예매를 취소하시겠습니까?`)) {
      alert('취소 요청이 접수되었습니다. 영업일 기준 3-5일 내 환불 처리됩니다.');
    }
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
            <h1 className="text-base font-bold text-gray-900">예매 내역</h1>
          </div>
          <Link to="/search" className="w-9 h-9 flex items-center justify-center">
            <i className="ri-search-line text-xl text-gray-700"></i>
          </Link>
        </div>
      </header>

      {/* Stats Card */}
      <div className="pt-16 px-4 py-4">
        <div className="bg-gradient-to-r from-purple-600 to-pink-600 rounded-2xl p-4 text-white">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-white/90 text-sm mb-1">총 예매 내역</p>
              <p className="text-3xl font-bold">{tickets.length}건</p>
            </div>
            <div className="text-right">
              <p className="text-white/90 text-sm mb-1">예정된 공연</p>
              <p className="text-2xl font-bold">
                {tickets.filter(t => t.status === 'confirmed').length}건
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Tab Filter */}
      <div className="px-4 mb-4">
        <div className="bg-white rounded-full p-1 inline-flex gap-1">
          <button
            onClick={() => setSelectedTab('all')}
            className={`px-4 py-2 rounded-full text-sm font-medium transition-all ${
              selectedTab === 'all'
                ? 'bg-purple-600 text-white'
                : 'text-gray-600'
            }`}
          >
            전체
          </button>
          <button
            onClick={() => setSelectedTab('confirmed')}
            className={`px-4 py-2 rounded-full text-sm font-medium transition-all ${
              selectedTab === 'confirmed'
                ? 'bg-purple-600 text-white'
                : 'text-gray-600'
            }`}
          >
            예매완료
          </button>
          <button
            onClick={() => setSelectedTab('cancelled')}
            className={`px-4 py-2 rounded-full text-sm font-medium transition-all ${
              selectedTab === 'cancelled'
                ? 'bg-purple-600 text-white'
                : 'text-gray-600'
            }`}
          >
            취소/환불
          </button>
        </div>
      </div>

      {/* Tickets List */}
      <div className="px-4 space-y-3">
        {filteredTickets.length === 0 ? (
          <div className="bg-white rounded-2xl p-8 text-center">
            <div className="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <i className="ri-ticket-line text-4xl text-gray-400"></i>
            </div>
            <p className="text-gray-900 font-medium mb-2">예매 내역이 없습니다</p>
            <p className="text-gray-500 text-sm mb-4">콘서트를 예매하고 공연을 즐겨보세요!</p>
            <Link
              to="/concert"
              className="inline-block bg-purple-600 text-white px-6 py-2 rounded-full text-sm font-medium"
            >
              공연 둘러보기
            </Link>
          </div>
        ) : (
          filteredTickets.map(ticket => (
            <div
              key={ticket.id}
              className="bg-white rounded-2xl overflow-hidden shadow-sm"
            >
              <div
                onClick={() => setSelectedTicket(ticket)}
                className="flex gap-3 p-4 cursor-pointer"
              >
                <img
                  src={ticket.poster}
                  alt={ticket.concertName}
                  className="w-24 h-32 object-cover rounded-xl"
                />
                <div className="flex-1">
                  <div className="flex items-start justify-between mb-2">
                    <div className="flex-1">
                      <h3 className="font-bold text-gray-900 mb-1">{ticket.concertName}</h3>
                      <p className="text-sm text-gray-600 mb-2">{ticket.artist}</p>
                    </div>
                    {getStatusBadge(ticket.status)}
                  </div>
                  <div className="space-y-1">
                    <div className="flex items-center gap-2 text-sm text-gray-600">
                      <i className="ri-map-pin-line text-base"></i>
                      <span>{ticket.venue}</span>
                    </div>
                    <div className="flex items-center gap-2 text-sm text-gray-600">
                      <i className="ri-calendar-line text-base"></i>
                      <span>{ticket.date} {ticket.time}</span>
                    </div>
                    <div className="flex items-center gap-2 text-sm text-gray-600">
                      <i className="ri-armchair-line text-base"></i>
                      <span>{ticket.seat}</span>
                    </div>
                  </div>
                </div>
              </div>
              <div className="border-t border-gray-100 px-4 py-3 flex items-center justify-between">
                <div>
                  <p className="text-xs text-gray-500 mb-1">결제 금액</p>
                  <p className="text-lg font-bold text-purple-600">{ticket.price.toLocaleString()}원</p>
                </div>
                {ticket.status === 'confirmed' && (
                  <button
                    onClick={() => handleCancelRequest(ticket)}
                    className="bg-gray-100 text-gray-700 px-4 py-2 rounded-full text-sm font-medium"
                  >
                    취소 요청
                  </button>
                )}
              </div>
            </div>
          ))
        )}
      </div>

      {/* Ticket Detail Modal */}
      {selectedTicket && (
        <div
          className="fixed inset-0 bg-black/50 z-50 flex items-end"
          onClick={() => setSelectedTicket(null)}
        >
          <div
            className="bg-white rounded-t-3xl w-full max-h-[85vh] overflow-y-auto"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="sticky top-0 bg-white border-b border-gray-200 px-4 py-4 flex items-center justify-between z-10">
              <h2 className="text-lg font-bold text-gray-900">예매 상세</h2>
              <button
                onClick={() => setSelectedTicket(null)}
                className="w-9 h-9 flex items-center justify-center"
              >
                <i className="ri-close-line text-2xl text-gray-700"></i>
              </button>
            </div>

            <div className="p-4 pb-6">
              {/* Poster */}
              <img
                src={selectedTicket.poster}
                alt={selectedTicket.concertName}
                className="w-full h-48 object-cover rounded-2xl mb-4"
              />

              {/* Concert Info */}
              <div className="bg-gray-50 rounded-2xl p-4 mb-3">
                <div className="flex items-start justify-between mb-3">
                  <div>
                    <h3 className="text-lg font-bold text-gray-900 mb-1">
                      {selectedTicket.concertName}
                    </h3>
                    <p className="text-sm text-gray-600">{selectedTicket.artist}</p>
                  </div>
                  {getStatusBadge(selectedTicket.status)}
                </div>
                <div className="space-y-2">
                  <div className="flex items-center gap-3">
                    <i className="ri-map-pin-line text-lg text-purple-600"></i>
                    <span className="text-sm text-gray-700">{selectedTicket.venue}</span>
                  </div>
                  <div className="flex items-center gap-3">
                    <i className="ri-calendar-line text-lg text-purple-600"></i>
                    <span className="text-sm text-gray-700">{selectedTicket.date} {selectedTicket.time}</span>
                  </div>
                  <div className="flex items-center gap-3">
                    <i className="ri-armchair-line text-lg text-purple-600"></i>
                    <span className="text-sm text-gray-700">{selectedTicket.seat}</span>
                  </div>
                </div>
              </div>

              {/* QR Code */}
              {selectedTicket.status === 'confirmed' && (
                <div className="bg-white border-2 border-purple-200 rounded-2xl p-4 mb-3 text-center">
                  <p className="text-sm text-gray-600 mb-3">입장 QR 코드</p>
                  <img
                    src={selectedTicket.qrCode}
                    alt="QR Code"
                    className="w-40 h-40 mx-auto mb-2"
                  />
                  <p className="text-xs text-gray-500">
                    공연 당일 입장 시 제시해주세요
                  </p>
                </div>
              )}

              {/* Booking Info */}
              <div className="bg-gray-50 rounded-2xl p-4 mb-3">
                <h4 className="font-bold text-gray-900 mb-3">예매 정보</h4>
                <div className="space-y-2">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">예매번호</span>
                    <span className="text-gray-900 font-medium">{selectedTicket.bookingNumber}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">예매일</span>
                    <span className="text-gray-900 font-medium">{selectedTicket.bookingDate}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">좌석</span>
                    <span className="text-gray-900 font-medium">{selectedTicket.seat}</span>
                  </div>
                  <div className="border-t border-gray-200 pt-2 mt-2 flex justify-between">
                    <span className="text-gray-900 font-bold">결제 금액</span>
                    <span className="text-purple-600 font-bold text-lg">
                      {selectedTicket.price.toLocaleString()}원
                    </span>
                  </div>
                </div>
              </div>

              {/* Action Buttons */}
              {selectedTicket.status === 'confirmed' && (
                <div className="space-y-2 mb-3">
                  <button
                    onClick={() => {
                      alert('티켓이 다운로드되었습니다.');
                    }}
                    className="w-full bg-purple-600 text-white py-3 rounded-full font-medium"
                  >
                    티켓 다운로드
                  </button>
                  <button
                    onClick={() => handleCancelRequest(selectedTicket)}
                    className="w-full bg-gray-100 text-gray-700 py-3 rounded-full font-medium"
                  >
                    예매 취소
                  </button>
                </div>
              )}

              {/* Notice */}
              <div className="bg-yellow-50 border border-yellow-200 rounded-xl p-3">
                <p className="text-xs text-yellow-800 leading-relaxed">
                  <i className="ri-information-line mr-1"></i>
                  공연 시작 24시간 전까지 취소 가능하며, 취소 수수료가 부과될 수 있습니다.
                </p>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Bottom Navigation */}
      <nav className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 z-40">
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
