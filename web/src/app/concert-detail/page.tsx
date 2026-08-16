"use client";

import PageHeader from "@/components/layout/PageHeader";
import PageWrapper from "@/components/layout/PageWrapper";
import { fetchConcert, type Concert } from "@/lib/api/concert";
import { useSearchParams } from "next/navigation";
import { useEffect, useState } from "react";

function formatDateRange(startDate: string, endDate: string): string {
  const format = (value: string) => new Intl.DateTimeFormat("ko-KR", {
    year: "numeric", month: "long", day: "numeric", timeZone: "Asia/Seoul",
  }).format(new Date(`${value}T00:00:00+09:00`));
  return startDate === endDate ? format(startDate) : `${format(startDate)} ~ ${format(endDate)}`;
}

export default function ConcertDetailPage() {
  const searchParams = useSearchParams();
  const id = searchParams.get("id");
  const [concert, setConcert] = useState<Concert | null>(null);
  const [loading, setLoading] = useState(Boolean(id));
  const [error, setError] = useState<string | null>(null);
  const [resolvedId, setResolvedId] = useState<string | null>(null);
  const [retryKey, setRetryKey] = useState(0);

  useEffect(() => {
    if (!id) return;
    const controller = new AbortController();
    queueMicrotask(() => {
      if (controller.signal.aborted) return;
      setLoading(true); setError(null); setConcert(null); setResolvedId(null);
    });
    const load = async () => {
      try {
        const value = await fetchConcert(id, controller.signal);
        if (!controller.signal.aborted) {
          setConcert(value);
          setResolvedId(id);
        }
      } catch {
        if (!controller.signal.aborted) {
          setError("공연 상세 정보를 불러오지 못했습니다.");
          setResolvedId(id);
        }
      } finally {
        if (!controller.signal.aborted) setLoading(false);
      }
    };
    void load();
    return () => controller.abort();
  }, [id, retryKey]);

  return (
    <>
      <PageHeader title="공연 상세" />
      <PageWrapper className="pb-24">
        {!id ? (
          <div role="alert" className="py-16 text-center text-gray-700">공연 ID가 올바르지 않습니다.</div>
        ) : loading || resolvedId !== id ? (
          <div role="status" className="py-16 text-center text-gray-500">공연 상세 정보를 불러오는 중입니다.</div>
        ) : error || !concert ? (
          <div role="alert" className="py-16 text-center">
            <p className="text-gray-700">{error ?? "공연 상세 정보를 불러오지 못했습니다."}</p>
            {id && <button onClick={() => setRetryKey((value) => value + 1)} className="mt-4 px-5 py-2 rounded-full bg-purple-600 text-white">다시 시도</button>}
          </div>
        ) : (
          <article>
            {concert.posterUrl ? (
              // eslint-disable-next-line @next/next/no-img-element
              <img src={concert.posterUrl} alt={concert.title} referrerPolicy="no-referrer" className="w-full h-80 object-cover object-top" />
            ) : (
              <div className="h-56 bg-gray-100 flex items-center justify-center text-gray-400" aria-label="포스터 없음"><i className="ri-image-line text-4xl" /></div>
            )}
            <div className="px-4 py-6 space-y-6">
              <header>
                <p className="text-sm font-medium text-purple-600">{concert.status}</p>
                <h1 className="mt-1 text-2xl font-bold text-gray-900">{concert.title}</h1>
                {concert.artist && <p className="mt-2 text-gray-600">{concert.artist}</p>}
              </header>
              <section className="rounded-2xl bg-gray-50 p-5">
                <h2 className="font-bold text-gray-900">공연 정보</h2>
                <dl className="mt-4 space-y-3 text-sm">
                  <div><dt className="text-gray-500">기간</dt><dd className="mt-1 text-gray-900">{formatDateRange(concert.startDate, concert.endDate)}</dd></div>
                  {concert.performanceTime && <div><dt className="text-gray-500">시간</dt><dd className="mt-1 text-gray-900">{concert.performanceTime}</dd></div>}
                  {concert.venue && <div><dt className="text-gray-500">장소</dt><dd className="mt-1 text-gray-900">{concert.venue}</dd></div>}
                  {concert.venueAddress && <div><dt className="text-gray-500">주소</dt><dd className="mt-1 text-gray-900">{concert.venueAddress}</dd></div>}
                  <div><dt className="text-gray-500">가격</dt><dd className="mt-1 text-gray-900">{concert.priceText ?? "가격 정보 없음"}</dd></div>
                  {concert.runtime && <div><dt className="text-gray-500">관람시간</dt><dd className="mt-1 text-gray-900">{concert.runtime}</dd></div>}
                  {concert.ageRating && <div><dt className="text-gray-500">관람연령</dt><dd className="mt-1 text-gray-900">{concert.ageRating}</dd></div>}
                  {concert.performers && <div><dt className="text-gray-500">출연진</dt><dd className="mt-1 text-gray-900">{concert.performers}</dd></div>}
                </dl>
              </section>
              <p className="text-xs leading-relaxed text-gray-500">공연 정보와 예매처는 KOPIS 공식 상세 페이지에서 다시 확인해 주세요.</p>
            </div>
            <div className="fixed bottom-0 left-0 right-0 z-40 border-t border-gray-200 bg-white px-4 py-4">
              <div className="mx-auto max-w-4xl">
                <a href={concert.ticketUrl} target="_blank" rel="noopener noreferrer" className="block w-full rounded-full bg-purple-600 py-4 text-center font-bold text-white">KOPIS 공식 정보 확인</a>
              </div>
            </div>
          </article>
        )}
      </PageWrapper>
    </>
  );
}
