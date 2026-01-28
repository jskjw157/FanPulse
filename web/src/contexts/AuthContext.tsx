"use client";

import {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
  ReactNode,
} from "react";
import { useRouter } from "next/navigation";
import {
  checkAuthStatus,
  logout as apiLogout,
  AuthUser,
} from "@/lib/auth";

interface AuthContextValue {
  isAuthenticated: boolean;
  isLoading: boolean;
  user: AuthUser | null;
  logout: () => Promise<void>;
  refreshAuth: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [user, setUser] = useState<AuthUser | null>(null);
  const router = useRouter();

  // 인증 상태 새로고침
  const refreshAuth = useCallback(async () => {
    try {
      const status = await checkAuthStatus();
      setIsAuthenticated(status.authenticated);
      setUser(status.user ?? null);
    } catch {
      setIsAuthenticated(false);
      setUser(null);
    }
  }, []);

  // 초기 인증 상태 확인
  useEffect(() => {
    const checkAuth = async () => {
      setIsLoading(true);
      try {
        const status = await checkAuthStatus();
        setIsAuthenticated(status.authenticated);
        setUser(status.user ?? null);
      } catch {
        setIsAuthenticated(false);
        setUser(null);
      } finally {
        setIsLoading(false);
      }
    };

    checkAuth();
  }, []);

  // 로그아웃 함수
  const logout = useCallback(async () => {
    try {
      await apiLogout();
    } finally {
      setIsAuthenticated(false);
      setUser(null);
      router.push("/login");
    }
  }, [router]);

  return (
    <AuthContext.Provider
      value={{
        isAuthenticated,
        isLoading,
        user,
        logout,
        refreshAuth,
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
