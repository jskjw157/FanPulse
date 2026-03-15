"""
Phase 2 - Test 2.2: 제거 대상 모델 import 실패 테스트

40+ 모델 중 AI 전용 4개 모델만 남기고 나머지는 제거되었는지 검증합니다.

유지 대상 (4개 + BaseModel):
- BaseModel (abstract)
- CrawledNews
- Comment
- CommentFilterRule
- FilteredCommentLog

제거 대상 (전체 목록):
- User, AuthToken, OAuthAccount, UserSettings, UserDailyMission
- Artist, UserFavorite
- Poll, VoteOption, Vote, VotingPower
- Point, PointTransaction, Membership, Reward
- Notification, Media, Like, SavedPost, SearchHistory
- CrawledChart, CrawledChartHistory, CrawledConcert, CrawledAd
- StreamingEvent, ChatMessage, LiveHeart
- TicketReservation, SupportTicket, FAQ, Notice
- Post
"""
import pytest


# =============================================
# 제거 대상 모델 - ImportError 검증
# =============================================

class TestRemovedModelsNotImportable:
    """제거된 모델들은 api.models에서 import할 수 없어야 함"""

    def test_user_not_importable(self):
        """User 모델이 삭제되었는지 확인"""
        with pytest.raises((ImportError, AttributeError)):
            from api.models import User  # noqa: F401

    def test_artist_not_importable(self):
        """Artist 모델이 삭제되었는지 확인"""
        with pytest.raises((ImportError, AttributeError)):
            from api.models import Artist  # noqa: F401

    def test_poll_not_importable(self):
        """Poll 모델이 삭제되었는지 확인"""
        with pytest.raises((ImportError, AttributeError)):
            from api.models import Poll  # noqa: F401

    def test_post_not_importable(self):
        """Post 모델이 삭제되었는지 확인"""
        with pytest.raises((ImportError, AttributeError)):
            from api.models import Post  # noqa: F401

    def test_auth_token_not_importable(self):
        """AuthToken 모델이 삭제되었는지 확인"""
        with pytest.raises((ImportError, AttributeError)):
            from api.models import AuthToken  # noqa: F401

    def test_oauth_account_not_importable(self):
        """OAuthAccount 모델이 삭제되었는지 확인"""
        with pytest.raises((ImportError, AttributeError)):
            from api.models import OAuthAccount  # noqa: F401

    def test_user_settings_not_importable(self):
        """UserSettings 모델이 삭제되었는지 확인"""
        with pytest.raises((ImportError, AttributeError)):
            from api.models import UserSettings  # noqa: F401

    def test_user_daily_mission_not_importable(self):
        """UserDailyMission 모델이 삭제되었는지 확인"""
        with pytest.raises((ImportError, AttributeError)):
            from api.models import UserDailyMission  # noqa: F401

    def test_user_favorite_not_importable(self):
        """UserFavorite 모델이 삭제되었는지 확인"""
        with pytest.raises((ImportError, AttributeError)):
            from api.models import UserFavorite  # noqa: F401

    def test_vote_option_not_importable(self):
        """VoteOption 모델이 삭제되었는지 확인"""
        with pytest.raises((ImportError, AttributeError)):
            from api.models import VoteOption  # noqa: F401

    def test_vote_not_importable(self):
        """Vote 모델이 삭제되었는지 확인"""
        with pytest.raises((ImportError, AttributeError)):
            from api.models import Vote  # noqa: F401

    def test_voting_power_not_importable(self):
        """VotingPower 모델이 삭제되었는지 확인"""
        with pytest.raises((ImportError, AttributeError)):
            from api.models import VotingPower  # noqa: F401

    def test_point_not_importable(self):
        """Point 모델이 삭제되었는지 확인"""
        with pytest.raises((ImportError, AttributeError)):
            from api.models import Point  # noqa: F401

    def test_point_transaction_not_importable(self):
        """PointTransaction 모델이 삭제되었는지 확인"""
        with pytest.raises((ImportError, AttributeError)):
            from api.models import PointTransaction  # noqa: F401

    def test_membership_not_importable(self):
        """Membership 모델이 삭제되었는지 확인"""
        with pytest.raises((ImportError, AttributeError)):
            from api.models import Membership  # noqa: F401

    def test_reward_not_importable(self):
        """Reward 모델이 삭제되었는지 확인"""
        with pytest.raises((ImportError, AttributeError)):
            from api.models import Reward  # noqa: F401

    def test_notification_not_importable(self):
        """Notification 모델이 삭제되었는지 확인"""
        with pytest.raises((ImportError, AttributeError)):
            from api.models import Notification  # noqa: F401

    def test_media_not_importable(self):
        """Media 모델이 삭제되었는지 확인"""
        with pytest.raises((ImportError, AttributeError)):
            from api.models import Media  # noqa: F401

    def test_like_not_importable(self):
        """Like 모델이 삭제되었는지 확인"""
        with pytest.raises((ImportError, AttributeError)):
            from api.models import Like  # noqa: F401

    def test_saved_post_not_importable(self):
        """SavedPost 모델이 삭제되었는지 확인"""
        with pytest.raises((ImportError, AttributeError)):
            from api.models import SavedPost  # noqa: F401

    def test_search_history_not_importable(self):
        """SearchHistory 모델이 삭제되었는지 확인"""
        with pytest.raises((ImportError, AttributeError)):
            from api.models import SearchHistory  # noqa: F401

    def test_crawled_chart_not_importable(self):
        """CrawledChart 모델이 삭제되었는지 확인"""
        with pytest.raises((ImportError, AttributeError)):
            from api.models import CrawledChart  # noqa: F401

    def test_crawled_chart_history_not_importable(self):
        """CrawledChartHistory 모델이 삭제되었는지 확인"""
        with pytest.raises((ImportError, AttributeError)):
            from api.models import CrawledChartHistory  # noqa: F401

    def test_crawled_concert_not_importable(self):
        """CrawledConcert 모델이 삭제되었는지 확인"""
        with pytest.raises((ImportError, AttributeError)):
            from api.models import CrawledConcert  # noqa: F401

    def test_crawled_ad_not_importable(self):
        """CrawledAd 모델이 삭제되었는지 확인"""
        with pytest.raises((ImportError, AttributeError)):
            from api.models import CrawledAd  # noqa: F401

    def test_streaming_event_not_importable(self):
        """StreamingEvent 모델이 삭제되었는지 확인"""
        with pytest.raises((ImportError, AttributeError)):
            from api.models import StreamingEvent  # noqa: F401

    def test_chat_message_not_importable(self):
        """ChatMessage 모델이 삭제되었는지 확인"""
        with pytest.raises((ImportError, AttributeError)):
            from api.models import ChatMessage  # noqa: F401

    def test_live_heart_not_importable(self):
        """LiveHeart 모델이 삭제되었는지 확인"""
        with pytest.raises((ImportError, AttributeError)):
            from api.models import LiveHeart  # noqa: F401

    def test_ticket_reservation_not_importable(self):
        """TicketReservation 모델이 삭제되었는지 확인"""
        with pytest.raises((ImportError, AttributeError)):
            from api.models import TicketReservation  # noqa: F401

    def test_support_ticket_not_importable(self):
        """SupportTicket 모델이 삭제되었는지 확인"""
        with pytest.raises((ImportError, AttributeError)):
            from api.models import SupportTicket  # noqa: F401

    def test_faq_not_importable(self):
        """FAQ 모델이 삭제되었는지 확인"""
        with pytest.raises((ImportError, AttributeError)):
            from api.models import FAQ  # noqa: F401

    def test_notice_not_importable(self):
        """Notice 모델이 삭제되었는지 확인"""
        with pytest.raises((ImportError, AttributeError)):
            from api.models import Notice  # noqa: F401


