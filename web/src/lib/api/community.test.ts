import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@/lib/api-client', () => ({
  apiClient: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));

import { apiClient } from '@/lib/api-client';
import {
  createCommunityComment,
  createCommunityPost,
  deleteCommunityComment,
  deleteCommunityPost,
  fetchCommunityComments,
  fetchCommunityPost,
  fetchCommunityPosts,
  likeCommunityPost,
  saveCommunityPost,
  unlikeCommunityPost,
  unsaveCommunityPost,
  updateCommunityComment,
  updateCommunityPost,
} from './community';

const dto = {
  id: '11111111-1111-1111-1111-111111111111',
  authorId: '22222222-2222-2222-2222-222222222222',
  authorName: 'real-author',
  artistId: '33333333-3333-3333-3333-333333333333',
  artistName: 'Real Artist',
  artistProfileImageUrl: null,
  content: '실제 PostgreSQL 게시글',
  imageUrl: null,
  likeCount: 2,
  commentCount: 1,
  createdAt: '2026-08-14T08:00:00Z',
};

const mapped = {
  id: dto.id,
  author: { id: dto.authorId, name: dto.authorName },
  artist: { id: dto.artistId, name: dto.artistName, profileImageUrl: null },
  content: dto.content,
  imageUrl: null,
  likeCount: 2,
  commentCount: 1,
  createdAt: dto.createdAt,
};

const pageDto = {
  content: [dto],
  page: 0,
  size: 20,
  totalElements: 1,
  totalPages: 1,
  last: true,
};

describe('community API', () => {
  beforeEach(() => vi.clearAllMocks());

  it('validates and maps the persisted community page', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: pageDto } });

    await expect(fetchCommunityPosts('LATEST', 0, 20)).resolves.toEqual({
      items: [mapped],
      page: 0,
      size: 20,
      totalElements: 1,
      totalPages: 1,
      last: true,
    });
    expect(apiClient.get).toHaveBeenCalledWith('/community/posts', {
      params: { sort: 'LATEST', page: 0, size: 20 },
      signal: undefined,
    });
  });

  it.each([
    { ...pageDto, content: [{ ...dto, id: 1 }] },
    { ...pageDto, content: [{ ...dto, createdAt: '2026-08-14T08:00:00' }] },
    { ...pageDto, content: [{ ...dto, likeCount: -1 }] },
    { ...pageDto, content: [{ ...dto, artistId: null, artistName: 'orphan' }] },
    { ...pageDto, size: 0 },
    { ...pageDto, totalPages: 2 },
  ])('rejects malformed successful pages: %o', async (data) => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data } });
    await expect(fetchCommunityPosts()).rejects.toThrow('커뮤니티 API 응답이 올바르지 않습니다.');
  });

  it('fetches a validated detail without UUID number coercion', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: dto } });
    await expect(fetchCommunityPost(dto.id)).resolves.toEqual(mapped);
    expect(apiClient.get).toHaveBeenCalledWith(`/community/posts/${dto.id}`, { signal: undefined });
  });

  it('rejects a detail whose id differs from the requested post id', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({
      data: { success: true, data: { ...dto, id: '99999999-9999-9999-9999-999999999999' } },
    });
    await expect(fetchCommunityPost(dto.id)).rejects.toThrow(
      '커뮤니티 상세 API 응답이 올바르지 않습니다.',
    );
  });

  it('creates a post with the selected real artist UUID', async () => {
    vi.mocked(apiClient.post).mockResolvedValue({ data: { success: true, data: dto } });
    const request = { artistId: dto.artistId, content: dto.content };

    await expect(createCommunityPost(request)).resolves.toEqual(mapped);
    expect(apiClient.post).toHaveBeenCalledWith('/community/posts', request);
  });

  it('validates like and save mutation states', async () => {
    vi.mocked(apiClient.post)
      .mockResolvedValueOnce({ data: { success: true, data: { liked: true, saved: false } } })
      .mockResolvedValueOnce({ data: { success: true, data: { liked: true, saved: true } } });
    vi.mocked(apiClient.delete)
      .mockResolvedValueOnce({ data: { success: true, data: { liked: false, saved: true } } })
      .mockResolvedValueOnce({ data: { success: true, data: { liked: false, saved: false } } });

    await expect(likeCommunityPost(dto.id)).resolves.toEqual({ liked: true, saved: false });
    await expect(saveCommunityPost(dto.id)).resolves.toEqual({ liked: true, saved: true });
    await expect(unlikeCommunityPost(dto.id)).resolves.toEqual({ liked: false, saved: true });
    await expect(unsaveCommunityPost(dto.id)).resolves.toEqual({ liked: false, saved: false });
  });
});

