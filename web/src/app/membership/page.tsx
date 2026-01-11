"use client";

import PageHeader from "@/components/layout/PageHeader";
import PageWrapper from "@/components/layout/PageWrapper";
import Skeleton from "@/components/ui/Skeleton";
import Card from "@/components/ui/Card";
import Button from "@/components/ui/Button";

export default function MembershipPage() {
  return (
    <>
      <PageHeader title="VIP Membership" />
      <PageWrapper>
        <div className="max-w-4xl mx-auto px-4 py-6">
          {/* VIP Card Skeleton */}
          <Card className="bg-slate-900 p-8 text-white mb-8 relative overflow-hidden h-56 flex flex-col justify-between">
            <div className="relative z-10 space-y-4">
              <Skeleton className="w-32 h-8 bg-white/10" />
              <Skeleton className="w-48 h-4 bg-white/10" />
            </div>
            <div className="relative z-10 flex justify-between items-end">
              <Skeleton className="w-24 h-4 bg-white/10" />
              <div className="w-12 h-12 bg-yellow-500 rounded-full animate-pulse" />
            </div>
            {/* Glossy Effect */}
            <div className="absolute inset-0 bg-gradient-to-tr from-white/5 to-transparent pointer-events-none" />
          </Card>

          {/* Benefits Section */}
          <h2 className="text-xl font-bold text-gray-900 mb-6">VIP Benefits</h2>
          <div data-testid="membership-benefits-skeleton" className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {[1, 2, 3, 4].map((idx) => (
              <Card key={idx} className="p-6 border border-gray-100 flex gap-4 items-start">
                <Skeleton className="w-12 h-12 rounded-2xl flex-shrink-0" />
                <div className="space-y-2 flex-1">
                  <Skeleton className="w-3/4 h-5" />
                  <Skeleton className="w-full h-4" />
                  <Skeleton className="w-2/3 h-4" />
                </div>
              </Card>
            ))}
          </div>

          {/* CTA Button */}
          <div className="mt-12 text-center">
            <Button size="lg" className="w-full max-w-sm">
              Join VIP Club
            </Button>
            <p className="mt-4 text-sm text-gray-500">Starting from $9.99/month</p>
          </div>
        </div>
      </PageWrapper>
    </>
  );
}
