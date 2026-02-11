import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import InfiniteScroll from './InfiniteScroll';

let observerCallback: IntersectionObserverCallback | null = null;

class MockIntersectionObserver implements IntersectionObserver {
  readonly root: Element | Document | null = null;
  readonly rootMargin: string = '';
  readonly thresholds: ReadonlyArray<number> = [];

  constructor(callback: IntersectionObserverCallback) {
    observerCallback = callback;
  }

  observe = vi.fn();
  unobserve = vi.fn();
  disconnect = vi.fn();
  takeRecords = vi.fn(() => []);
}

beforeEach(() => {
  observerCallback = null;
  vi.stubGlobal('IntersectionObserver', MockIntersectionObserver);
});

afterEach(() => {
  vi.unstubAllGlobals();
});

describe('InfiniteScroll', () => {
  it('renders children', () => {
    render(
      <InfiniteScroll hasMore={true} loading={false} onLoadMore={vi.fn()}>
        <div>Child Content</div>
      </InfiniteScroll>
    );

    expect(screen.getByText('Child Content')).toBeInTheDocument();
  });

  it('shows loading spinner when loading', () => {
    render(
      <InfiniteScroll hasMore={true} loading={true} onLoadMore={vi.fn()}>
        <div>Content</div>
      </InfiniteScroll>
    );

    expect(screen.getByRole('status')).toBeInTheDocument();
  });

  it('shows end message when no more items', () => {
    render(
      <InfiniteScroll hasMore={false} loading={false} onLoadMore={vi.fn()}>
        <div>Content</div>
      </InfiniteScroll>
    );

    expect(screen.getByText('모든 라이브를 확인했습니다')).toBeInTheDocument();
  });

  it('calls onLoadMore when sentinel is visible and hasMore is true', () => {
    const onLoadMore = vi.fn();

    render(
      <InfiniteScroll hasMore={true} loading={false} onLoadMore={onLoadMore}>
        <div>Content</div>
      </InfiniteScroll>
    );

    // Simulate intersection
    observerCallback?.([{ isIntersecting: true } as IntersectionObserverEntry], {} as IntersectionObserver);

    expect(onLoadMore).toHaveBeenCalledOnce();
  });

  it('does not call onLoadMore when loading', () => {
    const onLoadMore = vi.fn();

    render(
      <InfiniteScroll hasMore={true} loading={true} onLoadMore={onLoadMore}>
        <div>Content</div>
      </InfiniteScroll>
    );

    observerCallback?.([{ isIntersecting: true } as IntersectionObserverEntry], {} as IntersectionObserver);

    expect(onLoadMore).not.toHaveBeenCalled();
  });
});
