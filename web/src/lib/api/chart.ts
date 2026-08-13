import { apiClient } from '@/lib/api-client';

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

interface ApiResponse<T> {
  data: T;
}

export async function fetchLatestChart(
  chartType: ChartType,
  signal?: AbortSignal
): Promise<ChartResponse> {
  const response = await apiClient.get<ApiResponse<ChartResponse>>(
    `/charts/${chartType}/latest`,
    { signal }
  );

  return response.data.data;
}
