import { StrictMode } from 'react'
import { act, cleanup, fireEvent, render, screen, waitFor, within } from '@testing-library/react'
import { vi } from 'vitest'

const { router, refreshAuth, loginWithGoogle } = vi.hoisted(() => ({
  router: { push: vi.fn() },
  refreshAuth: vi.fn(),
  loginWithGoogle: vi.fn(),
}))

vi.mock('next/navigation', () => ({ useRouter: () => router }))
vi.mock('@/contexts/AuthContext', () => ({
  useAuth: () => ({ refreshAuth }),
}))
vi.mock('@/lib/auth', async (importOriginal) => ({
  ...await importOriginal<typeof import('@/lib/auth')>(),
  loginWithGoogle,
}))

import LoginPage from './page'

const scriptSelector = 'script[src="https://accounts.google.com/gsi/client"]'
const retryName = 'Google 로그인 다시 시도'
type Credential = { credential?: string }

function getScript() {
  const script = document.querySelector<HTMLScriptElement>(scriptSelector)
  expect(script).not.toBeNull()
  return script!
}

async function settleScript(script: HTMLScriptElement, event: 'load' | 'error') {
  await act(async () => {
    script.dispatchEvent(new Event(event))
  })
}

function installSdk() {
  const initialize = vi.fn<(config: {
    client_id: string
    callback: (response: Credential) => void
  }) => void>()
  const renderButton = vi.fn((container: HTMLElement) => {
    const button = document.createElement('button')
    button.textContent = 'Google SDK button'
    container.appendChild(button)
  })
  window.google = { accounts: { id: { initialize, renderButton } } }
  return {
    initialize,
    renderButton,
    credential: (response: Credential) => initialize.mock.calls[0][0].callback(response),
  }
}

