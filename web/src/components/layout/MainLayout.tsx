"use client";

import { useState } from "react";
import Header from "./Header";
import Sidebar from "./Sidebar";
import BottomNav from "./BottomNav";
import MobileMenu from "./MobileMenu";

export default function MainLayout({ children }: { children: React.ReactNode }) {
  const [isMenuOpen, setIsMenuOpen] = useState(false);

  return (
    <>
      <Header onMenuClick={() => setIsMenuOpen(true)} />
      <Sidebar />
      <MobileMenu isOpen={isMenuOpen} onClose={() => setIsMenuOpen(false)} />
      
      <main className="pt-16 lg:ml-64 min-h-screen bg-gray-50 pb-16 lg:pb-0">
        {children}
      </main>

      <BottomNav />
    </>
  );
}
