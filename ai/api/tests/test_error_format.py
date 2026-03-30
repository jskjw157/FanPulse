"""
Test 1.2: RFC7807 Problem Details 에러 포맷 테스트

에러 응답이 RFC7807 (application/problem+json) 형식인지 검증합니다.
- 401 응답에 type, title, status, detail 필드 포함
- Content-Type: application/problem+json
"""
from django.test import TestCase, override_settings
from rest_framework.test import APIClient


@override_settings(AI_SERVICE_ACCEPTED_KEYS="test-key")
class RFC7807ErrorFormatTest(TestCase):
    """RFC7807 Problem Details 에러 포맷 검증"""

    def setUp(self):
        self.client = APIClient()

    def test_401_has_rfc7807_fields(self):
        """401 응답에 RFC7807 필수 필드 포함"""
        response = self.client.post(
            "/api/ai/summarize",
            data={"input_type": "text", "text": "test"},
            format="json",
        )
        self.assertEqual(response.status_code, 401)

        data = response.json()
        self.assertIn("type", data)
        self.assertIn("title", data)
        self.assertIn("status", data)
        self.assertIn("detail", data)
        self.assertEqual(data["status"], 401)

    def test_401_content_type_is_problem_json(self):
        """401 응답의 Content-Type이 application/problem+json"""
        response = self.client.post(
            "/api/ai/summarize",
            data={"input_type": "text", "text": "test"},
            format="json",
        )
        self.assertEqual(response.status_code, 401)
        self.assertIn(
            "application/problem+json",
            response["Content-Type"],
        )

    def test_404_is_returned_for_nonexistent_url(self):
        """존재하지 않는 URL → 404"""
        response = self.client.get(
            "/api/nonexistent",
            HTTP_X_API_KEY="test-key",
        )
        self.assertEqual(response.status_code, 404)
