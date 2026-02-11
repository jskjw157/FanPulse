import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import LiveMetadata from './LiveMetadata';
import { mockLiveDetail } from '@/__mocks__/live';

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

    expect(screen.getByText(/How Sweet/)).toBeInTheDocument();
  });

  it('renders formatted viewer count', () => {
    render(<LiveMetadata live={mockLiveDetail} />);

    expect(screen.getByText(/24,583명 시청 중/)).toBeInTheDocument();
  });

  it('renders LIVE badge for live stream', () => {
    render(<LiveMetadata live={mockLiveDetail} />);

    expect(screen.getByText(/LIVE/)).toBeInTheDocument();
  });
});
