import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import PostCreatePage from './page';
import { fetchActiveArtists } from '@/lib/api/artist';
import { createCommunityPost } from '@/lib/api/community';

const push = vi.fn();
vi.mock('next/navigation', () => ({ useRouter: () => ({ push }) }));
vi.mock('@/components/auth/ProtectedRoute', () => ({
  default: ({ children }: { children: React.ReactNode }) => <>{children}</>,
}));
vi.mock('@/lib/api/artist', () => ({ fetchActiveArtists: vi.fn() }));
vi.mock('@/lib/api/community', () => ({ createCommunityPost: vi.fn() }));

const artist = {
  id: '33333333-3333-3333-3333-333333333333',
  name: 'Real Artist',
  englishName: null,
  agency: null,
  profileImageUrl: null,
  isGroup: true,
};

const created = {
  id: '11111111-1111-1111-1111-111111111111',
  author: { id: '22222222-2222-2222-2222-222222222222', name: 'real-author' },
  artist: { id: artist.id, name: artist.name, profileImageUrl: null },
  content: '실제 작성 내용',
  imageUrl: null,
  likeCount: 0,
  commentCount: 0,
  createdAt: '2026-08-14T08:00:00Z',
};

describe('PostCreatePage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(fetchActiveArtists).mockResolvedValue([artist]);
    vi.mocked(createCommunityPost).mockResolvedValue(created);
  });

  it('loads real active artists and creates a persisted post with the artist UUID', async () => {
    render(<PostCreatePage />);

    const artistButton = await screen.findByRole('button', { name: 'Real Artist' });
    expect(screen.queryByRole('button', { name: 'BTS' })).not.toBeInTheDocument();
    fireEvent.click(artistButton);
    fireEvent.change(screen.getByLabelText('내용 *'), { target: { value: '실제 작성 내용' } });
    fireEvent.click(screen.getByRole('button', { name: '게시' }));

    await waitFor(() => {
      expect(createCommunityPost).toHaveBeenCalledWith({
        artistId: artist.id,
        content: '실제 작성 내용',
      });
    });
    expect(push).toHaveBeenCalledWith(`/post-detail?id=${created.id}`);
  });

  it('shows an honest artist loading error without static choices', async () => {
    vi.mocked(fetchActiveArtists).mockRejectedValue(new Error('network'));
    render(<PostCreatePage />);

    expect(await screen.findByText('아티스트를 불러오지 못했습니다.')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'BTS' })).not.toBeInTheDocument();
  });

  it('keeps the form and shows the API error when creation fails', async () => {
    vi.mocked(createCommunityPost).mockRejectedValue(new Error('blocked'));
    render(<PostCreatePage />);

    fireEvent.click(await screen.findByRole('button', { name: 'Real Artist' }));
    fireEvent.change(screen.getByLabelText('내용 *'), { target: { value: '차단될 내용' } });
    fireEvent.click(screen.getByRole('button', { name: '게시' }));

    expect(await screen.findByText('게시글을 등록하지 못했습니다. 내용을 확인해 주세요.')).toBeInTheDocument();
    expect(push).not.toHaveBeenCalled();
    expect(screen.getByDisplayValue('차단될 내용')).toBeInTheDocument();
  });
});
