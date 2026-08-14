import type { News, NewsDetail } from '@/types/news';
import {
  isIsoInstant,
  isNonEmptyString,
  isRecord,
  isUuid,
} from '@/lib/api-response';

const NEWS_CATEGORIES = new Set([
  'GENERAL',
  'RELEASE',
  'TOUR',
  'AWARD',
  'VARIETY',
  'SOCIAL_MEDIA',
  'COLLABORATION',
]);

interface NewsApiDto {
  id: string;
  artistId: string;
  title: string;
  content: string;
  sourceUrl: string;
  sourceName: string;
  thumbnailUrl: string | null;
  category: string;
  viewCount: number;
  publishedAt: string;
  createdAt: string;
}

interface SearchNewsApiDto {
  id: string;
  title: string;
  summary: string;
  sourceName: string;
  publishedAt: string;
}

export interface NewsSummaryApiDto {
  id: string;
  artistId: string;
  title: string;
  thumbnailUrl: string | null;
  sourceName: string;
  category: string;
  publishedAt: string;
}

function isHttpsUrl(value: unknown): value is string {
  if (!isNonEmptyString(value)) return false;

  try {
    return new URL(value).protocol === 'https:';
  } catch {
    return false;
  }
}

function isNullableHttpsUrl(value: unknown): value is string | null {
  return value === null || isHttpsUrl(value);
}

export function isNewsApiDto(value: unknown): value is NewsApiDto {
  if (!isRecord(value)) return false;

  return (
    isUuid(value.id) &&
    isUuid(value.artistId) &&
    isNonEmptyString(value.title) &&
    isNonEmptyString(value.content) &&
    isHttpsUrl(value.sourceUrl) &&
    isNonEmptyString(value.sourceName) &&
    isNullableHttpsUrl(value.thumbnailUrl) &&
    typeof value.category === 'string' &&
    NEWS_CATEGORIES.has(value.category) &&
    Number.isInteger(value.viewCount) &&
    (value.viewCount as number) >= 0 &&
    isIsoInstant(value.publishedAt) &&
    isIsoInstant(value.createdAt)
  );
}

export function isNewsApiDtoArray(value: unknown): value is NewsApiDto[] {
  return Array.isArray(value) && value.every(isNewsApiDto);
}

export function mapNewsApiDto(value: NewsApiDto): News {
  return {
    id: value.id,
    title: value.title,
    summary: value.content,
    thumbnailUrl: value.thumbnailUrl,
    source: value.sourceName,
    publishedAt: value.publishedAt,
  };
}

export function mapNewsDetailApiDto(value: NewsApiDto): NewsDetail {
  return {
    ...mapNewsApiDto(value),
    artistId: value.artistId,
    content: value.content,
    sourceUrl: value.sourceUrl,
    category: value.category,
    viewCount: value.viewCount,
    createdAt: value.createdAt,
  };
}

export function isNewsSummaryApiDto(value: unknown): value is NewsSummaryApiDto {
  if (!isRecord(value)) return false;

  return (
    isUuid(value.id) &&
    isUuid(value.artistId) &&
    isNonEmptyString(value.title) &&
    isNullableHttpsUrl(value.thumbnailUrl) &&
    isNonEmptyString(value.sourceName) &&
    typeof value.category === 'string' &&
    NEWS_CATEGORIES.has(value.category) &&
    isIsoInstant(value.publishedAt)
  );
}

export function mapNewsSummaryApiDto(value: NewsSummaryApiDto): News {
  return {
    id: value.id,
    title: value.title,
    summary: null,
    thumbnailUrl: value.thumbnailUrl,
    source: value.sourceName,
    publishedAt: value.publishedAt,
  };
}

export function isSearchNewsApiDto(value: unknown): value is SearchNewsApiDto {
  if (!isRecord(value)) return false;

  return (
    isUuid(value.id) &&
    isNonEmptyString(value.title) &&
    isNonEmptyString(value.summary) &&
    isNonEmptyString(value.sourceName) &&
    isIsoInstant(value.publishedAt)
  );
}

export function mapSearchNewsApiDto(value: SearchNewsApiDto): News {
  return {
    id: value.id,
    title: value.title,
    summary: value.summary,
    thumbnailUrl: null,
    source: value.sourceName,
    publishedAt: value.publishedAt,
  };
}
