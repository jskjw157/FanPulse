# Backend Django

Django REST Framework 기반의 뉴스 요약 백엔드 서버입니다.

## 폴더 구조

```
backend_django/
├── api/                          # API 앱 (핵심 비즈니스 로직)
│   ├── __init__.py
│   ├── admin.py                  # Django Admin 설정
│   ├── apps.py                   # 앱 설정
│   ├── models.py                 # 데이터베이스 모델 (현재 미사용)
│   ├── serializers.py            # DRF 직렬화 클래스
│   ├── tests.py                  # 테스트 코드
│   ├── urls.py                   # API 라우팅 (엔드포인트 정의)
│   ├── views.py                  # API 뷰 (요청 처리 로직)
│   └── services/                 # 서비스 레이어
│       ├── __init__.py
│       ├── ai_summarizer.py      # AI 기반 요약 (Mistral-7B, BART)
│       ├── extractor.py          # 웹페이지 텍스트 추출
│       ├── news_crawler.py       # 네이버 뉴스 검색 및 저장
│       └── summarizer.py         # 규칙 기반 요약 (TF-IDF)
│
├── config/                       # Django 프로젝트 설정
│   ├── __init__.py
│   ├── asgi.py                   # ASGI 설정 (비동기 서버)
│   ├── settings.py               # Django 설정 파일
│   ├── urls.py                   # 프로젝트 URL 설정
│   └── wsgi.py                   # WSGI 설정 (동기 서버)
│
├── legacy/                       # 레거시 코드 (참고용)
│   └── ai_summarizer.py          # 이전 버전 AI 요약기
│
├── news_data/                    # 저장된 뉴스 데이터 (JSON)
│   └── *.json                    # 검색된 뉴스 원문
│
├── summarized_data/              # 요약된 뉴스 데이터 (JSON)
│   └── *.json                    # 요약 결과 파일
│
├── manage.py                     # Django 관리 스크립트
├── requirements.txt              # Python 의존성 목록
├── pytorch_install.txt           # PyTorch 설치 가이드
└── read_run.md                   # 실행 가이드 (레거시)
```

## 주요 파일 설명

### API 엔드포인트 (`api/urls.py`)

| 경로 | 메서드 | 설명 |
|------|--------|------|
| `/api/health` | GET | 서버 상태 확인 |
| `/api/summarize` | POST | 단일 텍스트/URL 요약 |
| `/api/news/search` | GET | 네이버 뉴스 검색 |
| `/api/news/saved` | GET | 저장된 뉴스 파일 목록 |
| `/api/news/saved/<filename>` | GET/DELETE | 저장된 뉴스 상세/삭제 |
| `/api/news/batch-summarize` | POST | 선택된 뉴스 배치 요약 |
| `/api/news/summarized` | GET | 요약된 뉴스 파일 목록 |
| `/api/news/summarized/<filename>` | GET/DELETE | 요약 결과 상세/삭제 |

### 서비스 레이어 (`api/services/`)

| 파일 | 클래스/함수 | 설명 |
|------|-------------|------|
| `ai_summarizer.py` | `AISummarizer` | Mistral-7B 모델 기반 AI 요약 |
| `summarizer.py` | `ArticleSummarizer` | TF-IDF 기반 규칙형 요약 |
| `extractor.py` | `ArticleExtractor` | URL에서 본문 추출 |
| `news_crawler.py` | `NaverNewsSearcher` | 네이버 뉴스 API 검색 |
| `news_crawler.py` | `SavedNewsReader` | 저장된 뉴스 관리 |
| `news_crawler.py` | `SummarizedNewsManager` | 요약 결과 관리 |

## 설치 방법

### 1. 가상환경 생성 및 활성화

```bash
cd backend_django

# 가상환경 생성
python -m venv venv

# 활성화 (Windows)
venv\Scripts\activate

# 활성화 (Linux/Mac)
source venv/bin/activate
```

### 2. 의존성 설치

```bash
pip install -r requirements.txt
```

### 3. PyTorch 설치 (GPU 사용 시)

AI 요약 기능을 사용하려면 PyTorch가 필요합니다.

```bash
# CUDA 11.8 버전 (권장)
pip install torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cu118

# CUDA 12.1 버전
pip install torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cu121

# CPU만 사용
pip install torch torchvision torchaudio
```

### 4. 환경 변수 설정 (선택사항)

`.env` 파일 생성:

```env
# 네이버 API (뉴스 검색용)
NAVER_CLIENT_ID=your_client_id
NAVER_CLIENT_SECRET=your_client_secret

# Django 설정
DEBUG=True
SECRET_KEY=your-secret-key
```

## 실행 방법

### 개발 서버 실행

```bash
cd backend_django
python manage.py runserver
```

서버가 `http://localhost:8000`에서 실행됩니다.

### API 문서 확인

- Swagger UI: `http://localhost:8000/swagger/`
- ReDoc: `http://localhost:8000/redoc/`

## 요약 방식

### 1. 규칙 기반 (Rule-based)

- **방식**: TF-IDF 알고리즘으로 중요 문장 추출
- **장점**: 빠름, GPU 불필요
- **단점**: 문맥 이해 제한적

### 2. AI 기반 (Abstractive)

- **모델**: Mistral-7B-Instruct-v0.3 (4bit 양자화)
- **방식**: LLM이 텍스트를 이해하고 새로운 문장 생성
- **장점**: 자연스러운 요약
- **단점**: GPU 필요, 첫 실행 시 모델 다운로드 필요 (수 GB)

## 데이터 저장 구조

### 저장된 뉴스 (`news_data/*.json`)

```json
{
  "query": "검색어",
  "searched_at": "2026-01-25T12:00:00",
  "count": 20,
  "items": [
    {
      "title": "뉴스 제목",
      "originallink": "원문 URL",
      "pubDate": "발행일",
      "origainal_news": "본문 텍스트"
    }
  ]
}
```

### 요약된 뉴스 (`summarized_data/*.json`)

```json
{
  "method": "rule|ai",
  "created_at": "2026-01-25T12:00:00",
  "count": 5,
  "items": [
    {
      "title": "뉴스 제목",
      "summary": "요약 결과",
      "bullets": ["핵심 포인트 1", "핵심 포인트 2"],
      "keywords": ["키워드1", "키워드2"],
      "summarized": true
    }
  ]
}
```

## 트러블슈팅

### CUDA 관련 오류

```bash
# CUDA 버전 확인
python -c "import torch; print(torch.cuda.is_available())"
```

### transformers 경고 메시지

`do_sample=False`일 때 temperature 경고가 발생하면 정상입니다. (greedy decoding 사용 시 temperature는 무시됨)

### 모델 다운로드 실패

- HuggingFace 토큰 설정 필요할 수 있음
- 네트워크 환경 확인
