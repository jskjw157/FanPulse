import { render, screen } from '@testing-library/react'
import CommunityPage from './page'
import { vi } from 'vitest'

// useRouter mock
vi.mock('next/navigation', () => ({
  useRouter: () => ({
    back: vi.fn(),
  }),
}))

describe('CommunityPage', () => {
  it('renders page header with correct title', () => {
    render(<CommunityPage />)
    expect(screen.getByText('Community')).toBeInTheDocument()
  })

  it('renders tab navigation', () => {
    render(<CommunityPage />)
    expect(screen.getByText('전체')).toBeInTheDocument()
    expect(screen.getByText('인기')).toBeInTheDocument()
  })

  it('renders post skeleton cards', () => {
    render(<CommunityPage />)
    const cards = screen.getAllByTestId('skeleton-card')
    expect(cards.length).toBeGreaterThanOrEqual(3)
  })
})
