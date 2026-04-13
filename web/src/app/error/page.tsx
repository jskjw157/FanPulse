"use client";

import { useRouter, useSearchParams } from 'next/navigation';
import { Suspense } from 'react';
import { useTranslation } from 'react-i18next';
import '@/i18n';

type ErrorType = 'network' | 'auth' | 'server' | 'default';

const ERROR_ICONS: Record<ErrorType, string> = {
  network: 'ri-wifi-off-line',
  auth: 'ri-lock-line',
  server: 'ri-server-line',
  default: 'ri-error-warning-line',
};

const ERROR_GRADIENTS: Record<ErrorType, string> = {
  network: 'from-orange-400 to-red-500',
  auth: 'from-yellow-400 to-orange-500',
  server: 'from-red-400 to-pink-500',
  default: 'from-pink-400 to-purple-500',
};

function ErrorContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { t } = useTranslation();

  const rawType = searchParams.get('type') ?? 'default';
  const errorType: ErrorType = ['network', 'auth', 'server'].includes(rawType)
    ? (rawType as ErrorType)
    : 'default';

  const icon = ERROR_ICONS[errorType];
  const gradient = ERROR_GRADIENTS[errorType];

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
        <div className={`w-28 h-28 bg-gradient-to-br ${gradient} rounded-full flex items-center justify-center shadow-2xl`}>
          <i className={`${icon} text-5xl text-white`} />
        </div>
      </div>

      {/* Error Message */}
      <div className="relative z-10 text-center mb-10">
        <h1 className="text-3xl md:text-4xl font-black text-gray-800 mb-3">
          {t(`errorPage.${errorType}.title`)}
        </h1>
        <p className="text-base text-gray-500 max-w-md">
          {t(`errorPage.${errorType}.description`)}
        </p>
      </div>

      {/* Action Buttons */}
      <div className="relative z-10 flex flex-col gap-3 w-full max-w-xs">
        <button
          onClick={() => router.push('/')}
          className="w-full py-3.5 bg-gradient-to-r from-pink-500 to-purple-600 text-white font-semibold rounded-full shadow-lg hover:shadow-xl transition-all duration-300 flex items-center justify-center gap-2"
        >
          <i className="ri-home-4-line text-xl" />
          <span>{t('errorPage.goHome')}</span>
        </button>

        <button
          onClick={() => router.back()}
          className="w-full py-3.5 bg-white text-gray-700 font-semibold rounded-full shadow-md hover:shadow-lg transition-all duration-300 flex items-center justify-center gap-2"
        >
          <i className="ri-arrow-left-line text-xl" />
          <span>{t('errorPage.goBack')}</span>
        </button>
      </div>
    </div>
  );
}

export default function ErrorPage() {
  return (
    <Suspense
      fallback={
        <div className="flex items-center justify-center min-h-[80vh]">
          <div className="w-8 h-8 border-4 border-purple-200 border-t-purple-600 rounded-full animate-spin" />
        </div>
      }
    >
      <ErrorContent />
    </Suspense>
  );
}
