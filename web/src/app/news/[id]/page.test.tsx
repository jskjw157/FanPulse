import { render, screen, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';

vi.mock('next/navigation', () => ({
  useParams: vi.fn(() => ({ id: '1' })),
  useRouter: () => ({
    back: vi.fn(),
  }),
}));

vi.mock('@/hooks/useNewsDetail', () => ({
  useNewsDetail: vi.fn(),
}));

import NewsDetailPage from './page';
import { useNewsDetail } from '@/hooks/useNewsDetail';
import { mockNewsDetail } from '@/__mocks__/news';

const mockedUseNewsDetail = vi.mocked(useNewsDetail);

describe('NewsDetailPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('shows loading skeleton while fetching', () => {
    mockedUseNewsDetail.mockReturnValue({
      news: null,
      state: 'loading',
      error: null,
      retry: vi.fn(),
    });

    render(<NewsDetailPage />);

    expect(screen.getByText('뒤로')).toBeInTheDocument();
    expect(document.querySelector('.animate-pulse')).toBeInTheDocument();
  });

  it('renders news detail on success', async () => {
    mockedUseNewsDetail.mockReturnValue({
      news: mockNewsDetail,
      state: 'success',
      error: null,
      retry: vi.fn(),
    });

    render(<NewsDetailPage />);

    await waitFor(() => {
      expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent(
        'BTS 새 앨범 발매 예정'
      );
    });

    expect(screen.getByText('스포츠조선')).toBeInTheDocument();
    expect(screen.getByText('김기자')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /원문 보기/ })).toBeInTheDocument();
  });

  it('shows 404 error message when news not found', () => {
    mockedUseNewsDetail.mockReturnValue({
      news: null,
      state: 'error',
      error: '뉴스를 찾을 수 없습니다',
      retry: vi.fn(),
    });

    render(<NewsDetailPage />);

    expect(screen.getByText('뉴스를 찾을 수 없습니다')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: '홈으로 이동' })).toBeInTheDocument();
  });

  it('shows network error with retry button', () => {
    const retryMock = vi.fn();
    mockedUseNewsDetail.mockReturnValue({
      news: null,
      state: 'error',
      error: '뉴스를 불러올 수 없습니다',
      retry: retryMock,
    });

    render(<NewsDetailPage />);

    expect(screen.getByText('뉴스를 불러올 수 없습니다')).toBeInTheDocument();

    const retryButton = screen.getByRole('button', { name: '다시 시도' });
    expect(retryButton).toBeInTheDocument();

    retryButton.click();
    expect(retryMock).toHaveBeenCalled();
  });
});
