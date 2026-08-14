"""뉴스 자동 수집 파이프라인 회귀 테스트."""

import pytest


@pytest.fixture(autouse=True)
def stable_public_dns(monkeypatch):
    """뉴스 저장 테스트가 외부 DNS 가용성에 의존하지 않게 한다."""
    monkeypatch.setattr(
        "api.services.url_security.socket.getaddrinfo",
        lambda host, port, type=None: [(2, 1, 6, "", ("93.184.216.34", port))],
    )


SAMPLE_GOOGLE_NEWS_RSS = b"""<?xml version="1.0" encoding="UTF-8"?>
<rss version="2.0">
  <channel>
    <item>
      <title>BTS announces a new tour - Example News</title>
      <link>https://news.google.com/rss/articles/real-article-id?oc=5</link>
      <guid>real-article-id</guid>
      <pubDate>Thu, 13 Aug 2026 08:29:45 GMT</pubDate>
      <description><![CDATA[<a href="https://example.com/article">BTS tour details</a>]]></description>
      <source url="https://example.com">Example News</source>
    </item>
  </channel>
</rss>
"""


class FakeHttpResponse:
    def __init__(self, body: bytes):
        self.body = body

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_value, traceback):
        return False

    def read(self, size: int = -1) -> bytes:
        del size
        return self.body


def test_google_news_rss_crawler_maps_real_feed_fields(monkeypatch):
    from api.services.news_crawler import GoogleNewsRssCrawler

    requested_urls = []

    def fake_urlopen(request, timeout):
        requested_urls.append(request.full_url)
        assert timeout == 10
        return FakeHttpResponse(SAMPLE_GOOGLE_NEWS_RSS)

    monkeypatch.setattr("urllib.request.urlopen", fake_urlopen)

    result = GoogleNewsRssCrawler().search("K-pop", display=10)

    assert result["success"] is True
    assert len(result["items"]) == 1
    assert "q=K-pop" in requested_urls[0]
    assert result["items"][0] == {
        "title": "BTS announces a new tour - Example News",
        "description": "BTS tour details",
        "originallink": "https://news.google.com/rss/articles/real-article-id?oc=5",
        "link": "https://news.google.com/rss/articles/real-article-id?oc=5",
        "pubDate": "Thu, 13 Aug 2026 08:29:45 GMT",
        "pubDateFormatted": "2026-08-13 08:29",
        "source": "Example News",
    }


def test_google_news_rss_crawler_rejects_oversized_response(monkeypatch):
    from api.services.news_crawler import MAX_PROVIDER_RESPONSE_BYTES, GoogleNewsRssCrawler

    class OversizedResponse(FakeHttpResponse):
        def read(self, size: int = -1) -> bytes:
            assert size == MAX_PROVIDER_RESPONSE_BYTES + 1
            return b"x" * size

    monkeypatch.setattr(
        "urllib.request.urlopen",
        lambda request, timeout: OversizedResponse(b""),
    )

    result = GoogleNewsRssCrawler().search("K-pop", display=10)

    assert result["success"] is False
    assert result["items"] == []
    assert "too large" in result["error"].lower()


@pytest.mark.django_db
def test_save_news_to_db_upserts_by_url_and_preserves_source():
    from api.models import CrawledNews
    from api.services.news_crawler import save_news_to_db

    url = "https://news.google.com/rss/articles/real-article-id?oc=5"
    first = [{
        "title": "BTS first title",
        "description": "first description",
        "originallink": url,
        "pubDate": "Thu, 13 Aug 2026 08:29:45 GMT",
        "source": "Example News",
    }]
    updated = [{
        **first[0],
        "title": "BTS updated title",
        "description": "updated description",
    }]

    first_result = save_news_to_db(first, source="google-news")
    second_result = save_news_to_db(updated, source="google-news")

    assert first_result == {"success": True, "count": 1, "error": None}
    assert second_result == {"success": True, "count": 0, "error": None}
    assert CrawledNews.objects.filter(url=url).count() == 1
    news = CrawledNews.objects.get(url=url)
    assert news.title == "BTS updated title"
    assert news.content == "updated description"
    assert news.source == "Example News"


