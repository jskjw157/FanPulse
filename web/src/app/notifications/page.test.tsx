import { fireEvent, render, screen } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@/components/auth/ProtectedRoute', () => ({ default: ({ children }: { children: React.ReactNode }) => children }));
vi.mock('next/navigation', () => ({ useRouter: () => ({ back: vi.fn() }) }));
vi.mock('@/hooks/useNotifications', () => ({ useNotifications: vi.fn() }));
vi.mock('@/contexts/AuthContext', () => ({
  useAuth: () => ({ user: { id: 'user-a', email: 'a@example.com' } }),
}));

import NotificationsPage from './page';
import { useNotifications } from '@/hooks/useNotifications';

const hook = vi.mocked(useNotifications);
const row = { id: '1', type: 'NEWS', message: '실제 API 알림', isRead: false, createdAt: '2026-08-13T12:00:00' };

describe('NotificationsPage', () => {
  beforeEach(() => vi.clearAllMocks());

  it('renders API notifications without fabricated users, avatars, or links', () => {
    hook.mockReturnValue({ notifications: [row], state: 'success', error: null, retry: vi.fn(), markAllRead: vi.fn(), markingAll: false, mutationError: null });
    render(<NotificationsPage />);
    expect(screen.getByText('실제 API 알림')).toBeInTheDocument();
    expect(screen.queryByText('ARMY_Forever')).not.toBeInTheDocument();
    expect(screen.queryByRole('img')).not.toBeInTheDocument();
  });

  it('changes the API unread filter and marks all read', () => {
    const markAllRead = vi.fn();
    hook.mockReturnValue({ notifications: [row], state: 'success', error: null, retry: vi.fn(), markAllRead, markingAll: false, mutationError: null });
    render(<NotificationsPage />);
    fireEvent.click(screen.getByRole('button', { name: '읽지 않음' }));
    expect(hook).toHaveBeenLastCalledWith(true, 'user-a');
    fireEvent.click(screen.getByRole('button', { name: '모두 읽음 처리' }));
    expect(markAllRead).toHaveBeenCalled();
  });
});
