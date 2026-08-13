import { act, renderHook, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@/lib/api/social', () => ({ fetchNotifications: vi.fn(), markAllNotificationsRead: vi.fn() }));

import { fetchNotifications, markAllNotificationsRead } from '@/lib/api/social';
import { useNotifications } from './useNotifications';

const notification = {
  id: '22222222-2222-2222-2222-222222222222', type: 'NEWS', message: '실제 알림',
  isRead: false, createdAt: '2026-08-13T12:00:00',
};

describe('useNotifications', () => {
  beforeEach(() => vi.clearAllMocks());

  it('reloads API notifications when the unread filter changes', async () => {
    vi.mocked(fetchNotifications).mockResolvedValue([notification]);
    const { result, rerender } = renderHook(({ unread }) => useNotifications(unread), {
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
    const { result } = renderHook(() => useNotifications(false));
    await waitFor(() => expect(result.current.state).toBe('success'));

    await act(() => result.current.markAllRead());
    expect(result.current.notifications[0].isRead).toBe(true);
  });
});
