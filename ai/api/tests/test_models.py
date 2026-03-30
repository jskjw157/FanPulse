"""
Phase 1 + Phase 2 - 유지 대상 4개 모델 Unit 테스트

유지 모델:
- CrawledNews: 크롤링 뉴스 (AI 요약 입력)
- Comment: 댓글 (AI 필터링/모더레이션 대상) - Phase 2: FK → UUID 전환
- CommentFilterRule: 댓글 필터링 규칙
- FilteredCommentLog: 필터링 이력

Phase 2 변경사항:
- Comment.post = ForeignKey(Post) → Comment.post_id = UUIDField()
- Comment.user = ForeignKey(User) → Comment.user_id = UUIDField()
"""
import uuid

import pytest
from django.utils import timezone

pytestmark = pytest.mark.django_db


# =============================================
# CrawledNews 모델 Unit Tests
# =============================================

class TestCrawledNewsModel:
    """CrawledNews 모델 - 필드 생성 및 검증"""

    def test_create_crawled_news_minimal(self):
        """필수 필드만으로 CrawledNews 생성"""
        from api.models import CrawledNews

        news = CrawledNews.objects.create(
            title="테스트 뉴스",
            url="https://example.com/news/1",
        )

        assert news.id is not None
        assert isinstance(news.id, uuid.UUID)
        assert news.title == "테스트 뉴스"
        assert news.url == "https://example.com/news/1"

    def test_create_crawled_news_full(self):
        """모든 필드로 CrawledNews 생성"""
        from api.models import CrawledNews

        published = timezone.now()
        news = CrawledNews.objects.create(
            title="전체 필드 테스트 뉴스",
            content="뉴스 본문 내용입니다.",
            origin_news="원본 뉴스 링크 추출 텍스트",
            thumbnail_url="https://example.com/thumb.jpg",
            url="https://example.com/news/2",
            source="테스트 언론사",
            published_at=published,
        )

        assert news.title == "전체 필드 테스트 뉴스"
        assert news.content == "뉴스 본문 내용입니다."
        assert news.source == "테스트 언론사"
        assert news.published_at is not None

    def test_crawled_news_db_table(self):
        """CrawledNews의 DB 테이블명 확인"""
        from api.models import CrawledNews

        assert CrawledNews._meta.db_table == "crawled_news"

    def test_crawled_news_has_uuid_pk(self):
        """CrawledNews의 PK가 UUID인지 확인"""
        from api.models import CrawledNews

        news = CrawledNews.objects.create(
            title="UUID PK 테스트",
            url="https://example.com/news/3",
        )

        # PK는 UUID여야 함
        assert isinstance(news.pk, uuid.UUID)

    def test_crawled_news_nullable_fields(self):
        """CrawledNews의 nullable 필드 None 허용 확인"""
        from api.models import CrawledNews

        news = CrawledNews.objects.create(
            title="Nullable 테스트",
            url="https://example.com/news/4",
            content=None,
            source=None,
            published_at=None,
        )

        assert news.content is None
        assert news.source is None
        assert news.published_at is None

    def test_crawled_news_created_at_auto(self):
        """CrawledNews.created_at이 자동 설정되는지 확인"""
        from api.models import CrawledNews

        news = CrawledNews.objects.create(
            title="created_at 테스트",
            url="https://example.com/news/5",
        )

        assert news.created_at is not None


# =============================================
# Comment 모델 Unit Tests (Phase 2: UUID 참조)
# =============================================

