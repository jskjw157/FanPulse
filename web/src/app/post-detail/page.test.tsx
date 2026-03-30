import { render, screen } from '@testing-library/react'
import PostDetailPage from './page'
import { vi } from 'vitest'

vi.mock('next/navigation', () => ({
  useRouter: () => ({
    back: vi.fn(),
  }),
  useSearchParams: () => ({
    get: (key: string) => key === 'id' ? '456' : null,
  }),
}))

describe('PostDetailPage', () => {
  it('renders page header', () => {
    render(<PostDetailPage />)
    expect(screen.getByText('Post Detail')).toBeInTheDocument()
  })

  it('renders post content and comments section', () => {
    render(<PostDetailPage />)
    expect(screen.getByText('ARMY_Forever')).toBeInTheDocument()
    expect(screen.getByText('댓글 3')).toBeInTheDocument()
    expect(screen.getByPlaceholderText('댓글을 입력하세요...')).toBeInTheDocument()
  })
})
