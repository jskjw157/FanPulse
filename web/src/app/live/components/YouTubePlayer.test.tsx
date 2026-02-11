import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import YouTubePlayer from './YouTubePlayer';

describe('YouTubePlayer', () => {
  const mockStreamUrl = 'https://www.youtube.com/embed/dQw4w9WgXcQ';

  it('renders iframe with correct src', () => {
    render(<YouTubePlayer streamUrl={mockStreamUrl} />);

    const iframe = screen.getByTitle('YouTube video player');
    expect(iframe).toBeInTheDocument();
    expect(iframe).toHaveAttribute('src', expect.stringContaining(mockStreamUrl));
  });

  it('renders iframe with custom title', () => {
    render(<YouTubePlayer streamUrl={mockStreamUrl} title="Test Video" />);

    const iframe = screen.getByTitle('Test Video');
    expect(iframe).toBeInTheDocument();
  });

  it('has allowfullscreen attribute', () => {
    render(<YouTubePlayer streamUrl={mockStreamUrl} />);

    const iframe = screen.getByTitle('YouTube video player');
    expect(iframe).toHaveAttribute('allowfullscreen');
  });

  it('has correct allow attributes for YouTube', () => {
    render(<YouTubePlayer streamUrl={mockStreamUrl} />);

    const iframe = screen.getByTitle('YouTube video player');
    expect(iframe).toHaveAttribute(
      'allow',
      expect.stringContaining('accelerometer')
    );
    expect(iframe).toHaveAttribute('allow', expect.stringContaining('autoplay'));
    expect(iframe).toHaveAttribute(
      'allow',
      expect.stringContaining('encrypted-media')
    );
  });

  it('maintains 16:9 aspect ratio', () => {
    render(<YouTubePlayer streamUrl={mockStreamUrl} />);

    const container = screen.getByTitle('YouTube video player').parentElement;
    expect(container).toHaveClass('aspect-video');
  });
});
