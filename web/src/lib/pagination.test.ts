import { applyCursorPagination } from './pagination'

type Item = {
  id: string
}

const getId = (item: Item) => item.id

describe('applyCursorPagination', () => {
  const items: Item[] = ['a', 'b', 'c', 'd', 'e'].map(id => ({ id }))

  it('returns first page when cursor is null', () => {
    const result = applyCursorPagination(items, null, 2, getId)

    expect(result.page.map(getId)).toEqual(['a', 'b'])
    expect(result.nextCursor).toBe('b')
  })

  it('starts after cursor when cursor is found', () => {
    const result = applyCursorPagination(items, 'b', 2, getId)

    expect(result.page.map(getId)).toEqual(['c', 'd'])
    expect(result.nextCursor).toBe('d')
  })

  it('treats missing cursor as start', () => {
    const result = applyCursorPagination(items, 'missing', 2, getId)

    expect(result.page.map(getId)).toEqual(['a', 'b'])
    expect(result.nextCursor).toBe('b')
  })

  it('returns empty page when cursor is last item', () => {
    const result = applyCursorPagination(items, 'e', 2, getId)

    expect(result.page).toEqual([])
    expect(result.nextCursor).toBeNull()
  })

  it('clamps limit to minimum of 1', () => {
    const result = applyCursorPagination(items, null, 0, getId)

    expect(result.page.map(getId)).toEqual(['a'])
    expect(result.nextCursor).toBe('a')
  })

  it('clamps limit to maximum of 50', () => {
    const largeItems: Item[] = Array.from({ length: 60 }, (_, index) => ({
      id: String(index + 1),
    }))

    const result = applyCursorPagination(largeItems, null, 100, getId)

    expect(result.page).toHaveLength(50)
    expect(result.nextCursor).toBe('50')
  })
})
