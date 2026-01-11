"use client";

import PageHeader from "@/components/layout/PageHeader";
import PageWrapper from "@/components/layout/PageWrapper";
import Skeleton from "@/components/ui/Skeleton";

export default function LiveDetailPage() {
  return (
    <>
      <PageHeader title="Live Stream" />
      <PageWrapper>
        <div className="max-w-5xl mx-auto bg-black min-h-screen">
          {/* Video Player Skeleton */}
          <div data-testid="video-player-skeleton" className="w-full aspect-video bg-gray-900 relative">
             <div className="absolute inset-0 flex items-center justify-center">
               <Skeleton className="w-16 h-16 rounded-full opacity-20" variant="circle" />
             </div>
          </div>

          {/* Live Info */}
          <div className="p-4 bg-white rounded-t-3xl -mt-6 relative z-10 min-h-[50vh]">
            <div className="w-12 h-1 bg-gray-300 rounded-full mx-auto mb-4" />
            
            <div className="flex justify-between items-start mb-6">
              <div className="space-y-2 flex-1">
                <Skeleton className="w-3/4 h-6" />
                <div className="flex items-center gap-2">
                   <Skeleton className="w-6 h-6 rounded-full" variant="circle" />
                   <Skeleton className="w-24 h-4" />
                </div>
              </div>
              <Skeleton className="w-20 h-8 rounded-full" />
            </div>

            {/* Chat Area Skeleton */}
            <div className="space-y-4 mt-8">
              {[1, 2, 3, 4, 5].map((idx) => (
                <div key={idx} className="flex gap-2 items-start">
                   <Skeleton className="w-8 h-8 rounded-full flex-shrink-0" variant="circle" />
                   <div className="space-y-1 flex-1">
                     <Skeleton className="w-20 h-3" />
                     <Skeleton className="w-full h-4" />
                   </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </PageWrapper>
    </>
  );
}
