import { render, screen } from '@testing-library/react'
import ArtistDetailPage from './page'
import { vi } from 'vitest'

vi.mock('next/navigation', () => ({
  useRouter: () => ({
    back: vi.fn(),
  }),
  useSearchParams: () => ({
    get: (key: string) => key === 'artist' ? 'BTS' : null,
  }),
}))

describe('ArtistDetailPage', () => {
  it('renders page header', () => {
    render(<ArtistDetailPage />)
    expect(screen.getByText('Artist Profile')).toBeInTheDocument()
  })

  it('renders profile section skeleton', () => {
    render(<ArtistDetailPage />)
    expect(screen.getByTestId('artist-profile-skeleton')).toBeInTheDocument()
  })
})