class TestCommentModel:
    """Comment 모델 - Phase 2: FK 없이 UUID 직접 참조 테스트"""

    def _create_comment(self, **kwargs):
        """UUID 직접 할당으로 Comment 생성 (FK 없음)"""
        from api.models import Comment

        defaults = {
            'post_id': uuid.uuid4(),
            'user_id': uuid.uuid4(),
            'content': '테스트 댓글 내용입니다.',
        }
        defaults.update(kwargs)
        return Comment.objects.create(**defaults)

    def test_create_comment_basic(self):
        """기본 Comment 생성 테스트 (UUID 참조)"""
        from api.models import Comment

        post_uuid = uuid.uuid4()
        user_uuid = uuid.uuid4()

        comment = Comment.objects.create(
            post_id=post_uuid,
            user_id=user_uuid,
            content="테스트 댓글 내용입니다.",
        )

        assert comment.id is not None
        assert isinstance(comment.id, uuid.UUID)
        assert comment.content == "테스트 댓글 내용입니다."
        assert comment.post_id == post_uuid
        assert comment.user_id == user_uuid

    def test_comment_default_values(self):
        """Comment 기본값 확인"""
        comment = self._create_comment(content="기본값 테스트 댓글")

        assert comment.is_deleted is False
        assert comment.is_filtered is False
        assert comment.filter_reason is None
        assert comment.like_count == 0

    def test_comment_filter_fields(self):
        """Comment의 AI 필터링 관련 필드 확인"""
        comment = self._create_comment(
            content="필터링된 댓글입니다.",
            is_filtered=True,
            filter_reason="욕설 포함",
        )

        assert comment.is_filtered is True
        assert comment.filter_reason == "욕설 포함"

    def test_comment_db_table(self):
        """Comment의 DB 테이블명 확인"""
        from api.models import Comment

        assert Comment._meta.db_table == "comments"

    def test_comment_reply(self):
        """댓글에 대한 대댓글(reply) 생성 확인 (UUID 참조)"""
        post_uuid = uuid.uuid4()
        user_uuid = uuid.uuid4()

        parent_comment = self._create_comment(
            post_id=post_uuid,
            user_id=user_uuid,
            content="부모 댓글",
        )
        reply = self._create_comment(
            post_id=post_uuid,
            user_id=user_uuid,
            content="대댓글",
        )
        # parent 설정
        from api.models import Comment
        reply.parent = parent_comment
        reply.save()

        assert reply.parent == parent_comment


# =============================================
# CommentFilterRule 모델 Unit Tests
# =============================================

class TestCommentFilterRuleModel:
    """CommentFilterRule 모델 - 필드 생성 및 검증"""

    def test_create_keyword_rule(self):
        """keyword 타입 필터링 규칙 생성"""
        from api.models import CommentFilterRule

        rule = CommentFilterRule.objects.create(
            name="욕설 필터",
            filter_type="keyword",
            pattern="욕설1,욕설2,비속어",
            action="block",
        )

        assert rule.id is not None
        assert rule.name == "욕설 필터"
        assert rule.filter_type == "keyword"
        assert rule.pattern == "욕설1,욕설2,비속어"

    def test_is_active_default_true(self):
        """CommentFilterRule.is_active 기본값이 True인지 확인"""
        from api.models import CommentFilterRule

        rule = CommentFilterRule.objects.create(
            name="기본값 테스트 규칙",
            filter_type="keyword",
            pattern="테스트",
        )

        assert rule.is_active is True

    def test_priority_default_zero(self):
        """CommentFilterRule.priority 기본값이 0인지 확인"""
        from api.models import CommentFilterRule

        rule = CommentFilterRule.objects.create(
            name="우선순위 테스트",
            filter_type="keyword",
            pattern="테스트",
        )

        assert rule.priority == 0

    def test_action_default_block(self):
        """CommentFilterRule.action 기본값이 'block'인지 확인"""
        from api.models import CommentFilterRule

        rule = CommentFilterRule.objects.create(
            name="액션 기본값 테스트",
            filter_type="keyword",
            pattern="테스트",
        )

        assert rule.action == "block"

    def test_ordering_by_priority(self):
        """CommentFilterRule이 priority 내림차순으로 정렬되는지 확인"""
        from api.models import CommentFilterRule

        rule_low = CommentFilterRule.objects.create(
            name="저우선순위 규칙",
            filter_type="keyword",
            pattern="low",
            priority=1,
        )
        rule_high = CommentFilterRule.objects.create(
            name="고우선순위 규칙",
            filter_type="keyword",
            pattern="high",
            priority=10,
        )

        rules = list(CommentFilterRule.objects.all())
        # 기본 ordering: -priority, created_at -> 고우선순위가 먼저
        assert rules[0].priority >= rules[-1].priority

    def test_all_filter_types(self):
        """모든 filter_type 값으로 규칙 생성 가능한지 확인"""
        from api.models import CommentFilterRule

        filter_types = ["keyword", "regex", "spam", "url", "repeat"]
        for ft in filter_types:
            rule = CommentFilterRule.objects.create(
                name=f"{ft} 규칙",
                filter_type=ft,
                pattern="패턴",
            )
            assert rule.filter_type == ft

    def test_all_action_types(self):
        """모든 action 값으로 규칙 생성 가능한지 확인"""
        from api.models import CommentFilterRule

        actions = ["block", "hide", "review"]
        for action in actions:
            rule = CommentFilterRule.objects.create(
                name=f"{action} 규칙",
                filter_type="keyword",
                pattern="패턴",
                action=action,
            )
            assert rule.action == action

    def test_comment_filter_rule_db_table(self):
        """CommentFilterRule의 DB 테이블명 확인"""
        from api.models import CommentFilterRule

        assert CommentFilterRule._meta.db_table == "comment_filter_rules"


