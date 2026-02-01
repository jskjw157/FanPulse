import { fireEvent, render, screen } from '@testing-library/react'
import { vi } from 'vitest'

const { pushMock } = vi.hoisted(() => ({
  pushMock: vi.fn(),
}))

vi.mock('next/navigation', () => ({
  useRouter: () => ({
    push: pushMock,
  }),
}))

import LoginPage from './page'

describe('LoginPage', () => {
  beforeEach(() => {
    pushMock.mockClear()
  })

  it('renders Google-only login UI', () => {
    render(<LoginPage />)

    expect(screen.getByText('Welcome to FanPulse')).toBeInTheDocument()
    expect(screen.getByText('Google로 로그인')).toBeInTheDocument()

    expect(screen.queryByPlaceholderText('이메일')).not.toBeInTheDocument()
    expect(screen.queryByPlaceholderText('비밀번호')).not.toBeInTheDocument()
    expect(
      screen.queryByRole('button', { name: '회원가입 탭' }),
    ).not.toBeInTheDocument()
  })

  it('shows helpful message when Google Client ID is missing', () => {
    render(<LoginPage />)
    fireEvent.click(screen.getByText('Google로 로그인'))

    expect(
      screen.getByText(
        'Google 로그인 설정이 필요합니다. NEXT_PUBLIC_GOOGLE_CLIENT_ID를 확인해주세요.',
      ),
    ).toBeInTheDocument()
  })

  it('navigates to home on browse', () => {
    render(<LoginPage />)
    fireEvent.click(screen.getByText('둘러보기'))
    expect(pushMock).toHaveBeenCalledWith('/')
  })
})
