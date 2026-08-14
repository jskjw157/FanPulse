'use client';

import { useCallback, useEffect, useState } from 'react';
import type { AsyncState } from '@/types/common';
import {
  fetchNotifications,
  markAllNotificationsRead,
  type UserNotification,
} from '@/lib/api/social';

interface NotificationsSnapshot {
  userId: string | undefined;
  unreadOnly: boolean;
  notifications: UserNotification[];
  state: AsyncState;
  error: string | null;
}

interface NotificationMutationState {
  userId: string | undefined;
  markingAll: boolean;
  error: string | null;
}

export function useNotifications(unreadOnly: boolean, userId: string | undefined) {
  const [snapshot, setSnapshot] = useState<NotificationsSnapshot>({
    userId,
    unreadOnly,
    notifications: [],
    state: 'loading',
    error: null,
  });
  const [mutation, setMutation] = useState<NotificationMutationState>({
    userId,
    markingAll: false,
    error: null,
  });

  const load = useCallback(async (signal?: AbortSignal) => {
    if (!userId) {
      setSnapshot({
        userId,
        unreadOnly,
        notifications: [],
        state: 'error',
        error: '인증된 사용자 정보를 확인할 수 없습니다',
      });
      return;
    }

    setSnapshot({ userId, unreadOnly, notifications: [], state: 'loading', error: null });
    setMutation({ userId, markingAll: false, error: null });
    try {
      const rows = await fetchNotifications(unreadOnly, signal);
      if (signal?.aborted) return;
      setSnapshot({ userId, unreadOnly, notifications: rows, state: 'success', error: null });
    } catch {
      if (signal?.aborted) return;
      setSnapshot({
        userId,
        unreadOnly,
        notifications: [],
        state: 'error',
        error: '알림을 불러올 수 없습니다',
      });
    }
  }, [unreadOnly, userId]);

  useEffect(() => {
    const controller = new AbortController();
    void load(controller.signal);
    return () => controller.abort();
  }, [load]);

  const markAllRead = useCallback(async () => {
    setMutation({ userId, markingAll: true, error: null });
    try {
      await markAllNotificationsRead();
      setSnapshot((current) => {
        if (current.userId !== userId || current.unreadOnly !== unreadOnly) return current;
        return {
          ...current,
          notifications: unreadOnly
            ? []
            : current.notifications.map((row) => ({ ...row, isRead: true })),
        };
      });
      setMutation({ userId, markingAll: false, error: null });
    } catch {
      setMutation({ userId, markingAll: false, error: '알림 읽음 처리에 실패했습니다' });
    }
  }, [unreadOnly, userId]);

  const isCurrentSnapshot = snapshot.userId === userId && snapshot.unreadOnly === unreadOnly;
  const isCurrentMutation = mutation.userId === userId;

  return {
    notifications: isCurrentSnapshot ? snapshot.notifications : [],
    state: isCurrentSnapshot ? snapshot.state : 'loading' as AsyncState,
    error: isCurrentSnapshot ? snapshot.error : null,
    retry: load,
    markAllRead,
    markingAll: isCurrentMutation ? mutation.markingAll : false,
    mutationError: isCurrentMutation ? mutation.error : null,
  };
}
