'use client';

import { useCallback, useEffect, useState } from 'react';
import { fetchLatestChart, type ChartResponse, type ChartType } from '@/lib/api/chart';
import type { AsyncState } from '@/types/common';

interface UseLatestChartReturn {
  chart: ChartResponse | null;
  state: AsyncState;
  error: string | null;
  retry: () => void;
}

export function useLatestChart(chartType: ChartType): UseLatestChartReturn {
  const [chart, setChart] = useState<ChartResponse | null>(null);
  const [state, setState] = useState<AsyncState>('loading');
  const [error, setError] = useState<string | null>(null);
  const [retryCount, setRetryCount] = useState(0);

  useEffect(() => {
    const controller = new AbortController();

    async function loadChart() {
      setState('loading');
      setError(null);

      try {
        const result = await fetchLatestChart(chartType, controller.signal);
        if (controller.signal.aborted) return;
        setChart(result);
        setState('success');
      } catch {
        if (controller.signal.aborted) return;
        setChart(null);
        setError('차트를 불러올 수 없습니다');
        setState('error');
      }
    }

    loadChart();
    return () => controller.abort();
  }, [chartType, retryCount]);

  const retry = useCallback(() => {
    setRetryCount((count) => count + 1);
  }, []);

  return { chart, state, error, retry };
}
