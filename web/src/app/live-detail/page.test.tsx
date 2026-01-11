import { render, screen } from '@testing-library/react'
import LiveDetailPage from './page'
import { vi } from 'vitest'

vi.mock('next/navigation', () => ({
  useRouter: () => ({
    back: vi.fn(),
  }),
}))

describe('LiveDetailPage', () => {
  it('renders page header', () => {
    render(<LiveDetailPage />)
    expect(screen.getByText('Live Stream')).toBeInTheDocument()
  })

  it('renders video player skeleton', () => {
    render(<LiveDetailPage />)
    expect(screen.getByTestId('video-player-skeleton')).toBeInTheDocument()
  })
})