# =============================================
# FilteredCommentLog 모델 Unit Tests
# =============================================

class TestFilteredCommentLogModel:
    """FilteredCommentLog 모델 - FK 관계 및 필드 검증"""

    def _create_comment_and_rule(self):
        """FilteredCommentLog 생성에 필요한 Comment, CommentFilterRule 생성"""
        from api.models import Comment, CommentFilterRule

        comment = Comment.objects.create(
            post_id=uuid.uuid4(),
            user_id=uuid.uuid4(),
            content="필터링될 댓글",
        )
        rule = CommentFilterRule.objects.create(
            name="테스트 필터 규칙",
            filter_type="keyword",
            pattern="금칙어",
        )
        return comment, rule

    def test_create_filtered_comment_log(self):
        """FilteredCommentLog 기본 생성 테스트"""
        from api.models import FilteredCommentLog

        comment, rule = self._create_comment_and_rule()

        log = FilteredCommentLog.objects.create(
            comment=comment,
            filter_rule=rule,
            original_content="필터링된 원본 댓글 내용",
            matched_pattern="금칙어",
            action_taken="block",
        )

        assert log.id is not None
        assert isinstance(log.id, uuid.UUID)
        assert log.original_content == "필터링된 원본 댓글 내용"
        assert log.matched_pattern == "금칙어"
        assert log.action_taken == "block"

    def test_filtered_comment_log_nullable_fk(self):
        """FilteredCommentLog의 comment, filter_rule FK가 nullable인지 확인"""
        from api.models import FilteredCommentLog

        # FK 없이 생성 가능 (SET_NULL 설정)
        log = FilteredCommentLog.objects.create(
            comment=None,
            filter_rule=None,
            original_content="FK 없는 로그",
            matched_pattern="패턴",
            action_taken="block",
        )

        assert log.comment is None
        assert log.filter_rule is None

    def test_filtered_comment_log_fk_to_comment(self):
        """FilteredCommentLog가 Comment FK를 가지는지 확인"""
        from api.models import FilteredCommentLog, Comment

        comment_field = FilteredCommentLog._meta.get_field("comment")
        assert comment_field.is_relation is True
        assert comment_field.related_model == Comment

    def test_filtered_comment_log_fk_to_rule(self):
        """FilteredCommentLog가 CommentFilterRule FK를 가지는지 확인"""
        from api.models import FilteredCommentLog, CommentFilterRule

        rule_field = FilteredCommentLog._meta.get_field("filter_rule")
        assert rule_field.is_relation is True
        assert rule_field.related_model == CommentFilterRule

    def test_filtered_comment_log_db_table(self):
        """FilteredCommentLog의 DB 테이블명 확인"""
        from api.models import FilteredCommentLog

        assert FilteredCommentLog._meta.db_table == "filtered_comment_logs"

    def test_filtered_comment_log_created_at_auto(self):
        """FilteredCommentLog.created_at이 자동 설정되는지 확인"""
        from api.models import FilteredCommentLog

        log = FilteredCommentLog.objects.create(
            comment=None,
            filter_rule=None,
            original_content="타임스탬프 테스트",
            matched_pattern="패턴",
            action_taken="block",
        )

        assert log.created_at is not None

    def test_set_null_on_comment_delete(self):
        """Comment 삭제 시 FilteredCommentLog.comment가 NULL로 설정되는지 확인"""
        from api.models import FilteredCommentLog

        comment, rule = self._create_comment_and_rule()

        log = FilteredCommentLog.objects.create(
            comment=comment,
            filter_rule=rule,
            original_content="삭제 테스트 댓글",
            matched_pattern="금칙어",
            action_taken="block",
        )

        comment.delete()

        log.refresh_from_db()
        assert log.comment is None


