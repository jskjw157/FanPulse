"""운영 PostgreSQL에 실제 아티스트 뉴스를 한 번 수집한다."""

import json
import os

from django.core.management.base import BaseCommand, CommandError
from django.db import connection

from api.services.news_ingestion import (
    collect_news,
    load_active_artist_targets,
    resolve_news_crawler,
)


class Command(BaseCommand):
    help = "활성 아티스트의 실제 뉴스를 외부 공급원에서 수집해 crawled_news에 upsert합니다."
    requires_system_checks = []

    def add_arguments(self, parser):
        parser.add_argument(
            "--provider",
            default=os.getenv("NEWS_COLLECTION_PROVIDER", "auto"),
            choices=["auto", "naver", "google-news"],
        )
        parser.add_argument(
            "--max-artists",
            type=int,
            default=int(os.getenv("NEWS_COLLECTION_MAX_ARTISTS", "30")),
        )
        parser.add_argument(
            "--per-artist",
            type=int,
            default=int(os.getenv("NEWS_COLLECTION_PER_ARTIST", "10")),
        )

    def handle(self, *args, **options):
        max_artists = options["max_artists"]
        per_artist = options["per_artist"]
        if not 1 <= max_artists <= 200:
            raise CommandError("--max-artists must be between 1 and 200")
        if not 1 <= per_artist <= 100:
            raise CommandError("--per-artist must be between 1 and 100")

        try:
            crawler, source = resolve_news_crawler(options["provider"])
            targets = load_active_artist_targets(connection, max_artists=max_artists)
            if not targets:
                raise CommandError("No active artists found; news collection cannot proceed")

            report = collect_news(
                crawler=crawler,
                targets=targets,
                display=per_artist,
                source=source,
            )
        except CommandError:
            raise
        except Exception as exc:
            raise CommandError(str(exc)) from exc

        self.stdout.write(json.dumps(report, ensure_ascii=False, sort_keys=True))
        if report["failed_queries"] == report["queries"]:
            raise CommandError("All news queries failed")
