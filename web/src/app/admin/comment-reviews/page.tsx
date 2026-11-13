"use client";

import Link from 'next/link';
import { useCallback, useEffect, useRef, useState } from 'react';
import PageHeader from '@/components/layout/PageHeader';
import PageWrapper from '@/components/layout/PageWrapper';
import { useAuth } from '@/contexts/AuthContext';
import { toFanPulseError } from '@/lib/api-client';
import {
  decideCommentReview,
  fetchCommentReviewHistory,
  fetchPendingCommentReviews,
  type CommentReviewHistory,
  type ReviewComment,
  type ReviewCommentPage,
} from '@/lib/api/comment-review';

function accessMessage(status: number | undefined) {
  if (status === 401) return '로그인이 만료되었습니다. 다시 로그인해 주세요.';
  if (status === 403) return '댓글 검토 권한이 없습니다. 운영자 계정으로 확인해 주세요.';
  return null;
}

function formatTime(value: string) {
  return new Date(value).toLocaleString('ko-KR');
}

function ReviewDetails({ comment, onDecision, onAccessDenied }: {
  comment: ReviewComment;
  onDecision: (comment: ReviewComment) => void;
  onAccessDenied: (message: string) => void;
}) {
  const [reason, setReason] = useState('');
  const [busy, setBusy] = useState(false);
  const [locked, setLocked] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [history, setHistory] = useState<CommentReviewHistory[] | null>(null);
  const [historyError, setHistoryError] = useState<string | null>(null);
  const [historyRevision, setHistoryRevision] = useState(0);
  const lifetime = useRef<AbortController | null>(null);

  useEffect(() => {
    const controller = new AbortController();
    lifetime.current = controller;
    return () => controller.abort();
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    fetchCommentReviewHistory(comment.id, controller.signal)
      .then((rows) => {
        if (!controller.signal.aborted) setHistory(rows);
      })
      .catch((cause) => {
        if (controller.signal.aborted) return;
        const denied = accessMessage(toFanPulseError(cause).status);
        if (denied) {
          lifetime.current?.abort();
          onAccessDenied(denied);
        } else setHistoryError('감사 이력을 불러오지 못했습니다. 다시 시도해 주세요.');
      });
    return () => controller.abort();
  }, [comment.id, historyRevision, onAccessDenied]);

  const refreshHistory = () => {
    setHistory(null);
    setHistoryError(null);
    setHistoryRevision((value) => value + 1);
  };

  const decide = async (status: 'APPROVED' | 'BLOCKED') => {
    if (busy || locked || comment.status !== 'PENDING' || !reason.trim()) return;
    const signal = lifetime.current!.signal;
    setBusy(true);
    setError(null);
    try {
      const result = await decideCommentReview(comment.id, {
        expectedVersion: comment.version, status, reason: reason.trim(),
      }, signal);
      if (!signal.aborted) onDecision(result);
    } catch (cause) {
      if (signal.aborted) return;
      const statusCode = toFanPulseError(cause).status;
      const denied = accessMessage(statusCode);
      if (denied) {
        lifetime.current?.abort();
        onAccessDenied(denied);
        return;
      }
      // Never overwrite a newer review or blindly retry an uncertain write.
      setLocked(true);
      setError(statusCode === 409
        ? '다른 검토로 상태가 변경되었습니다. 목록을 새로고침한 후 다시 선택해 주세요.'
        : '검토 결정을 확인하지 못했습니다. 목록과 이력을 새로고침해 상태를 확인해 주세요.');
      refreshHistory();
    } finally {
      if (!signal.aborted) setBusy(false);
    }
  };

  return (
    <section aria-label="선택한 댓글 검토" className="rounded-2xl bg-white p-5 shadow-sm">
      <h2 className="text-lg font-bold text-gray-900">댓글 검토</h2>
      <p className="mt-2 break-all text-xs text-gray-500">댓글 {comment.id} · 버전 {comment.version}</p>
      <Link href={`/post-detail?id=${comment.postId}`} className="mt-2 inline-block text-sm text-purple-700">게시글 보기</Link>
      <p className="mt-3 whitespace-pre-wrap break-words text-sm leading-relaxed text-gray-800">{comment.content}</p>
      <p className="mt-3 text-xs text-gray-500">작성 {formatTime(comment.createdAt)} · 검토 시도 {comment.reviewAttempts}회</p>
      {comment.requiresManualReview && <p className="mt-2 text-sm text-amber-700">운영자 검토 필요</p>}
      {comment.nextReviewAt && <p className="mt-2 text-xs text-gray-500">다음 자동 검토: {formatTime(comment.nextReviewAt)}</p>}
      {comment.status === 'PENDING' ? (
        <div className="mt-5">
          <label htmlFor="review-reason" className="text-sm font-semibold text-gray-900">결정 사유 (필수)</label>
          <textarea id="review-reason" value={reason} onChange={(event) => setReason(event.target.value)}
            disabled={busy || locked} rows={3} maxLength={1000} required
            className="mt-2 w-full rounded-xl border border-gray-200 p-3 text-sm text-gray-900 outline-none focus:border-purple-500 disabled:opacity-50" />
          <div className="mt-3 flex gap-3">
            <button type="button" onClick={() => void decide('APPROVED')} disabled={busy || locked || !reason.trim()}
              className="rounded-full bg-purple-600 px-5 py-2 text-sm font-semibold text-white disabled:opacity-40">승인</button>
            <button type="button" onClick={() => void decide('BLOCKED')} disabled={busy || locked || !reason.trim()}
              className="rounded-full bg-red-50 px-5 py-2 text-sm font-semibold text-red-700 disabled:opacity-40">차단</button>
          </div>
          {busy && <p role="status" className="mt-3 text-sm text-gray-500">결정을 저장하는 중입니다.</p>}
        </div>
      ) : (
        <p className="mt-4 font-semibold text-purple-700">{comment.status === 'APPROVED' ? '승인됨' : '차단됨'}</p>
      )}
      {error && <p role="alert" className="mt-3 text-sm text-red-600">{error}</p>}
      <div className="mt-6 border-t border-gray-100 pt-4">
        <div className="flex items-center justify-between gap-3">
          <h3 className="font-bold text-gray-900">감사 이력</h3>
          <button type="button" onClick={refreshHistory}
            className="text-sm font-semibold text-purple-700">이력 새로고침</button>
        </div>
        {historyError && <p role="alert" className="mt-3 text-sm text-red-600">{historyError}</p>}
        {!history && !historyError && <p className="mt-3 text-sm text-gray-500">이력을 불러오는 중입니다.</p>}
        {history?.length === 0 && <p className="mt-3 text-sm text-gray-500">감사 이력이 없습니다.</p>}
        <ol className="mt-3 space-y-3">
          {history?.map((row) => (
            <li key={row.id} className="rounded-xl bg-gray-50 p-3 text-sm text-gray-700">
              <p className="font-semibold">{row.action} · {row.fromStatus ?? '없음'} → {row.toStatus}</p>
              {row.reason && <p className="mt-1 whitespace-pre-wrap break-words">{row.reason}</p>}
              <p className="mt-1 break-all text-xs text-gray-500">행위자: {row.actorId ?? '시스템'} · 버전 {row.commentVersion} · 시도 {row.attempt}</p>
              <time dateTime={row.createdAt} className="mt-1 block text-xs text-gray-500">{formatTime(row.createdAt)}</time>
            </li>
          ))}
        </ol>
      </div>
    </section>
  );
}

