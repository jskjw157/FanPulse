"use client";

import PageHeader from "@/components/layout/PageHeader";
import PageWrapper from "@/components/layout/PageWrapper";
import { fetchConcerts, type Concert } from "@/lib/api/concert";
import Link from "next/link";
import { useEffect, useState } from "react";

function formatDateRange(startDate: string, endDate: string): string {
  const format = (value: string) => new Intl.DateTimeFormat("ko-KR", {
    year: "numeric", month: "long", day: "numeric", timeZone: "Asia/Seoul",
  }).format(new Date(`${value}T00:00:00+09:00`));
  return startDate === endDate ? format(startDate) : `${format(startDate)} ~ ${format(endDate)}`;
}

export default function ConcertPage() {
  const [concerts, setConcerts] = useState<Concert[]>([]);
  const [page, setPage] = useState(0);
  const [last, setLast] = useState(true);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [retryKey, setRetryKey] = useState(0);

  useEffect(() => {
    const controller = new AbortController();
    setLoading(true); setError(null); setConcerts([]);
    const load = async () => {
      try {
        const result = await fetchConcerts(0, 20, controller.signal);
        if (controller.signal.aborted) return;
        setConcerts(result.items); setPage(result.page); setLast(result.last);
      } catch {
        if (!controller.signal.aborted) setError("공연 정보를 불러오지 못했습니다.");
      } finally {
        if (!controller.signal.aborted) setLoading(false);
      }
    };
    void load();
    return () => controller.abort();
  }, [retryKey]);

  const loadMore = async () => {
    if (last || loadingMore) return;
    setLoadingMore(true); setError(null);
    try {
      const next = await fetchConcerts(page + 1, 20);
      setConcerts((current) => {
        const ids = new Set(current.map((item) => item.id));
        return [...current, ...next.items.filter((item) => !ids.has(item.id))];
      });
      setPage(next.page); setLast(next.last);
    } catch {
      setError("공연 정보를 불러오지 못했습니다.");
    } finally {
      setLoadingMore(false);
    }
  };

  return (
    <>
      <PageHeader title="공연" />
      <PageWrapper>
        <div className="max-w-7xl mx-auto px-4 lg:px-8 py-6">
          {loading ? (
            <div role="status" className="py-16 text-center text-gray-500">공연 정보를 불러오는 중입니다.</div>
          ) : error && concerts.length === 0 ? (
            <div role="alert" className="py-16 text-center">
              <p className="text-gray-700">{error}</p>
              <button onClick={() => setRetryKey((value) => value + 1)} className="mt-4 px-5 py-2 rounded-full bg-purple-600 text-white">다시 시도</button>
            </div>
          ) : concerts.length === 0 ? (
            <div className="py-16 text-center text-gray-500">예정된 공연이 없습니다.</div>
          ) : (
            <>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {concerts.map((concert) => (
                  <article key={concert.id} className="bg-white rounded-2xl overflow-hidden shadow-sm border border-gray-100">
                    {concert.posterUrl ? (
                      // eslint-disable-next-line @next/next/no-img-element
                      <img src={concert.posterUrl} alt={concert.title} referrerPolicy="no-referrer" className="w-full h-56 object-cover object-top" />
                    ) : (
                      <div className="h-56 bg-gray-100 flex items-center justify-center text-gray-400" aria-label="포스터 없음">
                        <i className="ri-image-line text-4xl" />
                      </div>
                    )}
                    <div className="p-5">
                      <p className="text-xs font-medium text-purple-600">{concert.status}</p>
                      <h2 className="mt-1 font-bold text-gray-900 text-lg leading-snug">{concert.title}</h2>
                      {concert.artist && <p className="text-sm text-gray-600 mt-1">{concert.artist}</p>}
                      <dl className="mt-4 space-y-2 text-sm text-gray-600">
                        <div className="flex gap-2"><dt><i className="ri-calendar-line" /></dt><dd>{formatDateRange(concert.startDate, concert.endDate)}</dd></div>
                        {concert.venue && <div className="flex gap-2"><dt><i className="ri-map-pin-line" /></dt><dd>{concert.venue}</dd></div>}
                        <div className="flex gap-2"><dt><i className="ri-ticket-line" /></dt><dd>{concert.priceText ?? "가격 정보 없음"}</dd></div>
                      </dl>
                      <Link aria-label={`${concert.title} 상세 보기`} href={`/concert-detail?id=${concert.id}`} className="mt-5 block w-full text-center bg-purple-600 text-white font-medium py-3 rounded-full">상세 보기</Link>
                    </div>
                  </article>
                ))}
              </div>
              {error && <p role="alert" className="mt-5 text-center text-red-600">{error}</p>}
              {!last && <button onClick={() => void loadMore()} disabled={loadingMore} className="mx-auto mt-8 block px-7 py-3 rounded-full border border-purple-600 text-purple-600 disabled:opacity-50">{loadingMore ? "불러오는 중" : "더 보기"}</button>}
              <p className="mt-8 text-center text-xs text-gray-400">공연 정보 출처: KOPIS</p>
            </>
          )}
        </div>
      </PageWrapper>
    </>
  );
}
