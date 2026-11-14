import { fireEvent, render, screen } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import MainLayout from './MainLayout';
import PageHeader from './PageHeader';
import PageWrapper from './PageWrapper';

const navigation = vi.hoisted(() => ({ pathname: '/settings', back: vi.fn() }));
vi.mock('next/navigation', () => ({
  usePathname: () => navigation.pathname,
  useRouter: () => ({ back: navigation.back }),
}));
vi.mock('@/contexts/AuthContext', () => ({ useAuth: () => ({ user: null }) }));
vi.mock('@/hooks/useNotificationUnreadCount', () => ({
  useNotificationUnreadCount: () => ({ count: null, error: null }),
}));

describe('MainLayout mobile header ownership', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    navigation.pathname = '/settings';
  });

  it.each(['/settings', '/notifications'])('keeps global and page controls in separate rows on %s', (pathname) => {
    navigation.pathname = pathname;
    const action = vi.fn();
    render(
      <MainLayout>
        <PageHeader title="Page title" rightAction={<button onClick={action}>Page action</button>} />
        <PageWrapper><p>Page content</p></PageWrapper>
      </MainLayout>,
    );

    const menu = screen.getByRole('button', { name: '메뉴 열기' });
    const back = screen.getByRole('button', { name: '뒤로 가기' });
    expect(menu.closest('header')).toHaveClass('fixed', 'top-0');
    expect(back.closest('header')).toHaveClass('fixed', 'top-16', 'lg:static', 'lg:z-auto');
    expect(back.closest('header')).not.toHaveClass('top-0');
    expect(screen.getByRole('main')).toHaveClass('pt-16');
    expect(screen.getByText('Page content').parentElement).toHaveClass('pt-16');
    expect(screen.getByRole('link', { name: '검색' })).toHaveAttribute('href', '/search');
    expect(screen.getByRole('link', { name: '알림' })).toHaveAttribute('href', '/notifications');

    fireEvent.click(back);
    expect(navigation.back).toHaveBeenCalledOnce();
    fireEvent.click(screen.getByRole('button', { name: 'Page action' }));
    expect(action).toHaveBeenCalledOnce();

    fireEvent.click(menu);
    expect(screen.getByRole('heading', { name: '메뉴' })).toBeInTheDocument();
    // The drawer close control is the only icon-only button without a label.
    fireEvent.click(screen.getByRole('button', { name: '' }));
    expect(screen.queryByRole('heading', { name: '메뉴' })).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: '뒤로 가기' })).toBeInTheDocument();
  });

  it('retains home navigation without introducing a page header', () => {
    navigation.pathname = '/';
    render(<MainLayout><h2>Home content</h2></MainLayout>);

    expect(screen.getByRole('button', { name: '메뉴 열기' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '뒤로 가기' })).not.toBeInTheDocument();
    expect(screen.getAllByRole('banner')).toHaveLength(1);
    fireEvent.click(screen.getByRole('button', { name: '메뉴 열기' }));
    expect(screen.getByRole('heading', { name: '메뉴' })).toBeInTheDocument();
  });

  it.each(['/login', '/error'])('leaves full-screen %s free of global navigation', (pathname) => {
    navigation.pathname = pathname;
    render(<MainLayout><p>Full-screen content</p></MainLayout>);

    expect(screen.getByText('Full-screen content')).toBeInTheDocument();
    expect(screen.queryByRole('banner')).not.toBeInTheDocument();
    expect(screen.queryByRole('navigation')).not.toBeInTheDocument();
  });
});
