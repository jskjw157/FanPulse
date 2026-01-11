import os
import unittest

class TestMainLayout(unittest.TestCase):
    def test_main_layout_exists(self):
        self.assertTrue(os.path.exists("web/src/components/layout/MainLayout.tsx"), "MainLayout should exist")

    def test_root_layout_imports_main_layout(self):
        with open("web/src/app/layout.tsx", "r", encoding='utf-8') as f:
            content = f.read()
            self.assertIn("MainLayout", content, "Root layout should use MainLayout")

if __name__ == "__main__":
    unittest.main()
