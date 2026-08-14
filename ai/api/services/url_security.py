"""외부 기사 URL 저장 및 다운로드를 위한 공통 보안 경계."""

from dataclasses import dataclass
import http.client
import ipaddress
import socket
import ssl
from typing import Mapping, Optional
from urllib.parse import urljoin, urlsplit, urlunsplit


class UnsafeUrlError(ValueError):
    """URL이 허용된 공개 HTTP(S) 대상이 아닐 때 발생한다."""


class ExternalResponseError(RuntimeError):
    """외부 서버가 허용 범위를 벗어난 응답을 반환했을 때 발생한다."""


ALLOWED_PORTS = {"http": 80, "https": 443}
REDIRECT_STATUSES = {301, 302, 303, 307, 308}
MAX_ARTICLE_URL_LENGTH = 500
MAX_REDIRECTS = 5
MAX_ARTICLE_RESPONSE_BYTES = 5 * 1024 * 1024


@dataclass(frozen=True)
class ResolvedPublicUrl:
    url: str
    scheme: str
    host: str
    port: int
    request_target: str
    host_header: str
    family: int
    socktype: int
    proto: int
    sockaddr: tuple


@dataclass(frozen=True)
class SafeHttpResponse:
    url: str
    status: int
    headers: Mapping[str, str]
    body: bytes


def normalize_storable_article_url(url: str) -> str:
    """DB에 저장 가능한 HTTP(S) 기사 URL인지 네트워크 요청 없이 검증한다."""
    normalized = (url or "").strip()
    if (
        not normalized
        or len(normalized) > MAX_ARTICLE_URL_LENGTH
        or any(ord(character) < 32 for character in normalized)
    ):
        raise UnsafeUrlError("URL is not allowed")

    try:
        parsed = urlsplit(normalized)
        scheme = parsed.scheme.lower()
        host = parsed.hostname
        port = ALLOWED_PORTS.get(scheme) if parsed.port is None else parsed.port
    except ValueError as exc:
        raise UnsafeUrlError("URL is not allowed") from exc

    if (
        scheme not in ALLOWED_PORTS
        or not host
        or parsed.username is not None
        or parsed.password is not None
        or port != ALLOWED_PORTS[scheme]
    ):
        raise UnsafeUrlError("URL is not allowed")

    lowered_host = host.rstrip(".").lower()
    if (
        lowered_host == "localhost"
        or lowered_host.endswith(".localhost")
        or lowered_host.endswith(".local")
        or lowered_host.endswith(".internal")
    ):
        raise UnsafeUrlError("URL is not allowed")

    try:
        direct_ip = ipaddress.ip_address(lowered_host)
    except ValueError:
        direct_ip = None
    if direct_ip is not None and not direct_ip.is_global:
        raise UnsafeUrlError("URL is not allowed")
    if direct_ip is None:
        try:
            socket.inet_aton(lowered_host)
        except OSError:
            pass
        else:
            # 127.1, 0177.0.0.1, 단일 정수형 등 parser별 의미가 다른 IPv4 표기는 거부한다.
            raise UnsafeUrlError("URL is not allowed")

    # Fragment는 서버로 전송되지 않으며 같은 기사의 중복 키만 늘리므로 제거한다.
    return urlunsplit((scheme, parsed.netloc, parsed.path or "/", parsed.query, ""))


def first_safe_article_url(*candidates: Optional[str]) -> Optional[str]:
    """문법과 현재 DNS 결과가 모두 안전한 첫 저장 후보를 반환한다."""
    for candidate in candidates:
        try:
            return resolve_public_url(candidate or "").url
        except UnsafeUrlError:
            continue
    return None


