# Frontend Django

React + Vite + MUI 기반의 뉴스 요약 프론트엔드 애플리케이션입니다.

## 폴더 구조

```
frontend_django/
├── src/                          # 소스 코드
│   ├── api/                      # API 통신 레이어
│   │   └── client.js             # Axios 기반 API 클라이언트
│   │
│   ├── components/               # React 컴포넌트
│   │   ├── Layout.jsx            # 전체 레이아웃 (사이드바, 네비게이션)
│   │   ├── URLSummarize.jsx      # URL 요약 페이지
│   │   ├── TextSummarize.jsx     # 텍스트 직접 입력 요약
│   │   ├── NewsSearch.jsx        # 네이버 뉴스 검색
│   │   ├── SavedNewsList.jsx     # 저장된 뉴스 목록 (선택 → 요약)
│   │   ├── SummarizedNews.jsx    # 요약된 뉴스 결과 조회
│   │   ├── ResultCard.jsx        # 요약 결과 카드 컴포넌트
│   │   ├── History.jsx           # 요약 히스토리
│   │   └── Settings.jsx          # 설정 페이지
│   │
│   ├── utils/                    # 유틸리티 함수
│   │   └── storage.js            # LocalStorage 관리
│   │
│   ├── App.jsx                   # 라우팅 설정
│   ├── main.jsx                  # 앱 진입점
│   └── theme.js                  # MUI 테마 설정
│
├── index.html                    # HTML 템플릿
├── package.json                  # npm 의존성 및 스크립트
├── package-lock.json             # 의존성 잠금 파일
├── vite.config.js                # Vite 빌드 설정
└── .gitignore                    # Git 무시 파일
```

## 주요 파일 설명

### 페이지 컴포넌트

| 파일 | 경로 | 설명 |
|------|------|------|
| `URLSummarize.jsx` | `/`, `/url-summarize` | URL 입력 → 텍스트 추출 → 요약 |
| `TextSummarize.jsx` | `/text-summarize` | 텍스트 직접 입력 → 요약 |
| `NewsSearch.jsx` | `/news-search` | 네이버 뉴스 검색 및 JSON 저장 |
| `SavedNewsList.jsx` | `/saved-news` | 저장된 뉴스 조회, 선택 후 배치 요약 |
| `SummarizedNews.jsx` | `/summarized-news` | 요약 결과 파일 조회 |
| `History.jsx` | `/history` | 요약 히스토리 (LocalStorage) |
| `Settings.jsx` | `/settings` | 설정 페이지 |

### API 클라이언트 (`src/api/client.js`)

| 함수 | 설명 |
|------|------|
| `healthCheck()` | 서버 상태 확인 |
| `summarize(data)` | 단일 텍스트/URL 요약 |
| `searchNews(query, options)` | 네이버 뉴스 검색 |
| `getSavedNewsList()` | 저장된 뉴스 파일 목록 |
| `getSavedNewsDetail(filename)` | 저장된 뉴스 상세 조회 |
| `deleteSavedNews(filename)` | 저장된 뉴스 파일 삭제 |
| `batchSummarize(items, method, options)` | 선택된 뉴스 배치 요약 |
| `getSummarizedNewsList()` | 요약된 뉴스 파일 목록 |
| `getSummarizedNewsDetail(filename)` | 요약 결과 상세 조회 |
| `deleteSummarizedNews(filename)` | 요약 결과 파일 삭제 |

### 유틸리티 (`src/utils/storage.js`)

LocalStorage를 사용한 히스토리 관리:
- 요약 결과 저장
- 히스토리 조회/삭제

## 설치 방법

### 1. Node.js 설치

Node.js 18 이상 필요합니다.

```bash
# 버전 확인
node --version  # v18.x.x 이상
npm --version   # 9.x.x 이상
```

### 2. 의존성 설치

```bash
cd frontend_django
npm install
```

## 실행 방법

### 개발 서버 실행

```bash
npm run dev
```

개발 서버가 `http://localhost:5173`에서 실행됩니다.

### 프로덕션 빌드

```bash
npm run build
```

빌드 결과물이 `dist/` 폴더에 생성됩니다.

### 빌드 미리보기

```bash
npm run preview
```

## 사용 기술

### 핵심 라이브러리

| 라이브러리 | 버전 | 용도 |
|------------|------|------|
| React | 18.2.0 | UI 프레임워크 |
| React Router | 6.21.3 | 클라이언트 라우팅 |
| MUI (Material-UI) | 5.15.6 | UI 컴포넌트 |
| Axios | 1.6.5 | HTTP 클라이언트 |
| Vite | 5.0.11 | 빌드 도구 |

### MUI 컴포넌트 활용

- `Box`, `Stack` - 레이아웃
- `Card`, `Paper` - 컨테이너
- `Typography` - 텍스트
- `Button`, `IconButton` - 버튼
- `TextField`, `Select` - 입력
- `Accordion` - 접기/펼치기
- `Chip` - 태그/라벨
- `CircularProgress` - 로딩
- `Alert` - 알림 메시지
- `Dialog` - 모달

## 주요 워크플로우

### 1. URL 요약

```
URL 입력 → [요약하기] → 백엔드에서 텍스트 추출 및 요약 → 결과 표시
```

### 2. 뉴스 검색 및 저장

```
검색어 입력 → [검색] → 네이버 뉴스 API 호출 → JSON 파일 저장
```

### 3. 배치 요약

```
저장된 뉴스 → 체크박스로 선택 → 요약 방식 선택 (알고리즘/AI)
→ [선택한 뉴스 요약하기] → 요약 결과 저장 → 요약된 뉴스 페이지로 이동
```

### 4. 요약 결과 조회

```
요약된 뉴스 → 파일 선택 → 요약 결과 표시 (요약문, 핵심 포인트, 키워드)
```

## 환경 설정

### API 서버 주소 변경

`src/api/client.js`:

```javascript
const API_BASE_URL = 'http://localhost:8000/api';
```

### 타임아웃 설정

현재 1시간(3600000ms)으로 설정되어 있습니다:

```javascript
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 3600000, // 1 hour
});
```

## 트러블슈팅

### CORS 오류

백엔드에서 CORS 설정이 필요합니다. Django 설정에서 `django-cors-headers`가 올바르게 구성되어 있는지 확인하세요.

### 네트워크 오류

```
timeout of 3600000ms exceeded
```

- 백엔드 서버가 실행 중인지 확인
- AI 요약 시 GPU 메모리 부족 가능성 확인

### 빈 화면 표시

- 브라우저 개발자 도구(F12) → Console 탭에서 오류 확인
- `npm run dev` 터미널에서 빌드 오류 확인
