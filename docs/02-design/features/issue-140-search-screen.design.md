# Design: 검색 화면 구현 (이슈 #140)

> **Plan 참조**: `docs/01-plan/features/issue-140-search-screen.plan.md`
> **Branch**: `feature/140-search-screen`

---

## 1. 컴포넌트 Props 인터페이스

### 1.1 타입 정의

```typescript
// types/search.ts
export interface SearchResult {
  live: Live[];
  news: News[];
}

// types/recentSearch.ts
export interface RecentSearchItem {
  query: string;
  searchedAt: string;
}
```

### 1.2 컴포넌트별 Props

```typescript
// app/search/components/SearchBar.tsx
interface SearchBarProps {
  value: string;
  onChange: (value: string) => void;
  onSearch: (query: string) => void;
  onClear: () => void;
  placeholder?: string;
  minLength?: number;  // 기본값: 2
}

// app/search/components/RecentSearches.tsx
interface RecentSearchesProps {
  searches: string[];
  onSelect: (query: string) => void;
  onRemove: (query: string) => void;
  onClearAll: () => void;
}

// app/search/components/LiveResults.tsx
interface LiveResultsProps {
  lives: Live[];
}

// app/search/components/NewsResults.tsx
interface NewsResultsProps {
  newsList: News[];
}

// app/search/components/EmptyState.tsx
interface EmptyStateProps {
  message?: string;
  icon?: string;
}
```

### 1.3 Custom Hook 인터페이스

```typescript
// hooks/useSearch.ts
interface UseSearchReturn {
  query: string;
  setQuery: (q: string) => void;
  results: SearchResult | null;
  state: AsyncState;
  error: string | null;
  search: (q: string) => Promise<void>;
}

// hooks/useRecentSearches.ts
interface UseRecentSearchesReturn {
  searches: string[];
  addSearch: (query: string) => void;
  removeSearch: (query: string) => void;
  clearAll: () => void;
}
```

---

## 2. 테스트 케이스 상세 명세 (Given-When-Then)

### 2.1 SearchBar.test.tsx

**TC-SEARCH-001: 입력 변경**
```
Given: SearchBar 렌더링됨
When:  "BTS" 입력
Then:  onChange("BTS") 호출
```

**TC-SEARCH-002: 엔터키 검색**
```
Given: value = "BTS" (2자 이상)
When:  Enter 키 입력
Then:  onSearch("BTS") 호출
```

**TC-SEARCH-003: 검색 버튼 클릭**
```
Given: value = "BTS"
When:  검색 버튼 클릭
Then:  onSearch("BTS") 호출
```

**TC-SEARCH-004: 최소 글자 미달**
```
Given: value = "B" (1자)
When:  Enter 키 또는 검색 버튼 클릭
Then:  onSearch 호출 안 됨
       (검색 버튼 비활성화 또는 검증 메시지)
```

**TC-SEARCH-005: 지우기 버튼**
```
Given: value = "BTS" (텍스트 존재)
When:  X 버튼 클릭
Then:  onClear() 호출
       입력 필드 비워짐
```

**TC-SEARCH-006: Auto-focus**
```
Given: 검색 페이지 진입
When:  컴포넌트 마운트
Then:  입력 필드에 자동 포커스
```

### 2.2 RecentSearches.test.tsx

**TC-SEARCH-007: 최근 검색어 목록 표시**
```
Given: searches = ["BTS", "BLACKPINK", "콘서트"]
When:  RecentSearches 렌더링
Then:  - 3개 검색어 칩 표시
       - 각 칩에 시계 아이콘 + 텍스트 + X 버튼
```

**TC-SEARCH-008: 검색어 클릭**
```
Given: searches = ["BTS", "BLACKPINK"]
When:  "BTS" 칩 클릭
Then:  onSelect("BTS") 호출
```

**TC-SEARCH-009: 검색어 개별 삭제**
```
Given: searches = ["BTS", "BLACKPINK"]
When:  "BTS" 칩의 X 버튼 클릭
Then:  onRemove("BTS") 호출
```

**TC-SEARCH-010: 전체 삭제**
```
Given: searches = ["BTS", "BLACKPINK"]
When:  "전체 삭제" 버튼 클릭
Then:  onClearAll() 호출
```

**TC-SEARCH-011: 빈 최근 검색어**
```
Given: searches = []
When:  RecentSearches 렌더링
Then:  최근 검색어 섹션 미표시 (또는 "최근 검색어가 없습니다")
```

