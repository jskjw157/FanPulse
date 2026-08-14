import { describe, expect, it } from 'vitest';
import { isIsoInstant } from './api-response';

describe('isIsoInstant', () => {
  it.each([
    '2026-08-14T02:00:00Z',
    '2026-08-14T02:00:00.123456Z',
    '2026-08-14T11:00:00+09:00',
    '2026-08-14T20:00:00+18:00',
  ])('accepts an ISO-8601 instant with an explicit offset: %s', (value) => {
    expect(isIsoInstant(value)).toBe(true);
  });

  it('rejects a date-time without an explicit UTC offset', () => {
    expect(isIsoInstant('2026-08-14T02:00:00')).toBe(false);
  });

  it.each([
    '2026-08-14T20:00:00+18:01',
    '2026-08-14T20:00:00-18:01',
  ])('rejects an offset outside the Java Instant range: %s', (value) => {
    expect(isIsoInstant(value)).toBe(false);
  });
});
