"use client";

import ProtectedRoute from "@/components/auth/ProtectedRoute";
import PageWrapper from "@/components/layout/PageWrapper";
import { fetchActiveArtists, type ArtistSummary } from "@/lib/api/artist";
import { createCommunityPost } from "@/lib/api/community";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

export default function PostCreatePage() {
  const router = useRouter();
  const [content, setContent] = useState("");
  const [selectedArtistId, setSelectedArtistId] = useState<string | null>(null);
  const [artists, setArtists] = useState<ArtistSummary[]>([]);
  const [artistsLoading, setArtistsLoading] = useState(true);
  const [artistError, setArtistError] = useState(false);
  const [artistRetryKey, setArtistRetryKey] = useState(0);
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState(false);

  useEffect(() => {
    const controller = new AbortController();
    setArtistsLoading(true);
    setArtistError(false);
    fetchActiveArtists(controller.signal)
      .then(setArtists)
      .catch(() => {
        if (!controller.signal.aborted) {
          setArtists([]);
          setArtistError(true);
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) setArtistsLoading(false);
      });
    return () => controller.abort();
  }, [artistRetryKey]);

  const canSubmit = content.trim().length > 0 && selectedArtistId !== null && !submitting;

  const handlePost = async () => {
    if (!canSubmit) return;
    setSubmitting(true);
    setSubmitError(false);
    try {
      const post = await createCommunityPost({
        artistId: selectedArtistId,
        content: content.trim(),
      });
      router.push(`/post-detail?id=${post.id}`);
    } catch {
      setSubmitError(true);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <ProtectedRoute>
      <header className="fixed left-0 right-0 top-0 z-50 border-b border-gray-200 bg-white lg:static lg:z-auto lg:border-none lg:bg-transparent lg:pb-4 lg:pt-8">
        <div className="flex h-16 items-center justify-between px-4 py-3 lg:mx-auto lg:h-auto lg:max-w-4xl lg:px-0">
          <Link href="/community" className="text-sm text-gray-600 transition-colors hover:text-gray-900 lg:rounded-lg lg:bg-gray-100 lg:px-4 lg:py-2 lg:text-base">
            취소
          </Link>
          <h1 className="text-base font-bold text-gray-900 lg:ml-8 lg:flex-1 lg:text-3xl">게시글 작성</h1>
          <button
            onClick={handlePost}
            disabled={!canSubmit}
            className={`text-sm font-medium transition-colors lg:rounded-lg lg:px-6 lg:py-2 lg:text-base ${
              canSubmit
                ? "text-purple-600 hover:text-purple-700 lg:bg-purple-600 lg:text-white lg:hover:bg-purple-700"
                : "cursor-not-allowed text-gray-400 lg:bg-gray-200 lg:text-gray-500"
            }`}
          >
            {submitting ? "등록 중" : "게시"}
          </button>
        </div>
      </header>

      <PageWrapper>
        <div className="mx-auto max-w-4xl px-4 pb-6">
          <div className="py-4">
            <p className="mb-3 block text-sm font-bold text-gray-900">아티스트 선택 *</p>
            {artistsLoading && <p role="status" className="text-sm text-gray-500">아티스트를 불러오는 중입니다.</p>}
            {!artistsLoading && artistError && (
              <div className="rounded-xl border border-red-100 bg-red-50 p-4">
                <p className="text-sm text-red-700">아티스트를 불러오지 못했습니다.</p>
                <button
                  onClick={() => setArtistRetryKey((key) => key + 1)}
                  className="mt-3 rounded-lg bg-white px-3 py-2 text-xs font-medium text-purple-700"
                >
                  다시 시도
                </button>
              </div>
            )}
            {!artistsLoading && !artistError && artists.length === 0 && (
              <p className="text-sm text-gray-500">선택 가능한 아티스트가 없습니다.</p>
            )}
            {!artistsLoading && !artistError && artists.length > 0 && (
              <div className="flex flex-wrap gap-2">
                {artists.map((artist) => (
                  <button
                    key={artist.id}
                    onClick={() => setSelectedArtistId(artist.id)}
                    aria-pressed={selectedArtistId === artist.id}
                    className={`rounded-full px-4 py-2 text-sm font-medium transition-colors ${
                      selectedArtistId === artist.id
                        ? "bg-gradient-to-r from-purple-600 to-pink-600 text-white shadow-md"
                        : "bg-gray-100 text-gray-700 hover:bg-gray-200"
                    }`}
                  >
                    {artist.name}
                  </button>
                ))}
              </div>
            )}
          </div>

          <div className="py-4">
            <label htmlFor="community-content" className="mb-3 block text-sm font-bold text-gray-900">
              내용 *
            </label>
            <textarea
              id="community-content"
              value={content}
              onChange={(event) => setContent(event.target.value)}
              placeholder="팬 여러분과 공유하고 싶은 이야기를 작성해주세요..."
              maxLength={5000}
              className="h-48 w-full resize-none rounded-2xl border-none bg-gray-50 px-4 py-3 text-sm transition-all focus:outline-none focus:ring-2 focus:ring-purple-600"
            />
            <div className="mt-2 flex justify-end">
              <span className="text-xs text-gray-500">{content.length}/5000</span>
            </div>
          </div>


          {submitError && (
            <p role="alert" className="rounded-xl bg-red-50 p-4 text-sm text-red-700">
              게시글을 등록하지 못했습니다. 내용을 확인해 주세요.
            </p>
          )}

          <div className="mt-6 rounded-2xl bg-purple-50 p-4">
            <div className="flex items-start gap-2">
              <i className="ri-information-line mt-0.5 flex-shrink-0 text-lg text-purple-600" />
              <div>
                <h3 className="mb-2 text-sm font-bold text-purple-900">게시글 작성 가이드</h3>
                <ul className="space-y-1 text-xs text-purple-700">
                  <li>• 타인을 존중하는 내용을 작성해주세요</li>
                  <li>• 욕설, 비방, 허위사실은 등록이 거부될 수 있습니다</li>
                  <li>• 저작권을 침해하는 콘텐츠는 게시할 수 없습니다</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </PageWrapper>
    </ProtectedRoute>
  );
}
