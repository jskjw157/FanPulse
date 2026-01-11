"use client";

import PageHeader from "@/components/layout/PageHeader";
import PageWrapper from "@/components/layout/PageWrapper";
import SkeletonCard from "@/components/ui/SkeletonCard";
import { motion } from "framer-motion";

export default function ConcertPage() {
  return (
    <>
      <PageHeader title="Upcoming Concerts" />
      <PageWrapper>
        <div className="max-w-7xl mx-auto px-4 lg:px-8 py-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {[1, 2, 3, 4].map((idx) => (
              <div key={idx} className="space-y-4">
                <SkeletonCard />
                <div className="p-4 bg-white rounded-2xl shadow-sm space-y-2">
                  <div className="flex justify-between items-center">
                    <div className="w-24 h-6 bg-purple-100 rounded-full animate-pulse" />
                    <div className="w-16 h-4 bg-gray-100 rounded-full animate-pulse" />
                  </div>
                  <div className="w-full h-12 bg-purple-600 rounded-full animate-pulse opacity-20" />
                </div>
              </div>
            ))}
          </div>
        </div>
      </PageWrapper>
    </>
  );
}
