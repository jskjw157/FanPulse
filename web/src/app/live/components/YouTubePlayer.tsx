interface YouTubePlayerProps {
  streamUrl: string;
  title?: string;
}

/**
 * YouTube URL에서 비디오 ID를 추출합니다.
 * 지원하는 형식:
 * - https://www.youtube.com/watch?v=VIDEO_ID
 * - https://youtu.be/VIDEO_ID
 * - https://www.youtube.com/embed/VIDEO_ID
 * - VIDEO_ID (11자리 ID만)
 */
function extractVideoId(url: string): string | null {
  // 이미 embed URL인 경우
  const embedMatch = url.match(/youtube\.com\/embed\/([a-zA-Z0-9_-]{11})/);
  if (embedMatch) return embedMatch[1];

  // watch URL인 경우
  const watchMatch = url.match(/youtube\.com\/watch\?v=([a-zA-Z0-9_-]{11})/);
  if (watchMatch) return watchMatch[1];

  // 짧은 URL인 경우
  const shortMatch = url.match(/youtu\.be\/([a-zA-Z0-9_-]{11})/);
  if (shortMatch) return shortMatch[1];

  // 11자리 비디오 ID만 있는 경우
  const idMatch = url.match(/^[a-zA-Z0-9_-]{11}$/);
  if (idMatch) return url;

  return null;
}

/**
 * URL이 유효한 YouTube URL인지 검증합니다.
 */
function isValidYouTubeUrl(url: string): boolean {
  const videoId = extractVideoId(url);
  return videoId !== null;
}

export default function YouTubePlayer({
  streamUrl,
  title = 'YouTube video player',
}: YouTubePlayerProps) {
  const videoId = extractVideoId(streamUrl);

  // 유효하지 않은 URL인 경우 에러 표시
  if (!videoId) {
    return (
      <div className="w-full aspect-video bg-gray-900 flex items-center justify-center">
        <p className="text-gray-400">유효하지 않은 영상 URL입니다</p>
      </div>
    );
  }

  // 안전한 embed URL 생성 (검증된 videoId만 사용)
  const embedUrl = `https://www.youtube.com/embed/${videoId}?rel=0&modestbranding=1&playsinline=1`;

  return (
    <div className="w-full aspect-video bg-black">
      <iframe
        src={embedUrl}
        title={title}
        className="w-full h-full"
        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
        allowFullScreen
      />
    </div>
  );
}
