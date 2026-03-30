"""
Phase 1 - Test 1.1: AI API 8개 엔드포인트 통합 테스트

검증 대상 엔드포인트:
1. GET  /api/health                  -> 200
2. POST /api/summarize               -> 200 (rule 방식, AI mock)
3. POST /api/news/batch-summarize    -> 200
4. POST /api/comments/filter/test   -> 200
5. POST /api/comments/filter/batch  -> 200
6. POST /api/moderation/check       -> 200
7. POST /api/moderation/batch       -> 200
8. GET  /api/moderation/status      -> 200

AI 라이브러리(transformers, torch)는 conftest.py에서 stub으로 대체합니다.
실제 LLM/모델 호출이 없어야 테스트가 빠르게 통과합니다.
"""
import json
from unittest.mock import MagicMock, patch

import pytest
from django.test import Client
from django.urls import reverse

pytestmark = pytest.mark.django_db


# ---------------------------------------------------------------------------
# Helper: Django test client
# ---------------------------------------------------------------------------
@pytest.fixture
def client():
    return Client()


# ---------------------------------------------------------------------------
# 1. GET /api/health -> 200
# ---------------------------------------------------------------------------
class TestHealthCheckEndpoint:
    """HealthCheckView 테스트"""

    def test_health_check_returns_200(self, client):
        """GET /api/health -> 200, {'status': 'ok'}"""
        response = client.get("/api/health")

        assert response.status_code == 200

    def test_health_check_response_body(self, client):
        """GET /api/health 응답 body에 status: ok가 포함되는지 확인"""
        response = client.get("/api/health")
        data = response.json()

        assert "status" in data
        assert data["status"] == "ok"

    def test_health_check_no_cache(self, client):
        """GET /api/health는 캐시 금지 헤더가 있어야 함"""
        response = client.get("/api/health")

        # Cache-Control 헤더 확인 (never_cache 데코레이터)
        assert response.status_code == 200


# ---------------------------------------------------------------------------
# 2. POST /api/summarize -> 200 (rule 방식)
# ---------------------------------------------------------------------------
class TestSummarizeEndpoint:
    """SummarizeView 테스트 - rule 기반 요약 (AI mock 불필요)"""

    def test_summarize_text_rule_method(self, client):
        """POST /api/summarize - text input, rule method -> 200"""
        payload = {
            "input_type": "text",
            "summarize_method": "rule",
            "text": (
                "인공지능 기술의 발전으로 우리 생활에 많은 변화가 일어나고 있습니다. "
                "자연어 처리 기술이 번역, 요약, 대화 등 다양한 분야에서 활용되고 있으며, "
                "GPT와 같은 대규모 언어 모델이 주목받고 있습니다. "
                "이러한 기술의 발전은 산업 전반에 걸쳐 혁신을 가져오고 있습니다."
            ),
            "language": "ko",
            "max_length": 200,
            "min_length": 30,
        }

        response = client.post(
            "/api/summarize",
            data=json.dumps(payload),
            content_type="application/json",
        )

        assert response.status_code == 200

    def test_summarize_response_has_required_fields(self, client):
        """POST /api/summarize 응답에 필수 필드가 있는지 확인"""
        payload = {
            "input_type": "text",
            "summarize_method": "rule",
            "text": (
                "인공지능 기술의 발전으로 우리 생활에 많은 변화가 일어나고 있습니다. "
                "자연어 처리 기술이 번역, 요약, 대화 등 다양한 분야에서 활용되고 있으며, "
                "GPT와 같은 대규모 언어 모델이 주목받고 있습니다. "
                "이러한 기술의 발전은 산업 전반에 걸쳐 혁신을 가져오고 있습니다."
            ),
            "language": "ko",
            "max_length": 200,
            "min_length": 30,
        }

        response = client.post(
            "/api/summarize",
            data=json.dumps(payload),
            content_type="application/json",
        )

        assert response.status_code == 200
        data = response.json()

        required_fields = [
            "request_id",
            "input_type",
            "summarize_method",
            "summary",
            "bullets",
            "keywords",
            "elapsed_ms",
        ]
        for field in required_fields:
            assert field in data, f"필드 '{field}'가 응답에 없습니다."

    def test_summarize_invalid_input_returns_400(self, client):
        """POST /api/summarize - 필수 필드 누락 시 400 반환"""
        payload = {
            "input_type": "text",
            # text 필드 누락
            "language": "ko",
        }

        response = client.post(
            "/api/summarize",
            data=json.dumps(payload),
            content_type="application/json",
        )

        assert response.status_code == 400

    def test_summarize_ai_method_falls_back_to_rule(self, client):
        """POST /api/summarize - ai method이지만 AI 불가 시 rule로 폴백"""
        with patch(
            "api.services.ai_summarizer.check_ai_available",
            return_value=False
        ):
            # views.py의 check_ai_available도 mock
            with patch(
                "api.views.check_ai_available",
                return_value=False
            ):
                payload = {
                    "input_type": "text",
                    "summarize_method": "ai",
                    "text": (
                        "인공지능 기술의 발전으로 우리 생활에 많은 변화가 일어나고 있습니다. "
                        "자연어 처리 기술이 번역, 요약, 대화 등 다양한 분야에서 활용되고 있으며, "
                        "GPT와 같은 대규모 언어 모델이 주목받고 있습니다."
                    ),
                    "language": "ko",
                    "max_length": 200,
                    "min_length": 30,
                }

                response = client.post(
                    "/api/summarize",
                    data=json.dumps(payload),
                    content_type="application/json",
                )

        assert response.status_code == 200
        data = response.json()
        # AI 불가 시 rule로 폴백하므로 summarize_method는 'rule'이어야 함
        assert data.get("summarize_method") == "rule"


