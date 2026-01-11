"use client";

import PageHeader from "@/components/layout/PageHeader";
import PageWrapper from "@/components/layout/PageWrapper";
import Skeleton from "@/components/ui/Skeleton";
// import { useSearchParams } from "next/navigation";

export default function PostDetailPage() {
  // const searchParams = useSearchParams();
  // const id = searchParams.get('id');

  return (
    <>
      <PageHeader title="Post Detail" />
      <PageWrapper>
        <div className="max-w-3xl mx-auto bg-white min-h-screen pb-20">
          {/* Post Content Skeleton */}
          <div data-testid="post-content-skeleton" className="p-4 space-y-4 border-b border-gray-100">
            {/* User Info */}
            <div className="flex items-center gap-3">
              <Skeleton className="w-10 h-10 rounded-full" variant="circle" />
              <div className="space-y-1">
                <Skeleton className="w-32 h-4" />
                <Skeleton className="w-24 h-3" />
              </div>
            </div>

            {/* Content */}
            <div className="space-y-2 py-2">
              <Skeleton className="w-full h-4" />
              <Skeleton className="w-full h-4" />
              <Skeleton className="w-2/3 h-4" />
            </div>

            {/* Image */}
            <Skeleton className="w-full h-64 rounded-xl" />

            {/* Stats */}
            <div className="flex gap-4 pt-2">
              <Skeleton className="w-16 h-4" />
              <Skeleton className="w-16 h-4" />
              <Skeleton className="w-16 h-4" />
            </div>
          </div>

          {/* Comments Skeleton */}
          <div data-testid="comments-skeleton" className="p-4 space-y-6">
            <Skeleton className="w-24 h-6" />
            {[1, 2, 3].map((idx) => (
              <div key={idx} className="flex gap-3">
                <Skeleton className="w-8 h-8 rounded-full flex-shrink-0" variant="circle" />
                <div className="flex-1 space-y-2">
                  <Skeleton className="w-32 h-3" />
                  <Skeleton className="w-full h-4" />
                  <Skeleton className="w-24 h-3" />
                </div>
              </div>
            ))}
          </div>
        </div>
      </PageWrapper>
    </>
  );
}
