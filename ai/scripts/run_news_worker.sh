#!/bin/sh
set -eu

interval_seconds="${NEWS_COLLECTION_INTERVAL_SECONDS:-1800}"
heartbeat_file="${NEWS_COLLECTION_HEARTBEAT_FILE:-/tmp/fanpulse-news-worker.heartbeat}"

case "$interval_seconds" in
  ''|*[!0-9]*) echo "NEWS_COLLECTION_INTERVAL_SECONDS must be a positive integer" >&2; exit 2 ;;
esac
if [ "$interval_seconds" -lt 60 ]; then
  echo "NEWS_COLLECTION_INTERVAL_SECONDS must be at least 60" >&2
  exit 2
fi

while true; do
  python manage.py collect_news
  date +%s > "$heartbeat_file"
  sleep "$interval_seconds"
done
