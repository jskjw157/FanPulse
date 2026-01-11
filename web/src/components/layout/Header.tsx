"use client";

import Link from "next/link";

interface HeaderProps {
  onMenuClick: () => void;
}

export default function Header({ onMenuClick }: HeaderProps) {
  return (
    <header className="fixed top-0 left-0 right-0 bg-white border-b border-gray-200 z-50 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 lg:px-8">
        <div className="flex items-center justify-between h-16 lg:h-16">
          {/* Logo */}
          <Link href="/" className="flex items-center">
            <h1 className="text-xl md:text-2xl font-bold bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent font-pacifico">
              FanPulse
            </h1>
          </Link>

          {/* Desktop Navigation */}
          <nav className="hidden lg:flex items-center gap-1">
            <Link href="/" className="px-4 py-2 rounded-lg bg-purple-50 text-purple-600 font-medium">
              Home
            </Link>
            <Link href="/community" className="px-4 py-2 rounded-lg text-gray-700 hover:bg-gray-50 font-medium">
              Community
            </Link>
            <Link href="/live" className="px-4 py-2 rounded-lg text-gray-700 hover:bg-gray-50 font-medium">
              Live
            </Link>
            <Link href="/voting" className="px-4 py-2 rounded-lg text-gray-700 hover:bg-gray-50 font-medium">
              Voting
            </Link>
            <Link href="/chart" className="px-4 py-2 rounded-lg text-gray-700 hover:bg-gray-50 font-medium">
              Chart
            </Link>
            <Link href="/concert" className="px-4 py-2 rounded-lg text-gray-700 hover:bg-gray-50 font-medium">
              Concert
            </Link>
          </nav>

          {/* Right Actions */}
          <div className="flex items-center gap-2">
            <Link href="/search" className="w-9 h-9 lg:w-10 lg:h-10 flex items-center justify-center rounded-lg hover:bg-gray-100">
              <i className="ri-search-line text-xl text-gray-700"></i>
            </Link>
            <Link href="/notifications" className="w-9 h-9 lg:w-10 lg:h-10 flex items-center justify-center rounded-lg hover:bg-gray-100 relative">
              <i className="ri-notification-line text-xl text-gray-700"></i>
              <span className="absolute top-1.5 right-1.5 lg:top-2 lg:right-2 w-2 h-2 bg-red-500 rounded-full"></span>
            </Link>
            <Link href="/mypage" className="hidden lg:flex w-10 h-10 items-center justify-center rounded-lg hover:bg-gray-100">
              <i className="ri-user-line text-xl text-gray-700"></i>
            </Link>
            <button 
              onClick={onMenuClick}
              className="w-9 h-9 flex items-center justify-center lg:hidden"
            >
              <i className="ri-menu-line text-xl text-gray-700"></i>
            </button>
          </div>
        </div>
      </div>
    </header>
  );
}
