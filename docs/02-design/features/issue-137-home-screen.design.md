# Design: 홈 화면 구현 (이슈 #137)

> **Plan 참조**: `docs/01-plan/features/issue-137-home-screen.plan.md`
> **Branch**: `feature/137-home-screen`

---

## 1. 컴포넌트 Props 인터페이스

### 1.1 공통 타입

```typescript
// types/live.ts
export type LiveStatus = 'LIVE' | 'SCHEDULED' | 'ENDED';

export interface Live {
  id: number;
  title: string;
  artistName: string;
  thumbnailUrl: string;
  status: LiveStatus;
  scheduledAt?: string;
  viewerCount?: number;
}

// types/news.ts
export interface News {
  id: number;
  title: string;
  summary: string;
  thumbnailUrl: string;
  source: string;
  publishedAt: string;
}

// types/api.ts
export interface ApiResponse<T> {
  success: boolean;
  data: T;
}

export interface PaginatedResponse<T> {
  items: T[];
  nextCursor?: string;
  hasMore: boolean;
}

// types/common.ts
export type AsyncState = 'idle' | 'loading' | 'success' | 'error';
```

### 1.2 컴포넌트별 Props

```typescript
// components/home/LiveCard.tsx
interface LiveCardProps {
  live: Live;
}

// components/home/NewsCard.tsx
interface NewsCardProps {
  news: News;
}

// components/home/LiveNowSection.tsx
interface LiveSectionProps {
  title: string;
  lives: Live[];
  state: AsyncState;
  error?: string;
  onRetry?: () => void;
}

// components/home/LatestNewsSection.tsx
interface NewsSectionProps {
  news: News[];
  state: AsyncState;
  error?: string;
  onRetry?: () => void;
}
```

### 1.3 Custom Hook 인터페이스

```typescript
// hooks/useHomeSections.ts
interface UseHomeSectionsReturn {
  liveNow: Live[];
  upcoming: Live[];
  latestNews: News[];
  state: AsyncState;
  error: string | null;
  refresh: () => Promise<void>;
}
```

---

## 2. 테스트 케이스 상세 명세 (Given-When-Then)

### 2.1 LiveCard.test.tsx

**TC-HOME-001: LIVE 상태 카드 렌더링**
```
Given: Live 객체 (status: 'LIVE', viewerCount: 24583)
When:  LiveCard 컴포넌트를 렌더링
Then:  - 빨간 LIVE 배지가 표시됨
       - 썸네일 이미지가 표시됨
       - 제목/아티스트명이 표시됨
       - 시청자 수가 "24,583" 포맷으로 표시됨
```

**TC-HOME-002: SCHEDULED 상태 카드 렌더링**
```
Given: Live 객체 (status: 'SCHEDULED', scheduledAt: '2026-02-15T14:00:00Z')
When:  LiveCard 컴포넌트를 렌더링
Then:  - "예정" 배지가 표시됨 (회색)
       - 예정 시간이 표시됨
       - 시청자 수 미표시
```

**TC-HOME-003: 카드 클릭 네비게이션**
```
Given: LiveCard가 렌더링된 상태
When:  카드를 클릭
Then:  /live/{id} 경로로 네비게이션됨
```

### 2.2 NewsCard.test.tsx

**TC-HOME-004: 뉴스 카드 렌더링**
```
Given: News 객체 (title, summary, source, publishedAt)
When:  NewsCard 컴포넌트를 렌더링
Then:  - 썸네일 이미지가 표시됨
       - 제목이 1줄 말줄임으로 표시됨
       - 요약이 2줄 말줄임으로 표시됨
       - 출처/날짜가 표시됨
```

**TC-HOME-005: 뉴스 카드 클릭 네비게이션**
```
Given: NewsCard가 렌더링된 상태
When:  카드를 클릭
Then:  /news/{id} 경로로 네비게이션됨
```

### 2.3 LiveNowSection.test.tsx

**TC-HOME-006: 로딩 상태**
```
Given: state = 'loading'
When:  LiveNowSection 렌더링
Then:  SkeletonCard 5개가 표시됨
```

