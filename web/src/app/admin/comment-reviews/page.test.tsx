import { act, fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { FanPulseApiError } from '@/lib/api-client';
import {
  decideCommentReview, fetchCommentReviewHistory, fetchPendingCommentReviews,
  type CommentReviewHistory, type ReviewComment, type ReviewCommentPage,
} from '@/lib/api/comment-review';
import CommentReviewsPage from './page';

const auth = vi.hoisted(() => ({
  isAuthenticated: true, isLoading: false, user: null as { id: string } | null,
}));
vi.mock('@/contexts/AuthContext', () => ({ useAuth: () => auth }));
vi.mock('next/navigation', () => ({ useRouter: () => ({ back: vi.fn() }) }));
vi.mock('@/lib/api/comment-review', () => ({
  decideCommentReview: vi.fn(), fetchCommentReviewHistory: vi.fn(), fetchPendingCommentReviews: vi.fn(),
}));

const comment: ReviewComment = {
  id: '44444444-4444-4444-4444-444444444444', postId: '11111111-1111-1111-1111-111111111111',
  userId: '22222222-2222-2222-2222-222222222222', content: '운영자만 보는 검토 댓글',
  status: 'PENDING', parentCommentId: null, createdAt: '2026-11-06T08:00:00Z', authorName: null,
  version: 3, blockReason: null, reviewAttempts: 2, nextReviewAt: null, requiresManualReview: true,
};
const page: ReviewCommentPage = { items: [comment], page: 0, size: 20, totalElements: 1, totalPages: 1 };
const audit: CommentReviewHistory = {
  id: '55555555-5555-5555-5555-555555555555', commentId: comment.id, actorId: null,
  action: 'RETRY', fromStatus: 'PENDING', toStatus: 'PENDING', reason: '자동 검토 재시도',
  commentVersion: 3, attempt: 2, createdAt: '2026-11-06T08:01:00Z',
};

async function selectComment() {
  const button = await screen.findByRole('button', { name: /운영자만 보는 검토 댓글/ });
  await act(async () => { fireEvent.click(button); });
  return within(screen.getByRole('region', { name: '선택한 댓글 검토' }));
}

describe('operator comment reviews', () => {
  beforeEach(() => {
    vi.resetAllMocks();
    auth.isAuthenticated = true;
    auth.isLoading = false;
    auth.user = { id: '99999999-9999-9999-9999-999999999999' };
    vi.mocked(fetchPendingCommentReviews).mockResolvedValue(page);
    vi.mocked(fetchCommentReviewHistory).mockResolvedValue([audit]);
    vi.mocked(decideCommentReview).mockResolvedValue({ ...comment, status: 'APPROVED', version: 4 });
  });

  it('does not load private operator data before authentication resolves or after logout', async () => {
    auth.isLoading = true;
    const view = render(<CommentReviewsPage />);
    expect(screen.getByText('로그인 상태를 확인하는 중입니다.')).toBeInTheDocument();
    expect(fetchPendingCommentReviews).not.toHaveBeenCalled();
    auth.isLoading = false;
    auth.isAuthenticated = false;
    auth.user = null;
    view.rerender(<CommentReviewsPage />);
    expect(screen.getByRole('link', { name: '로그인' })).toHaveAttribute('href', '/login?redirect=%2Fadmin%2Fcomment-reviews');
    expect(fetchPendingCommentReviews).not.toHaveBeenCalled();
  });

  it.each([401, 403])('shows server HTTP %s feedback without assuming the user has an operator role', async (status) => {
    vi.mocked(fetchPendingCommentReviews).mockRejectedValue(new FanPulseApiError('denied', { status }));
    render(<CommentReviewsPage />);
    expect(await screen.findByRole('alert')).toHaveTextContent(status === 401 ? '로그인이 만료되었습니다.' : '댓글 검토 권한이 없습니다.');
    expect(screen.queryByText(comment.content)).not.toBeInTheDocument();
    expect(decideCommentReview).not.toHaveBeenCalled();
  });

  it.each([
    ['APPROVED', '승인', '승인됨'], ['BLOCKED', '차단', '차단됨'],
  ] as const)('requires a reason for %s, sends the expected version and refreshes queue/history', async (status, button, resultLabel) => {
    const completed = { ...comment, status, version: 4 };
    const decisionAudit = { ...audit, id: '66666666-6666-6666-6666-666666666666', action: 'MANUAL_REVIEW', toStatus: status, reason: '검토한 결정 사유', commentVersion: 4 };
    vi.mocked(fetchPendingCommentReviews).mockResolvedValueOnce(page)
      .mockResolvedValueOnce({ ...page, items: [], totalElements: 0, totalPages: 0 });
    vi.mocked(fetchCommentReviewHistory).mockResolvedValueOnce([audit]).mockResolvedValue([audit, decisionAudit]);
    vi.mocked(decideCommentReview).mockResolvedValue(completed);
    render(<CommentReviewsPage />);
    const details = await selectComment();
    expect(await details.findByText(audit.reason!)).toBeInTheDocument();
    expect(details.getByRole('button', { name: '승인' })).toBeDisabled();
    expect(details.getByRole('button', { name: '차단' })).toBeDisabled();
    fireEvent.change(details.getByLabelText('결정 사유 (필수)'), { target: { value: '  ' } });
    expect(details.getByRole('button', { name: button })).toBeDisabled();
    fireEvent.change(details.getByLabelText('결정 사유 (필수)'), { target: { value: ' 검토한 결정 사유 ' } });
    fireEvent.click(details.getByRole('button', { name: button }));
    await waitFor(() => expect(decideCommentReview).toHaveBeenCalledWith(comment.id, {
      expectedVersion: 3, status, reason: '검토한 결정 사유',
    }, expect.any(AbortSignal)));
    expect(await screen.findByText(resultLabel)).toBeInTheDocument();
    expect(await screen.findByText('검토한 결정 사유')).toBeInTheDocument();
    expect(await screen.findByText('검토 대기 댓글이 없습니다.')).toBeInTheDocument();
    expect(fetchPendingCommentReviews).toHaveBeenCalledTimes(2);
    expect(fetchCommentReviewHistory).toHaveBeenCalledTimes(2);
  });

  it('locks stale decisions after a 409 until the operator refreshes and selects the new version', async () => {
    vi.mocked(decideCommentReview).mockRejectedValueOnce(new FanPulseApiError('stale', { status: 409 }))
      .mockResolvedValue({ ...comment, status: 'APPROVED', version: 6 });
    vi.mocked(fetchPendingCommentReviews).mockResolvedValueOnce(page)
      .mockResolvedValueOnce({ ...page, items: [{ ...comment, version: 5 }] });
    render(<CommentReviewsPage />);
    const details = await selectComment();
    fireEvent.change(details.getByLabelText('결정 사유 (필수)'), { target: { value: '검토 사유' } });
    fireEvent.click(details.getByRole('button', { name: '승인' }));
    expect(await screen.findByRole('alert')).toHaveTextContent('다른 검토로 상태가 변경되었습니다.');
    expect(details.getByRole('button', { name: '승인' })).toBeDisabled();
    fireEvent.click(details.getByRole('button', { name: '승인' }));
    expect(decideCommentReview).toHaveBeenCalledTimes(1);
    fireEvent.click(screen.getByRole('button', { name: '목록 새로고침' }));
    const refreshed = await selectComment();
    expect(refreshed.getByLabelText('결정 사유 (필수)')).toHaveValue('');
    fireEvent.change(refreshed.getByLabelText('결정 사유 (필수)'), { target: { value: '다시 검토' } });
    fireEvent.click(refreshed.getByRole('button', { name: '승인' }));
    await waitFor(() => expect(decideCommentReview).toHaveBeenLastCalledWith(comment.id, {
      expectedVersion: 5, status: 'APPROVED', reason: '다시 검토',
    }, expect.any(AbortSignal)));
  });

  it('reports an uncertain decision error without a blind automatic retry', async () => {
    vi.mocked(decideCommentReview).mockRejectedValue(new Error('network'));
    render(<CommentReviewsPage />);
    const details = await selectComment();
    fireEvent.change(details.getByLabelText('결정 사유 (필수)'), { target: { value: '사유' } });
    fireEvent.click(details.getByRole('button', { name: '차단' }));
    expect(await screen.findByRole('alert')).toHaveTextContent('검토 결정을 확인하지 못했습니다.');
    expect(decideCommentReview).toHaveBeenCalledTimes(1);
    expect(details.getByRole('button', { name: '차단' })).toBeDisabled();
  });

  it('clears all private data when a decision is forbidden', async () => {
    vi.mocked(decideCommentReview).mockRejectedValue(new FanPulseApiError('forbidden', { status: 403 }));
    render(<CommentReviewsPage />);
    const details = await selectComment();
    await details.findByText(audit.reason!);
    fireEvent.change(details.getByLabelText('결정 사유 (필수)'), { target: { value: 'private reason' } });
    fireEvent.click(details.getByRole('button', { name: '승인' }));
    expect(await screen.findByRole('alert')).toHaveTextContent('댓글 검토 권한이 없습니다.');
    expect(screen.queryByText(comment.content)).not.toBeInTheDocument();
    expect(screen.queryByText(audit.reason!)).not.toBeInTheDocument();
    expect(screen.queryByLabelText('결정 사유 (필수)')).not.toBeInTheDocument();
  });

  it('retries queue and history failures with explicit refresh controls', async () => {
    vi.mocked(fetchPendingCommentReviews).mockRejectedValueOnce(new Error('network')).mockResolvedValue(page);
    vi.mocked(fetchCommentReviewHistory).mockRejectedValueOnce(new Error('network')).mockResolvedValue([audit]);
    render(<CommentReviewsPage />);
    expect(await screen.findByRole('alert')).toHaveTextContent('검토 목록을 불러오지 못했습니다.');
    fireEvent.click(screen.getByRole('button', { name: '목록 새로고침' }));
    const details = await selectComment();
    expect(await details.findByRole('alert')).toHaveTextContent('감사 이력을 불러오지 못했습니다.');
    fireEvent.click(details.getByRole('button', { name: '이력 새로고침' }));
    expect(await details.findByText(audit.reason!)).toBeInTheDocument();
  });

  it('clears loaded operator rows, history and reason on account change and ignores a late old queue', async () => {
    let finish!: (value: ReviewCommentPage) => void;
    const late = new Promise<ReviewCommentPage>((resolve) => { finish = resolve; });
    vi.mocked(fetchPendingCommentReviews).mockResolvedValueOnce(page).mockReturnValueOnce(late)
      .mockRejectedValueOnce(new FanPulseApiError('forbidden', { status: 403 }));
    const view = render(<CommentReviewsPage />);
    const details = await selectComment();
    await details.findByText(audit.reason!);
    fireEvent.change(details.getByLabelText('결정 사유 (필수)'), { target: { value: '이전 운영자의 사유' } });
    auth.user = { id: '88888888-8888-8888-8888-888888888888' };
    view.rerender(<CommentReviewsPage />);
    expect(screen.queryByText(comment.content)).not.toBeInTheDocument();
    expect(screen.queryByText(audit.reason!)).not.toBeInTheDocument();
    expect(screen.queryByLabelText('결정 사유 (필수)')).not.toBeInTheDocument();
    await waitFor(() => expect(fetchPendingCommentReviews).toHaveBeenCalledTimes(2));
    const signal = vi.mocked(fetchPendingCommentReviews).mock.calls[1][2];
    auth.user = { id: '77777777-7777-7777-7777-777777777777' };
    view.rerender(<CommentReviewsPage />);
    expect(await screen.findByRole('alert')).toHaveTextContent('댓글 검토 권한이 없습니다.');
    expect(signal?.aborted).toBe(true);
    await act(async () => { finish(page); await late; });
    expect(screen.queryByText(comment.content)).not.toBeInTheDocument();
  });

  it('ignores a late decision after logout', async () => {
    let finish!: (value: ReviewComment) => void;
    const late = new Promise<ReviewComment>((resolve) => { finish = resolve; });
    vi.mocked(decideCommentReview).mockReturnValue(late);
    const view = render(<CommentReviewsPage />);
    const details = await selectComment();
    fireEvent.change(details.getByLabelText('결정 사유 (필수)'), { target: { value: '검토 사유' } });
    fireEvent.click(details.getByRole('button', { name: '승인' }));
    const signal = vi.mocked(decideCommentReview).mock.calls[0][2];
    auth.isAuthenticated = false;
    auth.user = null;
    view.rerender(<CommentReviewsPage />);
    expect(signal?.aborted).toBe(true);
    await act(async () => { finish({ ...comment, status: 'APPROVED', version: 4 }); await late; });
    expect(screen.queryByText('검토 결정이 저장되었습니다.')).not.toBeInTheDocument();
    expect(screen.queryByText(comment.content)).not.toBeInTheDocument();
    expect(fetchPendingCommentReviews).toHaveBeenCalledTimes(1);
  });

  it('does not restore a late queue response after history denies access', async () => {
    let finish!: (value: ReviewCommentPage) => void;
    const late = new Promise<ReviewCommentPage>((resolve) => { finish = resolve; });
    vi.mocked(fetchPendingCommentReviews).mockResolvedValueOnce(page).mockReturnValueOnce(late);
    vi.mocked(fetchCommentReviewHistory).mockResolvedValueOnce([audit])
      .mockRejectedValueOnce(new FanPulseApiError('revoked', { status: 403 }));
    render(<CommentReviewsPage />);
    const details = await selectComment();
    await details.findByText(audit.reason!);
    fireEvent.change(details.getByLabelText('결정 사유 (필수)'), { target: { value: '검토 사유' } });
    fireEvent.click(details.getByRole('button', { name: '승인' }));
    expect(await screen.findByRole('alert')).toHaveTextContent('댓글 검토 권한이 없습니다.');
    await act(async () => { finish(page); await late; });
    expect(screen.queryByText(comment.content)).not.toBeInTheDocument();
  });

  it('discards history from a previously selected comment', async () => {
    let finish!: (value: CommentReviewHistory[]) => void;
    const late = new Promise<CommentReviewHistory[]>((resolve) => { finish = resolve; });
    const other = { ...comment, id: '77777777-7777-7777-7777-777777777777', content: '두 번째 검토 댓글' };
    vi.mocked(fetchPendingCommentReviews).mockResolvedValue({ ...page, items: [comment, other], totalElements: 2 });
    vi.mocked(fetchCommentReviewHistory).mockReturnValueOnce(late).mockResolvedValueOnce([]);
    render(<CommentReviewsPage />);
    await selectComment();
    const oldSignal = vi.mocked(fetchCommentReviewHistory).mock.calls[0][1];
    fireEvent.click(screen.getByRole('button', { name: /두 번째 검토 댓글/ }));
    expect(await screen.findByText('감사 이력이 없습니다.')).toBeInTheDocument();
    expect(oldSignal?.aborted).toBe(true);
    await act(async () => { finish([audit]); await late; });
    expect(screen.queryByText(audit.reason!)).not.toBeInTheDocument();
  });

  it('paginates and resets to page zero on manual refresh', async () => {
    vi.mocked(fetchPendingCommentReviews).mockResolvedValueOnce({ ...page, totalElements: 21, totalPages: 2 })
      .mockResolvedValueOnce({ ...page, page: 1, totalElements: 21, totalPages: 2 })
      .mockResolvedValueOnce(page);
    render(<CommentReviewsPage />);
    await screen.findByText(comment.content);
    fireEvent.click(screen.getByRole('button', { name: '다음 페이지' }));
    await waitFor(() => expect(fetchPendingCommentReviews).toHaveBeenLastCalledWith(1, 20, expect.any(AbortSignal)));
    await screen.findByText(comment.content);
    fireEvent.click(screen.getByRole('button', { name: '목록 새로고침' }));
    await waitFor(() => expect(fetchPendingCommentReviews).toHaveBeenLastCalledWith(0, 20, expect.any(AbortSignal)));
  });
});
