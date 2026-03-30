# OpenAI Codex Developers 문서 크롤러

OpenAI Codex Developers 문서를 크롤링하여 마크다운으로 변환합니다.

## 📦 개요

- **대상**: https://developers.openai.com/codex
- **크롤링 방식**: `/codex` 내부 링크 자동 수집
- **출력 디렉토리**: `doc/codex_docs/`
- **파일 형식**: Markdown

## 🚀 빠른 시작

```bash
# 필요한 패키지 설치
pip install requests beautifulsoup4 html2text

# 크롤링 실행
python3 codex_docs_crawler.py
```

## ⚙️ 옵션 커스터마이징

```python
from codex_docs_crawler import CodexDocsCrawler

crawler = CodexDocsCrawler(
    include_path_prefixes=["/codex"],
    exclude_path_patterns=[r"/codex/legacy"],
    max_pages=200,
    discover_links=True,
)

crawler.crawl()
```

### 옵션 설명

- `include_path_prefixes`: 허용할 URL 경로 프리픽스 목록
- `exclude_path_patterns`: 제외할 경로 정규식 목록
- `max_pages`: 최대 크롤링 페이지 수 (기본: 제한 없음)
- `discover_links`: 내부 링크 자동 수집 여부 (기본: True)

## 📁 출력 예시

```
doc/
└── codex_docs/
    ├── quickstart.md
    ├── cli.md
    ├── config-basic.md
    ├── ...
    └── _crawl_stats.json
```

## 🔎 참고

- 링크 수집은 `developers.openai.com` 도메인과 `/codex` 경로만 허용합니다.
- 이미지/JS/CSS 등 정적 자산 확장자는 자동 제외됩니다.
