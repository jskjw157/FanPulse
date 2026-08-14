import { render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@/lib/api/home', () => ({
  fetchLiveNow: vi.fn(),
  fetchUpcoming: vi.fn(),
  fetchRecentLives: vi.fn(),
  fetchLatestNews: vi.fn(),
}));

import Home from './page';
import {
  fetchLiveNow,
  fetchUpcoming,
  fetchRecentLives,
  fetchLatestNews,
} from '@/lib/api/home';
import { mockLiveNow, mockUpcoming } from '@/__mocks__/live';
import { mockLatestNews } from '@/__mocks__/news';

const mockedFetchLiveNow = vi.mocked(fetchLiveNow);
const mockedFetchUpcoming = vi.mocked(fetchUpcoming);
const mockedFetchRecentLives = vi.mocked(fetchRecentLives);
const mockedFetchLatestNews = vi.mocked(fetchLatestNews);

describe('Home Page Navigation', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockedFetchLiveNow.mockResolvedValue({ items: mockLiveNow, hasMore: false });
    mockedFetchUpcoming.mockResolvedValue({ items: mockUpcoming, hasMore: false });
    mockedFetchRecentLives.mockResolvedValue({ items: [], hasMore: false });
    mockedFetchLatestNews.mockResolvedValue(mockLatestNews);
  });

  it('renders canonical links for API-backed home content', async () => {
    render(<Home />);

    await waitFor(() => {
      expect(screen.getByText(mockLiveNow[0].title).closest('a')).toHaveAttribute(
        'href',
        `/live/${mockLiveNow[0].id}`,
      );
      expect(screen.getByText(mockLatestNews[0].title).closest('a')).toHaveAttribute(
        'href',
        `/news/${mockLatestNews[0].id}`,
      );
    });
  });
});
