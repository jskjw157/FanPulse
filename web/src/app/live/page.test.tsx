import { render, screen } from '@testing-library/react'
import LivePage from './page'

// useRouter mock (PageHeader 내부에서 사용)
vi.mock('next/navigation', () => ({
  useRouter: () => ({
    back: vi.fn(),
  }),
}))

describe('LivePage', () => {
  it('renders page header with correct title', () => {
    render(<LivePage />)
    expect(screen.getByText('Live Now')).toBeInTheDocument()
  })

  it('renders live stream cards', () => {
    render(<LivePage />)
    expect(screen.getByText('NewJeans 컴백 쇼케이스')).toBeInTheDocument()
    expect(screen.getByText('BTS Fan Meeting Special')).toBeInTheDocument()
  })
})
