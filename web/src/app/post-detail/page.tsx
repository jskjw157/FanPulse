"use client";

import PageHeader from "@/components/layout/PageHeader";
import PageWrapper from "@/components/layout/PageWrapper";
import { useAuth } from "@/contexts/AuthContext";
import { toFanPulseError } from "@/lib/api-client";
import {
  deleteCommunityComment,
  deleteCommunityPost,
  fetchCommunityComments,
  fetchCommunityPost,
  fetchCommunityPostState,
  likeCommunityPost,
  saveCommunityPost,
  unlikeCommunityPost,
  unsaveCommunityPost,
  updateCommunityComment,
  updateCommunityPost,
  type CommunityComment,
  type CommunityPost,
  type CommunityPostState,
} from "@/lib/api/community";
import { useRouter, useSearchParams } from "next/navigation";
import { FormEvent, useEffect, useRef, useState } from "react";
import AuthorCommentPanel from "./AuthorCommentPanel";

type ActionTarget = { kind: "post" } | { kind: "comment"; comment: CommunityComment };

function OwnerActions({ label, disabled, onEdit, onDelete }: {
  label: string;
  disabled: boolean;
  onEdit: () => void;
  onDelete: () => void;
}) {
  return (
    <details name="community-owner-actions" className="relative shrink-0">
      <summary aria-label={label} className="cursor-pointer list-none rounded-full px-3 py-2 text-gray-600 hover:bg-purple-50">
        <span aria-hidden="true">•••</span>
      </summary>
      <div className="absolute right-0 z-20 mt-1 w-32 rounded-xl border border-gray-100 bg-white p-1 shadow-lg">
        <button type="button" disabled={disabled} onClick={(event) => {
          event.currentTarget.closest("details")?.removeAttribute("open");
          onEdit();
        }} className="w-full rounded-lg px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-50 disabled:opacity-40">수정</button>
        <button type="button" disabled={disabled} onClick={(event) => {
          event.currentTarget.closest("details")?.removeAttribute("open");
          onDelete();
        }} className="w-full rounded-lg px-4 py-2 text-left text-sm text-red-600 hover:bg-red-50 disabled:opacity-40">삭제</button>
      </div>
    </details>
  );
}

function ContentEditor({ kind, value, busy, error, onChange, onCancel, onSubmit }: {
  kind: ActionTarget["kind"];
  value: string;
  busy: boolean;
  error: string | null;
  onChange: (value: string) => void;
  onCancel: () => void;
  onSubmit: (event: FormEvent) => void;
}) {
  const label = kind === "post" ? "게시글 내용 수정" : "댓글 내용 수정";
  return (
    <form onSubmit={onSubmit} className="space-y-3">
      <label htmlFor="edit-content" className="block text-sm font-semibold text-gray-900">{label}</label>
      <textarea id="edit-content" autoFocus value={value} disabled={busy}
        onChange={(event) => onChange(event.target.value)} maxLength={kind === "post" ? 5000 : 1000}
        className="min-h-32 w-full rounded-xl border border-purple-200 bg-white p-3 text-gray-900 outline-none focus:ring-2 focus:ring-purple-500" />
      {error && <p role="alert" className="text-sm text-red-600">{error}</p>}
      <div className="flex justify-end gap-2">
        <button type="button" disabled={busy} onClick={onCancel} className="rounded-full border border-gray-200 px-4 py-2 text-sm text-gray-700 disabled:opacity-40">취소</button>
        <button type="submit" disabled={busy || !value.trim()} className="rounded-full bg-purple-600 px-4 py-2 text-sm font-semibold text-white disabled:opacity-40">{busy ? "저장 중" : "수정 저장"}</button>
      </div>
    </form>
  );
}

