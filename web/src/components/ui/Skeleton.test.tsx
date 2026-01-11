import { render } from '@testing-library/react'
import Skeleton from './Skeleton'

describe('Skeleton', () => {
  it('renders correctly', () => {
    const { container } = render(<Skeleton />)
    expect(container.firstChild).toHaveClass('bg-gray-200')
    expect(container.firstChild).toHaveClass('rounded-md')
  })

  it('renders circle variant correctly', () => {
    const { container } = render(<Skeleton variant="circle" />)
    expect(container.firstChild).toHaveClass('rounded-full')
  })

  it('applies custom class names', () => {
    const { container } = render(<Skeleton className="w-10 h-10" />)
    expect(container.firstChild).toHaveClass('w-10')
    expect(container.firstChild).toHaveClass('h-10')
  })
})
