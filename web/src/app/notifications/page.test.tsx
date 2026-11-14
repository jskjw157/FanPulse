import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@/components/auth/ProtectedRoute', () => ({ default: ({ children }: { children: React.ReactNode }) => children }));
const push = vi.hoisted(() => vi.fn());
vi.mock('next/navigation', () => ({ useRouter: () => ({ back: vi.fn(), push }) }));
vi.mock('@/hooks/useNotifications', () => ({ useNotifications: vi.fn() }));
vi.mock('@/contexts/AuthContext', () => ({
  useAuth: () => ({ user: { id: 'user-a', email: 'a@example.com' }, isLoading: false, sessionVersion: 0 }),
}));

import NotificationsPage from './page';
import { useNotifications } from '@/hooks/useNotifications';

const hook = vi.mocked(useNotifications);
const row = { id: '1', type: 'NEWS', message: '실제 API 알림', isRead: false, createdAt: '2026-08-13T12:00:00' };
const defaults = () => ({
  notifications: [row], state: 'success' as const, error: null, retry: vi.fn(),
  markAllRead: vi.fn(), markingAll: false, mutationError: null, markRead: vi.fn(),
  readingId: null, notice: null, unreadCount: 23, page: 0, totalPages: 2, last: false, goToPage: vi.fn(),
});

describe('NotificationsPage', () => {
  beforeEach(() => vi.clearAllMocks());

  it('renders API notifications without fabricated users, avatars, or links', () => {
    hook.mockReturnValue(defaults());
    render(<NotificationsPage />);
    expect(screen.getByText('실제 API 알림')).toBeInTheDocument();
    expect(screen.queryByText('ARMY_Forever')).not.toBeInTheDocument();
    expect(screen.queryByRole('img')).not.toBeInTheDocument();
  });

  it('changes the API unread filter and marks all read', () => {
    const markAllRead = vi.fn();
    hook.mockReturnValue({ ...defaults(), markAllRead });
    render(<NotificationsPage />);
    fireEvent.click(screen.getByRole('button', { name: '읽지 않음' }));
    expect(hook).toHaveBeenLastCalledWith(true, 'user-a', 0);
    fireEvent.click(screen.getByRole('button', { name: '모두 읽음 처리' }));
    expect(markAllRead).toHaveBeenCalled();
  });

  it('displays account-wide unread metadata and page controls', () => {
    const value = defaults();
    hook.mockReturnValue(value);
    render(<NotificationsPage />);
    expect(screen.getByText('읽지 않은 알림 23개')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '이전' })).toBeDisabled();
    fireEvent.click(screen.getByRole('button', { name: '다음' }));
    expect(value.goToPage).toHaveBeenCalledWith(1);
  });

  it('marks the individual notification before navigating using fresh response data', async () => {
    const fresh = { ...row, isRead: true, targetType: 'POST', targetId: 'target-id', targetAvailable: true };
    const markRead = vi.fn().mockResolvedValue(fresh);
    hook.mockReturnValue({ ...defaults(), markRead });
    render(<NotificationsPage />);
    fireEvent.click(screen.getByRole('button', { name: /실제 API 알림/ }));
    expect(markRead).toHaveBeenCalledWith(row.id);
    await waitFor(() => expect(push).toHaveBeenCalledWith('/post-detail?id=target-id'));
  });

  it.each([null, { ...row, isRead: true }, { ...row, isRead: true, targetType: 'POST', targetId: 'id', targetAvailable: false }])(
    'never invents navigation after failed, legacy, or deleted read responses', async (response) => {
      hook.mockReturnValue({ ...defaults(), markRead: vi.fn().mockResolvedValue(response) });
      render(<NotificationsPage />);
      fireEvent.click(screen.getByRole('button', { name: /실제 API 알림/ }));
      await waitFor(() => expect(hook).toHaveBeenCalled());
      expect(push).not.toHaveBeenCalled();
    },
  );
});
