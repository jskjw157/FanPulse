import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, vi } from 'vitest';
import { RecentSearches } from './RecentSearches';

describe('RecentSearches', () => {
  const defaultProps = {
    searches: ['BTS', 'BLACKPINK', '콘서트'],
    onSelect: vi.fn(),
    onRemove: vi.fn(),
    onClearAll: vi.fn(),
  };

  it('renders search chips', () => {
    render(<RecentSearches {...defaultProps} />);

    expect(screen.getByText('BTS')).toBeInTheDocument();
    expect(screen.getByText('BLACKPINK')).toBeInTheDocument();
    expect(screen.getByText('콘서트')).toBeInTheDocument();
  });

  it('renders section header', () => {
    render(<RecentSearches {...defaultProps} />);
    expect(screen.getByText('최근 검색어')).toBeInTheDocument();
  });

  it('calls onSelect when chip clicked', async () => {
    const onSelect = vi.fn();
    render(<RecentSearches {...defaultProps} onSelect={onSelect} />);

    await userEvent.click(screen.getByText('BTS'));
    expect(onSelect).toHaveBeenCalledWith('BTS');
  });

  it('calls onRemove when X button clicked', async () => {
    const onRemove = vi.fn();
    render(<RecentSearches {...defaultProps} onRemove={onRemove} />);

    const removeButton = screen.getByRole('button', { name: 'BTS 삭제' });
    await userEvent.click(removeButton);

    expect(onRemove).toHaveBeenCalledWith('BTS');
  });

  it('calls onClearAll when clear all button clicked', async () => {
    const onClearAll = vi.fn();
    render(<RecentSearches {...defaultProps} onClearAll={onClearAll} />);

    await userEvent.click(screen.getByText('전체 삭제'));
    expect(onClearAll).toHaveBeenCalled();
  });

  it('does not render when searches is empty', () => {
    render(<RecentSearches {...defaultProps} searches={[]} />);
    expect(screen.queryByText('최근 검색어')).not.toBeInTheDocument();
  });
});