### 2.3 LiveResults.test.tsx

**TC-SEARCH-012: 라이브 결과 표시**
```
Given: lives = [2개 Live 객체]
When:  LiveResults 렌더링
Then:  - "라이브" 섹션 헤더
       - 2개 라이브 카드 (썸네일 + 제목 + 아티스트 + 상태배지)
```

**TC-SEARCH-013: 라이브 결과 클릭**
```
Given: 라이브 결과 카드 (id: 5)
When:  클릭
Then:  /live/5 경로로 이동
```

**TC-SEARCH-014: 빈 라이브 결과**
```
Given: lives = []
When:  LiveResults 렌더링
Then:  라이브 섹션 미표시
```

### 2.4 NewsResults.test.tsx

**TC-SEARCH-015: 뉴스 결과 표시**
```
Given: newsList = [2개 News 객체]
When:  NewsResults 렌더링
Then:  - "뉴스" 섹션 헤더
       - 2개 뉴스 카드 (썸네일 + 제목 + 출처 + 날짜)
```

**TC-SEARCH-016: 뉴스 결과 클릭**
```
Given: 뉴스 결과 카드 (id: 3)
When:  클릭
Then:  /news/3 경로로 이동
```

### 2.5 EmptyState.test.tsx

**TC-SEARCH-017: 빈 결과 상태**
```
Given: message = undefined (기본값)
When:  EmptyState 렌더링
Then:  - 검색 아이콘 표시
       - "검색 결과가 없습니다" 기본 메시지
```

**TC-SEARCH-018: 커스텀 메시지**
```
Given: message = "다른 검색어를 시도해보세요"
When:  EmptyState 렌더링
Then:  커스텀 메시지 표시
```

### 2.6 page.test.tsx (검색 페이지)

**TC-SEARCH-019: 초기 상태**
```
Given: 검색 페이지 진입 (검색어 없음)
When:  페이지 렌더링
Then:  - SearchBar 표시 (auto-focus)
       - 최근 검색어 표시
       - 검색 결과 미표시
```

**TC-SEARCH-020: 검색 실행**
```
Given: "BTS" 검색
When:  API 성공 (라이브 2개, 뉴스 3개)
Then:  - LiveResults에 2개 표시
       - NewsResults에 3개 표시
       - "BTS" 최근 검색어에 추가
```

**TC-SEARCH-021: 2자 미만 검색**
```
Given: "B" 입력 (1자)
When:  검색 시도
Then:  - "검색어는 최소 2자 이상 입력해주세요" 메시지
       - API 호출 없음
```

**TC-SEARCH-022: 빈 결과**
```
Given: "xyz123" 검색
When:  API 성공 (라이브 0개, 뉴스 0개)
Then:  EmptyState 표시
```

**TC-SEARCH-023: 에러 상태**
```
Given: 네트워크 에러
When:  검색 실행
Then:  - 에러 메시지 표시
       - 재시도 가능
```

**TC-SEARCH-024: 로딩 상태**
```
Given: 검색 API 호출 중
When:  로딩 중
Then:  - SearchBar 비활성화 또는 로딩 스피너
       - 결과 영역 스켈레톤 표시
```

### 2.7 useRecentSearches.test.ts

**TC-SEARCH-025: localStorage 저장**
```
Given: useRecentSearches hook 사용
When:  addSearch("BTS") 호출
Then:  localStorage에 "BTS" 저장
       searches = ["BTS"]
```

**TC-SEARCH-026: 중복 검색어 처리**
```
Given: searches = ["BTS", "BLACKPINK"]
When:  addSearch("BTS") 호출
Then:  searches = ["BTS", "BLACKPINK"] (순서 갱신, 중복 제거)
```

**TC-SEARCH-027: 최대 10개 제한**
```
Given: searches = [10개 검색어]
When:  addSearch("새 검색어") 호출
Then:  searches.length = 10
       가장 오래된 검색어 제거됨
```

**TC-SEARCH-028: SSR 안전성**
```
Given: typeof window === 'undefined' (서버 환경)
When:  useRecentSearches hook 초기화
Then:  searches = [] (에러 없음)
```

---

## 3. Mock 데이터 샘플

