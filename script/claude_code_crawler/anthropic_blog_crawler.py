#!/usr/bin/env python3
"""
Anthropic Blog/News Crawler
Anthropic의 뉴스 및 블로그 포스트를 크롤링하여 마크다운으로 변환합니다.
"""

import requests
from bs4 import BeautifulSoup
import html2text
import os
import time
import re
from urllib.parse import urljoin, urlparse, urlunparse
from bs4 import NavigableString
from pathlib import Path
import json

BLOG_OUTPUT_DIR = os.path.join("doc", "anthropic_blog")


class AnthropicBlogCrawler:
    def __init__(
        self,
        output_dir=BLOG_OUTPUT_DIR,
        include_path_prefixes=None,
        exclude_path_patterns=None,
        compact_mode=True,
        keep_links=True,
    ):
        self.base_url = "https://www.anthropic.com"
        self.output_dir = output_dir
        if include_path_prefixes is None:
            include_path_prefixes = ["/news/", "/blog/"]
        self.include_path_prefixes = include_path_prefixes
        self.exclude_path_patterns = [
            re.compile(pattern) for pattern in (exclude_path_patterns or [])
        ]
        self.visited_urls = set()
        self.headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'
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

    def is_valid_post_url(self, url):
        """블로그/뉴스 포스트 URL인지 확인"""
        parsed = urlparse(url)
        path = parsed.path or ""
        if parsed.netloc not in {"www.anthropic.com", "anthropic.com"}:
            return False
        if self.include_path_prefixes and not any(
            path.startswith(prefix) for prefix in self.include_path_prefixes
        ):
            return False
        if path.rstrip("/") in {"/news", "/blog"}:
            return False
        if path.endswith(('.png', '.jpg', '.jpeg', '.gif', '.css', '.js')):
            return False
        if any(pattern.search(path) for pattern in self.exclude_path_patterns):
            return False
        return True

    def strip_noise(self, content):
        """네비게이션/푸터 등 불필요한 요소 제거"""
        if content is None:
            return
        selectors = [
            'nav',
            'aside',
            'footer',
            'form',
            'button',
            '[role="navigation"]',
            '[role="search"]',
            '[aria-label="Search"]',
            '.sidebar',
            '.toc',
            '.table-of-contents',
            '.breadcrumbs',
            '.search',
            '.navigation',
            '.skip-to-content',
        ]
        for selector in selectors:
            for tag in content.select(selector):
                tag.decompose()
        for header in content.find_all('header'):
            if header.find('h1') is None:
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
                if stripped in {"⌘K"}:
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
            response = requests.get(url, headers=self.headers, timeout=15)
            response.raise_for_status()
            return response.text
        except Exception as e:
            print(f"❌ Error fetching {url}: {str(e)}")
            return None
    
    def extract_blog_posts_from_index(self, index_url):
        """인덱스 페이지에서 블로그 포스트 URL 추출"""
        html_content = self.get_page_content(index_url)
        if not html_content:
            return []
        
        soup = BeautifulSoup(html_content, 'html.parser')
        blog_posts = []
        
        for a in soup.find_all('a', href=True):
            href = a.get('href', '')
            
            full_url = self.normalize_url(urljoin(index_url, href))
            if self.is_valid_post_url(full_url) and full_url not in blog_posts:
                blog_posts.append(full_url)
        
        return list(set(blog_posts))
    
    def extract_main_content(self, soup):
        """메인 콘텐츠 추출"""
        selectors = [
            'article',
            'main',
            '.article-content',
            '.post-content',
            '.blog-post',
            '[role="article"]',
            '.content'
        ]
        
        for selector in selectors:
            content = soup.select_one(selector)
            if content:
                return content
        
        return soup.find('body')
    
    def clean_filename(self, url):
        """URL에서 안전한 파일명 생성"""
        parsed = urlparse(url)
        path = parsed.path.strip('/')
        
        # /news/ 또는 /blog/ 제거
        path = re.sub(r'^(news|blog)/', '', path)
        
        if not path:
            path = 'index'
        
        # 안전한 파일명으로 변환
        filename = path.replace('/', '_') + '.md'
        return filename
    
    def html_to_markdown(self, html_content):
        """HTML을 마크다운으로 변환"""
        try:
            markdown = self.html_converter.handle(str(html_content))
            markdown = re.sub(r'\n{3,}', '\n\n', markdown)
            markdown = markdown.strip()
            if self.compact_mode:
                markdown = self.compress_markdown(markdown)
            return markdown
        except Exception as e:
            print(f"⚠️  Markdown conversion error: {str(e)}")
            return str(html_content)
    
    def extract_metadata(self, soup):
        """포스트 메타데이터 추출"""
        metadata = {}
        
        # 제목
        title_tag = soup.find('h1') or soup.find('title')
        if title_tag:
            metadata['title'] = title_tag.get_text(strip=True)
        
        # 날짜
        date_selectors = ['time', '.date', '.published', '[datetime]']
        for selector in date_selectors:
            date = soup.select_one(selector)
            if date:
                metadata['date'] = date.get('datetime') or date.get_text(strip=True)
                break
        
        # 저자
        author_selectors = ['.author', '.byline', '[rel="author"]']
        for selector in author_selectors:
            author = soup.select_one(selector)
            if author:
                metadata['author'] = author.get_text(strip=True)
                break
        
        # 카테고리
        category_selectors = ['.category', '.tag', '[data-category]']
        for selector in category_selectors:
            category = soup.select_one(selector)
            if category:
                metadata['category'] = category.get_text(strip=True)
                break
        
        return metadata
    
    def save_markdown(self, url, content, metadata=None):
        """마크다운 파일로 저장"""
        filename = self.clean_filename(url)
        filepath = os.path.join(self.output_dir, filename)
        
        # 메타데이터 생성
        meta_lines = ["---", f"source: {url}"]
        
        if metadata:
            for key, value in metadata.items():
                if value:
                    meta_lines.append(f"{key}: {value}")
        
        meta_lines.append("---\n")
        metadata_block = "\n".join(meta_lines)
        
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(metadata_block + "\n" + content)
        
        print(f"✅ Saved: {filename}")
        return filepath
    
    def crawl_post(self, url):
        """단일 블로그 포스트 크롤링"""
        url = self.normalize_url(url)
        if url in self.visited_urls:
            return
        
        print(f"\n🔍 Crawling: {url}")
        self.visited_urls.add(url)
        
        html_content = self.get_page_content(url)
        if not html_content:
            return
        
        soup = BeautifulSoup(html_content, 'html.parser')
        
        # 메타데이터 추출
        metadata = self.extract_metadata(soup)
        
        # 메인 콘텐츠 추출
        main_content = self.extract_main_content(soup)
        self.strip_noise(main_content)
        self.trim_to_first_heading(main_content)
        
        # 마크다운 변환
        markdown_content = self.html_to_markdown(main_content)
        
        # 저장
        self.save_markdown(url, markdown_content, metadata)
    
    def crawl(self, index_urls=None, max_posts=50):
        """전체 블로그 크롤링"""
        if index_urls is None:
            index_urls = [
                "https://www.anthropic.com/news",
                "https://www.anthropic.com/news?category=claude-code",
            ]
        
        print("🚀 Starting blog crawl")
        print(f"📁 Output directory: {self.output_dir}")
        print(f"📄 Max posts: {max_posts}\n")
        
        # 모든 인덱스 페이지에서 블로그 포스트 수집
        all_posts = []
        for index_url in index_urls:
            print(f"📋 Fetching posts from: {index_url}")
            posts = self.extract_blog_posts_from_index(index_url)
            all_posts.extend(posts)
            print(f"   Found {len(posts)} posts")
        
        # 중복 제거
        all_posts = list(set(all_posts))[:max_posts]
        print(f"\n📊 Total unique posts to crawl: {len(all_posts)}\n")
        
        # 각 포스트 크롤링
        for i, post_url in enumerate(all_posts, 1):
            try:
                self.crawl_post(post_url)
                
                # 진행상황 표시
                if i % 5 == 0:
                    print(f"\n📈 Progress: {i}/{len(all_posts)} posts crawled")
                
                # 서버 부하 방지
                time.sleep(1.5)
                
            except Exception as e:
                print(f"❌ Error crawling {post_url}: {str(e)}")
                continue
        
        print(f"\n✨ Crawling complete!")
        print(f"📊 Posts crawled: {len(self.visited_urls)}")
        print(f"📁 Files saved in: {self.output_dir}")
        
        # 통계 저장
        stats = {
            "total_posts": len(self.visited_urls),
            "visited_urls": list(self.visited_urls),
            "output_dir": self.output_dir,
            "index_urls": index_urls
        }
        
        with open(os.path.join(self.output_dir, '_crawl_stats.json'), 'w', encoding='utf-8') as f:
            json.dump(stats, f, indent=2, ensure_ascii=False)


def main():
    """메인 실행 함수"""
    crawler = AnthropicBlogCrawler(output_dir=BLOG_OUTPUT_DIR)
    
    # 크롤링할 인덱스 페이지들
    index_urls = [
        "https://www.anthropic.com/news",
        "https://www.anthropic.com/news?category=claude-code",
        "https://www.anthropic.com/news?category=announcements",
        "https://www.anthropic.com/news?category=product",
    ]
    
    # 크롤링 시작
    crawler.crawl(index_urls=index_urls, max_posts=50)


if __name__ == "__main__":
    main()
