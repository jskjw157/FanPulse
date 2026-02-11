import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import SourceLink from './SourceLink';

describe('SourceLink', () => {
  it('renders link with source name', () => {
    render(
      <SourceLink
        url="https://sports.chosun.com/article/123"
        sourceName="스포츠조선"
      />
    );

    const link = screen.getByRole('link');
    expect(link).toHaveTextContent('스포츠조선에서 원문 보기');
  });

  it('opens in new tab with security attributes', () => {
    render(
      <SourceLink
        url="https://sports.chosun.com/article/123"
        sourceName="스포츠조선"
      />
    );

    const link = screen.getByRole('link');
    expect(link).toHaveAttribute('target', '_blank');
    expect(link).toHaveAttribute('rel', 'noopener noreferrer');
  });

  it('has correct href', () => {
    render(
      <SourceLink
        url="https://sports.chosun.com/article/123"
        sourceName="스포츠조선"
      />
    );

    const link = screen.getByRole('link');
    expect(link).toHaveAttribute('href', 'https://sports.chosun.com/article/123');
  });
});
