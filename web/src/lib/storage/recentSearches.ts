const STORAGE_KEY = 'fanpulse_recent_searches';
const MAX_ITEMS = 10;

export function getRecentSearches(): string[] {
  if (typeof window === 'undefined') return [];
  try {
    const stored = localStorage.getItem(STORAGE_KEY);
    return stored ? JSON.parse(stored) : [];
  } catch {
    return [];
  }
}

export function addRecentSearch(query: string): void {
  if (typeof window === 'undefined') return;
  const searches = getRecentSearches();
  const filtered = searches.filter((s) => s !== query);
  const updated = [query, ...filtered].slice(0, MAX_ITEMS);
  localStorage.setItem(STORAGE_KEY, JSON.stringify(updated));
}

export function removeRecentSearch(query: string): void {
  if (typeof window === 'undefined') return;
  const searches = getRecentSearches();
  const updated = searches.filter((s) => s !== query);
  localStorage.setItem(STORAGE_KEY, JSON.stringify(updated));
}

export function clearRecentSearches(): void {
  if (typeof window === 'undefined') return;
  localStorage.removeItem(STORAGE_KEY);
}
