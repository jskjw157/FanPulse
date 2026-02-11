# Plan: 검색 화면 구현 (이슈 #140)

> **Feature**: H018 - 검색
> **Issue**: #140
> **Branch**: `feature/140-search-screen`
> **경로**: `/search`

---

## 1. 목표 (Goal)

라이브/뉴스 통합 검색 기능을 제공하고 최근 검색어를 로컬에 저장하는 검색 화면을 TDD 방식으로 구현

### 주요 기능
- ✅ 검색 입력창 + 검색 버튼
- ✅ 최근 검색어 목록 (localStorage)
- ✅ 라이브 검색 결과 섹션
- ✅ 뉴스 검색 결과 섹션
- ✅ 빈 결과 상태 처리
- ✅ 검색어 최소 2자 검증
- ✅ 로딩/에러 상태 처리

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
├── search/
│   ├── page.tsx                       # 검색 페이지
│   ├── page.test.tsx
│   └── components/
│       ├── SearchBar.tsx              # 검색 입력창
│       ├── SearchBar.test.tsx
│       ├── RecentSearches.tsx         # 최근 검색어
│       ├── RecentSearches.test.tsx
│       ├── LiveResults.tsx            # 라이브 결과
│       ├── LiveResults.test.tsx
│       ├── NewsResults.tsx            # 뉴스 결과
│       ├── NewsResults.test.tsx
│       └── EmptyState.tsx             # 빈 결과 상태
│           └── EmptyState.test.tsx
```

---

## 4. API 연동

### 4.1 엔드포인트

| API | Method | 용도 | Response |
|-----|--------|------|----------|
| `/api/v1/search?q={query}` | GET | 통합 검색 | `{ live: Live[], news: News[] }` |

### 4.2 타입 정의

```typescript
// types/search.ts
export interface SearchResult {
  live: Live[];
  news: News[];
}

export interface SearchParams {
  query: string;
  minLength?: number;  // 기본값: 2
}
```

### 4.3 API Client

```typescript
// lib/api/search.ts
export async function searchContent(query: string): Promise<SearchResult>
```

### 4.4 로컬 스토리지 관리

```typescript
// lib/storage/recentSearches.ts
export function getRecentSearches(): string[]
export function addRecentSearch(query: string): void
export function removeRecentSearch(query: string): void
export function clearRecentSearches(): void

