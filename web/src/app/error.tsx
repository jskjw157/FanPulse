"use client";

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';

export default function Error({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  const router = useRouter();

  useEffect(() => {
    console.error(error);
  }, [error]);

  return (
    <div className="relative flex flex-col items-center justify-center h-screen bg-gradient-to-br from-pink-50 via-purple-50 to-blue-50 px-6">
      {/* Background Decoration */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-20 left-10 w-32 h-32 bg-pink-200/30 rounded-full blur-3xl"></div>
        <div className="absolute bottom-32 right-10 w-40 h-40 bg-purple-200/30 rounded-full blur-3xl"></div>
        <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-64 h-64 bg-blue-200/20 rounded-full blur-3xl"></div>
      </div>

      {/* Error Icon */}
      <div className="relative z-10 mb-8">
        <div className="w-32 h-32 bg-gradient-to-br from-pink-400 to-purple-500 rounded-full flex items-center justify-center shadow-2xl animate-pulse">
          <i className="ri-error-warning-line text-6xl text-white"></i>
        </div>
      </div>

      {/* Error Message */}
      <div className="relative z-10 text-center mb-12">
        <h1 className="text-4xl font-black text-gray-800 mb-4">
          Oops!
        </h1>
        <p className="text-xl font-semibold text-gray-700 mb-3">
          Something went wrong
        </p>
        <p className="text-base text-gray-500 max-w-md">
          We're sorry for the inconvenience. The page you're looking for might have been removed or is temporarily unavailable.
        </p>
      </div>

      {/* Action Buttons */}
      <div className="relative z-10 flex flex-col gap-3 w-full max-w-xs">
        <button
          onClick={() => router.push('/')}
          className="w-full py-4 bg-gradient-to-r from-pink-500 to-purple-600 text-white font-semibold rounded-full shadow-lg hover:shadow-xl transition-all duration-300 flex items-center justify-center gap-2"
        >
          <i className="ri-home-4-line text-xl"></i>
          <span>Go to Home</span>
        </button>
        
        <button
          onClick={() => reset()}
          className="w-full py-4 bg-white text-gray-700 font-semibold rounded-full shadow-md hover:shadow-lg transition-all duration-300 flex items-center justify-center gap-2"
        >
          <i className="ri-refresh-line text-xl"></i>
          <span>Try Again</span>
        </button>
      </div>

      {/* Help Text */}
      <div className="relative z-10 mt-12 text-center">
        <p className="text-sm text-gray-400">
          Need help? Contact our{' '}
          <button className="text-purple-600 font-semibold underline">
            support team
          </button>
        </p>
      </div>

      {/* Decorative Elements */}
      <div className="absolute bottom-0 left-0 right-0 h-24 bg-gradient-to-t from-white/50 to-transparent pointer-events-none"></div>
    </div>
  );
}
