import os
import json
import unittest

class TestNextJsSetup(unittest.TestCase):
    def test_package_json_exists(self):
        self.assertTrue(os.path.exists("web/package.json"), "web/package.json should exist")

    def test_dependencies(self):
        if not os.path.exists("web/package.json"):
            self.fail("web/package.json does not exist")
        
        with open("web/package.json", "r") as f:
            data = json.load(f)
            deps = data.get("dependencies", {})
            dev_deps = data.get("devDependencies", {})
            all_deps = {**deps, **dev_deps}
            
            self.assertIn("next", all_deps, "next should be installed")
            self.assertIn("tailwindcss", all_deps, "tailwindcss should be installed")
            self.assertIn("framer-motion", all_deps, "framer-motion should be installed")

if __name__ == "__main__":
    unittest.main()
