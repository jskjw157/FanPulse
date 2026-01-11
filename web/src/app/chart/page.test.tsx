import { render, screen } from '@testing-library/react'
import ChartPage from './page'
import { vi } from 'vitest'

vi.mock('next/navigation', () => ({
  useRouter: () => ({
    back: vi.fn(),
  }),
}))

describe('ChartPage', () => {
  it('renders page header with correct title', () => {
    render(<ChartPage />)
    expect(screen.getByText('Real-time Chart')).toBeInTheDocument()
  })

  it('renders ranking items (skeleton horizontal cards)', () => {
    render(<ChartPage />)
    const cards = screen.getAllByTestId('skeleton-card')
    expect(cards.length).toBeGreaterThanOrEqual(5)
  })
})
