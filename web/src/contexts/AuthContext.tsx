"use client";

import {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
  useRef,
  ReactNode,
} from "react";
import { useRouter } from "next/navigation";
import {
  checkAuthStatus,
  logout as apiLogout,
  AuthUser,
} from "@/lib/auth";
import { fetchMySettings, updateMyLanguage, type UserLanguage, type UserSettings } from "@/lib/api/user";
import { announceSessionChange, SESSION_CHANGED, SESSION_INVALIDATED } from "@/lib/session-sync";
import i18n from "@/i18n";

function applyLanguage(language: UserLanguage) {
  void i18n.changeLanguage(language);
  document.documentElement.lang = language;
}

interface AuthContextValue {
  isAuthenticated: boolean;
  isLoading: boolean;
  sessionVersion: number;
  user: AuthUser | null;
  logout: () => Promise<void>;
  refreshAuth: () => Promise<void>;
  updateUser: (user: AuthUser) => void;
  settings: UserSettings | null;
  settingsState: "idle" | "loading" | "success" | "error";
  isSavingLanguage: boolean;
  reloadSettings: () => Promise<void>;
  saveLanguage: (language: UserLanguage) => Promise<boolean>;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [sessionVersion, setSessionVersion] = useState(0);
  const [user, setUser] = useState<AuthUser | null>(null);
  const [settings, setSettings] = useState<UserSettings | null>(null);
  const [settingsState, setSettingsState] = useState<AuthContextValue["settingsState"]>("idle");
  const [isSavingLanguage, setIsSavingLanguage] = useState(false);
  const authRequest = useRef(0);
  const currentUserId = useRef<string | null>(null);
  const knownUserId = useRef<string | null | undefined>(undefined);
  const settingsRequest = useRef<AbortController | null>(null);
  const router = useRouter();

  const invalidateAuth = useCallback(() => { ++authRequest.current; }, []);

  const clearSettings = useCallback(() => {
    settingsRequest.current?.abort();
    setSettings(null);
    setSettingsState("idle");
    setIsSavingLanguage(false);
    applyLanguage("ko");
  }, []);

  // 인증 상태 새로고침
  const refreshSession = useCallback(async (announce: boolean) => {
    const request = ++authRequest.current;
    // Invalidate before awaiting the cookie principal. No old private scope may fetch or save.
    currentUserId.current = null;
    setSessionVersion((version) => version + 1);
    setIsLoading(true);
    setIsAuthenticated(false);
    setUser(null);
    clearSettings();
    try {
      const status = await checkAuthStatus();
      if (request !== authRequest.current) return;
      const nextUser = status.authenticated ? status.user ?? null : null;
      if (announce && knownUserId.current !== (nextUser?.id ?? null)) announceSessionChange();
      knownUserId.current = nextUser?.id ?? null;
      currentUserId.current = nextUser?.id ?? null;
      setIsAuthenticated(!!nextUser);
      setUser(nextUser);
    } catch {
      if (request !== authRequest.current) return;
      currentUserId.current = null;
      clearSettings();
      setIsAuthenticated(false);
      setUser(null);
    } finally {
      if (request === authRequest.current) setIsLoading(false);
    }
  }, [clearSettings]);

  const refreshAuth = useCallback(() => refreshSession(true), [refreshSession]);

  // 초기 인증 상태 확인
  useEffect(() => {
    void refreshAuth();
    return () => {
      invalidateAuth();
      settingsRequest.current?.abort();
    };
  }, [refreshAuth, invalidateAuth]);

  useEffect(() => {
    const onStorage = (event: StorageEvent) => {
      if (event.key === SESSION_CHANGED || event.key === null) void refreshSession(false);
    };
    const onFocus = () => { void refreshAuth(); };
    const onVisibility = () => {
      if (document.visibilityState === "visible") void refreshAuth();
    };
    window.addEventListener("storage", onStorage);
    window.addEventListener("focus", onFocus);
    window.addEventListener(SESSION_INVALIDATED, onFocus);
    document.addEventListener("visibilitychange", onVisibility);
    return () => {
      window.removeEventListener("storage", onStorage);
      window.removeEventListener("focus", onFocus);
      window.removeEventListener(SESSION_INVALIDATED, onFocus);
      document.removeEventListener("visibilitychange", onVisibility);
    };
  }, [refreshAuth, refreshSession]);

  const updateUser = useCallback((updated: AuthUser) => {
    if (updated.id !== currentUserId.current) return;
    invalidateAuth();
    setUser(updated);
  }, [invalidateAuth]);

  const reloadSettings = useCallback(async () => {
    const userId = currentUserId.current;
    if (!userId) return;
    settingsRequest.current?.abort();
    const controller = new AbortController();
    settingsRequest.current = controller;
    setSettingsState("loading");
    try {
      const result = await fetchMySettings(controller.signal, userId);
      if (controller.signal.aborted || currentUserId.current !== userId) return;
      setSettings(result);
      setSettingsState("success");
      applyLanguage(result.language);
    } catch {
      if (!controller.signal.aborted && currentUserId.current === userId) {
        setSettingsState("error");
      }
    }
  }, []);

  useEffect(() => {
    if (!isLoading && user?.id) void reloadSettings();
    return () => settingsRequest.current?.abort();
  }, [user?.id, isLoading, sessionVersion, reloadSettings]);

  const saveLanguage = useCallback(async (language: UserLanguage) => {
    // Capture the rendered account, not a newer principal from a pending React render.
    const userId = user?.id;
    if (isLoading || !userId || currentUserId.current !== userId) return false;
    settingsRequest.current?.abort();
    const controller = new AbortController();
    settingsRequest.current = controller;
    setIsSavingLanguage(true);
    try {
      const result = await updateMyLanguage(language, userId, controller.signal);
      if (controller.signal.aborted || currentUserId.current !== userId) return false;
      setSettings(result);
      setSettingsState("success");
      applyLanguage(result.language);
      return true;
    } catch (error) {
      if (controller.signal.aborted || currentUserId.current !== userId) return false;
      throw error;
    } finally {
      if (settingsRequest.current === controller) setIsSavingLanguage(false);
    }
  }, [user?.id, isLoading]);

  // 로그아웃 함수
  const logout = useCallback(async () => {
    const userId = currentUserId.current;
    invalidateAuth();
    // Keep the session visible until the server confirms it revoked the cookie session.
    await apiLogout();
    if (currentUserId.current !== userId) return;
    invalidateAuth();
    currentUserId.current = null;
    knownUserId.current = null;
    setSessionVersion((version) => version + 1);
    clearSettings();
    setIsAuthenticated(false);
    setUser(null);
    announceSessionChange();
    router.replace("/login");
  }, [router, clearSettings, invalidateAuth]);

  return (
    <AuthContext.Provider
      value={{
        isAuthenticated,
        isLoading,
        sessionVersion,
        user,
        logout,
        refreshAuth,
        updateUser,
        settings,
        settingsState,
        isSavingLanguage,
        reloadSettings,
        saveLanguage,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
