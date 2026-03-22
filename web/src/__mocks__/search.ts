import type { SearchResult } from '@/types/search';
import type { LiveStatus } from '@/types/live';

export const mockSearchResult: SearchResult = {
  live: [
    {
      id: 1,
      title: 'BTS Fan Meeting Special',
      artistName: 'BTS',
      thumbnailUrl: '/images/mock/live-bts.jpg',
      status: 'LIVE' as LiveStatus,
      viewerCount: 89200,
    },
    {
      id: 10,
      title: 'BTS World Tour Highlights',
      artistName: 'BTS',
      thumbnailUrl: '/images/mock/live-bts-2.jpg',
      status: 'ENDED' as LiveStatus,
      viewerCount: 150000,
    },
  ],
  news: [
    {
      id: 1,
      title: 'BTS 새 앨범 발매 예정',
      summary: 'BTS가 2026년 3월 새 앨범 발매를 예고했다.',
      thumbnailUrl: '/images/mock/news-bts.jpg',
      source: '스포츠조선',
      publishedAt: '2026-02-01T09:00:00Z',
    },
    {
      id: 5,
      title: 'BTS 멤버 진 솔로 앨범 차트 1위',
      summary: 'BTS 진의 솔로 앨범이 빌보드 차트 1위를 기록했다.',
      thumbnailUrl: '/images/mock/news-bts-2.jpg',
      source: '한국경제',
      publishedAt: '2026-01-30T12:00:00Z',
    },
    {
      id: 8,
      title: 'BTS 월드투어 추가 공연 확정',
      summary: 'BTS가 아시아 투어에 서울 추가 공연을 확정했다.',
      thumbnailUrl: '/images/mock/news-bts-3.jpg',
      source: '엔터미디어',
      publishedAt: '2026-01-28T15:30:00Z',
    },
  ],
};

export const mockEmptySearchResult: SearchResult = {
  live: [],
  news: [],
};

export const mockRecentSearches = [
  'BTS',
  'BLACKPINK',
  '콘서트',
  'NewJeans',
  'MAMA 2026',
];
