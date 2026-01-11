"use client";

import PageHeader from "@/components/layout/PageHeader";
import PageWrapper from "@/components/layout/PageWrapper";
import Skeleton from "@/components/ui/Skeleton";
import Card from "@/components/ui/Card";
import Button from "@/components/ui/Button";
import { motion } from "framer-motion";

export default function VotingPage() {
  return (
    <>
      <PageHeader title="Voting" />
      <PageWrapper>
        <div className="max-w-4xl mx-auto px-4 py-6">
          {/* Active Voting Banner Skeleton */}
          <motion.div 
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="mb-8"
          >
            <Card className="bg-gradient-to-br from-purple-600 to-pink-600 p-6 text-white overflow-hidden relative">
              <div className="relative z-10 space-y-4">
                <Skeleton className="w-24 h-6 bg-white/20" />
                <Skeleton className="w-3/4 h-8 bg-white/20" />
                <Skeleton className="w-1/2 h-4 bg-white/20" />
                <Button variant="secondary" className="bg-white text-purple-600 border-none">
                  Vote Now
                </Button>
              </div>
              <div className="absolute top-0 right-0 w-32 h-32 bg-white/10 rounded-full -mr-16 -mt-16 blur-2xl" />
            </Card>
          </motion.div>

          {/* Voting Categories */}
          <div className="flex gap-3 mb-6 overflow-x-auto pb-2 scrollbar-hide">
            {[1, 2, 3, 4].map((idx) => (
              <Skeleton key={idx} className="w-24 h-10 rounded-full flex-shrink-0" />
            ))}
          </div>

          {/* Voting List */}
          <div data-testid="voting-list-skeleton" className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {[1, 2, 3, 4, 5, 6].map((idx) => (
              <Card key={idx} className="p-4 flex gap-4 items-center">
                <Skeleton className="w-20 h-20 rounded-xl flex-shrink-0" />
                <div className="flex-1 space-y-2">
                  <Skeleton className="w-3/4 h-4" />
                  <Skeleton className="w-1/2 h-3" />
                  <div className="w-full bg-gray-100 h-2 rounded-full mt-2">
                    <motion.div 
                      initial={{ width: 0 }}
                      animate={{ width: `${Math.random() * 100}%` }}
                      transition={{ duration: 1, delay: 0.5 }}
                      className="bg-purple-600 h-full rounded-full"
                    />
                  </div>
                </div>
                <Button size="sm" variant="outline" className="flex-shrink-0">
                  Vote
                </Button>
              </Card>
            ))}
          </div>
        </div>
      </PageWrapper>
    </>
  );
}
