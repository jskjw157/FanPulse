import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import PostCreatePage from './page';
import { fetchActiveArtists } from '@/lib/api/artist';
import { createCommunityPost } from '@/lib/api/community';

const auth = vi.hoisted(() => ({
  isAuthenticated: true,
  isLoading: false,
  sessionVersion: 0,
  user: null as { id: string } | null,
}));
vi.mock('@/contexts/AuthContext', () => ({ useAuth: () => auth }));
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

function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason: Error) => void;
  const promise = new Promise<T>((done, fail) => { resolve = done; reject = fail; });
  return { promise, resolve, reject };
}

describe('PostCreatePage', () => {
  beforeEach(() => {
    vi.resetAllMocks();
    auth.isAuthenticated = true;
    auth.isLoading = false;
    auth.sessionVersion = 0;
    auth.user = { id: created.author.id };
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
      }, created.author.id);
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

  it.each(['revalidation', 'account', 'session-version'] as const)('clears the draft and selected artist across %s but not an ordinary rerender', async (boundary) => {
    const view = render(<PostCreatePage />);
    fireEvent.click(await screen.findByRole('button', { name: artist.name }));
    fireEvent.change(screen.getByLabelText('내용 *'), { target: { value: '이전 세션의 작성 초안' } });
    auth.user = { id: created.author.id };
    view.rerender(<PostCreatePage />);
    expect(screen.getByLabelText('내용 *')).toHaveValue('이전 세션의 작성 초안');
    expect(screen.getByRole('button', { name: artist.name })).toHaveAttribute('aria-pressed', 'true');

    if (boundary === 'revalidation') {
      auth.isLoading = true;
      view.rerender(<PostCreatePage />);
      expect(screen.queryByLabelText('내용 *')).not.toBeInTheDocument();
      expect(screen.queryByDisplayValue('이전 세션의 작성 초안')).not.toBeInTheDocument();
      auth.isLoading = false;
    } else if (boundary === 'account') {
      auth.user = { id: '66666666-6666-6666-6666-666666666666' };
    } else auth.sessionVersion += 1;
    view.rerender(<PostCreatePage />);

    expect(screen.getByLabelText('내용 *')).toHaveValue('');
    expect(await screen.findByRole('button', { name: artist.name })).toHaveAttribute('aria-pressed', 'false');
    expect(screen.getByRole('button', { name: '게시' })).toBeDisabled();
    expect(createCommunityPost).not.toHaveBeenCalled();
  });

  it.each(['success', 'failure'] as const)('ignores a late A create %s and submits only a deliberate new B draft as B', async (outcome) => {
    const oldCreate = deferred<typeof created>();
    const userB = '66666666-6666-6666-6666-666666666666';
    const createdB = { ...created, id: '77777777-7777-7777-7777-777777777777', author: { ...created.author, id: userB } };
    vi.mocked(createCommunityPost).mockImplementation((_input, expectedUserId) =>
      expectedUserId === created.author.id ? oldCreate.promise : Promise.resolve(createdB));
    const view = render(<PostCreatePage />);
    fireEvent.click(await screen.findByRole('button', { name: artist.name }));
    fireEvent.change(screen.getByLabelText('내용 *'), { target: { value: 'A의 게시 요청' } });
    fireEvent.click(screen.getByRole('button', { name: '게시' }));
    expect(createCommunityPost).toHaveBeenCalledWith({ artistId: artist.id, content: 'A의 게시 요청' }, created.author.id);
    expect(screen.getByRole('button', { name: '등록 중' })).toBeDisabled();

    auth.user = { id: userB };
    view.rerender(<PostCreatePage />);
    expect(screen.getByLabelText('내용 *')).toHaveValue('');
    fireEvent.click(await screen.findByRole('button', { name: artist.name }));
    fireEvent.change(screen.getByLabelText('내용 *'), { target: { value: 'B의 새 게시 요청' } });
    await act(async () => {
      if (outcome === 'success') oldCreate.resolve(created);
      else oldCreate.reject(new Error('late A failure'));
      await oldCreate.promise.catch(() => undefined);
    });
    expect(push).not.toHaveBeenCalled();
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
    expect(screen.getByLabelText('내용 *')).toHaveValue('B의 새 게시 요청');
    expect(screen.getByRole('button', { name: '게시' })).toBeEnabled();

    fireEvent.click(screen.getByRole('button', { name: '게시' }));
    await waitFor(() => expect(push).toHaveBeenCalledExactlyOnceWith(`/post-detail?id=${createdB.id}`));
    expect(createCommunityPost).toHaveBeenCalledWith({ artistId: artist.id, content: 'B의 새 게시 요청' }, userB);
    expect(createCommunityPost).toHaveBeenCalledTimes(2);
  });
});
