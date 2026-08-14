import { fireEvent, render, screen } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('next/navigation', () => ({
  useParams: () => ({ id: '11111111-1111-1111-1111-111111111111' }),
  useRouter: () => ({ back: vi.fn() }),
}));
vi.mock('@/hooks/useArtistDetail', () => ({ useArtistDetail: vi.fn() }));

import ArtistPage from './page';
import { useArtistDetail } from '@/hooks/useArtistDetail';

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
  beforeEach(() => vi.clearAllMocks());

  it('renders only the API artist fields', () => {
    mockedHook.mockReturnValue({ artist, state: 'success', error: null, retry: vi.fn() });
    render(<ArtistPage />);

    expect(screen.getByRole('heading', { name: 'aespa' })).toBeInTheDocument();
    expect(screen.getByText('SM Entertainment')).toBeInTheDocument();
    expect(screen.getByText('실제 API 소개')).toBeInTheDocument();
    expect(screen.getByText('Karina')).toBeInTheDocument();
    expect(screen.queryByText('2.5M')).not.toBeInTheDocument();
    expect(screen.queryByText('Billboard Hot 100 1위')).not.toBeInTheDocument();
  });

  it('shows a loading state', () => {
    mockedHook.mockReturnValue({ artist: null, state: 'loading', error: null, retry: vi.fn() });
    render(<ArtistPage />);
    expect(screen.getByText('아티스트 정보를 불러오는 중입니다')).toBeInTheDocument();
  });

  it('shows an error and retries', () => {
    const retry = vi.fn();
    mockedHook.mockReturnValue({ artist: null, state: 'error', error: '아티스트 정보를 불러올 수 없습니다', retry });
    render(<ArtistPage />);

    expect(screen.getByText('아티스트 정보를 불러올 수 없습니다')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '다시 시도' }));
    expect(retry).toHaveBeenCalledTimes(1);
  });
});