function ReviewQueue() {
  const [page, setPage] = useState(0);
  const [revision, setRevision] = useState(0);
  const [snapshot, setSnapshot] = useState<ReviewCommentPage | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [selected, setSelected] = useState<ReviewComment | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const queueRequest = useRef<AbortController | null>(null);

  const denyAccess = useCallback((message: string) => {
    // A late list response must not restore data after a history/decision denial.
    queueRequest.current?.abort();
    setSnapshot(null);
    setSelected(null);
    setNotice(null);
    setError(message);
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    queueRequest.current = controller;
    fetchPendingCommentReviews(page, 20, controller.signal)
      .then((result) => {
        if (!controller.signal.aborted) setSnapshot(result);
      })
      .catch((cause) => {
        if (controller.signal.aborted) return;
        denyAccess(accessMessage(toFanPulseError(cause).status) ?? '검토 목록을 불러오지 못했습니다. 다시 새로고침해 주세요.');
      });
    return () => controller.abort();
  }, [page, revision, denyAccess]);

  const refresh = (nextPage = 0) => {
    queueRequest.current?.abort();
    setSelected(null);
    setNotice(null);
    setSnapshot(null);
    setError(null);
    setPage(nextPage);
    setRevision((value) => value + 1);
  };

  const decided = (comment: ReviewComment) => {
    if (queueRequest.current?.signal.aborted) return;
    setSelected(comment);
    setNotice('검토 결정이 저장되었습니다.');
    setSnapshot(null);
    setError(null);
    setPage(0);
    setRevision((value) => value + 1);
  };

  return (
    <>
      <div className="mb-5 flex items-center justify-between gap-3">
        <p className="text-sm text-gray-500">검토 대기 댓글만 표시됩니다. 접근 권한과 모든 결정은 서버에서 확인합니다.</p>
        <button type="button" onClick={() => refresh()} className="shrink-0 rounded-full bg-purple-600 px-4 py-2 text-sm font-semibold text-white">목록 새로고침</button>
      </div>
      {notice && <p role="status" className="mb-4 text-sm text-purple-700">{notice}</p>}
      {error && <p role="alert" className="mb-4 text-sm text-red-600">{error}</p>}
      <div className="grid items-start gap-5 lg:grid-cols-2">
        <section aria-label="검토 대기 목록" className="rounded-2xl bg-white p-5 shadow-sm">
          <h2 className="text-lg font-bold text-gray-900">검토 대기 {snapshot ? `(${snapshot.totalElements})` : ''}</h2>
          {!snapshot && !error && <p className="mt-4 text-sm text-gray-500">검토 목록을 불러오는 중입니다.</p>}
          {snapshot?.items.length === 0 && <p className="mt-4 text-sm text-gray-500">검토 대기 댓글이 없습니다.</p>}
          <ul className="mt-4 space-y-3">
            {snapshot?.items.map((comment) => (
              <li key={comment.id}>
                <button type="button" aria-pressed={selected?.id === comment.id}
                  onClick={() => { setSelected(comment); setNotice(null); }}
                  className={`w-full rounded-xl border p-4 text-left ${selected?.id === comment.id ? 'border-purple-300 bg-purple-50' : 'border-gray-200 bg-gray-50'}`}>
                  <span className="block whitespace-pre-wrap break-words text-sm text-gray-800">{comment.content}</span>
                  <span className="mt-2 block text-xs text-gray-500">버전 {comment.version} · {formatTime(comment.createdAt)}</span>
                  <span className="mt-2 block text-xs font-semibold text-purple-700">검토 및 이력 보기</span>
                </button>
              </li>
            ))}
          </ul>
          {snapshot && (snapshot.totalPages > 1 || page > 0) && (
            <div className="mt-4 flex items-center justify-between text-sm text-purple-700">
              <button type="button" disabled={page === 0} onClick={() => refresh(page - 1)} className="disabled:opacity-40">이전 페이지</button>
              <span>{page + 1} / {Math.max(1, snapshot.totalPages)}</span>
              <button type="button" disabled={page + 1 >= snapshot.totalPages} onClick={() => refresh(page + 1)} className="disabled:opacity-40">다음 페이지</button>
            </div>
          )}
        </section>
        {selected && <ReviewDetails key={`${selected.id}:${selected.version}`} comment={selected} onDecision={decided} onAccessDenied={denyAccess} />}
      </div>
    </>
  );
}

export default function CommentReviewsPage() {
  const { isAuthenticated, isLoading, user } = useAuth();
  return (
    <div className="min-h-screen bg-gray-50">
      <PageHeader title="댓글 검토 관리" showBack />
      <PageWrapper className="mx-auto max-w-5xl px-4 py-6">
        {isLoading ? <p className="py-16 text-center text-gray-500">로그인 상태를 확인하는 중입니다.</p>
          : isAuthenticated && user?.id
            ? <ReviewQueue key={user.id} />
            : <p className="py-16 text-center text-gray-700">로그인이 필요합니다. <Link href="/login?redirect=%2Fadmin%2Fcomment-reviews" className="font-semibold text-purple-700">로그인</Link></p>}
      </PageWrapper>
    </div>
  );
}
