import { render, screen } from '@testing-library/react'
import ConcertPage from './page'
import { vi } from 'vitest'

vi.mock('next/navigation', () => ({
  useRouter: () => ({
    back: vi.fn(),
  }),
}))

describe('ConcertPage', () => {
  it('renders page header with correct title', () => {
    render(<ConcertPage />)
    expect(screen.getByText('Upcoming Concerts')).toBeInTheDocument()
  })

  it('renders event list (skeleton cards)', () => {
    render(<ConcertPage />)
    const cards = screen.getAllByTestId('skeleton-card')
    expect(cards.length).toBeGreaterThanOrEqual(2)
  })
})
