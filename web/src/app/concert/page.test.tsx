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

  it('renders event list', () => {
    render(<ConcertPage />)
    expect(screen.getByText('BTS World Tour Seoul')).toBeInTheDocument()
    expect(screen.getByText('BLACKPINK World Tour')).toBeInTheDocument()
  })
})
