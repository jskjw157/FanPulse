import { render, screen } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

const redirectMock = vi.hoisted(() => vi.fn());

vi.mock('next/navigation', () => ({
  redirect: redirectMock,
  useRouter: () => ({
    back: vi.fn(),
  }),
}));

import NewsDetailPage from './page';

describe('NewsDetailPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('redirects an ID-based legacy URL to the canonical news route', async () => {
    await NewsDetailPage({ searchParams: Promise.resolve({ id: 'news-123' }) });

    expect(redirectMock).toHaveBeenCalledWith('/news/news-123');
  });

  it('shows an explicit error and news-list link without an ID', async () => {
    const page = await NewsDetailPage({ searchParams: Promise.resolve({}) });

    render(page);

    expect(screen.getByText('뉴스 ID가 필요합니다')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: '뉴스 목록으로 이동' })).toHaveAttribute(
      'href',
      '/news'
    );
    expect(screen.queryByText('BTS 새 앨범 발매 예정')).not.toBeInTheDocument();
  });
});
