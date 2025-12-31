#!/usr/bin/env python3
"""
PreToolUse Hook: Block git commit if pending review exists.
Provides feedback to Claude to run /review first.
"""
import json
import sys
import os
from pathlib import Path

PENDING_FILE = Path(os.environ.get("CLAUDE_PROJECT_DIR", ".")) / ".claude" / "pending-review.json"

def load_pending():
    if PENDING_FILE.exists():
        try:
            return json.loads(PENDING_FILE.read_text())
        except:
            pass
    return {"files": [], "last_updated": None}

def main():
    try:
        input_data = json.load(sys.stdin)
        command = input_data.get("tool_input", {}).get("command", "")

        # Only check git commit commands
        if "git commit" not in command:
            sys.exit(0)

        pending = load_pending()

        if pending.get("files"):
            file_count = len(pending["files"])
            file_list = "\n".join(f"  - {f}" for f in pending["files"][:5])
            if file_count > 5:
                file_list += f"\n  ... and {file_count - 5} more"

            print(f"""
BLOCKED: Code review required before commit.

{file_count} file(s) have been modified but not reviewed:
{file_list}

Please run `/review` to review changes before committing.
After review, the pending list will be cleared automatically.
""")
            sys.exit(2)  # Block the commit

        sys.exit(0)  # Allow commit

    except Exception as e:
        sys.exit(0)  # On error, allow commit

if __name__ == "__main__":
    main()
