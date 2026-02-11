import { render, screen, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';

// Mock IntersectionObserver - must be set before component imports
class MockIntersectionObserver implements IntersectionObserver {
  readonly root: Element | Document | null = null;
  readonly rootMargin: string = '';
  readonly thresholds: ReadonlyArray<number> = [];
  observe = vi.fn();
  unobserve = vi.fn();
  disconnect = vi.fn();
  takeRecords = vi.fn(() => []);
}

vi.stubGlobal('IntersectionObserver', MockIntersectionObserver);

import LiveDetailPage from './page';

vi.mock('@/lib/api/live', () => ({
  fetchLiveDetail: vi.fn(),
}));

vi.mock('next/navigation', () => ({
  useParams: () => ({ id: '1' }),
  useRouter: () => ({ back: vi.fn(), push: vi.fn() }),
}));

import { fetchLiveDetail } from '@/lib/api/live';
import { mockLiveDetail } from '@/__mocks__/live';

const mockedFetchLiveDetail = vi.mocked(fetchLiveDetail);

describe('LiveDetailPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders YouTube player after loading', async () => {
    mockedFetchLiveDetail.mockResolvedValue(mockLiveDetail);

    render(<LiveDetailPage />);

    await waitFor(() => {
      // YouTubePlayer receives title={live.title}, so it uses the live's title
      const iframe = screen.getByTitle(mockLiveDetail.title);
      expect(iframe).toBeInTheDocument();
      expect(iframe).toHaveAttribute(
        'src',
        expect.stringContaining('youtube.com/embed')
      );
    });
  });

  it('renders live metadata', async () => {
    mockedFetchLiveDetail.mockResolvedValue(mockLiveDetail);

    render(<LiveDetailPage />);

    await waitFor(() => {
      expect(screen.getByText('NewJeans 컴백 쇼케이스')).toBeInTheDocument();
      expect(screen.getByText('NewJeans Official')).toBeInTheDocument();
    });
  });

  it('shows error state for non-existent live', async () => {
    mockedFetchLiveDetail.mockRejectedValue({
      response: { status: 404 },
    });

    render(<LiveDetailPage />);

    await waitFor(() => {
      expect(screen.getByText('라이브를 찾을 수 없습니다')).toBeInTheDocument();
    });
  });

  it('renders back button', async () => {
    mockedFetchLiveDetail.mockResolvedValue(mockLiveDetail);

    render(<LiveDetailPage />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /뒤로/i })).toBeInTheDocument();
    });
  });
});
