import { act, renderHook, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

const auth = vi.hoisted(() => ({ user: { id: 'user-a' } as { id: string } | null, isLoading: false, sessionVersion: 1 }));
vi.mock('@/contexts/AuthContext', () => ({ useAuth: () => auth }));
vi.mock('@/lib/api/user', () => ({ fetchMyProfile: vi.fn() }));

import { fetchMyProfile, type UserProfile } from '@/lib/api/user';
import { useMyProfile } from './useMyProfile';

const alice: UserProfile = { id: 'user-a', email: 'alice@example.com', username: 'Alice', hasPassword: false, createdAt: '2026-01-01T00:00:00Z' };
const bob = { ...alice, id: 'user-b', email: 'bob@example.com', username: 'Bob' };

function deferred<T>() {
  let resolve!: (value: T) => void;
  const promise = new Promise<T>((done) => { resolve = done; });
  return { promise, resolve };
}

describe('useMyProfile account scopes', () => {
  beforeEach(() => {
    vi.resetAllMocks();
    auth.user = { id: alice.id };
    auth.isLoading = false;
    auth.sessionVersion = 1;
    vi.mocked(fetchMyProfile).mockResolvedValue(alice);
  });

  it('cancels a queued effect refresh when unmounted before the microtask', async () => {
    const { unmount } = renderHook(() => useMyProfile());
    unmount();
    await act(async () => { await Promise.resolve(); });
    expect(fetchMyProfile).not.toHaveBeenCalled();
  });

  it('hides private data while identity is unknown and makes no private request', async () => {
    const { result, rerender } = renderHook(() => useMyProfile());
    await waitFor(() => expect(result.current.profile).toEqual(alice));
    auth.isLoading = true;
    auth.sessionVersion++;
    rerender();
    expect(result.current.profile).toBeNull();
    expect(result.current.state).toBe('loading');
    expect(fetchMyProfile).toHaveBeenCalledTimes(1);
    auth.user = { id: bob.id };
    auth.isLoading = false;
    vi.mocked(fetchMyProfile).mockResolvedValue(bob);
    rerender();
    await waitFor(() => expect(result.current.profile).toEqual(bob));
    expect(fetchMyProfile).toHaveBeenLastCalledWith(expect.any(AbortSignal), bob.id);
  });

  it('invalidates a manual refresh across A -> B -> A and ignores its late response', async () => {
    const late = deferred<UserProfile>();
    const current = deferred<UserProfile>();
    const { result, rerender } = renderHook(() => useMyProfile());
    await waitFor(() => expect(result.current.profile).toEqual(alice));
    vi.mocked(fetchMyProfile).mockReturnValueOnce(late.promise);
    let refresh!: Promise<void>;
    act(() => { refresh = result.current.refresh(); });
    auth.user = { id: bob.id };
    auth.sessionVersion++;
    vi.mocked(fetchMyProfile).mockResolvedValueOnce(bob);
    rerender();
    await waitFor(() => expect(result.current.profile).toEqual(bob));
    auth.user = { id: alice.id };
    auth.sessionVersion++;
    vi.mocked(fetchMyProfile).mockReturnValueOnce(current.promise);
    rerender();
    await act(async () => { late.resolve(alice); await refresh; });
    expect(result.current.profile).toBeNull();
    await act(async () => { current.resolve({ ...alice, username: 'Fresh_Alice' }); });
    expect(result.current.profile?.username).toBe('Fresh_Alice');
  });

  it('does not expose a different profile even if the API returns one', async () => {
    vi.mocked(fetchMyProfile).mockResolvedValue(bob);
    const { result } = renderHook(() => useMyProfile());
    await waitFor(() => expect(result.current.state).toBe('error'));
    expect(result.current.profile).toBeNull();
  });
});
