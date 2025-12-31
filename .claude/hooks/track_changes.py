#!/usr/bin/env python3
"""
PostToolUse Hook: Track file changes for code review.
Logs modified files to .claude/pending-review.json
"""
import json
import sys
import os
from datetime import datetime
from pathlib import Path

PENDING_FILE = Path(os.environ.get("CLAUDE_PROJECT_DIR", ".")) / ".claude" / "pending-review.json"

def load_pending():
    if PENDING_FILE.exists():
        try:
            return json.loads(PENDING_FILE.read_text())
        except:
            pass
    return {"files": [], "last_updated": None}

def save_pending(data):
    data["last_updated"] = datetime.now().isoformat()
    PENDING_FILE.parent.mkdir(parents=True, exist_ok=True)
    PENDING_FILE.write_text(json.dumps(data, indent=2, ensure_ascii=False))

def main():
    try:
        input_data = json.load(sys.stdin)
        file_path = input_data.get("tool_input", {}).get("file_path", "")

        if not file_path:
            return

        # Skip non-code files
        skip_patterns = [".json", ".md", ".txt", ".yml", ".yaml", ".lock", ".log"]
        if any(file_path.endswith(p) for p in skip_patterns):
            return

        # Track code files
        code_extensions = [".kt", ".java", ".py", ".ts", ".tsx", ".js", ".jsx", ".swift", ".go"]
        if not any(file_path.endswith(ext) for ext in code_extensions):
            return

        pending = load_pending()

        if file_path not in pending["files"]:
            pending["files"].append(file_path)
            save_pending(pending)
            print(f"[Review Tracker] Added: {os.path.basename(file_path)}")

    except Exception as e:
        # Silent fail - don't interrupt workflow
        pass

if __name__ == "__main__":
    main()
