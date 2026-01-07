import os
import unittest

class TestHomePage(unittest.TestCase):
    def test_home_page_content(self):
        with open("web/src/app/page.tsx", "r", encoding='utf-8') as f:
            content = f.read()
            self.assertIn("Welcome to FanPulse", content, "Home page should have Hero title")
            self.assertIn("최신 뉴스", content, "Home page should have News Ticker")

if __name__ == "__main__":
    unittest.main()
