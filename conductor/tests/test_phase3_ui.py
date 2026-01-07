import os
import unittest

class TestUIComponents(unittest.TestCase):
    def test_ui_components_exist(self):
        components = [
            "web/src/components/ui/Button.tsx",
            "web/src/components/ui/Badge.tsx",
            "web/src/components/ui/Card.tsx"
        ]
        for component in components:
            self.assertTrue(os.path.exists(component), f"{component} should exist")

    def test_button_uses_framer_motion(self):
        if os.path.exists("web/src/components/ui/Button.tsx"):
            with open("web/src/components/ui/Button.tsx", "r", encoding='utf-8') as f:
                content = f.read()
                self.assertIn("framer-motion", content, "Button should use framer-motion")
                self.assertIn("motion.button", content, "Button should use motion.button")

if __name__ == "__main__":
    unittest.main()
