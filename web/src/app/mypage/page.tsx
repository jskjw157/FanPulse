"use client";

import { useState } from "react";
import { useTranslation } from "react-i18next";
import PageHeader from "@/components/layout/PageHeader";
import PageWrapper from "@/components/layout/PageWrapper";
import ProtectedRoute from "@/components/auth/ProtectedRoute";
import ProfileEditor from "@/components/profile/ProfileEditor";
import { useAuth } from "@/contexts/AuthContext";
import { useMyProfile } from "@/hooks/useMyProfile";
import Link from "next/link";

export default function MyPage() {
  const { logout, user: authUser } = useAuth();
  const { profile, state, error, refresh } = useMyProfile();
  const { t } = useTranslation();
  const [savedForUser, setSavedForUser] = useState<string | null>(null);
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const [logoutFailed, setLogoutFailed] = useState(false);

  async function handleLogout() {
    if (isLoggingOut) return;
    setIsLoggingOut(true);
    setLogoutFailed(false);
    try {
      await logout();
    } catch {
      setLogoutFailed(true);
    } finally {
      setIsLoggingOut(false);
    }
  }

  const menuItems = [
    { icon: "ri-settings-3-line", label: t("settings.title"), link: "/settings" },
  ];

  return (
    <ProtectedRoute>
      <PageHeader
        title={t("profile.title")}
        showBack={false}
        rightAction={
          <Link
            href="/settings"
            aria-label={t("settings.title")}
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
              <div className="flex-1 min-w-0">
                {state === "loading" ? (
                  <div role="status" aria-label="프로필을 불러오는 중입니다">
                    <div className="h-6 w-24 bg-white/20 rounded mb-2 animate-pulse" />
                    <div className="h-4 w-36 bg-white/20 rounded animate-pulse" />
                  </div>
                ) : state === "error" ? (
                  <div role="alert">
                    <p className="text-white font-medium">{error}</p>
                    <button
                      type="button"
                      onClick={() => void refresh()}
                      className="mt-3 rounded-full bg-white px-4 py-2 text-sm font-semibold text-purple-700 hover:bg-purple-50"
                    >
                      다시 시도
                    </button>
                  </div>
                ) : state === "success" && profile ? (
                  <>
                    <h2 className="text-white text-xl font-bold break-all">
                      {profile.username}
                    </h2>
                    <p className="text-white/90 text-sm mt-1 break-all">
                      {profile.email}
                    </p>
                  </>
                ) : null}
              </div>
            </div>
            {state === "success" && profile && (
              <ProfileEditor key={authUser?.id} username={profile.username ?? ""} onSaved={() => {
                setSavedForUser(authUser?.id ?? null);
                void refresh();
              }} />
            )}
          </div>
        </div>

        <div className="max-w-4xl mx-auto px-4 mt-4">
          {savedForUser && savedForUser === authUser?.id && (
            <p role="status" className="mb-4 rounded-xl bg-green-50 p-3 text-sm text-green-800">{t("profile.saved")}</p>
          )}
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
              onClick={() => void handleLogout()}
              disabled={isLoggingOut}
              aria-busy={isLoggingOut}
              className="w-full bg-white border border-gray-200 text-gray-700 py-3 rounded-2xl font-medium hover:bg-gray-50 transition-colors disabled:opacity-50"
            >
              {t(isLoggingOut ? "settings.loggingOut" : "settings.logout")}
            </button>
            {logoutFailed && (
              <p role="alert" className="mt-2 text-sm text-red-700">{t("settings.logoutFailed")}</p>
            )}
          </div>
        </div>
      </PageWrapper>
    </ProtectedRoute>
  );
}
