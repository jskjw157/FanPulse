"use client";

import PageHeader from "@/components/layout/PageHeader";
import PageWrapper from "@/components/layout/PageWrapper";
import ProtectedRoute from "@/components/auth/ProtectedRoute";
import { useAuth } from "@/contexts/AuthContext";
import { useMyProfile } from "@/hooks/useMyProfile";
import Link from "next/link";

export default function MyPage() {
  const { logout, user: authUser } = useAuth();
  const { profile, state } = useMyProfile();

  const displayName =
    profile?.username ??
    authUser?.username ??
    authUser?.email?.split("@")[0] ??
    "사용자";
  const displayEmail = profile?.email ?? authUser?.email ?? "";

  const menuItems = [
    { icon: "ri-settings-3-line", label: "설정", link: "/settings" },
  ];

  return (
    <ProtectedRoute>
      <PageHeader
        title="마이페이지"
        showBack={false}
        rightAction={
          <Link
            href="/settings"
            className="w-9 h-9 flex items-center justify-center rounded-full hover:bg-gray-100"
          >
            <i className="ri-settings-3-line text-xl text-gray-700"></i>
          </Link>
        }
      />
      <PageWrapper>
        {/* Profile Section */}
        <div className="bg-gradient-to-r from-purple-600 to-pink-600 px-4 pt-6 pb-6 lg:rounded-b-3xl">
          <div className="max-w-4xl mx-auto">
            <div className="flex items-center gap-4 mb-2">
              {/* 아바타 플레이스홀더 */}
              <div className="w-20 h-20 rounded-full border-4 border-white shadow-md bg-white/20 backdrop-blur-sm flex items-center justify-center">
                {state === "loading" ? (
                  <div className="w-8 h-8 rounded-full border-2 border-white/60 border-t-transparent animate-spin" />
                ) : (
                  <i className="ri-user-3-fill text-3xl text-white/80"></i>
                )}
              </div>
              <div className="flex-1">
                {state === "loading" ? (
                  <>
                    <div className="h-6 w-24 bg-white/20 rounded mb-2 animate-pulse" />
                    <div className="h-4 w-36 bg-white/20 rounded animate-pulse" />
                  </>
                ) : (
                  <>
                    <h2 className="text-white text-xl font-bold">
                      {displayName}
                    </h2>
                    <p className="text-white/90 text-sm mt-1">
                      {displayEmail}
                    </p>
                  </>
                )}
              </div>
            </div>
          </div>
        </div>

        <div className="max-w-4xl mx-auto px-4 mt-4">
          {/* Menu List */}
          <div className="bg-white rounded-2xl overflow-hidden shadow-sm mb-4 border border-gray-100">
            {menuItems.map((item, index) => (
              <Link
                key={index}
                href={item.link}
                className={`flex items-center justify-between px-4 py-4 hover:bg-gray-50 transition-colors ${
                  index !== menuItems.length - 1
                    ? "border-b border-gray-100"
                    : ""
                }`}
              >
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 bg-purple-50 rounded-full flex items-center justify-center text-purple-600">
                    <i className={`${item.icon} text-xl`}></i>
                  </div>
                  <span className="text-gray-900 font-medium">
                    {item.label}
                  </span>
                </div>
                <i className="ri-arrow-right-s-line text-xl text-gray-400"></i>
              </Link>
            ))}
          </div>

          {/* Logout Button */}
          <div className="mb-6">
            <button
              onClick={logout}
              className="w-full bg-white border border-gray-200 text-gray-700 py-3 rounded-2xl font-medium hover:bg-gray-50 transition-colors"
            >
              로그아웃
            </button>
          </div>
        </div>
      </PageWrapper>
    </ProtectedRoute>
  );
}
