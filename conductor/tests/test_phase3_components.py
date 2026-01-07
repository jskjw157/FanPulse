import os
import unittest

class TestCoreComponents(unittest.TestCase):
    def test_navigation_components_exist(self):
        components = [
            "web/src/components/layout/Header.tsx",
            "web/src/components/layout/BottomNav.tsx",
            "web/src/components/layout/Sidebar.tsx",
            "web/src/components/layout/MobileMenu.tsx"
        ]
        for component in components:
            self.assertTrue(os.path.exists(component), f"{component} should exist")

    def test_components_have_export(self):
        components = [
            "web/src/components/layout/Header.tsx",
            "web/src/components/layout/BottomNav.tsx"
        ]
        for component in components:
            if os.path.exists(component):
                with open(component, "r", encoding='utf-8') as f:
                    content = f.read()
                    self.assertIn("export default function", content, f"{component} should have default export")

if __name__ == "__main__":
    unittest.main()
