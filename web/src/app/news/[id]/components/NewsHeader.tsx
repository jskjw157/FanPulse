import Image from 'next/image';

interface NewsHeaderProps {
  title: string;
  thumbnailUrl: string;
}

export default function NewsHeader({ title, thumbnailUrl }: NewsHeaderProps) {
  return (
    <>
      {thumbnailUrl && (
        <div className="w-full">
          <Image
            src={thumbnailUrl}
            alt={title}
            width={768}
            height={432}
            sizes="100vw"
            className="w-full h-56 object-cover"
            priority
          />
        </div>
      )}
      <div className="px-4 pt-4">
        <h1 className="text-xl font-bold text-gray-900 leading-tight">
          {title}
        </h1>
      </div>
    </>
  );
}
