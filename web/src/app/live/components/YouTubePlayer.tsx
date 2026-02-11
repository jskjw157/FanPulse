interface YouTubePlayerProps {
  streamUrl: string;
  title?: string;
}

export default function YouTubePlayer({
  streamUrl,
  title = 'YouTube video player',
}: YouTubePlayerProps) {
  // streamUrl이 이미 완전한 embed URL인 경우 그대로 사용
  // 그렇지 않으면 video ID로 간주하고 embed URL 생성
  const embedUrl = streamUrl.includes('youtube.com/embed')
    ? streamUrl
    : `https://www.youtube.com/embed/${streamUrl}`;

  // YouTube 플레이어 옵션 추가
  const urlWithParams = embedUrl.includes('?')
    ? `${embedUrl}&rel=0&modestbranding=1&playsinline=1`
    : `${embedUrl}?rel=0&modestbranding=1&playsinline=1`;

  return (
    <div className="w-full aspect-video bg-black">
      <iframe
        src={urlWithParams}
        title={title}
        className="w-full h-full"
        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
        allowFullScreen
      />
    </div>
  );
}
