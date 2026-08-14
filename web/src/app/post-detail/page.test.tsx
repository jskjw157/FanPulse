import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import PostDetailPage from './page';
import {
  createCommunityComment,
  fetchCommunityComments,
  fetchCommunityPost,
  fetchCommunityPostState,
  likeCommunityPost,
  saveCommunityPost,
} from '@/lib/api/community';

const nav = vi.hoisted(() => ({
  id: '11111111-1111-1111-1111-111111111111',
  back: vi.fn(),
  push: vi.fn(),
}));

vi.mock('next/navigation', () => ({
  useRouter: () => ({ back: nav.back, push: nav.push }),
  useSearchParams: () => ({ get: (key: string) => key === 'id' ? nav.id : null }),
}));
vi.mock('@/contexts/AuthContext', () => ({
  useAuth: () => ({ isAuthenticated: true, isLoading: false }),
}));
vi.mock('@/lib/api/community', () => ({
  createCommunityComment: vi.fn(),
  fetchCommunityComments: vi.fn(),
  fetchCommunityPost: vi.fn(),
  fetchCommunityPostState: vi.fn(),
  likeCommunityPost: vi.fn(),
  saveCommunityPost: vi.fn(),
  unlikeCommunityPost: vi.fn(),
  unsaveCommunityPost: vi.fn(),
}));

const post = {
  id: nav.id,
  author: { id: '22222222-2222-2222-2222-222222222222', name: 'real-author' },
  artist: { id: '33333333-3333-3333-3333-333333333333', name: 'Real Artist', profileImageUrl: null },
  content: '실제 상세 게시글',
  imageUrl: null,
  likeCount: 2,
  commentCount: 1,
  createdAt: '2026-08-14T08:00:00Z',
};

const comment = {
  id: '44444444-4444-4444-4444-444444444444',
  postId: nav.id,
  userId: post.author.id,
  content: '실제 저장 댓글',
  status: 'APPROVED' as const,
  parentCommentId: null,
  createdAt: '2026-08-14T09:00:00Z',
  authorName: 'comment-author',
};

const comments = {
  items: [comment],
  totalElements: 1,
  page: 0,
  size: 20,
  totalPages: 1,
};

describe('PostDetailPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    nav.id = post.id;
    vi.mocked(fetchCommunityPost).mockResolvedValue(post);
    vi.mocked(fetchCommunityComments).mockResolvedValue(comments);
    vi.mocked(fetchCommunityPostState).mockResolvedValue({ liked: false, saved: false });
    vi.mocked(likeCommunityPost).mockResolvedValue({ liked: true, saved: false });
    vi.mocked(saveCommunityPost).mockResolvedValue({ liked: true, saved: true });
    vi.mocked(createCommunityComment).mockResolvedValue(comment);
  });

  it('renders only the persisted post and persisted comments', async () => {
    render(<PostDetailPage />);

    expect(await screen.findByText('실제 상세 게시글')).toBeInTheDocument();
    expect(await screen.findByText('실제 저장 댓글')).toBeInTheDocument();
    expect(screen.getByText('real-author')).toBeInTheDocument();
    expect(screen.getByText('comment-author')).toBeInTheDocument();
    expect(screen.queryByText('ARMY_Forever')).not.toBeInTheDocument();
    expect(fetchCommunityPost).toHaveBeenCalledWith(post.id, expect.any(AbortSignal));
    expect(fetchCommunityComments).toHaveBeenCalledWith(post.id, 0, 20, expect.any(AbortSignal));
  });

  it('persists like and save mutations for the authenticated user', async () => {
    render(<PostDetailPage />);
    await screen.findByText('실제 상세 게시글');

    fireEvent.click(screen.getByRole('button', { name: '좋아요' }));
    await waitFor(() => expect(likeCommunityPost).toHaveBeenCalledWith(post.id));
    fireEvent.click(screen.getByRole('button', { name: '저장' }));
    await waitFor(() => expect(saveCommunityPost).toHaveBeenCalledWith(post.id));
  });

  it('creates a real comment and reloads the persisted comment list', async () => {
    vi.mocked(fetchCommunityComments)
      .mockResolvedValueOnce({ ...comments, items: [], totalElements: 0, totalPages: 0 })
      .mockResolvedValueOnce(comments);
    render(<PostDetailPage />);
    await screen.findByText('실제 상세 게시글');

    fireEvent.change(screen.getByLabelText('댓글 내용'), { target: { value: '실제 저장 댓글' } });
    fireEvent.click(screen.getByRole('button', { name: '댓글 등록' }));

    await waitFor(() => expect(createCommunityComment).toHaveBeenCalledWith(post.id, '실제 저장 댓글'));
    expect(await screen.findByText('실제 저장 댓글')).toBeInTheDocument();
    expect(fetchCommunityComments).toHaveBeenCalledTimes(2);
  });

  it('shows an honest error instead of a mock post when detail loading fails', async () => {
    vi.mocked(fetchCommunityPost).mockRejectedValue(new Error('not found'));
    render(<PostDetailPage />);

    expect(await screen.findByText('게시글을 불러오지 못했습니다.')).toBeInTheDocument();
    expect(screen.queryByText('ARMY_Forever')).not.toBeInTheDocument();
  });
});
