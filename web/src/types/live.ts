export type LiveStatus = 'LIVE' | 'SCHEDULED' | 'ENDED';

export interface Live {
  id: number;
  title: string;
  artistName: string;
  thumbnailUrl: string;
  status: LiveStatus;
  scheduledAt?: string;
  viewerCount?: number;
}

export interface LiveDetail extends Live {
  description: string;
  streamUrl: string;
  startedAt?: string;
  endedAt?: string;
  createdAt?: string;
}
