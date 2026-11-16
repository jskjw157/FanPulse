import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@/lib/api-client', async (importOriginal) => ({
  ...await importOriginal<typeof import('@/lib/api-client')>(),
  apiClient: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));

import { apiClient } from '@/lib/api-client';
import { SESSION_INVALIDATED } from '@/lib/session-sync';
import {
  createCommunityComment,
  createCommunityPost,
  deleteCommunityComment,
  deleteCommunityPost,
  fetchCommunityComments,
  fetchCommunityPost,
  fetchCommunityPosts,
  fetchCommunityPostState,
  fetchSavedCommunityPosts,
  likeCommunityPost,
  saveCommunityPost,
  unlikeCommunityPost,
  unsaveCommunityPost,
  updateCommunityComment,
  updateCommunityPost,
} from './community';

const expectedUserId = 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa';
const expectedConfig = { headers: { 'X-Expected-User-Id': expectedUserId } };

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

    await expect(createCommunityPost(request, expectedUserId)).resolves.toEqual(mapped);
    expect(apiClient.post).toHaveBeenCalledWith('/community/posts', request, expectedConfig);
  });

  it('validates like and save mutation states', async () => {
    vi.mocked(apiClient.post)
      .mockResolvedValueOnce({ data: { success: true, data: { liked: true, saved: false } } })
      .mockResolvedValueOnce({ data: { success: true, data: { liked: true, saved: true } } });
    vi.mocked(apiClient.delete)
      .mockResolvedValueOnce({ data: { success: true, data: { liked: false, saved: true } } })
      .mockResolvedValueOnce({ data: { success: true, data: { liked: false, saved: false } } });

    await expect(likeCommunityPost(dto.id, expectedUserId)).resolves.toEqual({ liked: true, saved: false });
    await expect(saveCommunityPost(dto.id, expectedUserId)).resolves.toEqual({ liked: true, saved: true });
    await expect(unlikeCommunityPost(dto.id, expectedUserId)).resolves.toEqual({ liked: false, saved: true });
    await expect(unsaveCommunityPost(dto.id, expectedUserId)).resolves.toEqual({ liked: false, saved: false });
    expect(apiClient.post).toHaveBeenNthCalledWith(1, `/community/posts/${dto.id}/likes`, undefined, expectedConfig);
    expect(apiClient.post).toHaveBeenNthCalledWith(2, `/community/posts/${dto.id}/saved`, undefined, expectedConfig);
    expect(apiClient.delete).toHaveBeenNthCalledWith(1, `/community/posts/${dto.id}/likes`, expectedConfig);
    expect(apiClient.delete).toHaveBeenNthCalledWith(2, `/community/posts/${dto.id}/saved`, expectedConfig);
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
    await expect(updateCommunityPost(dto.id, dto.content, expectedUserId)).resolves.toEqual(mapped);
    expect(apiClient.put).toHaveBeenCalledExactlyOnceWith(
      `/community/posts/${dto.id}`, { content: dto.content }, expectedConfig,
    );
  });

  it.each([
    { status: 202, data: { success: true, data: dto } },
    { status: 200, data: { success: false, data: dto } },
    { status: 200, data: { success: true, data: { ...dto, id: commentDto.id } } },
    { status: 200, data: { success: true, data: { ...dto, content: '' } } },
    { status: 200, data: { success: true, data: { ...dto, authorId: null } } },
  ])('rejects invalid post edit responses: %o', async (response) => {
    vi.mocked(apiClient.put).mockResolvedValue(response);
    await expect(updateCommunityPost(dto.id, 'edited', expectedUserId)).rejects.toThrow('게시글 수정 API 응답');
  });

  it.each([
    ['APPROVED', 200], ['BLOCKED', 200], ['PENDING', 202],
  ] as const)('preserves %s edit status and existing comment fields', async (status, httpStatus) => {
    const data = { ...commentDto, status, parentCommentId: dto.id };
    vi.mocked(apiClient.put).mockResolvedValue({ status: httpStatus, data: { success: true, data } });
    await expect(updateCommunityComment(dto.id, commentDto.id, data.content, expectedUserId)).resolves.toEqual(data);
    expect(apiClient.put).toHaveBeenCalledExactlyOnceWith(
      `/community/posts/${dto.id}/comments/${commentDto.id}`, { content: data.content }, expectedConfig,
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
    await expect(updateCommunityComment(dto.id, commentDto.id, 'edited', expectedUserId)).rejects.toThrow('댓글 수정 API 응답');
  });

  it('rejects APPROVED returned with HTTP 202', async () => {
    vi.mocked(apiClient.put).mockResolvedValue({ status: 202, data: { success: true, data: commentDto } });
    await expect(updateCommunityComment(dto.id, commentDto.id, 'edited', expectedUserId)).rejects.toThrow();
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
    await expect(deleteCommunityPost(dto.id, expectedUserId)).resolves.toBeUndefined();
    await expect(deleteCommunityComment(dto.id, commentDto.id, expectedUserId)).resolves.toBeUndefined();
    expect(apiClient.delete).toHaveBeenNthCalledWith(1, `/community/posts/${dto.id}`, expectedConfig);
    expect(apiClient.delete).toHaveBeenNthCalledWith(2, `/community/posts/${dto.id}/comments/${commentDto.id}`, expectedConfig);
  });

  it('does not accept unexpected delete success statuses', async () => {
    vi.mocked(apiClient.delete).mockResolvedValue({ status: 200 });
    await expect(deleteCommunityPost(dto.id, expectedUserId)).rejects.toThrow();
    await expect(deleteCommunityComment(dto.id, commentDto.id, expectedUserId)).rejects.toThrow();
  });

  it('validates all owner mutation IDs before making requests', async () => {
    await expect(updateCommunityPost('bad', 'edited', expectedUserId)).rejects.toThrow();
    await expect(deleteCommunityPost('bad', expectedUserId)).rejects.toThrow();
    await expect(updateCommunityComment('bad', commentDto.id, 'edited', expectedUserId)).rejects.toThrow();
    await expect(updateCommunityComment(dto.id, 'bad', 'edited', expectedUserId)).rejects.toThrow();
    await expect(deleteCommunityComment('bad', commentDto.id, expectedUserId)).rejects.toThrow();
    await expect(deleteCommunityComment(dto.id, 'bad', expectedUserId)).rejects.toThrow();
    expect(apiClient.put).not.toHaveBeenCalled();
    expect(apiClient.delete).not.toHaveBeenCalled();
  });

  it.each(['', '   ', 'x'.repeat(5001)])('rejects invalid post content before sending', async (content) => {
    await expect(updateCommunityPost(dto.id, content, expectedUserId)).rejects.toThrow();
    expect(apiClient.put).not.toHaveBeenCalled();
  });

  it.each(['', '   ', 'x'.repeat(1001)])('rejects invalid comment content before sending', async (content) => {
    await expect(updateCommunityComment(dto.id, commentDto.id, content, expectedUserId)).rejects.toThrow();
    expect(apiClient.put).not.toHaveBeenCalled();
  });

  it('propagates transport and authorization failures instead of fabricating a result', async () => {
    vi.mocked(apiClient.put).mockRejectedValue(new Error('403'));
    vi.mocked(apiClient.delete).mockRejectedValue(new Error('404'));
    await expect(updateCommunityPost(dto.id, 'edited', expectedUserId)).rejects.toThrow('403');
    await expect(updateCommunityComment(dto.id, commentDto.id, 'edited', expectedUserId)).rejects.toThrow('403');
    await expect(deleteCommunityPost(dto.id, expectedUserId)).rejects.toThrow('404');
    await expect(deleteCommunityComment(dto.id, commentDto.id, expectedUserId)).rejects.toThrow('404');
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

  it('creates a comment without sending a user id in the body', async () => {
    vi.mocked(apiClient.post).mockResolvedValue({ data: { success: true, data: commentDto } });

    await expect(createCommunityComment(dto.id, '실제 댓글', null, expectedUserId)).resolves.toEqual(commentDto);
    expect(apiClient.post).toHaveBeenCalledWith(`/community/posts/${dto.id}/comments`, {
      content: '실제 댓글',
      parentCommentId: null,
    }, expectedConfig);
  });

  it('preserves a PENDING status returned by the authenticated create endpoint', async () => {
    const pending = { ...commentDto, status: 'PENDING' };
    vi.mocked(apiClient.post).mockResolvedValue({ data: { success: true, data: pending } });
    await expect(createCommunityComment(dto.id, pending.content, null, expectedUserId)).resolves.toEqual(pending);
  });
});

const communityWrites = [
  { name: 'create post', method: 'post', request: (id: string) => createCommunityPost({ artistId: null, content: dto.content }, id) },
  { name: 'update post', method: 'put', request: (id: string) => updateCommunityPost(dto.id, dto.content, id) },
  { name: 'delete post', method: 'delete', request: (id: string) => deleteCommunityPost(dto.id, id) },
  { name: 'like post', method: 'post', request: (id: string) => likeCommunityPost(dto.id, id) },
  { name: 'unlike post', method: 'delete', request: (id: string) => unlikeCommunityPost(dto.id, id) },
  { name: 'save post', method: 'post', request: (id: string) => saveCommunityPost(dto.id, id) },
  { name: 'unsave post', method: 'delete', request: (id: string) => unsaveCommunityPost(dto.id, id) },
  { name: 'create comment', method: 'post', request: (id: string) => createCommunityComment(dto.id, commentDto.content, null, id) },
  { name: 'update comment', method: 'put', request: (id: string) => updateCommunityComment(dto.id, commentDto.id, commentDto.content, id) },
  { name: 'delete comment', method: 'delete', request: (id: string) => deleteCommunityComment(dto.id, commentDto.id, id) },
] as const;

describe.each(communityWrites)('displayed-account boundary: $name', ({ method, request }) => {
  beforeEach(() => vi.resetAllMocks());

  it.each([undefined, null, '', ' \t\n ', 42, true])('rejects invalid or missing expected identity %s before dispatch', async (id) => {
    // Exercise untyped/legacy callers as well as the required TypeScript argument.
    await expect(request(id as unknown as string)).rejects.toThrow('Expected user is required');
    expect(apiClient.get).not.toHaveBeenCalled();
    expect(apiClient.post).not.toHaveBeenCalled();
    expect(apiClient.put).not.toHaveBeenCalled();
    expect(apiClient.delete).not.toHaveBeenCalled();
  });

  it.each([
    [412, 'EXPECTED_USER_MISMATCH'],
    [428, 'EXPECTED_USER_REQUIRED'],
  ] as const)('invalidates on HTTP %s without replaying the mutation', async (status, code) => {
    const error = { isAxiosError: true, response: { status, data: {
      title: 'Expected User Precondition', status, errorCode: code,
    } } };
    vi.mocked(apiClient[method]).mockRejectedValue(error);
    const invalidated = vi.fn();
    window.addEventListener(SESSION_INVALIDATED, invalidated);
    try {
      await expect(request(expectedUserId)).rejects.toMatchObject({ status, code });
      expect(invalidated).toHaveBeenCalledTimes(1);
      expect(apiClient.get).not.toHaveBeenCalled();
      for (const verb of ['post', 'put', 'delete'] as const) {
        expect(apiClient[verb]).toHaveBeenCalledTimes(verb === method ? 1 : 0);
      }
      const [call] = vi.mocked(apiClient[method]).mock.calls;
      expect(call[call.length - 1]).toEqual(expectedConfig);
    } finally {
      window.removeEventListener(SESSION_INVALIDATED, invalidated);
    }
  });
});

describe('private community reads', () => {
  beforeEach(() => vi.resetAllMocks());

  it.each([undefined, expectedUserId])('keeps signal position and optional state assertion (%s)', async (id) => {
    const signal = new AbortController().signal;
    const state = { liked: false, saved: true };
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: state } });
    const request = id === undefined
      ? fetchCommunityPostState(dto.id, signal)
      : fetchCommunityPostState(dto.id, signal, id);
    await expect(request).resolves.toEqual(state);
    expect(apiClient.get).toHaveBeenCalledExactlyOnceWith(`/community/me/posts/${dto.id}/state`, {
      signal, ...(id === undefined ? {} : expectedConfig),
    });
  });

  it.each([undefined, expectedUserId])('keeps pagination and optional saved-page assertion (%s)', async (id) => {
    const signal = new AbortController().signal;
    const data = { ...pageDto, page: 1, size: 2, totalElements: 3, totalPages: 2 };
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data } });
    const request = id === undefined
      ? fetchSavedCommunityPosts(1, 2, signal)
      : fetchSavedCommunityPosts(1, 2, signal, id);
    await expect(request).resolves.toEqual({
      items: [mapped], page: 1, size: 2, totalElements: 3, totalPages: 2, last: true,
    });
    expect(apiClient.get).toHaveBeenCalledExactlyOnceWith('/community/me/saved', {
      params: { page: 1, size: 2 }, signal, ...(id === undefined ? {} : expectedConfig),
    });
  });

  it('retains default saved-page arguments for legacy callers', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: pageDto } });
    await expect(fetchSavedCommunityPosts()).resolves.toMatchObject({ items: [mapped], page: 0, size: 20 });
    expect(apiClient.get).toHaveBeenCalledExactlyOnceWith('/community/me/saved', {
      params: { page: 0, size: 20 }, signal: undefined,
    });
  });

  it.each([
    { name: 'state', request: () => fetchCommunityPostState(dto.id, undefined, expectedUserId) },
    { name: 'saved', request: () => fetchSavedCommunityPosts(0, 20, undefined, expectedUserId) },
  ])('invalidates a rejected private $name read without retrying or writing', async ({ request }) => {
    const invalidated = vi.fn();
    window.addEventListener(SESSION_INVALIDATED, invalidated);
    try {
      for (const [status, code] of [[412, 'EXPECTED_USER_MISMATCH'], [428, 'EXPECTED_USER_REQUIRED']] as const) {
        vi.mocked(apiClient.get).mockRejectedValue({ isAxiosError: true, response: { status, data: {
          success: false, error: { code, message: 'Expected user precondition failed' },
        } } });
        await expect(request()).rejects.toMatchObject({ status, code });
      }
      expect(apiClient.get).toHaveBeenCalledTimes(2);
      expect(invalidated).toHaveBeenCalledTimes(2);
      expect(apiClient.post).not.toHaveBeenCalled();
      expect(apiClient.put).not.toHaveBeenCalled();
      expect(apiClient.delete).not.toHaveBeenCalled();
    } finally {
      window.removeEventListener(SESSION_INVALIDATED, invalidated);
    }
  });

  it.each([
    { liked: 'false', saved: true },
    { liked: false },
    null,
  ])('still rejects malformed private state DTOs: %o', async (data) => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data } });
    await expect(fetchCommunityPostState(dto.id, undefined, expectedUserId)).rejects.toThrow('커뮤니티 상태 API 응답');
  });

  it.each([
    { ...pageDto, page: 1, totalElements: 21, totalPages: 2 },
    { ...pageDto, size: 10 },
    { ...pageDto, content: [{ ...dto, authorId: null }] },
  ])('still rejects mismatched or malformed saved pages: %o', async (data) => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data } });
    await expect(fetchSavedCommunityPosts(0, 20, undefined, expectedUserId)).rejects.toThrow('저장한 게시글 API 응답');
  });
});
