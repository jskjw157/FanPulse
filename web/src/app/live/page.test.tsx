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

  it('renders live stream skeleton cards', () => {
    render(<LivePage />)
    // 최소 2개 이상의 스켈레톤 카드가 렌더링되는지 확인 (더미 데이터 기준)
    const cards = screen.getAllByTestId('skeleton-card')
    expect(cards.length).toBeGreaterThanOrEqual(2)
  })
})
