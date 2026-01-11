import { render, screen } from '@testing-library/react'
import NewsDetailPage from './page'
import { vi } from 'vitest'

vi.mock('next/navigation', () => ({
  useRouter: () => ({
    back: vi.fn(),
  }),
  useSearchParams: () => ({
    get: (key: string) => key === 'id' ? '123' : null,
  }),
}))

describe('NewsDetailPage', () => {
  it('renders page header', () => {
    render(<NewsDetailPage />)
    expect(screen.getByText('News Detail')).toBeInTheDocument()
  })

  it('renders content skeleton', () => {
    render(<NewsDetailPage />)
    expect(screen.getByTestId('news-content-skeleton')).toBeInTheDocument()
  })
})
