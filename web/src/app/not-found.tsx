"use client";

import { useRouter } from 'next/navigation';
import { useTranslation } from 'react-i18next';
import '@/i18n';

export default function NotFound() {
  const router = useRouter();
  const { t } = useTranslation();

  return (
    <div className="relative flex flex-col items-center justify-center min-h-[80vh] bg-gradient-to-br from-pink-50 via-purple-50 to-blue-50 px-6 overflow-hidden">
      {/* Background Decoration */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-16 right-16 w-36 h-36 bg-purple-200/30 rounded-full blur-3xl" />
        <div className="absolute bottom-24 left-12 w-44 h-44 bg-pink-200/30 rounded-full blur-3xl" />
        <div className="absolute top-1/3 left-1/3 w-56 h-56 bg-blue-200/20 rounded-full blur-3xl" />
      </div>

      {/* Large 404 Background Text */}
      <div className="absolute inset-0 flex items-center justify-center pointer-events-none select-none">
        <span className="text-[12rem] md:text-[16rem] font-black text-gray-100/60 leading-none">
          {t('notFound.subtitle')}
        </span>
      </div>

      {/* Icon */}
      <div className="relative z-10 mb-8">
        <div className="w-28 h-28 bg-gradient-to-br from-purple-400 to-blue-500 rounded-full flex items-center justify-center shadow-2xl">
          <i className="ri-compass-discover-line text-5xl text-white" />
        </div>
      </div>

      {/* Message */}
      <div className="relative z-10 text-center mb-10">
        <h1 className="text-3xl md:text-4xl font-black text-gray-800 mb-3">
          {t('notFound.title')}
        </h1>
        <p className="text-base text-gray-500 max-w-md">
          {t('notFound.description')}
        </p>
      </div>

      {/* Action Buttons */}
      <div className="relative z-10 flex flex-col gap-3 w-full max-w-xs">
        <button
          onClick={() => router.push('/')}
          className="w-full py-3.5 bg-gradient-to-r from-purple-500 to-blue-600 text-white font-semibold rounded-full shadow-lg hover:shadow-xl transition-all duration-300 flex items-center justify-center gap-2"
        >
          <i className="ri-home-4-line text-xl" />
          <span>{t('notFound.goHome')}</span>
        </button>

        <button
          onClick={() => router.back()}
          className="w-full py-3.5 bg-white text-gray-700 font-semibold rounded-full shadow-md hover:shadow-lg transition-all duration-300 flex items-center justify-center gap-2"
        >
          <i className="ri-arrow-left-line text-xl" />
          <span>{t('notFound.goBack')}</span>
        </button>
      </div>
    </div>
  );
}