**TC-HOME-007: 성공 상태**
```
Given: state = 'success', lives = [5개 Live 객체]
When:  LiveNowSection 렌더링
Then:  LiveCard 5개가 가로 스크롤로 표시됨
```

**TC-HOME-008: 에러 상태**
```
Given: state = 'error', error = '네트워크 오류'
When:  LiveNowSection 렌더링
Then:  에러 메시지와 "다시 시도" 버튼이 표시됨
```

**TC-HOME-009: 빈 상태**
```
Given: state = 'success', lives = []
When:  LiveNowSection 렌더링
Then:  "현재 라이브 방송이 없습니다" 메시지 표시
```

**TC-HOME-010: 재시도 버튼 동작**
```
Given: 에러 상태에서 "다시 시도" 버튼이 표시됨
When:  "다시 시도" 버튼 클릭
Then:  onRetry 콜백이 호출됨
```

### 2.4 LatestNewsSection.test.tsx

**TC-HOME-011: 뉴스 섹션 로딩 상태**
```
Given: state = 'loading'
When:  LatestNewsSection 렌더링
Then:  SkeletonCard 3개가 표시됨
```

**TC-HOME-012: 뉴스 섹션 성공 상태**
```
Given: state = 'success', news = [10개 News 객체]
When:  LatestNewsSection 렌더링
Then:  NewsCard 10개가 세로 리스트로 표시됨
```

### 2.5 page.test.tsx (통합)

**TC-HOME-013: 홈 페이지 전체 렌더링**
```
Given: API 호출 성공 (3개 엔드포인트)
When:  홈 페이지 렌더링
Then:  - "Live Now" 섹션 표시
       - "Upcoming" 섹션 표시
       - "최신 뉴스" 섹션 표시
```

**TC-HOME-014: API 병렬 호출**
```
Given: 홈 페이지 진입
When:  useHomeSections hook 실행
Then:  3개 API가 Promise.all로 동시 호출됨
```

**TC-HOME-015: Pull-to-refresh**
```
Given: 홈 페이지가 렌더링된 상태
When:  pull-to-refresh 트리거 (모바일) 또는 새로고침 버튼 클릭
Then:  refresh() 호출 → 모든 데이터 재로드
```

---

## 3. Mock 데이터 샘플

### 3.1 Live Mock 데이터

```typescript
// __mocks__/live.ts
export const mockLiveNow: Live[] = [
  {
    id: 1,
    title: 'NewJeans 컴백 쇼케이스',
    artistName: 'NewJeans Official',
    thumbnailUrl: '/images/mock/live-1.jpg',
    status: 'LIVE',
    viewerCount: 24583,
  },
  {
    id: 2,
    title: 'BTS Fan Meeting Special',
    artistName: 'BTS',
    thumbnailUrl: '/images/mock/live-2.jpg',
    status: 'LIVE',
    viewerCount: 89200,
  },
  {
    id: 3,
    title: 'BLACKPINK Behind The Scenes',
    artistName: 'BLACKPINK',
    thumbnailUrl: '/images/mock/live-3.jpg',
    status: 'LIVE',
    viewerCount: 67800,
  },
];

export const mockUpcoming: Live[] = [
  {
    id: 10,
    title: 'SEVENTEEN Dance Practice',
    artistName: 'SEVENTEEN',
    thumbnailUrl: '/images/mock/upcoming-1.jpg',
    status: 'SCHEDULED',
    scheduledAt: '2026-02-15T14:00:00Z',
  },
  {
    id: 11,
    title: 'Stray Kids World Tour Highlights',
    artistName: 'Stray Kids',
    thumbnailUrl: '/images/mock/upcoming-2.jpg',
    status: 'SCHEDULED',
    scheduledAt: '2026-02-16T10:00:00Z',
  },
];
```

### 3.2 News Mock 데이터

