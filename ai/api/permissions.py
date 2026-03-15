"""
API Key 인증 Permission 클래스.

Spring 백엔드 → Django AI Sidecar 서비스 간 인증에 사용됩니다.
X-Api-Key 헤더를 통해 인증하며, hmac.compare_digest로 타이밍 공격을 방지합니다.

설정:
- AI_SERVICE_ACCEPTED_KEYS (Django settings 또는 env): 쉼표 구분 키 목록
- 듀얼 키 지원으로 Blue/Green 로테이션 가능
"""
import hmac
import logging

from django.conf import settings
from rest_framework.exceptions import NotAuthenticated
from rest_framework.permissions import BasePermission

logger = logging.getLogger(__name__)

API_KEY_HEADER = "HTTP_X_API_KEY"


class ApiKeyPermission(BasePermission):
    """
    X-Api-Key 헤더 기반 서비스 간 인증.

    settings.AI_SERVICE_ACCEPTED_KEYS에 정의된 키와 비교합니다.
    쉼표로 구분된 복수 키를 지원하여 무중단 키 로테이션이 가능합니다.

    NotAuthenticated를 직접 raise하여 DRF가 401을 반환하도록 합니다.
    (BasePermission의 기본 동작은 403을 반환)
    """

    def has_permission(self, request, view):
        api_key = request.META.get(API_KEY_HEADER, "")
        if not api_key:
            raise NotAuthenticated("Valid API key required.")

        accepted_keys_raw = getattr(settings, "AI_SERVICE_ACCEPTED_KEYS", "")
        if not accepted_keys_raw:
            logger.error("AI_SERVICE_ACCEPTED_KEYS is not configured")
            raise NotAuthenticated("Valid API key required.")

        accepted_keys = [k.strip() for k in accepted_keys_raw.split(",") if k.strip()]

        if not any(hmac.compare_digest(api_key, key) for key in accepted_keys):
            raise NotAuthenticated("Valid API key required.")

        return True
