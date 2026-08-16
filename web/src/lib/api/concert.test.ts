import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@/lib/api-client', () => ({
  apiClient: { get: vi.fn() },
}));

import { apiClient } from '@/lib/api-client';
import { fetchConcert, fetchConcerts } from './concert';

const dto = {
  id: '11111111-1111-1111-1111-111111111111',
  externalId: 'PF298563',
  name: 'WHIB FAN CONCERT: BLUE HOUR',
  artist: '김준민, 이하준',
  venueName: 'NOL 씨어터 합정',
  venueHall: '동양생명홀',
  startDate: '2026-09-12',
  endDate: '2026-09-12',
  status: '공연예정',
  posterUrl: 'https://kopis.or.kr/upload/pfmPoster/PF_PF298563.gif',
  performanceTime: '토요일(14:00,18:00)',
  priceText: '전석 110,000원',
  performers: '김준민, 이하준',
  runtime: '1시간 40분',
  ageRating: '만 7세 이상',
  venueAddress: '서울특별시 마포구 양화로 45',
  ticketUrl: 'https://kopis.or.kr/por/db/pblprfr/pblprfrView.do?menuId=MNU_00020&mt20Id=PF298563',
};

const page = {
  content: [dto], page: 0, size: 20, totalElements: 1, totalPages: 1, last: true,
};

describe('concert API contract', () => {
  beforeEach(() => vi.clearAllMocks());

  it('maps a strict upcoming concert page', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: page } });

    const result = await fetchConcerts(0, 20);

    expect(result.items[0]).toEqual({
      id: dto.id,
      externalId: dto.externalId,
      title: dto.name,
      artist: dto.artist,
      venue: 'NOL 씨어터 합정 · 동양생명홀',
      startDate: dto.startDate,
      endDate: dto.endDate,
      status: dto.status,
      posterUrl: dto.posterUrl,
      performanceTime: dto.performanceTime,
      priceText: dto.priceText,
      performers: dto.performers,
      runtime: dto.runtime,
      ageRating: dto.ageRating,
      venueAddress: dto.venueAddress,
      ticketUrl: dto.ticketUrl,
    });
    expect(apiClient.get).toHaveBeenCalledWith('/concerts', {
      params: { page: 0, size: 20 }, signal: undefined,
    });
  });

  it('loads a strict concert detail by UUID', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: dto } });
    await expect(fetchConcert(dto.id)).resolves.toMatchObject({ id: dto.id, title: dto.name });
  });

  it('accepts ongoing concerts in page and detail contracts', async () => {
    const ongoing = { ...dto, status: '공연중' };
    vi.mocked(apiClient.get).mockResolvedValueOnce({
      data: { success: true, data: { ...page, content: [ongoing] } },
    });

    await expect(fetchConcerts()).resolves.toMatchObject({
      items: [{ id: dto.id, status: '공연중' }],
    });

    vi.mocked(apiClient.get).mockResolvedValueOnce({ data: { success: true, data: ongoing } });
    await expect(fetchConcert(dto.id)).resolves.toMatchObject({ id: dto.id, status: '공연중' });
  });

  it('rejects a detail response for a different UUID', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({
      data: {
        success: true,
        data: { ...dto, id: '22222222-2222-2222-2222-222222222222' },
      },
    });

    await expect(fetchConcert(dto.id)).rejects.toThrow('요청과 일치하지 않습니다.');
  });

  it.each([
    { ...dto, id: 1 },
    { ...dto, externalId: 'wrong' },
    { ...dto, startDate: '2026-02-31' },
    { ...dto, endDate: '2026-09-11' },
    { ...dto, status: '공연완료' },
    { ...dto, posterUrl: 'https://example.com/poster.jpg' },
    { ...dto, ticketUrl: 'https://example.com/ticket' },
    { ...dto, priceText: 110000 },
  ])('rejects malformed successful rows: %o', async (malformed) => {
    vi.mocked(apiClient.get).mockResolvedValue({
      data: { success: true, data: { ...page, content: [malformed] } },
    });
    await expect(fetchConcerts()).rejects.toThrow('공연 API 응답이 올바르지 않습니다.');
  });

  it('rejects mismatched page metadata', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({
      data: {
        success: true,
        data: { ...page, page: 1, totalElements: 21, totalPages: 2, last: true },
      },
    });
    await expect(fetchConcerts(0, 20)).rejects.toThrow('pagination');
  });

  it.each([
    {
      requestPage: 0,
      data: { ...page, content: [dto, dto], totalElements: 1 },
    },
    {
      requestPage: 0,
      data: { ...page, content: [], totalElements: 1 },
    },
    {
      requestPage: 0,
      data: { ...page, content: [dto], totalElements: 21, totalPages: 2, last: false },
    },
    {
      requestPage: 1,
      data: { ...page, page: 1, content: [], totalElements: 21, totalPages: 2, last: true },
    },
  ])('rejects impossible page content metadata: %o', async ({ requestPage, data }) => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data } });

    await expect(fetchConcerts(requestPage, 20))
      .rejects.toThrow('공연 API 응답이 올바르지 않습니다.');
  });

  it.each([
    { page: -1, size: 20 },
    { page: 0.5, size: 20 },
    { page: 0, size: 0 },
    { page: 0, size: 101 },
    { page: 0, size: 1.5 },
  ])('rejects invalid pagination before the network request: %o', async ({ page, size }) => {
    await expect(fetchConcerts(page, size)).rejects.toThrow('올바르지 않습니다.');
    expect(apiClient.get).not.toHaveBeenCalled();
  });
});
