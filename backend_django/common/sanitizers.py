"""
공통 데이터 정제 유틸리티
- HTML 태그 제거 (XSS 방지)
"""
import logging

logger = logging.getLogger(__name__)


def strip_html(text: str) -> str:
    """
    HTML 태그 제거 및 특수문자 변환 (XSS 방지)

    bleach 라이브러리 사용으로 정규식 우회 공격 방지

    Args:
        text: 정제할 텍스트

    Returns:
        str: HTML 태그가 제거된 텍스트
    """
    if not text:
        return ""

    try:
        import bleach
        text = bleach.clean(text, tags=[], strip=True)
    except ImportError:
        # bleach 미설치 시 기본 정규식 폴백
        import re
        logger.warning("bleach 미설치 - 기본 정규식 사용")
        text = re.sub(r"<[^>]+>", "", text)

    text = text.replace("&quot;", '"').replace("&amp;", "&")
    text = text.replace("&lt;", "<").replace("&gt;", ">")
    return text.strip()
