import { render, screen } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

const redirectMock = vi.hoisted(() => vi.fn());

vi.mock('next/navigation', () => ({
  redirect: redirectMock,
  useRouter: () => ({
    back: vi.fn(),
  }),
}));

import LiveDetailPage from './page';

describe('LiveDetailPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('redirects an ID-based legacy URL to the canonical live route', async () => {
    await LiveDetailPage({ searchParams: Promise.resolve({ id: 'live-123' }) });

    expect(redirectMock).toHaveBeenCalledWith('/live/live-123');
  });

  it('shows an explicit error and live-list link without an ID', async () => {
    const page = await LiveDetailPage({ searchParams: Promise.resolve({}) });

    render(page);

    expect(screen.getByText('라이브 ID가 필요합니다')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: '라이브 목록으로 이동' })).toHaveAttribute(
      'href',
      '/live'
    );
    expect(screen.queryByText('NewJeans 컴백 쇼케이스')).not.toBeInTheDocument();
  });
});
