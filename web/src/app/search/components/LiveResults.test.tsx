import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { LiveResults } from './LiveResults';
import type { Live } from '@/types/live';

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

vi.mock('@/app/live/components/StatusBadge', () => ({
  default: ({ status }: { status: string }) => <span data-testid="status-badge">{status}</span>,
}));

vi.mock('@/lib/utils/format', () => ({
  formatViewerCount: (count: number) => count.toLocaleString(),
}));

const mockLives: Live[] = [
  {
    id: 1,
    title: 'BTS Fan Meeting Special',
    artistName: 'BTS',
    thumbnailUrl: '/images/mock/live-bts.jpg',
    status: 'LIVE',
    viewerCount: 89200,
  },
  {
    id: 10,
    title: 'BTS World Tour Highlights',
    artistName: 'BTS',
    thumbnailUrl: '/images/mock/live-bts-2.jpg',
    status: 'ENDED',
    viewerCount: 150000,
  },
];

describe('LiveResults', () => {
  it('renders section header', () => {
    render(<LiveResults lives={mockLives} />);
    expect(screen.getByText('라이브')).toBeInTheDocument();
  });

  it('renders live items', () => {
    render(<LiveResults lives={mockLives} />);
    expect(screen.getByText('BTS Fan Meeting Special')).toBeInTheDocument();
    expect(screen.getByText('BTS World Tour Highlights')).toBeInTheDocument();
  });

  it('renders artist names', () => {
    render(<LiveResults lives={mockLives} />);
    expect(screen.getAllByText('BTS')).toHaveLength(2);
  });

  it('links to live detail page', () => {
    render(<LiveResults lives={mockLives} />);
    const links = screen.getAllByRole('link');
    expect(links[0]).toHaveAttribute('href', '/live/1');
    expect(links[1]).toHaveAttribute('href', '/live/10');
  });

  it('does not render when lives is empty', () => {
    render(<LiveResults lives={[]} />);
    expect(screen.queryByText('라이브')).not.toBeInTheDocument();
  });
});
