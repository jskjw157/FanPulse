"use client";

import Link from "next/link";

interface MobileMenuProps {
  isOpen: boolean;
  onClose: () => void;
}

export default function MobileMenu({ isOpen, onClose }: MobileMenuProps) {
  if (!isOpen) return null;

  return (
    <>
      <div 
        className="fixed inset-0 bg-black/50 z-[60]"
        onClick={onClose}
      ></div>
      
      <div className="fixed top-0 right-0 bottom-0 w-72 bg-white z-[70] shadow-2xl animate-slide-in-right">
        <div className="bg-gradient-to-r from-purple-600 to-pink-600 p-4 flex items-center justify-between">
          <h2 className="text-white font-bold text-lg">메뉴</h2>
          <button 
            onClick={onClose}
            className="w-8 h-8 flex items-center justify-center"
          >
            <i className="ri-close-line text-2xl text-white"></i>
          </button>
        </div>

        <div className="overflow-y-auto h-[calc(100vh-64px)]">
          <div className="p-4 space-y-1">
            <Link 
              href="/chart" 
              onClick={onClose}
              className="flex items-center gap-3 p-3 rounded-xl hover:bg-purple-50 transition-colors"
            >
              <div className="w-10 h-10 flex items-center justify-center bg-purple-100 rounded-full">
                <i className="ri-bar-chart-box-line text-xl text-purple-600"></i>
              </div>
              <span className="font-medium text-gray-900">차트</span>
            </Link>

            <Link 
              href="/news-detail" 
              onClick={onClose}
              className="flex items-center gap-3 p-3 rounded-xl hover:bg-purple-50 transition-colors"
            >
              <div className="w-10 h-10 flex items-center justify-center bg-indigo-100 rounded-full">
                <i className="ri-newspaper-line text-xl text-indigo-600"></i>
              </div>
              <span className="font-medium text-gray-900">뉴스</span>
            </Link>

            {/* ... other items ... */}
            
            <Link 
              href="/settings" 
              onClick={onClose}
              className="flex items-center gap-3 p-3 rounded-xl hover:bg-purple-50 transition-colors"
            >
              <div className="w-10 h-10 flex items-center justify-center bg-gray-100 rounded-full">
                <i className="ri-settings-3-line text-xl text-gray-600"></i>
              </div>
              <span className="font-medium text-gray-900">설정</span>
            </Link>
          </div>
        </div>
      </div>
    </>
  );
}
