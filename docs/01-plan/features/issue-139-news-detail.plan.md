# Plan: 뉴스 상세 화면 구현 (이슈 #139)

> **Feature**: H011 - 뉴스 상세
> **Issue**: #139
> **Branch**: `feature/139-news-detail`
> **경로**: `/news/:id`

---

## 1. 목표 (Goal)

뉴스 상세 페이지를 통해 뉴스 본문 및 메타데이터를 표시하고 원문 링크를 제공하는 화면을 TDD 방식으로 구현

### 주요 기능
- ✅ 뉴스 제목/썸네일 표시
- ✅ 출처 및 게시일 표시
- ✅ 본문 내용 렌더링
- ✅ 원문 링크 버튼
- ✅ 로딩/에러 상태 처리
- ✅ 404 에러 처리 (존재하지 않는 뉴스)

---

## 2. 기술 스택

### 프레임워크 & 라이브러리
- **Next.js 16.1.1** (App Router, Dynamic Routes)
- **React 19.2.3**
- **TypeScript 5**
- **Tailwind CSS 4**
- **Axios 1.13.3**

### 테스트
- **Vitest 4.0.16**
- **@testing-library/react 16.3.1**
- **Playwright 1.58.0**

---

## 3. 컴포넌트 구조

```
web/src/app/
├── news/
│   └── [id]/
│       ├── page.tsx                   # 뉴스 상세 페이지
│       ├── page.test.tsx
│       └── components/
│           ├── NewsHeader.tsx         # 제목/썸네일
│           ├── NewsHeader.test.tsx
│           ├── NewsMetadata.tsx       # 출처/날짜
│           ├── NewsMetadata.test.tsx
│           ├── NewsContent.tsx        # 본문
│           ├── NewsContent.test.tsx
│           └── SourceLink.tsx         # 원문 링크 버튼
│               └── SourceLink.test.tsx
```

---

## 4. API 연동

### 4.1 엔드포인트

| API | Method | 용도 | Response |
|-----|--------|------|----------|
| `/api/v1/news/:id` | GET | 뉴스 상세 조회 | `{ id, title, content, ... }` |

### 4.2 타입 정의

```typescript
// types/news.ts (확장)
export interface NewsDetail extends News {
  content: string;         // 본문 (HTML 또는 Markdown)
  sourceUrl: string;       // 원문 링크
  author?: string;         // 작성자 (선택)
  tags?: string[];         // 태그 (선택)
}

// types/error.ts
export interface NewsNotFoundError {
  code: 'NOT_FOUND';
  message: string;
}
```

### 4.3 API Client

```typescript
// lib/api/news.ts
export async function fetchNewsDetail(id: string): Promise<NewsDetail>
```

---

## 5. TDD 전략

### 5.1 테스트 케이스

#### Unit Tests

**NewsHeader.test.tsx**
- [ ] 제목 렌더링
- [ ] 썸네일 이미지 렌더링
- [ ] 썸네일 없을 시 기본 이미지 표시

**NewsMetadata.test.tsx**
- [ ] 출처 표시
- [ ] 게시일 포맷팅 (예: "2024년 1월 1일")
- [ ] 작성자 표시 (선택)

**NewsContent.test.tsx**
- [ ] 본문 HTML 렌더링
- [ ] Markdown 렌더링 (필요 시)
- [ ] 이미지 lazy loading
- [ ] 외부 링크 target="_blank"

**SourceLink.test.tsx**
- [ ] 원문 링크 버튼 렌더링
- [ ] 클릭 시 새 탭 열림 (target="_blank")
- [ ] rel="noopener noreferrer" 속성

#### Component Tests

**page.test.tsx**
- [ ] 로딩 상태 - 스켈레톤 표시
- [ ] 성공 상태 - 뉴스 상세 정보 렌더링
- [ ] 404 에러 - "뉴스를 찾을 수 없습니다" 메시지
- [ ] 네트워크 에러 - 재시도 버튼

#### E2E Tests

