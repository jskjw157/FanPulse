import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import LiveMetadata from './LiveMetadata';
import type { LiveDetail } from '@/types/live';

const mockLiveDetail: LiveDetail = {
  id: 1,
  title: 'NewJeans 컴백 쇼케이스',
  artistName: 'NewJeans Official',
  thumbnailUrl: '/images/mock/live-1.jpg',
  status: 'LIVE',
  description: 'NewJeans의 새 앨범 컴백 쇼케이스 라이브 방송입니다.',
  streamUrl: 'https://www.youtube.com/embed/dQw4w9WgXcQ',
  scheduledAt: '2026-02-01T14:00:00Z',
  startedAt: '2026-02-01T14:00:00Z',
  viewerCount: 24583,
};

describe('LiveMetadata', () => {
  it('renders title', () => {
    render(<LiveMetadata live={mockLiveDetail} />);

    expect(screen.getByText('NewJeans 컴백 쇼케이스')).toBeInTheDocument();
  });

  it('renders artist name', () => {
    render(<LiveMetadata live={mockLiveDetail} />);

    expect(screen.getByText('NewJeans Official')).toBeInTheDocument();
  });

  it('renders description', () => {
    render(<LiveMetadata live={mockLiveDetail} />);

    expect(
      screen.getByText('NewJeans의 새 앨범 컴백 쇼케이스 라이브 방송입니다.')
    ).toBeInTheDocument();
  });

  it('formats viewer count with commas', () => {
    render(<LiveMetadata live={mockLiveDetail} />);

    expect(screen.getByText(/24,583/)).toBeInTheDocument();
  });

  it('shows viewer count label for LIVE status', () => {
    render(<LiveMetadata live={mockLiveDetail} />);

    expect(screen.getByText(/시청 중/)).toBeInTheDocument();
  });

  it('does not show viewer count when undefined', () => {
    const liveWithoutViewers = { ...mockLiveDetail, viewerCount: undefined };
    render(<LiveMetadata live={liveWithoutViewers} />);

    expect(screen.queryByText(/시청 중/)).not.toBeInTheDocument();
  });
});
