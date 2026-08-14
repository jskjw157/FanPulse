import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import SavedPage from './page';
import { fetchSavedCommunityPosts, unsaveCommunityPost } from '@/lib/api/community';

vi.mock('next/navigation', () => ({
  useRouter: () => ({ back: vi.fn(), push: vi.fn() }),
}));
vi.mock('@/components/auth/ProtectedRoute', () => ({
  default: ({ children }: { children: React.ReactNode }) => <>{children}</>,
}));
vi.mock('@/lib/api/community', () => ({
  fetchSavedCommunityPosts: vi.fn(),
  unsaveCommunityPost: vi.fn(),
}));

const post = {
  id: '11111111-1111-1111-1111-111111111111',
  author: { id: '22222222-2222-2222-2222-222222222222', name: 'saved-author' },
  artist: null,
  content: '실제로 저장한 게시글',
  imageUrl: null,
  likeCount: 3,
  commentCount: 2,
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

describe('SavedPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(fetchSavedCommunityPosts).mockResolvedValue(page);
    vi.mocked(unsaveCommunityPost).mockResolvedValue({ liked: false, saved: false });
  });

  it('renders only posts saved by the authenticated user', async () => {
    render(<SavedPage />);
    expect(await screen.findByText('실제로 저장한 게시글')).toBeInTheDocument();
    expect(screen.getByText('saved-author')).toBeInTheDocument();
    expect(screen.queryByText('ARMY_Forever')).not.toBeInTheDocument();
  });

  it('renders a neutral empty state', async () => {
    vi.mocked(fetchSavedCommunityPosts).mockResolvedValue({
      ...page,
      items: [],
      totalElements: 0,
      totalPages: 0,
    });
    render(<SavedPage />);
    expect(await screen.findByText('저장한 게시글이 없습니다.')).toBeInTheDocument();
  });

  it('fails closed instead of showing static saved posts', async () => {
    vi.mocked(fetchSavedCommunityPosts).mockRejectedValue(new Error('network'));
    render(<SavedPage />);
    expect(await screen.findByText('저장한 게시글을 불러오지 못했습니다.')).toBeInTheDocument();
    expect(screen.queryByText('ARMY_Forever')).not.toBeInTheDocument();
  });

  it('removes a post only after the real unsave API succeeds', async () => {
    render(<SavedPage />);
    await screen.findByText('실제로 저장한 게시글');
    fireEvent.click(screen.getByRole('button', { name: '저장 취소' }));

    await waitFor(() => expect(unsaveCommunityPost).toHaveBeenCalledWith(post.id));
    expect(await screen.findByText('저장한 게시글이 없습니다.')).toBeInTheDocument();
  });
});
