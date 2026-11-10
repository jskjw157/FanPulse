import { act, cleanup, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import '@/i18n';

vi.mock('@/lib/api-client', () => ({ apiClient: { get: vi.fn() } }));

import { apiClient } from '@/lib/api-client';
import SearchPage from './page';

function response(title: string) {
  return {
    data: {
      live: {
        items: [{
          id: '11111111-1111-1111-1111-111111111111',
          artistId: '22222222-2222-2222-2222-222222222222',
          artistName: 'Artist',
          title: `${title} live`,
          thumbnailUrl: null,
          status: 'LIVE',
          scheduledAt: '2026-08-14T02:00:00Z',
        }],
        totalCount: 1,
      },
      news: {
        items: [{
          id: '33333333-3333-3333-3333-333333333333',
          title: `${title} news`,
          summary: 'Search summary',
          sourceName: 'News source',
          publishedAt: '2026-08-14T02:30:00Z',
        }],
        totalCount: 1,
      },
    },
  };
}

function pendingResponse() {
  let resolve!: (value: ReturnType<typeof response>) => void;
  let reject!: (reason: Error) => void;
  const promise = new Promise<ReturnType<typeof response>>((yes, no) => {
    resolve = yes;
    reject = no;
  });
  return { promise, resolve, reject };
}

async function advance(ms = 300) {
  await act(async () => { await vi.advanceTimersByTimeAsync(ms); });
}

function typeQuery(query: string) {
  fireEvent.change(screen.getByRole('textbox'), { target: { value: query } });
}

describe('SearchPage input and retry contract', () => {
  beforeEach(() => {
    vi.useFakeTimers();
    vi.mocked(apiClient.get).mockReset();
    localStorage.clear();
  });

  afterEach(() => {
    cleanup();
    vi.useRealTimers();
  });

  it('distinguishes blank, one-character guidance, and trimmed two-character requests', async () => {
    vi.mocked(apiClient.get).mockResolvedValue(response('두 글자'));
    render(<SearchPage />);
    await advance();
    expect(screen.queryByRole('status')).not.toBeInTheDocument();

    typeQuery('   ');
    await advance();
    expect(screen.queryByRole('status')).not.toBeInTheDocument();

    typeQuery(' 가 ');
    await advance();
    expect(screen.getByRole('status')).toHaveTextContent('검색어를 2자 이상 입력해 주세요');
    expect(screen.getByRole('textbox')).toHaveAccessibleDescription('검색어를 2자 이상 입력해 주세요');
    expect(screen.queryByRole('button', { name: '다시 시도' })).not.toBeInTheDocument();
    fireEvent.keyDown(screen.getByRole('textbox'), { key: 'Enter' });
    expect(localStorage.getItem('fanpulse_recent_searches')).toBeNull();
    expect(apiClient.get).not.toHaveBeenCalled();

    typeQuery(' 가나 ');
    await advance(299);
    expect(apiClient.get).not.toHaveBeenCalled();
    await advance(1);
    expect(apiClient.get).toHaveBeenCalledExactlyOnceWith('/search', {
      params: { q: '가나', limit: 10 }, signal: expect.any(AbortSignal),
    });
    expect(screen.getByRole('heading', { name: '두 글자 live' })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: '두 글자 news' })).toBeInTheDocument();
    expect(screen.queryByRole('status')).not.toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: 'Clear search' }));
    await advance();
    expect(screen.queryByText('두 글자 live')).not.toBeInTheDocument();
    expect(screen.queryByText('두 글자 news')).not.toBeInTheDocument();
    expect(screen.queryByRole('status')).not.toBeInTheDocument();
    expect(apiClient.get).toHaveBeenCalledTimes(1);
  });

  it('debounces rapid input to just the latest valid query', async () => {
    vi.mocked(apiClient.get).mockResolvedValue(response('최신'));
    render(<SearchPage />);
    typeQuery('B');
    await advance(200);
    typeQuery('BT');
    await advance(200);
    typeQuery('BTS');
    await advance(299);
    expect(apiClient.get).not.toHaveBeenCalled();
    await advance(1);
    expect(apiClient.get).toHaveBeenCalledExactlyOnceWith('/search', {
      params: { q: 'BTS', limit: 10 }, signal: expect.any(AbortSignal),
    });
    expect(screen.getByRole('heading', { name: '최신 news' })).toBeInTheDocument();
  });

  it('retries a failed search with the unchanged query and renders the new response', async () => {
    vi.mocked(apiClient.get)
      .mockRejectedValueOnce(new Error('offline'))
      .mockResolvedValueOnce(response('재시도 성공'));
    render(<SearchPage />);
    typeQuery(' BTS ');
    await advance();
    expect(screen.getByText('검색 결과를 불러올 수 없습니다')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '다시 시도' }));
    await advance();
    expect(screen.getByRole('textbox')).toHaveValue(' BTS ');
    expect(apiClient.get).toHaveBeenCalledTimes(2);
    expect(apiClient.get).toHaveBeenLastCalledWith('/search', {
      params: { q: 'BTS', limit: 10 }, signal: expect.any(AbortSignal),
    });
    expect(screen.getByRole('heading', { name: '재시도 성공 live' })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: '재시도 성공 news' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '다시 시도' })).not.toBeInTheDocument();
  });

  it('retries the edited query without a duplicate pending debounce request', async () => {
    vi.mocked(apiClient.get)
      .mockRejectedValueOnce(new Error('offline'))
      .mockResolvedValue(response('새 검색'));
    render(<SearchPage />);
    typeQuery('이전');
    await advance();
    typeQuery(' 새 검색 ');
    fireEvent.click(screen.getByRole('button', { name: '다시 시도' }));
    await advance();
    expect(apiClient.get).toHaveBeenCalledTimes(2);
    expect(apiClient.get).toHaveBeenLastCalledWith('/search', {
      params: { q: '새 검색', limit: 10 }, signal: expect.any(AbortSignal),
    });
    expect(screen.getByRole('heading', { name: '새 검색 news' })).toBeInTheDocument();
  });

  it('cancels the old query before the next debounce and ignores its late success', async () => {
    const old = pendingResponse();
    vi.mocked(apiClient.get).mockReturnValueOnce(old.promise).mockResolvedValueOnce(response('최신'));
    render(<SearchPage />);
    typeQuery('이전');
    await advance();
    const signal = vi.mocked(apiClient.get).mock.calls[0][1]?.signal;
    typeQuery('최신');
    expect(signal?.aborted).toBe(true);
    await act(async () => { old.resolve(response('이전')); });
    expect(screen.queryByText('이전 news')).not.toBeInTheDocument();
    await advance();
    expect(screen.getByRole('heading', { name: '최신 news' })).toBeInTheDocument();
  });

  it.each(['success', 'error'])('ignores an old %s arriving after the latest results', async (outcome) => {
    const old = pendingResponse();
    vi.mocked(apiClient.get).mockReturnValueOnce(old.promise).mockResolvedValueOnce(response('최신'));
    render(<SearchPage />);
    typeQuery('이전');
    await advance();
    typeQuery('최신');
    await advance();
    await act(async () => {
      if (outcome === 'success') old.resolve(response('이전'));
      else old.reject(new Error('late failure'));
    });
    expect(screen.getByRole('heading', { name: '최신 live' })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: '최신 news' })).toBeInTheDocument();
    expect(screen.queryByText('이전 news')).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '다시 시도' })).not.toBeInTheDocument();
  });

  it.each([' 가 ', '   '])('cancels an in-flight request when shortened to %j', async (query) => {
    const old = pendingResponse();
    vi.mocked(apiClient.get).mockReturnValueOnce(old.promise);
    render(<SearchPage />);
    typeQuery('이전');
    await advance();
    const signal = vi.mocked(apiClient.get).mock.calls[0][1]?.signal;
    typeQuery(query);
    expect(signal?.aborted).toBe(true);
    await advance();
    await act(async () => { old.reject(new Error('late failure')); });
    expect(screen.queryByRole('button', { name: '다시 시도' })).not.toBeInTheDocument();
    expect(apiClient.get).toHaveBeenCalledTimes(1);
    if (query.trim()) expect(screen.getByRole('status')).toHaveTextContent('검색어를 2자 이상 입력해 주세요');
    else expect(screen.queryByRole('status')).not.toBeInTheDocument();
  });

  it('cleans up pending debounce and in-flight requests on unmount', async () => {
    const pending = pendingResponse();
    vi.mocked(apiClient.get).mockReturnValue(pending.promise);
    const first = render(<SearchPage />);
    typeQuery('예약');
    first.unmount();
    await advance();
    expect(apiClient.get).not.toHaveBeenCalled();

    const second = render(<SearchPage />);
    typeQuery('요청');
    await advance();
    const signal = vi.mocked(apiClient.get).mock.calls[0][1]?.signal;
    second.unmount();
    expect(signal?.aborted).toBe(true);
    await act(async () => { pending.resolve(response('늦은 응답')); });
    expect(apiClient.get).toHaveBeenCalledTimes(1);
  });
});
