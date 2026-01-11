"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

export default function Sidebar() {
  const pathname = usePathname();
  
  const isActive = (path: string) => pathname === path;

  return (
    <aside className="hidden lg:block fixed left-0 top-16 bottom-0 w-64 bg-white border-r border-gray-200 overflow-y-auto z-40">
      <div className="p-4 space-y-1">
        <Link 
          href="/" 
          className={`flex items-center gap-3 p-3 rounded-xl transition-colors ${isActive('/') ? 'bg-purple-50 text-purple-600' : 'hover:bg-purple-50 text-gray-900'}`}
        >
          <div className={`w-10 h-10 flex items-center justify-center ${isActive('/') ? '' : ''}`}>
            <i className={`ri-home-5-${isActive('/') ? 'fill' : 'line'} text-xl ${isActive('/') ? '' : 'text-gray-600'}`}></i>
          </div>
          <span className="font-medium">Home</span>
        </Link>

        <Link 
          href="/community" 
          className={`flex items-center gap-3 p-3 rounded-xl transition-colors ${isActive('/community') ? 'bg-purple-50 text-purple-600' : 'hover:bg-purple-50 text-gray-900'}`}
        >
          <div className="w-10 h-10 flex items-center justify-center">
            <i className={`ri-chat-3-${isActive('/community') ? 'fill' : 'line'} text-xl ${isActive('/community') ? '' : 'text-gray-600'}`}></i>
          </div>
          <span className="font-medium">Community</span>
        </Link>

        {/* ... Add other links as needed based on reference ... */}
        
        <Link 
          href="/live" 
          className={`flex items-center gap-3 p-3 rounded-xl transition-colors ${isActive('/live') ? 'bg-purple-50 text-purple-600' : 'hover:bg-purple-50 text-gray-900'}`}
        >
          <div className="w-10 h-10 flex items-center justify-center">
            <i className={`ri-live-${isActive('/live') ? 'fill' : 'line'} text-xl ${isActive('/live') ? '' : 'text-gray-600'}`}></i>
          </div>
          <span className="font-medium">Live</span>
        </Link>

        <Link 
          href="/voting" 
          className={`flex items-center gap-3 p-3 rounded-xl transition-colors ${isActive('/voting') ? 'bg-purple-50 text-purple-600' : 'hover:bg-purple-50 text-gray-900'}`}
        >
          <div className="w-10 h-10 flex items-center justify-center">
            <i className={`ri-trophy-${isActive('/voting') ? 'fill' : 'line'} text-xl ${isActive('/voting') ? '' : 'text-gray-600'}`}></i>
          </div>
          <span className="font-medium">Voting</span>
        </Link>

        <div className="h-px bg-gray-200 my-3"></div>

        <Link 
          href="/settings" 
          className={`flex items-center gap-3 p-3 rounded-xl transition-colors ${isActive('/settings') ? 'bg-purple-50 text-purple-600' : 'hover:bg-purple-50 text-gray-900'}`}
        >
          <div className="w-10 h-10 flex items-center justify-center bg-gray-100 rounded-full">
            <i className="ri-settings-3-line text-xl text-gray-600"></i>
          </div>
          <span className="font-medium">설정</span>
        </Link>
      </div>
    </aside>
  );
}