@pytest.mark.django_db
def test_save_news_to_db_rejects_unsafe_article_urls():
    from api.models import CrawledNews
    from api.services.news_crawler import save_news_to_db

    result = save_news_to_db([
        {"title": "script", "originallink": "javascript:alert(1)"},
        {"title": "loopback", "originallink": "http://127.0.0.1/admin"},
        {"title": "private", "originallink": "http://10.0.0.5/article"},
    ])

    assert result == {"success": True, "count": 0, "error": None}
    assert CrawledNews.objects.count() == 0


@pytest.mark.django_db
def test_save_news_to_db_rejects_hostname_resolving_to_private_ip(monkeypatch):
    from api.models import CrawledNews
    from api.services.news_crawler import save_news_to_db

    monkeypatch.setattr(
        "api.services.url_security.socket.getaddrinfo",
        lambda host, port, type=None: [(2, 1, 6, "", ("127.0.0.1", port))],
    )

    result = save_news_to_db([
        {"title": "private DNS", "originallink": "https://private.example/article"},
    ])

    assert result == {"success": True, "count": 0, "error": None}
    assert CrawledNews.objects.count() == 0


@pytest.mark.django_db
def test_save_news_to_db_merges_artist_relations_for_existing_url():
    from api.models import CrawledNews, CrawledNewsArtist
    from api.services.news_crawler import save_news_to_db

    url = "https://news.google.com/rss/articles/shared?oc=5"
    first_artist = "11111111-1111-1111-1111-111111111111"
    second_artist = "22222222-2222-2222-2222-222222222222"
    base = {
        "title": "shared K-pop article",
        "description": "real article description",
        "originallink": url,
        "pubDate": "Thu, 13 Aug 2026 08:29:45 GMT",
        "source": "Example News",
    }

    save_news_to_db([{**base, "artist_ids": [first_artist]}], source="google-news")
    save_news_to_db([{**base, "artist_ids": [second_artist]}], source="google-news")

    news = CrawledNews.objects.get(url=url)
    assert CrawledNews.objects.filter(url=url).count() == 1
    assert set(
        str(value)
        for value in CrawledNewsArtist.objects.filter(news=news).values_list("artist_id", flat=True)
    ) == {first_artist, second_artist}


@pytest.mark.django_db
def test_save_news_to_db_rolls_back_news_when_artist_relation_fails(monkeypatch):
    from api.models import CrawledNews, CrawledNewsArtist
    from api.services.news_crawler import save_news_to_db

    def fail_relation_insert(*args, **kwargs):
        raise RuntimeError("relation insert failed")

    monkeypatch.setattr(CrawledNewsArtist.objects, "bulk_create", fail_relation_insert)

    result = save_news_to_db([{
        "title": "atomic article",
        "description": "must not remain after relation failure",
        "originallink": "https://example.com/atomic",
        "artist_ids": ["11111111-1111-1111-1111-111111111111"],
    }])

    assert result["success"] is False
    assert result["count"] == 0
    assert CrawledNews.objects.count() == 0
    assert CrawledNewsArtist.objects.count() == 0


@pytest.mark.django_db
def test_save_news_to_db_rolls_back_whole_batch_when_later_relation_fails(monkeypatch):
    from api.models import CrawledNews, CrawledNewsArtist
    from api.services.news_crawler import save_news_to_db

    original_bulk_create = CrawledNewsArtist.objects.bulk_create
    calls = 0

    def fail_second_relation_insert(*args, **kwargs):
        nonlocal calls
        calls += 1
        if calls == 2:
            raise RuntimeError("second relation insert failed")
        return original_bulk_create(*args, **kwargs)

    monkeypatch.setattr(CrawledNewsArtist.objects, "bulk_create", fail_second_relation_insert)

    result = save_news_to_db([
        {
            "title": "first atomic article",
            "originallink": "https://example.com/atomic-first",
            "artist_ids": ["11111111-1111-1111-1111-111111111111"],
        },
        {
            "title": "second atomic article",
            "originallink": "https://example.com/atomic-second",
            "artist_ids": ["22222222-2222-2222-2222-222222222222"],
        },
    ])

    assert result["success"] is False
    assert result["count"] == 0
    assert CrawledNews.objects.count() == 0
    assert CrawledNewsArtist.objects.count() == 0


