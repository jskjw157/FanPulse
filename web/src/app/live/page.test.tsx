import { render, screen, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import LiveListPage from './page';

// Mock IntersectionObserver
class MockIntersectionObserver implements IntersectionObserver {
  readonly root: Element | Document | null = null;
  readonly rootMargin: string = '';
  readonly thresholds: ReadonlyArray<number> = [];
  observe = vi.fn();
  unobserve = vi.fn();
  disconnect = vi.fn();
  takeRecords = vi.fn(() => []);
}

beforeEach(() => {
  vi.stubGlobal('IntersectionObserver', MockIntersectionObserver);
});

afterEach(() => {
  vi.unstubAllGlobals();
});

vi.mock('@/lib/api/live', () => ({
  fetchLiveList: vi.fn(),
}));

vi.mock('next/navigation', () => ({
  useRouter: () => ({
    back: vi.fn(),
  }),
}));

import { fetchLiveList } from '@/lib/api/live';
import { mockLiveList } from '@/__mocks__/live';

const mockedFetchLiveList = vi.mocked(fetchLiveList);

describe('LiveListPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders page header', async () => {
    mockedFetchLiveList.mockResolvedValue({
      items: mockLiveList,
      hasMore: false,
    });

    render(<LiveListPage />);

    await waitFor(() => {
      expect(screen.getByText('Live Now')).toBeInTheDocument();
    });
  });

  it('renders live items after loading', async () => {
    mockedFetchLiveList.mockResolvedValue({
      items: mockLiveList,
      hasMore: false,
    });

    render(<LiveListPage />);

    await waitFor(() => {
      expect(screen.getByText('NewJeans 컴백 쇼케이스')).toBeInTheDocument();
    });
  });

  it('shows error state when API fails', async () => {
    mockedFetchLiveList.mockRejectedValue(new Error('API Error'));

    render(<LiveListPage />);

    await waitFor(() => {
      expect(screen.getByText('데이터를 불러올 수 없습니다')).toBeInTheDocument();
    });
  });

  it('shows empty message when no lives', async () => {
    mockedFetchLiveList.mockResolvedValue({
      items: [],
      hasMore: false,
    });

    render(<LiveListPage />);

    await waitFor(() => {
      expect(screen.getByText('라이브가 없습니다')).toBeInTheDocument();
    });
  });
});
