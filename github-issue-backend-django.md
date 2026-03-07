# [백엔드] Django 서버 구성, 뉴스 데이터 추출

## 📝 설명
Django REST Framework 기반의 백엔드 서버를 구성하고, 네이버 뉴스 API 및 웹 크롤링을 통해 뉴스 데이터를 추출하는 기능을 구현합니다.

## 🔧 서버 구성 상세
- **목적**: K-POP 뉴스 기사를 수집하고 본문 텍스트를 추출하여 저장
- **프레임워크**: Django 4.2 + Django REST Framework
- **API 문서화**: drf-yasg (Swagger/ReDoc)
- **데이터 저장**: JSON 파일 기반 (PostgreSQL 확장 가능)

## ✅ 구현 요구사항

### 기능

- [ ] 네이버 뉴스 API 연동 (검색어 기반 뉴스 목록 조회)
- [ ] 뉴스 원문 URL에서 본문 텍스트 자동 추출
- [ ] BeautifulSoup + lxml 기반 HTML 파싱
- [ ] newspaper3k 라이브러리 폴백 지원
- [ ] 검색 결과 JSON 파일 자동 저장
- [ ] 한국어/영어 뉴스 모두 지원

### API 엔드포인트

- [ ] `GET /api/health` - 서버 상태 확인
- [ ] `GET /api/news/search` - 네이버 뉴스 검색 + 본문 추출
- [ ] `GET /api/news/saved` - 저장된 뉴스 파일 목록
- [ ] `GET /api/news/saved/<filename>` - 저장된 뉴스 상세 조회
- [ ] `DELETE /api/news/saved/<filename>` - 저장된 뉴스 파일 삭제

### 기술 구현

- [ ] Django 4.2 + Django REST Framework 설정
- [ ] 네이버 Open API 연동 (뉴스 검색)
- [ ] BeautifulSoup4, lxml 파싱 구현
- [ ] newspaper3k 폴백 로직 구현
- [ ] ThreadPoolExecutor 비동기 파일 저장
- [ ] JSON 파일 저장 (UTF-8)

### 프로젝트 구조

```
backend_django/
├── api/
│   ├── views.py              # API 엔드포인트 핸들러
│   ├── urls.py               # URL 라우팅
│   └── services/
│       ├── news_crawler.py   # 네이버 뉴스 검색 및 저장
│       └── extractor.py      # 웹페이지 본문 추출
├── config/
│   ├── settings.py           # Django 설정
│   └── urls.py               # 프로젝트 URL 설정
└── news_data/                # 저장된 뉴스 JSON 파일
```

## 🔗 연관 기능
- AI 뉴스 요약 기능 (#87)
- 프론트엔드 뉴스 검색 UI

## 📌 라벨
`backend`, `feature`, `infrastructure`, `new`

## 🎯 Milestone
백엔드 (Backend)
