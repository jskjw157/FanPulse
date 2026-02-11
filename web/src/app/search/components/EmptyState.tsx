interface EmptyStateProps {
  message?: string;
}

export function EmptyState({
  message = '검색 결과가 없습니다',
}: EmptyStateProps) {
  const isCustomMessage = message !== '검색 결과가 없습니다';

  return (
    <div className="text-center py-16">
      <i
        data-testid="empty-icon"
        className="ri-search-line text-4xl text-gray-300 mb-4 block"
      />
      <p className="text-gray-500">{message}</p>
      {!isCustomMessage && (
        <p className="text-sm text-gray-400 mt-1">다른 검색어를 시도해보세요</p>
      )}
    </div>
  );
}
