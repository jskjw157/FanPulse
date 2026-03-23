"""
RFC7807 Problem Details 커스텀 exception handler.

모든 DRF 에러 응답을 RFC7807 (application/problem+json) 형식으로 변환합니다.
https://datatracker.ietf.org/doc/html/rfc7807
"""
from rest_framework.exceptions import NotAuthenticated
from rest_framework.views import exception_handler as drf_exception_handler


def rfc7807_exception_handler(exc, context):
    """
    DRF 예외를 RFC7807 Problem Details 형식으로 변환.

    Response body:
    {
        "type": "about:blank",
        "title": "Unauthorized",
        "status": 401,
        "detail": "Valid API key required.",
        "instance": "/api/ai/summarize"
    }
    """
    response = drf_exception_handler(exc, context)

    if response is None:
        return None

    # DRF는 WWW-Authenticate 헤더 없이 NotAuthenticated를 403으로 변환함.
    # API Key 인증에서는 401이 올바르므로 원래 상태 코드 복원.
    if isinstance(exc, NotAuthenticated):
        response.status_code = 401

    status_code = response.status_code

    # 기존 DRF 응답에서 detail 추출
    detail_text = ""
    if isinstance(response.data, dict):
        detail_text = response.data.get("detail", str(response.data))
    elif isinstance(response.data, list):
        detail_text = "; ".join(str(item) for item in response.data)
    else:
        detail_text = str(response.data)

    # RFC7807 표준 필드
    problem = {
        "type": "about:blank",
        "title": _status_title(status_code),
        "status": status_code,
        "detail": str(detail_text),
    }

    # request path를 instance로 추가
    request = context.get("request")
    if request:
        problem["instance"] = request.path

    response.data = problem
    response._is_rfc7807 = True  # Middleware에서 Content-Type 교체용 플래그
    return response


class RFC7807ContentTypeMiddleware:
    """RFC7807 응답의 Content-Type을 application/problem+json으로 설정."""

    def __init__(self, get_response):
        self.get_response = get_response

    def __call__(self, request):
        response = self.get_response(request)
        if getattr(response, "_is_rfc7807", False):
            response["Content-Type"] = "application/problem+json"
        return response


def _status_title(status_code: int) -> str:
    """HTTP 상태 코드 → 표준 제목 매핑."""
    titles = {
        400: "Bad Request",
        401: "Unauthorized",
        403: "Forbidden",
        404: "Not Found",
        405: "Method Not Allowed",
        406: "Not Acceptable",
        409: "Conflict",
        415: "Unsupported Media Type",
        429: "Too Many Requests",
        500: "Internal Server Error",
    }
    return titles.get(status_code, "Error")
