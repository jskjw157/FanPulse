import type { Live, LiveDetail } from '@/types/live';

export const mockLiveNow: Live[] = [
  {
    id: 1,
    title: 'NewJeans 컴백 쇼케이스',
    artistName: 'NewJeans Official',
    thumbnailUrl: '/images/mock/live-1.jpg',
    status: 'LIVE',
    viewerCount: 24583,
  },
  {
    id: 2,
    title: 'BTS Fan Meeting Special',
    artistName: 'BTS',
    thumbnailUrl: '/images/mock/live-2.jpg',
    status: 'LIVE',
    viewerCount: 89200,
  },
  {
    id: 3,
    title: 'BLACKPINK Behind The Scenes',
    artistName: 'BLACKPINK',
    thumbnailUrl: '/images/mock/live-3.jpg',
    status: 'LIVE',
    viewerCount: 67800,
  },
];

export const mockUpcoming: Live[] = [
  {
    id: 10,
    title: 'SEVENTEEN Dance Practice',
    artistName: 'SEVENTEEN',
    thumbnailUrl: '/images/mock/upcoming-1.jpg',
    status: 'SCHEDULED',
    scheduledAt: '2026-02-15T14:00:00Z',
  },
  {
    id: 11,
    title: 'Stray Kids World Tour Highlights',
    artistName: 'Stray Kids',
    thumbnailUrl: '/images/mock/upcoming-2.jpg',
    status: 'SCHEDULED',
    scheduledAt: '2026-02-16T10:00:00Z',
  },
];

export const mockLiveList: Live[] = [
  ...mockLiveNow,
  ...mockUpcoming,
  {
    id: 20,
    title: 'TWICE Concert Replay',
    artistName: 'TWICE',
    thumbnailUrl: '/images/mock/ended-1.jpg',
    status: 'ENDED',
  },
];

export const mockLiveDetail: LiveDetail = {
  id: 1,
  title: 'NewJeans 컴백 쇼케이스',
  artistName: 'NewJeans Official',
  thumbnailUrl: '/images/mock/live-1.jpg',
  status: 'LIVE',
  description: 'NewJeans의 새 앨범 "How Sweet" 컴백 쇼케이스 라이브 방송입니다. 새로운 타이틀곡 무대와 멤버들의 이야기를 만나보세요.',
  streamUrl: 'https://www.youtube.com/embed/dQw4w9WgXcQ?rel=0&modestbranding=1&playsinline=1',
  scheduledAt: '2026-02-01T14:00:00Z',
  startedAt: '2026-02-01T14:00:00Z',
  viewerCount: 24583,
};
