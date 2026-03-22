# Design: 뉴스 상세 화면 구현 (이슈 #139)

> **Plan 참조**: `docs/01-plan/features/issue-139-news-detail.plan.md`
> **Branch**: `feature/139-news-detail`

---

## 1. 컴포넌트 Props 인터페이스

### 1.1 타입 정의

```typescript
// types/news.ts (확장)
export interface News {
  id: number;
  title: string;
  summary: string;
  thumbnailUrl: string;
  source: string;
  publishedAt: string;
}

export interface NewsDetail extends News {
  content: string;         // HTML 형식 본문
  sourceUrl: string;       // 원문 링크
  author?: string;
}
```

### 1.2 컴포넌트별 Props

```typescript
// app/news/[id]/components/NewsHeader.tsx
interface NewsHeaderProps {
  title: string;
  thumbnailUrl: string;
}

// app/news/[id]/components/NewsMetadata.tsx
interface NewsMetadataProps {
  source: string;
  publishedAt: string;
  author?: string;
}

// app/news/[id]/components/NewsContent.tsx
interface NewsContentProps {
  content: string;  // HTML
}

// app/news/[id]/components/SourceLink.tsx
interface SourceLinkProps {
  url: string;
  sourceName: string;
}
```

### 1.3 Custom Hook 인터페이스

```typescript
// hooks/useNewsDetail.ts
interface UseNewsDetailReturn {
  news: NewsDetail | null;
  state: AsyncState;
  error: string | null;
  retry: () => void;
}
```

---

## 2. 테스트 케이스 상세 명세 (Given-When-Then)

### 2.1 NewsHeader.test.tsx

**TC-NEWS-001: 헤더 렌더링**
```
Given: title = "BTS 새 앨범 발매 예정", thumbnailUrl = "/mock.jpg"
When:  NewsHeader 렌더링
Then:  - <h1> 태그에 제목 표시
       - <img> 태그에 썸네일 이미지 표시 (16:9 비율)
       - alt 텍스트에 제목 설정
```

**TC-NEWS-002: 썸네일 없을 때**
```
Given: title = "제목", thumbnailUrl = "" (빈 문자열)
When:  NewsHeader 렌더링
Then:  - 제목만 표시
       - 이미지 영역 미표시 (또는 기본 이미지)
```

### 2.2 NewsMetadata.test.tsx

**TC-NEWS-003: 메타데이터 렌더링**
```
Given: source = "스포츠조선", publishedAt = "2026-02-01T09:00:00Z"
When:  NewsMetadata 렌더링
Then:  - "스포츠조선" 표시
       - "2026년 2월 1일" 포맷으로 표시 (한국 로케일)
```

**TC-NEWS-004: 작성자 포함**
```
Given: source, publishedAt, author = "김기자"
When:  NewsMetadata 렌더링
Then:  - "스포츠조선 · 김기자" 표시
       - 날짜 표시
```

**TC-NEWS-005: 상대적 시간 표시**
```
Given: publishedAt = 2시간 전
When:  NewsMetadata 렌더링
Then:  "2시간 전" 표시
```

### 2.3 NewsContent.test.tsx

**TC-NEWS-006: HTML 본문 렌더링**
```
Given: content = "<p>BTS가 새 앨범을 발표합니다.</p><p>상세 내용...</p>"
When:  NewsContent 렌더링
Then:  - HTML이 안전하게 렌더링됨
       - <p> 태그 스타일 적용 (여백)
```

**TC-NEWS-007: XSS 방지**
```
Given: content = '<script>alert("xss")</script><p>본문</p>'
When:  NewsContent 렌더링
Then:  - <script> 태그 제거됨
       - <p>본문</p>만 렌더링됨
```

**TC-NEWS-008: 이미지 포함 본문**
```
Given: content에 <img> 태그 포함
When:  NewsContent 렌더링
Then:  - 이미지가 lazy loading으로 렌더링
       - 최대 너비 100% 제한
```

### 2.4 SourceLink.test.tsx

**TC-NEWS-009: 원문 링크 버튼**
```
Given: url = "https://sports.chosun.com/article/123", sourceName = "스포츠조선"
When:  SourceLink 렌더링
Then:  - "스포츠조선에서 원문 보기" 텍스트
       - target="_blank" 속성
       - rel="noopener noreferrer" 속성
```