# =============================================
# Phase 2 - RED: Comment UUID 참조 테스트
# =============================================

class TestCommentUUIDReference:
    """Phase 2: Comment 모델이 FK 없이 UUID 참조를 사용하는지 확인"""

    def test_comment_post_id_is_uuid_field(self):
        """Comment.post_id가 UUIDField인지 확인 (FK가 아니어야 함)"""
        from django.db.models import UUIDField
        from api.models import Comment

        post_id_field = Comment._meta.get_field("post_id")
        assert isinstance(post_id_field, UUIDField), (
            f"Comment.post_id는 UUIDField여야 합니다. 현재: {type(post_id_field).__name__}"
        )

    def test_comment_user_id_is_uuid_field(self):
        """Comment.user_id가 UUIDField인지 확인 (FK가 아니어야 함)"""
        from django.db.models import UUIDField
        from api.models import Comment

        user_id_field = Comment._meta.get_field("user_id")
        assert isinstance(user_id_field, UUIDField), (
            f"Comment.user_id는 UUIDField여야 합니다. 현재: {type(user_id_field).__name__}"
        )

    def test_comment_no_fk_to_post(self):
        """Comment에 Post FK가 없어야 함 (post_id는 UUIDField)"""
        from api.models import Comment

        post_id_field = Comment._meta.get_field("post_id")
        # FK라면 is_relation=True, UUIDField라면 is_relation=False
        assert post_id_field.is_relation is False, (
            "Comment.post_id는 FK 관계가 아닌 일반 UUIDField여야 합니다."
        )

    def test_comment_no_fk_to_user(self):
        """Comment에 User FK가 없어야 함 (user_id는 UUIDField)"""
        from api.models import Comment

        user_id_field = Comment._meta.get_field("user_id")
        assert user_id_field.is_relation is False, (
            "Comment.user_id는 FK 관계가 아닌 일반 UUIDField여야 합니다."
        )

    def test_comment_create_with_uuid_directly(self):
        """Comment를 UUID 직접 할당으로 생성 가능한지 확인 (FK 없이)"""
        from api.models import Comment
        import uuid

        post_uuid = uuid.uuid4()
        user_uuid = uuid.uuid4()

        comment = Comment.objects.create(
            post_id=post_uuid,
            user_id=user_uuid,
            content="UUID 직접 할당 댓글",
        )

        assert comment.id is not None
        assert comment.post_id == post_uuid
        assert comment.user_id == user_uuid

    @pytest.mark.django_db
    def test_comment_uuid_fields_preserve_value(self):
        """Comment의 post_id, user_id UUID 값이 DB에 올바르게 저장/조회되는지 확인"""
        from api.models import Comment
        import uuid

        post_uuid = uuid.uuid4()
        user_uuid = uuid.uuid4()

        comment = Comment.objects.create(
            post_id=post_uuid,
            user_id=user_uuid,
            content="UUID 보존 테스트",
        )

        fetched = Comment.objects.get(id=comment.id)
        assert fetched.post_id == post_uuid
        assert fetched.user_id == user_uuid
