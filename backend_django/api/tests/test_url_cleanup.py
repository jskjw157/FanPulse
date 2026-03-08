"""
Test 1.3: URL 리네임 테스트

새 URL 패턴이 올바르게 라우팅되고, 이전 URL은 404를 반환하는지 검증합니다.
- /api/ai/summarize, /api/ai/filter, /api/ai/moderate 라우팅 확인
- 이전 URL (/api/summarize 등) → 404
- /api/health → 유지
"""
from django.test import TestCase, override_settings
from rest_framework.test import APIClient


@override_settings(AI_SERVICE_ACCEPTED_KEYS="test-key")
class NewUrlPatternTest(TestCase):
    """새 URL 패턴 라우팅 검증"""

    def setUp(self):
        self.client = APIClient()
        self.auth_header = {"HTTP_X_API_KEY": "test-key"}

    # ─────────────────────────────────────────
    # 새 URL이 정상 라우팅되는지 확인
    # (인증 통과 후 비즈니스 에러는 OK — 라우팅만 확인)
    # ─────────────────────────────────────────

    def test_new_summarize_url_routed(self):
        """/api/ai/summarize가 라우팅됨 (404가 아님)"""
        response = self.client.post(
            "/api/ai/summarize",
            data={"input_type": "text", "text": "test"},
            format="json",
            **self.auth_header,
        )
        self.assertNotEqual(response.status_code, 404)

    def test_new_filter_url_routed(self):
        """/api/ai/filter가 라우팅됨"""
        response = self.client.post(
            "/api/ai/filter",
            data={"comment": "test"},
            format="json",
            **self.auth_header,
        )
        self.assertNotEqual(response.status_code, 404)

    def test_new_moderate_url_routed(self):
        """/api/ai/moderate가 라우팅됨"""
        response = self.client.post(
            "/api/ai/moderate",
            data={"text": "test"},
            format="json",
            **self.auth_header,
        )
        self.assertNotEqual(response.status_code, 404)

    def test_new_moderate_status_url_routed(self):
        """/api/ai/moderate/status가 라우팅됨"""
        response = self.client.get(
            "/api/ai/moderate/status",
            **self.auth_header,
        )
        self.assertNotEqual(response.status_code, 404)

    # ─────────────────────────────────────────
    # 이전 URL이 404를 반환하는지 확인
    # ─────────────────────────────────────────

    def test_old_summarize_url_returns_404(self):
        """이전 /api/summarize → 404"""
        response = self.client.post(
            "/api/summarize",
            data={"input_type": "text", "text": "test"},
            format="json",
            **self.auth_header,
        )
        self.assertEqual(response.status_code, 404)

    def test_old_filter_url_returns_404(self):
        """이전 /api/comments/filter/test → 404"""
        response = self.client.post(
            "/api/comments/filter/test",
            data={"comment": "test"},
            format="json",
            **self.auth_header,
        )
        self.assertEqual(response.status_code, 404)

    def test_old_moderation_url_returns_404(self):
        """이전 /api/moderation/check → 404"""
        response = self.client.post(
            "/api/moderation/check",
            data={"text": "test"},
            format="json",
            **self.auth_header,
        )
        self.assertEqual(response.status_code, 404)

    # ─────────────────────────────────────────
    # Health 엔드포인트 유지 확인
    # ─────────────────────────────────────────

    def test_health_url_unchanged(self):
        """/api/health는 변경 없이 유지"""
        response = self.client.get("/api/health")
        self.assertEqual(response.status_code, 200)