**TC-NEWS-010: 링크 클릭**
```
Given: SourceLink 렌더링됨
When:  버튼 클릭
Then:  - 새 탭에서 원문 URL 열림
       - 현재 페이지 유지
```

### 2.5 page.test.tsx (뉴스 상세)

**TC-NEWS-011: 로딩 상태**
```
Given: API 호출 중
When:  /news/1 진입
Then:  - 스켈레톤 UI 표시 (제목 + 본문 영역)
```

**TC-NEWS-012: 성공 상태**
```
Given: API 성공 (NewsDetail 반환)
When:  /news/1 진입
Then:  - NewsHeader (제목/썸네일)
       - NewsMetadata (출처/날짜)
       - NewsContent (본문)
       - SourceLink (원문 링크)
       순서로 렌더링
```

**TC-NEWS-013: 404 에러**
```
Given: API 404 응답
When:  /news/999 진입
Then:  - "뉴스를 찾을 수 없습니다" 메시지
       - "홈으로" 버튼
```

**TC-NEWS-014: 네트워크 에러**
```
Given: 네트워크 연결 실패
When:  /news/1 진입
Then:  - "뉴스를 불러올 수 없습니다" 메시지
       - "다시 시도" 버튼 (retry 호출)
```

---

## 3. Mock 데이터 샘플

```typescript
// __mocks__/news.ts
export const mockNewsDetail: NewsDetail = {
  id: 1,
  title: 'BTS 새 앨범 발매 예정',
  summary: 'BTS가 2026년 3월 새 앨범 발매를 예고했다.',
  thumbnailUrl: '/images/mock/news-detail-1.jpg',
  source: '스포츠조선',
  publishedAt: '2026-02-01T09:00:00Z',
  content: `
    <p>BTS가 2026년 3월 새 앨범 발매를 예고했다. 멤버들의 솔로 활동 이후 첫 완전체 앨범으로 팬들의 기대를 모으고 있다.</p>
    <p>소속사 하이브에 따르면, 이번 앨범은 전 멤버가 참여한 자작곡으로 구성되며, 월드투어도 함께 진행될 예정이다.</p>
    <img src="/images/mock/news-content.jpg" alt="BTS 앨범 커버" />
    <p>BTS는 "팬들에게 새로운 모습을 보여드리고 싶다"고 전했다.</p>
  `,
  sourceUrl: 'https://sports.chosun.com/article/bts-2026',
  author: '김기자',
};

export const mockNewsNotFound = {
  success: false,
  error: {
    code: 'NOT_FOUND',
    message: '뉴스를 찾을 수 없습니다',
  },
};
```

---

## 4. 컴포넌트 렌더링 예시 (HTML 구조)

### 4.1 뉴스 상세 페이지

```html
<article class="min-h-screen bg-white pb-20">
  <!-- 헤더 (뒤로가기) -->
  <header class="sticky top-0 z-50 bg-white border-b px-4 py-3">
    <button onClick={router.back}>
      <i class="ri-arrow-left-line"></i>
    </button>
    <span class="font-bold">뉴스</span>
  </header>

  <!-- NewsHeader -->
  <div class="w-full">
    <img
      src="/images/mock/news-detail-1.jpg"
      alt="BTS 새 앨범 발매 예정"
      class="w-full h-56 object-cover"
    />
  </div>

  <div class="px-4 py-4">
    <!-- 제목 -->
    <h1 class="text-xl font-bold text-gray-900 leading-tight">
      BTS 새 앨범 발매 예정
    </h1>

    <!-- NewsMetadata -->
    <div class="flex items-center gap-2 mt-3 text-sm text-gray-500">
      <span>스포츠조선</span>
      <span>·</span>
      <span>김기자</span>
      <span>·</span>
      <time datetime="2026-02-01">2시간 전</time>
    </div>

    <hr class="my-4 border-gray-100" />

    <!-- NewsContent -->
    <div class="prose prose-sm max-w-none text-gray-700 leading-relaxed">
      <p>BTS가 2026년 3월 새 앨범 발매를 예고했다...</p>
      <p>소속사 하이브에 따르면...</p>
      <img src="..." alt="..." class="w-full rounded-lg" loading="lazy" />
      <p>BTS는 "팬들에게..."</p>
    </div>

    <hr class="my-4 border-gray-100" />

    <!-- SourceLink -->
    <a
      href="https://sports.chosun.com/article/bts-2026"
      target="_blank"
      rel="noopener noreferrer"
      class="flex items-center justify-center gap-2 w-full py-3 bg-gray-100 rounded-xl text-sm font-medium text-gray-700 hover:bg-gray-200"
    >
      <i class="ri-external-link-line"></i>
      스포츠조선에서 원문 보기
    </a>
  </div>
</article>
```

