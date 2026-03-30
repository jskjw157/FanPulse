# Plan: 홈 화면 구현 (이슈 #137)

> **Feature**: H001 - 메인(홈) 화면
> **Issue**: #137
> **Branch**: `feature/137-home-screen`
> **경로**: `/` (root)

---

## 1. 목표 (Goal)

FanPulse MVP의 메인 진입점으로, Live/News 요약 정보를 제공하고 주요 섹션으로 네비게이션을 제공하는 홈 화면을 TDD 방식으로 구현

### 주요 기능
- ✅ Live Now/Upcoming 섹션 (각 최대 5개)
- ✅ 최신 뉴스 섹션 (최대 10개)
- ✅ Pull-to-refresh (모바일 대응)
- ✅ 로딩/에러/빈 상태 처리
- ✅ 라이브/뉴스 카드 클릭 네비게이션

---

## 2. 기술 스택

### 프레임워크 & 라이브러리
- **Next.js 16.1.1** (App Router)
- **React 19.2.3**
- **TypeScript 5**
- **Tailwind CSS 4**
- **Axios 1.13.3** (API 통신)
- **i18next** (국제화, 필요 시)

### 테스트
- **Vitest 4.0.16** (Component/Unit 테스트)
- **@testing-library/react 16.3.1** (컴포넌트 테스트)
- **Playwright 1.58.0** (E2E 테스트)

---

## 3. 컴포넌트 구조

```
web/src/app/
├── page.tsx                    # 홈 페이지 (Server Component)
├── page.test.tsx               # 홈 페이지 테스트
└── components/
    └── home/
        ├── LiveNowSection.tsx          # Live Now 섹션
        ├── LiveNowSection.test.tsx
        ├── UpcomingSection.tsx         # Upcoming 섹션
        ├── UpcomingSection.test.tsx
        ├── LatestNewsSection.tsx       # 최신 뉴스 섹션
        ├── LatestNewsSection.test.tsx
        ├── LiveCard.tsx                # 라이브 카드 컴포넌트
        ├── LiveCard.test.tsx
        ├── NewsCard.tsx                # 뉴스 카드 컴포넌트
        ├── NewsCard.test.tsx
        └── PullToRefresh.tsx           # Pull-to-refresh wrapper
            └── PullToRefresh.test.tsx
```

### 재사용 가능 컴포넌트 (기존)
- `components/ui/Card.tsx` - 카드 베이스 컴포넌트
- `components/ui/Badge.tsx` - 상태 배지 (LIVE/SCHEDULED)
- `components/ui/Skeleton.tsx` - 로딩 스켈레톤
- `components/layout/MainLayout.tsx` - 레이아웃

---

## 4. API 연동

### 4.1 엔드포인트

| API | Method | 용도 | Response |
|-----|--------|------|----------|
| `/api/v1/live?status=LIVE&limit=5` | GET | Live Now 조회 | `{ items: Live[], nextCursor?, hasMore }` |
| `/api/v1/live?status=SCHEDULED&limit=5` | GET | Upcoming 조회 | `{ items: Live[], nextCursor?, hasMore }` |
| `/api/v1/news?limit=10` | GET | 최신 뉴스 조회 | `{ items: News[], nextCursor?, hasMore }` |

### 4.2 타입 정의

```typescript
// types/live.ts
export type LiveStatus = 'LIVE' | 'SCHEDULED' | 'ENDED';

export interface Live {
  id: string;
  title: string;
  artistName: string;
  thumbnailUrl: string;
  status: LiveStatus;
  scheduledAt?: string;
  viewerCount?: number;
}

// types/news.ts
export interface News {
  id: string;
  title: string;
  summary: string;
  thumbnailUrl: string;
  source: string;
  publishedAt: string;
}

// types/api.ts
export interface PaginatedResponse<T> {
  items: T[];
  nextCursor?: string;
  hasMore: boolean;
}
```

### 4.3 API Client

```typescript
// lib/api/home.ts
export async function fetchLiveNow(): Promise<PaginatedResponse<Live>>
export async function fetchUpcoming(): Promise<PaginatedResponse<Live>>
export async function fetchLatestNews(): Promise<PaginatedResponse<News>>
```

---

## 5. TDD 전략

### 5.1 테스트 계층

```
E2E (Playwright)
    ↓
Integration (Vitest + React Testing Library)
    ↓
Unit (Vitest)
```

### 5.2 테스트 케이스 (Red → Green → Refactor)

#### Unit Tests (Vitest)

**LiveCard.test.tsx**
- [ ] LIVE 상태 배지 표시
- [ ] SCHEDULED 상태 배지 표시
- [ ] 썸네일 이미지 렌더링
- [ ] 제목/아티스트 텍스트 렌더링
- [ ] 클릭 시 `/live/:id` 네비게이션

**NewsCard.test.tsx**
- [ ] 썸네일 이미지 렌더링
- [ ] 제목/요약 텍스트 렌더링
- [ ] 출처/날짜 표시
- [ ] 클릭 시 `/news/:id` 네비게이션

**PullToRefresh.test.tsx**
- [ ] 당겨서 새로고침 동작 시뮬레이션
- [ ] 로딩 인디케이터 표시
- [ ] onRefresh 콜백 호출

#### Component Tests (Vitest + React Testing Library)

**LiveNowSection.test.tsx**
- [ ] 로딩 상태 - 스켈레톤 표시
- [ ] 성공 상태 - 라이브 카드 5개 렌더링
- [ ] 에러 상태 - 에러 메시지 표시
- [ ] 빈 상태 - 빈 상태 메시지 표시
- [ ] API 호출 실패 시 재시도 버튼

