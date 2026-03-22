import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { EmptyState } from './EmptyState';

describe('EmptyState', () => {
  it('renders default message', () => {
    render(<EmptyState />);
    expect(screen.getByText('검색 결과가 없습니다')).toBeInTheDocument();
  });

  it('renders custom message', () => {
    render(<EmptyState message="다른 검색어를 시도해보세요" />);
    expect(
      screen.getByText('다른 검색어를 시도해보세요')
    ).toBeInTheDocument();
  });

  it('renders search icon', () => {
    render(<EmptyState />);
    expect(screen.getByTestId('empty-icon')).toBeInTheDocument();
  });
});
