# Design: 라이브 목록 및 상세 화면 구현 (이슈 #138)

> **Plan 참조**: `docs/01-plan/features/issue-138-live-list-detail.plan.md`
> **Branch**: `feature/138-live-list-detail`

---

## 1. 컴포넌트 Props 인터페이스

### 1.1 타입 정의 (확장)

```typescript
// types/live.ts (이슈 #137 공유)
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

export interface LiveDetail extends Live {
  description: string;
  youtubeVideoId: string;
  startedAt?: string;
  endedAt?: string;
}
```

### 1.2 컴포넌트별 Props

```typescript
// components/live/StatusBadge.tsx
interface StatusBadgeProps {
  status: LiveStatus;
}

// components/live/LiveListItem.tsx
interface LiveListItemProps {
  live: Live;
}

// components/live/LiveGrid.tsx
interface LiveGridProps {
  lives: Live[];
  state: AsyncState;
  error?: string;
  onRetry?: () => void;
}

// components/live/InfiniteScroll.tsx
interface InfiniteScrollProps {
  children: ReactNode;
  hasMore: boolean;
  loading: boolean;
  onLoadMore: () => void;
}

// components/live/YouTubePlayer.tsx
interface YouTubePlayerProps {
  videoId: string;
  title?: string;
}

// components/live/LiveMetadata.tsx
interface LiveMetadataProps {
  live: LiveDetail;
}
```

### 1.3 Custom Hook 인터페이스

```typescript
// hooks/useInfiniteLiveList.ts
interface UseInfiniteLiveListReturn {
  items: Live[];
  state: AsyncState;
  error: string | null;
  hasMore: boolean;
  loadMore: () => Promise<void>;
  refresh: () => Promise<void>;
}

// hooks/useLiveDetail.ts
interface UseLiveDetailReturn {
  live: LiveDetail | null;
  state: AsyncState;
  error: string | null;
}
```

---

## 2. 테스트 케이스 상세 명세 (Given-When-Then)

### 2.1 StatusBadge.test.tsx

**TC-LIVE-001: LIVE 상태 배지**
```
Given: status = 'LIVE'
When:  StatusBadge 렌더링
Then:  - 빨간색 배지 표시
       - "● LIVE" 텍스트
       - animate-pulse 클래스 적용
```

**TC-LIVE-002: SCHEDULED 상태 배지**
```
Given: status = 'SCHEDULED'
When:  StatusBadge 렌더링
Then:  - 회색 배지 표시
       - "예정" 텍스트
```

**TC-LIVE-003: ENDED 상태 배지**
```
Given: status = 'ENDED'
When:  StatusBadge 렌더링
Then:  - 검정 배지 표시
       - "종료" 텍스트
```

### 2.2 LiveListItem.test.tsx

**TC-LIVE-004: 리스트 아이템 렌더링**
```
Given: Live 객체 (LIVE 상태)
When:  LiveListItem 렌더링
Then:  - 썸네일 이미지
       - LIVE 배지
       - 제목 (1줄 말줄임)
       - 아티스트명
       - 시청자 수
```

**TC-LIVE-005: ENDED 아이템 렌더링**
```
Given: Live 객체 (ENDED 상태)
When:  LiveListItem 렌더링
Then:  - 썸네일 이미지 (그레이스케일 필터 없음)
       - ENDED 배지
       - 제목/아티스트명
```

**TC-LIVE-006: 클릭 네비게이션**
```
Given: LiveListItem (id: 5)
When:  클릭
Then:  /live/5 경로로 이동
```

### 2.3 LiveGrid.test.tsx

**TC-LIVE-007: 그리드 레이아웃**
```
Given: lives = [6개 Live 객체], state = 'success'
When:  LiveGrid 렌더링
Then:  - 모바일: 1열 그리드
       - 태블릿: 2열 그리드
       - 데스크톱: 3열 그리드
       - 6개 LiveListItem 렌더링
```

**TC-LIVE-008: 로딩 상태**
```
Given: state = 'loading'
When:  LiveGrid 렌더링
Then:  SkeletonCard 6개 표시
```

**TC-LIVE-009: 빈 상태**
```
Given: state = 'success', lives = []
When:  LiveGrid 렌더링
Then:  "라이브가 없습니다" 메시지 표시
```

### 2.4 InfiniteScroll.test.tsx

**TC-LIVE-010: 스크롤 끝 도달**
```
Given: hasMore = true, loading = false
When:  sentinel 요소가 viewport에 진입 (IntersectionObserver)
Then:  onLoadMore 콜백 호출
```

