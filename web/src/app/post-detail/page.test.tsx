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

  it('renders post content and comments skeleton', () => {
    render(<PostDetailPage />)
    expect(screen.getByTestId('post-content-skeleton')).toBeInTheDocument()
    expect(screen.getByTestId('comments-skeleton')).toBeInTheDocument()
  })
})
