import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

export default function PostCreate() {
  const navigate = useNavigate();
  const [content, setContent] = useState('');
  const [tags, setTags] = useState<string[]>([]);
  const [tagInput, setTagInput] = useState('');
  const [selectedArtist, setSelectedArtist] = useState('');

  const artists = ['BTS', 'BLACKPINK', 'SEVENTEEN', 'NewJeans', 'Stray Kids', 'TWICE'];

  const handleAddTag = () => {
    if (tagInput.trim() && tags.length < 5) {
      setTags([...tags, tagInput.trim()]);
      setTagInput('');
    }
  };

  const handleRemoveTag = (index: number) => {
    setTags(tags.filter((_, i) => i !== index));
  };

  const handlePost = () => {
    if (content.trim() && selectedArtist) {
      // 게시글 작성 로직
      navigate('/community');
    }
  };

  return (
    <div className="min-h-screen bg-white">
      {/* Header */}
      <header className="fixed top-0 left-0 right-0 bg-white border-b border-gray-200 z-50">
        <div className="px-4 py-3 flex items-center justify-between">
          <Link to="/community" className="text-sm text-gray-600">취소</Link>
          <h1 className="text-base font-bold text-gray-900">게시글 작성</h1>
          <button 
            onClick={handlePost}
            disabled={!content.trim() || !selectedArtist}
            className={`text-sm font-medium ${
              content.trim() && selectedArtist 
                ? 'text-purple-600' 
                : 'text-gray-400'
            }`}
          >
            게시
          </button>
        </div>
      </header>

      {/* Content */}
      <div className="pt-14 px-4 pb-6">
        {/* Artist Selection */}
        <div className="py-4">
          <label className="text-sm font-bold text-gray-900 mb-3 block">
            아티스트 선택 *
          </label>
          <div className="flex flex-wrap gap-2">
            {artists.map(artist => (
              <button
                key={artist}
                onClick={() => setSelectedArtist(artist)}
                className={`px-4 py-2 rounded-full text-sm font-medium transition-colors ${
                  selectedArtist === artist
                    ? 'bg-gradient-to-r from-purple-600 to-pink-600 text-white'
                    : 'bg-gray-100 text-gray-700'
                }`}
              >
                {artist}
              </button>
            ))}
          </div>
        </div>

        {/* Content Input */}
        <div className="py-4">
          <label className="text-sm font-bold text-gray-900 mb-3 block">
            내용 *
          </label>
          <textarea
            value={content}
            onChange={(e) => setContent(e.target.value)}
            placeholder="팬 여러분과 공유하고 싶은 이야기를 작성해주세요..."
            maxLength={500}
            className="w-full h-48 bg-gray-50 rounded-2xl px-4 py-3 text-sm border-none focus:outline-none focus:ring-2 focus:ring-purple-600 resize-none"
          />
          <div className="flex justify-end mt-2">
            <span className="text-xs text-gray-500">{content.length}/500</span>
          </div>
        </div>

        {/* Image Upload */}
        <div className="py-4">
          <label className="text-sm font-bold text-gray-900 mb-3 block">
            이미지 첨부
          </label>
          <button className="w-full h-32 bg-gray-50 rounded-2xl border-2 border-dashed border-gray-300 flex flex-col items-center justify-center gap-2 text-gray-500">
            <i className="ri-image-add-line text-3xl"></i>
            <span className="text-sm">이미지 추가 (최대 5장)</span>
          </button>
        </div>

        {/* Tags */}
        <div className="py-4">
          <label className="text-sm font-bold text-gray-900 mb-3 block">
            태그 (최대 5개)
          </label>
          <div className="flex gap-2 mb-3">
            <input
              type="text"
              value={tagInput}
              onChange={(e) => setTagInput(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && handleAddTag()}
              placeholder="태그 입력 후 엔터"
              className="flex-1 bg-gray-50 rounded-full px-4 py-2.5 text-sm border-none focus:outline-none focus:ring-2 focus:ring-purple-600"
            />
            <button
              onClick={handleAddTag}
              disabled={tags.length >= 5}
              className="px-4 py-2.5 bg-purple-600 text-white rounded-full text-sm font-medium disabled:bg-gray-300"
            >
              추가
            </button>
          </div>
          {tags.length > 0 && (
            <div className="flex flex-wrap gap-2">
              {tags.map((tag, index) => (
                <div
                  key={index}
                  className="bg-purple-50 text-purple-600 px-3 py-1.5 rounded-full text-sm flex items-center gap-2"
                >
                  <span>#{tag}</span>
                  <button
                    onClick={() => handleRemoveTag(index)}
                    className="w-4 h-4 flex items-center justify-center"
                  >
                    <i className="ri-close-line text-sm"></i>
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Guidelines */}
        <div className="mt-6 bg-purple-50 rounded-2xl p-4">
          <div className="flex items-start gap-2">
            <i className="ri-information-line text-purple-600 text-lg flex-shrink-0 mt-0.5"></i>
            <div>
              <h3 className="text-sm font-bold text-purple-900 mb-2">게시글 작성 가이드</h3>
              <ul className="text-xs text-purple-700 space-y-1">
                <li>• 타인을 존중하는 내용을 작성해주세요</li>
                <li>• 욕설, 비방, 허위사실은 삭제될 수 있습니다</li>
                <li>• 저작권을 침해하는 콘텐츠는 게시할 수 없습니다</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
