"use client";

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useTranslation } from 'react-i18next';
import '@/i18n';

export default function Error({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  const router = useRouter();
  const { t } = useTranslation();

  useEffect(() => {
    console.error(error);
  }, [error]);

  return (
    <div className="relative flex flex-col items-center justify-center min-h-[80vh] bg-gradient-to-br from-pink-50 via-purple-50 to-blue-50 px-6">
      {/* Background Decoration */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-20 left-10 w-32 h-32 bg-pink-200/30 rounded-full blur-3xl" />
        <div className="absolute bottom-32 right-10 w-40 h-40 bg-purple-200/30 rounded-full blur-3xl" />
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-64 h-64 bg-blue-200/20 rounded-full blur-3xl" />
      </div>

      {/* Error Icon */}
      <div className="relative z-10 mb-8">
        <div className="w-28 h-28 bg-gradient-to-br from-pink-400 to-purple-500 rounded-full flex items-center justify-center shadow-2xl">
          <i className="ri-error-warning-line text-5xl text-white" />
        </div>
      </div>

      {/* Error Message */}
      <div className="relative z-10 text-center mb-10">
        <h1 className="text-4xl font-black text-gray-800 mb-3">
          {t('error.title')}
        </h1>
        <p className="text-lg font-semibold text-gray-700 mb-2">
          {t('error.subtitle')}
        </p>
        <p className="text-base text-gray-500 max-w-md">
          {t('error.description')}
        </p>
      </div>

      {/* Action Buttons */}
      <div className="relative z-10 flex flex-col gap-3 w-full max-w-xs">
        <button
          onClick={() => reset()}
          className="w-full py-3.5 bg-gradient-to-r from-pink-500 to-purple-600 text-white font-semibold rounded-full shadow-lg hover:shadow-xl transition-all duration-300 flex items-center justify-center gap-2"
        >
          <i className="ri-refresh-line text-xl" />
          <span>{t('error.retry')}</span>
        </button>

        <button
          onClick={() => router.push('/')}
          className="w-full py-3.5 bg-white text-gray-700 font-semibold rounded-full shadow-md hover:shadow-lg transition-all duration-300 flex items-center justify-center gap-2"
        >
          <i className="ri-home-4-line text-xl" />
          <span>{t('error.goHome')}</span>
        </button>
      </div>

      {/* Help Text */}
      <div className="relative z-10 mt-10 text-center">
        <p className="text-sm text-gray-400">
          {t('error.help')}{' '}
          <button className="text-purple-600 font-semibold underline hover:text-purple-700 transition-colors">
            {t('error.support')}
          </button>
        </p>
      </div>
    </div>
  );
}
