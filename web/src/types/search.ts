import type { Live } from './live';
import type { News } from './news';

export interface SearchResult {
  live: Live[];
  news: News[];
}

export interface RecentSearchItem {
  query: string;
  searchedAt: string;
}
