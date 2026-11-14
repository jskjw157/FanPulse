import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import MobileMenu from './MobileMenu';

describe('MobileMenu news navigation', () => {
  it('links News to the list, not the legacy detail route without an ID', () => {
    render(<MobileMenu isOpen onClose={vi.fn()} />);

    expect(screen.getByRole('link', { name: '뉴스' })).toHaveAttribute('href', '/news');
    expect(screen.getByRole('link', { name: '차트' })).toHaveAttribute('href', '/chart');
    expect(screen.getByRole('link', { name: '설정' })).toHaveAttribute('href', '/settings');
  });

  it('does not expose menu navigation while closed', () => {
    render(<MobileMenu isOpen={false} onClose={vi.fn()} />);

    expect(screen.queryByRole('link', { name: '뉴스' })).not.toBeInTheDocument();
  });
});
