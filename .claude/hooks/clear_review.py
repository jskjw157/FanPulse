#!/usr/bin/env python3
"""
PreToolUse Hook: Clear pending review when /review command is detected.
Triggered when user runs a review-related slash command.
"""
import json
import sys
import os
from pathlib import Path

PENDING_FILE = Path(os.environ.get("CLAUDE_PROJECT_DIR", ".")) / ".claude" / "pending-review.json"

def clear_pending():
    if PENDING_FILE.exists():
        PENDING_FILE.unlink()
        return True
    return False

def main():
    try:
        input_data = json.load(sys.stdin)

        # Check if this is a Skill invocation for code-review
        tool_name = input_data.get("tool_name", "")
        skill_name = input_data.get("tool_input", {}).get("skill", "")

        # Clear pending when code-review skill is invoked
        if tool_name == "Skill" and "review" in skill_name.lower():
            if clear_pending():
                print("[Review Tracker] Pending review cleared.")

        sys.exit(0)

    except Exception as e:
        sys.exit(0)

if __name__ == "__main__":
    main()
