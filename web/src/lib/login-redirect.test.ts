import { describe, expect, it } from "vitest";
import { buildLoginUrl, getSafeLoginReturnTo } from "./login-redirect";
import { SAFE_LOGIN_RETURNS, UNSAFE_LOGIN_RETURNS } from "./login-redirect.fixtures";

describe("login return destinations", () => {
  it.each(SAFE_LOGIN_RETURNS)("round-trips the supported route %s", (input, expected) => {
    expect(getSafeLoginReturnTo(input)).toBe(expected);
    const loginUrl = buildLoginUrl(input);
    expect(loginUrl).toBe(`/login?redirect=${encodeURIComponent(expected)}`);
    const redirect = new URLSearchParams(loginUrl.slice(loginUrl.indexOf("?"))).get("redirect");
    expect(getSafeLoginReturnTo(redirect)).toBe(expected);
  });

  it.each([null, undefined, "", ...UNSAFE_LOGIN_RETURNS])("rejects %j at both boundaries", (input) => {
    expect(getSafeLoginReturnTo(input)).toBeNull();
    expect(buildLoginUrl(input)).toBe("/login");
  });

  it.each([
    "/ads", "/chart", "/community", "/concert", "/favorites", "/live",
    "/membership", "/mypage", "/news", "/search", "/support", "/tickets", "/voting",
    "/artist-detail", "/concert-detail", "/live-detail", "/news-detail", "/notice-detail", "/post-detail",
  ])("supports the remaining existing page %s without inventing nested routes", (pathname) => {
    expect(getSafeLoginReturnTo(pathname)).toBe(pathname);
    expect(getSafeLoginReturnTo(`${pathname}/unexpected/extra`)).toBeNull();
  });
});
