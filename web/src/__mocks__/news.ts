import type { News } from '@/types/news';

export const mockLatestNews: News[] = [
  {
    id: 1,
    title: 'BTS 새 앨범 발매 예정',
    summary: 'BTS가 2026년 3월 새 앨범 발매를 예고했다. 멤버들의 솔로 활동 이후 첫 완전체 앨범으로 팬들의 기대를 모으고 있다.',
    thumbnailUrl: '/images/mock/news-1.jpg',
    source: '스포츠조선',
    publishedAt: '2026-02-01T09:00:00Z',
  },
  {
    id: 2,
    title: 'BLACKPINK 월드투어 추가 공연',
    summary: 'BLACKPINK가 아시아 투어에 서울 추가 공연을 확정했다.',
    thumbnailUrl: '/images/mock/news-2.jpg',
    source: '엔터미디어',
    publishedAt: '2026-01-31T15:30:00Z',
  },
  {
    id: 3,
    title: 'NewJeans 신곡 음원차트 1위',
    summary: 'NewJeans의 신곡이 발매 즉시 주요 음원차트 1위를 석권했다.',
    thumbnailUrl: '/images/mock/news-3.jpg',
    source: '한국경제',
    publishedAt: '2026-01-30T12:00:00Z',
  },
];
