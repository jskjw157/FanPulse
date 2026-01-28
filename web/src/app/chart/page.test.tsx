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

  it('renders ranking items', () => {
    render(<ChartPage />)
    expect(screen.getByText('Super Shy')).toBeInTheDocument()
    expect(screen.getByText('Jungkook (BTS)')).toBeInTheDocument()
  })
})
