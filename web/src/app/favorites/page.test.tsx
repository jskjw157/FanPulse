import { fireEvent, render, screen } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@/components/auth/ProtectedRoute', () => ({ default: ({ children }: { children: React.ReactNode }) => children }));
vi.mock('next/navigation', () => ({ useRouter: () => ({ back: vi.fn() }) }));
vi.mock('@/hooks/useFavorites', () => ({ useFavorites: vi.fn() }));
vi.mock('@/contexts/AuthContext', () => ({
  useAuth: () => ({ user: { id: 'user-a', email: 'a@example.com' } }),
}));

import FavoritesPage from './page';
import { useFavorites } from '@/hooks/useFavorites';

const hook = vi.mocked(useFavorites);
const favorite = {
  id: '11111111-1111-1111-1111-111111111111', name: 'API Artist', englishName: null,
  agency: 'Real Agency', profileImageUrl: null, isGroup: true, followedAt: '2026-08-13T12:00:00',
};

describe('FavoritesPage', () => {
  beforeEach(() => vi.clearAllMocks());

  it('renders API favorites without fabricated metrics', () => {
    hook.mockReturnValue({ favorites: [favorite], state: 'success', error: null, retry: vi.fn(), unfollow: vi.fn(), mutatingId: null, mutationError: null });
    render(<FavoritesPage />);
    expect(screen.getByText('API Artist')).toBeInTheDocument();
    expect(screen.getByText('Real Agency')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'API Artist' })).toHaveAttribute('href', `/artists/${favorite.id}`);
    expect(screen.queryByText(/팔로워/)).not.toBeInTheDocument();
    expect(screen.queryByText('새 앨범 발매 예정')).not.toBeInTheDocument();
  });

  it('shows loading, error, and empty states explicitly', () => {
    hook.mockReturnValue({ favorites: [], state: 'loading', error: null, retry: vi.fn(), unfollow: vi.fn(), mutatingId: null, mutationError: null });
    const { rerender } = render(<FavoritesPage />);
    expect(screen.getByText('즐겨찾기를 불러오는 중입니다')).toBeInTheDocument();

    const retry = vi.fn();
    hook.mockReturnValue({ favorites: [], state: 'error', error: '즐겨찾기를 불러올 수 없습니다', retry, unfollow: vi.fn(), mutatingId: null, mutationError: null });
    rerender(<FavoritesPage />);
    fireEvent.click(screen.getByRole('button', { name: '다시 시도' }));
    expect(retry).toHaveBeenCalled();

    hook.mockReturnValue({ favorites: [], state: 'success', error: null, retry: vi.fn(), unfollow: vi.fn(), mutatingId: null, mutationError: null });
    rerender(<FavoritesPage />);
    expect(screen.getByText('좋아요한 아티스트가 없습니다')).toBeInTheDocument();
  });
});
