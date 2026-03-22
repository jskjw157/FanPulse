'use client';

import { useRef, useEffect } from 'react';

interface SearchBarProps {
  value: string;
  onChange: (value: string) => void;
  onSearch: (query: string) => void;
  onClear: () => void;
  placeholder?: string;
  minLength?: number;
  autoFocus?: boolean;
}

export function SearchBar({
  value,
  onChange,
  onSearch,
  onClear,
  placeholder = '라이브, 뉴스 검색...',
  minLength = 2,
  autoFocus = false,
}: SearchBarProps) {
  const inputRef = useRef<HTMLInputElement>(null);
  const canSearch = value.length >= minLength;

  useEffect(() => {
    if (autoFocus && inputRef.current) {
      inputRef.current.focus();
    }
  }, [autoFocus]);

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' && canSearch) {
      onSearch(value);
    }
  };

  const handleSearchClick = () => {
    if (canSearch) {
      onSearch(value);
    }
  };

  return (
    <div className="flex items-center gap-2">
      <div className="flex-1 relative">
        <i className="ri-search-line absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
        <input
          ref={inputRef}
          type="text"
          value={value}
          onChange={(e) => onChange(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder={placeholder}
          className="w-full bg-gray-100 rounded-full pl-10 pr-10 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-pink-500"
        />
        {value && (
          <button
            type="button"
            onClick={onClear}
            aria-label="지우기"
            className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
          >
            <i className="ri-close-circle-fill" />
          </button>
        )}
      </div>
      <button
        type="button"
        onClick={handleSearchClick}
        disabled={!canSearch}
        aria-label="검색"
        className="px-4 py-2.5 bg-pink-500 text-white rounded-full text-sm font-medium disabled:bg-gray-300 disabled:cursor-not-allowed hover:bg-pink-600 transition-colors"
      >
        검색
      </button>
    </div>
  );
}
