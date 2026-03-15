"""
Phase 1 Baseline Tests - conftest.py
공통 fixtures 및 AI 서비스 mock 설정

AI 모델(transformers, torch)은 테스트 환경에서 실제 로딩이 불필요하므로
pytest monkeypatch/mock으로 전부 대체합니다.
"""
import os
import sys
import types
import uuid
from unittest.mock import MagicMock, patch

import pytest

# Force SQLite for tests regardless of .env
os.environ["USE_POSTGRES"] = "false"
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "config.settings")


# ---------------------------------------------------------------------------
# AI 라이브러리 stub - import 전에 sys.modules에 삽입
# ---------------------------------------------------------------------------
def _make_torch_stub():
    """torch 모듈 stub 생성"""
    torch = types.ModuleType("torch")
    torch.cuda = MagicMock()
    torch.cuda.is_available = MagicMock(return_value=False)
    torch.cuda.get_device_name = MagicMock(return_value="NO GPU")
    torch.cuda.get_device_properties = MagicMock()
    torch.version = MagicMock()
    torch.version.cuda = None
    torch.__version__ = "2.0.0+stub"
    torch.no_grad = MagicMock(return_value=MagicMock(__enter__=MagicMock(), __exit__=MagicMock()))
    torch.float16 = "float16"

    class _FakeNoGrad:
        def __enter__(self):
            return self

        def __exit__(self, *args):
            pass

    torch.no_grad = _FakeNoGrad
    return torch


def _make_transformers_stub():
    """transformers 모듈 stub 생성"""
    transformers = types.ModuleType("transformers")
    transformers.__version__ = "4.40.0+stub"
    transformers.pipeline = MagicMock(return_value=MagicMock())
    transformers.AutoTokenizer = MagicMock()
    transformers.AutoModelForCausalLM = MagicMock()
    transformers.BitsAndBytesConfig = MagicMock()
    return transformers


def pytest_configure(config):
    """
    pytest 시작 전 AI 라이브러리 mock을 sys.modules에 삽입.
    이 시점에 삽입해야 서비스 모듈 import 시 stub이 사용됩니다.
    """
    if "torch" not in sys.modules:
        sys.modules["torch"] = _make_torch_stub()
    if "transformers" not in sys.modules:
        sys.modules["transformers"] = _make_transformers_stub()


# ---------------------------------------------------------------------------
# Django 설정 (pytest-django)
# ---------------------------------------------------------------------------
# django_db_setup 제거 - pytest-django 기본 동작(migrate 실행)을 사용


# ---------------------------------------------------------------------------
# FilterResult mock helper
# ---------------------------------------------------------------------------
class FakeFilterResult:
    """CommentFilterService.filter_comment() 반환값 stub"""

    def __init__(self, is_filtered=False):
        self.is_filtered = is_filtered
        self.action = "BLOCK" if is_filtered else None
        self.rule_id = "RULE-001" if is_filtered else None
        self.rule_name = "Test Rule" if is_filtered else None
        self.filter_type = "LLM" if is_filtered else None
        self.matched_pattern = None
        self.reason = "LLM 판단: 부적절한 표현" if is_filtered else None


class FakeModerationResult:
    """AIContentModerator.check() 반환값 stub"""

    def __init__(self, is_flagged=False):
        self.is_flagged = is_flagged
        self.action = "block" if is_flagged else "allow"
        self.categories = []
        self.highest_category = "profanity" if is_flagged else None
        self.highest_score = 0.95 if is_flagged else 0.1
        self.confidence = 0.95 if is_flagged else 0.9
        self.model_used = "rule_based"
        self.processing_time_ms = 10
        self.cached = False
        self.error = None

    def to_dict(self):
        return {
            "is_flagged": self.is_flagged,
            "action": self.action,
            "categories": self.categories,
            "highest_category": self.highest_category,
            "highest_score": round(self.highest_score, 4),
            "confidence": round(self.confidence, 4),
            "model_used": self.model_used,
            "processing_time_ms": self.processing_time_ms,
            "cached": self.cached,
            "error": self.error,
        }


# ---------------------------------------------------------------------------
# Fixtures
# ---------------------------------------------------------------------------
@pytest.fixture
def mock_filter_service_allow(monkeypatch):
    """CommentFilterService.filter_comment() -> 통과(allow) stub"""
    fake = MagicMock()
    fake.filter_comment = MagicMock(return_value=FakeFilterResult(is_filtered=False))
    fake.batch_filter = MagicMock(
        side_effect=lambda comments: [FakeFilterResult(is_filtered=False) for _ in comments]
    )
    monkeypatch.setattr(
        "api.services.comment_filter.get_filter_service",
        MagicMock(return_value=fake),
    )
    return fake


@pytest.fixture
def mock_filter_service_block(monkeypatch):
    """CommentFilterService.filter_comment() -> 차단(block) stub"""
    fake = MagicMock()
    fake.filter_comment = MagicMock(return_value=FakeFilterResult(is_filtered=True))
    fake.batch_filter = MagicMock(
        side_effect=lambda comments: [FakeFilterResult(is_filtered=True) for _ in comments]
    )
    monkeypatch.setattr(
        "api.services.comment_filter.get_filter_service",
        MagicMock(return_value=fake),
    )
    return fake


@pytest.fixture
def mock_ai_moderator_allow(monkeypatch):
    """AIContentModerator.check() -> 허용(allow) stub"""
    fake_moderator = MagicMock()
    fake_moderator.check = MagicMock(return_value=FakeModerationResult(is_flagged=False))
    fake_moderator.batch_check = MagicMock(
        side_effect=lambda texts, use_cache=True: [
            FakeModerationResult(is_flagged=False) for _ in texts
        ]
    )
    monkeypatch.setattr(
        "api.services.ai_moderation.get_ai_moderator",
        MagicMock(return_value=fake_moderator),
    )
    return fake_moderator


@pytest.fixture
def mock_summarizer(monkeypatch):
    """ArticleSummarizer.summarize() stub (rule-based fallback)"""
    fake_result = {
        "summary": "테스트 요약 결과입니다.",
        "bullets": ["핵심 포인트 1", "핵심 포인트 2"],
        "keywords": ["키워드1", "키워드2"],
    }
    mock = MagicMock()
    mock.return_value.summarize = MagicMock(return_value=fake_result)
    monkeypatch.setattr("api.services.summarizer.ArticleSummarizer", mock)
    return mock


@pytest.fixture
def sample_news_text():
    """뉴스 요약 테스트용 샘플 텍스트"""
    return (
        "인공지능 기술의 발전으로 우리 생활에 많은 변화가 일어나고 있습니다. "
        "자연어 처리 기술이 번역, 요약, 대화 등 다양한 분야에서 활용되고 있으며, "
        "GPT와 같은 대규모 언어 모델이 주목받고 있습니다."
    )


@pytest.fixture
def valid_comment_content():
    """정상 댓글 샘플"""
    return "정말 좋은 게시글이네요. 응원합니다!"


@pytest.fixture
def crawled_news_data():
    """CrawledNews 모델 생성용 fixture 데이터"""
    return {
        "title": "테스트 뉴스 제목",
        "content": "테스트 뉴스 본문입니다.",
        "url": "https://example.com/news/1",
        "source": "테스트 출처",
    }


@pytest.fixture
def comment_filter_rule_data():
    """CommentFilterRule 모델 생성용 fixture 데이터"""
    return {
        "name": "테스트 금칙어 필터",
        "filter_type": "keyword",
        "pattern": "욕설1,욕설2",
        "action": "block",
        "is_active": True,
        "priority": 10,
    }
