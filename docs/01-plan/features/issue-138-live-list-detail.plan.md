# Plan: 라이브 목록 및 상세 화면 구현 (이슈 #138)

> **Feature**: H006 (라이브 목록) + H019 (라이브 상세)
> **Issue**: #138
> **Branch**: `feature/138-live-list-detail`
> **경로**: `/live`, `/live/:id`

---

## 1. 목표 (Goal)

라이브 스트리밍 목록 조회 및 YouTube iframe을 통한 라이브 시청 화면을 TDD 방식으로 구현

### 주요 기능
- ✅ 라이브 목록 그리드/리스트 뷰
- ✅ 상태 배지 (LIVE/SCHEDULED/ENDED)
- ✅ 무한 스크롤 (Cursor 기반 페이지네이션)
- ✅ Pull-to-refresh
- ✅ YouTube iframe 플레이어 (16:9 비율)
- ✅ 라이브 메타데이터 표시

---

## 2. 기술 스택

### 프레임워크 & 라이브러리
- **Next.js 16.1.1** (App Router)
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
├── live/
│   ├── page.tsx                       # 라이브 목록 페이지
│   ├── page.test.tsx
│   ├── [id]/
│   │   ├── page.tsx                   # 라이브 상세 페이지
│   │   └── page.test.tsx
│   └── components/
│       ├── LiveGrid.tsx               # 그리드 레이아웃
│       ├── LiveGrid.test.tsx
│       ├── LiveListItem.tsx           # 리스트 아이템
│       ├── LiveListItem.test.tsx
│       ├── StatusBadge.tsx            # 상태 배지
│       ├── StatusBadge.test.tsx
│       ├── YouTubePlayer.tsx          # YouTube iframe 플레이어
│       ├── YouTubePlayer.test.tsx
│       ├── LiveMetadata.tsx           # 메타데이터 표시
│       ├── LiveMetadata.test.tsx
│       └── InfiniteScroll.tsx         # 무한 스크롤 wrapper
│           └── InfiniteScroll.test.tsx
```

---

## 4. API 연동

### 4.1 엔드포인트

| API | Method | 용도 | Response |
|-----|--------|------|----------|
| `/api/v1/live?limit=20&cursor={cursor}` | GET | 라이브 목록 조회 (페이지네이션) | `{ items: Live[], nextCursor?, hasMore }` |
| `/api/v1/live/:id` | GET | 라이브 상세 조회 | `{ id, title, artistName, ... }` |

### 4.2 타입 정의

```typescript
// types/live.ts (확장)
export interface LiveDetail extends Live {
  description: string;
  youtubeVideoId: string;
  scheduledAt: string;
  startedAt?: string;
  endedAt?: string;
  viewerCount: number;
}

// types/pagination.ts
export interface InfiniteScrollState<T> {
  items: T[];
  nextCursor?: string;
  hasMore: boolean;
  loading: boolean;
  error?: Error;
}
```

### 4.3 API Client

```typescript
// lib/api/live.ts
export async function fetchLiveList(cursor?: string, limit = 20): Promise<PaginatedResponse<Live>>
export async function fetchLiveDetail(id: string): Promise<LiveDetail>
```

---

## 5. TDD 전략

### 5.1 테스트 케이스

#### Unit Tests (라이브 목록)

**LiveListItem.test.tsx**
- [ ] LIVE 상태 - 빨간 배지 + "LIVE" 텍스트
- [ ] SCHEDULED 상태 - 회색 배지 + "예정" 텍스트
- [ ] ENDED 상태 - 검정 배지 + "종료" 텍스트
- [ ] 썸네일 이미지 렌더링
- [ ] 클릭 시 `/live/:id` 네비게이션

**LiveGrid.test.tsx**
- [ ] 라이브 아이템 렌더링 (그리드 레이아웃)
- [ ] 빈 상태 처리
- [ ] 로딩 상태 - 스켈레톤 표시

**InfiniteScroll.test.tsx**
- [ ] 스크롤 끝 도달 시 onLoadMore 호출
- [ ] 로딩 중에는 중복 호출 방지
- [ ] hasMore=false 시 추가 로딩 중지

#### Unit Tests (라이브 상세)

**YouTubePlayer.test.tsx**
- [ ] iframe src 정확성 검증
- [ ] 16:9 비율 유지
- [ ] allowfullscreen 속성 포함
- [ ] URL 파라미터 (rel=0, modestbranding=1, playsinline=1)

**LiveMetadata.test.tsx**
- [ ] 제목/아티스트 렌더링
- [ ] 설명 텍스트 렌더링
- [ ] 시청자 수 포맷팅 (예: 1,234명)
- [ ] 상태별 텍스트 변화

#### Component Tests

**page.test.tsx (라이브 목록)**
- [ ] 초기 로드 - 20개 아이템 렌더링
- [ ] 무한 스크롤 - 다음 페이지 로드
- [ ] Pull-to-refresh - 리스트 초기화
- [ ] API 에러 시 에러 메시지

**page.test.tsx (라이브 상세)**
- [ ] 라이브 ID로 상세 정보 로드
- [ ] YouTube 플레이어 렌더링
- [ ] 메타데이터 표시
- [ ] 존재하지 않는 ID - 404 에러

#### E2E Tests

**live.spec.ts**
- [ ] 라이브 목록 페이지 진입
- [ ] 무한 스크롤 동작
- [ ] 라이브 클릭 → 상세 페이지 이동
- [ ] YouTube 플레이어 로드 확인
- [ ] 뒤로가기 버튼 동작

---

## 6. 상태 관리

### Custom Hook: `useInfiniteLiveList()`

```typescript
export function useInfiniteLiveList() {
  const [items, setItems] = useState<Live[]>([]);
  const [nextCursor, setNextCursor] = useState<string | undefined>();
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const loadMore = async () => { /* ... */ };
  const refresh = async () => { /* ... */ };

  return { items, loading, error, hasMore, loadMore, refresh };
}
```

### Custom Hook: `useLiveDetail(id: string)`

```typescript
export function useLiveDetail(id: string) {
  const [live, setLive] = useState<LiveDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    fetchLiveDetail(id).then(setLive).catch(setError);
  }, [id]);

  return { live, loading, error };
}
```

---

## 7. YouTube iframe 설정

### iframe 속성

```html
<iframe
  src="https://www.youtube.com/embed/{VIDEO_ID}?rel=0&modestbranding=1&playsinline=1"
  width="100%"
  style="aspect-ratio: 16/9;"
  frameborder="0"
  allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
  allowfullscreen