```typescript
// __mocks__/search.ts
export const mockSearchResult: SearchResult = {
  live: [
    {
      id: 1,
      title: 'BTS Fan Meeting Special',
      artistName: 'BTS',
      thumbnailUrl: '/images/mock/live-bts.jpg',
      status: 'LIVE' as LiveStatus,
      viewerCount: 89200,
    },
    {
      id: 10,
      title: 'BTS World Tour Highlights',
      artistName: 'BTS',
      thumbnailUrl: '/images/mock/live-bts-2.jpg',
      status: 'ENDED' as LiveStatus,
      viewerCount: 150000,
    },
  ],
  news: [
    {
      id: 1,
      title: 'BTS 새 앨범 발매 예정',
      summary: 'BTS가 2026년 3월 새 앨범 발매를 예고했다.',
      thumbnailUrl: '/images/mock/news-bts.jpg',
      source: '스포츠조선',
      publishedAt: '2026-02-01T09:00:00Z',
    },
    {
      id: 5,
      title: 'BTS 멤버 진 솔로 앨범 차트 1위',
      summary: 'BTS 진의 솔로 앨범이 빌보드 차트 1위를 기록했다.',
      thumbnailUrl: '/images/mock/news-bts-2.jpg',
      source: '한국경제',
      publishedAt: '2026-01-30T12:00:00Z',
    },
    {
      id: 8,
      title: 'BTS 월드투어 추가 공연 확정',
      summary: 'BTS가 아시아 투어에 서울 추가 공연을 확정했다.',
      thumbnailUrl: '/images/mock/news-bts-3.jpg',
      source: '엔터미디어',
      publishedAt: '2026-01-28T15:30:00Z',
    },
  ],
};

export const mockEmptySearchResult: SearchResult = {
  live: [],
  news: [],
};

export const mockRecentSearches = ['BTS', 'BLACKPINK', '콘서트', 'NewJeans', 'MAMA 2026'];
```

---

## 4. 컴포넌트 렌더링 예시 (HTML 구조)

### 4.1 초기 상태 (검색 전)

```html
<div>
  <!-- SearchBar -->
  <header class="sticky top-0 bg-white border-b z-50">
    <div class="px-4 py-3 flex items-center gap-2">
      <Link href="/" class="lg:hidden">←</Link>
      <div class="flex-1 relative">
        <input
          type="text"
          placeholder="라이브, 뉴스 검색..."
          class="w-full bg-gray-100 rounded-full pl-10 pr-10 py-2.5 text-sm"
          autofocus
        />
        <i class="ri-search-line absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"></i>
      </div>
    </div>
  </header>

  <PageWrapper>
    <!-- RecentSearches -->
    <section class="px-4 py-4">
      <div class="flex items-center justify-between mb-3">
        <h2 class="text-sm font-bold">최근 검색어</h2>
        <button class="text-xs text-gray-500">전체 삭제</button>
      </div>
      <div class="flex flex-wrap gap-2">
        <button class="flex items-center gap-2 bg-gray-100 px-3 py-2 rounded-full text-sm">
          <i class="ri-time-line text-gray-400"></i>
          BTS
          <i class="ri-close-line text-gray-400"></i>
        </button>
        <!-- 추가 검색어 칩들 -->
      </div>
    </section>
  </PageWrapper>
</div>
```

### 4.2 검색 결과

```html
<PageWrapper>
  <!-- LiveResults -->
  <section class="px-4 py-4">
    <h2 class="text-sm font-bold text-gray-900 mb-3">라이브</h2>
    <div class="space-y-3">
      <Link href="/live/1" class="flex gap-3 hover:bg-gray-50 p-2 rounded-xl">
        <div class="relative">
          <img src="..." class="w-28 h-20 rounded-lg object-cover" />
          <StatusBadge status="LIVE" />
        </div>
        <div>
          <h3 class="text-sm font-medium line-clamp-1">BTS Fan Meeting</h3>
          <p class="text-xs text-gray-500">BTS</p>
          <span class="text-xs text-gray-400">89,200명</span>
        </div>
      </Link>
    </div>
  </section>

  <!-- NewsResults -->
  <section class="px-4 py-4 bg-gray-50">
    <h2 class="text-sm font-bold text-gray-900 mb-3">뉴스</h2>
    <div class="space-y-3">
      <Link href="/news/1" class="flex gap-3 p-2 rounded-xl">
        <img src="..." class="w-24 h-20 rounded-lg object-cover" />
        <div>
          <h3 class="text-sm font-medium line-clamp-2">BTS 새 앨범 발매 예정</h3>
          <span class="text-xs text-gray-500">스포츠조선 · 2시간 전</span>
        </div>
      </Link>
    </div>
  </section>
</PageWrapper>
```

