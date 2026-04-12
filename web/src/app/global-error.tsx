"use client";

/**
 * Root Layout 에러를 처리하는 글로벌 에러 바운더리.
 * html/body 태그를 직접 포함해야 한다.
 */
export default function GlobalError({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  return (
    <html lang="ko">
      <head>
        <link
          href="https://cdn.jsdelivr.net/npm/remixicon@4.7.0/fonts/remixicon.css"
          rel="stylesheet"
        />
      </head>
      <body className="antialiased">
        <div className="relative flex flex-col items-center justify-center h-screen bg-gradient-to-br from-pink-50 via-purple-50 to-blue-50 px-6">
          {/* Background Decoration */}
          <div className="absolute inset-0 overflow-hidden pointer-events-none">
            <div className="absolute top-20 left-10 w-32 h-32 bg-pink-200/30 rounded-full blur-3xl" />
            <div className="absolute bottom-32 right-10 w-40 h-40 bg-purple-200/30 rounded-full blur-3xl" />
          </div>

          {/* Error Icon */}
          <div className="relative z-10 mb-8">
            <div className="w-28 h-28 bg-gradient-to-br from-red-400 to-pink-500 rounded-full flex items-center justify-center shadow-2xl">
              <i className="ri-alarm-warning-line text-5xl text-white" />
            </div>
          </div>

          {/* Error Message */}
          <div className="relative z-10 text-center mb-10">
            <h1 className="text-3xl font-black text-gray-800 mb-3">
              Something Went Wrong
            </h1>
            <p className="text-base text-gray-500 max-w-md">
              A critical error occurred. Please try refreshing the page.
            </p>
          </div>

          {/* Retry Button */}
          <div className="relative z-10">
            <button
              onClick={() => reset()}
              className="px-8 py-3.5 bg-gradient-to-r from-pink-500 to-purple-600 text-white font-semibold rounded-full shadow-lg hover:shadow-xl transition-all duration-300 flex items-center gap-2"
            >
              <i className="ri-refresh-line text-xl" />
              <span>Refresh Page</span>
            </button>
          </div>
        </div>
      </body>
    </html>
  );
}
