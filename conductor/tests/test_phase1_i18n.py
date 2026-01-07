import os
import unittest

class TestI18nSetup(unittest.TestCase):
    def test_i18n_config_exists(self):
        self.assertTrue(os.path.exists("web/src/i18n/index.ts"), "web/src/i18n/index.ts should exist")
        self.assertTrue(os.path.exists("web/src/i18n/local/index.ts"), "web/src/i18n/local/index.ts should exist")

if __name__ == "__main__":
    unittest.main()
