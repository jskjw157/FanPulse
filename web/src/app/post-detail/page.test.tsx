import { act, fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import PostDetailPage from './page';
import { fetchMyCommentReviews, type ReviewCommentPage } from '@/lib/api/comment-review';
import { FanPulseApiError } from '@/lib/api-client';
import {
  createCommunityComment,
  deleteCommunityComment,
  deleteCommunityPost,
  fetchCommunityComments,
  fetchCommunityPost,
  fetchCommunityPostState,
  likeCommunityPost,
  saveCommunityPost,
  updateCommunityComment,
  updateCommunityPost,
  type CommunityComment,
  type CommunityCommentPage,
  type CommunityPostState,
} from '@/lib/api/community';

const auth = vi.hoisted(() => ({
  isAuthenticated: true,
  isLoading: false,
  sessionVersion: 0,
  user: null as { id: string } | null,
}));

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
  useAuth: () => auth,
}));
vi.mock('@/lib/api/comment-review', () => ({ fetchMyCommentReviews: vi.fn() }));
vi.mock('@/lib/api/community', () => ({
  createCommunityComment: vi.fn(),
  deleteCommunityComment: vi.fn(),
  deleteCommunityPost: vi.fn(),
  fetchCommunityComments: vi.fn(),
  fetchCommunityPost: vi.fn(),
  fetchCommunityPostState: vi.fn(),
  likeCommunityPost: vi.fn(),
  saveCommunityPost: vi.fn(),
  unlikeCommunityPost: vi.fn(),
  unsaveCommunityPost: vi.fn(),
  updateCommunityComment: vi.fn(),
  updateCommunityPost: vi.fn(),
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
const pending = {
  ...comment, id: '88888888-8888-8888-8888-888888888888',
  content: '나만 볼 수 있는 대기 댓글', status: 'PENDING' as const, authorName: null,
  version: 2, blockReason: null, reviewAttempts: 3, nextReviewAt: null, requiresManualReview: true,
};
const privatePage: ReviewCommentPage = { ...comments, items: [pending] };

function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason: Error) => void;
  const promise = new Promise<T>((done, fail) => { resolve = done; reject = fail; });
  return { promise, resolve, reject };
}

describe('PostDetailPage', () => {
  beforeEach(() => {
    vi.resetAllMocks();
    auth.isAuthenticated = true;
    auth.isLoading = false;
    auth.sessionVersion = 0;
    auth.user = { id: post.author.id };
    Object.defineProperty(HTMLDialogElement.prototype, 'showModal', {
      configurable: true,
      value: function (this: HTMLDialogElement) { this.setAttribute('open', ''); },
    });
    nav.id = post.id;
    auth.isAuthenticated = true;
    auth.isLoading = false;
    auth.user = { id: post.author.id };
    vi.mocked(fetchCommunityPost).mockResolvedValue(post);
    vi.mocked(fetchCommunityComments).mockResolvedValue(comments);
    vi.mocked(fetchMyCommentReviews).mockResolvedValue({ ...comments, items: [], totalElements: 0, totalPages: 0 });
    vi.mocked(fetchCommunityPostState).mockResolvedValue({ liked: false, saved: false });
    vi.mocked(likeCommunityPost).mockResolvedValue({ liked: true, saved: false });
    vi.mocked(saveCommunityPost).mockResolvedValue({ liked: true, saved: true });
    vi.mocked(createCommunityComment).mockResolvedValue(comment);
    vi.mocked(updateCommunityPost).mockResolvedValue({ ...post, content: '수정된 게시글' });
    vi.mocked(updateCommunityComment).mockResolvedValue({ ...comment, content: '수정된 댓글' });
    vi.mocked(deleteCommunityPost).mockResolvedValue(undefined);
    vi.mocked(deleteCommunityComment).mockResolvedValue(undefined);
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
    expect(fetchCommunityPostState).toHaveBeenCalledWith(post.id, expect.any(AbortSignal), post.author.id);
  });

  it('persists like and save mutations for the authenticated user', async () => {
    render(<PostDetailPage />);
    await screen.findByText('실제 상세 게시글');

    fireEvent.click(screen.getByRole('button', { name: '좋아요' }));
    await waitFor(() => expect(likeCommunityPost).toHaveBeenCalledWith(post.id, post.author.id));
    fireEvent.click(screen.getByRole('button', { name: '저장' }));
    await waitFor(() => expect(saveCommunityPost).toHaveBeenCalledWith(post.id, post.author.id));
  });

  it('isolates an in-flight A save from B state while fresh B likes and saves use B', async () => {
    const oldSave = deferred<CommunityPostState>();
    const userB = '66666666-6666-6666-6666-666666666666';
    vi.mocked(saveCommunityPost).mockImplementation((_postId, expectedUserId) =>
      expectedUserId === post.author.id ? oldSave.promise : Promise.resolve({ liked: true, saved: true }));
    const view = render(<PostDetailPage />);
    await screen.findByText(post.content);
    await waitFor(() => expect(screen.getByRole('button', { name: '저장' })).toBeEnabled());
    fireEvent.click(screen.getByRole('button', { name: '저장' }));
    expect(saveCommunityPost).toHaveBeenCalledWith(post.id, post.author.id);
    expect(screen.getByRole('button', { name: '저장' })).toBeDisabled();

    auth.user = { id: userB };
    view.rerender(<PostDetailPage />);
    await screen.findByText(post.content);
    await waitFor(() => expect(screen.getByRole('button', { name: '좋아요' })).toBeEnabled());
    expect(screen.getByRole('button', { name: '저장' })).toHaveAttribute('aria-pressed', 'false');
    fireEvent.click(screen.getByRole('button', { name: '좋아요' }));
    await waitFor(() => expect(screen.getByRole('button', { name: '좋아요' })).toHaveAttribute('aria-pressed', 'true'));
    expect(likeCommunityPost).toHaveBeenCalledExactlyOnceWith(post.id, userB);
    expect(fetchCommunityPostState).toHaveBeenCalledWith(post.id, expect.any(AbortSignal), userB);

    await act(async () => { oldSave.resolve({ liked: false, saved: true }); await oldSave.promise; });
    expect(screen.getByRole('button', { name: '좋아요' })).toHaveAttribute('aria-pressed', 'true');
    expect(screen.getByRole('button', { name: '저장' })).toHaveAttribute('aria-pressed', 'false');
    expect(screen.queryByText('게시글 저장 상태를 변경하지 못했습니다.')).not.toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '저장' }));
    await waitFor(() => expect(screen.getByRole('button', { name: '저장' })).toHaveAttribute('aria-pressed', 'true'));
    expect(saveCommunityPost).toHaveBeenCalledWith(post.id, userB);
    expect(saveCommunityPost).toHaveBeenCalledTimes(2);
  });

  it.each(['revalidation', 'session-version'] as const)('clears same-account private state across %s and fences late delete navigation', async (boundary) => {
    const deletion = deferred<void>();
    const freshPrivateRead = deferred<ReviewCommentPage>();
    const freshRow = { ...pending, id: '77777777-7777-7777-7777-777777777777', content: '재검증 후 다시 읽은 내 댓글' };
    vi.mocked(fetchMyCommentReviews).mockResolvedValue(privatePage);
    vi.mocked(fetchCommunityPostState).mockResolvedValue({ liked: true, saved: true });
    vi.mocked(deleteCommunityPost).mockReturnValue(deletion.promise);
    const view = render(<PostDetailPage />);
    await screen.findByText(pending.content);
    fireEvent.change(screen.getByLabelText('댓글 내용'), { target: { value: '이전 세션의 작성 초안' } });
    chooseAction('게시글 작업', '삭제');
    fireEvent.click(screen.getByRole('button', { name: '삭제 확인' }));
    expect(deleteCommunityPost).toHaveBeenCalledExactlyOnceWith(post.id, post.author.id);
    expect(screen.getByRole('button', { name: '삭제 중' })).toBeDisabled();

    vi.mocked(fetchMyCommentReviews).mockReturnValue(freshPrivateRead.promise);
    vi.mocked(fetchCommunityPostState).mockResolvedValue({ liked: false, saved: false });
    if (boundary === 'revalidation') {
      auth.isLoading = true;
      view.rerender(<PostDetailPage />);
      expect(screen.queryByDisplayValue('이전 세션의 작성 초안')).not.toBeInTheDocument();
      expect(screen.queryByText(pending.content)).not.toBeInTheDocument();
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
      auth.isLoading = false;
    } else auth.sessionVersion += 1;
    view.rerender(<PostDetailPage />);
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    expect(screen.queryByText(pending.content)).not.toBeInTheDocument();
    expect(await screen.findByLabelText('댓글 내용')).toHaveValue('');
    expect(screen.getByRole('button', { name: '저장' })).toHaveAttribute('aria-pressed', 'false');
    expect(screen.getByRole('button', { name: '좋아요' })).toHaveAttribute('aria-pressed', 'false');

    await act(async () => { freshPrivateRead.resolve({ ...privatePage, items: [freshRow] }); await freshPrivateRead.promise; });
    expect(await screen.findByText(freshRow.content)).toBeInTheDocument();
    fireEvent.change(screen.getByLabelText('댓글 내용'), { target: { value: '현재 세션의 작성 초안' } });
    await act(async () => { deletion.resolve(undefined); await deletion.promise; });
    expect(nav.push).not.toHaveBeenCalled();
    expect(screen.getByText(post.content)).toBeInTheDocument();
    expect(screen.getByText(freshRow.content)).toBeInTheDocument();
    expect(screen.getByLabelText('댓글 내용')).toHaveValue('현재 세션의 작성 초안');
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
    expect(screen.getByRole('heading', { name: '댓글 1' })).toBeInTheDocument();
  });

  it('creates a real comment and reloads the persisted comment list', async () => {
    vi.mocked(fetchCommunityComments)
      .mockResolvedValueOnce({ ...comments, items: [], totalElements: 0, totalPages: 0 })
      .mockResolvedValueOnce(comments);
    render(<PostDetailPage />);
    await screen.findByText('실제 상세 게시글');

    fireEvent.change(screen.getByLabelText('댓글 내용'), { target: { value: '실제 저장 댓글' } });
    fireEvent.click(screen.getByRole('button', { name: '댓글 등록' }));

    await waitFor(() => expect(createCommunityComment).toHaveBeenCalledWith(post.id, '실제 저장 댓글', null, post.author.id));
    expect(await screen.findByText('실제 저장 댓글')).toBeInTheDocument();
    expect(fetchCommunityComments).toHaveBeenCalledTimes(2);
  });

  it('shows an honest error instead of a mock post when detail loading fails', async () => {
    vi.mocked(fetchCommunityPost).mockRejectedValue(new Error('not found'));
    render(<PostDetailPage />);

    expect(await screen.findByText('게시글을 불러오지 못했습니다.')).toBeInTheDocument();
    expect(screen.queryByText('ARMY_Forever')).not.toBeInTheDocument();
  });

  function chooseAction(label: string, action: '수정' | '삭제') {
    const summary = screen.getByLabelText(label);
    fireEvent.click(summary);
    fireEvent.click(within(summary.closest('details')!).getByRole('button', { name: action }));
  }

  it.each(['other', 'logged-out', 'missing-user', 'loading'])('hides all owner actions for %s', async (mode) => {
    if (mode === 'other') auth.user = { id: '99999999-9999-9999-9999-999999999999' };
    if (mode === 'logged-out') auth.isAuthenticated = false;
    if (mode === 'missing-user') auth.user = null;
    if (mode === 'loading') auth.isLoading = true;
    render(<PostDetailPage />);
    await screen.findByText(post.content);
    expect(screen.queryByLabelText('게시글 작업')).not.toBeInTheDocument();
    expect(screen.queryByLabelText(/^댓글 작업:/)).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '수정' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '삭제' })).not.toBeInTheDocument();
  });

  it('checks comment ownership independently of post ownership', async () => {
    vi.mocked(fetchCommunityPost).mockResolvedValue({ ...post, author: { ...post.author, id: 'other' } });
    render(<PostDetailPage />);
    await screen.findByText(post.content);
    expect(screen.queryByLabelText('게시글 작업')).not.toBeInTheDocument();
    expect(screen.getByLabelText(`댓글 작업: ${comment.content}`)).toBeInTheDocument();
  });

  it('prefills post editing and cancels without a write', async () => {
    render(<PostDetailPage />);
    await screen.findByText(post.content);
    chooseAction('게시글 작업', '수정');
    expect(screen.getByLabelText('게시글 내용 수정')).toHaveValue(post.content);
    fireEvent.change(screen.getByLabelText('게시글 내용 수정'), { target: { value: '버릴 수정' } });
    fireEvent.click(screen.getByRole('button', { name: '취소' }));
    expect(screen.getByText(post.content)).toBeInTheDocument();
    expect(updateCommunityPost).not.toHaveBeenCalled();
  });

  it('renders the server post only after successful content-only editing', async () => {
    render(<PostDetailPage />);
    await screen.findByText(post.content);
    chooseAction('게시글 작업', '수정');
    fireEvent.change(screen.getByLabelText('게시글 내용 수정'), { target: { value: '  수정된 게시글  ' } });
    fireEvent.click(screen.getByRole('button', { name: '수정 저장' }));
    await waitFor(() => expect(screen.queryByLabelText('게시글 내용 수정')).not.toBeInTheDocument());
    expect(screen.getByText('수정된 게시글')).toBeInTheDocument();
    expect(updateCommunityPost).toHaveBeenCalledWith(post.id, '수정된 게시글', post.author.id);
    expect(screen.queryByLabelText('게시글 내용 수정')).not.toBeInTheDocument();
  });

  it.each(['moderation rejected', 'moderation unavailable'])('preserves previous post and draft when %s', async (reason) => {
    vi.mocked(updateCommunityPost).mockRejectedValue(new Error(reason));
    render(<PostDetailPage />);
    await screen.findByText(post.content);
    chooseAction('게시글 작업', '수정');
    fireEvent.change(screen.getByLabelText('게시글 내용 수정'), { target: { value: '거부된 수정' } });
    fireEvent.click(screen.getByRole('button', { name: '수정 저장' }));
    expect(await screen.findByRole('alert')).toHaveTextContent('수정하지 못했습니다.');
    expect(screen.getByLabelText('게시글 내용 수정')).toHaveValue('거부된 수정');
    fireEvent.click(screen.getByRole('button', { name: '취소' }));
    expect(screen.getByText(post.content)).toBeInTheDocument();
  });

  it('requires explicit post delete confirmation, supports cancel, and redirects only after success', async () => {
    render(<PostDetailPage />);
    await screen.findByText(post.content);
    chooseAction('게시글 작업', '삭제');
    expect(screen.getByRole('dialog', { name: '게시글을 삭제할까요?' })).toBeInTheDocument();
    expect(deleteCommunityPost).not.toHaveBeenCalled();
    fireEvent.click(screen.getByRole('button', { name: '취소' }));
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    chooseAction('게시글 작업', '삭제');
    fireEvent.click(screen.getByRole('button', { name: '삭제 확인' }));
    await waitFor(() => expect(nav.push).toHaveBeenCalledWith('/community'));
    expect(deleteCommunityPost).toHaveBeenCalledExactlyOnceWith(post.id, post.author.id);
  });

  it('keeps a failed deletion in the dialog without removing the post or navigating', async () => {
    vi.mocked(deleteCommunityPost).mockRejectedValue(new Error('forbidden'));
    render(<PostDetailPage />);
    await screen.findByText(post.content);
    chooseAction('게시글 작업', '삭제');
    fireEvent.click(screen.getByRole('button', { name: '삭제 확인' }));
    expect(await screen.findByRole('alert')).toHaveTextContent('삭제하지 못했습니다.');
    expect(screen.getByRole('dialog')).toBeInTheDocument();
    expect(screen.getByText(post.content)).toBeInTheDocument();
    expect(nav.push).not.toHaveBeenCalled();
  });

  it('cancels comment editing, preserves draft on error, and then uses the approved list, not the mutation response', async () => {
    vi.mocked(updateCommunityComment).mockRejectedValueOnce(new Error('network'));
    render(<PostDetailPage />);
    await screen.findByText(comment.content);
    chooseAction(`댓글 작업: ${comment.content}`, '수정');
    expect(screen.getByLabelText('댓글 내용 수정')).toHaveValue(comment.content);
    fireEvent.click(screen.getByRole('button', { name: '취소' }));
    expect(updateCommunityComment).not.toHaveBeenCalled();
    chooseAction(`댓글 작업: ${comment.content}`, '수정');
    fireEvent.change(screen.getByLabelText('댓글 내용 수정'), { target: { value: '수정 요청' } });
    fireEvent.click(screen.getByRole('button', { name: '수정 저장' }));
    expect(await screen.findByRole('alert')).toHaveTextContent('수정하지 못했습니다.');
    expect(screen.getByLabelText('댓글 내용 수정')).toHaveValue('수정 요청');
    vi.mocked(fetchCommunityComments).mockResolvedValue({ ...comments, items: [{ ...comment, content: '서버 공개 댓글' }] });
    fireEvent.click(screen.getByRole('button', { name: '수정 저장' }));
    expect(await screen.findByText('서버 공개 댓글')).toBeInTheDocument();
    expect(screen.queryByText('수정된 댓글')).not.toBeInTheDocument();
    expect(fetchCommunityComments).toHaveBeenLastCalledWith(post.id, 0, 20, expect.any(AbortSignal));
    expect(updateCommunityComment).toHaveBeenLastCalledWith(post.id, comment.id, '수정 요청', post.author.id);
  });

  it.each(['PENDING', 'BLOCKED'] as const)('removes %s edited comments and reloads the authoritative count', async (status) => {
    vi.mocked(updateCommunityComment).mockResolvedValue({ ...comment, status, content: '비공개 수정' });
    vi.mocked(fetchMyCommentReviews).mockResolvedValueOnce({ ...privatePage, items: [], totalElements: 0, totalPages: 0 })
      .mockResolvedValue({ ...privatePage, items: [{ ...pending, id: comment.id, status, content: '비공개 수정' }] });
    render(<PostDetailPage />);
    await screen.findByText(comment.content);
    vi.mocked(fetchCommunityComments).mockResolvedValue({ ...comments, items: [], totalElements: 0, totalPages: 0 });
    chooseAction(`댓글 작업: ${comment.content}`, '수정');
    fireEvent.change(screen.getByLabelText('댓글 내용 수정'), { target: { value: '비공개 수정' } });
    fireEvent.click(screen.getByRole('button', { name: '수정 저장' }));
    expect(await screen.findByRole('heading', { name: '댓글 0' })).toBeInTheDocument();
    expect(await screen.findByText('비공개 수정')).toBeInTheDocument();
    await waitFor(() => expect(screen.getByRole('button', { name: '상태 새로고침' })).toBeEnabled());
    expect(screen.getByRole('status')).toHaveTextContent('공개 목록에서 제외되었습니다.');
    expect(screen.queryByText(comment.content)).not.toBeInTheDocument();
    const panel = within(screen.getByRole('region', { name: '내 댓글 검토 상태' }));
    expect(panel.getByText('비공개 수정')).toBeInTheDocument();
    expect(panel.getByLabelText('댓글 작업: 비공개 수정')).toBeInTheDocument();
    expect(screen.getAllByText('비공개 수정')).toHaveLength(1);
    expect(fetchMyCommentReviews).toHaveBeenCalledTimes(2);
    expect(fetchCommunityComments).toHaveBeenCalledTimes(2);
  });

  it('deletes a parent only on confirmation, reloads counts, and preserves approved orphan replies', async () => {
    const reply = { ...comment, id: '55555555-5555-5555-5555-555555555555', parentCommentId: comment.id, content: '남아 있는 답글', userId: 'other' };
    vi.mocked(fetchCommunityComments).mockResolvedValueOnce({ ...comments, items: [comment, reply], totalElements: 2 })
      .mockResolvedValue({ ...comments, items: [reply] });
    render(<PostDetailPage />);
    await screen.findByText(comment.content);
    chooseAction(`댓글 작업: ${comment.content}`, '삭제');
    fireEvent.click(screen.getByRole('button', { name: '취소' }));
    expect(deleteCommunityComment).not.toHaveBeenCalled();
    chooseAction(`댓글 작업: ${comment.content}`, '삭제');
    fireEvent.click(screen.getByRole('button', { name: '삭제 확인' }));
    await waitFor(() => expect(screen.queryByText(comment.content)).not.toBeInTheDocument());
    expect(screen.getByText(reply.content)).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: '댓글 1' })).toBeInTheDocument();
    expect(deleteCommunityComment).toHaveBeenCalledExactlyOnceWith(post.id, comment.id, post.author.id);
  });

  it('retains comments and count on delete failure', async () => {
    vi.mocked(deleteCommunityComment).mockRejectedValue(new Error('network'));
    render(<PostDetailPage />);
    await screen.findByText(comment.content);
    chooseAction(`댓글 작업: ${comment.content}`, '삭제');
    fireEvent.click(screen.getByRole('button', { name: '삭제 확인' }));
    expect(await screen.findByRole('alert')).toHaveTextContent('삭제하지 못했습니다.');
    expect(screen.getByText(comment.content)).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: '댓글 1' })).toBeInTheDocument();
    expect(fetchCommunityComments).toHaveBeenCalledTimes(1);
  });

  it('hides stale comments on reload failure and retries reads without resubmitting deletion', async () => {
    render(<PostDetailPage />);
    await screen.findByText(comment.content);
    vi.mocked(fetchCommunityComments).mockRejectedValueOnce(new Error('reload failed'));
    chooseAction(`댓글 작업: ${comment.content}`, '삭제');
    fireEvent.click(screen.getByRole('button', { name: '삭제 확인' }));
    expect(await screen.findByText(/댓글 변경은 저장되었지만 목록을 불러오지 못했습니다/)).toBeInTheDocument();
    expect(screen.queryByText(comment.content)).not.toBeInTheDocument();
    vi.mocked(fetchCommunityComments).mockResolvedValue({ ...comments, items: [], totalElements: 0, totalPages: 0 });
    fireEvent.click(screen.getByRole('button', { name: '다시 시도' }));
    expect(await screen.findByRole('heading', { name: '댓글 0' })).toBeInTheDocument();
    expect(deleteCommunityComment).toHaveBeenCalledTimes(1);
  });

  it('recovers private pending and blocked states on a fresh mount without browser persistence', async () => {
    const blocked = { ...pending, id: '55555555-5555-5555-5555-555555555555', content: '차단된 내 댓글', status: 'BLOCKED' as const, blockReason: '게시 기준 위반' };
    vi.mocked(fetchMyCommentReviews).mockResolvedValue({ ...privatePage, items: [pending, blocked], totalElements: 2 });
    const storageWrite = vi.spyOn(Storage.prototype, 'setItem');
    const first = render(<PostDetailPage />);
    expect(await screen.findByText(pending.content)).toBeInTheDocument();
    const panel = within(screen.getByRole('region', { name: '내 댓글 검토 상태' }));
    expect(panel.getByText('검토 대기')).toBeInTheDocument();
    expect(panel.getByText('차단됨')).toBeInTheDocument();
    expect(panel.getByText('차단 사유: 게시 기준 위반')).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: '댓글 1' })).toBeInTheDocument();
    expect(fetchCommunityComments).toHaveBeenCalledTimes(1);
    first.unmount();
    render(<PostDetailPage />);
    expect(await screen.findByText(pending.content)).toBeInTheDocument();
    expect(fetchMyCommentReviews).toHaveBeenCalledTimes(2);
    expect(fetchMyCommentReviews).toHaveBeenLastCalledWith(post.id, 0, 20, expect.any(AbortSignal), post.author.id);
    expect(storageWrite).not.toHaveBeenCalled();
    storageWrite.mockRestore();
  });

  it('refreshes private status and approved public comments without duplicating the private content', async () => {
    vi.mocked(fetchMyCommentReviews).mockResolvedValueOnce(privatePage)
      .mockResolvedValueOnce({ ...privatePage, items: [{ ...pending, status: 'APPROVED' }] });
    const approved = { ...comment, id: pending.id, content: pending.content };
    vi.mocked(fetchCommunityComments).mockResolvedValueOnce({ ...comments, items: [], totalElements: 0, totalPages: 0 })
      .mockResolvedValueOnce({ ...comments, items: [approved] });
    render(<PostDetailPage />);
    await screen.findByText(pending.content);
    fireEvent.click(screen.getByRole('button', { name: '상태 새로고침' }));
    await waitFor(() => expect(fetchMyCommentReviews).toHaveBeenCalledTimes(2));
    await waitFor(() => expect(screen.queryByText('검토 대기', { exact: true })).not.toBeInTheDocument());
    expect(await screen.findByText(pending.content)).toBeInTheDocument();
    expect(within(screen.getByRole('region', { name: '내 댓글 검토 상태' })).queryByText(pending.content)).not.toBeInTheDocument();
    expect(screen.getByRole('heading', { name: '댓글 1' })).toBeInTheDocument();
  });

  it('loads persisted pending status after creation instead of adding it to the public list', async () => {
    vi.mocked(createCommunityComment).mockResolvedValue(pending);
    vi.mocked(fetchMyCommentReviews).mockResolvedValueOnce({ ...privatePage, items: [], totalElements: 0, totalPages: 0 })
      .mockResolvedValueOnce(privatePage);
    render(<PostDetailPage />);
    await screen.findByText('실제 상세 게시글');
    fireEvent.change(screen.getByLabelText('댓글 내용'), { target: { value: pending.content } });
    fireEvent.click(screen.getByRole('button', { name: '댓글 등록' }));
    expect(await screen.findByText('댓글이 검토 대기 상태로 저장되었습니다.')).toBeInTheDocument();
    expect(await screen.findByText(pending.content)).toBeInTheDocument();
    expect(fetchCommunityComments).toHaveBeenCalledTimes(1);
  });

  describe.each(['PENDING', 'BLOCKED'] as const)('own rendered %s rows', (status) => {
    const row = { ...pending, status, blockReason: status === 'BLOCKED' ? '게시 기준 위반' : null };

    it.each(['PENDING', 'BLOCKED', 'APPROVED'] as const)('edits with the existing UX and reloads authoritative %s state', async (nextStatus) => {
      const saved = { ...row, status: nextStatus, content: '서버에서 다시 읽은 댓글' };
      vi.mocked(fetchMyCommentReviews).mockResolvedValueOnce({ ...privatePage, items: [row] })
        .mockResolvedValue({ ...privatePage, items: [saved] });
      vi.mocked(updateCommunityComment).mockResolvedValue({ ...saved, content: '공개하면 안 되는 변경 응답' });
      render(<PostDetailPage />);
      await screen.findByText(row.content);
      fireEvent.change(screen.getByLabelText('댓글 내용'), { target: { value: '새 댓글 작성 초안' } });
      chooseAction(`댓글 작업: ${row.content}`, '수정');
      expect(screen.getByLabelText('댓글 내용 수정')).toHaveValue(row.content);
      expect(screen.getByLabelText('댓글 내용 수정')).toHaveAttribute('maxlength', '1000');
      expect(screen.getByRole('button', { name: '댓글 등록' })).toBeDisabled();
      expect(screen.getByRole('button', { name: '상태 새로고침' })).toBeDisabled();
      fireEvent.click(screen.getByRole('button', { name: '취소' }));
      expect(updateCommunityComment).not.toHaveBeenCalled();
      expect(screen.getByText(row.content)).toBeInTheDocument();
      chooseAction(`댓글 작업: ${row.content}`, '수정');
      fireEvent.change(screen.getByLabelText('댓글 내용 수정'), { target: { value: '   ' } });
      expect(screen.getByRole('button', { name: '수정 저장' })).toBeDisabled();
      fireEvent.change(screen.getByLabelText('댓글 내용 수정'), { target: { value: '  수정 요청  ' } });
      if (nextStatus === 'APPROVED') {
        vi.mocked(fetchCommunityComments).mockResolvedValue({ ...comments, items: [comment, saved], totalElements: 2 });
      }
      fireEvent.click(screen.getByRole('button', { name: '수정 저장' }));
      await waitFor(() => expect(screen.queryByLabelText('댓글 내용 수정')).not.toBeInTheDocument());
      expect(updateCommunityComment).toHaveBeenCalledExactlyOnceWith(post.id, row.id, '수정 요청', post.author.id);
      expect(fetchMyCommentReviews).toHaveBeenLastCalledWith(post.id, 0, 20, expect.any(AbortSignal), post.author.id);
      expect(fetchMyCommentReviews).toHaveBeenCalledTimes(2);
      expect(fetchCommunityComments).toHaveBeenCalledTimes(2);
      expect(await screen.findByText(saved.content)).toBeInTheDocument();
      const panel = screen.getByRole('region', { name: '내 댓글 검토 상태' });
      expect(panel.contains(screen.getByText(saved.content))).toBe(nextStatus !== 'APPROVED');
      expect(screen.getAllByText(saved.content)).toHaveLength(1);
      expect(screen.queryByText('공개하면 안 되는 변경 응답')).not.toBeInTheDocument();
      expect(screen.queryByText('수정 요청')).not.toBeInTheDocument();
      expect(screen.getByRole('heading', { name: `댓글 ${nextStatus === 'APPROVED' ? 2 : 1}` })).toBeInTheDocument();
      expect(screen.getByText(comment.content)).toBeInTheDocument();
      expect(screen.getByLabelText('댓글 내용')).toHaveValue('새 댓글 작성 초안');
    });

    it('keeps the edit draft and existing counts on failure without reloading or publishing it', async () => {
      vi.mocked(fetchMyCommentReviews).mockResolvedValue({ ...privatePage, items: [row] });
      vi.mocked(updateCommunityComment).mockRejectedValue(new Error('network'));
      render(<PostDetailPage />);
      await screen.findByText(row.content);
      chooseAction(`댓글 작업: ${row.content}`, '수정');
      fireEvent.change(screen.getByLabelText('댓글 내용 수정'), { target: { value: '저장 실패 초안' } });
      fireEvent.click(screen.getByRole('button', { name: '수정 저장' }));
      expect(await screen.findByRole('alert')).toHaveTextContent('수정하지 못했습니다.');
      expect(screen.getByLabelText('댓글 내용 수정')).toHaveValue('저장 실패 초안');
      expect(screen.getByRole('heading', { name: '댓글 1' })).toBeInTheDocument();
      expect(screen.getByText(comment.content)).toBeInTheDocument();
      expect(fetchMyCommentReviews).toHaveBeenCalledTimes(1);
      expect(fetchCommunityComments).toHaveBeenCalledTimes(1);
      fireEvent.click(screen.getByRole('button', { name: '취소' }));
      expect(screen.getByText(row.content)).toBeInTheDocument();
      expect(screen.queryByText('저장 실패 초안')).not.toBeInTheDocument();
    });

    it('cancels deletion without a write, prevents duplicate mutations while busy, and reloads both lists after confirmation', async () => {
      const deletion = deferred<void>();
      vi.mocked(deleteCommunityComment).mockReturnValue(deletion.promise);
      vi.mocked(fetchMyCommentReviews).mockResolvedValueOnce({ ...privatePage, items: [row] })
        .mockResolvedValue({ ...privatePage, items: [], totalElements: 0, totalPages: 0 });
      render(<PostDetailPage />);
      await screen.findByText(row.content);
      chooseAction(`댓글 작업: ${row.content}`, '삭제');
      expect(screen.getByRole('dialog', { name: '댓글을 삭제할까요?' })).toHaveTextContent('작성된 답글은 유지됩니다.');
      expect(deleteCommunityComment).not.toHaveBeenCalled();
      fireEvent.click(screen.getByRole('button', { name: '취소' }));
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
      chooseAction(`댓글 작업: ${row.content}`, '삭제');
      fireEvent.click(screen.getByRole('button', { name: '삭제 확인' }));
      expect(screen.getByRole('button', { name: '삭제 중' })).toBeDisabled();
      fireEvent.click(screen.getByRole('button', { name: '삭제 중' }));
      expect(screen.getByRole('button', { name: '취소' })).toBeDisabled();
      expect(screen.getByRole('button', { name: '상태 새로고침' })).toBeDisabled();
      expect(screen.getByRole('button', { name: '댓글 등록' })).toBeDisabled();
      const actions = screen.getByLabelText(`댓글 작업: ${row.content}`).closest('details')!;
      fireEvent.click(screen.getByLabelText(`댓글 작업: ${row.content}`));
      expect(within(actions).getByRole('button', { name: '수정' })).toBeDisabled();
      fireEvent.click(within(actions).getByRole('button', { name: '수정' }));
      expect(updateCommunityComment).not.toHaveBeenCalled();
      expect(screen.getByText(row.content)).toBeInTheDocument();
      expect(fetchMyCommentReviews).toHaveBeenCalledTimes(1);
      await act(async () => { deletion.resolve(undefined); await deletion.promise; });
      await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument());
      expect(screen.queryByText(row.content)).not.toBeInTheDocument();
      expect(screen.getByText(comment.content)).toBeInTheDocument();
      expect(screen.getByRole('heading', { name: '댓글 1' })).toBeInTheDocument();
      expect(deleteCommunityComment).toHaveBeenCalledExactlyOnceWith(post.id, row.id, post.author.id);
      expect(fetchMyCommentReviews).toHaveBeenCalledTimes(2);
      expect(fetchCommunityComments).toHaveBeenCalledTimes(2);
      expect(nav.push).not.toHaveBeenCalled();
    });

    it('keeps a failed deletion in the confirmation with the private row and public count intact', async () => {
      vi.mocked(fetchMyCommentReviews).mockResolvedValue({ ...privatePage, items: [row] });
      vi.mocked(deleteCommunityComment).mockRejectedValue(new Error('forbidden'));
      render(<PostDetailPage />);
      await screen.findByText(row.content);
      chooseAction(`댓글 작업: ${row.content}`, '삭제');
      fireEvent.click(screen.getByRole('button', { name: '삭제 확인' }));
      expect(await screen.findByRole('alert')).toHaveTextContent('삭제하지 못했습니다.');
      expect(screen.getByRole('dialog')).toBeInTheDocument();
      expect(screen.getByText(row.content)).toBeInTheDocument();
      expect(screen.getByText(comment.content)).toBeInTheDocument();
      expect(screen.getByRole('heading', { name: '댓글 1' })).toBeInTheDocument();
      expect(fetchMyCommentReviews).toHaveBeenCalledTimes(1);
      expect(fetchCommunityComments).toHaveBeenCalledTimes(1);
    });
  });

  it('retries a failed private reload after successful private editing without resubmitting the edit', async () => {
    vi.mocked(fetchMyCommentReviews).mockResolvedValueOnce(privatePage)
      .mockRejectedValueOnce(new Error('reload failed'))
      .mockResolvedValue({ ...privatePage, items: [{ ...pending, content: '다시 읽은 수정 댓글' }] });
    vi.mocked(updateCommunityComment).mockResolvedValue({ ...pending, content: '비공개 변경 응답' });
    render(<PostDetailPage />);
    await screen.findByText(pending.content);
    chooseAction(`댓글 작업: ${pending.content}`, '수정');
    fireEvent.change(screen.getByLabelText('댓글 내용 수정'), { target: { value: '수정 요청' } });
    fireEvent.click(screen.getByRole('button', { name: '수정 저장' }));
    expect(await screen.findByRole('alert')).toHaveTextContent('내 댓글 검토 상태를 불러오지 못했습니다.');
    expect(screen.queryByText(pending.content)).not.toBeInTheDocument();
    expect(screen.queryByText('비공개 변경 응답')).not.toBeInTheDocument();
    expect(screen.getByText(comment.content)).toBeInTheDocument();
    await waitFor(() => expect(screen.getByRole('button', { name: '상태 새로고침' })).toBeEnabled());
    fireEvent.click(screen.getByRole('button', { name: '상태 새로고침' }));
    expect(await screen.findByText('다시 읽은 수정 댓글')).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: '댓글 1' })).toBeInTheDocument();
    expect(updateCommunityComment).toHaveBeenCalledTimes(1);
  });

  it('retries reads rather than a successful private deletion when the public reload fails', async () => {
    vi.mocked(fetchMyCommentReviews).mockResolvedValueOnce(privatePage)
      .mockResolvedValue({ ...privatePage, items: [], totalElements: 0, totalPages: 0 });
    render(<PostDetailPage />);
    await screen.findByText(pending.content);
    vi.mocked(fetchCommunityComments).mockRejectedValueOnce(new Error('reload failed'));
    chooseAction(`댓글 작업: ${pending.content}`, '삭제');
    fireEvent.click(screen.getByRole('button', { name: '삭제 확인' }));
    expect(await screen.findByText(/댓글 변경은 저장되었지만 목록을 불러오지 못했습니다/)).toBeInTheDocument();
    expect(screen.queryByText(pending.content)).not.toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '다시 시도' }));
    expect(await screen.findByText(comment.content)).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: '댓글 1' })).toBeInTheDocument();
    expect(screen.queryByText(pending.content)).not.toBeInTheDocument();
    expect(deleteCommunityComment).toHaveBeenCalledExactlyOnceWith(post.id, pending.id, post.author.id);
  });

  it('returns private pagination to page zero after deleting its last row without clearing the compose draft', async () => {
    const next = { ...pending, content: '마지막 페이지의 비공개 댓글' };
    vi.mocked(fetchMyCommentReviews).mockResolvedValueOnce({ ...privatePage, totalElements: 21, totalPages: 2 })
      .mockResolvedValueOnce({ ...privatePage, items: [next], page: 1, totalElements: 21, totalPages: 2 })
      .mockResolvedValue({ ...privatePage, totalElements: 20 });
    render(<PostDetailPage />);
    await screen.findByText(pending.content);
    fireEvent.change(screen.getByLabelText('댓글 내용'), { target: { value: '유지할 작성 초안' } });
    fireEvent.click(screen.getByRole('button', { name: '내 댓글 다음' }));
    await screen.findByText(next.content);
    chooseAction(`댓글 작업: ${next.content}`, '삭제');
    fireEvent.click(screen.getByRole('button', { name: '삭제 확인' }));
    expect(await screen.findByText(pending.content)).toBeInTheDocument();
    expect(fetchMyCommentReviews).toHaveBeenLastCalledWith(post.id, 0, 20, expect.any(AbortSignal), post.author.id);
    expect(screen.queryByText(next.content)).not.toBeInTheDocument();
    expect(screen.getByLabelText('댓글 내용')).toHaveValue('유지할 작성 초안');
    expect(screen.getByRole('heading', { name: '댓글 1' })).toBeInTheDocument();
  });

  it('does not let an older status-refresh public read overwrite the newer private-mutation count', async () => {
    const oldRead = deferred<CommunityCommentPage>();
    vi.mocked(fetchMyCommentReviews).mockResolvedValue(privatePage);
    vi.mocked(fetchCommunityComments).mockResolvedValueOnce(comments).mockReturnValueOnce(oldRead.promise)
      .mockResolvedValue({ ...comments, items: [], totalElements: 0, totalPages: 0 });
    render(<PostDetailPage />);
    await screen.findByText(pending.content);
    fireEvent.click(screen.getByRole('button', { name: '상태 새로고침' }));
    await waitFor(() => expect(fetchMyCommentReviews).toHaveBeenCalledTimes(2));
    await screen.findByText(pending.content);
    vi.mocked(fetchMyCommentReviews).mockResolvedValue({ ...privatePage, items: [], totalElements: 0, totalPages: 0 });
    chooseAction(`댓글 작업: ${pending.content}`, '삭제');
    fireEvent.click(screen.getByRole('button', { name: '삭제 확인' }));
    expect(await screen.findByRole('heading', { name: '댓글 0' })).toBeInTheDocument();
    await act(async () => { oldRead.resolve(comments); await oldRead.promise; });
    expect(screen.getByRole('heading', { name: '댓글 0' })).toBeInTheDocument();
    expect(screen.queryByText(comment.content)).not.toBeInTheDocument();
    expect(screen.queryByText(pending.content)).not.toBeInTheDocument();
    expect(deleteCommunityComment).toHaveBeenCalledTimes(1);
    expect(fetchCommunityComments).toHaveBeenCalledTimes(3);
  });

  describe.each(['edit', 'delete'] as const)('stale private %s', (action) => {
    it.each([
      ['account', 'success'], ['account', 'failure'],
      ['logout', 'success'], ['logout', 'failure'],
      ['auth-loading', 'success'], ['auth-loading', 'failure'],
      ['post', 'success'], ['post', 'failure'],
    ] as const)('ignores %s-boundary %s responses and clears old drafts/dialogs', async (boundary, outcome) => {
      const mutation = deferred<CommunityComment>();
      if (action === 'edit') vi.mocked(updateCommunityComment).mockReturnValue(mutation.promise);
      else vi.mocked(deleteCommunityComment).mockImplementation(() => mutation.promise.then(() => undefined));
      vi.mocked(fetchMyCommentReviews).mockResolvedValue(privatePage);
      const view = render(<PostDetailPage />);
      await screen.findByText(pending.content);
      chooseAction(`댓글 작업: ${pending.content}`, action === 'edit' ? '수정' : '삭제');
      if (action === 'edit') {
        fireEvent.change(screen.getByLabelText('댓글 내용 수정'), { target: { value: '이전 계정의 수정 초안' } });
        fireEvent.click(screen.getByRole('button', { name: '수정 저장' }));
      } else fireEvent.click(screen.getByRole('button', { name: '삭제 확인' }));

      const nextRow = { ...pending, id: '77777777-7777-7777-7777-777777777777', content: '현재 계정 게시글의 비공개 댓글' };
      if (boundary === 'account') {
        nextRow.userId = '66666666-6666-6666-6666-666666666666';
        auth.user = { id: nextRow.userId };
      } else if (boundary === 'logout') {
        auth.isAuthenticated = false;
        auth.user = null;
      } else if (boundary === 'auth-loading') {
        auth.isLoading = true;
      } else {
        nav.id = '99999999-9999-9999-9999-999999999999';
        nextRow.postId = nav.id;
        vi.mocked(fetchCommunityPost).mockResolvedValue({ ...post, id: nav.id });
        vi.mocked(fetchCommunityComments).mockResolvedValue({ ...comments, items: [{ ...comment, postId: nav.id }] });
      }
      vi.mocked(fetchMyCommentReviews).mockResolvedValue({ ...privatePage, items: [nextRow] });
      view.rerender(<PostDetailPage />);
      expect(screen.queryByLabelText('댓글 내용 수정')).not.toBeInTheDocument();
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
      expect(screen.queryByText(pending.content)).not.toBeInTheDocument();
      if (boundary === 'account' || boundary === 'post') {
        await screen.findByText(nextRow.content);
        chooseAction(`댓글 작업: ${nextRow.content}`, '수정');
        fireEvent.change(screen.getByLabelText('댓글 내용 수정'), { target: { value: '현재 수정 초안' } });
      } else expect(screen.queryByRole('region', { name: '내 댓글 검토 상태' })).not.toBeInTheDocument();
      const privateReads = vi.mocked(fetchMyCommentReviews).mock.calls.length;
      const publicReads = vi.mocked(fetchCommunityComments).mock.calls.length;
      await act(async () => {
        if (outcome === 'success') mutation.resolve({ ...pending, content: '늦은 비공개 변경 응답' });
        else mutation.reject(new Error('late failure'));
        await mutation.promise.catch(() => undefined);
      });
      expect(fetchMyCommentReviews).toHaveBeenCalledTimes(privateReads);
      expect(fetchCommunityComments).toHaveBeenCalledTimes(publicReads);
      expect(screen.queryByRole('alert')).not.toBeInTheDocument();
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
      expect(screen.queryByText(/댓글을 삭제했습니다|수정한 댓글이/)).not.toBeInTheDocument();
      expect(screen.queryByText('늦은 비공개 변경 응답')).not.toBeInTheDocument();
      if (boundary === 'account' || boundary === 'post') {
        expect(screen.getByLabelText('댓글 내용 수정')).toHaveValue('현재 수정 초안');
        expect(screen.getByRole('button', { name: '수정 저장' })).toBeEnabled();
      }
      expect(nav.push).not.toHaveBeenCalled();
    });
  });

  it.each(['success', 'failure'] as const)('ignores a stale %s public reload after a private edit and account change', async (outcome) => {
    const reload = deferred<CommunityCommentPage>();
    vi.mocked(fetchMyCommentReviews).mockResolvedValue(privatePage);
    vi.mocked(fetchCommunityComments).mockResolvedValueOnce(comments).mockReturnValueOnce(reload.promise);
    vi.mocked(updateCommunityComment).mockResolvedValue(pending);
    const view = render(<PostDetailPage />);
    await screen.findByText(pending.content);
    chooseAction(`댓글 작업: ${pending.content}`, '수정');
    fireEvent.click(screen.getByRole('button', { name: '수정 저장' }));
    await waitFor(() => expect(fetchCommunityComments).toHaveBeenCalledTimes(2));
    const signal = vi.mocked(fetchCommunityComments).mock.calls[1][3];
    const other = { ...pending, content: '새 계정의 댓글', userId: '66666666-6666-6666-6666-666666666666' };
    vi.mocked(fetchMyCommentReviews).mockResolvedValue({ ...privatePage, items: [other] });
    auth.user = { id: other.userId };
    view.rerender(<PostDetailPage />);
    await screen.findByText(other.content);
    expect(signal?.aborted).toBe(true);
    await act(async () => {
      if (outcome === 'success') reload.resolve({ ...comments, items: [], totalElements: 0, totalPages: 0 });
      else reload.reject(new Error('stale reload failure'));
      await reload.promise.catch(() => undefined);
    });
    expect(screen.getByText(comment.content)).toBeInTheDocument();
    expect(screen.getByText(other.content)).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: '댓글 1' })).toBeInTheDocument();
    expect(screen.queryByText(/댓글 변경은 저장되었지만/)).not.toBeInTheDocument();
    expect(screen.queryByText(/수정한 댓글이/)).not.toBeInTheDocument();
  });

  it('clears private rows and drafts on account change/logout and ignores an old refresh result', async () => {
    let finish!: (value: ReviewCommentPage) => void;
    const late = new Promise<ReviewCommentPage>((resolve) => { finish = resolve; });
    const other = { ...pending, content: '새 계정의 비공개 댓글', userId: '66666666-6666-6666-6666-666666666666' };
    vi.mocked(fetchMyCommentReviews).mockResolvedValueOnce(privatePage).mockReturnValueOnce(late)
      .mockResolvedValueOnce({ ...privatePage, items: [other] });
    const view = render(<PostDetailPage />);
    await screen.findByText(pending.content);
    fireEvent.change(screen.getByLabelText('댓글 내용'), { target: { value: '이전 계정의 초안' } });
    fireEvent.click(screen.getByRole('button', { name: '상태 새로고침' }));
    await waitFor(() => expect(fetchMyCommentReviews).toHaveBeenCalledTimes(2));
    const oldSignal = vi.mocked(fetchMyCommentReviews).mock.calls[1][3];
    auth.user = { id: other.userId };
    view.rerender(<PostDetailPage />);
    expect(screen.queryByDisplayValue('이전 계정의 초안')).not.toBeInTheDocument();
    expect(screen.queryByText(pending.content)).not.toBeInTheDocument();
    expect(oldSignal?.aborted).toBe(true);
    await screen.findByText(other.content);
    expect(screen.getByLabelText('댓글 내용')).toHaveValue('');
    await act(async () => { finish(privatePage); await late; });
    expect(screen.queryByText(pending.content)).not.toBeInTheDocument();
    auth.isAuthenticated = false;
    auth.user = null;
    view.rerender(<PostDetailPage />);
    expect(screen.queryByRole('region', { name: '내 댓글 검토 상태' })).not.toBeInTheDocument();
    expect(screen.queryByText(other.content)).not.toBeInTheDocument();
    expect(fetchMyCommentReviews).toHaveBeenCalledTimes(3);
  });

  it('ignores a late create response after logout', async () => {
    let finish!: (value: typeof pending) => void;
    const late = new Promise<typeof pending>((resolve) => { finish = resolve; });
    vi.mocked(createCommunityComment).mockReturnValue(late);
    const view = render(<PostDetailPage />);
    await screen.findByText('실제 상세 게시글');
    fireEvent.change(screen.getByLabelText('댓글 내용'), { target: { value: pending.content } });
    fireEvent.click(screen.getByRole('button', { name: '댓글 등록' }));
    auth.isAuthenticated = false;
    auth.user = null;
    view.rerender(<PostDetailPage />);
    await screen.findByText(post.content);
    await act(async () => { finish(pending); await late; });
    expect(screen.queryByText('댓글이 검토 대기 상태로 저장되었습니다.')).not.toBeInTheDocument();
    expect(fetchMyCommentReviews).toHaveBeenCalledTimes(1);
    expect(fetchCommunityComments).toHaveBeenCalledTimes(2);
  });

  it('paginates own comments separately from public comments', async () => {
    vi.mocked(fetchMyCommentReviews).mockResolvedValueOnce({ ...privatePage, totalElements: 21, totalPages: 2 })
      .mockResolvedValueOnce({ ...privatePage, page: 1, totalElements: 21, totalPages: 2, items: [{ ...pending, content: '다음 페이지의 내 댓글' }] });
    render(<PostDetailPage />);
    await screen.findByText(pending.content);
    fireEvent.click(screen.getByRole('button', { name: '내 댓글 다음' }));
    expect(await screen.findByText('다음 페이지의 내 댓글')).toBeInTheDocument();
    expect(fetchMyCommentReviews).toHaveBeenLastCalledWith(post.id, 1, 20, expect.any(AbortSignal), post.author.id);
    expect(fetchCommunityComments).toHaveBeenCalledTimes(1);
  });

  it('shows a private retrieval error and retries without hiding the public thread', async () => {
    vi.mocked(fetchMyCommentReviews).mockRejectedValueOnce(new Error('network')).mockResolvedValueOnce(privatePage);
    render(<PostDetailPage />);
    expect(await screen.findByRole('alert')).toHaveTextContent('내 댓글 검토 상태를 불러오지 못했습니다.');
    expect(screen.getByText(comment.content)).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '상태 새로고침' }));
    expect(await screen.findByText(pending.content)).toBeInTheDocument();
  });

  it('shows session-expiry feedback for private retrieval', async () => {
    vi.mocked(fetchMyCommentReviews).mockRejectedValue(new FanPulseApiError('expired', { status: 401 }));
    render(<PostDetailPage />);
    expect(await screen.findByRole('alert')).toHaveTextContent('로그인이 만료되었습니다.');
  });

  it('rejects private rows for a different account rather than displaying them', async () => {
    vi.mocked(fetchMyCommentReviews).mockResolvedValue({ ...privatePage, items: [{ ...pending, userId: '77777777-7777-7777-7777-777777777777' }] });
    render(<PostDetailPage />);
    expect(await screen.findByRole('alert')).toHaveTextContent('내 댓글 검토 상태를 불러오지 못했습니다.');
    expect(screen.queryByText(pending.content)).not.toBeInTheDocument();
  });

  it('never retrieves private status anonymously and sends comment writers to login', async () => {
    auth.isAuthenticated = false;
    auth.user = null;
    render(<PostDetailPage />);
    await screen.findByText('실제 상세 게시글');
    fireEvent.change(screen.getByLabelText('댓글 내용'), { target: { value: 'login first' } });
    fireEvent.click(screen.getByRole('button', { name: '댓글 등록' }));
    expect(nav.push).toHaveBeenCalledWith(`/login?redirect=${encodeURIComponent(`/post-detail?id=${post.id}`)}`);
    expect(fetchMyCommentReviews).not.toHaveBeenCalled();
    expect(createCommunityComment).not.toHaveBeenCalled();
  });

  it('explicitly explains a target deleted between notification read and detail navigation', async () => {
    vi.mocked(fetchCommunityPost).mockRejectedValue(new FanPulseApiError('not found', { status: 404 }));
    render(<PostDetailPage />);
    expect(await screen.findByText('삭제되었거나 사용할 수 없는 게시글입니다.')).toBeInTheDocument();
    expect(screen.queryByText('실제 상세 게시글')).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: '다시 시도' })).toBeInTheDocument();
  });
});
