import { toFanPulseError } from '@/lib/api-client';

export const SESSION_CHANGED = 'fanpulse:session-changed';
export const SESSION_INVALIDATED = 'fanpulse:session-invalidated';

// No credentials or user data are persisted. Storage events reach other same-origin tabs.
export function announceSessionChange() {
  try {
    localStorage.setItem(SESSION_CHANGED, `${Date.now()}:${Math.random()}`);
  } catch {
    // Storage can be disabled; focus/visibility revalidation remains available.
  }
}

export function expectedUserHeaders(userId?: string) {
  return userId ? { headers: { 'X-Expected-User-Id': userId } } : {};
}

export function privateRequestError(error: unknown) {
  const failure = toFanPulseError(error);
  if (typeof window !== 'undefined' && (failure.code === 'EXPECTED_USER_MISMATCH' || failure.code === 'EXPECTED_USER_REQUIRED')) {
    window.dispatchEvent(new Event(SESSION_INVALIDATED));
  }
  return failure;
}