```typescript
// __mocks__/news.ts
export const mockLatestNews: News[] = [
  {
    id: 1,
    title: 'BTS 새 앨범 발매 예정',
    summary: 'BTS가 2026년 3월 새 앨범 발매를 예고했다. 멤버들의 솔로 활동 이후 첫 완전체 앨범으로...',
    thumbnailUrl: '/images/mock/news-1.jpg',
    source: '스포츠조선',
    publishedAt: '2026-02-01T09:00:00Z',
  },
  {
    id: 2,
    title: 'BLACKPINK 월드투어 추가 공연',
    summary: 'BLACKPINK가 아시아 투어에 서울 추가 공연을 확정했다...',
    thumbnailUrl: '/images/mock/news-2.jpg',
    source: '엔터미디어',
    publishedAt: '2026-01-31T15:30:00Z',
  },
  {
    id: 3,
    title: 'NewJeans 신곡 음원차트 1위',
    summary: 'NewJeans의 신곡이 발매 즉시 주요 음원차트 1위를 석권했다...',
    thumbnailUrl: '/images/mock/news-3.jpg',
    source: '한국경제',
    publishedAt: '2026-01-30T12:00:00Z',
  },
];
```

### 3.3 API Response Mock

```typescript
// __mocks__/api.ts
export const mockLiveNowResponse: ApiResponse<PaginatedResponse<Live>> = {
  success: true,
  data: {
    items: mockLiveNow,
    hasMore: false,
  },
};

export const mockUpcomingResponse: ApiResponse<PaginatedResponse<Live>> = {
  success: true,
  data: {
    items: mockUpcoming,
    hasMore: true,
    nextCursor: 'eyJpZCI6MTF9',
  },
};

export const mockNewsResponse: ApiResponse<PaginatedResponse<News>> = {
  success: true,
  data: {
    items: mockLatestNews,
    hasMore: true,
    nextCursor: 'eyJpZCI6M30',
  },
};
```

---

## 4. 컴포넌트 렌더링 예시 (HTML 구조)

### 4.1 홈 페이지 전체 구조

```html
<main>
  <!-- Live Now 섹션 -->
  <section aria-label="Live Now">
    <div class="flex items-center justify-between px-4 py-3">
      <h2 class="text-lg font-bold">Live Now</h2>
      <Link href="/live">더보기</Link>
    </div>
    <div class="flex overflow-x-auto gap-4 px-4 scrollbar-hide">
      <!-- LiveCard × 5 -->
      <article class="flex-shrink-0 w-[280px]">
        <Link href="/live/1">
          <div class="relative">
            <img src="..." alt="..." class="w-full h-40 object-cover rounded-2xl" />
            <Badge variant="danger">● LIVE</Badge>
            <span class="viewer-count">24,583명</span>
          </div>
          <div class="p-3">
            <h3 class="font-bold line-clamp-1">NewJeans 컴백 쇼케이스</h3>
            <p class="text-sm text-gray-500">NewJeans Official</p>
          </div>
        </Link>
      </article>
    </div>
  </section>

  <!-- Upcoming 섹션 -->
  <section aria-label="Upcoming">
    <h2>Upcoming</h2>
    <div class="flex overflow-x-auto gap-4 px-4">
      <!-- LiveCard × 5 (status: SCHEDULED) -->
    </div>
  </section>

  <!-- 최신 뉴스 섹션 -->
  <section aria-label="최신 뉴스">
    <h2>최신 뉴스</h2>
    <div class="space-y-4 px-4">
      <!-- NewsCard × 10 -->
      <article>
        <Link href="/news/1">
          <div class="flex gap-3">
            <img src="..." class="w-24 h-20 rounded-lg object-cover" />
            <div>
              <h3 class="font-medium line-clamp-1">BTS 새 앨범 발매 예정</h3>
              <p class="text-sm text-gray-600 line-clamp-2">요약...</p>
              <span class="text-xs text-gray-400">스포츠조선 · 2시간 전</span>
            </div>
          </div>
        </Link>
      </article>
    </div>
  </section>
</main>
```

### 4.2 로딩 상태

