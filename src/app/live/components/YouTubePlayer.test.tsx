import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import YouTubePlayer from './YouTubePlayer';

describe('YouTubePlayer', () => {
  it('renders iframe with correct src', () => {
    render(<YouTubePlayer streamUrl="https://www.youtube.com/embed/dQw4w9WgXcQ" />);

    const iframe = screen.getByTitle('YouTube video player');
    expect(iframe).toHaveAttribute('src', 'https://www.youtube.com/embed/dQw4w9WgXcQ');
  });

  it('has allowfullscreen attribute', () => {
    render(<YouTubePlayer streamUrl="https://www.youtube.com/embed/test" />);

    const iframe = screen.getByTitle('YouTube video player');
    expect(iframe).toHaveAttribute('allowfullscreen');
  });

  it('has correct allow attributes', () => {
    render(<YouTubePlayer streamUrl="https://www.youtube.com/embed/test" />);

    const iframe = screen.getByTitle('YouTube video player');
    expect(iframe.getAttribute('allow')).toContain('autoplay');
    expect(iframe.getAttribute('allow')).toContain('encrypted-media');
  });

  it('maintains 16:9 aspect ratio', () => {
    render(<YouTubePlayer streamUrl="https://www.youtube.com/embed/test" />);

    const container = screen.getByTitle('YouTube video player').parentElement;
    expect(container?.className).toContain('aspect-video');
  });
});