### 4.2 로딩 상태

```html
<article>
  <header>...</header>
  <Skeleton class="w-full h-56" />
  <div class="px-4 py-4">
    <Skeleton class="h-8 w-3/4 mb-3" />
    <Skeleton class="h-4 w-1/2 mb-6" />
    <Skeleton class="h-4 w-full mb-2" count={8} />
  </div>
</article>
```

---

## 5. API Response 예시

### 5.1 GET /api/v1/news/1

```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "BTS 새 앨범 발매 예정",
    "summary": "BTS가 2026년 3월 새 앨범 발매를 예고했다.",
    "thumbnailUrl": "https://cdn.fanpulse.app/news/thumb-1.jpg",
    "source": "스포츠조선",
    "publishedAt": "2026-02-01T09:00:00Z",
    "content": "<p>BTS가 2026년 3월 새 앨범 발매를 예고했다...</p>",
    "sourceUrl": "https://sports.chosun.com/article/bts-2026",
    "author": "김기자"
  }
}
```

### 5.2 GET /api/v1/news/999 (404)

```json
{
  "success": false,
  "error": {
    "code": "NOT_FOUND",
    "message": "뉴스를 찾을 수 없습니다"
  }
}
```

---

## 6. 유틸리티 함수

### 날짜 포맷팅

```typescript
// lib/utils/date.ts
export function formatRelativeTime(dateString: string): string {
  const date = new Date(dateString);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffMin = Math.floor(diffMs / 60000);
  const diffHour = Math.floor(diffMin / 60);
  const diffDay = Math.floor(diffHour / 24);

  if (diffMin < 1) return '방금 전';
  if (diffMin < 60) return `${diffMin}분 전`;
  if (diffHour < 24) return `${diffHour}시간 전`;
  if (diffDay < 7) return `${diffDay}일 전`;

  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  }).format(date);
}
```

### HTML 정제

```typescript
// lib/utils/sanitize.ts
import DOMPurify from 'isomorphic-dompurify';

export function sanitizeHtml(html: string): string {
  return DOMPurify.sanitize(html, {
    ALLOWED_TAGS: ['p', 'br', 'strong', 'em', 'a', 'img', 'h2', 'h3', 'ul', 'ol', 'li', 'blockquote'],
    ALLOWED_ATTR: ['href', 'src', 'alt', 'target', 'rel', 'loading', 'class'],
  });
}
```

---

## 7. 파일 생성 순서 (TDD)

| 순서 | 파일 | 타입 |
|------|------|------|
| 1 | `types/news.ts` (확장) | 타입 |
| 2 | `lib/api/news.ts` | API |
| 3 | `lib/utils/date.ts` | 유틸 |
| 4 | `lib/utils/sanitize.ts` | 유틸 |
| 5 | `__mocks__/news.ts` (확장) | Mock |
| 6 | `app/news/[id]/components/NewsHeader.test.tsx` | Test |
| 7 | `app/news/[id]/components/NewsHeader.tsx` | Component |
| 8 | `app/news/[id]/components/NewsMetadata.test.tsx` | Test |
| 9 | `app/news/[id]/components/NewsMetadata.tsx` | Component |
| 10 | `app/news/[id]/components/NewsContent.test.tsx` | Test |
| 11 | `app/news/[id]/components/NewsContent.tsx` | Component |
| 12 | `app/news/[id]/components/SourceLink.test.tsx` | Test |
| 13 | `app/news/[id]/components/SourceLink.tsx` | Component |
| 14 | `hooks/useNewsDetail.test.ts` | Test |
| 15 | `hooks/useNewsDetail.ts` | Hook |
| 16 | `app/news/[id]/page.test.tsx` | Test |
| 17 | `app/news/[id]/page.tsx` | Page |

---

**작성일**: 2026-02-01
**문서 버전**: 1.0
