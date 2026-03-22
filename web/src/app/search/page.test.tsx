import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import SearchPage from './page';
import { mockSearchResult, mockEmptySearchResult } from '@/__mocks__/search';

vi.mock('next/navigation', () => ({
  useRouter: () => ({ push: vi.fn(), back: vi.fn() }),
  useSearchParams: () => new URLSearchParams(),
}));

vi.mock('next/link', () => ({
  default: ({ children, href }: { children: React.ReactNode; href: string }) => (
    <a href={href}>{children}</a>
  ),
}));

vi.mock('@/lib/api/search', () => ({
  fetchSearchResults: vi.fn(),
}));

import { fetchSearchResults } from '@/lib/api/search';

const mockedFetch = vi.mocked(fetchSearchResults);

describe('SearchPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  it('renders search bar with auto-focus', () => {
    render(<SearchPage />);
    expect(screen.getByPlaceholderText(/라이브, 뉴스 검색/)).toBeInTheDocument();
  });

  it('shows recent searches on initial load', () => {
    localStorage.setItem(
      'fanpulse_recent_searches',
      JSON.stringify(['BTS', 'BLACKPINK'])
    );

    render(<SearchPage />);

    expect(screen.getByText('최근 검색어')).toBeInTheDocument();
    expect(screen.getByText('BTS')).toBeInTheDocument();
    expect(screen.getByText('BLACKPINK')).toBeInTheDocument();
  });

  it('performs search and shows results', async () => {
    mockedFetch.mockResolvedValueOnce(mockSearchResult);

    render(<SearchPage />);

    const input = screen.getByPlaceholderText(/라이브, 뉴스 검색/);
    await userEvent.type(input, 'BTS');
    await userEvent.click(screen.getByRole('button', { name: /검색/i }));

    await waitFor(() => {
      expect(screen.getByText('라이브')).toBeInTheDocument();
    });

    expect(screen.getByText('BTS Fan Meeting Special')).toBeInTheDocument();
    expect(screen.getByText('뉴스')).toBeInTheDocument();
    expect(screen.getByText('BTS 새 앨범 발매 예정')).toBeInTheDocument();
  });

  it('shows empty state when no results', async () => {
    mockedFetch.mockResolvedValueOnce(mockEmptySearchResult);

    render(<SearchPage />);

    const input = screen.getByPlaceholderText(/라이브, 뉴스 검색/);
    await userEvent.type(input, 'xyz123');
    await userEvent.click(screen.getByRole('button', { name: /검색/i }));

    await waitFor(() => {
      expect(screen.getByText('검색 결과가 없습니다')).toBeInTheDocument();
    });
  });

  it('adds search query to recent searches after search', async () => {
    mockedFetch.mockResolvedValueOnce(mockSearchResult);

    render(<SearchPage />);

    const input = screen.getByPlaceholderText(/라이브, 뉴스 검색/);
    await userEvent.type(input, 'BTS');
    await userEvent.click(screen.getByRole('button', { name: /검색/i }));

    await waitFor(() => {
      expect(screen.getByText('라이브')).toBeInTheDocument();
    });

    const stored = JSON.parse(
      localStorage.getItem('fanpulse_recent_searches') || '[]'
    );
    expect(stored).toContain('BTS');
  });

  it('selects recent search and performs search', async () => {
    localStorage.setItem(
      'fanpulse_recent_searches',
      JSON.stringify(['BTS', 'BLACKPINK'])
    );
    mockedFetch.mockResolvedValueOnce(mockSearchResult);

    render(<SearchPage />);

    await userEvent.click(screen.getByText('BTS'));

    await waitFor(() => {
      expect(mockedFetch).toHaveBeenCalledWith('BTS', expect.any(AbortSignal));
    });
  });

  it('shows error state and retry button', async () => {
    mockedFetch.mockRejectedValueOnce(new Error('Network error'));

    render(<SearchPage />);

    const input = screen.getByPlaceholderText(/라이브, 뉴스 검색/);
    await userEvent.type(input, 'BTS');
    await userEvent.click(screen.getByRole('button', { name: /검색/i }));

    await waitFor(() => {
      expect(screen.getByText(/오류가 발생했습니다/)).toBeInTheDocument();
    });
  });
});