const commentDto = {
  id: '44444444-4444-4444-4444-444444444444',
  postId: dto.id,
  userId: dto.authorId,
  content: '실제 댓글',
  status: 'APPROVED',
  parentCommentId: null,
  createdAt: '2026-08-14T09:00:00Z',
  authorName: 'comment-author',
};

const commentPageDto = {
  content: [commentDto],
  totalElements: 1,
  page: 0,
  size: 20,
  totalPages: 1,
};

describe('community owner mutations', () => {
  beforeEach(() => vi.resetAllMocks());

  it('PUTs only post content and maps the validated post', async () => {
    vi.mocked(apiClient.put).mockResolvedValue({ status: 200, data: { success: true, data: dto } });
    await expect(updateCommunityPost(dto.id, dto.content)).resolves.toEqual(mapped);
    expect(apiClient.put).toHaveBeenCalledExactlyOnceWith(`/community/posts/${dto.id}`, { content: dto.content });
  });

  it.each([
    { status: 202, data: { success: true, data: dto } },
    { status: 200, data: { success: false, data: dto } },
    { status: 200, data: { success: true, data: { ...dto, id: commentDto.id } } },
    { status: 200, data: { success: true, data: { ...dto, content: '' } } },
    { status: 200, data: { success: true, data: { ...dto, authorId: null } } },
  ])('rejects invalid post edit responses: %o', async (response) => {
    vi.mocked(apiClient.put).mockResolvedValue(response);
    await expect(updateCommunityPost(dto.id, 'edited')).rejects.toThrow('게시글 수정 API 응답');
  });

  it.each([
    ['APPROVED', 200], ['BLOCKED', 200], ['PENDING', 202],
  ] as const)('preserves %s edit status and existing comment fields', async (status, httpStatus) => {
    const data = { ...commentDto, status, parentCommentId: dto.id };
    vi.mocked(apiClient.put).mockResolvedValue({ status: httpStatus, data: { success: true, data } });
    await expect(updateCommunityComment(dto.id, commentDto.id, data.content)).resolves.toEqual(data);
    expect(apiClient.put).toHaveBeenCalledExactlyOnceWith(
      `/community/posts/${dto.id}/comments/${commentDto.id}`, { content: data.content },
    );
  });

  it.each([
    { ...commentDto, id: dto.id },
    { ...commentDto, postId: commentDto.id },
    { ...commentDto, status: 'DELETED' },
    { ...commentDto, status: 'PENDING' }, // PENDING requires HTTP 202.
    { ...commentDto, parentCommentId: 'invalid' },
    { ...commentDto, createdAt: 'invalid' },
  ])('rejects invalid comment edit responses: %o', async (data) => {
    vi.mocked(apiClient.put).mockResolvedValue({ status: 200, data: { success: true, data } });
    await expect(updateCommunityComment(dto.id, commentDto.id, 'edited')).rejects.toThrow('댓글 수정 API 응답');
  });

  it('rejects APPROVED returned with HTTP 202', async () => {
    vi.mocked(apiClient.put).mockResolvedValue({ status: 202, data: { success: true, data: commentDto } });
    await expect(updateCommunityComment(dto.id, commentDto.id, 'edited')).rejects.toThrow();
  });

  it('accepts approved replies without the deleted parent in the public list', async () => {
    const reply = { ...commentDto, parentCommentId: dto.id };
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: { ...commentPageDto, content: [reply] } } });
    await expect(fetchCommunityComments(dto.id)).resolves.toMatchObject({ items: [reply] });
  });

  it('rejects deleted comments in the public list', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: {
      ...commentPageDto, content: [{ ...commentDto, status: 'DELETED' }],
    } } });
    await expect(fetchCommunityComments(dto.id)).rejects.toThrow();
  });

  it('deletes the exact post and comment with HTTP 204 and no envelope', async () => {
    vi.mocked(apiClient.delete).mockResolvedValue({ status: 204, data: '' });
    await expect(deleteCommunityPost(dto.id)).resolves.toBeUndefined();
    await expect(deleteCommunityComment(dto.id, commentDto.id)).resolves.toBeUndefined();
    expect(apiClient.delete).toHaveBeenNthCalledWith(1, `/community/posts/${dto.id}`);
    expect(apiClient.delete).toHaveBeenNthCalledWith(2, `/community/posts/${dto.id}/comments/${commentDto.id}`);
  });

  it('does not accept unexpected delete success statuses', async () => {
    vi.mocked(apiClient.delete).mockResolvedValue({ status: 200 });
    await expect(deleteCommunityPost(dto.id)).rejects.toThrow();
    await expect(deleteCommunityComment(dto.id, commentDto.id)).rejects.toThrow();
  });

  it('validates all owner mutation IDs before making requests', async () => {
    await expect(updateCommunityPost('bad', 'edited')).rejects.toThrow();
    await expect(deleteCommunityPost('bad')).rejects.toThrow();
    await expect(updateCommunityComment('bad', commentDto.id, 'edited')).rejects.toThrow();
    await expect(updateCommunityComment(dto.id, 'bad', 'edited')).rejects.toThrow();
    await expect(deleteCommunityComment('bad', commentDto.id)).rejects.toThrow();
    await expect(deleteCommunityComment(dto.id, 'bad')).rejects.toThrow();
    expect(apiClient.put).not.toHaveBeenCalled();
    expect(apiClient.delete).not.toHaveBeenCalled();
  });

  it.each(['', '   ', 'x'.repeat(5001)])('rejects invalid post content before sending', async (content) => {
    await expect(updateCommunityPost(dto.id, content)).rejects.toThrow();
    expect(apiClient.put).not.toHaveBeenCalled();
  });

  it.each(['', '   ', 'x'.repeat(1001)])('rejects invalid comment content before sending', async (content) => {
    await expect(updateCommunityComment(dto.id, commentDto.id, content)).rejects.toThrow();
    expect(apiClient.put).not.toHaveBeenCalled();
  });

  it('propagates transport and authorization failures instead of fabricating a result', async () => {
    vi.mocked(apiClient.put).mockRejectedValue(new Error('403'));
    vi.mocked(apiClient.delete).mockRejectedValue(new Error('404'));
    await expect(updateCommunityPost(dto.id, 'edited')).rejects.toThrow('403');
    await expect(updateCommunityComment(dto.id, commentDto.id, 'edited')).rejects.toThrow('403');
    await expect(deleteCommunityPost(dto.id)).rejects.toThrow('404');
    await expect(deleteCommunityComment(dto.id, commentDto.id)).rejects.toThrow('404');
  });
});

