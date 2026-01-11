"use client";

import PageHeader from "@/components/layout/PageHeader";
import PageWrapper from "@/components/layout/PageWrapper";
import Skeleton from "@/components/ui/Skeleton";
// import { useSearchParams } from "next/navigation";

export default function NewsDetailPage() {
  // const searchParams = useSearchParams();
  // const id = searchParams.get('id');

  return (
    <>
      <PageHeader 
        title="News Detail" 
        rightAction={
          <div className="flex gap-2">
            <button className="w-9 h-9 flex items-center justify-center rounded-full hover:bg-gray-100">
              <i className="ri-share-line text-xl text-gray-700"></i>
            </button>
            <button className="w-9 h-9 flex items-center justify-center rounded-full hover:bg-gray-100">
              <i className="ri-bookmark-line text-xl text-gray-700"></i>
            </button>
          </div>
        }
      />
      <PageWrapper>
        <div data-testid="news-content-skeleton" className="max-w-3xl mx-auto bg-white min-h-screen pb-20">
          {/* Hero Image Skeleton */}
          <Skeleton className="w-full h-64 md:h-80 rounded-none" />
          
          <div className="px-4 py-6 space-y-6">
            {/* Meta Info */}
            <div className="flex items-center gap-2">
              <Skeleton className="w-16 h-6 rounded-full" />
              <Skeleton className="w-24 h-4" />
            </div>

            {/* Title */}
            <Skeleton className="w-full h-10" />
            
            {/* Author */}
            <div className="flex items-center gap-3 py-4 border-b border-gray-100">
              <Skeleton className="w-10 h-10 rounded-full" variant="circle" />
              <div className="space-y-1">
                <Skeleton className="w-24 h-4" />
                <Skeleton className="w-16 h-3" />
              </div>
            </div>

            {/* Body Text */}
            <div className="space-y-4">
              <Skeleton className="w-full h-4" />
              <Skeleton className="w-full h-4" />
              <Skeleton className="w-3/4 h-4" />
              <Skeleton className="w-full h-4" />
              <Skeleton className="w-5/6 h-4" />
            </div>

            {/* Action Buttons */}
            <div className="flex gap-3 pt-6">
              <Skeleton className="flex-1 h-12 rounded-xl" />
              <Skeleton className="flex-1 h-12 rounded-xl" />
            </div>
          </div>
        </div>
      </PageWrapper>
    </>
  );
}
