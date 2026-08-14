import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@/lib/api-client', () => ({
  apiClient: {
    get: vi.fn(),
    post: vi.fn(),
    delete: vi.fn(),
  },
}));

import { apiClient } from '@/lib/api-client';
import {
  createCommunityComment,
  createCommunityPost,
  fetchCommunityComments,
  fetchCommunityPost,
  fetchCommunityPosts,
  likeCommunityPost,
  saveCommunityPost,
  unlikeCommunityPost,
  unsaveCommunityPost,
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
