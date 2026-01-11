import { render, screen, fireEvent } from '@testing-library/react'
import { vi } from 'vitest'
import PageHeader from './PageHeader'

// Mock useRouter
const backMock = vi.fn()
vi.mock('next/navigation', () => ({
  useRouter: () => ({
    back: backMock,
  }),
}))

describe('PageHeader', () => {
  it('renders title correctly', () => {
    render(<PageHeader title="Test Title" />)
    expect(screen.getByText('Test Title')).toBeInTheDocument()
  })

  it('renders back button by default and handles click', () => {
    render(<PageHeader title="Test Title" />)
    const backButton = screen.getByRole('button')
    fireEvent.click(backButton)
    expect(backMock).toHaveBeenCalled()
  })

  it('hides back button when showBack is false', () => {
    render(<PageHeader title="Test Title" showBack={false} />)
    const backButton = screen.queryByRole('button')
    expect(backButton).not.toBeInTheDocument()
  })

  it('renders right action if provided', () => {
    render(<PageHeader title="Test Title" rightAction={<div data-testid="right-action">Action</div>} />)
    expect(screen.getByTestId('right-action')).toBeInTheDocument()
  })
})
