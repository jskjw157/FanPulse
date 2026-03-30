# Claude Code 문서 및 Anthropic 블로그 크롤러 모음

Claude Code 공식 문서와 Anthropic 블로그를 크롤링하여 마크다운으로 변환하는 도구 모음입니다.

## 📦 포함된 크롤러

### 1. Claude Code 문서 크롤러 (`claude_code_crawler.py`)
- **대상**: https://code.claude.com/docs
- **결과**: 48개의 문서 페이지
- **카테고리**: 시작하기, 통합, CI/CD, 플러그인, 배포, 관리, 설정, 레퍼런스

### 2. Anthropic 블로그 크롤러 (`anthropic_blog_crawler.py`)
- **대상**: https://www.anthropic.com/news
- **결과**: 15개의 블로그/뉴스 포스트
- **카테고리**: Claude Code, 제품 발표, 파트너십, 회사 소식, 정책

### Codex Developers 문서 크롤러
별도 디렉토리로 분리했습니다. `script/codex_crawler/README.md`를 참고하세요.

## 📊 전체 크롤링 결과

| 크롤러 | 페이지 수 | 출력 디렉토리 | 파일 형식 |
|--------|-----------|---------------|-----------|
| Claude Code 문서 | 48 | `doc/claude_code_docs/` | Markdown |
| Anthropic 블로그 | 15 | `doc/anthropic_blog/` | Markdown |
| **총합** | **63** | - | - |

## 🚀 빠른 시작

### 환경 설정

```bash
# 필요한 패키지 설치
pip install requests beautifulsoup4 html2text
```

### 크롤링 범위 제한 옵션

두 크롤러 모두 경로 필터를 지원합니다.

- `include_path_prefixes`: 허용할 URL 경로 프리픽스 목록
- `exclude_path_patterns`: 제외할 경로 정규식 목록

기본값:
- Claude Code 문서: `["/docs/en/"]`
- Anthropic 블로그: `["/news/", "/blog/"]`

예시 (코드 내에서 커스터마이징):
```python
crawler = ClaudeCodeCrawler(
    include_path_prefixes=["/docs/en/"],
    exclude_path_patterns=[r"/docs/en/iam"]
)
```

### 본문 압축 옵션

기본으로 `compact_mode=True`가 적용되어 이미지 URL과 불필요한 UI 텍스트를 제거합니다.

```python
crawler = ClaudeCodeCrawler(compact_mode=False)
```

링크는 유지하고 싶다면 기본값 그대로 두면 됩니다. 링크까지 제거하려면:

```python
crawler = ClaudeCodeCrawler(keep_links=False)
```

### Claude Code 문서 크롤링

```bash
python3 claude_code_crawler.py
```

### Anthropic 블로그 크롤링

```bash
python3 anthropic_blog_crawler.py
```

## ✅ 필터/정제 테스트

```bash
python3 tests/test_filters.py
```

## 📁 디렉토리 구조

```
.
├── claude_code_crawler.py          # Claude Code 문서 크롤러
├── anthropic_blog_crawler.py       # Anthropic 블로그 크롤러
├── doc/
│   ├── claude_code_docs/           # 크롤링된 문서 (48개)
│   ├── overview.md
│   ├── quickstart.md
│   ├── plugins.md
│   ├── ...
│   ├── README.md
│   └── _crawl_stats.json
│   └── anthropic_blog/             # 크롤링된 블로그 (15개)
│       ├── claude-opus-4-5.md
│       ├── anthropic-acquires-bun-as-claude-code-reaches-usd1b-milestone.md
│       ├── ...
│       ├── README.md
│       └── _crawl_stats.json
```

## 💡 주요 활용 사례

### 1. 오프라인 문서 보관
인터넷 연결 없이 로컬에서 문서 참조

### 2. 문서 검색 시스템 구축
Elasticsearch 등으로 강력한 검색 시스템 구축

### 3. AI 학습 데이터
RAG 시스템의 지식 베이스로 활용

### 4. 문서 변경 추적
주기적 크롤링으로 버전 관리

### 5. 번역 자동화
다국어 문서 생성

---

**생성일**: 2024-12-30
**크롤링 소스**: 
- Claude Code Documentation (https://code.claude.com/docs)
- Anthropic News & Blog (https://www.anthropic.com/news)
