import { beforeEach, describe, expect, it, vi } from 'vitest';
import { apiClient } from '@/lib/api-client';
import {
  decideCommentReview,
  fetchCommentReviewHistory,
  fetchMyCommentReviews,
  fetchPendingCommentReviews,
} from './comment-review';

vi.mock('@/lib/api-client', () => ({ apiClient: { get: vi.fn(), post: vi.fn() } }));

const comment = {
  id: '44444444-4444-4444-4444-444444444444',
  postId: '11111111-1111-1111-1111-111111111111',
  userId: '22222222-2222-2222-2222-222222222222',
  content: 'private pending comment',
  status: 'PENDING',
  parentCommentId: null,
  createdAt: '2026-11-06T08:00:00Z',
  authorName: null,
  version: 3,
  blockReason: null,
  reviewAttempts: 2,
  nextReviewAt: '2026-11-06T08:05:00Z',
  requiresManualReview: false,
};
const page = { content: [comment], page: 0, size: 20, totalElements: 1, totalPages: 1 };
const history = {
  id: '55555555-5555-5555-5555-555555555555', commentId: comment.id, actorId: null,
  action: 'RETRY', fromStatus: 'PENDING', toStatus: 'PENDING', reason: null,
  commentVersion: 3, attempt: 2, createdAt: '2026-11-06T08:01:00Z',
};

describe('private comment review API', () => {
  beforeEach(() => vi.resetAllMocks());

  it('parses a raw author page and never sends caller identity', async () => {
    const signal = new AbortController().signal;
    vi.mocked(apiClient.get).mockResolvedValue({ data: page });
    await expect(fetchMyCommentReviews(comment.postId, 0, 20, signal)).resolves.toEqual({
      items: [comment], page: 0, size: 20, totalElements: 1, totalPages: 1,
    });
    expect(apiClient.get).toHaveBeenCalledWith('/comments/mine', {
      params: { postId: comment.postId, page: 0, size: 20 }, signal,
      headers: { 'Cache-Control': 'no-store' },
    });
  });

  it.each(['PENDING', 'BLOCKED', 'APPROVED'])('accepts own %s comments without changing the public parser', async (status) => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { ...page, content: [{ ...comment, status }] } });
    expect((await fetchMyCommentReviews(comment.postId)).items[0].status).toBe(status);
  });

  it.each([
    { success: true, data: page },
    { ...page, content: [{ ...comment, version: -1 }] },
    { ...page, content: [{ ...comment, reviewAttempts: 1.5 }] },
    { ...page, content: [{ ...comment, nextReviewAt: 'tomorrow' }] },
    { ...page, content: [{ ...comment, requiresManualReview: null }] },
    { ...page, content: [{ ...comment, blockReason: 3 }] },
    { ...page, content: [{ ...comment, postId: '99999999-9999-9999-9999-999999999999' }] },
    { ...page, page: 1 },
    { ...page, size: 10 },
    { ...page, totalPages: 2 },
  ])('rejects malformed or cross-post private payloads: %o', async (data) => {
    vi.mocked(apiClient.get).mockResolvedValue({ data });
    await expect(fetchMyCommentReviews(comment.postId)).rejects.toThrow('댓글 검토 API 응답');
  });

  it('requests only the pending operator queue and rejects nonpending rows', async () => {
    vi.mocked(apiClient.get).mockResolvedValueOnce({ data: page })
      .mockResolvedValueOnce({ data: { ...page, content: [{ ...comment, status: 'BLOCKED' }] } });
    expect((await fetchPendingCommentReviews()).items).toEqual([comment]);
    expect(apiClient.get).toHaveBeenCalledWith('/admin/comment-reviews', {
      params: { page: 0, size: 20 }, signal: undefined, headers: { 'Cache-Control': 'no-store' },
    });
    await expect(fetchPendingCommentReviews()).rejects.toThrow('댓글 검토 API 응답');
  });

  it('accepts an empty Spring page after the queue shrinks', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { ...page, content: [], page: 1, totalElements: 0, totalPages: 0 } });
    expect((await fetchPendingCommentReviews(1)).items).toEqual([]);
  });

  it('validates raw history including system actors and binds rows to the requested comment', async () => {
    vi.mocked(apiClient.get).mockResolvedValueOnce({ data: [history] })
      .mockResolvedValueOnce({ data: [{ ...history, commentId: comment.postId }] });
    await expect(fetchCommentReviewHistory(comment.id)).resolves.toEqual([history]);
    expect(apiClient.get).toHaveBeenCalledWith(`/admin/comment-reviews/${comment.id}/history`, {
      signal: undefined, headers: { 'Cache-Control': 'no-store' },
    });
    await expect(fetchCommentReviewHistory(comment.id)).rejects.toThrow('댓글 검토 API 응답');
  });

  it('accepts a stale-result audit targeting a deleted comment', async () => {
    const deleted = { ...history, action: 'STALE_RESULT', toStatus: 'DELETED' };
    vi.mocked(apiClient.get).mockResolvedValue({ data: [deleted] });
    await expect(fetchCommentReviewHistory(comment.id)).resolves.toEqual([deleted]);
  });

  it.each(['APPROVED', 'BLOCKED'] as const)('sends a versioned %s decision with a trimmed reason', async (status) => {
    const result = { ...comment, version: 4, status };
    vi.mocked(apiClient.post).mockResolvedValue({ data: result });
    const signal = new AbortController().signal;
    await expect(decideCommentReview(comment.id, { expectedVersion: 3, status, reason: ' policy checked ' }, signal)).resolves.toEqual(result);
    expect(apiClient.post).toHaveBeenCalledWith(`/admin/comment-reviews/${comment.id}/decision`, {
      expectedVersion: 3, status, reason: 'policy checked',
    }, { signal });
  });

  it('rejects an empty reason before making a request', async () => {
    await expect(decideCommentReview(comment.id, { expectedVersion: 3, status: 'BLOCKED', reason: '  ' })).rejects.toThrow('결정 사유');
    expect(apiClient.post).not.toHaveBeenCalled();
  });

  it.each([
    { ...comment, status: 'APPROVED', version: 3 },
    { ...comment, status: 'BLOCKED', version: 4 },
    { ...comment, id: comment.postId, status: 'APPROVED', version: 4 },
  ])('does not treat an inconsistent decision result as success', async (data) => {
    vi.mocked(apiClient.post).mockResolvedValue({ data });
    await expect(decideCommentReview(comment.id, { expectedVersion: 3, status: 'APPROVED', reason: 'checked' })).rejects.toThrow('댓글 검토 API 응답');
  });

  it.each([401, 403, 409, 500])('preserves HTTP %s for UI feedback without retrying the write', async (status) => {
    const error = { response: { status } };
    vi.mocked(apiClient.post).mockRejectedValue(error);
    await expect(decideCommentReview(comment.id, { expectedVersion: 3, status: 'BLOCKED', reason: 'policy' })).rejects.toBe(error);
    expect(apiClient.post).toHaveBeenCalledTimes(1);
  });
});