describe('LoginPage Google SDK recovery', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.stubEnv('NEXT_PUBLIC_GOOGLE_CLIENT_ID', 'unit-test-client')
    loginWithGoogle.mockReset().mockResolvedValue({})
    refreshAuth.mockReset().mockResolvedValue(undefined)
    window.history.replaceState({}, '', '/login')
    delete window.google
  })

  afterEach(async () => {
    cleanup()
    // Settle pending shared work even when an assertion fails, without real requests.
    for (const script of document.querySelectorAll<HTMLScriptElement>(scriptSelector)) {
      await settleScript(script, 'error')
      script.remove()
    }
    delete window.google
    window.history.replaceState({}, '', '/')
    vi.useRealTimers()
    vi.unstubAllEnvs()
    vi.restoreAllMocks()
  })

  it.each(['error', 'load'] as const)(
    'recovers through the retry button after %s without a usable SDK',
    async (failureEvent) => {
      render(<LoginPage />)
      const failedScript = getScript()
      const removeListener = vi.spyOn(failedScript, 'removeEventListener')
      await settleScript(failedScript, failureEvent)

      expect(failedScript.isConnected).toBe(false)
      expect(removeListener).toHaveBeenCalledWith('load', expect.any(Function))
      expect(removeListener).toHaveBeenCalledWith('error', expect.any(Function))
      expect(screen.getByRole('alert')).toHaveTextContent('Google 로그인을 불러오지 못했습니다.')
      expect(loginWithGoogle).not.toHaveBeenCalled()
      expect(router.push).not.toHaveBeenCalled()

      const retry = screen.getByRole('button', { name: retryName })
      fireEvent.click(retry)
      fireEvent.click(retry)
      const replacement = getScript()
      expect(replacement).not.toBe(failedScript)
      expect(document.querySelectorAll(scriptSelector)).toHaveLength(1)
      expect(screen.queryByRole('alert')).not.toBeInTheDocument()
      expect(screen.queryByRole('button', { name: retryName })).not.toBeInTheDocument()

      const sdk = installSdk()
      await settleScript(replacement, 'load')
      // Late events from the removed script must not poison a successful retry.
      await settleScript(failedScript, 'error')
      expect(sdk.initialize).toHaveBeenCalledTimes(1)
      expect(sdk.initialize).toHaveBeenCalledWith({
        client_id: 'unit-test-client', callback: expect.any(Function),
      })
      expect(sdk.renderButton).toHaveBeenCalledTimes(1)
      expect(screen.getByRole('button', { name: 'Google SDK button' })).toBeInTheDocument()
      expect(screen.queryByRole('alert')).not.toBeInTheDocument()
      expect(loginWithGoogle).not.toHaveBeenCalled()

      await act(async () => { sdk.credential({ credential: 'synthetic-id-token' }) })
      await waitFor(() => expect(router.push).toHaveBeenCalledWith('/'))
      expect(loginWithGoogle).toHaveBeenCalledExactlyOnceWith({ idToken: 'synthetic-id-token' })
      expect(refreshAuth).toHaveBeenCalledTimes(1)
    },
  )

  it('recovers on SPA remount after failure when the SDK becomes available', async () => {
    const first = render(<LoginPage />)
    await settleScript(getScript(), 'error')
    expect(screen.getByRole('button', { name: retryName })).toBeInTheDocument()
    first.unmount()

    const sdk = installSdk()
    render(<LoginPage />)
    await waitFor(() => expect(sdk.renderButton).toHaveBeenCalledTimes(1))
    expect(sdk.initialize).toHaveBeenCalledTimes(1)
    expect(document.querySelector(scriptSelector)).toBeNull()
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: retryName })).not.toBeInTheDocument()
  })

  it('creates a fresh script on SPA remount after failure if the SDK is still absent', async () => {
    const first = render(<LoginPage />)
    const failedScript = getScript()
    await settleScript(failedScript, 'error')
    first.unmount()

    render(<LoginPage />)
    const replacement = getScript()
    expect(replacement).not.toBe(failedScript)
    const sdk = installSdk()
    await settleScript(replacement, 'load')
    expect(sdk.initialize).toHaveBeenCalledTimes(1)
    expect(sdk.renderButton).toHaveBeenCalledTimes(1)
  })

  it('handles repeated failures and retries without caching a rejection', async () => {
    render(<LoginPage />)
    await settleScript(getScript(), 'error')
    fireEvent.click(screen.getByRole('button', { name: retryName }))
    await settleScript(getScript(), 'error')
    fireEvent.click(screen.getByRole('button', { name: retryName }))

    const sdk = installSdk()
    await settleScript(getScript(), 'load')
    expect(sdk.initialize).toHaveBeenCalledTimes(1)
    expect(sdk.renderButton).toHaveBeenCalledTimes(1)
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
  })

  it('waits for a pre-existing pending script rather than treating its presence as success', async () => {
    const script = document.createElement('script')
    script.src = 'https://accounts.google.com/gsi/client'
    document.head.appendChild(script)
    render(<LoginPage />)
    await act(async () => {})
    expect(document.querySelectorAll(scriptSelector)).toHaveLength(1)
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: retryName })).not.toBeInTheDocument()

    const sdk = installSdk()
    await settleScript(script, 'load')
    expect(sdk.initialize).toHaveBeenCalledTimes(1)
    expect(sdk.renderButton).toHaveBeenCalledTimes(1)
  })

  it('times out an already-failed or stalled existing script and permits retry', async () => {
    vi.useFakeTimers()
    const script = document.createElement('script')
    script.src = 'https://accounts.google.com/gsi/client'
    document.head.appendChild(script)
    render(<LoginPage />)
    await act(async () => { await vi.advanceTimersByTimeAsync(15_000) })
    expect(script.isConnected).toBe(false)
    expect(vi.getTimerCount()).toBe(0)

    fireEvent.click(screen.getByRole('button', { name: retryName }))
    const sdk = installSdk()
    await settleScript(getScript(), 'load')
    expect(sdk.initialize).toHaveBeenCalledTimes(1)
    expect(sdk.renderButton).toHaveBeenCalledTimes(1)
    expect(vi.getTimerCount()).toBe(0)
  })

  it('loads and initializes only once in Strict Mode and does not duplicate button markup', async () => {
    const page = render(<StrictMode><LoginPage /></StrictMode>)
    expect(document.querySelectorAll(scriptSelector)).toHaveLength(1)
    const script = getScript()
    const removeListener = vi.spyOn(script, 'removeEventListener')
    const sdk = installSdk()
    await settleScript(script, 'load')
    page.rerender(<StrictMode><LoginPage /></StrictMode>)

    expect(sdk.initialize).toHaveBeenCalledTimes(1)
    expect(sdk.renderButton).toHaveBeenCalledTimes(1)
    expect(screen.getAllByRole('button', { name: 'Google SDK button' })).toHaveLength(1)
    expect(removeListener).toHaveBeenCalledWith('load', expect.any(Function))
    expect(removeListener).toHaveBeenCalledWith('error', expect.any(Function))
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
    const container = sdk.renderButton.mock.calls[0][0]
    page.unmount()
    expect(container).toBeEmptyDOMElement()
    await act(async () => { sdk.credential({ credential: 'synthetic-id-token' }) })
    expect(loginWithGoogle).not.toHaveBeenCalled()
  })

  it('shares an in-flight script across unmount/remount and initializes only the live mount', async () => {
    const first = render(<LoginPage />)
    const script = getScript()
    first.unmount()
    const second = render(<LoginPage />)
    expect(getScript()).toBe(script)

    const sdk = installSdk()
    await settleScript(script, 'load')
    expect(sdk.initialize).toHaveBeenCalledTimes(1)
    expect(sdk.renderButton).toHaveBeenCalledTimes(1)
    expect(second.container).toContainElement(sdk.renderButton.mock.calls[0][0])
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
  })

  it('shares concurrent success and routes credentials to only the latest live mount', async () => {
    const first = render(<LoginPage />)
    const second = render(<LoginPage />)
    expect(document.querySelectorAll(scriptSelector)).toHaveLength(1)
    const sdk = installSdk()
    await settleScript(getScript(), 'load')
    expect(sdk.initialize).toHaveBeenCalledTimes(1)
    expect(sdk.renderButton).toHaveBeenCalledTimes(2)

    await act(async () => { sdk.credential({}) })
    expect(within(first.container).queryByRole('alert')).not.toBeInTheDocument()
    expect(within(second.container).getByRole('alert')).toHaveTextContent('Google 로그인 토큰을 받지 못했습니다.')
    expect(loginWithGoogle).not.toHaveBeenCalled()

    second.unmount()
    await act(async () => { sdk.credential({ credential: 'synthetic-id-token' }) })
    await waitFor(() => expect(router.push).toHaveBeenCalledTimes(1))
    expect(loginWithGoogle).toHaveBeenCalledTimes(1)
    first.unmount()
    await act(async () => { sdk.credential({ credential: 'stale-synthetic-token' }) })
    expect(loginWithGoogle).toHaveBeenCalledTimes(1)
    expect(sdk.initialize).toHaveBeenCalledTimes(1)
  })

  it('reuses an already-ready SDK across successful SPA mounts without another initialize', async () => {
    const sdk = installSdk()
    const first = render(<LoginPage />)
    await waitFor(() => expect(sdk.renderButton).toHaveBeenCalledTimes(1))
    first.unmount()
    render(<LoginPage />)
    await waitFor(() => expect(sdk.renderButton).toHaveBeenCalledTimes(2))

    expect(sdk.initialize).toHaveBeenCalledTimes(1)
    expect(document.querySelector(scriptSelector)).toBeNull()
    await act(async () => { sdk.credential({ credential: 'synthetic-id-token' }) })
    await waitFor(() => expect(router.push).toHaveBeenCalledTimes(1))
    expect(loginWithGoogle).toHaveBeenCalledTimes(1)
  })

  it('deduplicates credential submissions and ignores completion after unmount', async () => {
    let finishLogin!: () => void
    loginWithGoogle.mockReturnValue(new Promise<void>((resolve) => { finishLogin = resolve }))
    const sdk = installSdk()
    const page = render(<LoginPage />)
    await waitFor(() => expect(sdk.renderButton).toHaveBeenCalledTimes(1))
    await act(async () => {
      sdk.credential({ credential: 'synthetic-id-token' })
      sdk.credential({ credential: 'duplicate-synthetic-token' })
    })
    expect(loginWithGoogle).toHaveBeenCalledTimes(1)
    expect(screen.getByText('처리 중...')).toBeInTheDocument()

    page.unmount()
    await act(async () => { finishLogin() })
    expect(refreshAuth).not.toHaveBeenCalled()
    expect(router.push).not.toHaveBeenCalled()
  })

  it('does not navigate if unmounted while authentication refresh is pending', async () => {
    let finishRefresh!: () => void
    refreshAuth.mockReturnValue(new Promise<void>((resolve) => { finishRefresh = resolve }))
    const sdk = installSdk()
    const page = render(<LoginPage />)
    await waitFor(() => expect(sdk.renderButton).toHaveBeenCalledTimes(1))
    await act(async () => { sdk.credential({ credential: 'synthetic-id-token' }) })
    expect(refreshAuth).toHaveBeenCalledTimes(1)

    page.unmount()
    await act(async () => { finishRefresh() })
    expect(router.push).not.toHaveBeenCalled()
  })

  it('preserves backend error handling without offering a loader retry or bypassing auth', async () => {
    loginWithGoogle.mockRejectedValueOnce(new Error('Synthetic authentication failure'))
    const sdk = installSdk()
    render(<LoginPage />)
    await waitFor(() => expect(sdk.renderButton).toHaveBeenCalledTimes(1))
    await act(async () => { sdk.credential({ credential: 'synthetic-id-token' }) })

    expect(screen.getByRole('alert')).toHaveTextContent('Synthetic authentication failure')
    expect(screen.queryByRole('button', { name: retryName })).not.toBeInTheDocument()
    expect(refreshAuth).not.toHaveBeenCalled()
    expect(router.push).not.toHaveBeenCalled()
    expect(sdk.initialize).toHaveBeenCalledTimes(1)
  })
})
