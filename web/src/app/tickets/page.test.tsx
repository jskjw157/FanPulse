import { act, render, screen } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import TicketsPage from './page';
import { fetchConcerts } from '@/lib/api/concert';

vi.mock('@/lib/api/concert', () => ({ fetchConcerts: vi.fn() }));
vi.mock('next/navigation', () => ({ useRouter: () => ({ back: vi.fn() }) }));

const concert = {
  id: '11111111-1111-1111-1111-111111111111', externalId: 'PF298563',
  title: 'WHIB FAN CONCERT: BLUE HOUR', artist: '김준민, 이하준',
  venue: 'NOL 씨어터 합정 · 동양생명홀', startDate: '2026-09-12', endDate: '2026-09-12',
  status: '공연예정' as const, posterUrl: null, performanceTime: '토요일(18:00)',
  priceText: '전석 110,000원', performers: '김준민, 이하준', runtime: '100분',
  ageRating: '만 7세 이상', venueAddress: '서울특별시',
  ticketUrl: 'https://kopis.or.kr/por/db/pblprfr/pblprfrView.do?menuId=MNU_00020&mt20Id=PF298563',
};
const page = { items: [concert], page: 0, size: 20, totalElements: 1, totalPages: 1, last: true };

describe('TicketsPage', () => {
  beforeEach(() => {
    vi.mocked(fetchConcerts).mockReset();
  });

  it('shows real ticket information without claiming that FanPulse sells tickets', async () => {
    vi.mocked(fetchConcerts).mockResolvedValue(page);
    render(<TicketsPage />);
    expect(await screen.findByText(concert.title)).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'KOPIS 공식 정보 보기' })).toHaveAttribute('href', concert.ticketUrl);
    expect(screen.getByText('FanPulse는 티켓을 판매하거나 예약하지 않습니다.')).toBeInTheDocument();
    expect(screen.queryByText(/예매 가능한/)).not.toBeInTheDocument();
    expect(screen.queryByText('QR 코드')).not.toBeInTheDocument();
    expect(screen.queryByText('주문번호')).not.toBeInTheDocument();
  });

  it('formats KOPIS date-only values in the Seoul timezone', async () => {
    const originalTimezone = process.env.TZ;
    process.env.TZ = 'UTC';
    try {
      vi.mocked(fetchConcerts).mockResolvedValue(page);
      render(<TicketsPage />);

      expect(await screen.findByText('2026년 9월 12일')).toBeInTheDocument();
    } finally {
      if (originalTimezone === undefined) delete process.env.TZ;
      else process.env.TZ = originalTimezone;
    }
  });

  it('shows an honest empty state', async () => {
    vi.mocked(fetchConcerts).mockResolvedValue({ ...page, items: [], totalElements: 0, totalPages: 0 });
    render(<TicketsPage />);
    expect(await screen.findByText('현재 확인할 수 있는 티켓 정보가 없습니다.')).toBeInTheDocument();
  });

  it('fails closed without static tickets', async () => {
    let rejectFetch: ((reason: Error) => void) | undefined;
    vi.mocked(fetchConcerts).mockImplementation(() => new Promise((_, reject) => { rejectFetch = reject; }));
    render(<TicketsPage />);
    await vi.waitFor(() => expect(rejectFetch).toBeDefined());
    await act(async () => rejectFetch?.(new Error('network')));
    expect(await screen.findByText('티켓 정보를 불러오지 못했습니다.')).toBeInTheDocument();
  });
});
