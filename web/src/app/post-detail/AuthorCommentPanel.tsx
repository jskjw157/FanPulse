"use client";

import { FormEvent, type ReactNode, useEffect, useRef, useState } from 'react';
import { createCommunityComment, type CommunityComment } from '@/lib/api/community';
import { fetchMyCommentReviews, type ReviewCommentPage } from '@/lib/api/comment-review';
import { toFanPulseError } from '@/lib/api-client';

interface Props {
  postId: string;
  userId: string | null;
  authLoading: boolean;
  ownerBusy: boolean;
  commentRevision: number;
  renderOwnerActions: (comment: CommunityComment, disabled: boolean) => ReactNode;
  renderCommentEditor: (comment: CommunityComment) => ReactNode;
  requireAuthentication: () => boolean;
  refreshPublicComments: (signal: AbortSignal) => Promise<void>;
}

// The parent keys this component by post and authenticated account. No private data is persisted.
export default function AuthorCommentPanel({
  postId, userId, authLoading, ownerBusy, commentRevision, renderOwnerActions, renderCommentEditor,
  requireAuthentication, refreshPublicComments,
}: Props) {
  const [text, setText] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [snapshot, setSnapshot] = useState<ReviewCommentPage | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loadState, setLoadState] = useState({ loading: Boolean(userId), commentRevision });
  const loading = loadState.loading || loadState.commentRevision !== commentRevision;
  const [pagination, setPagination] = useState({ page: 0, commentRevision });
  // A mutation may remove the last row of a private page. Restart reads, not the compose draft.
  const page = pagination.commentRevision === commentRevision ? pagination.page : 0;
  const [revision, setRevision] = useState(0);
  const lifetime = useRef<AbortController | null>(null);

  useEffect(() => {
    const controller = new AbortController();
    lifetime.current = controller;
    return () => controller.abort();
  }, []);

  useEffect(() => {
    if (!userId) return;
    const controller = new AbortController();
    fetchMyCommentReviews(postId, page, 20, controller.signal, userId)
      .then((result) => {
        if (controller.signal.aborted) return;
        if (result.items.some((row) => row.userId !== userId)) throw new Error('댓글 작성자가 현재 계정과 다릅니다.');
        setSnapshot(result);
        setError(null);
      })
      .catch((cause) => {
        if (controller.signal.aborted) return;
        setSnapshot(null);
        setError(toFanPulseError(cause).status === 401
          ? '로그인이 만료되었습니다. 다시 로그인해 주세요.'
          : '내 댓글 검토 상태를 불러오지 못했습니다. 다시 새로고침해 주세요.');
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoadState({ loading: false, commentRevision });
      });
    return () => controller.abort();
  }, [postId, userId, page, revision, commentRevision]);

  const loadPrivatePage = (nextPage = 0) => {
    setLoadState({ loading: true, commentRevision });
    setSnapshot(null);
    setError(null);
    setPagination({ page: nextPage, commentRevision });
    setRevision((value) => value + 1);
  };

  const refresh = async () => {
    loadPrivatePage();
    const signal = lifetime.current!.signal;
    setMessage(null);
    try {
      await refreshPublicComments(signal);
    } catch {
      if (!signal.aborted) setMessage('공개 댓글을 새로고침하지 못했습니다. 다시 시도해 주세요.');
    }
  };

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    if (authLoading || ownerBusy || !text.trim() || submitting || !requireAuthentication()) return;
    if (!userId) return;
    const signal = lifetime.current!.signal;
    setSubmitting(true);
    setMessage(null);
    try {
      const result = await createCommunityComment(postId, text.trim(), null, userId);
      if (signal.aborted) return;
      setText('');
      setMessage(result.status === 'PENDING'
        ? '댓글이 검토 대기 상태로 저장되었습니다.'
        : result.status === 'BLOCKED' ? '댓글이 게시 기준을 통과하지 못했습니다.' : '댓글이 등록되었습니다.');
      loadPrivatePage();
      if (result.status === 'APPROVED') {
        try {
          await refreshPublicComments(signal);
        } catch {
          if (!signal.aborted) setMessage('댓글은 등록되었지만 공개 목록을 새로고침하지 못했습니다.');
        }
      }
    } catch {
      if (!signal.aborted) setMessage('댓글을 저장하지 못했습니다.');
    } finally {
      if (!signal.aborted) setSubmitting(false);
    }
  };

  return (
    <>
      <form onSubmit={submit} className="mb-6 flex gap-2">
        <label htmlFor="comment-content" className="sr-only">댓글 내용</label>
        <input id="comment-content" value={text} onChange={(event) => setText(event.target.value)}
          placeholder="댓글을 입력하세요" maxLength={1000} disabled={authLoading || ownerBusy}
          className="min-w-0 flex-1 rounded-full border border-gray-200 px-4 py-2.5 outline-none focus:border-purple-500" />
        <button type="submit" disabled={authLoading || ownerBusy || !text.trim() || submitting}
          className="rounded-full bg-purple-600 px-4 py-2.5 text-sm font-semibold text-white disabled:opacity-40">
          {submitting ? '저장 중' : '댓글 등록'}
        </button>
      </form>
      {message && <p role="status" className="mb-4 text-sm text-gray-700">{message}</p>}

      {userId && (
        <section aria-label="내 댓글 검토 상태" className="mb-6 rounded-2xl border border-purple-100 bg-purple-50/50 p-4">
          <div className="flex items-center justify-between gap-3">
            <h3 className="font-bold text-gray-900">내 댓글 검토 상태</h3>
            <button type="button" onClick={() => void refresh()} disabled={loading || submitting || ownerBusy}
              className="text-sm font-semibold text-purple-700 disabled:opacity-40">상태 새로고침</button>
          </div>
          <p className="mt-1 text-xs text-gray-500">검토 대기·차단 댓글은 나에게만 표시됩니다. 승인된 댓글은 공개 목록에서 확인하세요.</p>
          {loading && <p role="status" className="mt-3 text-sm text-gray-500">검토 상태를 불러오는 중입니다.</p>}
          {!loading && error && <p role="alert" className="mt-3 text-sm text-red-600">{error}</p>}
          {!loading && snapshot && (
            <>
              {snapshot.items.filter((row) => row.status !== 'APPROVED').length === 0 && (
                <p className="mt-3 text-sm text-gray-500">이 페이지에 검토 대기·차단 댓글이 없습니다.</p>
              )}
              <ul className="mt-3 space-y-3">
                {snapshot.items.filter((row) => row.status !== 'APPROVED').map((row) => (
                  <li key={row.id} className="rounded-xl bg-white p-3">
                    <div className="flex items-center justify-between gap-3">
                      <span className={`rounded-full px-2 py-1 text-xs font-semibold ${row.status === 'BLOCKED' ? 'bg-red-50 text-red-700' : 'bg-amber-50 text-amber-700'}`}>
                        {row.status === 'BLOCKED' ? '차단됨' : '검토 대기'}
                      </span>
                      {renderOwnerActions(row, submitting)}
                    </div>
                    {renderCommentEditor(row) ?? <p className="mt-2 whitespace-pre-wrap break-words text-sm text-gray-800">{row.content}</p>}
                    {row.blockReason && <p className="mt-2 text-sm text-red-700">차단 사유: {row.blockReason}</p>}
                    {row.status === 'PENDING' && (
                      <p className="mt-2 text-xs text-gray-500">
                        {row.requiresManualReview ? '운영자 검토가 필요합니다.' : '자동 검토를 기다리고 있습니다.'}
                        {' '}검토 시도 {row.reviewAttempts}회
                        {row.nextReviewAt && ` · 다음 검토: ${new Date(row.nextReviewAt).toLocaleString('ko-KR')}`}
                      </p>
                    )}
                  </li>
                ))}
              </ul>
              {(snapshot.totalPages > 1 || page > 0) && (
                <div className="mt-3 flex items-center justify-between text-sm text-purple-700">
                  <button type="button" disabled={page === 0 || submitting || ownerBusy} onClick={() => loadPrivatePage(page - 1)} className="disabled:opacity-40">내 댓글 이전</button>
                  <span>{page + 1} / {Math.max(1, snapshot.totalPages)}</span>
                  <button type="button" disabled={page + 1 >= snapshot.totalPages || submitting || ownerBusy} onClick={() => loadPrivatePage(page + 1)} className="disabled:opacity-40">내 댓글 다음</button>
                </div>
              )}
            </>
          )}
        </section>
      )}
    </>
  );
}
