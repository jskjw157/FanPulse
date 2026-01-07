"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

export default function BottomNav() {
  const pathname = usePathname();

  const isActive = (path: string) => {
    if (path === '/' && pathname === '/') return true;
    if (path !== '/' && pathname?.startsWith(path)) return true;
    return false;
  };

  const navItems = [
    { href: '/', icon: 'ri-home-5-line', activeIcon: 'ri-home-5-fill', label: 'Home' },
    { href: '/community', icon: 'ri-chat-3-line', activeIcon: 'ri-chat-3-fill', label: 'Community' },
    { href: '/live', icon: 'ri-live-line', activeIcon: 'ri-live-fill', label: 'Live' },
    { href: '/voting', icon: 'ri-trophy-line', activeIcon: 'ri-trophy-fill', label: 'Voting' },
    { href: '/mypage', icon: 'ri-user-line', activeIcon: 'ri-user-fill', label: 'My' },
  ];

  return (
    <nav className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 z-50 lg:hidden pb-[env(safe-area-inset-bottom)]">
      <div className="grid grid-cols-5 h-16">
        {navItems.map((item) => (
          <Link 
            key={item.href} 
            href={item.href}
            className={`flex flex-col items-center justify-center ${
              isActive(item.href) ? 'text-purple-600' : 'text-gray-500'
            }`}
          >
            <i className={`${isActive(item.href) ? item.activeIcon : item.icon} text-xl`}></i>
            <span className={`text-xs mt-1 ${isActive(item.href) ? 'font-medium' : ''}`}>{item.label}</span>
          </Link>
        ))}
      </div>
    </nav>
  );
}
