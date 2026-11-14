"use client";

import { useState, type FormEvent } from "react";
import { useTranslation } from "react-i18next";
import PageHeader from "@/components/layout/PageHeader";
import PageWrapper from "@/components/layout/PageWrapper";
import ProtectedRoute from "@/components/auth/ProtectedRoute";
import { useAuth } from "@/contexts/AuthContext";
import type { UserLanguage } from "@/lib/api/user";

const APP_VERSION = "1.0.0";

function LanguageSettings() {
  const { settings, settingsState, isSavingLanguage, reloadSettings, saveLanguage } = useAuth();
  const { t } = useTranslation();
  const [draft, setDraft] = useState<UserLanguage | null>(null);
  const [message, setMessage] = useState<"saved" | "error" | null>(null);

  async function save(event: FormEvent) {
    event.preventDefault();
    if (!draft || isSavingLanguage) return;
    setMessage(null);
    try {
      if (await saveLanguage(draft)) {
        setDraft(null);
        setMessage("saved");
      }
    } catch {
      setMessage("error");
    }
  }

  return (
    <section className="py-4" aria-labelledby="language-heading">
      <h2 id="language-heading" className="text-sm font-bold text-gray-500 mb-3 px-1">{t("settings.language")}</h2>
      <div className="bg-white rounded-2xl border border-gray-100 p-4 shadow-sm">
        {settingsState === "error" ? (
          <div>
            <p role="alert" className="text-sm text-red-700">{t("settings.loadFailed")}</p>
            <button onClick={() => void reloadSettings()} className="mt-3 text-sm font-medium text-purple-700">{t("common.retry")}</button>
          </div>
        ) : settingsState !== "success" || !settings ? (
          <p role="status" className="text-sm text-gray-600">{t("common.loading")}</p>
        ) : (
          <form onSubmit={save} aria-busy={isSavingLanguage}>
            <label htmlFor="language" className="block text-sm font-medium text-gray-900">{t("settings.language")}</label>
            <p id="language-hint" className="mt-1 text-xs text-gray-600">{t("settings.languageHint")}</p>
            <select id="language" value={draft ?? settings.language} disabled={isSavingLanguage}
              aria-describedby="language-hint"
              onChange={(event) => {
                const value = event.target.value;
                if (value === "ko" || value === "en") setDraft(value);
                setMessage(null);
              }}
              className="mt-3 w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900 disabled:opacity-50">
              <option value="ko">한국어</option>
              <option value="en">English</option>
            </select>
            {message === "error" && <p role="alert" className="mt-3 text-sm text-red-700">{t("settings.saveFailed")}</p>}
            {message === "saved" && <p role="status" className="mt-3 text-sm text-green-700">{t("settings.saved")}</p>}
            <button type="submit" disabled={isSavingLanguage || !draft || draft === settings.language}
              className="mt-3 rounded-lg bg-purple-600 px-4 py-2 text-sm font-medium text-white disabled:opacity-50">
              {t(isSavingLanguage ? "common.saving" : "common.save")}
            </button>
          </form>
        )}
      </div>
    </section>
  );
}

export default function SettingsPage() {
  const { logout, user, sessionVersion } = useAuth();
  const { t } = useTranslation();
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

  return (
    <ProtectedRoute>
      <PageHeader title={t("settings.title")} />
      <PageWrapper>
        <div className="max-w-3xl mx-auto px-4 py-4">
          {/* Account Section */}
          <div className="py-4">
            <h2 className="text-sm font-bold text-gray-500 mb-3 px-1">{t("settings.account")}</h2>

            <div className="bg-white rounded-2xl border border-gray-100 overflow-hidden shadow-sm">
              <button
                onClick={() => void handleLogout()}
                disabled={isLoggingOut}
                aria-busy={isLoggingOut}
                className="w-full flex items-center justify-between px-4 py-4 hover:bg-gray-50 transition-colors disabled:opacity-50"
              >
                <div className="flex items-center gap-3">
                  <div className="w-9 h-9 bg-red-100 rounded-full flex items-center justify-center">
                    <i className="ri-logout-box-r-line text-red-600"></i>
                  </div>
                  <span className="text-sm text-gray-900 font-medium">
                    {t(isLoggingOut ? "settings.loggingOut" : "settings.logout")}
                  </span>
                </div>
                <i className="ri-arrow-right-s-line text-gray-400 text-lg"></i>
              </button>
            </div>
          </div>

          {logoutFailed && (
            <p role="alert" className="mt-2 text-sm text-red-700">{t("settings.logoutFailed")}</p>
          )}

          <LanguageSettings key={`${user?.id}:${sessionVersion}`} />

          {/* App Info Section */}
          <div className="py-4">
            <h2 className="text-sm font-bold text-gray-500 mb-3 px-1">
              {t("settings.appInfo")}
            </h2>

            <div className="bg-white rounded-2xl border border-gray-100 overflow-hidden shadow-sm">
              <div className="flex items-center justify-between px-4 py-4 border-b border-gray-100">
                <div className="flex items-center gap-3">
                  <div className="w-9 h-9 bg-gray-100 rounded-full flex items-center justify-center">
                    <i className="ri-information-line text-gray-600"></i>
                  </div>
                  <span className="text-sm text-gray-900 font-medium">
                    {t("settings.version")}
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
                    {t("settings.serviceInfo")}
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
