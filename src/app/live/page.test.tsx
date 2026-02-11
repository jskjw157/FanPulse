import { render, screen, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import LiveListPage from './page';

vi.mock('@/lib/api/live', () => ({
  fetchLiveList: vi.fn(),
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
      expect(screen.getByRole('heading', { name: /Live/i })).toBeInTheDocument();
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
