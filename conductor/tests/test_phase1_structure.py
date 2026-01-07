import os
import unittest

class TestFolderStructure(unittest.TestCase):
    def test_folders_exist(self):
        required_folders = [
            "web/src/app",
            "web/src/components",
            "web/src/hooks",
            "web/src/lib",
            "web/src/types"
        ]
        for folder in required_folders:
            self.assertTrue(os.path.exists(folder), f"{folder} should exist")

if __name__ == "__main__":
    unittest.main()
