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
    expect(screen.getByText('NewJeans 컴백 쇼케이스')).toBeInTheDocument()
    expect(screen.getByText('실시간 채팅')).toBeInTheDocument()
  })

  it('renders video thumbnail', () => {
    render(<LiveDetailPage />)
    expect(screen.getByAltText('Live Stream')).toBeInTheDocument()
  })
})
