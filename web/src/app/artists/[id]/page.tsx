'use client';

import { useParams } from 'next/navigation';
import PageHeader from '@/components/layout/PageHeader';
import PageWrapper from '@/components/layout/PageWrapper';
import { useArtistDetail } from '@/hooks/useArtistDetail';

function formatDate(value: string | null): string {
  if (!value) return '정보 없음';
  return new Intl.DateTimeFormat('ko-KR', { dateStyle: 'long' }).format(new Date(`${value}T00:00:00`));
}

export default function ArtistPage() {
  const params = useParams<{ id: string }>();
  const id = Array.isArray(params.id) ? params.id[0] : params.id;
  const { artist, state, error, retry } = useArtistDetail(id);

  return (
    <>
      <PageHeader title="Artist Profile" />
      <PageWrapper>
        <main className="mx-auto max-w-3xl px-4 py-6">
          {state === 'loading' && (
            <p className="py-16 text-center text-gray-500">아티스트 정보를 불러오는 중입니다</p>
          )}

          {state === 'error' && (
            <div className="py-16 text-center">
              <p className="text-gray-600">{error}</p>
              <button
                type="button"
                onClick={retry}
                className="mt-4 rounded-full bg-purple-600 px-6 py-2 text-white"
              >
                다시 시도
              </button>
            </div>
          )}

          {state === 'success' && artist && (
            <article className="overflow-hidden rounded-3xl bg-white shadow-sm">
              {artist.profileImageUrl ? (
                // eslint-disable-next-line @next/next/no-img-element
                <img
                  src={artist.profileImageUrl}
                  alt={artist.name}
                  className="h-72 w-full object-cover object-top"
                />
              ) : (
                <div className="flex h-56 items-center justify-center bg-gray-100 text-gray-500">
                  등록된 프로필 이미지가 없습니다
                </div>
              )}

              <div className="space-y-7 p-6">
                <header>
                  <h1 className="text-3xl font-bold text-gray-900">{artist.name}</h1>
                  {artist.englishName && artist.englishName !== artist.name && (
                    <p className="mt-1 text-gray-500">{artist.englishName}</p>
                  )}
                  <p className="mt-3 font-medium text-purple-700">
                    {artist.agency ?? '소속사 정보 없음'}
                  </p>
                </header>

                <section>
                  <h2 className="font-bold text-gray-900">소개</h2>
                  <p className="mt-2 whitespace-pre-wrap leading-7 text-gray-700">
                    {artist.description ?? '등록된 소개가 없습니다.'}
                  </p>
                </section>

                <section className="grid gap-4 sm:grid-cols-2">
                  <div className="rounded-2xl bg-purple-50 p-4">
                    <p className="text-xs text-gray-500">데뷔일</p>
                    <p className="mt-1 font-semibold text-gray-900">{formatDate(artist.debutDate)}</p>
                  </div>
                  <div className="rounded-2xl bg-purple-50 p-4">
                    <p className="text-xs text-gray-500">활동 상태</p>
                    <p className="mt-1 font-semibold text-gray-900">
                      {artist.active ? '활동 중' : '활동 종료'}
                    </p>
                  </div>
                </section>

                {artist.isGroup && (
                  <section>
                    <h2 className="font-bold text-gray-900">멤버</h2>
                    {artist.members.length > 0 ? (
                      <div className="mt-3 flex flex-wrap gap-2">
                        {artist.members.map((member) => (
                          <span
                            key={member}
                            className="rounded-full bg-purple-50 px-3 py-1.5 text-sm font-medium text-purple-700"
                          >
                            {member}
                          </span>
                        ))}
                      </div>
                    ) : (
                      <p className="mt-2 text-gray-500">등록된 멤버 정보가 없습니다.</p>
                    )}
                  </section>
                )}
              </div>
            </article>
          )}
        </main>
      </PageWrapper>
    </>
  );
}