def test_collect_news_deduplicates_urls_and_merges_artist_relations():
    from api.services.news_ingestion import ArtistNewsTarget, collect_news

    bts_id = "11111111-1111-1111-1111-111111111111"
    blackpink_id = "22222222-2222-2222-2222-222222222222"

    class FakeCrawler:
        def search(self, query, display):
            slug = "bts" if "BTS" in query else "blackpink"
            shared = {
                "title": f"{query} shared article",
                "originallink": "https://example.com/shared",
                "link": "https://example.com/shared",
            }
            unique = {
                "title": f"{query} unique article",
                "originallink": f"https://example.com/{slug}",
                "link": f"https://example.com/{slug}",
            }
            return {"success": True, "items": [shared, unique], "error": None}

    captured = {}

    def fake_save(items, source):
        captured["items"] = items
        captured["source"] = source
        return {"success": True, "count": len(items), "error": None}

    report = collect_news(
        crawler=FakeCrawler(),
        targets=[
            ArtistNewsTarget(artist_id=bts_id, name="BTS", english_name=None),
            ArtistNewsTarget(artist_id=blackpink_id, name="BLACKPINK", english_name=None),
        ],
        display=10,
        source="google-news",
        save_fn=fake_save,
    )

    assert [item["originallink"] for item in captured["items"]] == [
        "https://example.com/shared",
        "https://example.com/bts",
        "https://example.com/blackpink",
    ]
    assert captured["items"][0]["artist_ids"] == [bts_id, blackpink_id]
    assert captured["items"][1]["artist_ids"] == [bts_id]
    assert captured["items"][2]["artist_ids"] == [blackpink_id]
    assert captured["source"] == "google-news"
    assert report == {
        "queries": 2,
        "fetched": 4,
        "unique": 3,
        "inserted": 3,
        "failed_queries": 0,
        "errors": [],
    }


def test_collect_news_keeps_successful_results_when_one_query_fails():
    from api.services.news_ingestion import ArtistNewsTarget, collect_news

    class PartiallyFailingCrawler:
        def search(self, query, display):
            if "FAIL" in query:
                return {"success": False, "items": [], "error": "upstream timeout"}
            return {
                "success": True,
                "items": [{
                    "title": "real article",
                    "originallink": "https://example.com/real",
                    "link": "https://example.com/real",
                }],
                "error": None,
            }

    saved = []

    def fake_save(items, source):
        saved.extend(items)
        return {"success": True, "count": len(items), "error": None}

    report = collect_news(
        crawler=PartiallyFailingCrawler(),
        targets=[
            ArtistNewsTarget(artist_id="11111111-1111-1111-1111-111111111111", name="FAIL", english_name=None),
            ArtistNewsTarget(artist_id="22222222-2222-2222-2222-222222222222", name="BTS", english_name=None),
        ],
        display=10,
        source="google-news",
        save_fn=fake_save,
    )

    assert len(saved) == 1
    assert report["failed_queries"] == 1
    assert report["errors"] == ["query=FAIL: upstream timeout"]
    assert report["inserted"] == 1


def test_collect_news_keeps_successful_results_when_one_query_raises():
    from api.services.news_ingestion import ArtistNewsTarget, collect_news

    class RaisingCrawler:
        def search(self, query: str, display: int = 20) -> dict:
            if "FAIL" in query:
                raise TimeoutError("provider socket timed out\nwith control text")
            return {
                "success": True,
                "items": [{
                    "title": "real article after another query raised",
                    "originallink": "https://example.com/real-after-raise",
                    "link": "https://example.com/real-after-raise",
                }],
                "error": None,
            }

    saved = []

    report = collect_news(
        crawler=RaisingCrawler(),
        targets=[
            ArtistNewsTarget(
                artist_id="11111111-1111-1111-1111-111111111111",
                name="FAIL",
                english_name=None,
            ),
            ArtistNewsTarget(
                artist_id="22222222-2222-2222-2222-222222222222",
                name="BTS",
                english_name=None,
            ),
        ],
        display=10,
        source="google-news",
        save_fn=lambda items, source: saved.extend(items) or {
            "success": True,
            "count": len(items),
            "error": None,
        },
    )

    assert len(saved) == 1
    assert report["failed_queries"] == 1
    assert report["errors"] == [
        "query=FAIL: TimeoutError: provider socket timed out with control text"
    ]
    assert report["inserted"] == 1


