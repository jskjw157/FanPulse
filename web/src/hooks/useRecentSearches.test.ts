import { renderHook, act } from '@testing-library/react';
import { describe, it, expect, beforeEach } from 'vitest';
import { useRecentSearches } from './useRecentSearches';

describe('useRecentSearches', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('initializes with empty array', () => {
    const { result } = renderHook(() => useRecentSearches());
    expect(result.current.searches).toEqual([]);
  });

  it('loads existing searches from localStorage', () => {
    localStorage.setItem(
      'fanpulse_recent_searches',
      JSON.stringify(['BTS', 'BLACKPINK'])
    );

    const { result } = renderHook(() => useRecentSearches());
    expect(result.current.searches).toEqual(['BTS', 'BLACKPINK']);
  });

  it('adds search and updates state', () => {
    const { result } = renderHook(() => useRecentSearches());

    act(() => {
      result.current.addSearch('BTS');
    });

    expect(result.current.searches).toEqual(['BTS']);
  });

  it('removes search and updates state', () => {
    localStorage.setItem(
      'fanpulse_recent_searches',
      JSON.stringify(['BTS', 'BLACKPINK'])
    );

    const { result } = renderHook(() => useRecentSearches());

    act(() => {
      result.current.removeSearch('BTS');
    });

    expect(result.current.searches).toEqual(['BLACKPINK']);
  });

  it('clears all searches', () => {
    localStorage.setItem(
      'fanpulse_recent_searches',
      JSON.stringify(['BTS', 'BLACKPINK'])
    );

    const { result } = renderHook(() => useRecentSearches());

    act(() => {
      result.current.clearAll();
    });

    expect(result.current.searches).toEqual([]);
  });
});