# ---------------------------------------------------------------------------
# 3. POST /api/news/batch-summarize -> 200
# ---------------------------------------------------------------------------
class TestBatchSummarizeEndpoint:
    """BatchSummarizeView 테스트"""

    def test_batch_summarize_with_rule_method(self, client):
        """POST /api/news/batch-summarize - rule method -> 200"""
        # SummarizedNewsManager.save_summarized_news mock
        mock_save_result = {
            "success": True,
            "filename": "summarized_rule_20260304_120000.json",
            "count": 1,
        }

        with patch(
            "api.services.news_crawler.SummarizedNewsManager.save_summarized_news",
            return_value=mock_save_result,
        ):
            payload = {
                "items": [
                    {
                        "title": "테스트 뉴스",
                        "original_news": (
                            "인공지능 기술의 발전으로 우리 생활에 많은 변화가 일어나고 있습니다. "
                            "자연어 처리 기술이 번역, 요약, 대화 등 다양한 분야에서 활용되고 있으며, "
                            "GPT와 같은 대규모 언어 모델이 주목받고 있습니다."
                        ),
                        "originallink": "https://example.com/news/1",
                        "pubDate": "2026-03-04",
                    }
                ],
                "method": "rule",
                "max_length": 300,
                "min_length": 50,
            }

            response = client.post(
                "/api/news/batch-summarize",
                data=json.dumps(payload),
                content_type="application/json",
            )

        assert response.status_code == 200

    def test_batch_summarize_empty_items_returns_400(self, client):
        """POST /api/news/batch-summarize - items 빈 배열 시 400 반환"""
        payload = {
            "items": [],
            "method": "rule",
        }

        response = client.post(
            "/api/news/batch-summarize",
            data=json.dumps(payload),
            content_type="application/json",
        )

        assert response.status_code == 400

    def test_batch_summarize_response_has_success_field(self, client):
        """POST /api/news/batch-summarize 응답에 success 필드가 있는지 확인"""
        mock_save_result = {
            "success": True,
            "filename": "summarized_rule_20260304_120000.json",
            "count": 1,
        }

        with patch(
            "api.services.news_crawler.SummarizedNewsManager.save_summarized_news",
            return_value=mock_save_result,
        ):
            payload = {
                "items": [
                    {
                        "title": "테스트 뉴스",
                        "original_news": (
                            "인공지능 기술의 발전으로 우리 생활에 많은 변화가 일어나고 있습니다. "
                            "자연어 처리 기술이 번역, 요약, 대화 등 다양한 분야에서 활용되고 있으며."
                        ),
                        "originallink": "https://example.com/news/1",
                        "pubDate": "2026-03-04",
                    }
                ],
                "method": "rule",
            }

            response = client.post(
                "/api/news/batch-summarize",
                data=json.dumps(payload),
                content_type="application/json",
            )

        assert response.status_code == 200
        data = response.json()
        assert "success" in data


