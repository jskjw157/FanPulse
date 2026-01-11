"use client";

import PageHeader from "@/components/layout/PageHeader";
import PageWrapper from "@/components/layout/PageWrapper";
import Skeleton from "@/components/ui/Skeleton";
import { useSearchParams } from "next/navigation";

export default function ArtistDetailPage() {
  const searchParams = useSearchParams();
  const artist = searchParams.get('artist');

  return (
    <>
      <PageHeader title="Artist Profile" />
      <PageWrapper>
        <div data-testid="artist-profile-skeleton" className="min-h-screen bg-white pb-20">
          {/* Cover Image */}
          <Skeleton className="w-full h-48 md:h-64 rounded-none" />
          
          <div className="max-w-4xl mx-auto px-4 lg:px-8 -mt-16 relative">
            {/* Profile Header */}
            <div className="flex flex-col items-center">
              <div className="p-1 bg-white rounded-full">
                <Skeleton className="w-32 h-32 rounded-full" variant="circle" />
              </div>
              <div className="mt-4 text-center space-y-2">
                <Skeleton className="w-40 h-6 mx-auto" />
                <Skeleton className="w-24 h-4 mx-auto" />
              </div>
              
              {/* Stats */}
              <div className="flex gap-8 mt-6">
                <div className="text-center space-y-1">
                  <Skeleton className="w-12 h-6 mx-auto" />
                  <Skeleton className="w-16 h-3" />
                </div>
                <div className="text-center space-y-1">
                  <Skeleton className="w-12 h-6 mx-auto" />
                  <Skeleton className="w-16 h-3" />
                </div>
              </div>

              {/* Action Button */}
              <Skeleton className="w-full max-w-xs h-12 rounded-full mt-6" />
            </div>

            {/* Content Tabs */}
            <div className="mt-8 border-b border-gray-100 flex gap-6 justify-center">
              <Skeleton className="w-16 h-8" />
              <Skeleton className="w-16 h-8" />
              <Skeleton className="w-16 h-8" />
            </div>

            {/* Tab Content */}
            <div className="py-6 grid grid-cols-2 md:grid-cols-3 gap-4">
              {[1, 2, 3, 4, 5, 6].map((idx) => (
                <Skeleton key={idx} className="w-full h-40 rounded-xl" />
              ))}
            </div>
          </div>
        </div>
      </PageWrapper>
    </>
  );
}