### 4.3 빈 결과

```html
<PageWrapper>
  <EmptyState>
    <div class="text-center py-16">
      <i class="ri-search-line text-4xl text-gray-300 mb-4"></i>
      <p class="text-gray-500">검색 결과가 없습니다</p>
      <p class="text-sm text-gray-400 mt-1">다른 검색어를 시도해보세요</p>
    </div>
  </EmptyState>
</PageWrapper>
```

---

## 5. API Response 예시

### 5.1 GET /api/v1/search?q=BTS

```json
{
  "success": true,
  "data": {
    "live": [
      {
        "id": 1,
        "title": "BTS Fan Meeting Special",
        "artistName": "BTS",
        "thumbnailUrl": "https://cdn.fanpulse.app/live/thumb-bts.jpg",
        "status": "LIVE",
        "viewerCount": 89200
      }
    ],
    "news": [
      {
        "id": 1,
        "title": "BTS 새 앨범 발매 예정",
        "summary": "BTS가 2026년 3월 새 앨범 발매를 예고했다.",
        "thumbnailUrl": "https://cdn.fanpulse.app/news/thumb-bts.jpg",
        "source": "스포츠조선",
        "publishedAt": "2026-02-01T09:00:00Z"
      }
    ]
  }
}
```

### 5.2 GET /api/v1/search?q=xyz (빈 결과)

```json
{
  "success": true,
  "data": {
    "live": [],
    "news": []
  }
}
```

### 5.3 GET /api/v1/search?q=B (400 에러)

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "검색어는 최소 2자 이상이어야 합니다"
  }
}
```

---

## 6. localStorage 설계

### Storage Key & 구조

```typescript
// lib/storage/recentSearches.ts
const STORAGE_KEY = 'fanpulse_recent_searches';
const MAX_ITEMS = 10;

// 저장 형식 (JSON 배열)
// ["BTS", "BLACKPINK", "콘서트"]

export function getRecentSearches(): string[] {
  if (typeof window === 'undefined') return [];
  try {
    const stored = localStorage.getItem(STORAGE_KEY);
    return stored ? JSON.parse(stored) : [];
  } catch {
    return [];
  }
}

export function addRecentSearch(query: string): void {
  const searches = getRecentSearches();
  const filtered = searches.filter(s => s !== query);
  const updated = [query, ...filtered].slice(0, MAX_ITEMS);
  localStorage.setItem(STORAGE_KEY, JSON.stringify(updated));
}

export function removeRecentSearch(query: string): void {
  const searches = getRecentSearches();
  const updated = searches.filter(s => s !== query);
  localStorage.setItem(STORAGE_KEY, JSON.stringify(updated));
}

export function clearRecentSearches(): void {
  localStorage.removeItem(STORAGE_KEY);
}
```

---

## 7. 파일 생성 순서 (TDD)

| 순서 | 파일 | 타입 |
|------|------|------|
| 1 | `types/search.ts` | 타입 |
| 2 | `lib/api/search.ts` | API |
| 3 | `lib/storage/recentSearches.ts` | Storage |
| 4 | `lib/storage/recentSearches.test.ts` | Test |
| 5 | `__mocks__/search.ts` | Mock |
| 6 | `app/search/components/SearchBar.test.tsx` | Test |
| 7 | `app/search/components/SearchBar.tsx` | Component |
| 8 | `app/search/components/RecentSearches.test.tsx` | Test |
| 9 | `app/search/components/RecentSearches.tsx` | Component |
| 10 | `app/search/components/EmptyState.test.tsx` | Test |
| 11 | `app/search/components/EmptyState.tsx` | Component |
| 12 | `app/search/components/LiveResults.test.tsx` | Test |
| 13 | `app/search/components/LiveResults.tsx` | Component |
| 14 | `app/search/components/NewsResults.test.tsx` | Test |
| 15 | `app/search/components/NewsResults.tsx` | Component |
| 16 | `hooks/useSearch.test.ts` | Test |
| 17 | `hooks/useSearch.ts` | Hook |
| 18 | `hooks/useRecentSearches.test.ts` | Test |
| 19 | `hooks/useRecentSearches.ts` | Hook |
| 20 | `app/search/page.test.tsx` | Test |
| 21 | `app/search/page.tsx` | Page |

---

**작성일**: 2026-02-01
**문서 버전**: 1.0
