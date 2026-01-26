"use client";

import { useAuth } from "@/contexts/AuthContext";
import { usePathname, useRouter } from "next/navigation";
import { useEffect } from "react";

// 로그인 없이 접근 가능한 경로
const PUBLIC_PATHS = ["/login"];

export default function AuthGuard({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, isLoading } = useAuth();
  const pathname = usePathname();
  const router = useRouter();

  const isPublicPath = PUBLIC_PATHS.some(
    (path) => pathname === path || pathname.startsWith(`${path}/`)
  );

  useEffect(() => {
    if (!isLoading && !isAuthenticated && !isPublicPath) {
      router.push("/login");
    }
  }, [isAuthenticated, isLoading, isPublicPath, router]);

  // 로딩 중
  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-gradient-to-br from-purple-600 to-pink-600">
        <div className="flex flex-col items-center gap-4">
          <div className="animate-spin rounded-full h-12 w-12 border-4 border-white border-t-transparent" />
          <p className="text-white text-sm">로딩 중...</p>
        </div>
      </div>
    );
  }

  // 비로그인 상태에서 보호된 경로 접근 시
  if (!isAuthenticated && !isPublicPath) {
    return null;
  }

  return <>{children}</>;
}
