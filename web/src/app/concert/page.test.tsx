import { act, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import ConcertPage from './page';
import { fetchConcerts } from '@/lib/api/concert';

vi.mock('@/lib/api/concert', () => ({ fetchConcerts: vi.fn() }));
vi.mock('next/navigation', () => ({ useRouter: () => ({ back: vi.fn() }) }));

const concert = {
  id: '11111111-1111-1111-1111-111111111111', externalId: 'PF298563',
  title: 'WHIB FAN CONCERT: BLUE HOUR', artist: '김준민, 이하준',
  venue: 'NOL 씨어터 합정 · 동양생명홀', startDate: '2026-09-12', endDate: '2026-09-12',
  status: '공연예정' as const, posterUrl: 'https://kopis.or.kr/upload/PF298563.gif',
  performanceTime: '토요일(18:00)', priceText: '전석 110,000원', performers: '김준민, 이하준',
  runtime: '100분', ageRating: '만 7세 이상', venueAddress: '서울특별시',
  ticketUrl: 'https://kopis.or.kr/por/db/pblprfr/pblprfrView.do?menuId=MNU_00020&mt20Id=PF298563',
};
const response = { items: [concert], page: 0, size: 20, totalElements: 1, totalPages: 1, last: true };

describe('ConcertPage', () => {
  beforeEach(() => {
    vi.mocked(fetchConcerts).mockReset();
  });

  it('renders actual API concerts without static mock rows', async () => {
    vi.mocked(fetchConcerts).mockResolvedValue(response);
    render(<ConcertPage />);
    expect(await screen.findByText(concert.title)).toBeInTheDocument();
    expect(screen.getByText(concert.venue)).toBeInTheDocument();
    expect(screen.getByRole('link', { name: `${concert.title} 상세 보기` }))
      .toHaveAttribute('href', `/concert-detail?id=${concert.id}`);
    expect(screen.queryByText('BTS World Tour Seoul')).not.toBeInTheDocument();
  });

  it('formats KOPIS date-only values in the Seoul timezone', async () => {
    const originalTimezone = process.env.TZ;
    process.env.TZ = 'UTC';
    try {
      vi.mocked(fetchConcerts).mockResolvedValue(response);
      render(<ConcertPage />);

      expect(await screen.findByText('2026년 9월 12일')).toBeInTheDocument();
    } finally {
      if (originalTimezone === undefined) delete process.env.TZ;
      else process.env.TZ = originalTimezone;
    }
  });

  it('renders an honest empty state', async () => {
    vi.mocked(fetchConcerts).mockResolvedValue({ ...response, items: [], totalElements: 0, totalPages: 0 });
    render(<ConcertPage />);
    expect(await screen.findByText('예정된 공연이 없습니다.')).toBeInTheDocument();
  });

  it('renders an error without fallback rows', async () => {
    let rejectFetch: ((reason: Error) => void) | undefined;
    vi.mocked(fetchConcerts).mockImplementation(() => new Promise((_, reject) => {
      rejectFetch = reject;
    }));
    render(<ConcertPage />);
    await waitFor(() => expect(rejectFetch).toBeDefined());
    await act(async () => rejectFetch?.(new Error('network')));
    expect(await screen.findByText('공연 정보를 불러오지 못했습니다.')).toBeInTheDocument();
    await waitFor(() => expect(screen.queryByText(concert.title)).not.toBeInTheDocument());
  });
});
