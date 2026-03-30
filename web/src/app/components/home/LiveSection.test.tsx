import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, vi } from 'vitest';
import LiveSection from './LiveSection';
import { mockLiveNow } from '@/__mocks__/live';

describe('LiveSection', () => {
  it('renders section title', () => {
    render(<LiveSection title="Live Now" lives={mockLiveNow} state="success" />);
    expect(screen.getByText('Live Now')).toBeInTheDocument();
  });

  it('renders skeleton cards when loading', () => {
    render(<LiveSection title="Live Now" lives={[]} state="loading" />);
    const skeletons = screen.getAllByTestId('skeleton-card');
    expect(skeletons.length).toBeGreaterThanOrEqual(3);
  });

  it('renders live cards on success', () => {
    render(<LiveSection title="Live Now" lives={mockLiveNow} state="success" />);
    expect(screen.getByText('NewJeans 컴백 쇼케이스')).toBeInTheDocument();
    expect(screen.getByText('BTS Fan Meeting Special')).toBeInTheDocument();
  });

  it('renders error message with retry button on error', async () => {
    const onRetry = vi.fn();
    render(<LiveSection title="Live Now" lives={[]} state="error" error="네트워크 오류" onRetry={onRetry} />);
    expect(screen.getByText('네트워크 오류')).toBeInTheDocument();

    const retryButton = screen.getByText('다시 시도');
    await userEvent.click(retryButton);
    expect(onRetry).toHaveBeenCalledOnce();
  });

  it('renders empty message when no lives', () => {
    render(<LiveSection title="Live Now" lives={[]} state="success" />);
    expect(screen.getByText('현재 라이브 방송이 없습니다')).toBeInTheDocument();
  });
});