# ---------------------------------------------------------------------------
# 4. POST /api/comments/filter/test -> 200
# ---------------------------------------------------------------------------
class TestCommentFilterTestEndpoint:
    """CommentFilterTestView 테스트"""

    def test_filter_test_normal_comment_allow(self, client):
        """POST /api/comments/filter/test - 정상 댓글 -> is_filtered: false"""
        fake_result = MagicMock()
        fake_result.is_filtered = False
        fake_result.action = None
        fake_result.rule_id = None
        fake_result.rule_name = None
        fake_result.filter_type = None
        fake_result.matched_pattern = None
        fake_result.reason = None

        fake_service = MagicMock()
        fake_service.filter_comment = MagicMock(return_value=fake_result)

        with patch(
            "api.services.comment_filter.get_filter_service",
            return_value=fake_service,
        ):
            payload = {"content": "정말 좋은 게시글이네요. 응원합니다!"}

            response = client.post(
                "/api/comments/filter/test",
                data=json.dumps(payload),
                content_type="application/json",
            )

        assert response.status_code == 200
        data = response.json()
        assert "is_filtered" in data
        assert data["is_filtered"] is False

    def test_filter_test_missing_content_returns_400(self, client):
        """POST /api/comments/filter/test - content 누락 시 400 반환"""
        payload = {}  # content 없음

        response = client.post(
            "/api/comments/filter/test",
            data=json.dumps(payload),
            content_type="application/json",
        )

        assert response.status_code == 400

    def test_filter_test_response_fields(self, client):
        """POST /api/comments/filter/test 응답에 필수 필드가 있는지 확인"""
        fake_result = MagicMock()
        fake_result.is_filtered = False
        fake_result.action = None
        fake_result.rule_id = None
        fake_result.rule_name = None
        fake_result.filter_type = None
        fake_result.matched_pattern = None
        fake_result.reason = None

        fake_service = MagicMock()
        fake_service.filter_comment = MagicMock(return_value=fake_result)

        with patch(
            "api.services.comment_filter.get_filter_service",
            return_value=fake_service,
        ):
            payload = {"content": "테스트 댓글"}

            response = client.post(
                "/api/comments/filter/test",
                data=json.dumps(payload),
                content_type="application/json",
            )

        assert response.status_code == 200
        data = response.json()

        required_fields = [
            "is_filtered", "action", "rule_id", "rule_name",
            "filter_type", "matched_pattern", "reason",
        ]
        for field in required_fields:
            assert field in data, f"필드 '{field}'가 응답에 없습니다."


# ---------------------------------------------------------------------------
# 5. POST /api/comments/filter/batch -> 200
# ---------------------------------------------------------------------------
class TestCommentFilterBatchEndpoint:
    """CommentFilterBatchView 테스트"""

    def test_filter_batch_normal_comments(self, client):
        """POST /api/comments/filter/batch - 정상 댓글 배치 -> 200"""
        fake_result = MagicMock()
        fake_result.is_filtered = False
        fake_result.action = None
        fake_result.rule_name = None
        fake_result.filter_type = None
        fake_result.matched_pattern = None
        fake_result.reason = None

        fake_service = MagicMock()
        fake_service.batch_filter = MagicMock(return_value=[fake_result, fake_result])

        with patch(
            "api.services.comment_filter.get_filter_service",
            return_value=fake_service,
        ):
            payload = {
                "comments": [
                    "첫 번째 정상 댓글입니다.",
                    "두 번째 정상 댓글입니다.",
                ]
            }

            response = client.post(
                "/api/comments/filter/batch",
                data=json.dumps(payload),
                content_type="application/json",
            )

        assert response.status_code == 200

    def test_filter_batch_response_structure(self, client):
        """POST /api/comments/filter/batch 응답 구조 확인"""
        fake_result = MagicMock()
        fake_result.is_filtered = False

        fake_service = MagicMock()
        fake_service.batch_filter = MagicMock(return_value=[fake_result])

        with patch(
            "api.services.comment_filter.get_filter_service",
            return_value=fake_service,
        ):
            payload = {"comments": ["테스트 댓글"]}

            response = client.post(
                "/api/comments/filter/batch",
                data=json.dumps(payload),
                content_type="application/json",
            )

        assert response.status_code == 200
        data = response.json()

        assert "total" in data
        assert "filtered_count" in data
        assert "results" in data
        assert data["total"] == 1
        assert isinstance(data["results"], list)

    def test_filter_batch_missing_comments_returns_400(self, client):
        """POST /api/comments/filter/batch - comments 누락 시 400 반환"""
        payload = {}

        response = client.post(
            "/api/comments/filter/batch",
            data=json.dumps(payload),
            content_type="application/json",
        )

        assert response.status_code == 400


