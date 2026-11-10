import { act, fireEvent, render, screen } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('next/navigation', () => ({ useRouter: () => ({ back: vi.fn(), push: vi.fn() }) }));
vi.mock('@/lib/api-client', () => ({ apiClient: { get: vi.fn() } }));
vi.mock('@/contexts/AuthContext', () => ({
  useAuth: () => ({
    isAuthenticated: true,
    isLoading: false,
    user: { id: '11111111-1111-1111-1111-111111111111', username: 'Cached Name', email: 'cached@example.com' },
    logout: vi.fn(),
  }),
}));

import { apiClient } from '@/lib/api-client';
import MyPage from './page';

const profile = {
  id: '11111111-1111-1111-1111-111111111111',
  username: 'Live API Name',
  email: 'live@example.com',
  hasPassword: false,
  createdAt: '2026-01-02T03:04:05Z',
};

describe('MyPage profile retrieval', () => {
  beforeEach(() => vi.resetAllMocks());

  it.each(['network', 'malformed'] as const)('recovers from a %s failure only after explicit retry', async (failure) => {
    const get = vi.mocked(apiClient.get);
    if (failure === 'network') {
      get.mockRejectedValueOnce(new Error('network failure'));
    } else {
      get.mockResolvedValueOnce({ data: { ...profile, username: null } });
    }
    let resolveRetry!: (value: { data: typeof profile }) => void;
    get.mockReturnValueOnce(new Promise((resolve) => { resolveRetry = resolve; }));

    render(<MyPage />);

    expect(await screen.findByRole('alert')).toHaveTextContent('프로필을 불러올 수 없습니다');
    expect(screen.queryByText('Cached Name')).not.toBeInTheDocument();
    expect(screen.queryByText('cached@example.com')).not.toBeInTheDocument();
    expect(screen.queryByText(profile.username)).not.toBeInTheDocument();
    expect(get).toHaveBeenCalledTimes(1);

    fireEvent.click(screen.getByRole('button', { name: '다시 시도' }));
    expect(screen.getByRole('status', { name: '프로필을 불러오는 중입니다' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '다시 시도' })).not.toBeInTheDocument();
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();

    await act(async () => { resolveRetry({ data: profile }); });

    expect(await screen.findByRole('heading', { name: profile.username })).toBeInTheDocument();
    expect(screen.getByText(profile.email)).toBeInTheDocument();
    expect(screen.queryByText('Cached Name')).not.toBeInTheDocument();
    expect(screen.queryByText('cached@example.com')).not.toBeInTheDocument();
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
    expect(screen.queryByRole('status')).not.toBeInTheDocument();
    expect(get).toHaveBeenCalledTimes(2);
  });

  it('shows loading without treating the auth snapshot as a retrieved profile', async () => {
    let resolveRequest!: (value: { data: typeof profile }) => void;
    vi.mocked(apiClient.get).mockReturnValue(new Promise((resolve) => { resolveRequest = resolve; }));
    render(<MyPage />);

    expect(screen.getByRole('status', { name: '프로필을 불러오는 중입니다' })).toBeInTheDocument();
    expect(screen.queryByText('Cached Name')).not.toBeInTheDocument();
    expect(screen.queryByText('cached@example.com')).not.toBeInTheDocument();

    await act(async () => { resolveRequest({ data: profile }); });
    expect(await screen.findByRole('heading', { name: profile.username })).toBeInTheDocument();
    expect(screen.getByText(profile.email)).toBeInTheDocument();
  });
});
