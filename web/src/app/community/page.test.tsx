import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import CommunityPage from './page';
import { fetchCommunityPosts } from '@/lib/api/community';

vi.mock('next/navigation', () => ({
  useRouter: () => ({ back: vi.fn(), push: vi.fn() }),
}));

vi.mock('framer-motion', () => ({
  motion: {
    div: ({ children, layoutId, ...props }: React.HTMLAttributes<HTMLDivElement> & { layoutId?: string }) => {
      void layoutId;
      return <div {...props}>{children}</div>;
    },
  },
}));

vi.mock('@/lib/api/community', () => ({
  fetchCommunityPosts: vi.fn(),
}));

const post = {
  id: '11111111-1111-1111-1111-111111111111',
  author: { id: '22222222-2222-2222-2222-222222222222', name: 'real-author' },
  artist: { id: '33333333-3333-3333-3333-333333333333', name: 'Real Artist', profileImageUrl: null },
  content: '실제 PostgreSQL 게시글',
  imageUrl: null,
  likeCount: 2,
  commentCount: 1,
  createdAt: '2026-08-14T08:00:00Z',
};

const page = {
  items: [post],
  page: 0,
  size: 20,
  totalElements: 1,
  totalPages: 1,
  last: true,
};

describe('CommunityPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(fetchCommunityPosts).mockResolvedValue(page);
  });

  it('renders only posts returned by the real community API', async () => {
    render(<CommunityPage />);

    expect(await screen.findByText('실제 PostgreSQL 게시글')).toBeInTheDocument();
    expect(screen.getByText('real-author')).toBeInTheDocument();
    expect(screen.getByText('Real Artist')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /실제 PostgreSQL 게시글/ })).toHaveAttribute(
      'href',
      `/post-detail?id=${post.id}`,
    );
    expect(screen.queryByText(/BTS 새 앨범 티저/)).not.toBeInTheDocument();
    expect(fetchCommunityPosts).toHaveBeenCalledWith('LATEST', 0, 20, expect.any(AbortSignal));
  });

  it('renders an honest empty state without demo posts', async () => {
    vi.mocked(fetchCommunityPosts).mockResolvedValue({ ...page, items: [], totalElements: 0, totalPages: 0 });
    render(<CommunityPage />);

    expect(await screen.findByText('아직 작성된 게시글이 없습니다.')).toBeInTheDocument();
    expect(screen.queryByText(/ARMY_Forever/)).not.toBeInTheDocument();
  });

  it('renders a retryable error rather than falling back to fake posts', async () => {
    vi.mocked(fetchCommunityPosts).mockRejectedValueOnce(new Error('network'));
    render(<CommunityPage />);

    expect(await screen.findByText('커뮤니티를 불러오지 못했습니다.')).toBeInTheDocument();
    vi.mocked(fetchCommunityPosts).mockResolvedValue(page);
    fireEvent.click(screen.getByRole('button', { name: '다시 시도' }));
    expect(await screen.findByText('실제 PostgreSQL 게시글')).toBeInTheDocument();
  });

  it('requests server-side popular sorting when the popular tab is selected', async () => {
    render(<CommunityPage />);
    await screen.findByText('실제 PostgreSQL 게시글');

    fireEvent.click(screen.getByRole('button', { name: '인기' }));

    await waitFor(() => {
      expect(fetchCommunityPosts).toHaveBeenLastCalledWith('POPULAR', 0, 20, expect.any(AbortSignal));
    });
  });

  it('discards an old load-more response after the sort changes', async () => {
    const stalePost = { ...post, id: '44444444-4444-4444-4444-444444444444', content: '이전 정렬 게시글' };
    const popularPost = { ...post, id: '55555555-5555-5555-5555-555555555555', content: '현재 인기 게시글' };
    let resolveStale: ((value: typeof page) => void) | undefined;
    vi.mocked(fetchCommunityPosts).mockImplementation((sort, requestPage) => {
      if (sort === 'LATEST' && requestPage === 0) {
        return Promise.resolve({ ...page, totalElements: 2, totalPages: 2, last: false });
      }
      if (sort === 'LATEST' && requestPage === 1) {
        return new Promise((resolve) => { resolveStale = resolve; });
      }
      return Promise.resolve({ ...page, items: [popularPost] });
    });

    render(<CommunityPage />);
    await screen.findByText(post.content);
    fireEvent.click(screen.getByRole('button', { name: '더 보기' }));
    fireEvent.click(screen.getByRole('button', { name: '인기' }));
    await screen.findByText(popularPost.content);

    await act(async () => {
      resolveStale?.({
        ...page,
        items: [stalePost],
        page: 1,
        totalElements: 2,
        totalPages: 2,
        last: true,
      });
    });

    expect(screen.queryByText(stalePost.content)).not.toBeInTheDocument();
    expect(screen.getByText(popularPost.content)).toBeInTheDocument();
  });
});