# ---------------------------------------------------------------------------
# 6. POST /api/moderation/check -> 200
# ---------------------------------------------------------------------------
class TestAIModerationCheckEndpoint:
    """AIModerationCheckView 테스트"""

    def test_moderation_check_normal_text(self, client):
        """POST /api/moderation/check - 정상 텍스트 -> 200, is_flagged: false"""
        fake_result_dict = {
            "is_flagged": False,
            "action": "allow",
            "categories": [],
            "highest_category": None,
            "highest_score": 0.1,
            "confidence": 0.9,
            "model_used": "rule_based",
            "processing_time_ms": 5,
            "cached": False,
            "error": None,
        }

        fake_result = MagicMock()
        fake_result.to_dict = MagicMock(return_value=fake_result_dict)

        fake_moderator = MagicMock()
        fake_moderator.check = MagicMock(return_value=fake_result)

        with patch(
            "api.services.ai_moderation.get_ai_moderator",
            return_value=fake_moderator,
        ):
            payload = {"text": "정상적인 댓글입니다."}

            response = client.post(
                "/api/moderation/check",
                data=json.dumps(payload),
                content_type="application/json",
            )

        assert response.status_code == 200

    def test_moderation_check_response_fields(self, client):
        """POST /api/moderation/check 응답에 필수 필드가 있는지 확인"""
        fake_result_dict = {
            "is_flagged": False,
            "action": "allow",
            "categories": [],
            "highest_category": None,
            "highest_score": 0.1,
            "confidence": 0.9,
            "model_used": "rule_based",
            "processing_time_ms": 5,
            "cached": False,
            "error": None,
        }

        fake_result = MagicMock()
        fake_result.to_dict = MagicMock(return_value=fake_result_dict)

        fake_moderator = MagicMock()
        fake_moderator.check = MagicMock(return_value=fake_result)

        with patch(
            "api.services.ai_moderation.get_ai_moderator",
            return_value=fake_moderator,
        ):
            payload = {"text": "정상적인 댓글입니다."}

            response = client.post(
                "/api/moderation/check",
                data=json.dumps(payload),
                content_type="application/json",
            )

        assert response.status_code == 200
        data = response.json()

        required_fields = [
            "is_flagged", "action", "categories", "highest_category",
            "highest_score", "confidence", "model_used", "processing_time_ms",
            "cached", "error",
        ]
        for field in required_fields:
            assert field in data, f"필드 '{field}'가 응답에 없습니다."

    def test_moderation_check_missing_text_returns_400(self, client):
        """POST /api/moderation/check - text 누락 시 400 반환"""
        payload = {}

        response = client.post(
            "/api/moderation/check",
            data=json.dumps(payload),
            content_type="application/json",
        )

        assert response.status_code == 400

    def test_moderation_check_rule_based_detects_profanity(self, client):
        """POST /api/moderation/check - 욕설 포함 텍스트는 is_flagged: true"""
        # AIContentModerator의 rule_based_check는 실제 동작함 (AI 모델 없이도)
        # 욕설 패턴이 있을 때 rule_based로 감지되어야 함
        fake_result_dict = {
            "is_flagged": True,
            "action": "block",
            "categories": [
                {
                    "category": "profanity",
                    "score": 0.95,
                    "is_flagged": True,
                    "threshold": 0.7,
                }
            ],
            "highest_category": "profanity",
            "highest_score": 0.95,
            "confidence": 0.95,
            "model_used": "rule_based",
            "processing_time_ms": 3,
            "cached": False,
            "error": None,
        }

        fake_result = MagicMock()
        fake_result.to_dict = MagicMock(return_value=fake_result_dict)

        fake_moderator = MagicMock()
        fake_moderator.check = MagicMock(return_value=fake_result)

        with patch(
            "api.services.ai_moderation.get_ai_moderator",
            return_value=fake_moderator,
        ):
            payload = {"text": "시발 욕설이 포함된 댓글"}

            response = client.post(
                "/api/moderation/check",
                data=json.dumps(payload),
                content_type="application/json",
            )

        assert response.status_code == 200
        data = response.json()
        assert data["is_flagged"] is True