**TC-LIVE-011: 로딩 중 중복 호출 방지**
```
Given: hasMore = true, loading = true
When:  sentinel 요소가 viewport에 진입
Then:  onLoadMore 호출 안 됨
```

**TC-LIVE-012: 더 이상 데이터 없음**
```
Given: hasMore = false
When:  스크롤 끝 도달
Then:  onLoadMore 호출 안 됨
       "모든 라이브를 확인했습니다" 메시지 표시
```

### 2.5 YouTubePlayer.test.tsx

**TC-LIVE-013: iframe 렌더링**
```
Given: videoId = 'dQw4w9WgXcQ'
When:  YouTubePlayer 렌더링
Then:  - iframe src = "https://www.youtube.com/embed/dQw4w9WgXcQ?rel=0&modestbranding=1&playsinline=1"
       - allowfullscreen 속성 존재
       - allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
       - aspect-ratio: 16/9
```

**TC-LIVE-014: 16:9 비율 유지**
```
Given: YouTubePlayer 렌더링됨
When:  다양한 화면 크기
Then:  aspect-ratio: 16/9 유지
```

### 2.6 LiveMetadata.test.tsx

**TC-LIVE-015: 메타데이터 표시**
```
Given: LiveDetail 객체 (viewerCount: 24583)
When:  LiveMetadata 렌더링
Then:  - 제목 표시
       - 아티스트명 표시
       - 설명 텍스트 표시
       - "24,583명 시청 중" 포맷
```

### 2.7 라이브 목록 page.test.tsx

**TC-LIVE-016: 목록 초기 로드**
```
Given: API 성공 (20개 아이템)
When:  라이브 목록 페이지 진입
Then:  - PageHeader "Live Now" 표시
       - LiveGrid에 20개 아이템 렌더링
       - 무한 스크롤 sentinel 표시
```

**TC-LIVE-017: 무한 스크롤 페이지 로드**
```
Given: 초기 20개 아이템 로드됨, hasMore = true
When:  스크롤 끝 도달
Then:  - 추가 20개 아이템 로드
       - 총 40개 아이템 표시
```

**TC-LIVE-018: Pull-to-refresh**
```
Given: 40개 아이템 표시됨
When:  refresh 실행
Then:  - 리스트 초기화
       - 첫 페이지부터 다시 로드
```

### 2.8 라이브 상세 page.test.tsx

**TC-LIVE-019: 상세 페이지 렌더링**
```
Given: API 성공 (LiveDetail 객체)
When:  /live/1 페이지 진입
Then:  - YouTubePlayer 렌더링 (videoId 기반)
       - LiveMetadata 렌더링
       - 뒤로가기 버튼 표시
```

**TC-LIVE-020: 404 에러**
```
Given: API 404 응답 (존재하지 않는 ID)
When:  /live/999 페이지 진입
Then:  "라이브를 찾을 수 없습니다" 메시지
       홈으로 이동 버튼
```

---

## 3. Mock 데이터 샘플

```typescript
// __mocks__/live.ts
export const mockLiveList: Live[] = Array.from({ length: 20 }, (_, i) => ({
  id: i + 1,
  title: `라이브 방송 ${i + 1}`,
  artistName: ['NewJeans', 'BTS', 'BLACKPINK', 'SEVENTEEN', 'Stray Kids'][i % 5],
  thumbnailUrl: `/images/mock/live-${i + 1}.jpg`,
  status: i < 3 ? 'LIVE' : i < 8 ? 'SCHEDULED' : 'ENDED' as LiveStatus,
  scheduledAt: `2026-02-${String(i + 1).padStart(2, '0')}T14:00:00Z`,
  viewerCount: Math.floor(Math.random() * 100000),
}));

export const mockLiveDetail: LiveDetail = {
  id: 1,
  title: 'NewJeans 컴백 쇼케이스',
  artistName: 'NewJeans Official',
  thumbnailUrl: '/images/mock/live-1.jpg',
  status: 'LIVE',
  description: 'NewJeans의 새 앨범 "How Sweet" 컴백 쇼케이스 라이브 방송입니다.',
  youtubeVideoId: 'dQw4w9WgXcQ',
  scheduledAt: '2026-02-01T14:00:00Z',
  startedAt: '2026-02-01T14:00:00Z',
  viewerCount: 24583,
};
```

---

## 4. 컴포넌트 렌더링 예시 (HTML 구조)

### 4.1 라이브 목록 페이지

