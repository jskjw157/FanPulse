#!/usr/bin/env python3
"""
OpenAI Codex Developers Docs Crawler
Developers 문서를 크롤링하여 마크다운으로 변환합니다.
"""

import json
import os
import re
import time
from pathlib import Path
from urllib.parse import urljoin, urlparse, urlunparse

import html2text
import requests
from bs4 import BeautifulSoup
from bs4 import NavigableString

CODEX_OUTPUT_DIR = os.path.join("doc", "codex_docs")

DEFAULT_START_URLS = ["https://developers.openai.com/codex/"]

ASSET_EXTENSIONS = (
    ".png",
    ".jpg",
    ".jpeg",
    ".gif",
    ".svg",
    ".webp",
    ".ico",
    ".css",
    ".js",
    ".map",
    ".woff",
    ".woff2",
    ".ttf",
    ".otf",
    ".eot",
    ".pdf",
    ".zip",
)


class CodexDocsCrawler:
    def __init__(
        self,
        output_dir=CODEX_OUTPUT_DIR,
        urls=None,
        start_urls=None,
        include_path_prefixes=None,
        exclude_path_patterns=None,
        max_pages=None,
        compact_mode=True,
        keep_links=True,
        discover_links=True,
    ):
        self.base_url = "https://developers.openai.com"
        self.output_dir = output_dir
        seed_urls = start_urls if start_urls is not None else urls
        self.start_urls = list(seed_urls) if seed_urls else list(DEFAULT_START_URLS)
        self.include_path_prefixes = include_path_prefixes or ["/codex"]
        self.exclude_path_patterns = [
            re.compile(pattern) for pattern in (exclude_path_patterns or [])
        ]
        self.max_pages = max_pages
        self.discover_links = discover_links
        self.visited_urls = set()
        self.visited_order = []
        self.failed_urls = []
        self.headers = {
            "User-Agent": (
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                "AppleWebKit/537.36 (KHTML, like Gecko) "
                "Chrome/120.0.0.0 Safari/537.36"
            )
        }
        self.html_converter = html2text.HTML2Text()
        self.compact_mode = compact_mode
        self.keep_links = keep_links
        self.html_converter.ignore_links = compact_mode and not keep_links
        self.html_converter.ignore_images = compact_mode
        self.html_converter.body_width = 0

        Path(output_dir).mkdir(parents=True, exist_ok=True)

    def normalize_url(self, url):
        """쿼리/프래그먼트 제거"""
        parsed = urlparse(url)
        return urlunparse((parsed.scheme, parsed.netloc, parsed.path, "", "", ""))

    def is_valid_doc_url(self, url):
        """Codex 문서 URL인지 확인"""
        parsed = urlparse(url)
        if parsed.scheme not in {"http", "https"}:
            return False
        if parsed.netloc != "developers.openai.com":
            return False
        path = parsed.path or ""
        if self.include_path_prefixes and not any(
            path.startswith(prefix) for prefix in self.include_path_prefixes
        ):
            return False
        if path.endswith(ASSET_EXTENSIONS):
            return False
        if any(pattern.search(path) for pattern in self.exclude_path_patterns):
            return False
        return True

    def strip_noise(self, content):
        """네비게이션/푸터 등 불필요한 요소 제거"""
        if content is None:
            return
        selectors = [
            "nav",
            "aside",
            "footer",
            "form",
            "button",
            '[role="navigation"]',
            '[role="search"]',
            '[aria-label="Search"]',
            ".sidebar",
            ".toc",
            ".table-of-contents",
            ".breadcrumbs",
            ".search",
            ".navigation",
            ".skip-to-content",
        ]
        for selector in selectors:
            for tag in content.select(selector):
                tag.decompose()
        for header in content.find_all("header"):
            if header.find("h1") is None:
                header.decompose()

    def trim_to_first_heading(self, content, heading_tag="h1"):
        """첫 번째 제목 이전의 콘텐츠 제거"""
        if content is None:
            return
        first_heading = content.find(heading_tag)
        if not first_heading:
            return
        node = first_heading
        while node and node is not content:
            prev = node.previous_sibling
            while prev:
                to_remove = prev
                prev = prev.previous_sibling
                if isinstance(to_remove, NavigableString):
                    to_remove.extract()
                else:
                    to_remove.decompose()
            node = node.parent

    def compress_markdown(self, markdown):
        """마크다운 크기 축소"""
        if not markdown:
            return markdown
        markdown = markdown.replace("\u200b", "")
        skip_exact = {
            "Copy",
            "Copied",
            "Skip to main content",
            "Search",
            "Navigation",
        }
        lines = []
        in_code_block = False
        prev_blank = False
        skip_next_blank = False
        for raw_line in markdown.splitlines():
            line = raw_line.rstrip()
            stripped = line.strip()
            if stripped.startswith("```"):
                if not in_code_block:
                    while lines and lines[-1] == "":
                        lines.pop()
                else:
                    skip_next_blank = True
                in_code_block = not in_code_block
                lines.append(line)
                prev_blank = False
                continue
            if not in_code_block:
                if stripped in skip_exact:
                    continue
                if re.match(r"^#+\s*$", stripped):
                    continue
            if not stripped:
                if skip_next_blank:
                    continue
                if prev_blank:
                    continue
                prev_blank = True
                lines.append("")
                continue
            if skip_next_blank:
                skip_next_blank = False
            prev_blank = False
            lines.append(line)
        return "\n".join(lines).strip()

    def get_page_content(self, url):
        """페이지 내용 가져오기"""
        try:
            response = requests.get(url, headers=self.headers, timeout=20)
            response.raise_for_status()
            return response.text
        except Exception as exc:
            print(f"❌ Error fetching {url}: {exc}")
            return None

    def extract_main_content(self, soup):
        """메인 콘텐츠 추출"""
        selectors = [
            "main",
            "article",
            "[data-testid='content']",
            ".docs-content",
            ".documentation",
            ".prose",
            "[role='main']",
        ]
        for selector in selectors:
            content = soup.select_one(selector)
            if content:
                return content
        return soup.find("body")

    def clean_filename(self, url):
        """URL에서 안전한 파일명 생성"""
        parsed = urlparse(url)
        path = parsed.path.strip("/")

        if path == "codex":
            path = ""

        path = re.sub(r"^codex/", "", path)

        if not path:
            path = "index"

        filename = path.replace("/", "_") + ".md"
        return filename

    def html_to_markdown(self, html_content):
        """HTML을 마크다운으로 변환"""
        try:
            markdown = self.html_converter.handle(str(html_content))
            markdown = re.sub(r"\n{3,}", "\n\n", markdown)
            markdown = markdown.strip()
            if self.compact_mode:
                markdown = self.compress_markdown(markdown)
            return markdown
        except Exception as exc:
            print(f"⚠️  Markdown conversion error: {exc}")
            return str(html_content)

    def save_markdown(self, url, content, title=""):
        """마크다운 파일로 저장"""
        filename = self.clean_filename(url)
        filepath = os.path.join(self.output_dir, filename)

        metadata = ["---", f"source: {url}"]
        if title:
            metadata.append(f"title: {title}")
        metadata.append("---\n")
        metadata_block = "\n".join(metadata)

        with open(filepath, "w", encoding="utf-8") as file:
            file.write(metadata_block + "\n" + content)

        print(f"✅ Saved: {filename}")
        return filepath

    def extract_links(self, soup, current_url):
        """페이지에서 Codex 문서 링크 추출"""
        links = set()
        for a_tag in soup.find_all("a", href=True):
            href = a_tag["href"].strip()
            full_url = urljoin(current_url, href)
            full_url = self.normalize_url(full_url)
            if self.is_valid_doc_url(full_url):
                links.add(full_url)
        return links

    def crawl_page(self, url):
        """단일 페이지 크롤링"""
        url = self.normalize_url(url)
        if url in self.visited_urls:
            return set()

        print(f"\n🔍 Crawling: {url}")
        self.visited_urls.add(url)
        self.visited_order.append(url)

        html_content = self.get_page_content(url)
        if not html_content:
            self.failed_urls.append(url)
            return set()

        soup = BeautifulSoup(html_content, "html.parser")
        title = ""
        title_tag = soup.find("h1") or soup.title
        if title_tag:
            title = title_tag.get_text(strip=True)

        new_links = set()
        if self.discover_links:
            new_links = self.extract_links(soup, url)

        main_content = self.extract_main_content(soup)
        self.strip_noise(main_content)
        self.trim_to_first_heading(main_content)

        markdown_content = self.html_to_markdown(main_content)
        self.save_markdown(url, markdown_content, title)

        return new_links

    def crawl(self):
        """전체 문서 크롤링"""
        print("🚀 Starting Codex docs crawl")
        print(f"📁 Output directory: {self.output_dir}")
        print(f"📄 Seed URLs: {len(self.start_urls)}")
        max_pages_label = self.max_pages if self.max_pages is not None else "unlimited"
        print(f"📄 Max pages: {max_pages_label}\n")

        to_visit = set()
        for url in self.start_urls:
            normalized = self.normalize_url(url)
            if self.is_valid_doc_url(normalized):
                to_visit.add(normalized)

        pages_crawled = 0
        while to_visit and (self.max_pages is None or pages_crawled < self.max_pages):
            url = to_visit.pop()
            if url in self.visited_urls:
                continue

            new_links = self.crawl_page(url)
            pages_crawled += 1

            if self.discover_links:
                for link in new_links:
                    if link not in self.visited_urls:
                        to_visit.add(link)

            if pages_crawled % 5 == 0:
                print(f"\n📈 Progress: {pages_crawled} pages crawled")

            time.sleep(1.0)

        stats = {
            "total_pages": len(self.visited_order),
            "visited_urls": self.visited_order,
            "failed_urls": self.failed_urls,
            "output_dir": self.output_dir,
            "start_urls": self.start_urls,
        }

        with open(os.path.join(self.output_dir, "_crawl_stats.json"), "w", encoding="utf-8") as file:
            json.dump(stats, file, indent=2, ensure_ascii=False)

        print(f"\n✨ Crawling complete!")
        print(f"📊 Pages crawled: {len(self.visited_order)}")
        if self.failed_urls:
            print(f"⚠️  Failed: {len(self.failed_urls)}")
        print(f"📁 Files saved in: {self.output_dir}")


def main():
    crawler = CodexDocsCrawler(output_dir=CODEX_OUTPUT_DIR)
    crawler.crawl()


if __name__ == "__main__":
    main()
