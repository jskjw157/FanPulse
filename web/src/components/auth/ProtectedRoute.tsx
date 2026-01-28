"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/contexts/AuthContext";

interface ProtectedRouteProps {
  children: React.ReactNode;
  fallback?: React.ReactNode;
}

/**
 * 인증이 필요한 페이지를 감싸는 컴포넌트
 * - 비인증 시 /login으로 리다이렉트
 * - 로딩 중 스피너 표시
 */
export default function ProtectedRoute({
  children,
  fallback,
}: ProtectedRouteProps) {
  const { isAuthenticated, isLoading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      router.push("/login");
    }
  }, [isAuthenticated, isLoading, router]);

  // 로딩 중
  if (isLoading) {
    return (
      fallback ?? (
        <div className="min-h-screen flex items-center justify-center bg-gray-50">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-purple-600" />
        </div>
      )
    );
  }

  // 인증되지 않음
  if (!isAuthenticated) {
    return null;
  }

  return <>{children}</>;
}
