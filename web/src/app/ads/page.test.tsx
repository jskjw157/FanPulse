import { render, screen } from '@testing-library/react'
import AdsPage from './page'
import { vi } from 'vitest'

vi.mock('next/navigation', () => ({
  useRouter: () => ({
    back: vi.fn(),
  }),
}))

describe('AdsPage', () => {
  it('renders page header', () => {
    render(<AdsPage />)
    expect(screen.getByText('Ads & Rewards')).toBeInTheDocument()
  })

  it('renders reward missions skeleton', () => {
    render(<AdsPage />)
    expect(screen.getByTestId('reward-missions-skeleton')).toBeInTheDocument()
  })
})
