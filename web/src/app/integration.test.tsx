import { render, screen } from '@testing-library/react'
import { vi } from 'vitest'
import LoginPage from './login/page'
import MyPage from './mypage/page'
import SettingsPage from './settings/page'
import FavoritesPage from './favorites/page'
import SavedPage from './saved/page'
import TicketsPage from './tickets/page'
import SearchPage from './search/page'
import NotificationsPage from './notifications/page'
import SupportPage from './support/page'
import ErrorPage from './error/page'
import ConcertDetailPage from './concert-detail/page'
import NoticeDetailPage from './notice-detail/page'
import PostCreatePage from './post-create/page'

// Mock dependencies
vi.mock('next/navigation', () => ({
  useRouter: () => ({
    push: vi.fn(),
    back: vi.fn(),
  }),
  usePathname: () => '/',
  useSearchParams: () => ({
    get: () => '1',
  }),
}))

describe('Smoke Test - All Pages Render', () => {
  it('renders Login Page', () => {
    render(<LoginPage />)
    expect(screen.getByText('Welcome to FanPulse')).toBeInTheDocument()
  })

  it('renders My Page', () => {
    render(<MyPage />)
    expect(screen.getByText('마이페이지')).toBeInTheDocument()
  })

  it('renders Settings Page', () => {
    render(<SettingsPage />)
    expect(screen.getByText('설정')).toBeInTheDocument()
  })

  it('renders Favorites Page', () => {
    render(<FavoritesPage />)
    expect(screen.getByText('좋아요한 아티스트')).toBeInTheDocument()
  })

  it('renders Saved Page', () => {
    render(<SavedPage />)
    expect(
      screen.getByRole('heading', { name: '저장한 게시물' }),
    ).toBeInTheDocument()
  })

  it('renders Tickets Page', () => {
    render(<TicketsPage />)
    expect(screen.getByText('예매 내역')).toBeInTheDocument()
  })

  it('renders Search Page', () => {
    render(<SearchPage />)
    expect(screen.getByPlaceholderText(/아티스트, 게시글, 뉴스 검색.../i)).toBeInTheDocument()
  })

  it('renders Notifications Page', () => {
    render(<NotificationsPage />)
    expect(screen.getByText('알림')).toBeInTheDocument()
  })

  it('renders Support Page', () => {
    render(<SupportPage />)
    expect(screen.getByText('고객센터')).toBeInTheDocument()
  })

  it('renders Error Page', () => {
    render(<ErrorPage />)
    expect(screen.getByText('Oops!')).toBeInTheDocument()
  })

  it('renders Concert Detail Page', () => {
    render(<ConcertDetailPage />)
    expect(screen.getByText('공연 상세')).toBeInTheDocument()
  })

  it('renders Notice Detail Page', () => {
    render(<NoticeDetailPage />)
    expect(screen.getByText('공지사항')).toBeInTheDocument()
  })

  it('renders Post Create Page', () => {
    render(<PostCreatePage />)
    expect(screen.getByText('게시글 작성')).toBeInTheDocument()
  })
})
