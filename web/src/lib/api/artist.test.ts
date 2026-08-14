import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@/lib/api-client', () => ({
  apiClient: { get: vi.fn() },
}));

import { apiClient } from '@/lib/api-client';
import { fetchActiveArtists, fetchArtistDetail } from './artist';

const mockedGet = vi.mocked(apiClient.get);

const artist = {
  id: '11111111-1111-1111-1111-111111111111',
  name: 'aespa',
  englishName: 'aespa',
  agency: 'SM Entertainment',
  description: 'Artist description',
  profileImageUrl: 'https://example.com/aespa.jpg',
  isGroup: true,
  members: ['Karina', 'Winter'],
  active: true,
  debutDate: '2020-11-17',
  createdAt: '2026-01-01T00:00:00Z',
};

describe('fetchArtistDetail', () => {
  beforeEach(() => vi.clearAllMocks());

  it('loads an artist from the Spring artist detail endpoint', async () => {
    mockedGet.mockResolvedValue({ data: { success: true, data: artist } });

    await expect(fetchArtistDetail(artist.id)).resolves.toEqual(artist);
    expect(mockedGet).toHaveBeenCalledWith(`/artists/${artist.id}`, { signal: undefined });
  });

  it.each([
    { success: false, data: artist },
    { success: true, data: null },
  ])('rejects an unsuccessful or empty API envelope: %o', async (payload) => {
    mockedGet.mockResolvedValue({ data: payload });

    await expect(fetchArtistDetail(artist.id)).rejects.toThrow(
      '아티스트 API 응답이 올바르지 않습니다.'
    );
  });

  it.each([
    { ...artist, id: 'not-a-uuid' },
    { ...artist, members: ['Karina', 42] },
    { ...artist, debutDate: '2020-02-31' },
  ])('rejects malformed successful artist payload: %o', async (data) => {
    mockedGet.mockResolvedValue({ data: { success: true, data } });

    await expect(fetchArtistDetail(artist.id)).rejects.toThrow(
      '아티스트 API 응답이 올바르지 않습니다.'
    );
  });
});

describe('fetchActiveArtists', () => {
  beforeEach(() => vi.clearAllMocks());

  const summary = {
    id: artist.id,
    name: artist.name,
    englishName: artist.englishName,
    agency: artist.agency,
    profileImageUrl: artist.profileImageUrl,
    isGroup: artist.isGroup,
  };

  it('loads active artist choices from the real paginated API', async () => {
    mockedGet.mockResolvedValue({
      data: {
        success: true,
        data: { content: [summary], totalElements: 1, page: 0, size: 100, totalPages: 1 },
      },
    });

    await expect(fetchActiveArtists()).resolves.toEqual([summary]);
    expect(mockedGet).toHaveBeenCalledWith('/artists', {
      params: { activeOnly: true, page: 0, size: 100, sortBy: 'name', sortDir: 'asc' },
      signal: undefined,
    });
  });

  it('rejects repeated or incomplete rows across pages', async () => {
    mockedGet
      .mockResolvedValueOnce({
        data: {
          success: true,
          data: { content: [summary], totalElements: 101, page: 0, size: 100, totalPages: 2 },
        },
      })
      .mockResolvedValueOnce({
        data: {
          success: true,
          data: { content: [summary], totalElements: 101, page: 1, size: 100, totalPages: 2 },
        },
      });

    await expect(fetchActiveArtists()).rejects.toThrow('아티스트 목록 API 응답이 올바르지 않습니다.');
  });

  it.each([
    { content: [{ ...summary, id: 7 }], totalElements: 1, page: 0, size: 100, totalPages: 1 },
    { content: [summary], totalElements: -1, page: 0, size: 100, totalPages: 1 },
    { content: [summary], totalElements: 1, page: 0, size: 100, totalPages: 2 },
    { content: [summary], totalElements: 101, page: 1, size: 100, totalPages: 2 },
  ])('rejects malformed artist list payloads: %o', async (data) => {
    mockedGet.mockResolvedValue({ data: { success: true, data } });
    await expect(fetchActiveArtists()).rejects.toThrow('아티스트 목록 API 응답이 올바르지 않습니다.');
  });
});