```html
<section aria-label="Live Now">
  <h2>Live Now</h2>
  <div class="flex overflow-x-auto gap-4 px-4">
    <SkeletonCard width="280px" height="200px" />
    <SkeletonCard width="280px" height="200px" />
    <SkeletonCard width="280px" height="200px" />
  </div>
</section>
```

### 4.3 에러 상태

```html
<section aria-label="Live Now">
  <h2>Live Now</h2>
  <div class="text-center py-8">
    <p class="text-gray-500">데이터를 불러올 수 없습니다</p>
    <Button onClick={onRetry}>다시 시도</Button>
  </div>
</section>
```

---

## 5. API Response 예시

### 5.1 GET /api/v1/live?status=LIVE&limit=5

```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": 1,
        "title": "NewJeans 컴백 쇼케이스",
        "artistName": "NewJeans Official",
        "thumbnailUrl": "https://cdn.fanpulse.app/live/thumb-1.jpg",
        "status": "LIVE",
        "scheduledAt": "2026-02-01T14:00:00Z",
        "viewerCount": 24583
      }
    ],
    "nextCursor": null,
    "hasMore": false
  }
}
```

### 5.2 GET /api/v1/news?limit=10

```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": 1,
        "title": "BTS 새 앨범 발매 예정",
        "summary": "BTS가 2026년 3월 새 앨범 발매를 예고했다...",
        "thumbnailUrl": "https://cdn.fanpulse.app/news/thumb-1.jpg",
        "source": "스포츠조선",
        "publishedAt": "2026-02-01T09:00:00Z"
      }
    ],
    "nextCursor": "eyJpZCI6MTB9",
    "hasMore": true
  }
}
```

---

## 6. API Client 설계

```typescript
// lib/api/client.ts
import axios from 'axios';

const apiClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
});

// lib/api/home.ts
import { apiClient } from './client';

export async function fetchLiveNow(limit = 5): Promise<PaginatedResponse<Live>> {
  const { data } = await apiClient.get('/api/v1/live', {
    params: { status: 'LIVE', limit },
  });
  return data.data;
}

export async function fetchUpcoming(limit = 5): Promise<PaginatedResponse<Live>> {
  const { data } = await apiClient.get('/api/v1/live', {
    params: { status: 'SCHEDULED', limit },
  });
  return data.data;
}

export async function fetchLatestNews(limit = 10): Promise<PaginatedResponse<News>> {
  const { data } = await apiClient.get('/api/v1/news', {
    params: { limit },
  });
  return data.data;
}
```

---

## 7. 파일 생성 순서 (TDD)

| 순서 | 파일 | 타입 |
|------|------|------|
| 1 | `types/live.ts` | 타입 |
| 2 | `types/news.ts` | 타입 |
| 3 | `types/api.ts` | 타입 |
| 4 | `lib/api/client.ts` | API |
| 5 | `lib/api/home.ts` | API |
| 6 | `__mocks__/live.ts` | Mock |
| 7 | `__mocks__/news.ts` | Mock |
| 8 | `components/home/LiveCard.test.tsx` | Test |
| 9 | `components/home/LiveCard.tsx` | Component |
| 10 | `components/home/NewsCard.test.tsx` | Test |
| 11 | `components/home/NewsCard.tsx` | Component |
| 12 | `components/home/LiveNowSection.test.tsx` | Test |
| 13 | `components/home/LiveNowSection.tsx` | Component |
| 14 | `components/home/UpcomingSection.test.tsx` | Test |
| 15 | `components/home/UpcomingSection.tsx` | Component |
| 16 | `components/home/LatestNewsSection.test.tsx` | Test |
| 17 | `components/home/LatestNewsSection.tsx` | Component |
| 18 | `hooks/useHomeSections.test.ts` | Test |
| 19 | `hooks/useHomeSections.ts` | Hook |
| 20 | `app/page.test.tsx` | Test |
| 21 | `app/page.tsx` | Page |

---

**작성일**: 2026-02-01
**문서 버전**: 1.0
