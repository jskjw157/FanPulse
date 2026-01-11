import { render, screen } from '@testing-library/react'
import LoginPage from './page'
import { vi } from 'vitest'

// Mock useRouter
vi.mock('next/navigation', () => ({
  useRouter: () => ({
    push: vi.fn(),
  }),
}))

describe('LoginPage', () => {
  it('renders login form', () => {
    render(<LoginPage />)
    expect(screen.getByPlaceholderText(/email/i)).toBeInTheDocument()
    expect(screen.getByPlaceholderText(/password/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument()
  })

  it('renders social login options', () => {
    render(<LoginPage />)
    expect(screen.getByText(/continue with google/i)).toBeInTheDocument()
  })
})
