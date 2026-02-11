import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import NewsMetadata from './NewsMetadata';

describe('NewsMetadata', () => {
  beforeEach(() => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-02-01T11:00:00Z'));
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('renders source name', () => {
    render(
      <NewsMetadata
        source="스포츠조선"
        publishedAt="2026-02-01T09:00:00Z"
      />
    );

    expect(screen.getByText('스포츠조선')).toBeInTheDocument();
  });

  it('renders relative time for recent date', () => {
    render(
      <NewsMetadata
        source="스포츠조선"
        publishedAt="2026-02-01T09:00:00Z"
      />
    );

    expect(screen.getByText('2시간 전')).toBeInTheDocument();
  });

  it('renders author when provided', () => {
    render(
      <NewsMetadata
        source="스포츠조선"
        publishedAt="2026-02-01T09:00:00Z"
        author="김기자"
      />
    );

    expect(screen.getByText('스포츠조선')).toBeInTheDocument();
    expect(screen.getByText('김기자')).toBeInTheDocument();
  });

  it('renders formatted date for old dates', () => {
    render(
      <NewsMetadata
        source="스포츠조선"
        publishedAt="2026-01-15T09:00:00Z"
      />
    );

    expect(screen.getByText(/2026년 1월 15일/)).toBeInTheDocument();
  });
});
