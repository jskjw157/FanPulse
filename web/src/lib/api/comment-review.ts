import { apiClient } from '@/lib/api-client';
import { isIsoInstant, isNonEmptyString, isRecord, isUuid } from '@/lib/api-response';
import type { CommunityComment } from './community';

// Private endpoints deliberately do not use the public APPROVED-only parser or caches.
export interface ReviewComment extends CommunityComment {
  version: number;
  blockReason: string | null;
  reviewAttempts: number;
  nextReviewAt: string | null;
  requiresManualReview: boolean;
}

export interface ReviewCommentPage {
  items: ReviewComment[];
  totalElements: number;
  page: number;
  size: number;
  totalPages: number;
}

export interface CommentReviewHistory {
  id: string;
  commentId: string;
  actorId: string | null;
  action: string;
  fromStatus: string | null;
  toStatus: string;
  reason: string | null;
  commentVersion: number;
  attempt: number;
  createdAt: string;
}

const invalidResponse = '댓글 검토 API 응답이 올바르지 않습니다.';
const privateHeaders = { 'Cache-Control': 'no-store' };
const isCount = (value: unknown): value is number => Number.isSafeInteger(value) && (value as number) >= 0;
const isStatus = (value: unknown): value is CommunityComment['status'] =>
  value === 'APPROVED' || value === 'PENDING' || value === 'BLOCKED';

function isReviewComment(value: unknown): value is ReviewComment {
  return isRecord(value) &&
    isUuid(value.id) && isUuid(value.postId) && isUuid(value.userId) &&
    isNonEmptyString(value.content) && isStatus(value.status) &&
    (value.parentCommentId === null || isUuid(value.parentCommentId)) &&
    isIsoInstant(value.createdAt) &&
    (value.authorName === null || isNonEmptyString(value.authorName)) &&
    isCount(value.version) &&
    (value.blockReason === null || typeof value.blockReason === 'string') &&
    isCount(value.reviewAttempts) &&
    (value.nextReviewAt === null || isIsoInstant(value.nextReviewAt)) &&
    typeof value.requiresManualReview === 'boolean';
}

function parsePage(payload: unknown, page: number, size: number): ReviewCommentPage {
  if (!isRecord(payload) || !Array.isArray(payload.content) ||
    !payload.content.every(isReviewComment) || payload.page !== page || payload.size !== size ||
    !isCount(payload.totalElements) || !isCount(payload.totalPages) ||
    payload.totalPages !== Math.ceil(payload.totalElements / size) ||
    payload.content.length > size ||
    (page >= payload.totalPages && payload.content.length !== 0)) {
    throw new Error(invalidResponse);
  }
  return {
    items: payload.content,
    page,
    size,
    totalElements: payload.totalElements,
    totalPages: payload.totalPages,
  };
}

function validatePage(page: number, size: number) {
  if (!isCount(page) || !Number.isSafeInteger(size) || size <= 0) throw new Error('잘못된 페이지입니다.');
}

function validateId(id: string) {
  if (!isUuid(id)) throw new Error('댓글 또는 게시글 ID가 올바르지 않습니다.');
}

export async function fetchMyCommentReviews(
  postId: string, page = 0, size = 20, signal?: AbortSignal,
): Promise<ReviewCommentPage> {
  validateId(postId);
  validatePage(page, size);
  const response = await apiClient.get<unknown>('/comments/mine', {
    params: { postId, page, size }, signal, headers: privateHeaders,
  });
  const result = parsePage(response.data, page, size);
  if (result.items.some((comment) => comment.postId !== postId)) throw new Error(invalidResponse);
  return result;
}

export async function fetchPendingCommentReviews(
  page = 0, size = 20, signal?: AbortSignal,
): Promise<ReviewCommentPage> {
  validatePage(page, size);
  const response = await apiClient.get<unknown>('/admin/comment-reviews', {
    params: { page, size }, signal, headers: privateHeaders,
  });
  const result = parsePage(response.data, page, size);
  if (result.items.some((comment) => comment.status !== 'PENDING')) throw new Error(invalidResponse);
  return result;
}

export async function fetchCommentReviewHistory(
  commentId: string, signal?: AbortSignal,
): Promise<CommentReviewHistory[]> {
  validateId(commentId);
  const response = await apiClient.get<unknown>(`/admin/comment-reviews/${commentId}/history`, {
    signal, headers: privateHeaders,
  });
  const rows = response.data;
  if (!Array.isArray(rows) || !rows.every((row) => isRecord(row) &&
    isUuid(row.id) && row.commentId === commentId &&
    (row.actorId === null || isUuid(row.actorId)) && isNonEmptyString(row.action) &&
    // Stale automatic results can audit a DELETED target, not just live comment statuses.
    (row.fromStatus === null || isNonEmptyString(row.fromStatus)) && isNonEmptyString(row.toStatus) &&
    (row.reason === null || typeof row.reason === 'string') &&
    isCount(row.commentVersion) && isCount(row.attempt) && isIsoInstant(row.createdAt))) {
    throw new Error(invalidResponse);
  }
  return rows as CommentReviewHistory[];
}

export async function decideCommentReview(
  commentId: string,
  input: { expectedVersion: number; status: 'APPROVED' | 'BLOCKED'; reason: string },
  signal?: AbortSignal,
): Promise<ReviewComment> {
  validateId(commentId);
  if (!isCount(input.expectedVersion) || !isNonEmptyString(input.reason) ||
    (input.status !== 'APPROVED' && input.status !== 'BLOCKED')) {
    throw new Error('결정 사유와 올바른 버전이 필요합니다.');
  }
  const response = await apiClient.post<unknown>(`/admin/comment-reviews/${commentId}/decision`, {
    ...input, reason: input.reason.trim(),
  }, { signal });
  if (!isReviewComment(response.data) || response.data.id !== commentId ||
    response.data.status !== input.status || response.data.version <= input.expectedVersion) {
    throw new Error(invalidResponse);
  }
  return response.data;
}
