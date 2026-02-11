import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import LiveListItem from './LiveListItem';
import { mockLiveNow, mockUpcoming } from '@/__mocks__/live';

describe('LiveListItem', () => {
  it('renders thumbnail, title, and artist name', () => {
    render(<LiveListItem live={mockLiveNow[0]} />);

    expect(screen.getByAltText('NewJeans 컴백 쇼케이스')).toBeInTheDocument();
    expect(screen.getByText('NewJeans 컴백 쇼케이스')).toBeInTheDocument();
    expect(screen.getByText('NewJeans Official')).toBeInTheDocument();
  });

  it('renders LIVE badge and viewer count for live stream', () => {
    render(<LiveListItem live={mockLiveNow[0]} />);

    expect(screen.getByText(/LIVE/)).toBeInTheDocument();
    expect(screen.getByText('24,583명')).toBeInTheDocument();
  });

  it('renders SCHEDULED badge for upcoming stream', () => {
    render(<LiveListItem live={mockUpcoming[0]} />);

    expect(screen.getByText('예정')).toBeInTheDocument();
    expect(screen.queryByText(/명$/)).not.toBeInTheDocument();
  });

  it('links to /live/:id', () => {
    render(<LiveListItem live={mockLiveNow[0]} />);

    const link = screen.getByRole('link');
    expect(link).toHaveAttribute('href', '/live/1');
  });
});