def resolve_public_url(url: str) -> ResolvedPublicUrl:
    """URL의 모든 DNS 결과가 공개 IP인지 확인하고 실제 접속 주소를 고정한다."""
    normalized = normalize_storable_article_url(url)
    parsed = urlsplit(normalized)
    scheme = parsed.scheme.lower()
    host = parsed.hostname
    if host is None:  # normalize 단계가 보장하지만 타입을 명확히 한다.
        raise UnsafeUrlError("URL is not allowed")
    port = ALLOWED_PORTS[scheme] if parsed.port is None else parsed.port

    try:
        addresses = socket.getaddrinfo(host, port, type=socket.SOCK_STREAM)
    except OSError as exc:
        raise UnsafeUrlError("URL is not allowed") from exc
    if not addresses:
        raise UnsafeUrlError("URL is not allowed")

    public_addresses = []
    for family, socktype, proto, _canonical_name, sockaddr in addresses:
        try:
            address = ipaddress.ip_address(sockaddr[0])
        except ValueError as exc:
            raise UnsafeUrlError("URL is not allowed") from exc
        if not address.is_global:
            # public/private가 섞인 DNS 응답도 fail-closed 처리한다.
            raise UnsafeUrlError("URL is not allowed")
        public_addresses.append((family, socktype, proto, sockaddr))

    family, socktype, proto, sockaddr = public_addresses[0]
    request_target = parsed.path or "/"
    if parsed.query:
        request_target = f"{request_target}?{parsed.query}"
    default_port = ALLOWED_PORTS[scheme]
    display_host = f"[{host}]" if ":" in host else host
    host_header = display_host if port == default_port else f"{display_host}:{port}"

    return ResolvedPublicUrl(
        url=normalized,
        scheme=scheme,
        host=host,
        port=port,
        request_target=request_target,
        host_header=host_header,
        family=family,
        socktype=socktype,
        proto=proto,
        sockaddr=sockaddr,
    )


def _request_once(
    target: ResolvedPublicUrl,
    timeout: float,
    max_bytes: int,
    user_agent: str,
) -> SafeHttpResponse:
    """검증 시 선택한 sockaddr에 직접 연결해 DNS 재해석을 피한다."""
    raw_socket = socket.socket(target.family, target.socktype, target.proto)
    connection = http.client.HTTPConnection(target.host, target.port, timeout=timeout)
    try:
        raw_socket.settimeout(timeout)
        raw_socket.connect(target.sockaddr)
        try:
            peer = raw_socket.getpeername()
            peer_address = ipaddress.ip_address(peer[0])
            expected_address = ipaddress.ip_address(target.sockaddr[0])
            peer_port = int(peer[1])
        except (OSError, ValueError, TypeError, IndexError) as exc:
            raise UnsafeUrlError("Connected peer is not allowed") from exc
        if (
            not peer_address.is_global
            or peer_address != expected_address
            or peer_port != target.port
        ):
            raise UnsafeUrlError("Connected peer is not allowed")
        if target.scheme == "https":
            context = ssl.create_default_context()
            connected_socket = context.wrap_socket(raw_socket, server_hostname=target.host)
        else:
            connected_socket = raw_socket
        connection.sock = connected_socket
        connection.request(
            "GET",
            target.request_target,
            headers={
                "Host": target.host_header,
                "User-Agent": user_agent,
                "Accept": "text/html,application/xhtml+xml",
                "Accept-Encoding": "identity",
                "Connection": "close",
            },
        )
        response = connection.getresponse()
        content_length = response.getheader("Content-Length")
        if content_length:
            try:
                if int(content_length) > max_bytes:
                    raise ExternalResponseError("External response is too large")
            except ValueError:
                pass
        body = response.read(max_bytes + 1)
        if len(body) > max_bytes:
            raise ExternalResponseError("External response is too large")
        headers = {name.lower(): value for name, value in response.getheaders()}
        return SafeHttpResponse(
            url=target.url,
            status=response.status,
            headers=headers,
            body=body,
        )
    finally:
        connection.close()
        try:
            raw_socket.close()
        except OSError:
            pass


def safe_fetch_url(
    url: str,
    *,
    timeout: float = 10,
    max_bytes: int = MAX_ARTICLE_RESPONSE_BYTES,
    max_redirects: int = MAX_REDIRECTS,
    user_agent: str = "FanPulse/1.0",
) -> SafeHttpResponse:
    """공개 URL만 고정 IP로 다운로드하고 redirect마다 같은 검증을 반복한다."""
    current_url = normalize_storable_article_url(url)
    for redirect_count in range(max_redirects + 1):
        target = resolve_public_url(current_url)
        response = _request_once(target, timeout, max_bytes, user_agent)
        if response.status not in REDIRECT_STATUSES:
            if response.status < 200 or response.status >= 300:
                raise ExternalResponseError(f"External server returned HTTP {response.status}")
            return response

        location = response.headers.get("location")
        if not location or redirect_count == max_redirects:
            raise ExternalResponseError("Too many or invalid redirects")
        current_url = normalize_storable_article_url(urljoin(current_url, location))

    raise ExternalResponseError("Too many redirects")
