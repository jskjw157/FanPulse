"use client";

import { useRouter } from "next/navigation";

interface PageHeaderProps {
  title: string;
  showBack?: boolean;
  rightAction?: React.ReactNode;
}

export default function PageHeader({ 
  title, 
  showBack = true, 
  rightAction 
}: PageHeaderProps) {
  const router = useRouter();

  return (
    <header className="fixed top-0 left-0 right-0 bg-white border-b border-gray-200 z-50 lg:static lg:z-auto lg:border-none lg:bg-transparent lg:pt-8 lg:pb-4">
      <div className="max-w-7xl mx-auto px-4 h-16 flex items-center justify-between lg:h-auto lg:px-8">
        <div className="flex items-center gap-2">
          {showBack && (
            <button 
              onClick={() => router.back()}
              className="w-10 h-10 flex items-center justify-center rounded-full hover:bg-gray-100 transition-colors lg:hidden"
            >
              <i className="ri-arrow-left-line text-xl text-gray-900"></i>
            </button>
          )}
          <h1 className="text-lg font-bold text-gray-900 lg:text-3xl">{title}</h1>
        </div>
        
        {rightAction && (
          <div className="flex items-center gap-2">
            {rightAction}
          </div>
        )}
      </div>
    </header>
  );
}
