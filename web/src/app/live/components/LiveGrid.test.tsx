import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import LiveGrid from './LiveGrid';
import { mockLiveList } from '@/__mocks__/live';

describe('LiveGrid', () => {
  it('renders loading skeletons when state is loading', () => {
    render(<LiveGrid lives={[]} state="loading" />);

    const skeletons = screen.getAllByTestId('skeleton-card');
    expect(skeletons.length).toBeGreaterThanOrEqual(6);
  });

  it('renders live items when state is success', () => {
    render(<LiveGrid lives={mockLiveList} state="success" />);

    expect(screen.getByText('NewJeans 컴백 쇼케이스')).toBeInTheDocument();
    expect(screen.getByText('BTS Fan Meeting Special')).toBeInTheDocument();
  });

  it('renders empty message when lives array is empty', () => {
    render(<LiveGrid lives={[]} state="success" />);

    expect(screen.getByText('라이브가 없습니다')).toBeInTheDocument();
  });

  it('renders error state with retry button', () => {
    const onRetry = vi.fn();
    render(<LiveGrid lives={[]} state="error" error="에러 발생" onRetry={onRetry} />);

    expect(screen.getByText('에러 발생')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '다시 시도' })).toBeInTheDocument();
  });
});
