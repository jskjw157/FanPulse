import type { Live } from '@/types/live';

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
