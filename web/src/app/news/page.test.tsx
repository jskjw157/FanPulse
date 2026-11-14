import { act, cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import '@/i18n';

vi.mock('next/navigation', () => ({
  useRouter: () => ({ back: vi.fn() }),
}));

vi.mock('@/lib/api-client', () => ({ apiClient: { get: vi.fn() } }));

import { apiClient } from '@/lib/api-client';
import NewsListPage from './page';

const first = {
  id: '11111111-1111-1111-1111-111111111111',
  artistId: '33333333-3333-3333-3333-333333333333',
  title: '첫 기사',
  thumbnailUrl: null,
  sourceName: '언론사',
  category: 'GENERAL',
  publishedAt: '2026-08-14T03:00:00Z',
};

const second = {
  ...first,
  id: '22222222-2222-2222-2222-222222222222',
  title: '둘째 기사',
};

function newsPage(page: number) {
  return {
    data: {
      success: true,
      data: {
        content: [page === 0 ? first : second],
        totalElements: 21,
        page,
        size: 20,
        totalPages: 2,
      },
    },
  };
}

function pendingResponse() {
  let resolve!: (value: ReturnType<typeof newsPage>) => void;
  let reject!: (error: Error) => void;
  const promise = new Promise<ReturnType<typeof newsPage>>((yes, no) => {
    resolve = yes;
    reject = no;
  });
  return { promise, resolve, reject };
}

function serviceUnavailable() {
  return Object.assign(new Error('Service Unavailable'), {
    isAxiosError: true,
    response: { status: 503 },
  });
}

describe('NewsListPage pagination feedback', () => {
  beforeEach(() => vi.mocked(apiClient.get).mockReset());
  afterEach(cleanup);

  it('keeps cards visible through deferred page two, repeated clicks, a 503, and same-page retry', async () => {
    const pageTwo = pendingResponse();
    const retry = pendingResponse();
    vi.mocked(apiClient.get)
      .mockResolvedValueOnce(newsPage(0))
      .mockReturnValueOnce(pageTwo.promise)
      .mockReturnValueOnce(retry.promise);

    render(<NewsListPage />);
    expect(await screen.findByRole('link', { name: /첫 기사/ })).toHaveAttribute('href', `/news/${first.id}`);
    const more = screen.getByRole('button', { name: '더 보기' });
    fireEvent.click(more);

    expect(screen.getByRole('button', { name: '불러오는 중...' })).toBeDisabled();
    expect(screen.getByRole('status')).toHaveTextContent('불러오는 중...');
    expect(screen.getByRole('link', { name: /첫 기사/ })).toBeInTheDocument();
    fireEvent.click(more);
    fireEvent.click(more);
    expect(apiClient.get).toHaveBeenCalledTimes(2);
    expect(apiClient.get).toHaveBeenNthCalledWith(2, '/news', {
      params: { page: 1, size: 20, sortBy: 'publishedAt', sortDir: 'desc' },
      signal: expect.any(AbortSignal),
    });
    expect(vi.mocked(apiClient.get).mock.calls[1][1]?.signal?.aborted).toBe(false);

    await act(async () => { pageTwo.reject(serviceUnavailable()); });
    expect(await screen.findByRole('alert')).toHaveTextContent('서버에 문제가 발생했습니다');
    expect(screen.getByRole('link', { name: /첫 기사/ })).toBeInTheDocument();
    expect(screen.queryByRole('status')).not.toBeInTheDocument();
    const retryButton = screen.getByRole('button', { name: '다시 시도' });
    expect(retryButton).toBeEnabled();

    fireEvent.click(retryButton);
    expect(screen.getByRole('button', { name: '불러오는 중...' })).toBeDisabled();
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
    expect(screen.getByRole('link', { name: /첫 기사/ })).toBeInTheDocument();
    fireEvent.click(retryButton);
    expect(apiClient.get).toHaveBeenCalledTimes(3);
    expect(apiClient.get).toHaveBeenNthCalledWith(3, '/news', {
      params: { page: 1, size: 20, sortBy: 'publishedAt', sortDir: 'desc' },
      signal: expect.any(AbortSignal),
    });

    await act(async () => { retry.resolve(newsPage(1)); });
    expect(await screen.findByRole('link', { name: /둘째 기사/ })).toBeInTheDocument();
    expect(screen.getAllByRole('link', { name: /첫 기사/ })).toHaveLength(1);
    expect(screen.getAllByRole('link', { name: /둘째 기사/ })).toHaveLength(1);
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
    expect(screen.queryByRole('status')).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '더 보기' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '다시 시도' })).not.toBeInTheDocument();
    expect(screen.getByText('모든 뉴스를 불러왔습니다')).toBeInTheDocument();
    expect(apiClient.get).toHaveBeenCalledTimes(3);
  });

  it('still retries an initial failure from page zero', async () => {
    vi.mocked(apiClient.get)
      .mockRejectedValueOnce(serviceUnavailable())
      .mockResolvedValueOnce(newsPage(0));

    render(<NewsListPage />);
    expect(await screen.findByText('서버에 문제가 발생했습니다')).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: /첫 기사/ })).not.toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '다시 시도' }));

    expect(await screen.findByRole('link', { name: /첫 기사/ })).toBeInTheDocument();
    await waitFor(() => expect(screen.getByRole('button', { name: '더 보기' })).toBeEnabled());
    expect(apiClient.get).toHaveBeenCalledTimes(2);
    expect(apiClient.get).toHaveBeenNthCalledWith(2, '/news', {
      params: { page: 0, size: 20, sortBy: 'publishedAt', sortDir: 'desc' },
      signal: expect.any(AbortSignal),
    });
  });
});
