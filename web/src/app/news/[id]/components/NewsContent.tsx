'use client';

import { sanitizeHtml } from '@/lib/utils/sanitize';

interface NewsContentProps {
  content: string;
}

export default function NewsContent({ content }: NewsContentProps) {
  const sanitizedContent = sanitizeHtml(content);

  return (
    <div
      className="prose prose-sm max-w-none text-gray-700 leading-relaxed px-4 py-4 [&_img]:w-full [&_img]:rounded-lg [&_img]:my-4"
      dangerouslySetInnerHTML={{ __html: sanitizedContent }}
    />
  );
}
