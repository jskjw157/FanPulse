#!/usr/bin/env python3
"""뉴스 worker heartbeat가 허용 지연 안에 갱신됐는지 검사한다."""

import os
import sys
import time
from pathlib import Path

heartbeat = Path(os.getenv("NEWS_COLLECTION_HEARTBEAT_FILE", "/tmp/fanpulse-news-worker.heartbeat"))
interval = int(os.getenv("NEWS_COLLECTION_INTERVAL_SECONDS", "1800"))
max_age = max(interval * 2, 300)

try:
    age = time.time() - int(heartbeat.read_text(encoding="utf-8").strip())
except (OSError, ValueError):
    sys.exit(1)

sys.exit(0 if age <= max_age else 1)
