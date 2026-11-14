import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { SAFE_LOGIN_RETURNS, UNSAFE_LOGIN_RETURNS } from "@/lib/login-redirect.fixtures";
import AuthGuard from "./AuthGuard";

const { auth, nav } = vi.hoisted(() => ({
  auth: { isAuthenticated: false, isLoading: false },
  nav: { pathname: "/", router: { push: vi.fn(), replace: vi.fn() } },
}));

vi.mock("@/contexts/AuthContext", () => ({ useAuth: () => auth }));
vi.mock("next/navigation", () => ({
  usePathname: () => nav.pathname,
  useRouter: () => nav.router,
}));

function setReturnTo(value: string) {
  const queryIndex = value.indexOf("?");
  nav.pathname = queryIndex < 0 ? value : value.slice(0, queryIndex);
  const search = queryIndex < 0 ? "" : value.slice(queryIndex);
  // Mock the raw pathname separately so browser URL normalization cannot hide
  // a traversal/backslash/control input before the guard sees it.
  window.history.replaceState({}, "", `/${search}`);
}

describe("AuthGuard login return destinations", () => {
  beforeEach(() => {
    auth.isAuthenticated = false;
    auth.isLoading = false;
    nav.router.push.mockClear();
    nav.router.replace.mockClear();
  });

  afterEach(() => {
    cleanup();
    window.history.replaceState({}, "", "/");
    expect(nav.router.push).not.toHaveBeenCalled();
  });

  it.each(SAFE_LOGIN_RETURNS)("preserves the supported destination %s", (input, expected) => {
    setReturnTo(input);
    render(<AuthGuard><p>Private content</p></AuthGuard>);
    expect(nav.router.replace).toHaveBeenCalledTimes(1);
    expect(nav.router.replace).toHaveBeenCalledWith(`/login?redirect=${encodeURIComponent(expected)}`);
    expect(screen.queryByText("Private content")).not.toBeInTheDocument();
  });

  it.each(UNSAFE_LOGIN_RETURNS)("does not forward an unsafe destination %j", (input) => {
    setReturnTo(input);
    render(<AuthGuard><p>Private content</p></AuthGuard>);
    if (nav.pathname === "/login" || nav.pathname.startsWith("/login/")) {
      expect(nav.router.replace).not.toHaveBeenCalled();
    } else {
      expect(nav.router.replace).toHaveBeenCalledTimes(1);
      expect(nav.router.replace).toHaveBeenCalledWith("/login");
      expect(screen.queryByText("Private content")).not.toBeInTheDocument();
    }
  });

  it("waits for auth resolution and preserves the query when the session is absent", () => {
    setReturnTo("/post-detail?id=post-123");
    auth.isLoading = true;
    const view = render(<AuthGuard><p>Private content</p></AuthGuard>);
    expect(nav.router.replace).not.toHaveBeenCalled();
    expect(screen.queryByText("Private content")).not.toBeInTheDocument();
    auth.isLoading = false;
    view.rerender(<AuthGuard><p>Private content</p></AuthGuard>);
    expect(nav.router.replace).toHaveBeenCalledWith("/login?redirect=%2Fpost-detail%3Fid%3Dpost-123");
  });

  it("keeps authenticated content and never redirects it", () => {
    setReturnTo("/settings");
    auth.isAuthenticated = true;
    render(<AuthGuard><p>Private content</p></AuthGuard>);
    expect(screen.getByText("Private content")).toBeInTheDocument();
    expect(nav.router.replace).not.toHaveBeenCalled();
  });
});
