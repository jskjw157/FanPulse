export interface News {
  id: number;
  title: string;
  summary: string;
  thumbnailUrl: string;
  source: string;
  publishedAt: string;
}

export interface NewsDetail extends News {
  content: string;
  sourceUrl: string;
  author?: string;
}
