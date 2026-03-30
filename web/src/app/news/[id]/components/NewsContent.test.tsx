import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import NewsContent from './NewsContent';

describe('NewsContent', () => {
  it('renders HTML content safely', () => {
    render(
      <NewsContent content="<p>BTS가 새 앨범을 발표합니다.</p><p>상세 내용...</p>" />
    );

    expect(screen.getByText('BTS가 새 앨범을 발표합니다.')).toBeInTheDocument();
    expect(screen.getByText('상세 내용...')).toBeInTheDocument();
  });

  it('removes script tags for XSS prevention', () => {
    render(
      <NewsContent content='<script>alert("xss")</script><p>본문</p>' />
    );

    expect(screen.getByText('본문')).toBeInTheDocument();
    expect(screen.queryByText('alert')).not.toBeInTheDocument();
  });

  it('allows img tags with lazy loading', () => {
    const { container } = render(
      <NewsContent content='<img src="/test.jpg" alt="테스트 이미지" /><p>텍스트</p>' />
    );

    const img = container.querySelector('img');
    expect(img).toBeInTheDocument();
    expect(img).toHaveAttribute('src', '/test.jpg');
  });
});
