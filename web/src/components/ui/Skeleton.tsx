"use client";

import { motion } from "framer-motion";

interface SkeletonProps {
  className?: string;
  variant?: "rectangle" | "circle";
}

export default function Skeleton({ className = "", variant = "rectangle" }: SkeletonProps) {
  return (
    <motion.div
      animate={{
        opacity: [0.5, 1, 0.5],
      }}
      transition={{
        duration: 1.5,
        repeat: Infinity,
        ease: "easeInOut",
      }}
      className={`bg-gray-200 ${variant === "circle" ? "rounded-full" : "rounded-md"} ${className}`}
    />
  );
}
