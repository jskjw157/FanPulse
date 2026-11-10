'use client';

import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import type { AsyncState } from '@/types/common';
import {
  fetchNotificationPage, markAllNotificationsRead, markNotificationRead,
  type NotificationPage, type UserNotification,
} from '@/lib/api/social';
import { NOTIFICATION_CHANGED, notifyUnreadCountChanged, type NotificationChange } from './useNotificationUnreadCount';

interface Scope { userId: string | undefined; unreadOnly: boolean }
interface RequestContext { scope: Scope; signal: AbortSignal; sequence: number; busy: boolean }
interface Snapshot {
  scope: Scope;
  data: NotificationPage | null;
  page: number;
  state: AsyncState;
  error: string | null;
}
interface Mutation {
  scope: Scope;
  id: string | null;
  error: string | null;
  notice: string | null;
}

export function useNotifications(unreadOnly: boolean, userId: string | undefined) {
  // Identity, not just equality of IDs, prevents A -> B -> A request replay.
  const scope = useMemo(() => ({ userId, unreadOnly }), [userId, unreadOnly]);
  const context = useRef<RequestContext | null>(null);
  const [snapshot, setSnapshot] = useState<Snapshot>({ scope, data: null, page: 0, state: 'loading', error: null });
  const [mutation, setMutation] = useState<Mutation>({ scope, id: null, error: null, notice: null });

  const load = useCallback(async (page: number, current: RequestContext) => {
    const sequence = ++current.sequence;
    setSnapshot({ scope, data: null, page, state: 'loading', error: null });
    if (!userId) {
      setSnapshot({ scope, data: null, page, state: 'error', error: '인증된 사용자 정보를 확인할 수 없습니다' });
      return;
    }
    try {
      let data = await fetchNotificationPage(unreadOnly, page, 20, current.signal);
      if (current.signal.aborted || sequence !== current.sequence) return;
      // Another session can read rows while an offset page is loading.
      if (page > 0 && page >= data.totalPages) {
        data = await fetchNotificationPage(unreadOnly, 0, 20, current.signal);
        if (current.signal.aborted || sequence !== current.sequence) return;
      }
      setSnapshot({ scope, data, page: data.page, state: 'success', error: null });
      notifyUnreadCountChanged(userId);
    } catch {
      if (current.signal.aborted || sequence !== current.sequence) return;
      setSnapshot({ scope, data: null, page, state: 'error', error: '알림을 불러올 수 없습니다' });
    }
  }, [scope, unreadOnly, userId]);

  useEffect(() => {
    const controller = new AbortController();
    const current = { scope, signal: controller.signal, sequence: 0, busy: false };
    context.current = current;
    void load(0, current);
    const onChanged = (event: Event) => {
      const change = (event as CustomEvent<NotificationChange>).detail;
      if (change.userId === userId && change.reloadList && !current.busy) void load(0, current);
    };
    window.addEventListener(NOTIFICATION_CHANGED, onChanged);
    return () => {
      controller.abort();
      window.removeEventListener(NOTIFICATION_CHANGED, onChanged);
    };
  }, [load, scope, userId]);

  const goToPage = useCallback(async (page: number) => {
    const current = context.current;
    if (!current || current.scope !== scope || current.signal.aborted || current.busy) return;
    await load(page, current);
  }, [load, scope]);

  const mutate = useCallback(async (id: string | null): Promise<UserNotification | null> => {
    const current = context.current;
    if (!userId || !current || current.scope !== scope || current.signal.aborted || current.busy) return null;
    current.busy = true;
    ++current.sequence; // Invalidate any older list request before changing read state.
    setMutation({ scope, id: id ?? 'all', error: null, notice: null });
    try {
      const row = id ? await markNotificationRead(id) : (await markAllNotificationsRead(), null);
      if (current.signal.aborted) {
        // The server still applied the write. Refresh the active account/filter,
        // but never replay old row data or navigation into the new view.
        notifyUnreadCountChanged(userId, true);
        return null;
      }
      const unavailable = row?.targetType === 'POST' && row.targetAvailable === false;
      setMutation({ scope, id: null, error: null, notice: unavailable ? '삭제되었거나 사용할 수 없는 게시글입니다.' : null });
      setSnapshot((previous) => {
        if (previous.scope !== scope || !previous.data) return previous;
        const wasUnread = row && previous.data.content.some((item) => item.id === row.id && !item.isRead);
        return { ...previous, data: {
          ...previous.data,
          unreadCount: row ? Math.max(0, previous.data.unreadCount - (wasUnread ? 1 : 0)) : 0,
          content: previous.data.content.map((item) => row ? (item.id === row.id ? row : item) : { ...item, isRead: true }),
        } };
      });
      notifyUnreadCountChanged(userId);
      // Removing read rows shifts offset pages; start from page zero to avoid skips.
      if (unreadOnly) await load(0, current);
      if (current.signal.aborted) return null;
      return row;
    } catch {
      if (!current.signal.aborted) setMutation({ scope, id: null, error: '알림 읽음 처리에 실패했습니다', notice: null });
      return null;
    } finally {
      current.busy = false;
    }
  }, [load, scope, unreadOnly, userId]);

  const data = snapshot.scope === scope ? snapshot.data : null;
  const currentMutation = mutation.scope === scope ? mutation : null;
  return {
    notifications: data?.content ?? [],
    page: data?.page ?? (snapshot.scope === scope ? snapshot.page : 0),
    totalPages: data?.totalPages ?? 0,
    last: data?.last ?? true,
    unreadCount: data?.unreadCount ?? 0,
    state: snapshot.scope === scope ? snapshot.state : 'loading' as AsyncState,
    error: snapshot.scope === scope ? snapshot.error : null,
    retry: () => goToPage(snapshot.scope === scope ? snapshot.page : 0),
    goToPage,
    markAllRead: () => mutate(null),
    markRead: (id: string) => mutate(id),
    markingAll: currentMutation?.id === 'all',
    readingId: currentMutation?.id === 'all' ? null : currentMutation?.id ?? null,
    mutationError: currentMutation?.error ?? null,
    notice: currentMutation?.notice ?? null,
  };
}
