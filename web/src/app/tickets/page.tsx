"use client";

import PageHeader from "@/components/layout/PageHeader";
import PageWrapper from "@/components/layout/PageWrapper";
import { fetchConcerts, type Concert } from "@/lib/api/concert";
import { useEffect, useState } from "react";

function formatDate(value: string): string {
  return new Intl.DateTimeFormat("ko-KR", {
    year: "numeric",
    month: "long",
    day: "numeric",
    timeZone: "Asia/Seoul",
  }).format(new Date(`${value}T00:00:00+09:00`));
}

export default function TicketsPage() {
  const [concerts, setConcerts] = useState<Concert[]>([]);
  const [page, setPage] = useState(0);
  const [last, setLast] = useState(true);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [retryKey, setRetryKey] = useState(0);

  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);
    setError(null);
    setConcerts([]);
    const load = async () => {
      try {
        const result = await fetchConcerts(0, 20, controller.signal);
        if (controller.signal.aborted) return;
        setConcerts(result.items);
        setPage(result.page);
        setLast(result.last);
      } catch {
        if (!controller.signal.aborted) setError("티켓 정보를 불러오지 못했습니다.");
      } finally {
        if (!controller.signal.aborted) setLoading(false);
      }
    };
    void load();
    return () => controller.abort();
  }, [retryKey]);

  const loadMore = async () => {
    if (last || loadingMore) return;
    setLoadingMore(true);
    setError(null);
    try {
      const result = await fetchConcerts(page + 1, 20);
      setConcerts((current) => {
        const ids = new Set(current.map((concert) => concert.id));
        return [...current, ...result.items.filter((concert) => !ids.has(concert.id))];
      });
      setPage(result.page);
      setLast(result.last);
    } catch {
      setError("티켓 정보를 더 불러오지 못했습니다.");
    } finally {
      setLoadingMore(false);
    }
  };

  return (
    <>
      <PageHeader title="티켓 정보" />
      <PageWrapper>
        <div className="mx-auto max-w-5xl px-4 py-6 lg:px-8">
          {loading ? (
            <p role="status" className="py-16 text-center text-gray-500">티켓 정보를 불러오는 중입니다.</p>
          ) : error && concerts.length === 0 ? (
            <div role="alert" className="py-16 text-center">
              <p className="text-gray-700">{error}</p>
              <button onClick={() => setRetryKey((value) => value + 1)} className="mt-4 rounded-full bg-purple-600 px-5 py-2 text-white">다시 시도</button>
            </div>
          ) : concerts.length === 0 ? (
            <p className="py-16 text-center text-gray-500">현재 확인할 수 있는 티켓 정보가 없습니다.</p>
          ) : (
            <>
              <div className="space-y-4">
                {concerts.map((concert) => (
                  <article key={concert.id} className="flex gap-4 rounded-2xl border border-gray-100 bg-white p-4 shadow-sm">
                    {concert.posterUrl ? (
                      // eslint-disable-next-line @next/next/no-img-element
                      <img src={concert.posterUrl} alt={concert.title} referrerPolicy="no-referrer" className="h-32 w-24 rounded-xl object-cover object-top" />
                    ) : (
                      <div aria-label="포스터 없음" className="flex h-32 w-24 shrink-0 items-center justify-center rounded-xl bg-gray-100 text-gray-400"><i className="ri-image-line text-3xl" /></div>
                    )}
                    <div className="min-w-0 flex-1">
                      <p className="text-xs font-medium text-purple-600">{concert.status}</p>
                      <h2 className="mt-1 font-bold text-gray-900">{concert.title}</h2>
                      {concert.artist && <p className="mt-1 text-sm text-gray-600">{concert.artist}</p>}
                      <p className="mt-3 text-sm text-gray-600">{formatDate(concert.startDate)}{concert.startDate !== concert.endDate && ` ~ ${formatDate(concert.endDate)}`}</p>
                      {concert.venue && <p className="mt-1 text-sm text-gray-600">{concert.venue}</p>}
                      <p className="mt-1 text-sm text-gray-600">{concert.priceText ?? "가격 정보 없음"}</p>
                      <a href={concert.ticketUrl} target="_blank" rel="noopener noreferrer" className="mt-4 inline-flex rounded-full bg-purple-600 px-5 py-2 text-sm font-medium text-white">KOPIS 공식 정보 보기</a>
                    </div>
                  </article>
                ))}
              </div>
              {error && <p role="alert" className="mt-5 text-center text-red-600">{error}</p>}
              {!last && <button onClick={() => void loadMore()} disabled={loadingMore} className="mx-auto mt-8 block rounded-full border border-purple-600 px-7 py-3 text-purple-600 disabled:opacity-50">{loadingMore ? "불러오는 중" : "더 보기"}</button>}
              <p className="mt-8 text-center text-xs text-gray-500">FanPulse는 티켓을 판매하거나 예약하지 않습니다.</p>
              <p className="mt-2 text-center text-xs text-gray-400">공연·티켓 정보 출처: KOPIS</p>
            </>
          )}
        </div>
      </PageWrapper>
    </>
  );
}