**news-detail.spec.ts**
- [ ] 뉴스 상세 페이지 진입
- [ ] 제목/본문/메타데이터 표시 확인
- [ ] 원문 링크 클릭 (새 탭 열림)
- [ ] 존재하지 않는 뉴스 ID → 404 페이지
- [ ] 뒤로가기 버튼 동작

---

## 6. 상태 관리

### Custom Hook: `useNewsDetail(id: string)`

```typescript
export function useNewsDetail(id: string) {
  const [news, setNews] = useState<NewsDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    fetchNewsDetail(id)
      .then(setNews)
      .catch(setError)
      .finally(() => setLoading(false));
  }, [id]);

  return { news, loading, error };
}
```

---

## 7. 본문 렌더링

### HTML Content

```typescript
// DOMPurify로 XSS 방지
import DOMPurify from 'isomorphic-dompurify';

function NewsContent({ content }: { content: string }) {
  const sanitizedContent = DOMPurify.sanitize(content);
  return <div dangerouslySetInnerHTML={{ __html: sanitizedContent }} />;
}
```

### Markdown (선택)

```typescript
// react-markdown 사용 (필요 시)
import ReactMarkdown from 'react-markdown';

function NewsContent({ content }: { content: string }) {
  return <ReactMarkdown>{content}</ReactMarkdown>;
}
```

---

## 8. 에러 처리

### 404 Not Found
```typescript
if (error?.code === 'NOT_FOUND') {
  return (
    <div className="text-center py-12">
      <h2>뉴스를 찾을 수 없습니다</h2>
      <Button onClick={() => router.push('/')}>홈으로</Button>
    </div>
  );
}
```

### 네트워크 에러
```typescript
if (error) {
  return (
    <div className="text-center py-12">
      <p>뉴스를 불러오는 중 오류가 발생했습니다</p>
      <Button onClick={retry}>다시 시도</Button>
    </div>
  );
}
```

---

## 9. SEO 최적화

### Metadata 설정

```typescript
// app/news/[id]/page.tsx
export async function generateMetadata({ params }: { params: { id: string } }) {
  const news = await fetchNewsDetail(params.id);

  return {
    title: news.title,
    description: news.summary,
    openGraph: {
      title: news.title,
      description: news.summary,
      images: [news.thumbnailUrl],
    },
  };
}
```

---

## 10. 접근성 (a11y)

- [ ] 시맨틱 HTML (`<article>`, `<header>`, `<footer>`)
- [ ] 이미지 alt 텍스트
- [ ] 원문 링크 명확한 레이블 ("원문 보기" 버튼)

---

## 11. 구현 순서 (TDD Cycle)

### Phase 1: 타입 & API Client
1. `types/news.ts` 확장
2. `lib/api/news.ts` 구현 및 테스트

### Phase 2: 컴포넌트
1. `NewsHeader.test.tsx` → `NewsHeader.tsx`
2. `NewsMetadata.test.tsx` → `NewsMetadata.tsx`
3. `NewsContent.test.tsx` → `NewsContent.tsx`
4. `SourceLink.test.tsx` → `SourceLink.tsx`

### Phase 3: 페이지 통합
1. `useNewsDetail` hook 테스트 및 구현
2. `page.test.tsx` 작성
3. `page.tsx` 구현

### Phase 4: E2E 테스트
1. `news-detail.spec.ts` 작성 및 검증

---

## 12. 완료 조건 (Definition of Done)

- [ ] 모든 Unit 테스트 통과 (커버리지 > 80%)
- [ ] 404 에러 처리 검증
- [ ] XSS 방지 검증 (DOMPurify)
- [ ] SEO 메타데이터 확인
- [ ] E2E 테스트 통과
- [ ] 접근성 검증

---

## 13. 의존성 & 블로커

### 의존성
- **API**: `/api/v1/news/:id` 엔드포인트
- **Backend**: `content`, `sourceUrl` 필드 제공

### 보안 고려사항
- HTML 본문 XSS 방지 필수 (DOMPurify)

---

## 14. 다음 단계

✅ Plan 완료
⏭️ **Design 단계**: 상세 컴포넌트 설계 및 테스트 케이스 명세

---

**작성일**: 2026-02-01
**작성자**: Claude (AI Assistant)
**문서 버전**: 1.0
