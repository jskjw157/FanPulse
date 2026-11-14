import { act, renderHook, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@/lib/api/social', () => ({ fetchNotificationPage: vi.fn(), markAllNotificationsRead: vi.fn(), markNotificationRead: vi.fn() }));

import { fetchNotificationPage, markAllNotificationsRead, markNotificationRead, type NotificationPage } from '@/lib/api/social';
import { useNotifications } from './useNotifications';
import { notifyUnreadCountChanged } from './useNotificationUnreadCount';

const notification = {
  id: '22222222-2222-2222-2222-222222222222', type: 'POST_LIKED', message: '실제 알림',
  isRead: false, createdAt: '2026-08-13T12:00:00', targetType: 'POST',
  targetId: '33333333-3333-3333-3333-333333333333', targetAvailable: true,
};
const page: NotificationPage = { content: [notification], page: 0, size: 20, totalElements: 21, totalPages: 2, last: false, unreadCount: 17 };
const empty: NotificationPage = { ...page, content: [], totalElements: 0, totalPages: 0, last: true, unreadCount: 0 };

function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason: Error) => void;
  const promise = new Promise<T>((resolver, rejecter) => { resolve = resolver; reject = rejecter; });
  return { promise, resolve, reject };
}

describe('useNotifications', () => {
  beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(fetchNotificationPage).mockResolvedValue(page);
    vi.mocked(markAllNotificationsRead).mockResolvedValue(17);
    vi.mocked(markNotificationRead).mockResolvedValue({ ...notification, isRead: true });
  });

  it('uses server pagination and account-wide unread count, not visible row count', async () => {
    const { result } = renderHook(() => useNotifications(false, 'user-a'));
    await waitFor(() => expect(result.current.state).toBe('success'));
    expect(result.current.unreadCount).toBe(17);
    await act(() => result.current.goToPage(1));
    expect(fetchNotificationPage).toHaveBeenLastCalledWith(false, 1, 20, expect.any(AbortSignal));
  });

  it('resets the page when the unread filter changes and ignores late responses', async () => {
    const late = deferred<NotificationPage>();
    vi.mocked(fetchNotificationPage).mockReturnValueOnce(late.promise).mockResolvedValueOnce(empty);
    const { result, rerender } = renderHook(({ unread }) => useNotifications(unread, 'user-a'), { initialProps: { unread: false } });
    rerender({ unread: true });
    await waitFor(() => expect(result.current.state).toBe('success'));
    await act(async () => { late.resolve(page); });
    expect(result.current.notifications).toEqual([]);
    expect(fetchNotificationPage).toHaveBeenLastCalledWith(true, 0, 20, expect.any(AbortSignal));
  });

  it('marks all visible rows read only after API success and clears the account count', async () => {
    const pending = deferred<number>();
    vi.mocked(markAllNotificationsRead).mockReturnValue(pending.promise);
    const { result } = renderHook(() => useNotifications(false, 'user-a'));
    await waitFor(() => expect(result.current.state).toBe('success'));
    let action!: Promise<unknown>;
    act(() => { action = result.current.markAllRead(); });
    expect(result.current.notifications[0].isRead).toBe(false);
    await act(async () => { pending.resolve(17); await action; });
    expect(result.current.notifications[0].isRead).toBe(true);
    expect(result.current.unreadCount).toBe(0);
  });

  it('reloads unread page zero after mark-all, including when currently on a later page', async () => {
    const { result } = renderHook(() => useNotifications(true, 'user-a'));
    await waitFor(() => expect(result.current.state).toBe('success'));
    await act(() => result.current.goToPage(1));
    vi.mocked(fetchNotificationPage).mockResolvedValue(empty);
    await act(() => result.current.markAllRead());
    expect(result.current.notifications).toEqual([]);
    expect(result.current.page).toBe(0);
    expect(fetchNotificationPage).toHaveBeenLastCalledWith(true, 0, 20, expect.any(AbortSignal));
  });

  it('clears account rows synchronously and ignores A -> B -> A responses', async () => {
    const late = deferred<NotificationPage>();
    const next = deferred<NotificationPage>();
    vi.mocked(fetchNotificationPage).mockReturnValueOnce(late.promise).mockResolvedValueOnce(empty).mockReturnValueOnce(next.promise);
    const { result, rerender } = renderHook(({ userId }) => useNotifications(false, userId), { initialProps: { userId: 'user-a' } });
    rerender({ userId: 'user-b' });
    await waitFor(() => expect(result.current.state).toBe('success'));
    rerender({ userId: 'user-a' });
    expect(result.current.notifications).toEqual([]);
    await act(async () => { late.resolve(page); });
    expect(result.current.state).toBe('loading');
    await act(async () => { next.resolve(empty); });
    expect(result.current.notifications).toEqual([]);
  });

  it.each(['account', 'filter'])('does not return a navigation target from a stale %s mutation', async (change) => {
    const late = deferred<typeof notification>();
    vi.mocked(markNotificationRead).mockReturnValue(late.promise);
    const { result, rerender } = renderHook(({ userId, unread }) => useNotifications(unread, userId), {
      initialProps: { userId: 'user-a', unread: false },
    });
    await waitFor(() => expect(result.current.state).toBe('success'));
    let action!: ReturnType<typeof result.current.markRead>;
    act(() => { action = result.current.markRead(notification.id); });
    rerender(change === 'account' ? { userId: 'user-b', unread: false } : { userId: 'user-a', unread: true });
    let returned;
    await act(async () => { late.resolve({ ...notification, isRead: true }); returned = await action; });
    expect(returned).toBeNull();
    expect(result.current.notice).toBeNull();
    expect(result.current.notifications[0].isRead).toBe(false);
  });

  it.each(['success', 'failure'])('reconciles a stale unread read after switching to All while a second read is pending: %s', async (outcome) => {
    const second = { ...notification, id: '44444444-4444-4444-4444-444444444444' };
    const bothUnread: NotificationPage = { ...page, content: [notification, second], totalElements: 2, totalPages: 1, last: true, unreadCount: 2 };
    const firstRead = deferred<typeof notification>();
    const secondRead = deferred<typeof notification>();
    const reconciled = deferred<NotificationPage>();
    vi.mocked(fetchNotificationPage).mockResolvedValueOnce(bothUnread).mockResolvedValueOnce(bothUnread).mockReturnValueOnce(reconciled.promise);
    vi.mocked(markNotificationRead).mockReturnValueOnce(firstRead.promise).mockReturnValueOnce(secondRead.promise);
    const { result, rerender } = renderHook(({ unread }) => useNotifications(unread, 'user-a'), { initialProps: { unread: true } });
    await waitFor(() => expect(result.current.state).toBe('success'));
    let firstAction!: ReturnType<typeof result.current.markRead>;
    act(() => { firstAction = result.current.markRead(notification.id); });

    rerender({ unread: false });
    await waitFor(() => expect(result.current.state).toBe('success'));
    expect(result.current.notifications).toEqual(bothUnread.content);
    expect(result.current.readingId).toBeNull();
    let secondAction!: ReturnType<typeof result.current.markRead>;
    act(() => { secondAction = result.current.markRead(second.id); });
    expect(vi.mocked(markNotificationRead).mock.calls).toEqual([[notification.id], [second.id]]);

    // A's stale completion emits the real reloadList event; no event is injected here.
    await act(async () => {
      firstRead.resolve({ ...notification, isRead: true, targetAvailable: false });
      expect(await firstAction).toBeNull();
    });
    expect(result.current.readingId).toBe(second.id);
    expect(result.current.notifications).toEqual(bothUnread.content);
    expect(result.current.unreadCount).toBe(2);
    expect(result.current.notice).toBeNull();
    expect(fetchNotificationPage).toHaveBeenCalledTimes(2);

    await act(async () => {
      if (outcome === 'success') secondRead.resolve({ ...second, isRead: true });
      else secondRead.reject(new Error('network'));
      expect(await secondAction).toEqual(outcome === 'success' ? { ...second, isRead: true } : null);
    });
    const fresh: NotificationPage = {
      ...bothUnread,
      content: [{ ...notification, isRead: true, targetAvailable: false }, { ...second, isRead: outcome === 'success' }],
      unreadCount: outcome === 'success' ? 0 : 1,
    };
    await act(async () => { reconciled.resolve(fresh); });
    await waitFor(() => expect({ notifications: result.current.notifications, unreadCount: result.current.unreadCount })
      .toEqual({ notifications: fresh.content, unreadCount: fresh.unreadCount }));
    expect(result.current.state).toBe('success');
    expect(result.current.mutationError).toBe(outcome === 'success' ? null : '알림 읽음 처리에 실패했습니다');
    expect(result.current.notice).toBeNull();
    expect(fetchNotificationPage).toHaveBeenCalledTimes(3);
    expect(fetchNotificationPage).toHaveBeenLastCalledWith(false, 0, 20, expect.any(AbortSignal));
  });

  it.each(['individual', 'all'])('coalesces matching reload invalidations while an %s read is pending', async (kind) => {
    const pending = deferred<typeof notification>();
    const pendingAll = deferred<number>();
    vi.mocked(markNotificationRead).mockReturnValue(pending.promise);
    vi.mocked(markAllNotificationsRead).mockReturnValue(pendingAll.promise);
    const { result } = renderHook(() => useNotifications(false, 'user-a'));
    await waitFor(() => expect(result.current.state).toBe('success'));
    let action!: Promise<unknown>;
    act(() => { action = kind === 'all' ? result.current.markAllRead() : result.current.markRead(notification.id); });
    act(() => {
      notifyUnreadCountChanged('user-a', true);
      notifyUnreadCountChanged('user-a', true);
    });
    expect(fetchNotificationPage).toHaveBeenCalledTimes(1);
    vi.mocked(fetchNotificationPage).mockResolvedValue(empty);
    await act(async () => {
      if (kind === 'all') pendingAll.resolve(17);
      else pending.resolve({ ...notification, isRead: true });
      await action;
    });
    expect(fetchNotificationPage).toHaveBeenCalledTimes(2);
    expect(result.current.notifications).toEqual([]);
    expect(result.current.unreadCount).toBe(0);
  });

  it('does not queue count-only or another account invalidations while busy', async () => {
    const pending = deferred<typeof notification>();
    vi.mocked(markNotificationRead).mockReturnValue(pending.promise);
    const { result } = renderHook(() => useNotifications(false, 'user-a'));
    await waitFor(() => expect(result.current.state).toBe('success'));
    let action!: ReturnType<typeof result.current.markRead>;
    act(() => { action = result.current.markRead(notification.id); });
    act(() => {
      notifyUnreadCountChanged('user-b', true);
      notifyUnreadCountChanged('user-a');
    });
    await act(async () => { pending.resolve({ ...notification, isRead: true }); await action; });
    expect(fetchNotificationPage).toHaveBeenCalledTimes(1);
    expect(result.current.notifications[0].isRead).toBe(true);
    expect(result.current.unreadCount).toBe(16);
  });

  it.each(['account', 'filter'])('discards a queued invalidation when its %s scope is aborted', async (change) => {
    const pending = deferred<typeof notification>();
    vi.mocked(markNotificationRead).mockReturnValue(pending.promise);
    const { result, rerender } = renderHook(({ userId, unread }) => useNotifications(unread, userId), {
      initialProps: { userId: 'user-a', unread: false },
    });
    await waitFor(() => expect(result.current.state).toBe('success'));
    let action!: ReturnType<typeof result.current.markRead>;
    act(() => { action = result.current.markRead(notification.id); });
    act(() => { notifyUnreadCountChanged('user-a', true); });
    vi.mocked(fetchNotificationPage).mockResolvedValue(empty);
    rerender(change === 'account' ? { userId: 'user-b', unread: false } : { userId: 'user-a', unread: true });
    await waitFor(() => expect(result.current.state).toBe('success'));
    await act(async () => {
      // A rejected old mutation emits no new invalidation into the active scope.
      pending.reject(new Error('network'));
      expect(await action).toBeNull();
    });
    expect(fetchNotificationPage).toHaveBeenCalledTimes(2);
    expect(result.current.notifications).toEqual([]);
    expect(result.current.unreadCount).toBe(0);
    expect(result.current.mutationError).toBeNull();
  });

  it.each(['account', 'filter'])('ignores a deferred reconciliation response after a %s change', async (change) => {
    const pending = deferred<typeof notification>();
    const reloaded = deferred<NotificationPage>();
    vi.mocked(markNotificationRead).mockReturnValue(pending.promise);
    vi.mocked(fetchNotificationPage).mockResolvedValueOnce(page).mockReturnValueOnce(reloaded.promise).mockResolvedValueOnce(empty);
    const { result, rerender } = renderHook(({ userId, unread }) => useNotifications(unread, userId), {
      initialProps: { userId: 'user-a', unread: false },
    });
    await waitFor(() => expect(result.current.state).toBe('success'));
    let action!: ReturnType<typeof result.current.markRead>;
    act(() => { action = result.current.markRead(notification.id); });
    act(() => { notifyUnreadCountChanged('user-a', true); });
    await act(async () => { pending.resolve({ ...notification, isRead: true }); await action; });
    expect(fetchNotificationPage).toHaveBeenCalledTimes(2);
    const signal = vi.mocked(fetchNotificationPage).mock.calls[1][3];
    rerender(change === 'account' ? { userId: 'user-b', unread: false } : { userId: 'user-a', unread: true });
    await waitFor(() => expect(result.current.state).toBe('success'));
    expect(signal?.aborted).toBe(true);
    await act(async () => { reloaded.resolve(page); });
    expect(result.current.notifications).toEqual([]);
    expect(result.current.unreadCount).toBe(0);
    expect(fetchNotificationPage).toHaveBeenCalledTimes(3);
  });

  it('consumes a queued invalidation in the mandatory unread reload', async () => {
    const pending = deferred<typeof notification>();
    vi.mocked(markNotificationRead).mockReturnValue(pending.promise);
    const { result } = renderHook(() => useNotifications(true, 'user-a'));
    await waitFor(() => expect(result.current.state).toBe('success'));
    let action!: ReturnType<typeof result.current.markRead>;
    act(() => { action = result.current.markRead(notification.id); });
    act(() => { notifyUnreadCountChanged('user-a', true); });
    vi.mocked(fetchNotificationPage).mockResolvedValue(empty);
    await act(async () => { pending.resolve({ ...notification, isRead: true }); await action; });
    expect(result.current.notifications).toEqual([]);
    expect(fetchNotificationPage).toHaveBeenCalledTimes(2);
  });

  it('retains an invalidation arriving during the unread mutation reload', async () => {
    const pending = deferred<typeof notification>();
    const firstReload = deferred<NotificationPage>();
    vi.mocked(markNotificationRead).mockReturnValue(pending.promise);
    vi.mocked(fetchNotificationPage).mockResolvedValueOnce(page).mockReturnValueOnce(firstReload.promise).mockResolvedValueOnce(empty);
    const { result } = renderHook(() => useNotifications(true, 'user-a'));
    await waitFor(() => expect(result.current.state).toBe('success'));
    let action!: ReturnType<typeof result.current.markRead>;
    act(() => { action = result.current.markRead(notification.id); });
    await act(async () => { pending.resolve({ ...notification, isRead: true }); });
    expect(fetchNotificationPage).toHaveBeenCalledTimes(2);
    act(() => { notifyUnreadCountChanged('user-a', true); });
    await act(async () => { firstReload.resolve(page); await action; });
    expect(fetchNotificationPage).toHaveBeenCalledTimes(3);
    expect(result.current.notifications).toEqual([]);
    expect(result.current.unreadCount).toBe(0);
  });

  it('uses fresh target availability and exposes a deleted notice after a successful read', async () => {
    vi.mocked(markNotificationRead).mockResolvedValue({ ...notification, isRead: true, targetAvailable: false });
    const { result } = renderHook(() => useNotifications(false, 'user-a'));
    await waitFor(() => expect(result.current.state).toBe('success'));
    await act(() => result.current.markRead(notification.id));
    expect(result.current.notifications[0].isRead).toBe(true);
    expect(result.current.notice).toBe('삭제되었거나 사용할 수 없는 게시글입니다.');
    expect(result.current.unreadCount).toBe(16);
  });

  it('reloads unread offsets after an individual read', async () => {
    const { result } = renderHook(() => useNotifications(true, 'user-a'));
    await waitFor(() => expect(result.current.state).toBe('success'));
    vi.mocked(fetchNotificationPage).mockResolvedValue(empty);
    await act(() => result.current.markRead(notification.id));
    expect(result.current.notifications).toEqual([]);
    expect(fetchNotificationPage).toHaveBeenLastCalledWith(true, 0, 20, expect.any(AbortSignal));
  });

  it.each(['individual', 'all'])('preserves unread rows and exposes a handled %s read failure', async (kind) => {
    vi.mocked(markAllNotificationsRead).mockRejectedValue(new Error('network'));
    vi.mocked(markNotificationRead).mockRejectedValue(new Error('network'));
    const { result } = renderHook(() => useNotifications(true, 'user-a'));
    await waitFor(() => expect(result.current.state).toBe('success'));
    await act(() => kind === 'all' ? result.current.markAllRead() : result.current.markRead(notification.id));
    expect(result.current.notifications).toEqual([notification]);
    expect(result.current.unreadCount).toBe(17);
    expect(result.current.mutationError).toBe('알림 읽음 처리에 실패했습니다');
  });

  it('handles failed page loading and retry without fabricating notifications', async () => {
    vi.mocked(fetchNotificationPage).mockRejectedValueOnce(new Error('offline'));
    const { result } = renderHook(() => useNotifications(false, 'user-a'));
    await waitFor(() => expect(result.current.state).toBe('error'));
    expect(result.current.notifications).toEqual([]);
    await act(() => result.current.retry());
    expect(result.current.notifications).toEqual([notification]);
  });

  it('recovers to page zero when another session empties a later unread page', async () => {
    const { result } = renderHook(() => useNotifications(true, 'user-a'));
    await waitFor(() => expect(result.current.state).toBe('success'));
    vi.mocked(fetchNotificationPage).mockResolvedValueOnce({ ...empty, page: 1 }).mockResolvedValueOnce(empty);
    await act(() => result.current.goToPage(1));
    expect(result.current.page).toBe(0);
    expect(result.current.notifications).toEqual([]);
    expect(fetchNotificationPage).toHaveBeenLastCalledWith(true, 0, 20, expect.any(AbortSignal));
  });
});
