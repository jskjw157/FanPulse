import { render, screen } from '@testing-library/react'
import { vi } from 'vitest'
import { mockLiveNow, mockUpcoming } from '@/__mocks__/live'
import { mockLatestNews } from '@/__mocks__/news'

vi.mock('@/hooks/useHomeSections', () => ({
  useHomeSections: () => ({
    liveNow: mockLiveNow,
    upcoming: mockUpcoming,
    latestNews: mockLatestNews,
    state: 'success' as const,
    error: null,
    refresh: vi.fn(),
  }),
}))

vi.mock('next/navigation', () => ({
  useRouter: () => ({ push: vi.fn() }),
}))

import Home from './page'

describe('Home Page Navigation', () => {
  it('renders live and news links when data is loaded', () => {
    render(<Home />)

    // Helper to check link existence by href prefix
    const checkLink = (href: string) => {
      const link = screen.getAllByRole('link').find(l => l.getAttribute('href')?.startsWith(href))
      expect(link).toBeInTheDocument()
    }

    // LiveCard links use /live/{id}
    checkLink('/live/')
    // NewsCard links use /news/{id}
    checkLink('/news/')
  })
})
