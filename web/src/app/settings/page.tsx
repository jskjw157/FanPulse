"use client";

import PageHeader from "@/components/layout/PageHeader";
import PageWrapper from "@/components/layout/PageWrapper";
import ProtectedRoute from "@/components/auth/ProtectedRoute";
import { useAuth } from "@/contexts/AuthContext";

const APP_VERSION = "1.0.0";

export default function SettingsPage() {
  const { logout } = useAuth();

  return (
    <ProtectedRoute>
      <PageHeader title="설정" />
      <PageWrapper>
        <div className="max-w-3xl mx-auto px-4 py-4">
          {/* Account Section */}
          <div className="py-4">
            <h2 className="text-sm font-bold text-gray-500 mb-3 px-1">계정</h2>

            <div className="bg-white rounded-2xl border border-gray-100 overflow-hidden shadow-sm">
              <button
                onClick={logout}
                className="w-full flex items-center justify-between px-4 py-4 hover:bg-gray-50 transition-colors"
              >
                <div className="flex items-center gap-3">
                  <div className="w-9 h-9 bg-red-100 rounded-full flex items-center justify-center">
                    <i className="ri-logout-box-r-line text-red-600"></i>
                  </div>
                  <span className="text-sm text-gray-900 font-medium">
                    로그아웃
                  </span>
                </div>
                <i className="ri-arrow-right-s-line text-gray-400 text-lg"></i>
              </button>
            </div>
          </div>

          {/* App Info Section */}
          <div className="py-4">
            <h2 className="text-sm font-bold text-gray-500 mb-3 px-1">
              앱 정보
            </h2>

            <div className="bg-white rounded-2xl border border-gray-100 overflow-hidden shadow-sm">
              <div className="flex items-center justify-between px-4 py-4 border-b border-gray-100">
                <div className="flex items-center gap-3">
                  <div className="w-9 h-9 bg-gray-100 rounded-full flex items-center justify-center">
                    <i className="ri-information-line text-gray-600"></i>
                  </div>
                  <span className="text-sm text-gray-900 font-medium">
                    버전
                  </span>
                </div>
                <span className="text-xs text-gray-500">v{APP_VERSION}</span>
              </div>

              <div className="flex items-center justify-between px-4 py-4">
                <div className="flex items-center gap-3">
                  <div className="w-9 h-9 bg-purple-100 rounded-full flex items-center justify-center">
                    <i className="ri-copyright-line text-purple-600"></i>
                  </div>
                  <span className="text-sm text-gray-900 font-medium">
                    서비스 정보
                  </span>
                </div>
                <span className="text-xs text-gray-500">FanPulse</span>
              </div>
            </div>
          </div>
        </div>
      </PageWrapper>
    </ProtectedRoute>
  );
}
