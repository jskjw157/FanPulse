import { render, screen } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import ConcertDetailPage from './page';
import { fetchConcert } from '@/lib/api/concert';

const nav = vi.hoisted(() => ({ id: '11111111-1111-1111-1111-111111111111' }));
vi.mock('next/navigation', () => ({
  useSearchParams: () => ({ get: (key: string) => key === 'id' ? nav.id : null }),
  useRouter: () => ({ back: vi.fn() }),
}));
vi.mock('@/lib/api/concert', () => ({ fetchConcert: vi.fn() }));

const concert = {
  id: nav.id, externalId: 'PF298563', title: 'WHIB FAN CONCERT: BLUE HOUR',
  artist: '김준민, 이하준', venue: 'NOL 씨어터 합정 · 동양생명홀',
  startDate: '2026-09-12', endDate: '2026-09-12', status: '공연예정' as const,
  posterUrl: 'https://kopis.or.kr/upload/PF298563.gif', performanceTime: '토요일(14:00,18:00)',
  priceText: '전석 110,000원', performers: '김준민, 이하준', runtime: '100분',
  ageRating: '만 7세 이상', venueAddress: '서울특별시 마포구 양화로 45',
  ticketUrl: 'https://kopis.or.kr/por/db/pblprfr/pblprfrView.do?menuId=MNU_00020&mt20Id=PF298563',
};

describe('ConcertDetailPage', () => {
  beforeEach(() => { nav.id = concert.id; vi.mocked(fetchConcert).mockReset(); });

  it('renders actual detail and links only to the official source page', async () => {
    vi.mocked(fetchConcert).mockResolvedValue(concert);
    render(<ConcertDetailPage />);
    expect(await screen.findByText(concert.title)).toBeInTheDocument();
    expect(screen.getByText(concert.priceText)).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'KOPIS 공식 정보 확인' }))
      .toHaveAttribute('href', concert.ticketUrl);
    expect(screen.queryByText('VIP석')).not.toBeInTheDocument();
  });

  it('never renders detail state that belongs to a previous UUID', async () => {
    const secondId = '22222222-2222-2222-2222-222222222222';
    vi.mocked(fetchConcert)
      .mockResolvedValueOnce(concert)
      .mockImplementationOnce(() => new Promise<never>(() => undefined));
    const { rerender, unmount } = render(<ConcertDetailPage />);
    expect(await screen.findByText(concert.title)).toBeInTheDocument();

    const microtask = vi.spyOn(globalThis, 'queueMicrotask').mockImplementation(() => undefined);
    try {
      nav.id = secondId;
      rerender(<ConcertDetailPage />);

      expect(screen.queryByText(concert.title)).not.toBeInTheDocument();
      expect(document.querySelector(`a[href="${concert.ticketUrl}"]`)).not.toBeInTheDocument();
    } finally {
      microtask.mockRestore();
      unmount();
    }
  });

  it('formats KOPIS date-only values in the Seoul timezone', async () => {
    const originalTimezone = process.env.TZ;
    process.env.TZ = 'UTC';
    try {
      vi.mocked(fetchConcert).mockResolvedValue(concert);
      render(<ConcertDetailPage />);

      expect(await screen.findByText('2026년 9월 12일')).toBeInTheDocument();
    } finally {
      if (originalTimezone === undefined) delete process.env.TZ;
      else process.env.TZ = originalTimezone;
    }
  });

  it('keeps nullable detail fields neutral', async () => {
    vi.mocked(fetchConcert).mockResolvedValue({
      ...concert, artist: null, posterUrl: null, performanceTime: null, priceText: null,
      performers: null, runtime: null, ageRating: null, venueAddress: null,
    });
    render(<ConcertDetailPage />);
    expect(await screen.findByText(concert.title)).toBeInTheDocument();
    expect(screen.getByText('가격 정보 없음')).toBeInTheDocument();
    expect(screen.queryByAltText(concert.title)).not.toBeInTheDocument();
  });

  it('renders an error instead of the static detail', async () => {
    vi.mocked(fetchConcert).mockRejectedValue(new Error('missing'));
    render(<ConcertDetailPage />);
    expect(await screen.findByText('공연 상세 정보를 불러오지 못했습니다.')).toBeInTheDocument();
    expect(screen.queryByText('BTS World Tour Seoul')).not.toBeInTheDocument();
  });
});
