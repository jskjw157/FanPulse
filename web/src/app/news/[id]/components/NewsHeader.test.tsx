import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import NewsHeader from './NewsHeader';

describe('NewsHeader', () => {
  it('renders title in h1 tag', () => {
    render(<NewsHeader title="BTS 새 앨범 발매 예정" thumbnailUrl="/mock.jpg" />);

    const heading = screen.getByRole('heading', { level: 1 });
    expect(heading).toHaveTextContent('BTS 새 앨범 발매 예정');
  });

  it('renders thumbnail image with title as alt text', () => {
    render(<NewsHeader title="BTS 새 앨범 발매 예정" thumbnailUrl="/mock.jpg" />);

    const image = screen.getByAltText('BTS 새 앨범 발매 예정');
    expect(image).toBeInTheDocument();
    expect(image).toHaveAttribute('src', expect.stringContaining('mock.jpg'));
  });

  it('does not render image when thumbnailUrl is empty', () => {
    render(<NewsHeader title="제목" thumbnailUrl="" />);

    const heading = screen.getByRole('heading', { level: 1 });
    expect(heading).toHaveTextContent('제목');
    expect(screen.queryByRole('img')).not.toBeInTheDocument();
  });
});
