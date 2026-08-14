import Link from 'next/link';
import { redirect } from 'next/navigation';

interface LegacyArtistDetailPageProps {
  searchParams: Promise<{ id?: string | string[] }>;
}

export default async function ArtistDetailPage({ searchParams }: LegacyArtistDetailPageProps) {
  const rawId = (await searchParams).id;
  const id = (Array.isArray(rawId) ? rawId[0] : rawId)?.trim();

  if (id) {
    redirect(`/artists/${encodeURIComponent(id)}`);
  }

  return (
    <main className="flex min-h-screen flex-col items-center justify-center bg-white px-4 text-center">
      <h1 className="text-xl font-bold text-gray-900">아티스트 ID가 필요합니다</h1>
      <p className="mt-2 text-gray-500">검색 결과에서 확인할 아티스트를 선택해 주세요.</p>
      <Link
        href="/search"
        className="mt-6 rounded-full bg-purple-600 px-6 py-2 text-white hover:bg-purple-700"
      >
        아티스트 목록으로 이동
      </Link>
    </main>
  );
}
