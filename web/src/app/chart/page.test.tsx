import { render, screen } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@/hooks/useLatestChart', () => ({
  useLatestChart: vi.fn(),
}));

vi.mock('next/navigation', () => ({
  useRouter: () => ({ back: vi.fn() }),
}));

import ChartPage from './page';
import { useLatestChart } from '@/hooks/useLatestChart';

const mockedUseLatestChart = vi.mocked(useLatestChart);

const apiChart = {
  id: 'chart-1',
  chartType: 'APPLE_MUSIC' as const,
  chartDate: '2026-08-13',
  createdAt: '2026-08-13T00:00:00Z',
  entries: [
    {
      id: 'entry-1',
      rank: 1,
      trackId: 'track-1',
      artistId: 'artist-123',
      trackTitle: 'API Track',
      artistName: 'API Artist',
      previousRank: 3,
      peakRank: 1,
      weeksOnChart: 5,
      rankChange: 2,
      isNew: false,
      artworkUrl: 'https://is1-ssl.mzstatic.com/image/thumb/Music211/v4/ab/cd/ef/cover/100x100bb.jpg',
    },
    {
      id: 'entry-2',
      rank: 2,
      trackId: 'track-2',
      artistId: null,
      trackTitle: 'Unmatched Track',
      artistName: 'Unknown Artist',
      previousRank: null,
      peakRank: 2,
      weeksOnChart: 1,
      rankChange: null,
      isNew: true,
      artworkUrl: null,
    },
  ],
};

describe('ChartPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders API chart entries with ID-based canonical artist links', () => {
    mockedUseLatestChart.mockReturnValue({
      chart: apiChart,
      state: 'success',
      error: null,
      retry: vi.fn(),
    });

    render(<ChartPage />);

    expect(mockedUseLatestChart).toHaveBeenCalledWith('APPLE_MUSIC');
    expect(screen.getByText('Apple Music Korea Top 100')).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'API Track' })).toBeInTheDocument();
    expect(screen.getByText('API Artist')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /API Track/ })).toHaveAttribute(
      'href',
      '/artists/artist-123'
    );
    expect(screen.getByRole('img', { name: 'API Track' })).toHaveAttribute(
      'src',
      'https://is1-ssl.mzstatic.com/image/thumb/Music211/v4/ab/cd/ef/cover/100x100bb.jpg'
    );
    expect(screen.getByText('Unmatched Track')).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: /Unmatched Track/ })).not.toBeInTheDocument();
    expect(screen.queryByText('Super Shy')).not.toBeInTheDocument();
  });

  it('shows an explicit loading state', () => {
    mockedUseLatestChart.mockReturnValue({
      chart: null,
      state: 'loading',
      error: null,
      retry: vi.fn(),
    });

    render(<ChartPage />);

    expect(screen.getByText('차트를 불러오는 중입니다')).toBeInTheDocument();
  });

  it('shows an explicit API error with retry', () => {
    const retry = vi.fn();
    mockedUseLatestChart.mockReturnValue({
      chart: null,
      state: 'error',
      error: '차트를 불러올 수 없습니다',
      retry,
    });

    render(<ChartPage />);

    expect(screen.getByText('차트를 불러올 수 없습니다')).toBeInTheDocument();
    screen.getByRole('button', { name: '다시 시도' }).click();
    expect(retry).toHaveBeenCalledOnce();
  });

  it('shows an explicit empty state when the API chart has no entries', () => {
    mockedUseLatestChart.mockReturnValue({
      chart: { ...apiChart, entries: [] },
      state: 'success',
      error: null,
      retry: vi.fn(),
    });

    render(<ChartPage />);

    expect(screen.getByText('차트 항목이 없습니다')).toBeInTheDocument();
  });
});
