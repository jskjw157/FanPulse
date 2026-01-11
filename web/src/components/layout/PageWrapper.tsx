"use client";

import { ReactNode } from "react";

interface PageWrapperProps {
  children: ReactNode;
  className?: string;
  hasHeader?: boolean;
  hasBottomNav?: boolean;
}

export default function PageWrapper({ 
  children, 
  className = "", 
  hasHeader = true,
  hasBottomNav = true
}: PageWrapperProps) {
  return (
    <div className={`
      ${hasHeader ? 'pt-16' : ''} 
      ${hasBottomNav ? 'pb-20 lg:pb-0' : ''} 
      ${className}
    `}>
      {children}
    </div>
  );
}
