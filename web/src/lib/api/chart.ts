import { apiClient } from '@/lib/api-client';
import {
  isIsoDate,
  isIsoInstant,
  isNonEmptyString,
  isNullableInteger,
  isPositiveInteger,
  isRecord,
  isUuid,
  unwrapApiResponse,
} from '@/lib/api-response';
import type { ApiResponse } from '@/types/api';

export type ChartType =
  | 'MELON'
  | 'BUGS'
  | 'GENIE'
  | 'FLO'
  | 'VIBE'
  | 'BILLBOARD_KR'
  | 'BILLBOARD_US'
  | 'SPOTIFY'
  | 'APPLE_MUSIC';

const CHART_TYPES = new Set<ChartType>([
  'MELON',
  'BUGS',
  'GENIE',
  'FLO',
  'VIBE',
  'BILLBOARD_KR',
  'BILLBOARD_US',
  'SPOTIFY',
  'APPLE_MUSIC',
]);

export interface ChartEntry {
  id: string;
  rank: number;
  trackId: string;
  artistId: string;
  trackTitle: string;
  artistName: string;
  previousRank: number | null;
  peakRank: number;
  weeksOnChart: number;
  rankChange: number | null;
  isNew: boolean;
}

export interface ChartResponse {
  id: string;
  chartType: ChartType;
  chartDate: string;
  entries: ChartEntry[];
  createdAt: string;
}

function isChartEntry(value: unknown): value is ChartEntry {
  if (!isRecord(value)) return false;
  return (
    isUuid(value.id) &&
    isPositiveInteger(value.rank) &&
    isUuid(value.trackId) &&
    isUuid(value.artistId) &&
    isNonEmptyString(value.trackTitle) &&
    isNonEmptyString(value.artistName) &&
    (value.previousRank === null || isPositiveInteger(value.previousRank)) &&
    isPositiveInteger(value.peakRank) &&
    isPositiveInteger(value.weeksOnChart) &&
    isNullableInteger(value.rankChange) &&
    typeof value.isNew === 'boolean'
  );
}

function isChartResponse(value: unknown): value is ChartResponse {
  if (!isRecord(value)) return false;
  return (
    isUuid(value.id) &&
    typeof value.chartType === 'string' &&
    CHART_TYPES.has(value.chartType as ChartType) &&
    isIsoDate(value.chartDate) &&
    Array.isArray(value.entries) &&
    value.entries.every(isChartEntry) &&
    isIsoInstant(value.createdAt)
  );
}

export async function fetchLatestChart(
  chartType: ChartType,
  signal?: AbortSignal
): Promise<ChartResponse> {
  const response = await apiClient.get<ApiResponse<ChartResponse>>(
    `/charts/${chartType}/latest`,
    { signal }
  );

  return unwrapApiResponse(
    response.data,
    '차트 API 응답이 올바르지 않습니다.',
    isChartResponse
  );
}