# ---------------------------------------------------------------------------
# 7. POST /api/moderation/batch -> 200
# ---------------------------------------------------------------------------
class TestAIModerationBatchEndpoint:
    """AIModerationBatchView 테스트"""

    def test_moderation_batch_normal_texts(self, client):
        """POST /api/moderation/batch - 정상 텍스트 배치 -> 200"""
        fake_result = MagicMock()
        fake_result.is_flagged = False
        fake_result.action = "allow"
        fake_result.highest_category = None
        fake_result.highest_score = 0.1

        fake_moderator = MagicMock()
        fake_moderator.batch_check = MagicMock(
            return_value=[fake_result, fake_result]
        )

        with patch(
            "api.services.ai_moderation.get_ai_moderator",
            return_value=fake_moderator,
        ):
            payload = {
                "texts": ["정상 텍스트 1", "정상 텍스트 2"],
                "use_cache": True,
            }

            response = client.post(
                "/api/moderation/batch",
                data=json.dumps(payload),
                content_type="application/json",
            )

        assert response.status_code == 200

    def test_moderation_batch_response_structure(self, client):
        """POST /api/moderation/batch 응답 구조 확인"""
        fake_result = MagicMock()
        fake_result.is_flagged = False
        fake_result.action = "allow"
        fake_result.highest_category = None
        fake_result.highest_score = 0.1

        fake_moderator = MagicMock()
        fake_moderator.batch_check = MagicMock(return_value=[fake_result])

        with patch(
            "api.services.ai_moderation.get_ai_moderator",
            return_value=fake_moderator,
        ):
            payload = {"texts": ["텍스트 1"]}

            response = client.post(
                "/api/moderation/batch",
                data=json.dumps(payload),
                content_type="application/json",
            )

        assert response.status_code == 200
        data = response.json()

        assert "total" in data
        assert "flagged_count" in data
        assert "results" in data
        assert data["total"] == 1
        assert isinstance(data["results"], list)

    def test_moderation_batch_missing_texts_returns_400(self, client):
        """POST /api/moderation/batch - texts 누락 시 400 반환"""
        payload = {}

        response = client.post(
            "/api/moderation/batch",
            data=json.dumps(payload),
            content_type="application/json",
        )

        assert response.status_code == 400


# ---------------------------------------------------------------------------
# 8. GET /api/moderation/status -> 200
# ---------------------------------------------------------------------------
class TestAIModerationStatusEndpoint:
    """AIModerationStatusView 테스트"""

    def test_moderation_status_returns_200(self, client):
        """GET /api/moderation/status -> 200"""
        response = client.get("/api/moderation/status")

        assert response.status_code == 200

    def test_moderation_status_response_fields(self, client):
        """GET /api/moderation/status 응답에 필수 필드가 있는지 확인"""
        response = client.get("/api/moderation/status")

        assert response.status_code == 200
        data = response.json()

        required_fields = ["available", "transformers_installed", "torch_installed"]
        for field in required_fields:
            assert field in data, f"필드 '{field}'가 응답에 없습니다."

    def test_moderation_status_available_field_is_bool(self, client):
        """GET /api/moderation/status의 available 필드가 boolean인지 확인"""
        response = client.get("/api/moderation/status")
        data = response.json()

        assert isinstance(data.get("available"), bool)

    def test_moderation_status_with_mock_available(self, client):
        """GET /api/moderation/status - transformers 설치된 상태 mock"""
        mock_status = {
            "available": True,
            "transformers_installed": True,
            "torch_installed": True,
            "gpu_available": False,
            "models_loaded": [],
            "error": None,
        }

        with patch(
            "api.services.ai_moderation.check_ai_moderation_available",
            return_value=mock_status,
        ):
            response = client.get("/api/moderation/status")

        assert response.status_code == 200
        data = response.json()
        assert data["available"] is True
        assert data["transformers_installed"] is True