// 최대 10개 유지
const MAX_RECENT_SEARCHES = 10;
```

---

## 5. TDD 전략

### 5.1 테스트 케이스

#### Unit Tests

**SearchBar.test.tsx**
- [ ] 검색어 입력 시 onChange 호출
- [ ] 엔터키 입력 시 onSearch 호출
- [ ] 검색 버튼 클릭 시 onSearch 호출
- [ ] 검색어 2자 미만 시 버튼 비활성화
- [ ] 빈 검색어 입력 방지

**RecentSearches.test.tsx**
- [ ] 최근 검색어 목록 렌더링
- [ ] 검색어 클릭 시 검색 실행
- [ ] 삭제 버튼 클릭 시 검색어 제거
- [ ] 전체 삭제 버튼
- [ ] 최대 10개 제한

**LiveResults.test.tsx**
- [ ] 라이브 검색 결과 렌더링
- [ ] 빈 결과 시 "검색 결과가 없습니다" 메시지
- [ ] 라이브 카드 클릭 → `/live/:id` 이동

**NewsResults.test.tsx**
- [ ] 뉴스 검색 결과 렌더링
- [ ] 빈 결과 처리
- [ ] 뉴스 카드 클릭 → `/news/:id` 이동

**EmptyState.test.tsx**
- [ ] 빈 상태 메시지 렌더링
- [ ] 커스텀 메시지 props 전달

#### Component Tests

**page.test.tsx**
- [ ] 초기 상태 - 최근 검색어 표시
- [ ] 검색 실행 - API 호출 및 결과 표시
- [ ] 검색어 2자 미만 - 검증 메시지
- [ ] 빈 결과 - 빈 상태 UI
- [ ] 로딩 상태 - 스켈레톤 표시
- [ ] 에러 상태 - 에러 메시지

#### E2E Tests

**search.spec.ts**
- [ ] 검색 페이지 진입
- [ ] 검색어 입력 및 검색 실행
- [ ] 검색 결과 표시 확인
- [ ] 최근 검색어 저장 확인
- [ ] 최근 검색어 클릭 재검색
- [ ] 라이브 결과 클릭 → 상세 이동
- [ ] 뉴스 결과 클릭 → 상세 이동

---

## 6. 상태 관리

### Custom Hook: `useSearch()`

```typescript
export function useSearch() {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<SearchResult | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const search = async (searchQuery: string) => {
    if (searchQuery.length < 2) {
      setError(new Error('검색어는 최소 2자 이상이어야 합니다'));
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const result = await searchContent(searchQuery);
      setResults(result);
      addRecentSearch(searchQuery);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  };

  return { query, setQuery, results, loading, error, search };
}
```

### Custom Hook: `useRecentSearches()`

```typescript
export function useRecentSearches() {
  const [searches, setSearches] = useState<string[]>([]);

  useEffect(() => {
    setSearches(getRecentSearches());
  }, []);

  const addSearch = (query: string) => {
    addRecentSearch(query);
    setSearches(getRecentSearches());
  };

  const removeSearch = (query: string) => {
    removeRecentSearch(query);
    setSearches(getRecentSearches());
  };

  const clearAll = () => {
    clearRecentSearches();
    setSearches([]);
  };

  return { searches, addSearch, removeSearch, clearAll };
}
```

---

## 7. localStorage 구현

### Storage Key
```typescript
const RECENT_SEARCHES_KEY = 'fanpulse_recent_searches';
```

### 구현 예시

```typescript
export function getRecentSearches(): string[] {
  if (typeof window === 'undefined') return [];
  const stored = localStorage.getItem(RECENT_SEARCHES_KEY);
  return stored ? JSON.parse(stored) : [];
}

export function addRecentSearch(query: string): void {
  const searches = getRecentSearches();
  const filtered = searches.filter(s => s !== query);
  const updated = [query, ...filtered].slice(0, MAX_RECENT_SEARCHES);
  localStorage.setItem(RECENT_SEARCHES_KEY, JSON.stringify(updated));
}
```

---

## 8. 검색 UX 개선

### Debounce (선택 사항)
```typescript
import { debounce } from 'lodash';

const debouncedSearch = debounce((query: string) => {
  search(query);
}, 300);
```

### Auto-focus
```typescript
useEffect(() => {
  searchInputRef.current?.focus();
}, []);
```

---

## 9. 에러 처리

### 검증 에러
- 검색어 2자 미만: "검색어는 최소 2자 이상 입력해주세요"

### API 에러
- 네트워크 에러: 재시도 버튼
- 빈 결과: "검색 결과가 없습니다" (EmptyState)

---

## 10. 성능 최적화

- [ ] 검색 결과 캐싱 (같은 검색어 재검색 방지)
- [ ] 이미지 lazy loading
- [ ] 검색어 debounce (실시간 검색 시)

---

## 11. 접근성 (a11y)

- [ ] 검색 입력창 label
- [ ] 검색 버튼 명확한 레이블
- [ ] 키보드 네비게이션 (↑↓ 키로 최근 검색어 선택)
- [ ] 검색 결과 aria-live 영역

---

## 12. 구현 순서 (TDD Cycle)

### Phase 1: 타입 & API Client
1. `types/search.ts` 정의
2. `lib/api/search.ts` 구현 및 테스트
3. `lib/storage/recentSearches.ts` 구현 및 테스트

### Phase 2: 컴포넌트
1. `SearchBar.test.tsx` → `SearchBar.tsx`
2. `RecentSearches.test.tsx` → `RecentSearches.tsx`
3. `LiveResults.test.tsx` → `LiveResults.tsx`
4. `NewsResults.test.tsx` → `NewsResults.tsx`
5. `EmptyState.test.tsx` → `EmptyState.tsx`

### Phase 3: Custom Hooks
1. `useSearch` hook 테스트 및 구현
2. `useRecentSearches` hook 테스트 및 구현

### Phase 4: 페이지 통합
1. `page.test.tsx` 작성
2. `page.tsx` 구현

### Phase 5: E2E 테스트
1. `search.spec.ts` 작성 및 검증

---

## 13. 완료 조건 (Definition of Done)

- [ ] 모든 Unit 테스트 통과 (커버리지 > 80%)
- [ ] 검색어 검증 동작 확인
- [ ] 최근 검색어 저장/삭제 동작 확인
- [ ] 검색 결과 표시 검증
- [ ] E2E 테스트 통과
- [ ] 접근성 검증

---

## 14. 의존성 & 블로커

### 의존성
- **API**: `/api/v1/search` 엔드포인트
- **Backend**: 라이브/뉴스 통합 검색 지원

### 보안 고려사항
- XSS 방지 (검색어 입력)

---

## 15. 다음 단계

✅ Plan 완료
⏭️ **Design 단계**: 상세 컴포넌트 설계 및 테스트 케이스 명세

---

**작성일**: 2026-02-01
**작성자**: Claude (AI Assistant)
**문서 버전**: 1.0