def test_collect_news_discards_unsafe_urls_before_deduplication():
    from api.services.news_ingestion import ArtistNewsTarget, collect_news

    class UnsafeCrawler:
        def search(self, query, display):
            return {
                "success": True,
                "items": [
                    {"title": "unsafe", "originallink": "http://169.254.169.254/latest/meta-data"},
                    {"title": "safe", "originallink": "https://example.com/real"},
                ],
                "error": None,
            }

    captured = []
    report = collect_news(
        crawler=UnsafeCrawler(),
        targets=[ArtistNewsTarget(
            artist_id="11111111-1111-1111-1111-111111111111",
            name="BTS",
            english_name=None,
        )],
        display=10,
        source="google-news",
        save_fn=lambda items, source: captured.extend(items) or {
            "success": True,
            "count": len(items),
            "error": None,
        },
    )

    assert [item["originallink"] for item in captured] == ["https://example.com/real"]
    assert report["fetched"] == 2
    assert report["unique"] == 1


def test_collect_news_discards_private_dns_before_deduplication(monkeypatch):
    from api.services.news_ingestion import ArtistNewsTarget, collect_news

    monkeypatch.setattr(
        "api.services.url_security.socket.getaddrinfo",
        lambda host, port, type=None: [(2, 1, 6, "", ("10.0.0.7", port))],
    )

    class PrivateDnsCrawler:
        def search(self, query: str, display: int) -> dict:
            return {
                "success": True,
                "items": [{
                    "title": "private DNS",
                    "originallink": "https://private.example/article",
                    "link": "https://private.example/article",
                }],
                "error": None,
            }

    captured = []
    report = collect_news(
        crawler=PrivateDnsCrawler(),
        targets=[ArtistNewsTarget(
            artist_id="11111111-1111-1111-1111-111111111111",
            name="BTS",
            english_name=None,
        )],
        display=10,
        source="google-news",
        save_fn=lambda items, source: captured.extend(items) or {
            "success": True,
            "count": len(items),
            "error": None,
        },
    )

    assert captured == []
    assert report["unique"] == 0
    assert report["inserted"] == 0


def test_collect_news_command_skips_unrelated_web_system_checks():
    from api.management.commands.collect_news import Command

    assert Command.requires_system_checks == []


def test_resolve_news_crawler_uses_google_rss_without_naver_credentials():
    from api.services.news_ingestion import resolve_news_crawler
    from api.services.news_crawler import GoogleNewsRssCrawler

    crawler, source = resolve_news_crawler("auto", environ={})

    assert isinstance(crawler, GoogleNewsRssCrawler)
    assert source == "google-news"


def test_resolve_news_crawler_uses_naver_when_credentials_exist():
    from api.services.news_ingestion import resolve_news_crawler
    from api.services.news_crawler import NaverNewsCrawler

    crawler, source = resolve_news_crawler(
        "auto",
        environ={"NAVER_CLIENT_ID": "client", "NAVER_CLIENT_SECRET": "secret"},
    )

    assert isinstance(crawler, NaverNewsCrawler)
    assert source == "naver"


def test_load_active_artist_targets_reads_real_spring_artist_table():
    from api.services.news_ingestion import load_active_artist_targets

    class FakeCursor:
        def __init__(self):
            self.sql = None
            self.params = None

        def execute(self, sql, params):
            self.sql = sql
            self.params = params

        def fetchall(self):
            return [
                ("11111111-1111-1111-1111-111111111111", "BTS", None),
                ("22222222-2222-2222-2222-222222222222", "BLACKPINK", "BLACKPINK"),
                ("11111111-1111-1111-1111-111111111111", "BTS", None),
            ]

        def __enter__(self):
            return self

        def __exit__(self, exc_type, exc_value, traceback):
            return False

    class FakeConnection:
        def __init__(self):
            self.fake_cursor = FakeCursor()

        def cursor(self):
            return self.fake_cursor

    connection = FakeConnection()
    targets = load_active_artist_targets(connection, max_artists=20)

    assert [target.artist_id for target in targets] == [
        "11111111-1111-1111-1111-111111111111",
        "22222222-2222-2222-2222-222222222222",
    ]
    assert [target.query for target in targets] == ['"BTS" K-pop', '"BLACKPINK" K-pop']
    assert "SELECT id, name, english_name" in connection.fake_cursor.sql
    assert "active = TRUE" in connection.fake_cursor.sql
    assert connection.fake_cursor.params == [20]
