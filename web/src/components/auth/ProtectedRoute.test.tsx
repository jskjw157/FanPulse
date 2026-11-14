import { cleanup, render, screen } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import ProtectedRoute from './ProtectedRoute';

const { auth, router } = vi.hoisted(() => ({
  auth: { isAuthenticated: false, isLoading: false },
  router: { push: vi.fn(), replace: vi.fn() },
}));

vi.mock('@/contexts/AuthContext', () => ({ useAuth: () => auth }));
vi.mock('next/navigation', () => ({ useRouter: () => router }));

describe('ProtectedRoute history boundary', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    auth.isAuthenticated = false;
    auth.isLoading = false;
  });

  afterEach(() => {
    cleanup();
    expect(router.push).not.toHaveBeenCalled();
  });

  it('replaces the blocked page with the existing login fallback', () => {
    render(<ProtectedRoute><p>Private content</p></ProtectedRoute>);
    expect(router.replace).toHaveBeenCalledExactlyOnceWith('/login');
    expect(screen.queryByText('Private content')).not.toBeInTheDocument();
  });

  it('preserves the loading fallback and waits before redirecting', () => {
    auth.isLoading = true;
    const view = render(
      <ProtectedRoute fallback={<p role="status">Checking session</p>}>
        <p>Private content</p>
      </ProtectedRoute>,
    );
    expect(screen.getByRole('status')).toHaveTextContent('Checking session');
    expect(router.replace).not.toHaveBeenCalled();
    auth.isLoading = false;
    view.rerender(<ProtectedRoute><p>Private content</p></ProtectedRoute>);
    expect(router.replace).toHaveBeenCalledExactlyOnceWith('/login');
    expect(screen.queryByRole('status')).not.toBeInTheDocument();
  });

  it('does not replace or hide an authenticated page', () => {
    auth.isAuthenticated = true;
    render(<ProtectedRoute><p>Private content</p></ProtectedRoute>);
    expect(screen.getByText('Private content')).toBeVisible();
    expect(router.replace).not.toHaveBeenCalled();
  });
});
