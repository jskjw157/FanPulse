import os
import unittest

class TestErrorPages(unittest.TestCase):
    def test_error_pages_exist(self):
        self.assertTrue(os.path.exists("web/src/app/not-found.tsx"), "not-found.tsx should exist")
        self.assertTrue(os.path.exists("web/src/app/error.tsx"), "error.tsx should exist")

if __name__ == "__main__":
    unittest.main()
