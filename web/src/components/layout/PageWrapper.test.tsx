import { render, screen } from '@testing-library/react'
import PageWrapper from './PageWrapper'

describe('PageWrapper', () => {
  it('renders children correctly', () => {
    render(
      <PageWrapper>
        <div data-testid="child">Child Content</div>
      </PageWrapper>
    )
    expect(screen.getByTestId('child')).toBeInTheDocument()
  })

  it('applies padding for header and bottom nav by default', () => {
    const { container } = render(<PageWrapper>Content</PageWrapper>)
    expect(container.firstChild).toHaveClass('pt-16')
    expect(container.firstChild).toHaveClass('pb-20')
  })

  it('removes padding when props are false', () => {
    const { container } = render(
      <PageWrapper hasHeader={false} hasBottomNav={false}>
        Content
      </PageWrapper>
    )
    expect(container.firstChild).not.toHaveClass('pt-16')
    expect(container.firstChild).not.toHaveClass('pb-20')
  })
})
