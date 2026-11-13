import { act, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import Header from './Header';
import { fetchNotificationUnreadCount } from '@/lib/api/social';
import { notifyUnreadCountChanged } from '@/hooks/useNotificationUnreadCount';

const auth = vi.hoisted(() => ({ user: { id: 'account-a' } as { id: string } | null }));
vi.mock('@/contexts/AuthContext', () => ({ useAuth: () => auth }));
vi.mock('@/lib/api/social', () => ({ fetchNotificationUnreadCount: vi.fn() }));

describe('Header notification badge', () => {
  beforeEach(() => {
    vi.resetAllMocks();
    auth.user = { id: 'account-a' };
    vi.mocked(fetchNotificationUnreadCount).mockResolvedValue(27);
  });

  it('renders the account-wide count instead of a fabricated dot', async () => {
    render(<Header onMenuClick={vi.fn()} />);
    const link = await screen.findByRole('link', { name: '알림, 읽지 않은 알림 27개' });
    expect(link).toHaveTextContent('27');
    expect(link).toHaveAttribute('href', '/notifications');
  });

  it('hides the badge when the count is zero', async () => {
    vi.mocked(fetchNotificationUnreadCount).mockResolvedValue(0);
    render(<Header onMenuClick={vi.fn()} />);
    expect((await screen.findByRole('link', { name: '알림, 읽지 않은 알림 0개' })).textContent).toBe('');
  });

  it('caps the visual count while keeping the full accessible count', async () => {
    vi.mocked(fetchNotificationUnreadCount).mockResolvedValue(125);
    render(<Header onMenuClick={vi.fn()} />);
    expect(await screen.findByRole('link', { name: '알림, 읽지 않은 알림 125개' })).toHaveTextContent('99+');
  });

  it('does not fabricate a zero when unread count loading fails and retries on focus', async () => {
    vi.mocked(fetchNotificationUnreadCount).mockRejectedValueOnce(new Error('offline'));
    render(<Header onMenuClick={vi.fn()} />);
    expect((await screen.findByRole('link', { name: '알림 (읽지 않은 알림 수 확인 불가)' })).textContent).toBe('');
    act(() => { window.dispatchEvent(new Event('focus')); });
    expect(await screen.findByRole('link', { name: '알림, 읽지 않은 알림 27개' })).toBeInTheDocument();
  });

  it('refreshes after successful read events and ignores a late previous count', async () => {
    let resolve!: (value: number) => void;
    vi.mocked(fetchNotificationUnreadCount)
      .mockReturnValueOnce(new Promise<number>((done) => { resolve = done; }))
      .mockResolvedValueOnce(0);
    render(<Header onMenuClick={vi.fn()} />);
    act(() => { notifyUnreadCountChanged('account-a'); });
    await screen.findByRole('link', { name: '알림, 읽지 않은 알림 0개' });
    await act(async () => { resolve(27); });
    expect(screen.getByRole('link', { name: '알림, 읽지 않은 알림 0개' }).textContent).toBe('');
  });

  it('clears previous account counts immediately, suppresses stale responses, and makes no guest request', async () => {
    let resolve!: (value: number) => void;
    vi.mocked(fetchNotificationUnreadCount).mockReturnValueOnce(new Promise<number>((done) => { resolve = done; }));
    const { rerender } = render(<Header onMenuClick={vi.fn()} />);
    auth.user = { id: 'account-b' };
    rerender(<Header onMenuClick={vi.fn()} />);
    expect(screen.getByRole('link', { name: '알림' }).textContent).toBe('');
    await screen.findByRole('link', { name: '알림, 읽지 않은 알림 27개' });
    await act(async () => { resolve(99); });
    expect(screen.getByRole('link', { name: '알림, 읽지 않은 알림 27개' })).toBeInTheDocument();
    auth.user = null;
    rerender(<Header onMenuClick={vi.fn()} />);
    expect(screen.getByRole('link', { name: '알림' }).textContent).toBe('');
    await waitFor(() => expect(fetchNotificationUnreadCount).toHaveBeenCalledTimes(2));
  });
});
