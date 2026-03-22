import type { Live, LiveDetail } from '@/types/live';
import type { CursorPaginatedResponse } from '@/types/api';
import { apiClient } from '@/lib/api-client';

export interface FetchLiveListParams {
  status?: 'LIVE' | 'SCHEDULED' | 'ENDED';
  limit?: number;
  cursor?: string;
  signal?: AbortSignal;
}

export async function fetchLiveList({
  status,
  limit = 20,
  cursor,
  signal,
}: FetchLiveListParams = {}): Promise<CursorPaginatedResponse<Live>> {
  const { data } = await apiClient.get('/streaming-events', {
    params: { status, limit, cursor },
    signal,
  });
  return data.data;
}

export async function fetchLiveDetail(
  id: string | number,
  signal?: AbortSignal
): Promise<LiveDetail> {
  const { data } = await apiClient.get(`/streaming-events/${id}`, { signal });
  return data.data;
}
