import { act, renderHook, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@/lib/api/social', () => ({ fetchNotifications: vi.fn(), markAllNotificationsRead: vi.fn() }));

import { fetchNotifications, markAllNotificationsRead } from '@/lib/api/social';
import { useNotifications } from './useNotifications';

const notification = {
  id: '22222222-2222-2222-2222-222222222222', type: 'NEWS', message: '실제 알림',
  isRead: false, createdAt: '2026-08-13T12:00:00',
};
const otherNotification = {
  ...notification,
  id: '44444444-4444-4444-4444-444444444444',
  message: '다른 사용자 알림',
};

function deferred<T>() {
  let resolve!: (value: T) => void;
  const promise = new Promise<T>((resolver) => { resolve = resolver; });
  return { promise, resolve };
}

describe('useNotifications', () => {
  beforeEach(() => vi.resetAllMocks());

  it('reloads API notifications when the unread filter changes', async () => {
    vi.mocked(fetchNotifications).mockResolvedValue([notification]);
    const { result, rerender } = renderHook(({ unread }) => useNotifications(unread, 'user-a'), {
      initialProps: { unread: false },
    });
    await waitFor(() => expect(result.current.state).toBe('success'));
    expect(fetchNotifications).toHaveBeenLastCalledWith(false, expect.any(AbortSignal));

    rerender({ unread: true });
    await waitFor(() => expect(fetchNotifications).toHaveBeenLastCalledWith(true, expect.any(AbortSignal)));
  });

  it('marks all rows read only after API success', async () => {
    vi.mocked(fetchNotifications).mockResolvedValue([notification]);
    vi.mocked(markAllNotificationsRead).mockResolvedValue(1);
    const { result } = renderHook(() => useNotifications(false, 'user-a'));
    await waitFor(() => expect(result.current.state).toBe('success'));

    await act(() => result.current.markAllRead());
    expect(result.current.notifications[0].isRead).toBe(true);
  });

  it('removes rows after mark-all succeeds in unread-only mode', async () => {
    vi.mocked(fetchNotifications).mockResolvedValue([notification]);
    vi.mocked(markAllNotificationsRead).mockResolvedValue(1);
    const { result } = renderHook(() => useNotifications(true, 'user-a'));
    await waitFor(() => expect(result.current.state).toBe('success'));

    await act(() => result.current.markAllRead());

    expect(result.current.notifications).toEqual([]);
  });

  it('clears the previous account rows while the next account reloads', async () => {
    const next = deferred<Array<typeof otherNotification>>();
    vi.mocked(fetchNotifications)
      .mockResolvedValueOnce([notification])
      .mockReturnValueOnce(next.promise);
    const { result, rerender } = renderHook(
      ({ userId }) => useNotifications(false, userId),
      { initialProps: { userId: 'user-a' } },
    );
    await waitFor(() => expect(result.current.notifications).toEqual([notification]));

    rerender({ userId: 'user-b' });
    await waitFor(() => expect(result.current.state).toBe('loading'));
    expect(result.current.notifications).toEqual([]);

    next.resolve([otherNotification]);
    await waitFor(() => expect(result.current.notifications).toEqual([otherNotification]));
  });

  it('keeps unread rows and exposes a handled error when mark-all fails', async () => {
    vi.mocked(fetchNotifications).mockResolvedValue([notification]);
    vi.mocked(markAllNotificationsRead).mockRejectedValue(new Error('network'));
    const { result } = renderHook(() => useNotifications(true, 'user-a'));
    await waitFor(() => expect(result.current.state).toBe('success'));

    await act(() => result.current.markAllRead());

    expect(result.current.notifications).toEqual([notification]);
    expect(result.current.mutationError).toBe('알림 읽음 처리에 실패했습니다');
  });
});
