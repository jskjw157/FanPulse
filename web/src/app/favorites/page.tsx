'use client';

import Link from 'next/link';
import ProtectedRoute from '@/components/auth/ProtectedRoute';
import PageHeader from '@/components/layout/PageHeader';
import PageWrapper from '@/components/layout/PageWrapper';
import { useFavorites } from '@/hooks/useFavorites';

function FavoritesContent() {
  const { favorites, state, error, retry, unfollow, mutatingId } = useFavorites();

  return (
    <div className="min-h-screen bg-gray-50">
      <PageHeader title="좋아요한 아티스트" />
      <PageWrapper>
        <main className="mx-auto max-w-3xl px-4 py-6">
          {state === 'loading' && (
            <p className="py-16 text-center text-gray-500">즐겨찾기를 불러오는 중입니다</p>
          )}

          {state === 'error' && (
            <div className="py-16 text-center">
              <p className="text-gray-600">{error}</p>
              <button
                type="button"
                onClick={() => void retry()}
                className="mt-4 rounded-full bg-purple-600 px-5 py-2 text-sm font-semibold text-white"
              >
                다시 시도
              </button>
            </div>
          )}

          {state === 'success' && favorites.length === 0 && (
            <div className="py-16 text-center">
              <i className="ri-heart-line text-5xl text-gray-300" aria-hidden="true" />
              <p className="mt-4 text-gray-600">좋아요한 아티스트가 없습니다</p>
            </div>
          )}

          {state === 'success' && favorites.length > 0 && (
            <ul className="space-y-3">
              {favorites.map((artist) => (
                <li key={artist.id} className="flex items-center gap-4 rounded-2xl bg-white p-4 shadow-sm">
                  <Link
                    href={`/artists/${artist.id}`}
                    aria-label={artist.name}
                    className="flex min-w-0 flex-1 items-center gap-4"
                  >
                    {artist.profileImageUrl ? (
                      <img
                        src={artist.profileImageUrl}
                        alt=""
                        className="h-16 w-16 shrink-0 rounded-full object-cover"
                      />
                    ) : (
                      <div className="flex h-16 w-16 shrink-0 items-center justify-center rounded-full bg-purple-100 text-xl font-bold text-purple-700" aria-hidden="true">
                        {artist.name.slice(0, 1)}
                      </div>
                    )}
                    <div className="min-w-0">
                      <h2 className="truncate font-bold text-gray-900">{artist.name}</h2>
                      {artist.englishName && <p className="truncate text-sm text-gray-500">{artist.englishName}</p>}
                      {artist.agency && <p className="truncate text-sm text-gray-500">{artist.agency}</p>}
                    </div>
                  </Link>
                  <button
                    type="button"
                    disabled={mutatingId === artist.id}
                    onClick={() => void unfollow(artist.id)}
                    className="rounded-full border border-gray-300 px-4 py-2 text-sm font-semibold text-gray-700 disabled:opacity-50"
                    aria-label={`${artist.name} 좋아요 취소`}
                  >
                    {mutatingId === artist.id ? '처리 중' : '좋아요 취소'}
                  </button>
                </li>
              ))}
            </ul>
          )}
        </main>
      </PageWrapper>
    </div>
  );
}

export default function FavoritesPage() {
  return (
    <ProtectedRoute>
      <FavoritesContent />
    </ProtectedRoute>
  );
}
