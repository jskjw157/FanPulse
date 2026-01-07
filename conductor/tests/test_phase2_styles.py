import os
import unittest

class TestGlobalStyles(unittest.TestCase):
    def test_globals_css_exists(self):
        self.assertTrue(os.path.exists("web/src/app/globals.css"), "globals.css should exist")

    def test_layout_includes_remixicon(self):
        with open("web/src/app/layout.tsx", "r", encoding='utf-8') as f:
            content = f.read()
            self.assertIn("remixicon.css", content, "Layout should include Remix Icon CDN")

    def test_layout_includes_pacifico_font(self):
        with open("web/src/app/layout.tsx", "r", encoding='utf-8') as f:
            content = f.read()
            self.assertIn("Pacifico", content, "Layout should include Pacifico font")

    def test_globals_css_includes_brand_color(self):
        with open("web/src/app/globals.css", "r", encoding='utf-8') as f:
            content = f.read()
            self.assertIn("--color-brand: #FF2D55", content, "globals.css should include brand color")

if __name__ == "__main__":
    unittest.main()