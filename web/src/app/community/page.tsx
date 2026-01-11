"use client";

import PageHeader from "@/components/layout/PageHeader";
import PageWrapper from "@/components/layout/PageWrapper";
import SkeletonCard from "@/components/ui/SkeletonCard";
import { useState } from "react";
import { motion } from "framer-motion";

export default function CommunityPage() {
  const [activeTab, setActiveTab] = useState("all");

  return (
    <>
      <PageHeader 
        title="Community" 
        rightAction={
          <button className="p-2">
            <i className="ri-search-line text-xl text-gray-700"></i>
          </button>
        }
      />
      <PageWrapper>
        {/* Tab Navigation */}
        <div className="sticky top-16 bg-white border-b border-gray-200 z-30 lg:ml-64">
          <div className="flex max-w-7xl mx-auto">
            <button 
              onClick={() => setActiveTab("all")}
              className={`flex-1 py-4 text-sm font-medium transition-colors relative ${
                activeTab === "all" ? "text-purple-600" : "text-gray-500"
              }`}
            >
              전체
              {activeTab === "all" && (
                <motion.div 
                  layoutId="activeTab"
                  className="absolute bottom-0 left-0 right-0 h-0.5 bg-purple-600" 
                />
              )}
            </button>
            <button 
              onClick={() => setActiveTab("popular")}
              className={`flex-1 py-4 text-sm font-medium transition-colors relative ${
                activeTab === "popular" ? "text-purple-600" : "text-gray-500"
              }`}
            >
              인기
              {activeTab === "popular" && (
                <motion.div 
                  layoutId="activeTab"
                  className="absolute bottom-0 left-0 right-0 h-0.5 bg-purple-600" 
                />
              )}
            </button>
          </div>
        </div>

        <div className="max-w-3xl mx-auto px-4 py-6 space-y-4">
          {[1, 2, 3, 4].map((idx) => (
            <SkeletonCard key={idx} />
          ))}
        </div>

        {/* Floating Action Button Placeholder */}
        <div className="fixed bottom-24 right-4 lg:right-8 lg:bottom-8 w-14 h-14 bg-purple-600 rounded-full flex items-center justify-center shadow-lg text-white">
          <i className="ri-add-line text-2xl"></i>
        </div>
      </PageWrapper>
    </>
  );
}
