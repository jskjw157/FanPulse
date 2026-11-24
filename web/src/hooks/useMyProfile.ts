"use client";

import { useState, useEffect, useCallback, useMemo, useRef } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { fetchMyProfile, type UserProfile } from '@/lib/api/user';
import type { AsyncState } from '@/types/common';

interface UseMyProfileReturn {
  profile: UserProfile | null;
  state: AsyncState;
  error: string | null;
  refresh: () => Promise<void>;
}

export function useMyProfile(): UseMyProfileReturn {
  const { user, isLoading, sessionVersion } = useAuth();
  const userId = isLoading ? undefined : user?.id;
  const scope = useMemo(() => ({ userId, sessionVersion }), [userId, sessionVersion]);
  const request = useRef<AbortController | null>(null);
  const [snapshot, setSnapshot] = useState<{
    scope: typeof scope; profile: UserProfile | null; state: AsyncState; error: string | null;
  }>({ scope, profile: null, state: 'loading', error: null });

  const refresh = useCallback(async () => {
    request.current?.abort();
    const controller = new AbortController();
    request.current = controller;
    setSnapshot({ scope, profile: null, state: 'loading', error: null });
    if (!userId) return;
    try {
      const profile = await fetchMyProfile(controller.signal, userId);
      if (controller.signal.aborted) return;
      if (profile.id !== userId) throw new Error('Profile user mismatch');
      setSnapshot({ scope, profile, state: 'success', error: null });
    } catch {
      if (!controller.signal.aborted) {
        setSnapshot({ scope, profile: null, state: 'error', error: '프로필을 불러올 수 없습니다' });
      }
    }
  }, [scope, userId]);

  useEffect(() => {
    let active = true;
    queueMicrotask(() => {
      if (active) void refresh();
    });
    return () => {
      active = false;
      request.current?.abort();
    };
  }, [refresh]);

  return {
    profile: snapshot.scope === scope && userId ? snapshot.profile : null,
    state: snapshot.scope === scope && userId ? snapshot.state : 'loading',
    error: snapshot.scope === scope && userId ? snapshot.error : null,
    refresh,
  };
}
