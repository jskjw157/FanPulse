"""
공통 유효성 검증 유틸리티
- SSRF 방지 URL 검증
- 파일명 검증
"""
import ipaddress
import socket
import logging
from urllib.parse import urlparse

logger = logging.getLogger(__name__)


def validate_url(url: str) -> bool:
    """
    URL 안전성 검증 (SSRF 방지)

    차단 대상:
    - http/https 외 스킴
    - 프라이빗 IP (10.x, 172.16-31.x, 192.168.x)
    - 루프백 (127.x, localhost)
    - 링크 로컬 (169.254.x)

    Args:
        url: 검증할 URL

    Returns:
        bool: 안전하면 True

    Raises:
        ValueError: 안전하지 않은 URL인 경우
    """
    parsed = urlparse(url)

    # 스킴 검증
    if parsed.scheme not in ('http', 'https'):
        raise ValueError(f"허용되지 않는 프로토콜: {parsed.scheme}")

    hostname = parsed.hostname
    if not hostname:
        raise ValueError("호스트명이 없습니다.")

    # 블랙리스트 호스트 검증
    blacklist = ['localhost', '0.0.0.0']
    if hostname.lower() in blacklist:
        raise ValueError(f"차단된 호스트: {hostname}")

    # IP 주소 검증
    try:
        ip = ipaddress.ip_address(socket.gethostbyname(hostname))
        if ip.is_private or ip.is_loopback or ip.is_link_local:
            raise ValueError(f"내부 네트워크 접근 차단: {ip}")
    except socket.gaierror:
        # DNS 해석 실패 - 외부 URL이므로 통과
        pass

    return True


def validate_filename(filename: str) -> bool:
    """
    파일명 안전성 검증 (경로 조작 방지)

    Args:
        filename: 검증할 파일명

    Returns:
        bool: 안전하면 True

    Raises:
        ValueError: 안전하지 않은 파일명인 경우
    """
    if not filename:
        raise ValueError("파일명이 비어있습니다.")

    if '..' in filename or '/' in filename or '\\' in filename:
        raise ValueError("잘못된 파일명입니다.")

    if not filename.endswith('.json'):
        raise ValueError("JSON 파일만 허용됩니다.")

    return True
