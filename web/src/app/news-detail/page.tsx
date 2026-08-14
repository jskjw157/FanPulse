import Link from 'next/link';
import { redirect } from 'next/navigation';

interface LegacyNewsDetailPageProps {
  searchParams: Promise<{ id?: string | string[] }>;
}

export default async function NewsDetailPage({ searchParams }: LegacyNewsDetailPageProps) {
  const rawId = (await searchParams).id;
  const id = (Array.isArray(rawId) ? rawId[0] : rawId)?.trim();

  if (id) {
    redirect(`/news/${encodeURIComponent(id)}`);
  }

  return (
    <main className="min-h-screen bg-white flex flex-col items-center justify-center px-4 text-center">
      <h1 className="text-xl font-bold text-gray-900">뉴스 ID가 필요합니다</h1>
      <p className="mt-2 text-gray-500">뉴스 목록에서 확인할 콘텐츠를 선택해 주세요.</p>
      <Link
        href="/news"
        className="mt-6 rounded-full bg-purple-600 px-6 py-2 text-white hover:bg-purple-700"
      >
        뉴스 목록으로 이동
      </Link>
    </main>
  );
}