function DeleteConfirmation({ kind, busy, error, onCancel, onConfirm }: {
  kind: ActionTarget["kind"];
  busy: boolean;
  error: string | null;
  onCancel: () => void;
  onConfirm: () => void;
}) {
  const ref = useRef<HTMLDialogElement>(null);
  useEffect(() => { ref.current?.showModal(); }, []);
  const label = kind === "post" ? "게시글" : "댓글";
  return (
    <dialog ref={ref} aria-labelledby="delete-title" aria-describedby="delete-description"
      onCancel={(event) => { event.preventDefault(); if (!busy) onCancel(); }}
      className="fixed inset-0 m-auto w-[calc(100%_-_2rem)] max-w-sm rounded-2xl bg-white p-6 shadow-xl backdrop:bg-black/40">
      <h2 id="delete-title" className="text-lg font-bold text-gray-900">{label}을 삭제할까요?</h2>
      <p id="delete-description" className="mt-3 text-sm text-gray-600">삭제한 {label}은 복구할 수 없습니다.{kind === "comment" && " 작성된 답글은 유지됩니다."}</p>
      {error && <p role="alert" className="mt-3 text-sm text-red-600">{error}</p>}
      <div className="mt-6 flex justify-end gap-2">
        <button type="button" autoFocus disabled={busy} onClick={onCancel} className="rounded-full border border-gray-200 px-4 py-2 text-sm text-gray-700 disabled:opacity-40">취소</button>
        <button type="button" disabled={busy} onClick={onConfirm} className="rounded-full bg-red-600 px-4 py-2 text-sm font-semibold text-white disabled:opacity-40">{busy ? "삭제 중" : "삭제 확인"}</button>
      </div>
    </dialog>
  );
}

function formatTime(value: string) {
  return new Intl.DateTimeFormat("ko-KR", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(value));
}

