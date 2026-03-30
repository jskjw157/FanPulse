"""
Phase 4 - Test 4.1: Migration 검증 테스트

빈 DB에서 migrate 실행 시 에러 없음 확인
최종 테이블에 4개 AI 모델 테이블 존재 확인
Django TestCase를 사용하여 in-memory SQLite에서 검증
"""
from django.test import TestCase
from django.db import connection


class MigrationTableTest(TestCase):
    """마이그레이션 후 테이블 존재 여부 검증"""

    def test_all_ai_model_tables_exist(self):
        """4개 AI 모델 테이블이 모두 생성되었는지 확인"""
        with connection.cursor() as cursor:
            cursor.execute(
                "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name;"
            )
            tables = {row[0] for row in cursor.fetchall()}

        expected_tables = {
            'crawled_news',
            'comments',
            'comment_filter_rules',
            'filtered_comment_logs',
        }

        for table in expected_tables:
            self.assertIn(
                table,
                tables,
                msg=f"Expected table '{table}' not found in DB. Found tables: {tables}"
            )

    def test_crawled_news_table_exists(self):
        """crawled_news 테이블 존재 확인"""
        with connection.cursor() as cursor:
            cursor.execute(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='crawled_news';"
            )
            result = cursor.fetchone()
        self.assertIsNotNone(result, "crawled_news 테이블이 존재해야 합니다")

    def test_comments_table_exists(self):
        """comments 테이블 존재 확인"""
        with connection.cursor() as cursor:
            cursor.execute(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='comments';"
            )
            result = cursor.fetchone()
        self.assertIsNotNone(result, "comments 테이블이 존재해야 합니다")

    def test_comment_filter_rules_table_exists(self):
        """comment_filter_rules 테이블 존재 확인"""
        with connection.cursor() as cursor:
            cursor.execute(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='comment_filter_rules';"
            )
            result = cursor.fetchone()
        self.assertIsNotNone(result, "comment_filter_rules 테이블이 존재해야 합니다")

    def test_filtered_comment_logs_table_exists(self):
        """filtered_comment_logs 테이블 존재 확인"""
        with connection.cursor() as cursor:
            cursor.execute(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='filtered_comment_logs';"
            )
            result = cursor.fetchone()
        self.assertIsNotNone(result, "filtered_comment_logs 테이블이 존재해야 합니다")

    def test_no_removed_model_tables_exist(self):
        """제거된 모델 테이블이 존재하지 않는지 확인"""
        with connection.cursor() as cursor:
            cursor.execute(
                "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name;"
            )
            tables = {row[0] for row in cursor.fetchall()}

        removed_tables = {
            'api_user',
            'api_artist',
            'api_poll',
            'api_post',
            'api_live',
            'api_venue',
        }

        for table in removed_tables:
            self.assertNotIn(
                table,
                tables,
                msg=f"Removed table '{table}' should not exist in DB"
            )

    def test_table_count_matches_ai_models(self):
        """DB에 생성된 앱 관련 테이블 수가 AI 모델 4개와 일치하는지 확인"""
        with connection.cursor() as cursor:
            cursor.execute(
                "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name;"
            )
            all_tables = {row[0] for row in cursor.fetchall()}

        # 앱 테이블만 필터링 (django_ 내부 테이블 제외)
        django_internal_prefixes = (
            'django_',
            'auth_',
            'sqlite_',
        )
        app_tables = {
            t for t in all_tables
            if not any(t.startswith(prefix) for prefix in django_internal_prefixes)
        }

        expected_ai_tables = {
            'crawled_news',
            'comments',
            'comment_filter_rules',
            'filtered_comment_logs',
        }

        self.assertEqual(
            app_tables,
            expected_ai_tables,
            msg=f"AI 모델 테이블이 정확히 4개여야 합니다. 실제: {app_tables}"
        )
