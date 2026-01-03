import sys
import unittest
from pathlib import Path

from bs4 import BeautifulSoup

ROOT = Path(__file__).resolve().parents[1]
CODEX_ROOT = Path(__file__).resolve().parents[2] / "codex_crawler"
sys.path.insert(0, str(ROOT))
if CODEX_ROOT.exists():
    sys.path.insert(0, str(CODEX_ROOT))

from claude_code_crawler import ClaudeCodeCrawler
from anthropic_blog_crawler import AnthropicBlogCrawler
from codex_docs_crawler import CodexDocsCrawler


class TestClaudeCodeCrawlerFilters(unittest.TestCase):
    def setUp(self):
        self.crawler = ClaudeCodeCrawler()

    def test_docs_en_allowed(self):
        self.assertTrue(
            self.crawler.is_valid_doc_url("https://code.claude.com/docs/en/overview")
        )

    def test_docs_ko_blocked(self):
        self.assertFalse(
            self.crawler.is_valid_doc_url("https://code.claude.com/docs/ko/overview")
        )

    def test_assets_blocked(self):
        self.assertFalse(
            self.crawler.is_valid_doc_url("https://code.claude.com/docs/en/overview.png")
        )

    def test_normalize_url(self):
        normalized = self.crawler.normalize_url(
            "https://code.claude.com/docs/en/overview?x=1#y"
        )
        self.assertEqual(normalized, "https://code.claude.com/docs/en/overview")

    def test_exclude_pattern(self):
        crawler = ClaudeCodeCrawler(exclude_path_patterns=[r"/docs/en/iam"])
        self.assertFalse(crawler.is_valid_doc_url("https://code.claude.com/docs/en/iam"))

    def test_strip_noise(self):
        html = """
        <html><body>
        <main>
          <nav>Nav</nav>
          <aside>Aside</aside>
          <header><div>Logo</div></header>
          <header><h1>Title</h1></header>
          <section>Keep me</section>
          <footer>Footer</footer>
        </main>
        </body></html>
        """
        soup = BeautifulSoup(html, "html.parser")
        content = soup.find("main")
        self.crawler.strip_noise(content)
        self.assertIsNone(content.find("nav"))
        self.assertIsNone(content.find("aside"))
        self.assertIsNone(content.find("footer"))
        headers = content.find_all("header")
        self.assertTrue(any(header.find("h1") for header in headers))
        self.assertEqual(len(headers), 1)

    def test_trim_to_first_heading(self):
        html = """
        <html><body>
        <main>
          <p>Menu</p>
          <section>
            <h1>Title</h1>
            <p>Body</p>
          </section>
        </main>
        </body></html>
        """
        soup = BeautifulSoup(html, "html.parser")
        content = soup.find("main")
        self.crawler.trim_to_first_heading(content)
        self.assertIsNone(content.find(string="Menu"))
        self.assertIsNotNone(content.find(string="Body"))

    def test_compress_markdown(self):
        markdown = "Copy\n\n# Title\n\n\n```\nCopy\n```\n\n\nText\n"
        compressed = self.crawler.compress_markdown(markdown)
        self.assertTrue(compressed.startswith("# Title"))
        self.assertIn("```\nCopy\n```", compressed)
        self.assertNotIn("\n\n\n", compressed)
        self.assertNotIn("# Title\n\n```", compressed)
        self.assertNotIn("```\n\nText", compressed)

    def test_compact_mode_keeps_links(self):
        self.assertFalse(self.crawler.html_converter.ignore_links)
        self.assertTrue(self.crawler.html_converter.ignore_images)