export default function PostDetailPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const postId = searchParams.get("id");
  const { isAuthenticated, isLoading: authLoading, user } = useAuth();
  const privateUserId = !authLoading && isAuthenticated ? user?.id ?? null : null;

  const [post, setPost] = useState<CommunityPost | null>(null);
  const [comments, setComments] = useState<CommunityComment[]>([]);
  const [commentPage, setCommentPage] = useState({ page: 0, totalPages: 0, totalElements: 0 });
  const [state, setState] = useState<CommunityPostState | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [stateError, setStateError] = useState<string | null>(null);
  const [mutationLoading, setMutationLoading] = useState(false);
  const [commentMessage, setCommentMessage] = useState<string | null>(null);
  const [commentsLoadingMore, setCommentsLoadingMore] = useState(false);
  const [retryKey, setRetryKey] = useState(0);
  const [editing, setEditing] = useState<ActionTarget | null>(null);
  const [draft, setDraft] = useState("");
  const [deleting, setDeleting] = useState<ActionTarget | null>(null);
  const [ownerBusy, setOwnerBusy] = useState(false);
  const [ownerError, setOwnerError] = useState<string | null>(null);
  const [ownerMessage, setOwnerMessage] = useState<string | null>(null);
  const generationRef = useRef(0);
  const owns = (authorId: string) => !authLoading && isAuthenticated && user?.id === authorId;
  const ownsTarget = (target: ActionTarget) => target.kind === "post"
    ? !!post && owns(post.author.id)
    : owns(target.comment.userId);
  const ownerDisabled = ownerBusy || commentsLoadingMore || mutationLoading;

  useEffect(() => {
    const generation = ++generationRef.current;
    setEditing(null);
    setDeleting(null);
    setOwnerError(null);
    setOwnerMessage(null);
    setOwnerBusy(false);
    setCommentsLoadingMore(false);
    if (!postId) {
      setError("게시글을 불러오지 못했습니다.");
      setLoading(false);
      return;
    }

    const controller = new AbortController();
    setLoading(true);
    setError(null);
    setPost(null);
    setComments([]);

    Promise.all([
      fetchCommunityPost(postId, controller.signal),
      fetchCommunityComments(postId, 0, 20, controller.signal),
    ])
      .then(([postData, commentData]) => {
        if (controller.signal.aborted) return;
        setPost(postData);
        setComments(commentData.items);
        setCommentPage({
          page: commentData.page,
          totalPages: commentData.totalPages,
          totalElements: commentData.totalElements,
        });
      })
      .catch((cause: unknown) => {
        if (!controller.signal.aborted) setError(toFanPulseError(cause).status === 404
          ? "삭제되었거나 사용할 수 없는 게시글입니다."
          : "게시글을 불러오지 못했습니다.");
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false);
      });

    return () => { controller.abort(); generationRef.current = generation + 1; };
  }, [postId, retryKey]);

  useEffect(() => {
    if (!postId || authLoading) return;
    if (!isAuthenticated) {
      setState(null);
      setStateError(null);
      return;
    }

    const controller = new AbortController();
    setState(null);
    setStateError(null);
    fetchCommunityPostState(postId, controller.signal)
      .then((result) => {
        if (!controller.signal.aborted) setState(result);
      })
      .catch(() => {
        if (!controller.signal.aborted) setStateError("좋아요·저장 상태를 불러오지 못했습니다.");
      });
    return () => controller.abort();
  }, [authLoading, isAuthenticated, user?.id, postId, retryKey]);

  const requireAuthentication = () => {
    if (isAuthenticated) return true;
    router.push(`/login?redirect=${encodeURIComponent(`/post-detail?id=${postId ?? ""}`)}`);
    return false;
  };

  const handleLike = async () => {
    if (!postId || !requireAuthentication() || !state || mutationLoading) return;
    setMutationLoading(true);
    setStateError(null);
    try {
      const next = state.liked
        ? await unlikeCommunityPost(postId)
        : await likeCommunityPost(postId);
      setPost((current) => current ? {
        ...current,
        likeCount: Math.max(0, current.likeCount + (next.liked ? 1 : -1)),
      } : current);
      setState(next);
    } catch {
      setStateError("좋아요 상태를 저장하지 못했습니다.");
    } finally {
      setMutationLoading(false);
    }
  };

  const handleSave = async () => {
    if (!postId || !requireAuthentication() || !state || mutationLoading) return;
    setMutationLoading(true);
    setStateError(null);
    try {
      const next = state.saved
        ? await unsaveCommunityPost(postId)
        : await saveCommunityPost(postId);
      setState(next);
    } catch {
      setStateError("게시글 저장 상태를 변경하지 못했습니다.");
    } finally {
      setMutationLoading(false);
    }
  };

  const reloadComments = async (signal?: AbortSignal) => {
    if (!postId) return;
    const generation = generationRef.current;
    const result = signal
      ? await fetchCommunityComments(postId, 0, 20, signal)
      : await fetchCommunityComments(postId, 0, 20);
    if (signal?.aborted || generation !== generationRef.current) return;
    setComments(result.items);
    setCommentPage({
      page: result.page,
      totalPages: result.totalPages,
      totalElements: result.totalElements,
    });
    setPost((current) => current ? { ...current, commentCount: result.totalElements } : current);
  };

  const beginEdit = (target: ActionTarget) => {
    if (!ownsTarget(target) || ownerDisabled) return;
    setEditing(target);
    setDraft(target.kind === "post" ? post!.content : target.comment.content);
    setOwnerError(null);
    setOwnerMessage(null);
  };

  const beginDelete = (target: ActionTarget) => {
    if (!ownsTarget(target) || ownerDisabled) return;
    setEditing(null);
    setDeleting(target);
    setOwnerError(null);
    setOwnerMessage(null);
  };

  const refreshChangedComments = async () => {
    const generation = generationRef.current;
    try {
      await reloadComments();
    } catch {
      if (generation !== generationRef.current) return;
      // The mutation succeeded. Hide stale public content and retry reads, not the mutation.
      setComments([]);
      setError("댓글 변경은 저장되었지만 목록을 불러오지 못했습니다. 다시 시도해 주세요.");
    }
  };

  const handleEdit = async (event: FormEvent) => {
    event.preventDefault();
    if (!postId || !editing || !ownsTarget(editing) || ownerDisabled || !draft.trim()) return;
    const generation = generationRef.current;
    setOwnerBusy(true);
    setOwnerError(null);
    try {
      if (editing.kind === "post") {
        const result = await updateCommunityPost(postId, draft.trim());
        if (generation !== generationRef.current) return;
        setPost(result);
        setOwnerMessage("게시글을 수정했습니다.");
      } else {
        const result = await updateCommunityComment(postId, editing.comment.id, draft.trim());
        if (generation !== generationRef.current) return;
        setOwnerMessage(result.status === "PENDING"
          ? "수정한 댓글이 검토 대기 중이므로 공개 목록에서 제외되었습니다."
          : result.status === "BLOCKED"
            ? "수정한 댓글이 게시 기준을 통과하지 못해 공개 목록에서 제외되었습니다."
            : "댓글을 수정했습니다.");
        await refreshChangedComments();
      }
      if (generation === generationRef.current) setEditing(null);
    } catch {
      if (generation === generationRef.current) setOwnerError("수정하지 못했습니다. 내용을 확인한 뒤 다시 시도해 주세요.");
    } finally {
      if (generation === generationRef.current) setOwnerBusy(false);
    }
  };

  const handleDelete = async () => {
    if (!postId || !deleting || !ownsTarget(deleting) || ownerDisabled) return;
    const generation = generationRef.current;
    setOwnerBusy(true);
    setOwnerError(null);
    try {
      if (deleting.kind === "post") {
        await deleteCommunityPost(postId);
        if (generation !== generationRef.current) return;
        router.push("/community");
      } else {
        await deleteCommunityComment(postId, deleting.comment.id);
        if (generation !== generationRef.current) return;
        setOwnerMessage("댓글을 삭제했습니다. 작성된 답글은 유지됩니다.");
        await refreshChangedComments();
      }
      if (generation === generationRef.current) setDeleting(null);
    } catch {
      if (generation === generationRef.current) setOwnerError("삭제하지 못했습니다. 다시 시도해 주세요.");
    } finally {
      if (generation === generationRef.current) setOwnerBusy(false);
    }
  };

  const loadMoreComments = async () => {
    if (!postId || commentsLoadingMore || ownerBusy || commentPage.page + 1 >= commentPage.totalPages) return;
    const generation = generationRef.current;
    setCommentsLoadingMore(true);
    try {
      const result = await fetchCommunityComments(postId, commentPage.page + 1, 20);
      if (generation !== generationRef.current) return;
      setComments((current) => {
        const seen = new Set(current.map((comment) => comment.id));
        return [...current, ...result.items.filter((comment) => !seen.has(comment.id))];
      });
      setCommentPage({
        page: result.page,
        totalPages: result.totalPages,
        totalElements: result.totalElements,
      });
    } catch {
      if (generation === generationRef.current) setCommentMessage("댓글을 더 불러오지 못했습니다.");
    } finally {
      if (generation === generationRef.current) setCommentsLoadingMore(false);
    }
  };

  return (
    <>
      <PageHeader title="게시글" showBack />
      <PageWrapper className="mx-auto max-w-3xl px-4 py-6">
        {loading && <p className="py-16 text-center text-gray-500">게시글을 불러오는 중입니다.</p>}

        {!loading && error && (
          <div className="py-16 text-center">
            <p className="text-gray-700">{error}</p>
            <button
              type="button"
              onClick={() => setRetryKey((value) => value + 1)}
              className="mt-4 rounded-full bg-purple-600 px-5 py-2 text-sm font-semibold text-white"
            >
              다시 시도
            </button>
          </div>
        )}

        {!loading && !error && post && post.id === postId && (
          <article className="rounded-3xl bg-white p-5 shadow-sm">
            <header className="mb-5 flex items-center gap-3">
              <div
                aria-hidden="true"
                className="flex h-11 w-11 items-center justify-center rounded-full bg-purple-100 font-bold text-purple-700"
              >
                {post.author.name.slice(0, 1).toUpperCase()}
              </div>
              <div className="min-w-0 flex-1">
                <p className="truncate font-bold text-gray-900">{post.author.name}</p>
                <p className="text-xs text-gray-500">{formatTime(post.createdAt)}</p>
              </div>
              {post.artist && (
                <span className="rounded-full bg-purple-50 px-3 py-1 text-xs font-semibold text-purple-700">
                  {post.artist.name}
                </span>
              )}
              {owns(post.author.id) && <OwnerActions label="게시글 작업" disabled={ownerDisabled}
                onEdit={() => beginEdit({ kind: "post" })} onDelete={() => beginDelete({ kind: "post" })} />}
            </header>

            {editing?.kind === "post" && owns(post.author.id) ? (
              <ContentEditor kind="post" value={draft} busy={ownerBusy} error={ownerError}
                onChange={setDraft} onCancel={() => { setEditing(null); setOwnerError(null); }} onSubmit={handleEdit} />
            ) : <p className="whitespace-pre-wrap text-base leading-relaxed text-gray-800">{post.content}</p>}

            {ownerMessage && <p role="status" className="mt-4 rounded-xl bg-purple-50 p-3 text-sm text-purple-800">{ownerMessage}</p>}

            {post.imageUrl && (
              // eslint-disable-next-line @next/next/no-img-element -- validated external HTTPS URL must bypass Next host allowlists
              <img
                src={post.imageUrl}
                alt="게시글 첨부 이미지"
                referrerPolicy="no-referrer"
                className="mt-5 max-h-[560px] w-full rounded-2xl object-cover"
              />
            )}

            <div className="mt-6 flex items-center gap-3 border-y border-gray-100 py-3">
              <button
                type="button"
                aria-label="좋아요"
                aria-pressed={state?.liked ?? false}
                disabled={ownerBusy || mutationLoading || (isAuthenticated && state === null)}
                onClick={handleLike}
                className={`rounded-full px-4 py-2 text-sm font-semibold ${state?.liked ? "bg-pink-50 text-pink-600" : "bg-gray-50 text-gray-700"}`}
              >
                <i className={state?.liked ? "ri-heart-fill" : "ri-heart-line"} /> {post.likeCount.toLocaleString()}
              </button>
              <span className="rounded-full bg-gray-50 px-4 py-2 text-sm text-gray-700">
                <i className="ri-chat-3-line" /> {commentPage.totalElements.toLocaleString()}
              </span>
              <button
                type="button"
                aria-label="저장"
                aria-pressed={state?.saved ?? false}
                disabled={ownerBusy || mutationLoading || (isAuthenticated && state === null)}
                onClick={handleSave}
                className={`ml-auto rounded-full px-4 py-2 text-sm font-semibold ${state?.saved ? "bg-purple-50 text-purple-700" : "bg-gray-50 text-gray-700"}`}
              >
                <i className={state?.saved ? "ri-bookmark-fill" : "ri-bookmark-line"} /> 저장
              </button>
            </div>

            {stateError && <p className="mt-3 text-sm text-red-600">{stateError}</p>}

            <section className="mt-7">
              <h2 className="mb-4 text-lg font-bold text-gray-900">댓글 {commentPage.totalElements}</h2>

              <AuthorCommentPanel
                key={`${post.id}:${privateUserId ?? 'anonymous'}:${authLoading}`}
                postId={post.id}
                userId={privateUserId}
                authLoading={authLoading}
                requireAuthentication={requireAuthentication}
                refreshPublicComments={reloadComments}
              />

              {commentMessage && <p role="alert" className="mb-4 text-sm text-red-600">{commentMessage}</p>}

              {comments.length === 0 ? (
                <p className="rounded-2xl bg-gray-50 py-10 text-center text-sm text-gray-500">
                  아직 작성된 댓글이 없습니다.
                </p>
              ) : (
                <div className="space-y-3">
                  {comments.map((comment) => (
                    <article key={comment.id} className="rounded-2xl bg-gray-50 p-4">
                      <div className="mb-2 flex items-center justify-between gap-3">
                        <p className="font-semibold text-gray-900">
                          {comment.authorName ?? "탈퇴한 사용자"}
                        </p>
                        <time className="ml-auto text-xs text-gray-500">{formatTime(comment.createdAt)}</time>
                        {owns(comment.userId) && <OwnerActions label={`댓글 작업: ${comment.content}`} disabled={ownerDisabled}
                          onEdit={() => beginEdit({ kind: "comment", comment })} onDelete={() => beginDelete({ kind: "comment", comment })} />}
                      </div>
                      {editing?.kind === "comment" && editing.comment.id === comment.id && owns(comment.userId) ? (
                        <ContentEditor kind="comment" value={draft} busy={ownerBusy} error={ownerError}
                          onChange={setDraft} onCancel={() => { setEditing(null); setOwnerError(null); }} onSubmit={handleEdit} />
                      ) : <p className="whitespace-pre-wrap text-sm leading-relaxed text-gray-700">{comment.content}</p>}
                    </article>
                  ))}
                </div>
              )}

              {commentPage.page + 1 < commentPage.totalPages && (
                <button
                  type="button"
                  onClick={loadMoreComments}
                  disabled={commentsLoadingMore || ownerBusy}
                  className="mt-4 w-full rounded-xl border border-gray-200 py-3 text-sm font-semibold text-gray-700"
                >
                  {commentsLoadingMore ? "불러오는 중" : "댓글 더 보기"}
                </button>
              )}
            </section>
          </article>
        )}
      </PageWrapper>
      {deleting && ownsTarget(deleting) && <DeleteConfirmation kind={deleting.kind} busy={ownerBusy} error={ownerError}
        onCancel={() => { setDeleting(null); setOwnerError(null); }} onConfirm={handleDelete} />}
    </>
  );
}
