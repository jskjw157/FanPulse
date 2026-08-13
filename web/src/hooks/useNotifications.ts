'use client';

import { useCallback, useEffect, useState } from 'react';
import type { AsyncState } from '@/types/common';
import {
  fetchNotifications,
  markAllNotificationsRead,
  type UserNotification,
} from '@/lib/api/social';

export function useNotifications(unreadOnly: boolean) {
  const [notifications, setNotifications] = useState<UserNotification[]>([]);
  const [state, setState] = useState<AsyncState>('loading');
  const [error, setError] = useState<string | null>(null);
  const [markingAll, setMarkingAll] = useState(false);

  const load = useCallback(async (signal?: AbortSignal) => {
    setState('loading');
    setError(null);
    try {
      const rows = await fetchNotifications(unreadOnly, signal);
      if (signal?.aborted) return;
      setNotifications(rows);
      setState('success');
    } catch {
      if (signal?.aborted) return;
      setNotifications([]);
      setError('알림을 불러올 수 없습니다');
      setState('error');
    }
  }, [unreadOnly]);

  useEffect(() => {
    const controller = new AbortController();
    void load(controller.signal);
    return () => controller.abort();
  }, [load]);

  const markAllRead = useCallback(async () => {
    setMarkingAll(true);
    try {
      await markAllNotificationsRead();
      setNotifications((current) => current.map((row) => ({ ...row, isRead: true })));
    } finally {
      setMarkingAll(false);
    }
  }, []);

  return { notifications, state, error, retry: load, markAllRead, markingAll };
}
