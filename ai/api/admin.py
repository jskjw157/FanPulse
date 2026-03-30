"""
Admin 설정 - AI Sidecar 전용 4개 모델만 등록

유지 모델:
- CrawledNews: 크롤링 뉴스
- Comment: 댓글 (AI 필터링 대상)
- CommentFilterRule: 댓글 필터링 규칙
- FilteredCommentLog: 필터링 이력
"""
from django.contrib import admin
from .models import CrawledNews, Comment, CommentFilterRule, FilteredCommentLog


@admin.register(CrawledNews)
class CrawledNewsAdmin(admin.ModelAdmin):
    list_display = ('id', 'title', 'source', 'published_at', 'created_at')
    search_fields = ('title', 'source')
    list_filter = ('source',)
    ordering = ('-created_at',)


@admin.register(Comment)
class CommentAdmin(admin.ModelAdmin):
    list_display = ('id', 'post_id', 'user_id', 'is_filtered', 'filter_reason', 'created_at')
    search_fields = ('content', 'filter_reason')
    list_filter = ('is_filtered', 'is_deleted')
    ordering = ('-created_at',)


@admin.register(CommentFilterRule)
class CommentFilterRuleAdmin(admin.ModelAdmin):
    list_display = ('id', 'name', 'filter_type', 'action', 'is_active', 'priority')
    search_fields = ('name', 'pattern')
    list_filter = ('filter_type', 'action', 'is_active')
    ordering = ('-priority', 'created_at')


@admin.register(FilteredCommentLog)
class FilteredCommentLogAdmin(admin.ModelAdmin):
    list_display = ('id', 'comment', 'filter_rule', 'action_taken', 'created_at')
    search_fields = ('matched_pattern', 'action_taken')
    list_filter = ('action_taken',)
    ordering = ('-created_at',)
