import { render, screen } from '@testing-library/react'
import MembershipPage from './page'
import { vi } from 'vitest'

vi.mock('next/navigation', () => ({
  useRouter: () => ({
    back: vi.fn(),
  }),
}))

describe('MembershipPage', () => {
  it('renders page header', () => {
    render(<MembershipPage />)
    expect(screen.getByText('VIP Membership')).toBeInTheDocument()
  })

  it('renders benefit list skeleton', () => {
    render(<MembershipPage />)
    expect(screen.getByTestId('membership-benefits-skeleton')).toBeInTheDocument()
  })
})
