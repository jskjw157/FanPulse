import { apiClient } from '@/lib/api-client';
import {
  isIsoInstant,
  isNonEmptyString,
  isRecord,
  isUuid,
  unwrapApiResponse,
} from '@/lib/api-response';
import type { ApiResponse } from '@/types/api';

export type CommunitySort = 'LATEST' | 'POPULAR';

interface CommunityPostDto {
  id: string;
  authorId: string;
  authorName: string;
  artistId: string | null;
  artistName: string | null;
  artistProfileImageUrl: string | null;
  content: string;
  imageUrl: string | null;
  likeCount: number;
  commentCount: number;
  createdAt: string;
}

interface CommunityPageDto {
  content: CommunityPostDto[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface CommunityPost {
  id: string;
  author: { id: string; name: string };
  artist: { id: string; name: string; profileImageUrl: string | null } | null;
  content: string;
  imageUrl: string | null;
  likeCount: number;
  commentCount: number;
  createdAt: string;
}

export interface CommunityPage {
  items: CommunityPost[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface CommunityPostState {
  liked: boolean;
  saved: boolean;
}

export interface CreateCommunityPostInput {
  artistId: string | null;
  content: string;
}

type CommunityCommentStatus = 'APPROVED' | 'PENDING' | 'BLOCKED';

interface CommunityCommentDto {
  id: string;
  postId: string;
  userId: string;
  content: string;
  status: CommunityCommentStatus;
  parentCommentId: string | null;
  createdAt: string;
  authorName: string | null;
}

interface CommunityCommentPageDto {
  content: CommunityCommentDto[];
  totalElements: number;
  page: number;
  size: number;
  totalPages: number;
}

export interface CommunityComment {
  id: string;
  postId: string;
  userId: string;
  content: string;
  status: CommunityCommentStatus;
  parentCommentId: string | null;
  createdAt: string;
  authorName: string | null;
}

export interface CommunityCommentPage {
  items: CommunityComment[];
  totalElements: number;
  page: number;
  size: number;
  totalPages: number;
}

function isNonNegativeInteger(value: unknown): value is number {
  return Number.isInteger(value) && (value as number) >= 0;
}

function isNullableSafeHttpsUrl(value: unknown): value is string | null {
  if (value === null) return true;
  if (typeof value !== 'string') return false;
  try {
    const url = new URL(value);
    return (
      url.protocol.toLowerCase() === 'https:' &&
      url.hostname.length > 0 &&
      url.username === '' &&
      url.password === '' &&
      (url.port === '' || url.port === '443')
    );
  } catch {
    return false;
  }
}

function isCommunityPostDto(value: unknown): value is CommunityPostDto {
  if (!isRecord(value)) return false;

  const artistIsConsistent = value.artistId === null
    ? value.artistName === null && value.artistProfileImageUrl === null
    : isUuid(value.artistId) &&
      isNonEmptyString(value.artistName) &&
      isNullableSafeHttpsUrl(value.artistProfileImageUrl);

  return (
    isUuid(value.id) &&
    isUuid(value.authorId) &&
    isNonEmptyString(value.authorName) &&
    artistIsConsistent &&
    isNonEmptyString(value.content) &&
    isNullableSafeHttpsUrl(value.imageUrl) &&
    isNonNegativeInteger(value.likeCount) &&
    isNonNegativeInteger(value.commentCount) &&
    isIsoInstant(value.createdAt)
  );
}

function isCommunityPageDto(value: unknown): value is CommunityPageDto {
  if (!isRecord(value)) return false;
  if (
    !Array.isArray(value.content) ||
    !value.content.every(isCommunityPostDto) ||
    !isNonNegativeInteger(value.page) ||
    !Number.isInteger(value.size) ||
    (value.size as number) <= 0 ||
    !isNonNegativeInteger(value.totalElements) ||
    !isNonNegativeInteger(value.totalPages) ||
    typeof value.last !== 'boolean'
  ) {
    return false;
  }

  const page = value.page as number;
  const size = value.size as number;
  const totalElements = value.totalElements as number;
  const totalPages = value.totalPages as number;
  const expectedPages = totalElements === 0 ? 0 : Math.ceil(totalElements / size);

  if (totalPages !== expectedPages || value.content.length > size) return false;
  if (totalPages === 0) {
    return page === 0 && value.content.length === 0 && value.last === true;
  }
  return page < totalPages && value.last === (page === totalPages - 1);
}

function isCommunityPostState(value: unknown): value is CommunityPostState {
  return isRecord(value) && typeof value.liked === 'boolean' && typeof value.saved === 'boolean';
}

function isCommunityCommentDto(value: unknown): value is CommunityCommentDto {
  return isRecord(value) &&
    isUuid(value.id) &&
    isUuid(value.postId) &&
    isUuid(value.userId) &&
    isNonEmptyString(value.content) &&
    (value.status === 'APPROVED' || value.status === 'PENDING' || value.status === 'BLOCKED') &&
    (value.parentCommentId === null || isUuid(value.parentCommentId)) &&
    isIsoInstant(value.createdAt) &&
    (value.authorName === null || isNonEmptyString(value.authorName));
}

function isCommunityCommentPageDto(value: unknown): value is CommunityCommentPageDto {
  if (!isRecord(value)) return false;
  if (
    !Array.isArray(value.content) ||
    !value.content.every(isCommunityCommentDto) ||
    !isNonNegativeInteger(value.totalElements) ||
    !isNonNegativeInteger(value.page) ||
    !Number.isInteger(value.size) ||
    (value.size as number) <= 0 ||
    !isNonNegativeInteger(value.totalPages)
  ) return false;

  const page = value.page as number;
  const size = value.size as number;
  const totalElements = value.totalElements as number;
  const totalPages = value.totalPages as number;
  const expectedPages = totalElements === 0 ? 0 : Math.ceil(totalElements / size);
  if (totalPages !== expectedPages || value.content.length > size) return false;
  return totalPages === 0
    ? page === 0 && value.content.length === 0
    : page < totalPages;
}

function mapComment(dto: CommunityCommentDto): CommunityComment {
  return {
    id: dto.id,
    postId: dto.postId,
    userId: dto.userId,
    content: dto.content,
    status: dto.status,
    parentCommentId: dto.parentCommentId,
    createdAt: dto.createdAt,
    authorName: dto.authorName,
  };
}

function mapPost(dto: CommunityPostDto): CommunityPost {
  return {
    id: dto.id,
    author: { id: dto.authorId, name: dto.authorName },
    artist: dto.artistId === null
      ? null
      : {
          id: dto.artistId,
          name: dto.artistName as string,
          profileImageUrl: dto.artistProfileImageUrl,
        },
    content: dto.content,
    imageUrl: dto.imageUrl,
    likeCount: dto.likeCount,
    commentCount: dto.commentCount,
    createdAt: dto.createdAt,
  };
}

function mapPage(dto: CommunityPageDto): CommunityPage {
  return {
    items: dto.content.map(mapPost),
    page: dto.page,
    size: dto.size,
    totalElements: dto.totalElements,
    totalPages: dto.totalPages,
    last: dto.last,
  };
}

export async function fetchCommunityPosts(
  sort: CommunitySort = 'LATEST',
  page = 0,
  size = 20,
  signal?: AbortSignal,
): Promise<CommunityPage> {
  const response = await apiClient.get<ApiResponse<CommunityPageDto>>('/community/posts', {
    params: { sort, page, size },
    signal,
  });
  const data = unwrapApiResponse(
    response.data,
    '커뮤니티 API 응답이 올바르지 않습니다.',
    isCommunityPageDto,
  );
  if (data.page !== page || data.size !== size) {
    throw new Error('커뮤니티 API 응답이 올바르지 않습니다.');
  }
  return mapPage(data);
}

export async function fetchCommunityPost(
  postId: string,
  signal?: AbortSignal,
): Promise<CommunityPost> {
  if (!isUuid(postId)) throw new Error('게시글 ID가 올바르지 않습니다.');
  const response = await apiClient.get<ApiResponse<CommunityPostDto>>(
    `/community/posts/${postId}`,
    { signal },
  );
  return mapPost(unwrapApiResponse(
    response.data,
    '커뮤니티 상세 API 응답이 올바르지 않습니다.',
    isCommunityPostDto,
  ));
}

export async function createCommunityPost(
  input: CreateCommunityPostInput,
): Promise<CommunityPost> {
  const response = await apiClient.post<ApiResponse<CommunityPostDto>>('/community/posts', input);
  return mapPost(unwrapApiResponse(
    response.data,
    '커뮤니티 작성 API 응답이 올바르지 않습니다.',
    isCommunityPostDto,
  ));
}

async function mutateState(method: 'post' | 'delete', path: string): Promise<CommunityPostState> {
  const response = await apiClient[method]<ApiResponse<CommunityPostState>>(path);
  return unwrapApiResponse(
    response.data,
    '커뮤니티 상태 API 응답이 올바르지 않습니다.',
    isCommunityPostState,
  );
}

export const likeCommunityPost = (postId: string) =>
  mutateState('post', `/community/posts/${postId}/likes`);
export const unlikeCommunityPost = (postId: string) =>
  mutateState('delete', `/community/posts/${postId}/likes`);
export const saveCommunityPost = (postId: string) =>
  mutateState('post', `/community/posts/${postId}/saved`);
export const unsaveCommunityPost = (postId: string) =>
  mutateState('delete', `/community/posts/${postId}/saved`);

export async function fetchCommunityPostState(
  postId: string,
  signal?: AbortSignal,
): Promise<CommunityPostState> {
  const response = await apiClient.get<ApiResponse<CommunityPostState>>(
    `/community/me/posts/${postId}/state`,
    { signal },
  );
  return unwrapApiResponse(
    response.data,
    '커뮤니티 상태 API 응답이 올바르지 않습니다.',
    isCommunityPostState,
  );
}

export async function fetchSavedCommunityPosts(
  page = 0,
  size = 20,
  signal?: AbortSignal,
): Promise<CommunityPage> {
  const response = await apiClient.get<ApiResponse<CommunityPageDto>>('/community/me/saved', {
    params: { page, size },
    signal,
  });
  const data = unwrapApiResponse(
    response.data,
    '저장한 게시글 API 응답이 올바르지 않습니다.',
    isCommunityPageDto,
  );
  if (data.page !== page || data.size !== size) {
    throw new Error('저장한 게시글 API 응답이 올바르지 않습니다.');
  }
  return mapPage(data);
}

export async function fetchCommunityComments(
  postId: string,
  page = 0,
  size = 20,
  signal?: AbortSignal,
): Promise<CommunityCommentPage> {
  if (!isUuid(postId)) throw new Error('게시글 ID가 올바르지 않습니다.');
  const response = await apiClient.get<ApiResponse<CommunityCommentPageDto>>(
    `/community/posts/${postId}/comments`,
    { params: { page, size }, signal },
  );
  const data = unwrapApiResponse(
    response.data,
    '커뮤니티 댓글 API 응답이 올바르지 않습니다.',
    isCommunityCommentPageDto,
  );
  if (data.page !== page || data.size !== size || data.content.some((comment) => comment.postId !== postId)) {
    throw new Error('커뮤니티 댓글 API 응답이 올바르지 않습니다.');
  }
  return {
    items: data.content.map(mapComment),
    totalElements: data.totalElements,
    page: data.page,
    size: data.size,
    totalPages: data.totalPages,
  };
}

export async function createCommunityComment(
  postId: string,
  content: string,
  parentCommentId: string | null = null,
): Promise<CommunityComment> {
  if (!isUuid(postId) || (parentCommentId !== null && !isUuid(parentCommentId))) {
    throw new Error('댓글 요청 ID가 올바르지 않습니다.');
  }
  const response = await apiClient.post<ApiResponse<CommunityCommentDto>>(
    `/community/posts/${postId}/comments`,
    { content, parentCommentId },
  );
  const data = unwrapApiResponse(
    response.data,
    '커뮤니티 댓글 작성 API 응답이 올바르지 않습니다.',
    isCommunityCommentDto,
  );
  if (data.postId !== postId) {
    throw new Error('커뮤니티 댓글 작성 API 응답이 올바르지 않습니다.');
  }
  return mapComment(data);
}