# =============================================
# 유지 대상 모델 - 여전히 import 가능한지 확인
# =============================================

class TestKeptModelsImportable:
    """유지 모델 4개는 여전히 import 가능해야 함"""

    def test_crawled_news_importable(self):
        """CrawledNews 모델은 여전히 import 가능해야 함"""
        from api.models import CrawledNews  # noqa: F401
        assert CrawledNews is not None

    def test_comment_importable(self):
        """Comment 모델은 여전히 import 가능해야 함"""
        from api.models import Comment  # noqa: F401
        assert Comment is not None

    def test_comment_filter_rule_importable(self):
        """CommentFilterRule 모델은 여전히 import 가능해야 함"""
        from api.models import CommentFilterRule  # noqa: F401
        assert CommentFilterRule is not None

    def test_filtered_comment_log_importable(self):
        """FilteredCommentLog 모델은 여전히 import 가능해야 함"""
        from api.models import FilteredCommentLog  # noqa: F401
        assert FilteredCommentLog is not None

    def test_base_model_importable(self):
        """BaseModel은 여전히 import 가능해야 함"""
        from api.models import BaseModel  # noqa: F401
        assert BaseModel is not None


# =============================================
# 모델 개수 검증
# =============================================

class TestModelCount:
    """api.models에 정확히 4개의 concrete 모델만 존재하는지 확인"""

    def test_only_four_concrete_models_exist(self):
        """concrete 모델(abstract 제외)이 정확히 4개인지 확인"""
        import api.models as models_module
        from django.db.models import Model

        # api.models에서 정의된 concrete 모델 목록 추출
        concrete_models = []
        for name in dir(models_module):
            obj = getattr(models_module, name)
            try:
                if (
                    isinstance(obj, type)
                    and issubclass(obj, Model)
                    and not getattr(obj._meta, 'abstract', False)
                    and obj.__module__ == 'api.models'
                ):
                    concrete_models.append(name)
            except AttributeError:
                pass

        expected = {'CrawledNews', 'Comment', 'CommentFilterRule', 'FilteredCommentLog'}
        actual = set(concrete_models)

        assert actual == expected, (
            f"예상 모델: {sorted(expected)}, "
            f"실제 모델: {sorted(actual)}"
        )
