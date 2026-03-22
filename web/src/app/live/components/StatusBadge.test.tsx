import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import StatusBadge from './StatusBadge';

describe('StatusBadge', () => {
  it('renders LIVE badge with red color and pulse animation', () => {
    render(<StatusBadge status="LIVE" />);

    const badge = screen.getByText(/LIVE/);
    expect(badge).toBeInTheDocument();
    expect(badge.className).toContain('bg-red');
    expect(badge.querySelector('.animate-pulse')).toBeInTheDocument();
  });

  it('renders SCHEDULED badge with gray color', () => {
    render(<StatusBadge status="SCHEDULED" />);

    const badge = screen.getByText('예정');
    expect(badge).toBeInTheDocument();
    expect(badge.className).toContain('bg-gray');
  });

  it('renders ENDED badge with dark color', () => {
    render(<StatusBadge status="ENDED" />);

    const badge = screen.getByText('종료');
    expect(badge).toBeInTheDocument();
    expect(badge.className).toContain('bg-gray-800');
  });
});