```html
<PageHeader title="Live Now" />
<PageWrapper>
  <div class="max-w-7xl mx-auto px-4 lg:px-8 py-6">
    <motion.div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <!-- LiveListItem × N -->
      <article>
        <Link href="/live/1">
          <div class="relative bg-white rounded-2xl overflow-hidden shadow-sm hover:shadow-md">
            <img src="..." alt="..." class="w-full h-48 object-cover" />
            <StatusBadge status="LIVE" />  <!-- 절대 위치 -->
            <div class="p-4">
              <h3 class="font-bold line-clamp-1">NewJeans 컴백 쇼케이스</h3>
              <p class="text-sm text-gray-500">NewJeans Official</p>
              <span class="text-xs text-gray-400">👁 24,583</span>
            </div>
          </div>
        </Link>
      </article>
    </motion.div>

    <!-- InfiniteScroll sentinel -->
    <InfiniteScroll hasMore={true} loading={false} onLoadMore={loadMore}>
      <div ref={sentinelRef} class="h-4" />
    </InfiniteScroll>
  </div>
</PageWrapper>
```

### 4.2 라이브 상세 페이지

```html
<div class="min-h-screen bg-white">
  <!-- 뒤로가기 헤더 -->
  <header class="sticky top-0 z-50 bg-white border-b px-4 py-3">
    <button onClick={router.back}>← 뒤로</button>
  </header>

  <!-- YouTube 플레이어 -->
  <YouTubePlayer videoId="dQw4w9WgXcQ" title="NewJeans 컴백 쇼케이스" />

  <!-- 메타데이터 -->
  <LiveMetadata live={liveDetail} />
    <div class="px-4 py-4">
      <h1 class="text-xl font-bold">NewJeans 컴백 쇼케이스</h1>
      <p class="text-sm text-gray-500 mt-1">NewJeans Official</p>
      <p class="text-sm text-purple-600 mt-1">24,583명 시청 중</p>
      <p class="text-sm text-gray-700 mt-4">설명...</p>
    </div>
</div>
```

---

## 5. API Response 예시

### 5.1 GET /api/v1/live?limit=20

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
    "nextCursor": "eyJpZCI6MjB9",
    "hasMore": true
  }
}
```

### 5.2 GET /api/v1/live/1

```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "NewJeans 컴백 쇼케이스",
    "artistName": "NewJeans Official",
    "thumbnailUrl": "https://cdn.fanpulse.app/live/thumb-1.jpg",
    "status": "LIVE",
    "description": "NewJeans의 새 앨범 컴백 쇼케이스 라이브 방송입니다.",
    "youtubeVideoId": "dQw4w9WgXcQ",
    "scheduledAt": "2026-02-01T14:00:00Z",
    "startedAt": "2026-02-01T14:00:00Z",
    "viewerCount": 24583
  }
}
```

### 5.3 GET /api/v1/live/999 (404)

```json
{
  "success": false,
  "error": {
    "code": "NOT_FOUND",
    "message": "라이브를 찾을 수 없습니다"
  }
}
```

---

## 6. 파일 생성 순서 (TDD)

| 순서 | 파일 | 타입 |
|------|------|------|
| 1 | `types/live.ts` (확장) | 타입 |
| 2 | `lib/api/live.ts` | API |
| 3 | `__mocks__/live.ts` (확장) | Mock |
| 4 | `app/live/components/StatusBadge.test.tsx` | Test |
| 5 | `app/live/components/StatusBadge.tsx` | Component |
| 6 | `app/live/components/LiveListItem.test.tsx` | Test |
| 7 | `app/live/components/LiveListItem.tsx` | Component |
| 8 | `app/live/components/InfiniteScroll.test.tsx` | Test |
| 9 | `app/live/components/InfiniteScroll.tsx` | Component |
| 10 | `app/live/components/LiveGrid.test.tsx` | Test |
| 11 | `app/live/components/LiveGrid.tsx` | Component |
| 12 | `hooks/useInfiniteLiveList.test.ts` | Test |
| 13 | `hooks/useInfiniteLiveList.ts` | Hook |
| 14 | `app/live/page.test.tsx` | Test |
| 15 | `app/live/page.tsx` | Page |
| 16 | `app/live/components/YouTubePlayer.test.tsx` | Test |
| 17 | `app/live/components/YouTubePlayer.tsx` | Component |
| 18 | `app/live/components/LiveMetadata.test.tsx` | Test |
| 19 | `app/live/components/LiveMetadata.tsx` | Component |
| 20 | `hooks/useLiveDetail.test.ts` | Test |
| 21 | `hooks/useLiveDetail.ts` | Hook |
| 22 | `app/live/[id]/page.test.tsx` | Test |
| 23 | `app/live/[id]/page.tsx` | Page |

---

**작성일**: 2026-02-01
**문서 버전**: 1.0
