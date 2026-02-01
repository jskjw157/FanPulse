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

  it('renders news content', () => {
    render(<NewsDetailPage />)
    expect(screen.getByText('BTS 새 앨범 발매 예정')).toBeInTheDocument()
    expect(screen.getByText('관련 뉴스')).toBeInTheDocument()
  })
})