**UpcomingSection.test.tsx**
- [ ] 로딩 상태 - 스켈레톤 표시
- [ ] 성공 상태 - Upcoming 카드 렌더링
- [ ] 빈 상태 처리

**LatestNewsSection.test.tsx**
- [ ] 로딩 상태 - 스켈레톤 표시
- [ ] 성공 상태 - 뉴스 카드 10개 렌더링
- [ ] 에러 상태 처리

**page.test.tsx (홈 페이지 통합)**
- [ ] 3개 섹션 모두 렌더링
- [ ] 동시 API 호출 (병렬)
- [ ] Pull-to-refresh 동작
- [ ] 전체 새로고침 시 데이터 reload

#### E2E Tests (Playwright)

**home.spec.ts**
- [ ] 홈 페이지 진입 시 3개 섹션 표시
- [ ] 라이브 카드 클릭 → 라이브 상세 이동
- [ ] 뉴스 카드 클릭 → 뉴스 상세 이동
- [ ] Pull-to-refresh 동작 확인
- [ ] 네트워크 오류 시 에러 상태 표시
- [ ] 반응형 레이아웃 (모바일/태블릿/데스크톱)

---

## 6. 상태 관리

### Client State (React Hooks)
- `useState` - 로딩/에러/데이터 상태
- `useEffect` - 초기 데이터 로드
- Custom Hook: `useHomeSections()`

```typescript
// hooks/useHomeSections.ts
export function useHomeSections() {
  const [liveNow, setLiveNow] = useState<Live[]>([]);
  const [upcoming, setUpcoming] = useState<Live[]>([]);
  const [news, setNews] = useState<News[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  const fetchAll = async () => { /* ... */ };
  const refresh = async () => { /* ... */ };

  return { liveNow, upcoming, news, loading, error, refresh };
}
```

---

## 7. 에러 처리

### 에러 타입
- **네트워크 에러**: 재시도 버튼 + 안내 메시지
- **API 에러 (4xx/5xx)**: 에러 메시지 표시
- **타임아웃**: 재시도 유도

### Fallback UI
- 로딩 상태: `<SkeletonCard />` 사용
- 에러 상태: 에러 메시지 + 재시도 버튼
- 빈 상태: "라이브 예정이 없습니다" 메시지

---

## 8. 성능 최적화

- [ ] 이미지 lazy loading (`next/image`)
- [ ] API 병렬 호출 (`Promise.all`)
- [ ] 메모이제이션 (`useMemo`, `useCallback`)
- [ ] 스켈레톤 UI로 CLS 방지

---

## 9. 접근성 (a11y)

- [ ] 시맨틱 HTML (`<section>`, `<article>`)
- [ ] ARIA 레이블 (스크린 리더 지원)
- [ ] 키보드 네비게이션
- [ ] 이미지 alt 텍스트

---

## 10. 구현 순서 (TDD Cycle)

### Phase 1: 타입 & API Client
1. 타입 정의 (`types/live.ts`, `types/news.ts`)
2. API Client 구현 (`lib/api/home.ts`)
3. API Client 테스트 작성 및 검증

### Phase 2: 카드 컴포넌트 (Bottom-Up)
1. `LiveCard.test.tsx` 작성 (RED)
2. `LiveCard.tsx` 구현 (GREEN)
3. 리팩토링 (REFACTOR)
4. `NewsCard.test.tsx` 작성 및 구현 반복

### Phase 3: 섹션 컴포넌트
1. `LiveNowSection.test.tsx` 작성 (RED)
2. `LiveNowSection.tsx` 구현 (GREEN)
3. 리팩토링
4. `UpcomingSection`, `LatestNewsSection` 반복

### Phase 4: Custom Hook
1. `useHomeSections.test.ts` 작성
2. `useHomeSections.ts` 구현
3. 에러 처리 로직 추가

### Phase 5: 페이지 통합
1. `page.test.tsx` 작성
2. `page.tsx` 구현
3. Pull-to-refresh 통합

### Phase 6: E2E 테스트
1. `home.spec.ts` 작성
2. E2E 시나리오 검증
3. 반응형 테스트 추가

---

## 11. 완료 조건 (Definition of Done)

- [ ] 모든 Unit 테스트 통과 (커버리지 > 80%)
- [ ] 모든 Component 테스트 통과
- [ ] E2E 테스트 통과
- [ ] 코드 리뷰 완료
- [ ] 접근성 검증 (WCAG 2.1 AA)
- [ ] 반응형 디자인 검증 (모바일/태블릿/데스크톱)
- [ ] 로딩/에러/빈 상태 UI 검증
- [ ] API 에러 처리 검증

---

## 12. 의존성 & 블로커

### 의존성
- **API**: `/api/v1/live`, `/api/v1/news` 엔드포인트 준비 필요
- **디자인**: 라이브 카드, 뉴스 카드 디자인 확정

### 블로커
- 백엔드 API 미구현 시 Mock API 사용

---

## 13. 리스크 & 대응

| 리스크 | 영향 | 대응 방안 |
|--------|------|----------|
| API 응답 지연 | UX 저하 | 스켈레톤 UI + 타임아웃 설정 |
| 이미지 로딩 실패 | 빈 썸네일 | 기본 이미지 Fallback |
| 네트워크 오류 | 화면 미표시 | 재시도 버튼 + 에러 메시지 |

---

## 14. 다음 단계

✅ Plan 완료
⏭️ **Design 단계**: 상세 컴포넌트 설계 및 테스트 케이스 명세
   → `/pdca design issue-137-home-screen`

---

**작성일**: 2026-02-01
**작성자**: Claude (AI Assistant)
**문서 버전**: 1.0
