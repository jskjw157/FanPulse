export interface News {
  id: string;
  title: string;
  summary: string | null;
  thumbnailUrl: string | null;
  source: string;
  publishedAt: string;
}

export interface NewsDetail extends News {
  artistId: string;
  content: string;
  sourceUrl: string;
  category: string;
  viewCount: number;
  createdAt: string;
}