></iframe>
```

### 반응형 처리

```css
.youtube-player-wrapper {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
  overflow: hidden;
}

.youtube-player-wrapper iframe {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}
```

---

## 8. 무한 스크롤 구현

### Intersection Observer 활용

```typescript
const observerRef = useRef<HTMLDivElement>(null);

useEffect(() => {
  const observer = new IntersectionObserver(
    (entries) => {
      if (entries[0].isIntersecting && hasMore && !loading) {
        loadMore();
      }
    },
    { threshold: 1.0 }
  );

  if (observerRef.current) {
    observer.observe(observerRef.current);
  }

  return () observer.disconnect();
}, [hasMore, loading]);
```

---

## 9. 에러 처리

### 라이브 목록
- **네트워크 에러**: 재시도 버튼
- **빈 결과**: "라이브가 없습니다" 메시지

### 라이브 상세
- **404 에러**: "라이브를 찾을 수 없습니다"
- **YouTube 로드 실패**: Fallback UI

---

## 10. 성능 최적화

- [ ] 이미지 lazy loading
- [ ] 무한 스크롤 throttle/debounce
- [ ] YouTube iframe lazy loading
- [ ] Virtual scrolling (많은 아이템 시)

---

## 11. 구현 순서 (TDD Cycle)

### Phase 1: 타입 & API Client
1. `types/live.ts` 확장
2. `lib/api/live.ts` 구현 및 테스트

### Phase 2: 라이브 목록 컴포넌트
1. `StatusBadge.test.tsx` → `StatusBadge.tsx`
2. `LiveListItem.test.tsx` → `LiveListItem.tsx`
3. `LiveGrid.test.tsx` → `LiveGrid.tsx`
4. `InfiniteScroll.test.tsx` → `InfiniteScroll.tsx`

### Phase 3: 라이브 목록 페이지
1. `useInfiniteLiveList` hook 테스트 및 구현
2. `page.test.tsx` (목록) 작성
3. `page.tsx` (목록) 구현

### Phase 4: 라이브 상세 컴포넌트
1. `YouTubePlayer.test.tsx` → `YouTubePlayer.tsx`
2. `LiveMetadata.test.tsx` → `LiveMetadata.tsx`

### Phase 5: 라이브 상세 페이지
1. `useLiveDetail` hook 테스트 및 구현
2. `page.test.tsx` (상세) 작성
3. `page.tsx` (상세) 구현

### Phase 6: E2E 테스트
1. `live.spec.ts` 작성 및 검증

---

## 12. 완료 조건 (Definition of Done)

- [ ] 모든 Unit 테스트 통과 (커버리지 > 80%)
- [ ] 무한 스크롤 동작 검증
- [ ] YouTube 플레이어 재생 확인
- [ ] 반응형 디자인 검증
- [ ] E2E 테스트 통과
- [ ] 접근성 검증

---

## 13. 의존성 & 블로커

### 의존성
- **API**: `/api/v1/live` 엔드포인트 (Cursor 페이지네이션 지원)
- **Backend**: `youtubeVideoId` 필드 제공

### 블로커
- YouTube API 제한 시 대응 방안 필요

---

## 14. 다음 단계

✅ Plan 완료
⏭️ **Design 단계**: 상세 컴포넌트 설계 및 테스트 케이스 명세

---

**작성일**: 2026-02-01
**작성자**: Claude (AI Assistant)
**문서 버전**: 1.0
