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

  it('renders posts', () => {
    render(<CommunityPage />)
    expect(screen.getByText('ARMY_Forever')).toBeInTheDocument()
    expect(screen.getByText('Blink_Girl')).toBeInTheDocument()
  })
})
