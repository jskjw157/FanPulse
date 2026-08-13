"""실제 뉴스 공급원 조회와 Django ORM 저장을 연결하는 배치 서비스."""

import os
from dataclasses import dataclass
from typing import Callable, Iterable, Mapping, Optional, Protocol

from .news_crawler import GoogleNewsRssCrawler, NaverNewsCrawler, save_news_to_db


class NewsCrawler(Protocol):
    def search(self, query: str, display: int = 20) -> dict:
        ...


@dataclass(frozen=True)
class ArtistNewsTarget:
    artist_id: str
    name: str
    english_name: Optional[str]

    @property
    def query(self) -> str:
        search_name = (self.english_name or self.name).strip()
        return f'"{search_name}" K-pop'


def resolve_news_crawler(provider: str, environ: Mapping[str, str] = os.environ):
    """설정된 공급원을 반환한다. auto는 Naver 키가 없을 때 RSS로 안전하게 전환한다."""
    normalized = provider.strip().lower()
    if normalized not in {"auto", "naver", "google-news"}:
        raise ValueError(f"unsupported news provider: {provider}")

    has_naver_credentials = bool(
        environ.get("NAVER_CLIENT_ID") and environ.get("NAVER_CLIENT_SECRET")
    )
    if normalized == "naver" or (normalized == "auto" and has_naver_credentials):
        crawler = NaverNewsCrawler()
        crawler.client_id = environ.get("NAVER_CLIENT_ID")
        crawler.client_secret = environ.get("NAVER_CLIENT_SECRET")
        if not crawler.is_available():
            raise RuntimeError("Naver provider requires NAVER_CLIENT_ID and NAVER_CLIENT_SECRET")
        return crawler, "naver"

    return GoogleNewsRssCrawler(), "google-news"


def load_active_artist_targets(connection, max_artists: int) -> list[ArtistNewsTarget]:
    """Spring 운영 artists 테이블에서 UUID를 포함한 뉴스 검색 target을 만든다."""
    with connection.cursor() as cursor:
        cursor.execute(
            """
            SELECT id, name, english_name
            FROM artists
            WHERE active = TRUE
            ORDER BY name
            LIMIT %s
            """,
            [max_artists],
        )
        rows = cursor.fetchall()

    targets = []
    seen_ids = set()
    for artist_id, name, english_name in rows:
        normalized_name = (name or "").strip()
        normalized_id = str(artist_id)
        if not normalized_name or normalized_id in seen_ids:
            continue
        seen_ids.add(normalized_id)
        targets.append(
            ArtistNewsTarget(
                artist_id=normalized_id,
                name=normalized_name,
                english_name=(english_name or "").strip() or None,
            )
        )
    return targets


def collect_news(
    crawler: NewsCrawler,
    targets: Iterable[ArtistNewsTarget],
    display: int,
    source: str,
    save_fn: Callable[[list, str], dict] = save_news_to_db,
) -> dict:
    """검색어별 실제 기사 메타데이터를 수집하고 URL 기준으로 한 번만 저장한다."""
    target_list = list(targets)
    items_by_url: dict[str, dict] = {}
    fetched = 0
    errors = []

    for target in target_list:
        query = target.query
        result = crawler.search(query, display=display)
        if not result.get("success"):
            errors.append(f"query={target.name}: {result.get('error') or 'unknown error'}")
            continue

        items = result.get("items") or []
        fetched += len(items)
        for item in items:
            url = (item.get("originallink") or item.get("link") or "").strip()
            if not url:
                continue
            stored = items_by_url.get(url)
            if stored is None:
                stored = dict(item)
                stored["artist_ids"] = []
                items_by_url[url] = stored
            if target.artist_id not in stored["artist_ids"]:
                stored["artist_ids"].append(target.artist_id)

    unique_items = list(items_by_url.values())

    save_result = save_fn(unique_items, source) if unique_items else {
        "success": True,
        "count": 0,
        "error": None,
    }
    if not save_result.get("success"):
        raise RuntimeError(f"news DB upsert failed: {save_result.get('error') or 'unknown error'}")

    return {
        "queries": len(target_list),
        "fetched": fetched,
        "unique": len(unique_items),
        "inserted": save_result.get("count", 0),
        "failed_queries": len(errors),
        "errors": errors,
    }
