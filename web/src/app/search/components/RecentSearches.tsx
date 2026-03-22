'use client';

interface RecentSearchesProps {
  searches: string[];
  onSelect: (query: string) => void;
  onRemove: (query: string) => void;
  onClearAll: () => void;
}

export function RecentSearches({
  searches,
  onSelect,
  onRemove,
  onClearAll,
}: RecentSearchesProps) {
  if (searches.length === 0) {
    return null;
  }

  return (
    <section className="px-4 py-4">
      <div className="flex items-center justify-between mb-3">
        <h2 className="text-sm font-bold text-gray-900">최근 검색어</h2>
        <button
          type="button"
          onClick={onClearAll}
          className="text-xs text-gray-500 hover:text-gray-700"
        >
          전체 삭제
        </button>
      </div>
      <div className="flex flex-wrap gap-2">
        {searches.map((query) => (
          <div
            key={query}
            className="flex items-center gap-1 bg-gray-100 rounded-full"
          >
            <button
              type="button"
              onClick={() => onSelect(query)}
              className="flex items-center gap-2 pl-3 pr-1 py-2 text-sm text-gray-700 hover:text-gray-900"
            >
              <i className="ri-time-line text-gray-400" />
              {query}
            </button>
            <button
              type="button"
              onClick={(e) => {
                e.stopPropagation();
                onRemove(query);
              }}
              aria-label={`${query} 삭제`}
              className="pr-3 py-2 text-gray-400 hover:text-gray-600"
            >
              <i className="ri-close-line" />
            </button>
          </div>
        ))}
      </div>
    </section>
  );
}
