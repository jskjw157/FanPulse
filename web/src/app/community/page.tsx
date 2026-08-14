"use client";

import PageHeader from "@/components/layout/PageHeader";
import PageWrapper from "@/components/layout/PageWrapper";
import { fetchCommunityPosts, type CommunityPost, type CommunitySort } from "@/lib/api/community";
import { motion } from "framer-motion";
import Link from "next/link";
import { useEffect, useRef, useState } from "react";

const PAGE_SIZE = 20;

function formatCreatedAt(value: string): string {
  return new Intl.DateTimeFormat("ko-KR", {
    year: "numeric",
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  }).format(new Date(value));
}

export default function CommunityPage() {
  const [sort, setSort] = useState<CommunitySort>("LATEST");
  const [posts, setPosts] = useState<CommunityPost[]>([]);
  const [page, setPage] = useState(0);
  const [last, setLast] = useState(true);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState(false);
  const [retryKey, setRetryKey] = useState(0);
  const generationRef = useRef(0);
  const loadMoreControllerRef = useRef<AbortController | null>(null);

  useEffect(() => {
    const controller = new AbortController();
    const generation = ++generationRef.current;
    loadMoreControllerRef.current?.abort();
    setLoading(true);
    setLoadingMore(false);
    setError(false);
    setPosts([]);
    setPage(0);
    setLast(true);

    fetchCommunityPosts(sort, 0, PAGE_SIZE, controller.signal)
      .then((result) => {
        if (generationRef.current !== generation) return;
        setPosts(result.items);
        setPage(result.page);
        setLast(result.last);
      })
      .catch(() => {
        if (!controller.signal.aborted && generationRef.current === generation) setError(true);
      })
      .finally(() => {
        if (!controller.signal.aborted && generationRef.current === generation) setLoading(false);
      });

    return () => {
      controller.abort();
      loadMoreControllerRef.current?.abort();
    };
  }, [sort, retryKey]);

  const changeSort = (nextSort: CommunitySort) => {
    if (nextSort === sort) return;
    generationRef.current += 1;
    loadMoreControllerRef.current?.abort();
    setLoadingMore(false);
    setSort(nextSort);
  };

  const loadMore = async () => {
    if (loadingMore || last) return;
    const generation = generationRef.current;
    const requestedSort = sort;
    const controller = new AbortController();
    loadMoreControllerRef.current?.abort();
    loadMoreControllerRef.current = controller;
    setLoadingMore(true);
    setError(false);
    try {
      const result = await fetchCommunityPosts(requestedSort, page + 1, PAGE_SIZE, controller.signal);
      if (controller.signal.aborted || generationRef.current !== generation || sort !== requestedSort) return;
      setPosts((current) => {
        const seen = new Set(current.map((post) => post.id));
        return [...current, ...result.items.filter((post) => !seen.has(post.id))];
      });
      setPage(result.page);
      setLast(result.last);
    } catch {
      if (!controller.signal.aborted && generationRef.current === generation) setError(true);
    } finally {
      if (generationRef.current === generation) setLoadingMore(false);
      if (loadMoreControllerRef.current === controller) loadMoreControllerRef.current = null;
    }
  };

  return (
    <>
      <PageHeader
        title="Community"
        rightAction={
          <Link
            href="/post-create"
            className="hidden items-center gap-1 rounded-lg bg-purple-600 px-4 py-2 font-medium text-white transition-colors hover:bg-purple-700 lg:flex"
          >
            <i className="ri-edit-line" />
            <span>글쓰기</span>
          </Link>
        }
      />
      <PageWrapper>
        <div className="sticky top-16 z-30 border-b border-gray-200 bg-white">
          <div className="mx-auto flex max-w-7xl">
            <button
              onClick={() => changeSort("LATEST")}
              className={`relative flex-1 py-4 text-sm font-medium transition-colors ${
                sort === "LATEST" ? "text-purple-600" : "text-gray-500"
              }`}
            >
              전체
              {sort === "LATEST" && (
                <motion.div
                  layoutId="activeTab"
                  className="absolute bottom-0 left-0 right-0 h-0.5 bg-purple-600"
                />
              )}
            </button>
            <button
              onClick={() => changeSort("POPULAR")}
              className={`relative flex-1 py-4 text-sm font-medium transition-colors ${
                sort === "POPULAR" ? "text-purple-600" : "text-gray-500"
              }`}
            >
              인기
              {sort === "POPULAR" && (
                <motion.div
                  layoutId="activeTab"
                  className="absolute bottom-0 left-0 right-0 h-0.5 bg-purple-600"
                />
              )}
            </button>
          </div>
        </div>

        <div className="mx-auto max-w-3xl space-y-4 px-4 py-6">
          {loading && (
            <div role="status" className="rounded-2xl border border-gray-100 bg-white p-8 text-center text-sm text-gray-500">
              커뮤니티를 불러오는 중입니다.
            </div>
          )}

          {!loading && error && posts.length === 0 && (
            <div className="rounded-2xl border border-red-100 bg-white p-8 text-center">
              <p className="text-sm text-gray-700">커뮤니티를 불러오지 못했습니다.</p>
              <button
                onClick={() => setRetryKey((key) => key + 1)}
                className="mt-4 rounded-lg bg-purple-600 px-4 py-2 text-sm font-medium text-white"
              >
                다시 시도
              </button>
            </div>
          )}

          {!loading && !error && posts.length === 0 && (
            <div className="rounded-2xl border border-gray-100 bg-white p-10 text-center">
              <p className="text-sm font-medium text-gray-700">아직 작성된 게시글이 없습니다.</p>
              <p className="mt-2 text-xs text-gray-500">첫 번째 이야기를 공유해 보세요.</p>
            </div>
          )}

          {posts.map((post) => (
            <Link
              key={post.id}
              href={`/post-detail?id=${post.id}`}
              aria-label={`${post.content} 상세 보기`}
              className="block overflow-hidden rounded-2xl border border-gray-100 bg-white transition-shadow hover:shadow-md"
            >
              <div className="flex items-center gap-3 p-4">
                <div
                  aria-hidden="true"
                  className="flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-full bg-purple-100 text-sm font-bold text-purple-700"
                >
                  {post.author.name.slice(0, 1).toUpperCase()}
                </div>
                <div className="min-w-0">
                  <div className="truncate text-sm font-bold text-gray-900">{post.author.name}</div>
                  <div className="mt-0.5 flex items-center gap-2">
                    {post.artist && (
                      <span className="truncate text-xs font-bold text-purple-600">{post.artist.name}</span>
                    )}
                    <span className="text-xs text-gray-400">{formatCreatedAt(post.createdAt)}</span>
                  </div>
                </div>
              </div>

              <div className="px-4 pb-3">
                <p className="whitespace-pre-wrap text-sm leading-relaxed text-gray-800">{post.content}</p>
              </div>

              {post.imageUrl && (
                // eslint-disable-next-line @next/next/no-img-element
                <img
                  src={post.imageUrl}
                  alt="게시글 첨부 이미지"
                  referrerPolicy="no-referrer"
                  className="h-64 w-full object-cover"
                />
              )}

              <div className="flex items-center gap-5 border-t border-gray-50 p-3 text-gray-500">
                <span className="flex items-center gap-1.5 text-xs font-medium">
                  <i className="ri-heart-line text-lg" />
                  {post.likeCount.toLocaleString()}
                </span>
                <span className="flex items-center gap-1.5 text-xs font-medium">
                  <i className="ri-chat-3-line text-lg" />
                  {post.commentCount.toLocaleString()}
                </span>
              </div>
            </Link>
          ))}

          {posts.length > 0 && !last && (
            <button
              onClick={loadMore}
              disabled={loadingMore}
              className="w-full rounded-xl border border-purple-200 bg-white py-3 text-sm font-medium text-purple-700 disabled:text-gray-400"
            >
              {loadingMore ? "불러오는 중" : "더 보기"}
            </button>
          )}

          {posts.length > 0 && error && (
            <p role="alert" className="text-center text-sm text-red-600">다음 게시글을 불러오지 못했습니다.</p>
          )}
        </div>

        <Link
          href="/post-create"
          aria-label="글쓰기"
          className="fixed bottom-24 right-4 flex h-14 w-14 items-center justify-center rounded-full bg-gradient-to-r from-purple-600 to-pink-600 text-white shadow-lg transition-transform hover:scale-105 lg:hidden"
        >
          <i className="ri-add-line text-2xl" />
        </Link>
      </PageWrapper>
    </>
  );
}
