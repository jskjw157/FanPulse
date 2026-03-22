import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import LiveCard from './LiveCard';
import type { Live } from '@/types/live';

const mockLive: Live = {
  id: 1,
  title: 'NewJeans 컴백 쇼케이스',
  artistName: 'NewJeans Official',
  thumbnailUrl: '/images/mock/live-1.jpg',
  status: 'LIVE',
  viewerCount: 24583,
};

const mockScheduled: Live = {
  id: 10,
  title: 'SEVENTEEN Dance Practice',
  artistName: 'SEVENTEEN',
  thumbnailUrl: '/images/mock/upcoming-1.jpg',
  status: 'SCHEDULED',
  scheduledAt: '2026-02-15T14:00:00Z',
};

describe('LiveCard', () => {
  it('renders LIVE status badge', () => {
    render(<LiveCard live={mockLive} />);
    expect(screen.getByText('LIVE')).toBeInTheDocument();
  });

  it('renders thumbnail image', () => {
    render(<LiveCard live={mockLive} />);
    const img = screen.getByAltText(mockLive.title);
    expect(img).toBeInTheDocument();
  });

  it('renders title and artist name', () => {
    render(<LiveCard live={mockLive} />);
    expect(screen.getByText(mockLive.title)).toBeInTheDocument();
    expect(screen.getByText(mockLive.artistName)).toBeInTheDocument();
  });

  it('renders formatted viewer count for LIVE status', () => {
    render(<LiveCard live={mockLive} />);
    expect(screen.getByText('24,583명')).toBeInTheDocument();
  });

  it('renders SCHEDULED badge for upcoming live', () => {
    render(<LiveCard live={mockScheduled} />);
    expect(screen.getByText('예정')).toBeInTheDocument();
  });

  it('does not show viewer count for SCHEDULED status', () => {
    render(<LiveCard live={mockScheduled} />);
    expect(screen.queryByText(/명$/)).not.toBeInTheDocument();
  });

  it('links to /live/:id', () => {
    render(<LiveCard live={mockLive} />);
    const link = screen.getByRole('link');
    expect(link).toHaveAttribute('href', '/live/1');
  });
});
