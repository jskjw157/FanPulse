interface SourceLinkProps {
  url: string;
  sourceName: string;
}

export default function SourceLink({ url, sourceName }: SourceLinkProps) {
  return (
    <div className="px-4 pb-4">
      <a
        href={url}
        target="_blank"
        rel="noopener noreferrer"
        className="flex items-center justify-center gap-2 w-full py-3 bg-gray-100 rounded-xl text-sm font-medium text-gray-700 hover:bg-gray-200 transition-colors"
      >
        <i className="ri-external-link-line" />
        {sourceName}에서 원문 보기
      </a>
    </div>
  );
}
