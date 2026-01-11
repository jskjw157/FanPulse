"use client";

import PageHeader from "@/components/layout/PageHeader";
import PageWrapper from "@/components/layout/PageWrapper";
import SkeletonCard from "@/components/ui/SkeletonCard";
import Skeleton from "@/components/ui/Skeleton";
import { motion } from "framer-motion";

export default function ChartPage() {
  return (
    <>
      <PageHeader title="Real-time Chart" />
      <PageWrapper>
        <div className="max-w-4xl mx-auto px-4 py-6">
          {/* Chart Summary Skeleton */}
          <div className="mb-8 p-6 bg-white rounded-3xl shadow-sm flex items-center justify-between">
            <div className="space-y-2">
              <Skeleton className="w-24 h-4" />
              <Skeleton className="w-48 h-8" />
            </div>
            <Skeleton className="w-12 h-12 variant-circle" />
          </div>

          <div className="space-y-3">
            {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10].map((idx) => (
              <div key={idx} className="flex items-center gap-4 p-2">
                <div className="w-8 text-center font-bold text-gray-400">{idx}</div>
                <SkeletonCard layout="horizontal" />
              </div>
            ))}
          </div>
        </div>
      </PageWrapper>
    </>
  );
}
