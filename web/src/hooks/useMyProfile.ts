"use client";

import { useState, useEffect, useCallback } from 'react';
import { fetchMyProfile, type UserProfile } from '@/lib/api/user';
import type { AsyncState } from '@/types/common';

interface UseMyProfileReturn {
  profile: UserProfile | null;
  state: AsyncState;
  error: string | null;
  refresh: () => Promise<void>;
}

export function useMyProfile(): UseMyProfileReturn {
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [state, setState] = useState<AsyncState>('loading');
  const [error, setError] = useState<string | null>(null);

  const fetchProfile = useCallback((signal?: AbortSignal) =>
    fetchMyProfile(signal).then(
      (data) => {
        if (signal?.aborted) return;
        setProfile(data);
        setState('success');
      },
      () => {
        if (signal?.aborted) return;
        setError('프로필을 불러올 수 없습니다');
        setState('error');
      }
    ), []);

  const refresh = useCallback(async () => {
    setProfile(null);
    setState('loading');
    setError(null);
    await fetchProfile();
  }, [fetchProfile]);

  useEffect(() => {
    const abortController = new AbortController();
    void fetchProfile(abortController.signal);

    return () => {
      abortController.abort();
    };
  }, [fetchProfile]);

  return {
    profile,
    state,
    error,
    refresh,
  };
}
