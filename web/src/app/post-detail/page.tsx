"use client";

import PageHeader from "@/components/layout/PageHeader";
import PageWrapper from "@/components/layout/PageWrapper";
import { useAuth } from "@/contexts/AuthContext";
import {
  createCommunityComment,
  fetchCommunityComments,
  fetchCommunityPost,
  fetchCommunityPostState,
  likeCommunityPost,
  saveCommunityPost,
  unlikeCommunityPost,
  unsaveCommunityPost,
  type CommunityComment,
  type CommunityPost,
  type CommunityPostState,
} from "@/lib/api/community";
import { useRouter, useSearchParams } from "next/navigation";
import { FormEvent, useEffect, useState } from "react";

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
  const { isAuthenticated, isLoading: authLoading } = useAuth();

  const [post, setPost] = useState<CommunityPost | null>(null);
  const [comments, setComments] = useState<CommunityComment[]>([]);
  const [commentPage, setCommentPage] = useState({ page: 0, totalPages: 0, totalElements: 0 });
  const [state, setState] = useState<CommunityPostState | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [stateError, setStateError] = useState<string | null>(null);
  const [mutationLoading, setMutationLoading] = useState(false);
  const [commentText, setCommentText] = useState("");
  const [commentSubmitting, setCommentSubmitting] = useState(false);
  const [commentMessage, setCommentMessage] = useState<string | null>(null);
  const [commentsLoadingMore, setCommentsLoadingMore] = useState(false);
  const [retryKey, setRetryKey] = useState(0);

  useEffect(() => {
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
      .catch(() => {
        if (!controller.signal.aborted) setError("게시글을 불러오지 못했습니다.");
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false);
      });

    return () => controller.abort();
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
  }, [authLoading, isAuthenticated, postId, retryKey]);

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

  const reloadComments = async () => {
    if (!postId) return;
    const result = await fetchCommunityComments(postId, 0, 20);
    setComments(result.items);
    setCommentPage({
      page: result.page,
      totalPages: result.totalPages,
      totalElements: result.totalElements,
    });
    setPost((current) => current ? { ...current, commentCount: result.totalElements } : current);
  };

  const handleCommentSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (!postId || !commentText.trim() || !requireAuthentication() || commentSubmitting) return;
    setCommentSubmitting(true);
    setCommentMessage(null);
    try {
      const result = await createCommunityComment(postId, commentText.trim());
      setCommentText("");
      if (result.status === "APPROVED") {
        await reloadComments();
      } else if (result.status === "PENDING") {
        setCommentMessage("댓글이 검토 대기 상태로 저장되었습니다.");
      } else {
        setCommentMessage("댓글이 게시 기준을 통과하지 못했습니다.");
      }
    } catch {
      setCommentMessage("댓글을 저장하지 못했습니다.");
    } finally {
      setCommentSubmitting(false);
    }
  };

  const loadMoreComments = async () => {
    if (!postId || commentsLoadingMore || commentPage.page + 1 >= commentPage.totalPages) return;
    setCommentsLoadingMore(true);
    try {
      const result = await fetchCommunityComments(postId, commentPage.page + 1, 20);
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
      setCommentMessage("댓글을 더 불러오지 못했습니다.");
    } finally {
      setCommentsLoadingMore(false);
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

        {!loading && !error && post && (
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
            </header>

            <p className="whitespace-pre-wrap text-base leading-relaxed text-gray-800">{post.content}</p>

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
                disabled={mutationLoading || (isAuthenticated && state === null)}
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
                disabled={mutationLoading || (isAuthenticated && state === null)}
                onClick={handleSave}
                className={`ml-auto rounded-full px-4 py-2 text-sm font-semibold ${state?.saved ? "bg-purple-50 text-purple-700" : "bg-gray-50 text-gray-700"}`}
              >
                <i className={state?.saved ? "ri-bookmark-fill" : "ri-bookmark-line"} /> 저장
              </button>
            </div>

            {stateError && <p className="mt-3 text-sm text-red-600">{stateError}</p>}

            <section className="mt-7">
              <h2 className="mb-4 text-lg font-bold text-gray-900">댓글 {commentPage.totalElements}</h2>

              <form onSubmit={handleCommentSubmit} className="mb-6 flex gap-2">
                <label htmlFor="comment-content" className="sr-only">댓글 내용</label>
                <input
                  id="comment-content"
                  value={commentText}
                  onChange={(event) => setCommentText(event.target.value)}
                  placeholder="댓글을 입력하세요"
                  maxLength={1000}
                  className="min-w-0 flex-1 rounded-full border border-gray-200 px-4 py-2.5 outline-none focus:border-purple-500"
                />
                <button
                  type="submit"
                  disabled={!commentText.trim() || commentSubmitting}
                  className="rounded-full bg-purple-600 px-4 py-2.5 text-sm font-semibold text-white disabled:opacity-40"
                >
                  {commentSubmitting ? "저장 중" : "댓글 등록"}
                </button>
              </form>

              {commentMessage && <p className="mb-4 text-sm text-gray-700">{commentMessage}</p>}

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
                        <time className="text-xs text-gray-500">{formatTime(comment.createdAt)}</time>
                      </div>
                      <p className="whitespace-pre-wrap text-sm leading-relaxed text-gray-700">{comment.content}</p>
                    </article>
                  ))}
                </div>
              )}

              {commentPage.page + 1 < commentPage.totalPages && (
                <button
                  type="button"
                  onClick={loadMoreComments}
                  disabled={commentsLoadingMore}
                  className="mt-4 w-full rounded-xl border border-gray-200 py-3 text-sm font-semibold text-gray-700"
                >
                  {commentsLoadingMore ? "불러오는 중" : "댓글 더 보기"}
                </button>
              )}
            </section>
          </article>
        )}
      </PageWrapper>
    </>
  );
}
