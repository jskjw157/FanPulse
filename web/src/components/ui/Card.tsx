"use client";

import { motion, HTMLMotionProps } from "framer-motion";
import { ReactNode } from "react";

interface CardProps extends HTMLMotionProps<"div"> {
  children: ReactNode;
  hoverEffect?: boolean;
}

export default function Card({ 
  children, 
  className = "", 
  hoverEffect = false,
  ...props 
}: CardProps) {
  return (
    <motion.div
      whileHover={hoverEffect ? { y: -5, transition: { duration: 0.2 } } : undefined}
      className={`bg-white rounded-2xl shadow-sm overflow-hidden ${hoverEffect ? 'hover:shadow-md' : ''} ${className}`}
      {...props}
    >
      {children}
    </motion.div>
  );
}
