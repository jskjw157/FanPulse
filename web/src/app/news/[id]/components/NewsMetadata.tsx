import { formatRelativeTime } from '@/lib/utils/format';

interface NewsMetadataProps {
  source: string;
  publishedAt: string;
  author?: string;
}

export default function NewsMetadata({
  source,
  publishedAt,
  author,
}: NewsMetadataProps) {
  return (
    <div className="flex items-center gap-2 mt-3 text-sm text-gray-500 px-4">
      <span>{source}</span>
      {author && (
        <>
          <span>·</span>
          <span>{author}</span>
        </>
      )}
      <span>·</span>
      <time dateTime={publishedAt}>{formatRelativeTime(publishedAt)}</time>
    </div>
  );
}