class TestAnthropicBlogCrawlerFilters(unittest.TestCase):
    def setUp(self):
        self.crawler = AnthropicBlogCrawler()

    def test_news_post_allowed(self):
        self.assertTrue(
            self.crawler.is_valid_post_url(
                "https://www.anthropic.com/news/claude-opus-4-5"
            )
        )

    def test_news_index_blocked(self):
        self.assertFalse(self.crawler.is_valid_post_url("https://www.anthropic.com/news"))

    def test_blog_post_allowed(self):
        self.assertTrue(
            self.crawler.is_valid_post_url("https://www.anthropic.com/blog/some-post")
        )

    def test_assets_blocked(self):
        self.assertFalse(
            self.crawler.is_valid_post_url("https://www.anthropic.com/news/foo.png")
        )

    def test_domain_allowed(self):
        self.assertTrue(
            self.crawler.is_valid_post_url(
                "https://anthropic.com/news/claude-opus-4-5"
            )
        )

    def test_normalize_url(self):
        normalized = self.crawler.normalize_url(
            "https://www.anthropic.com/news/x?y=1#z"
        )
        self.assertEqual(normalized, "https://www.anthropic.com/news/x")

    def test_exclude_pattern(self):
        crawler = AnthropicBlogCrawler(
            exclude_path_patterns=[r"/news/claude-opus-4-5"]
        )
        self.assertFalse(
            crawler.is_valid_post_url("https://www.anthropic.com/news/claude-opus-4-5")
        )

    def test_trim_to_first_heading(self):
        html = """
        <html><body>
        <main>
          <p>Menu</p>
          <section>
            <h1>Title</h1>
            <p>Body</p>
          </section>
        </main>
        </body></html>
        """
        soup = BeautifulSoup(html, "html.parser")
        content = soup.find("main")
        self.crawler.trim_to_first_heading(content)
        self.assertIsNone(content.find(string="Menu"))
        self.assertIsNotNone(content.find(string="Body"))

    def test_compress_markdown(self):
        markdown = "Copy\n\n# Title\n\n\n```\nCopy\n```\n\n\nText\n"
        compressed = self.crawler.compress_markdown(markdown)
        self.assertTrue(compressed.startswith("# Title"))
        self.assertIn("```\nCopy\n```", compressed)
        self.assertNotIn("\n\n\n", compressed)
        self.assertNotIn("# Title\n\n```", compressed)
        self.assertNotIn("```\n\nText", compressed)

    def test_compact_mode_keeps_links(self):
        self.assertFalse(self.crawler.html_converter.ignore_links)
        self.assertTrue(self.crawler.html_converter.ignore_images)


class TestCodexDocsCrawlerFilters(unittest.TestCase):
    def setUp(self):
        self.crawler = CodexDocsCrawler(urls=["https://developers.openai.com/codex/cli/"])

    def test_normalize_url(self):
        normalized = self.crawler.normalize_url(
            "https://developers.openai.com/codex/cli/?x=1#y"
        )
        self.assertEqual(normalized, "https://developers.openai.com/codex/cli/")

    def test_clean_filename(self):
        filename = self.crawler.clean_filename(
            "https://developers.openai.com/codex/cli/reference/"
        )
        self.assertEqual(filename, "cli_reference.md")

    def test_trim_to_first_heading(self):
        html = """
        <html><body>
        <main>
          <p>Menu</p>
          <section>
            <h1>Title</h1>
            <p>Body</p>
          </section>
        </main>
        </body></html>
        """
        soup = BeautifulSoup(html, "html.parser")
        content = soup.find("main")
        self.crawler.trim_to_first_heading(content)
        self.assertIsNone(content.find(string="Menu"))
        self.assertIsNotNone(content.find(string="Body"))

    def test_compress_markdown(self):
        markdown = "Copy\n\n# Title\n\n\n```\nCopy\n```\n\n\nText\n"
        compressed = self.crawler.compress_markdown(markdown)
        self.assertTrue(compressed.startswith("# Title"))
        self.assertIn("```\nCopy\n```", compressed)
        self.assertNotIn("\n\n\n", compressed)
        self.assertNotIn("# Title\n\n```", compressed)
        self.assertNotIn("```\n\nText", compressed)

    def test_compact_mode_keeps_links(self):
        self.assertFalse(self.crawler.html_converter.ignore_links)
        self.assertTrue(self.crawler.html_converter.ignore_images)


if __name__ == "__main__":
    unittest.main()
