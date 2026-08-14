import { render, screen } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

const redirectMock = vi.hoisted(() => vi.fn());

vi.mock('next/navigation', () => ({ redirect: redirectMock }));

import ArtistDetailPage from './page';

describe('ArtistDetailPage', () => {
  beforeEach(() => vi.clearAllMocks());

  it('redirects an ID-based legacy URL to the canonical artist route', async () => {
    await ArtistDetailPage({ searchParams: Promise.resolve({ id: '11111111-1111-1111-1111-111111111111' }) });

    expect(redirectMock).toHaveBeenCalledWith('/artists/11111111-1111-1111-1111-111111111111');
  });

  it('shows an explicit error and artist-list link without an ID', async () => {
    const page = await ArtistDetailPage({ searchParams: Promise.resolve({}) });
    render(page);

    expect(screen.getByText('아티스트 ID가 필요합니다')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: '아티스트 목록으로 이동' })).toHaveAttribute(
      'href',
      '/search'
    );
    expect(screen.queryByText('2.5M')).not.toBeInTheDocument();
  });
});
