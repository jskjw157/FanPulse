import pytest


def test_article_extractor_rejects_private_ip_before_network(monkeypatch):
    from api.services.extractor import ArticleExtractor

    connected = []
    monkeypatch.setattr(
        "api.services.url_security.socket.getaddrinfo",
        lambda *args, **kwargs: connected.append(args),
    )

    result = ArticleExtractor().extract("http://127.0.0.1/private")

    assert result["success"] is False
    assert result["error"] == "URL is not allowed"
    assert connected == []


def test_article_extractor_rejects_redirect_to_private_ip(monkeypatch):
    from api.services.extractor import ArticleExtractor
    from api.services.url_security import SafeHttpResponse

    requested = []

    monkeypatch.setattr(
        "api.services.url_security.socket.getaddrinfo",
        lambda host, port, type=None: [(2, 1, 6, "", ("93.184.216.34", port))],
    )

    def fake_request(target, timeout, max_bytes, user_agent):
        requested.append(target.url)
        return SafeHttpResponse(
            url=target.url,
            status=302,
            headers={"location": "http://169.254.169.254/latest/meta-data"},
            body=b"",
        )

    monkeypatch.setattr("api.services.url_security._request_once", fake_request)

    result = ArticleExtractor().extract("https://example.com/article")

    assert result["success"] is False
    assert result["error"] == "URL is not allowed"
    assert requested == ["https://example.com/article"]


@pytest.mark.parametrize("peer_ip", ["127.0.0.1", "93.184.216.35"])
def test_request_rejects_unexpected_connected_peer(monkeypatch, peer_ip):
    from api.services.url_security import ResolvedPublicUrl, UnsafeUrlError, _request_once

    class FakeSocket:
        def settimeout(self, timeout):
            pass

        def connect(self, sockaddr):
            pass

        def getpeername(self):
            return (peer_ip, 80)

        def close(self):
            pass

    class FakeConnection:
        requested = False

        def __init__(self, host, port, timeout):
            self.sock = None

        def request(self, method, target, headers):
            self.requested = True

        def close(self):
            pass

    connection = FakeConnection("", 0, 0)
    monkeypatch.setattr("api.services.url_security.socket.socket", lambda *args: FakeSocket())
    monkeypatch.setattr(
        "api.services.url_security.http.client.HTTPConnection",
        lambda *args, **kwargs: connection,
    )
    target = ResolvedPublicUrl(
        url="http://example.com/",
        scheme="http",
        host="example.com",
        port=80,
        request_target="/",
        host_header="example.com",
        family=2,
        socktype=1,
        proto=6,
        sockaddr=("93.184.216.34", 80),
    )

    with pytest.raises(UnsafeUrlError, match="Connected peer"):
        _request_once(target, timeout=1, max_bytes=128, user_agent="test")

    assert connection.requested is False


@pytest.mark.parametrize(
    "url",
    [
        "http://127.1/private",
        "http://0177.0.0.1/private",
        "http://2130706433/private",
        "https://example.com:0/article",
    ],
)
def test_storable_url_rejects_legacy_ipv4_and_explicit_zero_port(url):
    from api.services.url_security import UnsafeUrlError, normalize_storable_article_url

    with pytest.raises(UnsafeUrlError):
        normalize_storable_article_url(url)
