import { Link, useSearchParams } from 'react-router-dom';

export default function NoticeDetail() {
  const [searchParams] = useSearchParams();
  const noticeId = searchParams.get('id') || '1';

  const notices = [
    {
      id: '1',
      title: '2024년 연말 시스템 점검 안내',
      date: '2024.12.15',
      category: '시스템',
      content: `안녕하세요, K-POP 팬 여러분.

2024년 연말을 맞이하여 더 나은 서비스 제공을 위한 시스템 점검을 실시합니다.

■ 점검 일시
- 2024년 12월 31일 (화) 02:00 ~ 06:00 (약 4시간)

■ 점검 내용
- 서버 안정화 작업
- 데이터베이스 최적화
- 보안 업데이트
- 신규 기능 추가 준비

■ 점검 중 이용 불가 서비스
- 전체 서비스 일시 중단
- 로그인 및 회원가입
- 투표 및 포인트 적립
- 티켓 예매

점검 시간은 작업 진행 상황에 따라 다소 변경될 수 있습니다.
이용에 불편을 드려 죄송하며, 더 나은 서비스로 보답하겠습니다.

감사합니다.`
    },
    {
      id: '2',
      title: '개인정보 처리방침 개정 안내',
      date: '2024.12.10',
      category: '정책',
      content: `회원 여러분께 알려드립니다.

개인정보 보호법 개정에 따라 개인정보 처리방침이 일부 변경됩니다.

■ 시행일
- 2024년 12월 20일 (금)

■ 주요 변경 내용
1. 개인정보 수집 항목 명확화
2. 개인정보 보유 기간 조정
3. 제3자 제공 내역 업데이트
4. 회원 권리 보장 강화

■ 확인 방법
- 앱 내 설정 &gt; 개인정보 처리방침에서 확인 가능

변경된 개인정보 처리방침은 시행일부터 적용되며, 회원님의 소중한 개인정보 보호를 위해 최선을 다하겠습니다.

감사합니다.`
    },
    {
      id: '3',
      title: '신규 멤버십 혜택 추가',
      date: '2024.12.05',
      category: '서비스',
      content: `VIP 멤버십 회원님들께 특별한 소식을 전해드립니다.

2024년 12월부터 신규 멤버십 혜택이 추가됩니다!

■ 추가 혜택
1. 월간 무료 포인트 2배 증정
   - 기존: 1,000P → 변경: 2,000P

2. 콘서트 티켓 우선 예매권
   - 일반 예매 시작 24시간 전 예매 가능

3. 아티스트 영상통화 이벤트 우선 참여
   - VIP 회원 전용 추첨 이벤트

4. 한정판 굿즈 구매 기회
   - 월 1회 VIP 전용 굿즈 판매

■ 적용 대상
- 현재 VIP 멤버십 이용 중인 모든 회원
- 신규 가입 회원

더 많은 혜택으로 찾아뵙겠습니다.
감사합니다.`
    }
  ];

  const currentNotice = notices.find(n => n.id === noticeId) || notices[0];
  const currentIndex = notices.findIndex(n => n.id === noticeId);
  const prevNotice = currentIndex > 0 ? notices[currentIndex - 1] : null;
  const nextNotice = currentIndex < notices.length - 1 ? notices[currentIndex + 1] : null;

  return (
    <div className="min-h-screen bg-white pb-20">
      {/* Header */}
      <header className="fixed top-0 left-0 right-0 bg-white border-b border-gray-200 z-50">
        <div className="px-4 py-3 flex items-center justify-between">
          <Link to="/support" className="w-9 h-9 flex items-center justify-center">
            <i className="ri-arrow-left-line text-xl text-gray-900"></i>
          </Link>
          <h1 className="text-base font-bold text-gray-900">공지사항</h1>
          <div className="w-9"></div>
        </div>
      </header>

      {/* Content */}
      <div className="pt-14 px-4 py-6">
        {/* Category Badge */}
        <div className="mb-3">
          <span className="inline-block bg-purple-100 text-purple-700 text-xs font-medium px-3 py-1 rounded-full">
            {currentNotice.category}
          </span>
        </div>

        {/* Title */}
        <h1 className="text-xl font-bold text-gray-900 mb-3 leading-tight">
          {currentNotice.title}
        </h1>

        {/* Date */}
        <div className="flex items-center gap-2 text-sm text-gray-500 mb-6 pb-6 border-b border-gray-200">
          <i className="ri-calendar-line"></i>
          <span>{currentNotice.date}</span>
        </div>

        {/* Content */}
        <div className="prose prose-sm max-w-none">
          <div className="text-sm text-gray-700 leading-relaxed whitespace-pre-line">
            {currentNotice.content}
          </div>
        </div>

        {/* Navigation */}
        <div className="mt-8 pt-6 border-t border-gray-200 space-y-3">
          {prevNotice && (
            <Link
              to={`/notice-detail?id=${prevNotice.id}`}
              className="block p-4 bg-gray-50 rounded-xl"
            >
              <div className="flex items-center justify-between">
                <div className="flex-1">
                  <p className="text-xs text-gray-500 mb-1">이전 글</p>
                  <p className="text-sm font-medium text-gray-900 line-clamp-1">
                    {prevNotice.title}
                  </p>
                </div>
                <i className="ri-arrow-up-line text-gray-400 ml-2"></i>
              </div>
            </Link>
          )}

          {nextNotice && (
            <Link
              to={`/notice-detail?id=${nextNotice.id}`}
              className="block p-4 bg-gray-50 rounded-xl"
            >
              <div className="flex items-center justify-between">
                <div className="flex-1">
                  <p className="text-xs text-gray-500 mb-1">다음 글</p>
                  <p className="text-sm font-medium text-gray-900 line-clamp-1">
                    {nextNotice.title}
                  </p>
                </div>
                <i className="ri-arrow-down-line text-gray-400 ml-2"></i>
              </div>
            </Link>
          )}
        </div>

        {/* Back to List Button */}
        <div className="mt-6">
          <Link
            to="/support"
            className="block w-full bg-gray-100 text-gray-700 text-center py-3 rounded-full font-medium text-sm"
          >
            목록으로
          </Link>
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
