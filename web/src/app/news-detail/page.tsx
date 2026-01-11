"use client";

import PageHeader from "@/components/layout/PageHeader";
import PageWrapper from "@/components/layout/PageWrapper";
import Link from "next/link";
import { useSearchParams } from "next/navigation";

export default function NewsDetailPage() {
  const searchParams = useSearchParams();
  const newsId = searchParams.get('id') || '1';

  const newsData = {
    '1': {
      title: 'BTS 새 앨범 발매 예정',
      category: '뉴스',
      date: '2024.12.10',
      author: 'FanPulse 편집부',
      image: 'https://readdy.ai/api/search-image?query=BTS%20new%20album%20announcement%2C%20professional%20press%20photo%2C%20modern%20studio%20setting%2C%20album%20cover%20concept%2C%20high%20quality%20photography%2C%20purple%20and%20blue%20theme%2C%20elegant%20composition&width=800&height=500&seq=news001&orientation=landscape',
      content: `방탄소년단(BTS)이 2025년 초 새 앨범 발매를 예고했습니다.

소속사 빅히트 뮤직은 공식 SNS를 통해 "BTS가 새로운 앨범 작업에 박차를 가하고 있다"며 "팬 여러분께 좋은 소식을 전할 수 있도록 최선을 다하고 있다"고 밝혔습니다.

이번 앨범은 멤버들이 직접 프로듀싱에 참여하며, 글로벌 팬들을 위한 특별한 메시지를 담을 예정입니다.

업계 관계자는 "BTS의 새 앨범은 K-POP의 새로운 장을 열 것"이라며 "전 세계 팬들의 기대가 매우 높다"고 전했습니다.

앨범 발매 일정과 수록곡 등 자세한 내용은 추후 공개될 예정입니다.`
    },
    '2': {
      title: 'BLACKPINK 월드투어 추가 공연',
      category: '공연',
      date: '2024.12.09',
      author: 'FanPulse 편집부',
      image: 'https://readdy.ai/api/search-image?query=BLACKPINK%20world%20tour%20concert%20announcement%2C%20glamorous%20stage%20setup%2C%20pink%20and%20black%20theme%2C%20professional%20concert%20photography%2C%20spectacular%20lighting%2C%20global%20tour%20concept&width=800&height=500&seq=news002&orientation=landscape',
      content: `블랙핑크(BLACKPINK)가 월드투어 추가 공연을 확정했습니다.

YG엔터테인먼트는 "팬들의 뜨거운 성원에 힘입어 아시아 및 유럽 지역 추가 공연을 결정했다"고 발표했습니다.

추가 공연은 서울, 도쿄, 방콕, 파리, 런던 등 주요 도시에서 진행되며, 티켓 예매는 다음 주부터 순차적으로 오픈됩니다.

이번 투어에서는 최신 히트곡은 물론 팬들이 사랑하는 명곡들을 선보일 예정입니다.

공연 관계자는 "블랙핑크만의 화려한 퍼포먼스와 특별한 무대를 준비하고 있다"며 "팬 여러분의 많은 관심 부탁드린다"고 말했습니다.`
    }
  };

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const news: any = newsData[newsId as keyof typeof newsData] || newsData['1'];

  const relatedNews = [
    {
      id: 3,
      title: 'SEVENTEEN 새 앨범 차트 1위',
      category: '차트',
      image: 'https://readdy.ai/api/search-image?query=SEVENTEEN%20album%20chart%20success%2C%20professional%20group%20photo%2C%20vibrant%20colors%2C%20celebration%20concept%2C%20high%20quality%20photography%2C%20energetic%20atmosphere&width=300&height=200&seq=related001&orientation=landscape'
    },
    {
      id: 4,
      title: 'NewJeans 글로벌 인기 급상승',
      category: '뉴스',
      image: 'https://readdy.ai/api/search-image?query=NewJeans%20global%20popularity%2C%20fresh%20youthful%20concept%2C%20modern%20photography%2C%20trendy%20aesthetic%2C%20pastel%20colors%2C%20professional%20group%20photo&width=300&height=200&seq=related002&orientation=landscape'
    }
  ];

  return (
    <>
      <PageHeader 
        title="News Detail" 
        rightAction={
          <div className="flex gap-2">
            <button className="w-9 h-9 flex items-center justify-center rounded-full hover:bg-gray-100 transition-colors">
              <i className="ri-share-line text-xl text-gray-700"></i>
            </button>
            <button className="w-9 h-9 flex items-center justify-center rounded-full hover:bg-gray-100 transition-colors">
              <i className="ri-bookmark-line text-xl text-gray-700"></i>
            </button>
          </div>
        }
      />
      <PageWrapper>
        {/* Featured Image */}
        <div className="w-full h-56 md:h-80">
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img 
            src={news.image}
            alt={news.title}
            className="w-full h-full object-cover object-top"
          />
        </div>

        {/* Article Info */}
        <div className="max-w-3xl mx-auto px-4 py-6 bg-white -mt-6 relative rounded-t-3xl shadow-sm">
          <div className="flex items-center gap-2 mb-3">
            <span className="bg-purple-100 text-purple-700 text-xs px-2 py-1 rounded-full font-medium">
              {news.category}
            </span>
            <span className="text-xs text-gray-500">{news.date}</span>
          </div>

          <h1 className="text-xl md:text-2xl font-bold text-gray-900 mb-4">
            {news.title}
          </h1>

          <div className="flex items-center gap-3 mb-6 pb-6 border-b border-gray-100">
            <div className="w-10 h-10 bg-gradient-to-br from-purple-500 to-pink-500 rounded-full flex items-center justify-center">
              <i className="ri-user-line text-white text-lg"></i>
            </div>
            <div>
              <p className="text-sm font-medium text-gray-900">{news.author}</p>
              <p className="text-xs text-gray-500">공식 기자</p>
            </div>
          </div>

          {/* Article Content */}
          <div className="prose prose-sm max-w-none mb-8">
            {news.content.split('\n\n').map((paragraph: string, index: number) => (
              <p key={index} className="text-gray-700 leading-relaxed mb-4 text-base">
                {paragraph}
              </p>
            ))}
          </div>

          {/* Engagement Buttons */}
          <div className="flex items-center gap-3 pt-6 border-t border-gray-100">
            <button className="flex-1 bg-gray-50 hover:bg-gray-100 transition-colors text-gray-700 py-3 rounded-xl font-medium text-sm flex items-center justify-center gap-2">
              <i className="ri-heart-line text-lg"></i>
              좋아요 1.2K
            </button>
            <button className="flex-1 bg-gray-50 hover:bg-gray-100 transition-colors text-gray-700 py-3 rounded-xl font-medium text-sm flex items-center justify-center gap-2">
              <i className="ri-chat-3-line text-lg"></i>
              댓글 234
            </button>
          </div>
        </div>

        {/* Related News */}
        <div className="max-w-3xl mx-auto px-4 py-8 bg-gray-50">
          <h2 className="text-lg font-bold text-gray-900 mb-4">관련 뉴스</h2>
          <div className="space-y-3">
            {relatedNews.map(item => (
              <Link 
                key={item.id} 
                href={`/news-detail?id=${item.id}`}
                className="flex gap-4 bg-white rounded-xl p-3 shadow-sm hover:shadow-md transition-shadow"
              >
                {/* eslint-disable-next-line @next/next/no-img-element */}
                <img 
                  src={item.image}
                  alt={item.title}
                  className="w-24 h-20 rounded-lg object-cover object-top flex-shrink-0"
                />
                <div className="flex-1 min-w-0">
                  <span className="text-xs text-purple-600 font-medium bg-purple-50 px-2 py-0.5 rounded-md">{item.category}</span>
                  <h3 className="text-sm font-medium text-gray-900 mt-1 line-clamp-2 leading-snug">
                    {item.title}
                  </h3>
                </div>
              </Link>
            ))}
          </div>
        </div>
      </PageWrapper>
    </>
  );
}