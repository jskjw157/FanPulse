import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, vi } from 'vitest';
import LatestNewsSection from './LatestNewsSection';
import { mockLatestNews } from '@/__mocks__/news';

describe('LatestNewsSection', () => {
  it('renders section title', () => {
    render(<LatestNewsSection news={mockLatestNews} state="success" />);
    expect(screen.getByText('최신 뉴스')).toBeInTheDocument();
  });

  it('renders skeleton cards when loading', () => {
    render(<LatestNewsSection news={[]} state="loading" />);
    const skeletons = screen.getAllByTestId('skeleton-card');
    expect(skeletons.length).toBeGreaterThanOrEqual(3);
  });

  it('renders news cards on success', () => {
    render(<LatestNewsSection news={mockLatestNews} state="success" />);
    expect(screen.getByText('BTS 새 앨범 발매 예정')).toBeInTheDocument();
    expect(screen.getByText('BLACKPINK 월드투어 추가 공연')).toBeInTheDocument();
  });

  it('renders error message with retry button on error', async () => {
    const onRetry = vi.fn();
    render(<LatestNewsSection news={[]} state="error" error="뉴스를 불러올 수 없습니다" onRetry={onRetry} />);
    expect(screen.getByText('뉴스를 불러올 수 없습니다')).toBeInTheDocument();

    const retryButton = screen.getByText('다시 시도');
    await userEvent.click(retryButton);
    expect(onRetry).toHaveBeenCalledOnce();
  });

  it('renders empty message when no news', () => {
    render(<LatestNewsSection news={[]} state="success" />);
    expect(screen.getByText('최신 뉴스가 없습니다')).toBeInTheDocument();
  });
});
