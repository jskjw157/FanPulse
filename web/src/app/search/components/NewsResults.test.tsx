import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { NewsResults } from './NewsResults';
import type { News } from '@/types/news';

vi.mock('next/link', () => ({
  default: ({ children, href }: { children: React.ReactNode; href: string }) => (
    <a href={href}>{children}</a>
  ),
}));

vi.mock('next/image', () => ({
  default: ({ src, alt }: { src: string; alt: string }) => (
    // eslint-disable-next-line @next/next/no-img-element
    <img src={src} alt={alt} />
  ),
}));

vi.mock('@/lib/utils/format', () => ({
  formatRelativeTime: () => '2시간 전',
}));

const mockNews: News[] = [
  {
    id: 1,
    title: 'BTS 새 앨범 발매 예정',
    summary: 'BTS가 2026년 3월 새 앨범 발매를 예고했다.',
    thumbnailUrl: '/images/mock/news-bts.jpg',
    source: '스포츠조선',
    publishedAt: '2026-02-01T09:00:00Z',
  },
  {
    id: 5,
    title: 'BTS 멤버 진 솔로 앨범 차트 1위',
    summary: 'BTS 진의 솔로 앨범이 빌보드 차트 1위를 기록했다.',
    thumbnailUrl: '/images/mock/news-bts-2.jpg',
    source: '한국경제',
    publishedAt: '2026-01-30T12:00:00Z',
  },
];

describe('NewsResults', () => {
  it('renders section header', () => {
    render(<NewsResults newsList={mockNews} />);
    expect(screen.getByText('뉴스')).toBeInTheDocument();
  });

  it('renders news items', () => {
    render(<NewsResults newsList={mockNews} />);
    expect(screen.getByText('BTS 새 앨범 발매 예정')).toBeInTheDocument();
    expect(
      screen.getByText('BTS 멤버 진 솔로 앨범 차트 1위')
    ).toBeInTheDocument();
  });

  it('renders source names', () => {
    render(<NewsResults newsList={mockNews} />);
    expect(screen.getByText(/스포츠조선/)).toBeInTheDocument();
    expect(screen.getByText(/한국경제/)).toBeInTheDocument();
  });

  it('links to news detail page', () => {
    render(<NewsResults newsList={mockNews} />);
    const links = screen.getAllByRole('link');
    expect(links[0]).toHaveAttribute('href', '/news/1');
    expect(links[1]).toHaveAttribute('href', '/news/5');
  });

  it('does not render when newsList is empty', () => {
    render(<NewsResults newsList={[]} />);
    expect(screen.queryByText('뉴스')).not.toBeInTheDocument();
  });
});