describe('community comment API', () => {
  beforeEach(() => vi.clearAllMocks());

  it('validates persisted comments and preserves the real author name', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: commentPageDto } });

    await expect(fetchCommunityComments(dto.id)).resolves.toEqual({
      items: [commentDto],
      totalElements: 1,
      page: 0,
      size: 20,
      totalPages: 1,
    });
    expect(apiClient.get).toHaveBeenCalledWith(`/community/posts/${dto.id}/comments`, {
      params: { page: 0, size: 20 },
      signal: undefined,
    });
  });

  it.each([
    { ...commentPageDto, content: [{ ...commentDto, postId: 'not-a-uuid' }] },
    { ...commentPageDto, content: [{ ...commentDto, createdAt: '2026-08-14T09:00:00' }] },
    { ...commentPageDto, content: [{ ...commentDto, authorName: 7 }] },
    { ...commentPageDto, content: [{ ...commentDto, status: 'PENDING' }] },
    { ...commentPageDto, content: [{ ...commentDto, status: 'BLOCKED' }] },
    { ...commentPageDto, totalPages: 2 },
  ])('rejects malformed comment pages: %o', async (data) => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data } });
    await expect(fetchCommunityComments(dto.id)).rejects.toThrow(
      '커뮤니티 댓글 API 응답이 올바르지 않습니다.',
    );
  });

  it('creates a comment without sending a user id', async () => {
    vi.mocked(apiClient.post).mockResolvedValue({ data: { success: true, data: commentDto } });

    await expect(createCommunityComment(dto.id, '실제 댓글')).resolves.toEqual(commentDto);
    expect(apiClient.post).toHaveBeenCalledWith(`/community/posts/${dto.id}/comments`, {
      content: '실제 댓글',
      parentCommentId: null,
    });
  });

  it('preserves a PENDING status returned by the authenticated create endpoint', async () => {
    const pending = { ...commentDto, status: 'PENDING' };
    vi.mocked(apiClient.post).mockResolvedValue({ data: { success: true, data: pending } });
    await expect(createCommunityComment(dto.id, pending.content)).resolves.toEqual(pending);
  });
});
