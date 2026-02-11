interface YouTubePlayerProps {
  streamUrl: string;
  title?: string;
}

export default function YouTubePlayer({ streamUrl, title }: YouTubePlayerProps) {
  return (
    <div className="w-full aspect-video bg-black">
      <iframe
        src={streamUrl}
        title={title || 'YouTube video player'}
        className="w-full h-full"
        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
        allowFullScreen
      />
    </div>
  );
}
