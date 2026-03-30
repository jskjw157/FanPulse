"use client";

import { useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";

import { FanPulseApiError, loginWithGoogle } from "@/lib/auth";
import { useAuth } from "@/contexts/AuthContext";

type GoogleIdentity = {
  accounts?: {
    id?: {
      initialize: (config: {
        client_id: string;
        callback: (response: { credential?: string }) => void;
      }) => void;
      renderButton: (
        container: HTMLElement,
        options: {
          theme?: string;
          size?: string;
          shape?: string;
          text?: string;
          logo_alignment?: string;
          width?: number;
        },
      ) => void;
    };
  };
};

declare global {
  interface Window {
    google?: GoogleIdentity;
  }
}

const GOOGLE_IDENTITY_SCRIPT_SRC = "https://accounts.google.com/gsi/client";

let googleIdentityScriptPromise: Promise<void> | null = null;

function loadGoogleIdentityScript(): Promise<void> {
  if (typeof window === "undefined") {
    return Promise.reject(new Error("Google script can only be loaded in browser"));
  }

  if (googleIdentityScriptPromise) return googleIdentityScriptPromise;

  googleIdentityScriptPromise = new Promise((resolve, reject) => {
    const existing = document.querySelector(
      `script[src="${GOOGLE_IDENTITY_SCRIPT_SRC}"]`,
    );
    if (existing) {
      resolve();
      return;
    }

    const script = document.createElement("script");
    script.src = GOOGLE_IDENTITY_SCRIPT_SRC;
    script.async = true;
    script.defer = true;
    script.onload = () => resolve();
    script.onerror = () => reject(new Error("Google Identity script load failed"));
    document.head.appendChild(script);
  });

  return googleIdentityScriptPromise;
}

function getErrorMessage(err: unknown): string {
  if (err instanceof FanPulseApiError) return err.message;
  if (err instanceof Error) return err.message;
  return "요청 처리 중 오류가 발생했습니다.";
}

export default function LoginPage() {
  const router = useRouter();
  const { refreshAuth } = useAuth();
  const [formError, setFormError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const googleButtonRef = useRef<HTMLDivElement | null>(null);
  const googleClientId = process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID;
  const canUseGoogleLogin = Boolean(googleClientId);

  useEffect(() => {
    if (!googleClientId) return;
    const container = googleButtonRef.current;
    if (!container) return;

    let cancelled = false;

    loadGoogleIdentityScript()
      .then(() => {
        if (cancelled) return;

        const google = window.google;
        if (!google?.accounts?.id) return;

        google.accounts.id.initialize({
          client_id: googleClientId,
          callback: async (response: { credential?: string }) => {
            if (!response?.credential) {
              setFormError("Google 로그인 토큰을 받지 못했습니다.");
              return;
            }

            setIsSubmitting(true);
            setFormError(null);
            try {
              await loginWithGoogle({ idToken: response.credential });
              // 쿠키가 설정되었으므로 인증 상태 새로고침
              await refreshAuth();
              router.push("/");
            } catch (err) {
              setFormError(getErrorMessage(err));
            } finally {
              setIsSubmitting(false);
            }
          },
        });

        // Google SDK expects to own the button markup.
        const containerWidth = container.getBoundingClientRect().width;
        const buttonWidth = containerWidth
          ? Math.min(360, Math.floor(containerWidth))
          : 320;

        google.accounts.id.renderButton(container, {
          theme: "outline",
          size: "large",
          shape: "pill",
          text: "continue_with",
          logo_alignment: "left",
          width: buttonWidth,
        });
      })
      .catch(() => {
        if (cancelled) return;
        setFormError("Google 로그인을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.");
      });

    return () => {
      cancelled = true;
    };
  }, [googleClientId, router]);

  return (
    <div className="min-h-screen bg-gradient-to-br from-purple-600 via-pink-500 to-purple-700 flex items-center justify-center px-4">
      <div className="w-full max-w-md">
        {/* Logo */}
        <div className="text-center mb-8">
          <h1 className="text-white text-3xl font-bold mb-2 font-pacifico">Welcome to FanPulse</h1>
          <p className="text-white/90 text-sm">글로벌 K-POP 팬들의 인터랙티브 플랫폼</p>
        </div>

        {/* Login Card */}
        <div className="bg-white rounded-3xl p-6 shadow-2xl">
          {formError && (
            <div
              className="rounded-2xl bg-red-50 text-red-700 px-4 py-3 text-sm mb-4"
              role="alert"
            >
              {formError}
            </div>
          )}

          {/* Google Login */}
          <div className="space-y-3">
            {canUseGoogleLogin ? (
              <div className="flex justify-center" ref={googleButtonRef} />
            ) : (
              <button
                type="button"
                onClick={() =>
                  setFormError(
                    "Google 로그인 설정이 필요합니다. NEXT_PUBLIC_GOOGLE_CLIENT_ID를 확인해주세요.",
                  )
                }
                className="w-full flex items-center justify-center gap-3 bg-white border-2 border-gray-200 rounded-full py-3 font-medium text-gray-700 hover:border-purple-300 transition-colors"
              >
                <svg
                  width="20"
                  height="20"
                  viewBox="0 0 20 20"
                  fill="none"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path
                    d="M19.8055 10.2292C19.8055 9.55056 19.7501 8.86667 19.6306 8.19861H10.2002V12.0492H15.6014C15.3773 13.2911 14.6571 14.3898 13.6025 15.0875V17.5866H16.8251C18.7173 15.8449 19.8055 13.2728 19.8055 10.2292Z"
                    fill="#4285F4"
                  />
                  <path
                    d="M10.2002 20.0006C12.9516 20.0006 15.2727 19.1151 16.8296 17.5865L13.607 15.0874C12.7096 15.6972 11.5521 16.0428 10.2047 16.0428C7.54356 16.0428 5.28217 14.2828 4.48642 11.9165H1.16699V14.4923C2.76077 17.8695 6.31545 20.0006 10.2002 20.0006Z"
                    fill="#34A853"
                  />
                  <path
                    d="M4.48174 11.9163C4.06649 10.6744 4.06649 9.33009 4.48174 8.08813V5.51233H1.16708C-0.389027 8.66929 -0.389027 12.3352 1.16708 15.4921L4.48174 11.9163Z"
                    fill="#FBBC04"
                  />
                  <path
                    d="M10.2002 3.95805C11.6241 3.936 13.0007 4.47247 14.0409 5.45722L16.8911 2.60218C15.1826 0.990831 12.9335 0.0808353 10.2002 0.104384C6.31545 0.104384 2.76077 2.23549 1.16699 5.61261L4.48165 8.18841C5.27263 5.81691 7.53879 3.95805 10.2002 3.95805Z"
                    fill="#EA4335"
                  />
                </svg>
                Google로 로그인
              </button>
            )}
          </div>

          {isSubmitting && (
            <p className="mt-4 text-center text-xs text-gray-500">
              처리 중...
            </p>
          )}

          <p className="mt-4 text-center text-xs text-gray-500">
            구글 계정으로만 로그인할 수 있어요.
          </p>
        </div>
      </div>
    </div>
  );
}
