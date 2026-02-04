import { render, screen, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';

vi.mock('@/lib/api/home', () => ({
  fetchLiveNow: vi.fn(),
  fetchUpcoming: vi.fn(),
  fetchLatestNews: vi.fn(),
}));

import HomePage from './page';
import { fetchLiveNow, fetchUpcoming, fetchLatestNews } from '@/lib/api/home';
import { mockLiveNow, mockUpcoming } from '@/__mocks__/live';
import { mockLatestNews } from '@/__mocks__/news';

const mockedFetchLiveNow = vi.mocked(fetchLiveNow);
const mockedFetchUpcoming = vi.mocked(fetchUpcoming);
const mockedFetchLatestNews = vi.mocked(fetchLatestNews);

describe('HomePage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockedFetchLiveNow.mockResolvedValue({ items: mockLiveNow, hasMore: false });
    mockedFetchUpcoming.mockResolvedValue({ items: mockUpcoming, hasMore: false });
    mockedFetchLatestNews.mockResolvedValue(mockLatestNews);
  });

  it('renders all three sections', async () => {
    render(<HomePage />);

    await waitFor(() => {
      expect(screen.getByText('Live Now')).toBeInTheDocument();
      expect(screen.getByText('Upcoming')).toBeInTheDocument();
      expect(screen.getByText('최신 뉴스')).toBeInTheDocument();
    });
  });

  it('renders live cards after loading', async () => {
    render(<HomePage />);

    await waitFor(() => {
      expect(screen.getByText('NewJeans 컴백 쇼케이스')).toBeInTheDocument();
      expect(screen.getByText('BTS Fan Meeting Special')).toBeInTheDocument();
    });
  });

  it('renders upcoming cards after loading', async () => {
    render(<HomePage />);

    await waitFor(() => {
      expect(screen.getByText('SEVENTEEN Dance Practice')).toBeInTheDocument();
    });
  });

  it('renders news cards after loading', async () => {
    render(<HomePage />);

    await waitFor(() => {
      expect(screen.getByText('BTS 새 앨범 발매 예정')).toBeInTheDocument();
    });
  });

  it('shows error state when API fails', async () => {
    mockedFetchLiveNow.mockRejectedValue(new Error('fail'));

    render(<HomePage />);

    await waitFor(() => {
      expect(screen.getByText('데이터를 불러올 수 없습니다')).toBeInTheDocument();
    });
  });

  it('calls all three APIs on mount', async () => {
    render(<HomePage />);

    await waitFor(() => {
      expect(mockedFetchLiveNow).toHaveBeenCalledOnce();
      expect(mockedFetchUpcoming).toHaveBeenCalledOnce();
      expect(mockedFetchLatestNews).toHaveBeenCalledOnce();
    });
  });
});
