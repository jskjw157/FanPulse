import { act, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { vi } from 'vitest'
import { SAFE_LOGIN_RETURNS, UNSAFE_LOGIN_RETURNS } from '@/lib/login-redirect.fixtures'

const { pushMock } = vi.hoisted(() => ({
  pushMock: vi.fn(),
}))

vi.mock('next/navigation', () => ({
  useRouter: () => ({
    push: pushMock,
  }),
}))

vi.mock('@/contexts/AuthContext', () => ({
  useAuth: () => ({ refreshAuth: vi.fn() }),
}))

vi.mock('@/lib/auth', async (importOriginal) => ({
  ...await importOriginal<typeof import('@/lib/auth')>(),
  loginWithGoogle: vi.fn().mockResolvedValue({}),
}))

import LoginPage from './page'

describe('LoginPage', () => {
  beforeEach(() => {
    pushMock.mockClear()
    vi.stubEnv('NEXT_PUBLIC_GOOGLE_CLIENT_ID', '')
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

  it('does not expose an authentication bypass', () => {
    render(<LoginPage />)
    expect(screen.queryByText('둘러보기')).not.toBeInTheDocument()
    expect(pushMock).not.toHaveBeenCalled()
  })

  it.each([
    ...SAFE_LOGIN_RETURNS,
    ...UNSAFE_LOGIN_RETURNS.map((input): [string, string] => [input, '/']),
    ['', '/'],
  ])('returns only to a safe supported destination after login: %s', async (redirect, expected) => {
    vi.stubEnv('NEXT_PUBLIC_GOOGLE_CLIENT_ID', 'unit-test-client')
    window.history.replaceState({}, '', `/login?redirect=${encodeURIComponent(redirect)}`)
    const script = document.createElement('script')
    script.src = 'https://accounts.google.com/gsi/client'
    document.head.appendChild(script)
    let callback!: (response: { credential?: string }) => void
    const initialize = vi.fn((config: { callback: typeof callback }) => { callback = config.callback })
    window.google = { accounts: { id: { initialize, renderButton: vi.fn() } } }
    try {
      render(<LoginPage />)
      await waitFor(() => expect(initialize).toHaveBeenCalled())
      await act(async () => { await callback({ credential: 'test-token' }) })
      expect(pushMock).toHaveBeenCalledWith(expected)
    } finally {
      script.remove()
      delete window.google
      window.history.replaceState({}, '', '/')
      vi.unstubAllEnvs()
    }
  })
})
