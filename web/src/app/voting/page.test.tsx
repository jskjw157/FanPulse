import { render, screen } from '@testing-library/react'
import VotingPage from './page'
import { vi } from 'vitest'

vi.mock('next/navigation', () => ({
  useRouter: () => ({
    back: vi.fn(),
  }),
}))

describe('VotingPage', () => {
  it('renders page header with correct title', () => {
    render(<VotingPage />)
    expect(screen.getByText('Voting')).toBeInTheDocument()
  })

  it('renders voting categories and items', () => {
    render(<VotingPage />)
    expect(screen.getByTestId('voting-list-skeleton')).toBeInTheDocument()
  })
})
