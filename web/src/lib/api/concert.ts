import { apiClient } from '@/lib/api-client';
import {
  isIsoDate,
  isNonEmptyString,
  isRecord,
  isUuid,
  unwrapApiResponse,
} from '@/lib/api-response';
import type { ApiResponse } from '@/types/api';

export type ConcertStatus = '공연예정' | '공연중';

interface ConcertDto {
  id: string;
  externalId: string;
  name: string;
  artist: string | null;
  venueName: string | null;
  venueHall: string | null;
  startDate: string;
  endDate: string;
  status: ConcertStatus;
  posterUrl: string | null;
  performanceTime: string | null;
  priceText: string | null;
  performers: string | null;
  runtime: string | null;
  ageRating: string | null;
  venueAddress: string | null;
  ticketUrl: string;
}

interface ConcertPageDto {
  content: ConcertDto[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface Concert {
  id: string;
  externalId: string;
  title: string;
  artist: string | null;
  venue: string | null;
  startDate: string;
  endDate: string;
  status: ConcertStatus;
  posterUrl: string | null;
  performanceTime: string | null;
  priceText: string | null;
  performers: string | null;
  runtime: string | null;
  ageRating: string | null;
  venueAddress: string | null;
  ticketUrl: string;
}

export interface ConcertPage {
  items: Concert[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

function isNullableText(value: unknown): value is string | null {
  return value === null || isNonEmptyString(value);
}

function isNonNegativeInteger(value: unknown): value is number {
  return typeof value === 'number' && Number.isInteger(value) && value >= 0;
}

function isPositiveInteger(value: unknown): value is number {
  return typeof value === 'number' && Number.isInteger(value) && value > 0;
}

function isKopisPosterUrl(value: unknown): value is string | null {
  if (value === null) return true;
  if (typeof value !== 'string') return false;
  try {
    const url = new URL(value);
    return (
      url.protocol === 'https:' &&
      url.hostname === 'kopis.or.kr' &&
      url.port === '' &&
      url.username === '' &&
      url.password === '' &&
      url.pathname.startsWith('/upload/')
    );
  } catch {
    return false;
  }
}

function isKopisTicketUrl(value: unknown, externalId: string): value is string {
  if (typeof value !== 'string') return false;
  try {
    const url = new URL(value);
    return (
      url.protocol === 'https:' &&
      url.hostname === 'kopis.or.kr' &&
      url.port === '' &&
      url.username === '' &&
      url.password === '' &&
      url.pathname === '/por/db/pblprfr/pblprfrView.do' &&
      url.searchParams.get('menuId') === 'MNU_00020' &&
      url.searchParams.get('mt20Id') === externalId
    );
  } catch {
    return false;
  }
}

function isConcertDto(value: unknown): value is ConcertDto {
  if (!isRecord(value)) return false;
  if (
    !isUuid(value.id) ||
    typeof value.externalId !== 'string' ||
    !/^PF\d{6,12}$/.test(value.externalId) ||
    !isNonEmptyString(value.name) ||
    !isNullableText(value.artist) ||
    !isNullableText(value.venueName) ||
    !isNullableText(value.venueHall) ||
    !isIsoDate(value.startDate) ||
    !isIsoDate(value.endDate) ||
    (value.status !== '공연예정' && value.status !== '공연중') ||
    !isKopisPosterUrl(value.posterUrl) ||
    !isNullableText(value.performanceTime) ||
    !isNullableText(value.priceText) ||
    !isNullableText(value.performers) ||
    !isNullableText(value.runtime) ||
    !isNullableText(value.ageRating) ||
    !isNullableText(value.venueAddress)
  ) {
    return false;
  }
  if (value.endDate < value.startDate) return false;
  return isKopisTicketUrl(value.ticketUrl, value.externalId);
}

function isConcertPageDto(value: unknown): value is ConcertPageDto {
  if (
    !isRecord(value) ||
    !Array.isArray(value.content) ||
    !value.content.every(isConcertDto) ||
    !isNonNegativeInteger(value.page) ||
    !isPositiveInteger(value.size) ||
    !isNonNegativeInteger(value.totalElements) ||
    !isNonNegativeInteger(value.totalPages) ||
    typeof value.last !== 'boolean' ||
    value.content.length > value.size
  ) {
    return false;
  }
  const expectedPages = value.totalElements === 0 ? 0 : Math.ceil(value.totalElements / value.size);
  if (value.totalPages !== expectedPages) return false;
  if (value.totalPages === 0) {
    return value.page === 0 && value.content.length === 0 && value.last;
  }
  if (value.page >= value.totalPages || value.last !== (value.page === value.totalPages - 1)) {
    return false;
  }
  const remaining = value.totalElements - value.page * value.size;
  const expectedContentLength = Math.min(value.size, remaining);
  return expectedContentLength > 0 && value.content.length === expectedContentLength;
}

function mapConcert(dto: ConcertDto): Concert {
  const venue = [dto.venueName, dto.venueHall].filter((value): value is string => value !== null).join(' · ') || null;
  return {
    id: dto.id,
    externalId: dto.externalId,
    title: dto.name,
    artist: dto.artist,
    venue,
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
  };
}

export async function fetchConcerts(
  page = 0,
  size = 20,
  signal?: AbortSignal,
): Promise<ConcertPage> {
  if (!isNonNegativeInteger(page)) {
    throw new Error('공연 페이지가 올바르지 않습니다.');
  }
  if (!isPositiveInteger(size) || size > 100) {
    throw new Error('공연 페이지 크기가 올바르지 않습니다.');
  }
  const response = await apiClient.get<ApiResponse<ConcertPageDto>>('/concerts', {
    params: { page, size },
    signal,
  });
  const data = unwrapApiResponse(response.data, '공연 API 응답이 올바르지 않습니다.', isConcertPageDto);
  if (data.page !== page || data.size !== size) {
    throw new Error('공연 API pagination metadata가 요청과 일치하지 않습니다.');
  }
  return {
    items: data.content.map(mapConcert),
    page: data.page,
    size: data.size,
    totalElements: data.totalElements,
    totalPages: data.totalPages,
    last: data.last,
  };
}

export async function fetchConcert(id: string, signal?: AbortSignal): Promise<Concert> {
  if (!isUuid(id)) throw new Error('공연 ID가 올바르지 않습니다.');
  const response = await apiClient.get<ApiResponse<ConcertDto>>(`/concerts/${id}`, { signal });
  const data = unwrapApiResponse(
    response.data,
    '공연 상세 API 응답이 올바르지 않습니다.',
    isConcertDto,
  );
  if (data.id.toLowerCase() !== id.toLowerCase()) {
    throw new Error('공연 상세 API 응답이 요청과 일치하지 않습니다.');
  }
  return mapConcert(data);
}
