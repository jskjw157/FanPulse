"""
FanPulse Django Models - AI Sidecar 전용

Django는 오직 AI/ML 기능(뉴스 요약, 댓글 필터링, 콘텐츠 모더레이션)만 담당합니다.
Spring 백엔드가 메인 비즈니스 로직(User, Post, Artist 등)을 담당합니다.

유지 모델 (4개):
- CrawledNews: 크롤링 뉴스 - AI 요약 입력
- Comment: 댓글 - AI 필터링/모더레이션 (FK 없음, UUID 참조)
- CommentFilterRule: 댓글 필터링 규칙
- FilteredCommentLog: 필터링 이력
"""
import uuid
from django.db import models


class BaseModel(models.Model):
    """공통 베이스 모델 (UUID PK)"""
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        abstract = True


# =============================================
# 크롤링 데이터 모델
# =============================================

class CrawledNews(BaseModel):
    """크롤링된 뉴스 테이블 - AI 요약 입력 소스"""
    title = models.CharField(max_length=255)
    content = models.TextField(null=True, blank=True)
    origin_news = models.TextField(null=True, blank=True)  # 뉴스 데이터 원본 (원문 링크에서 추출)
    thumbnail_url = models.TextField(null=True, blank=True)
    url = models.CharField(max_length=500)
    source = models.CharField(max_length=100, null=True, blank=True)
    published_at = models.DateTimeField(null=True, blank=True)

    class Meta:
        db_table = 'crawled_news'


# =============================================
# 게시글/댓글 모델 (AI 필터링/모더레이션 대상)
# =============================================

class Comment(BaseModel):
    """댓글 테이블 - AI 필터링/모더레이션 대상

    Note: Post/User는 Spring 백엔드에서 관리합니다.
    Django는 UUID만 저장하여 크로스 서비스 참조를 유지합니다.
    DB 레벨 FK 대신 애플리케이션 레벨에서 UUID로 참조합니다.
    """
    post_id = models.UUIDField()    # Spring Post UUID 참조 (FK 없음)
    user_id = models.UUIDField()    # Spring User UUID 참조 (FK 없음)
    parent = models.ForeignKey(
        'self',
        on_delete=models.CASCADE,
        null=True,
        blank=True,
        related_name='replies'
    )
    content = models.TextField()
    like_count = models.IntegerField(default=0)
    is_deleted = models.BooleanField(default=False)
    is_filtered = models.BooleanField(default=False)    # AI 자동 필터링 여부
    filter_reason = models.CharField(max_length=100, null=True, blank=True)  # 필터링 사유
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = 'comments'


# =============================================
# 댓글 필터링 모델
# =============================================

class CommentFilterRule(BaseModel):
    """댓글 필터링 규칙 테이블"""
    FILTER_TYPE_CHOICES = [
        ('keyword', '금칙어'),
        ('regex', '정규식'),
        ('spam', '스팸 패턴'),
        ('url', 'URL 차단'),
        ('repeat', '반복 문자'),
    ]

    ACTION_CHOICES = [
        ('block', '차단'),
        ('hide', '숨김'),
        ('review', '검토 대기'),
    ]

    name = models.CharField(max_length=100)  # 규칙 이름
    filter_type = models.CharField(max_length=20, choices=FILTER_TYPE_CHOICES)
    pattern = models.TextField()  # 필터링 패턴 (키워드 또는 정규식)
    action = models.CharField(max_length=20, choices=ACTION_CHOICES, default='block')
    is_active = models.BooleanField(default=True)
    priority = models.IntegerField(default=0)  # 우선순위 (높을수록 먼저 적용)
    description = models.TextField(null=True, blank=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = 'comment_filter_rules'
        ordering = ['-priority', 'created_at']


class FilteredCommentLog(BaseModel):
    """필터링된 댓글 로그 테이블"""
    comment = models.ForeignKey(
        Comment,
        on_delete=models.SET_NULL,
        null=True,
        blank=True,
        related_name='filter_logs'
    )
    filter_rule = models.ForeignKey(
        CommentFilterRule,
        on_delete=models.SET_NULL,
        null=True,
        blank=True,
        related_name='filter_logs'
    )
    original_content = models.TextField()     # 원본 내용
    matched_pattern = models.CharField(max_length=255)  # 매칭된 패턴
    action_taken = models.CharField(max_length=20)      # 취해진 조치

    class Meta:
        db_table = 'filtered_comment_logs'
