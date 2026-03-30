"""
Test 1.1: API Key 인증 테스트

X-Api-Key 헤더 기반 서비스 간 인증을 검증합니다.
- 키 없이 호출 → 401
- 유효 키 → 200
- 잘못된 키 → 401
- Health 엔드포인트는 키 없이도 200
"""
import os
from django.test import TestCase, override_settings
from rest_framework.test import APIClient


TEST_API_KEY = "test-secret-key-for-testing"


@override_settings(AI_SERVICE_ACCEPTED_KEYS=TEST_API_KEY)
class ApiKeyAuthTest(TestCase):
    """API Key 인증 동작 검증"""

    def setUp(self):
        self.client = APIClient()

    def test_no_key_returns_401(self):
        """API Key 없이 AI 엔드포인트 호출 시 401"""
        response = self.client.post(
            "/api/ai/summarize",
            data={"input_type": "text", "text": "test"},
            format="json",
        )
        self.assertEqual(response.status_code, 401)

    def test_valid_key_returns_success(self):
        """유효한 API Key로 health-like 엔드포인트 호출 시 인증 통과"""
        response = self.client.get(
            "/api/ai/moderate/status",
            HTTP_X_API_KEY=TEST_API_KEY,
        )
        # 인증 통과 확인 (401이 아님)
        self.assertNotEqual(response.status_code, 401)

    def test_invalid_key_returns_401(self):
        """잘못된 API Key로 호출 시 401"""
        response = self.client.post(
            "/api/ai/summarize",
            data={"input_type": "text", "text": "test"},
            format="json",
            HTTP_X_API_KEY="wrong-key",
        )
        self.assertEqual(response.status_code, 401)

    def test_health_endpoint_no_key_required(self):
        """Health 엔드포인트는 API Key 없이도 200"""
        response = self.client.get("/api/health")
        self.assertEqual(response.status_code, 200)

    def test_multiple_accepted_keys(self):
        """쉼표로 구분된 복수 키 지원 (Blue/Green 로테이션)"""
        second_key = "second-rotation-key"
        with self.settings(
            AI_SERVICE_ACCEPTED_KEYS=f"{TEST_API_KEY},{second_key}"
        ):
            response = self.client.get(
                "/api/ai/moderate/status",
                HTTP_X_API_KEY=second_key,
            )
            self.assertNotEqual(response.status_code, 401)
