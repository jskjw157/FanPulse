import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import NewsCard from './NewsCard';
import type { News } from '@/types/news';

const mockNews: News = {
  id: 1,
  title: 'BTS 새 앨범 발매 예정',
  summary: 'BTS가 2026년 3월 새 앨범 발매를 예고했다. 멤버들의 솔로 활동 이후 첫 완전체 앨범이다.',
  thumbnailUrl: '/images/mock/news-1.jpg',
  source: '스포츠조선',
  publishedAt: '2026-02-01T09:00:00Z',
};

describe('NewsCard', () => {
  it('renders thumbnail image', () => {
    render(<NewsCard news={mockNews} />);
    const img = screen.getByAltText(mockNews.title);
    expect(img).toBeInTheDocument();
  });

  it('renders title', () => {
    render(<NewsCard news={mockNews} />);
    expect(screen.getByText(mockNews.title)).toBeInTheDocument();
  });

  it('renders summary', () => {
    render(<NewsCard news={mockNews} />);
    expect(screen.getByText(mockNews.summary)).toBeInTheDocument();
  });

  it('renders source', () => {
    render(<NewsCard news={mockNews} />);
    expect(screen.getByText(/스포츠조선/)).toBeInTheDocument();
  });

  it('links to /news/:id', () => {
    render(<NewsCard news={mockNews} />);
    const link = screen.getByRole('link');
    expect(link).toHaveAttribute('href', '/news/1');
  });
});
