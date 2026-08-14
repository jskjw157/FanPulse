"use client";

import ProtectedRoute from "@/components/auth/ProtectedRoute";
import PageHeader from "@/components/layout/PageHeader";
import PageWrapper from "@/components/layout/PageWrapper";
import {
  fetchSavedCommunityPosts,
  unsaveCommunityPost,
  type CommunityPost,
} from "@/lib/api/community";
import Link from "next/link";
import { useEffect, useState } from "react";

function formatTime(value: string) {
  return new Intl.DateTimeFormat("ko-KR", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(value));
}

export default function SavedPage() {
  const [posts, setPosts] = useState<CommunityPost[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [unsavingId, setUnsavingId] = useState<string | null>(null);
  const [retryKey, setRetryKey] = useState(0);

  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);
    setError(null);
    setPosts([]);
    fetchSavedCommunityPosts(0, 20, controller.signal)
      .then((result) => {
        if (controller.signal.aborted) return;
        setPosts(result.items);
        setPage(result.page);
        setTotalPages(result.totalPages);
        setTotalElements(result.totalElements);
      })
      .catch(() => {
        if (!controller.signal.aborted) setError("저장한 게시글을 불러오지 못했습니다.");
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false);
      });
    return () => controller.abort();
  }, [retryKey]);

  const handleUnsave = async (postId: string) => {
    if (unsavingId) return;
    setUnsavingId(postId);
    setActionError(null);
    try {
      const result = await unsaveCommunityPost(postId);
      if (result.saved) throw new Error("unsave failed");
      setPosts((current) => current.filter((post) => post.id !== postId));
      setTotalElements((current) => Math.max(0, current - 1));
    } catch {
      setActionError("저장을 취소하지 못했습니다.");
    } finally {
      setUnsavingId(null);
    }
  };

  const loadMore = async () => {
    if (loadingMore || page + 1 >= totalPages) return;
    setLoadingMore(true);
    setActionError(null);
    try {
      const result = await fetchSavedCommunityPosts(page + 1, 20);
      setPosts((current) => {
        const seen = new Set(current.map((post) => post.id));
        return [...current, ...result.items.filter((post) => !seen.has(post.id))];
      });
      setPage(result.page);
      setTotalPages(result.totalPages);
      setTotalElements(result.totalElements);
    } catch {
      setActionError("저장한 게시글을 더 불러오지 못했습니다.");
    } finally {
      setLoadingMore(false);
    }
  };

  return (
    <ProtectedRoute>
      <PageHeader title="저장한 게시물" showBack />
      <PageWrapper className="mx-auto max-w-3xl px-4 py-6">
        {loading && <p className="py-16 text-center text-gray-500">저장한 게시글을 불러오는 중입니다.</p>}

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

        {!loading && !error && (
          <>
            <div className="mb-5 rounded-2xl bg-gradient-to-r from-purple-600 to-pink-600 p-5 text-white shadow-lg">
              <p className="text-sm text-white/80">저장한 게시글</p>
              <p className="mt-1 text-3xl font-bold">{totalElements.toLocaleString()}</p>
            </div>

            {actionError && <p className="mb-4 text-sm text-red-600">{actionError}</p>}

            {posts.length === 0 ? (
              <p className="rounded-2xl bg-white py-16 text-center text-gray-500 shadow-sm">
                저장한 게시글이 없습니다.
              </p>
            ) : (
              <div className="space-y-4">
                {posts.map((post) => (
                  <article key={post.id} className="rounded-2xl border border-gray-100 bg-white p-5 shadow-sm">
                    <header className="mb-3 flex items-center gap-3">
                      <div
                        aria-hidden="true"
                        className="flex h-10 w-10 items-center justify-center rounded-full bg-purple-100 font-bold text-purple-700"
                      >
                        {post.author.name.slice(0, 1).toUpperCase()}
                      </div>
                      <div className="min-w-0 flex-1">
                        <p className="truncate font-semibold text-gray-900">{post.author.name}</p>
                        <p className="text-xs text-gray-500">{formatTime(post.createdAt)}</p>
                      </div>
                      <button
                        type="button"
                        aria-label="저장 취소"
                        onClick={() => handleUnsave(post.id)}
                        disabled={unsavingId === post.id}
                        className="rounded-full bg-purple-50 px-3 py-2 text-sm font-semibold text-purple-700 disabled:opacity-40"
                      >
                        <i className="ri-bookmark-fill" /> 저장 취소
                      </button>
                    </header>

                    <Link href={`/post-detail?id=${post.id}`} aria-label={`${post.content} 상세 보기`}>
                      <p className="whitespace-pre-wrap leading-relaxed text-gray-800">{post.content}</p>
                      {post.imageUrl && (
                        // eslint-disable-next-line @next/next/no-img-element -- validated external HTTPS URL must bypass Next host allowlists
                        <img
                          src={post.imageUrl}
                          alt="게시글 첨부 이미지"
                          referrerPolicy="no-referrer"
                          className="mt-4 max-h-96 w-full rounded-xl object-cover"
                        />
                      )}
                      <div className="mt-4 flex gap-4 text-sm text-gray-500">
                        <span><i className="ri-heart-line" /> {post.likeCount.toLocaleString()}</span>
                        <span><i className="ri-chat-3-line" /> {post.commentCount.toLocaleString()}</span>
                      </div>
                    </Link>
                  </article>
                ))}
              </div>
            )}

            {page + 1 < totalPages && (
              <button
                type="button"
                onClick={loadMore}
                disabled={loadingMore}
                className="mt-5 w-full rounded-xl border border-gray-200 bg-white py-3 font-semibold text-gray-700"
              >
                {loadingMore ? "불러오는 중" : "더 보기"}
              </button>
            )}
          </>
        )}
      </PageWrapper>
    </ProtectedRoute>
  );
}
