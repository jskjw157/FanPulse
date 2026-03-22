import { describe, it, expect, beforeEach, vi } from 'vitest';
import {
  getRecentSearches,
  addRecentSearch,
  removeRecentSearch,
  clearRecentSearches,
} from './recentSearches';

describe('recentSearches', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  describe('getRecentSearches', () => {
    it('returns empty array when no searches stored', () => {
      expect(getRecentSearches()).toEqual([]);
    });

    it('returns stored searches', () => {
      localStorage.setItem(
        'fanpulse_recent_searches',
        JSON.stringify(['BTS', 'BLACKPINK'])
      );
      expect(getRecentSearches()).toEqual(['BTS', 'BLACKPINK']);
    });

    it('returns empty array on parse error', () => {
      localStorage.setItem('fanpulse_recent_searches', 'invalid json');
      expect(getRecentSearches()).toEqual([]);
    });
  });

  describe('addRecentSearch', () => {
    it('adds search to beginning', () => {
      addRecentSearch('BTS');
      expect(getRecentSearches()).toEqual(['BTS']);
    });

    it('moves duplicate to beginning', () => {
      addRecentSearch('BTS');
      addRecentSearch('BLACKPINK');
      addRecentSearch('BTS');
      expect(getRecentSearches()).toEqual(['BTS', 'BLACKPINK']);
    });

    it('limits to 10 items', () => {
      for (let i = 0; i < 12; i++) {
        addRecentSearch(`search${i}`);
      }
      const searches = getRecentSearches();
      expect(searches).toHaveLength(10);
      expect(searches[0]).toBe('search11');
    });
  });

  describe('removeRecentSearch', () => {
    it('removes specific search', () => {
      addRecentSearch('BTS');
      addRecentSearch('BLACKPINK');
      removeRecentSearch('BTS');
      expect(getRecentSearches()).toEqual(['BLACKPINK']);
    });
  });

  describe('clearRecentSearches', () => {
    it('clears all searches', () => {
      addRecentSearch('BTS');
      addRecentSearch('BLACKPINK');
      clearRecentSearches();
      expect(getRecentSearches()).toEqual([]);
    });
  });

  describe('SSR safety', () => {
    it('handles server environment', () => {
      const originalWindow = global.window;
      // @ts-expect-error - testing SSR
      delete global.window;

      expect(getRecentSearches()).toEqual([]);

      global.window = originalWindow;
    });
  });
});
