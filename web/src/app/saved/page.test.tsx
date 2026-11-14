import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import SavedPage from './page';
import { fetchSavedCommunityPosts, unsaveCommunityPost } from '@/lib/api/community';

const auth = vi.hoisted(() => ({
  isAuthenticated: true,
  isLoading: false,
  sessionVersion: 0,
  user: null as { id: string } | null,
}));
vi.mock('@/contexts/AuthContext', () => ({ useAuth: () => auth }));
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

const nextPost = {
  ...post,
  id: '33333333-3333-3333-3333-333333333333',
  content: '다시 채워진 저장 게시글',
};

function deferred<T>() {
  let resolve!: (value: T) => void;
  const promise = new Promise<T>((done) => { resolve = done; });
  return { promise, resolve };
}

describe('SavedPage', () => {
  beforeEach(() => {
    vi.resetAllMocks();
    auth.isAuthenticated = true;
    auth.isLoading = false;
    auth.sessionVersion = 0;
    auth.user = { id: post.author.id };
    vi.mocked(fetchSavedCommunityPosts).mockResolvedValue(page);
    vi.mocked(unsaveCommunityPost).mockResolvedValue({ liked: false, saved: false });
  });

  it('renders only posts saved by the authenticated user', async () => {
    render(<SavedPage />);
    expect(await screen.findByText('실제로 저장한 게시글')).toBeInTheDocument();
    expect(screen.getByText('saved-author')).toBeInTheDocument();
    expect(screen.queryByText('ARMY_Forever')).not.toBeInTheDocument();
    expect(fetchSavedCommunityPosts).toHaveBeenCalledWith(0, 20, expect.any(AbortSignal), post.author.id);
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
    vi.mocked(fetchSavedCommunityPosts)
      .mockResolvedValueOnce(page)
      .mockResolvedValueOnce({ ...page, items: [], totalElements: 0, totalPages: 0 });
    render(<SavedPage />);
    await screen.findByText('실제로 저장한 게시글');
    fireEvent.click(screen.getByRole('button', { name: '저장 취소' }));

    await waitFor(() => expect(unsaveCommunityPost).toHaveBeenCalledWith(post.id, post.author.id));
    expect(await screen.findByText('저장한 게시글이 없습니다.')).toBeInTheDocument();
    expect(fetchSavedCommunityPosts).toHaveBeenLastCalledWith(0, 20, undefined, post.author.id);
  });

  it('refetches page zero after unsave so shifted rows and metadata stay authoritative', async () => {
    vi.mocked(fetchSavedCommunityPosts)
      .mockResolvedValueOnce({ ...page, totalElements: 21, totalPages: 2, last: false })
      .mockResolvedValueOnce({
        ...page,
        items: [nextPost],
        totalElements: 20,
        totalPages: 1,
        last: true,
      });
    render(<SavedPage />);
    await screen.findByText(post.content);
    fireEvent.click(screen.getByRole('button', { name: '저장 취소' }));

    expect(await screen.findByText(nextPost.content)).toBeInTheDocument();
    expect(screen.getByText('20')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '더 보기' })).not.toBeInTheDocument();
  });

  it.each(['revalidation', 'account', 'session-version'] as const)('hides saved private rows immediately across %s until fresh reads settle', async (boundary) => {
    const freshRead = deferred<typeof page>();
    const view = render(<SavedPage />);
    await screen.findByText(post.content);
    auth.user = { id: post.author.id };
    view.rerender(<SavedPage />);
    expect(screen.getByText(post.content)).toBeInTheDocument();

    vi.mocked(fetchSavedCommunityPosts).mockReturnValue(freshRead.promise);
    if (boundary === 'revalidation') {
      auth.isLoading = true;
      view.rerender(<SavedPage />);
      expect(screen.queryByText(post.content)).not.toBeInTheDocument();
      expect(screen.queryByRole('button', { name: '저장 취소' })).not.toBeInTheDocument();
      auth.isLoading = false;
    } else if (boundary === 'account') {
      auth.user = { id: '66666666-6666-6666-6666-666666666666' };
    } else auth.sessionVersion += 1;
    view.rerender(<SavedPage />);
    expect(screen.queryByText(post.content)).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '저장 취소' })).not.toBeInTheDocument();
    expect(screen.getByText('저장한 게시글을 불러오는 중입니다.')).toBeInTheDocument();

    await act(async () => { freshRead.resolve({ ...page, items: [nextPost] }); await freshRead.promise; });
    expect(await screen.findByText(nextPost.content)).toBeInTheDocument();
    expect(screen.queryByText(post.content)).not.toBeInTheDocument();
    expect(fetchSavedCommunityPosts).toHaveBeenCalledWith(0, 20, expect.any(AbortSignal), auth.user!.id);
    expect(unsaveCommunityPost).not.toHaveBeenCalled();
  });

  it('does not display a late A saved-page result over the current B page', async () => {
    const oldRead = deferred<typeof page>();
    const userB = '66666666-6666-6666-6666-666666666666';
    vi.mocked(fetchSavedCommunityPosts).mockImplementation((_page, _size, _signal, expectedUserId) =>
      expectedUserId === post.author.id ? oldRead.promise : Promise.resolve({ ...page, items: [nextPost] }));
    const view = render(<SavedPage />);
    expect(screen.getByText('저장한 게시글을 불러오는 중입니다.')).toBeInTheDocument();
    auth.user = { id: userB };
    view.rerender(<SavedPage />);
    expect(await screen.findByText(nextPost.content)).toBeInTheDocument();

    await act(async () => { oldRead.resolve(page); await oldRead.promise; });
    expect(screen.getByText(nextPost.content)).toBeInTheDocument();
    expect(screen.queryByText(post.content)).not.toBeInTheDocument();
    expect(screen.queryByText('저장한 게시글을 불러오지 못했습니다.')).not.toBeInTheDocument();
  });

  it('keeps an in-flight A unsave bound to A while a deliberate B unsave remains usable', async () => {
    const oldUnsave = deferred<{ liked: boolean; saved: boolean }>();
    const userB = '66666666-6666-6666-6666-666666666666';
    // Both accounts saved the same public post; B must not inherit A's busy state.
    const sharedPostForB = { ...post, content: 'B가 저장한 같은 게시글' };
    let bSaved = true;
    vi.mocked(fetchSavedCommunityPosts).mockImplementation((_page, _size, _signal, expectedUserId) => Promise.resolve(
      expectedUserId === post.author.id ? page : {
        ...page, items: bSaved ? [sharedPostForB] : [], totalElements: bSaved ? 1 : 0, totalPages: bSaved ? 1 : 0,
      },
    ));
    vi.mocked(unsaveCommunityPost).mockImplementation((_postId, expectedUserId) => {
      if (expectedUserId === post.author.id) return oldUnsave.promise;
      bSaved = false;
      return Promise.resolve({ liked: false, saved: false });
    });
    const view = render(<SavedPage />);
    await screen.findByText(post.content);
    fireEvent.click(screen.getByRole('button', { name: '저장 취소' }));
    expect(unsaveCommunityPost).toHaveBeenCalledWith(post.id, post.author.id);
    expect(screen.getByRole('button', { name: '저장 취소' })).toBeDisabled();

    auth.user = { id: userB };
    view.rerender(<SavedPage />);
    await screen.findByText(sharedPostForB.content);
    expect(screen.getByRole('button', { name: '저장 취소' })).toBeEnabled();
    fireEvent.click(screen.getByRole('button', { name: '저장 취소' }));
    expect(await screen.findByText('저장한 게시글이 없습니다.')).toBeInTheDocument();
    expect(unsaveCommunityPost).toHaveBeenCalledWith(post.id, userB);

    await act(async () => { oldUnsave.resolve({ liked: false, saved: false }); await oldUnsave.promise; });
    expect(screen.getByText('저장한 게시글이 없습니다.')).toBeInTheDocument();
    expect(screen.queryByText(post.content)).not.toBeInTheDocument();
    expect(screen.queryByText(sharedPostForB.content)).not.toBeInTheDocument();
    expect(screen.queryByText('저장을 취소하지 못했습니다.')).not.toBeInTheDocument();
    expect(unsaveCommunityPost).toHaveBeenCalledTimes(2);
    // A completed write must not start another private read after its session unmounts.
    expect(vi.mocked(fetchSavedCommunityPosts).mock.calls.filter((call) => call[3] === post.author.id)).toHaveLength(1);
  });
});
