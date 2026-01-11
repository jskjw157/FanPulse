"use client";

import PageHeader from "@/components/layout/PageHeader";
import PageWrapper from "@/components/layout/PageWrapper";
import Skeleton from "@/components/ui/Skeleton";
import Card from "@/components/ui/Card";
import Button from "@/components/ui/Button";

export default function AdsPage() {
  return (
    <>
      <PageHeader title="Earn Rewards" />
      <PageWrapper>
        <div className="max-w-4xl mx-auto px-4 py-6">
          {/* Points Summary Skeleton */}
          <Card className="bg-white p-6 shadow-sm mb-8 flex items-center justify-between border border-purple-100">
            <div className="space-y-2">
              <p className="text-sm text-gray-500">My Points</p>
              <Skeleton className="w-32 h-10" />
            </div>
            <div className="w-12 h-12 bg-yellow-400 rounded-full flex items-center justify-center">
              <i className="ri-coin-fill text-2xl text-white"></i>
            </div>
          </Card>

          {/* Reward Missions */}
          <h2 className="text-xl font-bold text-gray-900 mb-6">Today's Missions</h2>
          <div data-testid="reward-missions-skeleton" className="space-y-4">
            {[1, 2, 3, 4, 5].map((idx) => (
              <Card key={idx} className="p-4 flex items-center gap-4">
                <div className="w-12 h-12 bg-gray-100 rounded-xl flex items-center justify-center flex-shrink-0">
                  <Skeleton className="w-6 h-6" />
                </div>
                <div className="flex-1 space-y-2">
                  <Skeleton className="w-1/2 h-4" />
                  <Skeleton className="w-3/4 h-3" />
                </div>
                <div className="text-right space-y-2">
                  <div className="text-sm font-bold text-purple-600">+100 P</div>
                  <Button size="sm" variant="secondary" className="px-4">
                    Go
                  </Button>
                </div>
              </Card>
            ))}
          </div>
        </div>
      </PageWrapper>
    </>
  );
}
