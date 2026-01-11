"use client";

import Skeleton from "./Skeleton";
import Card from "./Card";

interface SkeletonCardProps {
  layout?: "vertical" | "horizontal";
}

export default function SkeletonCard({ layout = "vertical" }: SkeletonCardProps) {
  if (layout === "horizontal") {
    return (
      <Card data-testid="skeleton-card" className="flex gap-3 p-3">
        <Skeleton className="w-24 h-20 rounded-xl flex-shrink-0" />
        <div className="flex-1 space-y-2 py-1">
          <Skeleton className="w-1/3 h-3" />
          <Skeleton className="w-full h-4" />
          <Skeleton className="w-1/2 h-3" />
        </div>
      </Card>
    );
  }

  return (
    <Card data-testid="skeleton-card" className="flex flex-col">
      <Skeleton className="w-full h-40 rounded-none" />
      <div className="p-3 space-y-2">
        <Skeleton className="w-full h-4" />
        <Skeleton className="w-2/3 h-3" />
        <div className="flex justify-between items-center pt-2">
          <Skeleton className="w-1/4 h-3" />
          <Skeleton className="w-1/4 h-3" />
        </div>
      </div>
    </Card>
  );
}
