import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

const { push, auth } = vi.hoisted(() => ({
  push: vi.fn(),
  auth: { isAuthenticated: true, isLoading: false, user: { id: 'user-a' } as { id: string } | null },
}));
vi.mock('next/navigation', () => ({
  useParams: () => ({ id: '11111111-1111-1111-1111-111111111111' }),
  useRouter: () => ({ back: vi.fn(), push }),
}));
vi.mock('@/hooks/useArtistDetail', () => ({ useArtistDetail: vi.fn() }));
vi.mock('@/contexts/AuthContext', () => ({ useAuth: () => auth }));
vi.mock('@/lib/api/social', () => ({ fetchFavorites: vi.fn(), addFavorite: vi.fn(), removeFavorite: vi.fn() }));

import ArtistPage from './page';
import { useArtistDetail } from '@/hooks/useArtistDetail';
import { fetchFavorites, addFavorite, removeFavorite } from '@/lib/api/social';

const mockedHook = vi.mocked(useArtistDetail);
const artist = {
  id: '11111111-1111-1111-1111-111111111111',
  name: 'aespa',
  englishName: 'aespa',
  agency: 'SM Entertainment',
  description: '실제 API 소개',
  profileImageUrl: 'https://example.com/aespa.jpg',
  isGroup: true,
  members: ['Karina', 'Winter'],
  active: true,
  debutDate: '2020-11-17',
  createdAt: '2026-01-01T00:00:00Z',
};

describe('ArtistPage', () => {
  beforeEach(() => {
    vi.resetAllMocks();
    auth.isAuthenticated = true;
    auth.user = { id: 'user-a' };
    vi.mocked(fetchFavorites).mockResolvedValue([]);
    mockedHook.mockReturnValue({ artist, state: 'success', error: null, retry: vi.fn() });
  });

  it('renders only the API artist fields', async () => {
    mockedHook.mockReturnValue({ artist, state: 'success', error: null, retry: vi.fn() });
    await act(async () => { render(<ArtistPage />); });

    expect(screen.getByRole('heading', { name: 'aespa' })).toBeInTheDocument();
    expect(screen.getByText('SM Entertainment')).toBeInTheDocument();
    expect(screen.getByText('실제 API 소개')).toBeInTheDocument();
    expect(screen.getByText('Karina')).toBeInTheDocument();
    expect(screen.queryByText('2.5M')).not.toBeInTheDocument();
    expect(screen.queryByText('Billboard Hot 100 1위')).not.toBeInTheDocument();
  });

  it('shows a loading state', async () => {
    mockedHook.mockReturnValue({ artist: null, state: 'loading', error: null, retry: vi.fn() });
    await act(async () => { render(<ArtistPage />); });
    expect(screen.getByText('아티스트 정보를 불러오는 중입니다')).toBeInTheDocument();
  });

  it('shows an error and retries', async () => {
    const retry = vi.fn();
    mockedHook.mockReturnValue({ artist: null, state: 'error', error: '아티스트 정보를 불러올 수 없습니다', retry });
    await act(async () => { render(<ArtistPage />); });

    expect(screen.getByText('아티스트 정보를 불러올 수 없습니다')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '다시 시도' }));
    expect(retry).toHaveBeenCalledTimes(1);
  });

  it('loads initial follow state and only changes it after successful mutations', async () => {
    const favorite = { ...artist, followedAt: '2026-08-13T12:00:00' };
    vi.mocked(fetchFavorites).mockResolvedValue([favorite]);
    vi.mocked(removeFavorite).mockRejectedValueOnce(new Error('offline')).mockResolvedValueOnce();
    vi.mocked(addFavorite).mockResolvedValue(favorite);
    render(<ArtistPage />);

    await waitFor(() => expect(screen.getByRole('button', { name: '팔로우 해제' })).toHaveAttribute('aria-pressed', 'true'));
    fireEvent.click(screen.getByRole('button', { name: '팔로우 해제' }));
    expect(await screen.findByRole('alert')).toHaveTextContent('좋아요 취소에 실패했습니다');
    expect(screen.getByRole('button', { name: '팔로우 해제' })).toHaveAttribute('aria-pressed', 'true');
    fireEvent.click(screen.getByRole('button', { name: '팔로우 해제' }));
    await waitFor(() => expect(screen.getByRole('button', { name: '팔로우' })).toBeEnabled());
    fireEvent.click(screen.getByRole('button', { name: '팔로우' }));
    await waitFor(() => expect(screen.getByRole('button', { name: '팔로우 해제' })).toHaveAttribute('aria-pressed', 'true'));
    expect(screen.getByRole('link', { name: '좋아요한 아티스트' })).toHaveAttribute('href', '/favorites');
  });

  it('requires a known server state and allows retry after initial load failure', async () => {
    vi.mocked(fetchFavorites).mockRejectedValueOnce(new Error('offline')).mockResolvedValueOnce([]);
    render(<ArtistPage />);
    expect(screen.getByRole('button', { name: '팔로우 상태 확인 중' })).toBeDisabled();
    expect(await screen.findByRole('alert')).toHaveTextContent('즐겨찾기를 불러올 수 없습니다');
    expect(screen.getByRole('button', { name: '팔로우' })).toBeDisabled();
    fireEvent.click(screen.getByRole('button', { name: '팔로우 상태 다시 확인' }));
    await waitFor(() => expect(screen.getByRole('button', { name: '팔로우' })).toBeEnabled());
    vi.mocked(addFavorite).mockRejectedValue(new Error('not found'));
    fireEvent.click(screen.getByRole('button', { name: '팔로우' }));
    expect(await screen.findByRole('alert')).toHaveTextContent('팔로우에 실패했습니다');
    expect(screen.getByRole('button', { name: '팔로우' })).toHaveAttribute('aria-pressed', 'false');
  });

  it('sends an unauthenticated visitor to login with the original artist path', () => {
    auth.isAuthenticated = false;
    auth.user = null;
    render(<ArtistPage />);
    fireEvent.click(screen.getByRole('button', { name: '팔로우' }));
    expect(push).toHaveBeenCalledWith('/login?redirect=%2Fartists%2F11111111-1111-1111-1111-111111111111');
    expect(fetchFavorites).not.toHaveBeenCalled();
    expect(addFavorite).not.toHaveBeenCalled();
  });
});
