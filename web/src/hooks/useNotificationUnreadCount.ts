'use client';

import { useEffect, useMemo, useState } from 'react';
import { fetchNotificationUnreadCount } from '@/lib/api/social';

export const NOTIFICATION_CHANGED = 'fanpulse:notification-read';
export interface NotificationChange { userId: string; reloadList: boolean }

export function notifyUnreadCountChanged(userId: string, reloadList = false) {
  window.dispatchEvent(new CustomEvent<NotificationChange>(NOTIFICATION_CHANGED, { detail: { userId, reloadList } }));
}

export function useNotificationUnreadCount(userId: string | undefined, sessionVersion = 0) {
  const scope = useMemo(() => ({ userId, sessionVersion }), [userId, sessionVersion]);
  const [snapshot, setSnapshot] = useState<{ scope: typeof scope; count: number | null; error: boolean }>({ scope, count: null, error: false });

  useEffect(() => {
    if (!userId) return;
    let controller: AbortController;
    const refresh = async () => {
      controller?.abort();
      const request = new AbortController();
      controller = request;
      try {
        const count = await fetchNotificationUnreadCount(request.signal, userId);
        if (!request.signal.aborted) setSnapshot({ scope, count, error: false });
      } catch {
        if (!request.signal.aborted) setSnapshot({ scope, count: null, error: true });
      }
    };
    const onChanged = (event: Event) => {
      if ((event as CustomEvent<NotificationChange>).detail.userId === userId) void refresh();
    };
    void refresh();
    window.addEventListener(NOTIFICATION_CHANGED, onChanged);
    // AuthProvider revalidates identity on focus before opening a new private scope.
    return () => {
      controller?.abort();
      window.removeEventListener(NOTIFICATION_CHANGED, onChanged);
    };
  }, [scope, userId]);

  return snapshot.scope === scope && userId ? { count: snapshot.count, error: snapshot.error } : { count: null, error: false };
}
