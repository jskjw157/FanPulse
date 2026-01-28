export type CursorPaginationResult<T> = {
  page: T[]
  nextCursor: string | null
}

export function applyCursorPagination<T>(
  items: T[],
  cursor: string | null,
  limit: number,
  getId: (item: T) => string
): CursorPaginationResult<T> {
  const clampedLimit = Math.min(50, Math.max(1, limit))

  let startIndex = 0

  if (cursor !== null) {
    const cursorIndex = items.findIndex(item => getId(item) === cursor)
    startIndex = cursorIndex === -1 ? 0 : cursorIndex + 1
  }

  const page = items.slice(startIndex, startIndex + clampedLimit)
  const hasMore = startIndex + page.length < items.length
  const nextCursor = hasMore && page.length > 0 ? getId(page[page.length - 1]) : null

  return { page, nextCursor }
}
