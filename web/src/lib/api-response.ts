export type DataGuard<T> = (data: unknown) => data is T;

export function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}

export function isUuid(value: unknown): value is string {
  return (
    typeof value === 'string' &&
    /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(value)
  );
}

export function isNonEmptyString(value: unknown): value is string {
  return typeof value === 'string' && value.trim().length > 0;
}

export function isNullableString(value: unknown): value is string | null {
  return value === null || typeof value === 'string';
}

export function isPositiveInteger(value: unknown): value is number {
  return Number.isInteger(value) && (value as number) > 0;
}

export function isNullableInteger(value: unknown): value is number | null {
  return value === null || Number.isInteger(value);
}

export function isIsoDate(value: unknown): value is string {
  if (typeof value !== 'string') return false;
  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value);
  if (!match) return false;
  const year = Number(match[1]);
  const month = Number(match[2]);
  const day = Number(match[3]);
  const date = new Date(Date.UTC(year, month - 1, day));
  return (
    date.getUTCFullYear() === year &&
    date.getUTCMonth() === month - 1 &&
    date.getUTCDate() === day
  );
}

export function isIsoDateTime(value: unknown): value is string {
  return (
    typeof value === 'string' &&
    value.length > 11 &&
    value[10] === 'T' &&
    isIsoDate(value.slice(0, 10)) &&
    !Number.isNaN(Date.parse(value))
  );
}

export function isIsoInstant(value: unknown): value is string {
  return (
    isIsoDateTime(value) &&
    /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d+)?(?:Z|[+-]\d{2}:\d{2})$/.test(value)
  );
}

export function unwrapApiResponse<T>(
  payload: unknown,
  errorMessage: string,
  guard: DataGuard<T>
): T {
  if (!isRecord(payload) || payload.success !== true || payload.data == null) {
    throw new Error(errorMessage);
  }

  if (!guard(payload.data)) {
    throw new Error(errorMessage);
  }

  return payload.data;
}
