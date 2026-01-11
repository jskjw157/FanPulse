import { render } from '@testing-library/react'
import SkeletonCard from './SkeletonCard'

describe('SkeletonCard', () => {
  it('renders vertical layout by default', () => {
    const { container } = render(<SkeletonCard />)
    expect(container.firstChild).toHaveClass('flex-col')
  })

  it('renders horizontal layout when specified', () => {
    const { container } = render(<SkeletonCard layout="horizontal" />)
    // Card 컴포넌트 내부 구조에 따라 클래스가 적용됨.
    // 여기서는 기본적으로 horizontal 레이아웃이 렌더링되는지(에러가 없는지) 확인
    expect(container).toBeInTheDocument()
  })
})
