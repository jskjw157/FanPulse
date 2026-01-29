"""
댓글 자동 필터링 서비스

이 모듈은 댓글 내용을 분석하여 부적절한 내용을 자동으로 필터링합니다.

주요 기능:
- 금칙어 필터링
- 정규식 패턴 매칭
- 스팸 탐지
- URL 차단
- 반복 문자 탐지
"""
import re
from typing import Optional
from dataclasses import dataclass

from django.db.models import QuerySet


@dataclass
class FilterResult:
    """필터링 결과 데이터 클래스"""
    is_filtered: bool
    action: Optional[str] = None  # block, hide, review
    rule_id: Optional[str] = None
    rule_name: Optional[str] = None
    filter_type: Optional[str] = None
    matched_pattern: Optional[str] = None
    reason: Optional[str] = None


class CommentFilterService:
    """
    댓글 필터링 서비스 클래스

    사용 예시:
        service = CommentFilterService()
        result = service.filter_comment("댓글 내용")
        if result.is_filtered:
            print(f"필터링됨: {result.reason}")
    """

    def __init__(self):
        self._rules_cache = None
        self._cache_timestamp = None

    def get_active_rules(self) -> QuerySet:
        """
        활성화된 필터링 규칙 목록 조회
        우선순위 순으로 정렬하여 반환
        """
        from api.models import CommentFilterRule
        return CommentFilterRule.objects.filter(is_active=True).order_by('-priority')

    def filter_comment(self, content: str) -> FilterResult:
        """
        댓글 내용을 필터링 규칙에 따라 검사

        Args:
            content: 검사할 댓글 내용

        Returns:
            FilterResult: 필터링 결과
        """
        if not content or not content.strip():
            return FilterResult(is_filtered=False)

        rules = self.get_active_rules()

        for rule in rules:
            result = self._check_rule(content, rule)
            if result.is_filtered:
                return result

        return FilterResult(is_filtered=False)

    def _check_rule(self, content: str, rule) -> FilterResult:
        """
        단일 규칙에 대해 댓글 검사

        Args:
            content: 검사할 댓글 내용
            rule: CommentFilterRule 인스턴스

        Returns:
            FilterResult: 필터링 결과
        """
        filter_type = rule.filter_type
        pattern = rule.pattern

        matched = False
        matched_pattern = None

        if filter_type == 'keyword':
            matched, matched_pattern = self._check_keyword(content, pattern)
        elif filter_type == 'regex':
            matched, matched_pattern = self._check_regex(content, pattern)
        elif filter_type == 'spam':
            matched, matched_pattern = self._check_spam(content, pattern)
        elif filter_type == 'url':
            matched, matched_pattern = self._check_url(content, pattern)
        elif filter_type == 'repeat':
            matched, matched_pattern = self._check_repeat(content, pattern)

        if matched:
            return FilterResult(
                is_filtered=True,
                action=rule.action,
                rule_id=str(rule.id),
                rule_name=rule.name,
                filter_type=filter_type,
                matched_pattern=matched_pattern,
                reason=f"[{rule.name}] {filter_type} 규칙에 의해 필터링됨"
            )

        return FilterResult(is_filtered=False)

    def _check_keyword(self, content: str, pattern: str) -> tuple[bool, Optional[str]]:
        """
        금칙어 검사
        패턴은 쉼표로 구분된 키워드 목록

        Args:
            content: 검사할 내용
            pattern: 쉼표로 구분된 금칙어 목록

        Returns:
            (매칭 여부, 매칭된 키워드)
        """
        keywords = [kw.strip().lower() for kw in pattern.split(',') if kw.strip()]
        content_lower = content.lower()

        for keyword in keywords:
            if keyword in content_lower:
                return True, keyword

        return False, None

    def _check_regex(self, content: str, pattern: str) -> tuple[bool, Optional[str]]:
        """
        정규식 패턴 검사

        Args:
            content: 검사할 내용
            pattern: 정규식 패턴

        Returns:
            (매칭 여부, 매칭된 문자열)
        """
        try:
            match = re.search(pattern, content, re.IGNORECASE)
            if match:
                return True, match.group()
        except re.error:
            # 잘못된 정규식 패턴 무시
            pass

        return False, None

    def _check_spam(self, content: str, pattern: str) -> tuple[bool, Optional[str]]:
        """
        스팸 패턴 검사
        패턴은 JSON 형식으로 스팸 조건 정의

        Args:
            content: 검사할 내용
            pattern: 스팸 검사 조건 (예: 이모지 과다 사용, 특수문자 반복)

        Returns:
            (매칭 여부, 스팸 유형)
        """
        # 이모지 과다 사용 검사 (5개 이상 연속)
        emoji_pattern = r'[\U0001F600-\U0001F64F\U0001F300-\U0001F5FF\U0001F680-\U0001F6FF]{5,}'
        if re.search(emoji_pattern, content):
            return True, "이모지 과다 사용"

        # 특수문자 과다 사용 검사
        special_char_count = len(re.findall(r'[!@#$%^&*()]{3,}', content))
        if special_char_count > 3:
            return True, "특수문자 과다 사용"

        return False, None

    def _check_url(self, content: str, pattern: str) -> tuple[bool, Optional[str]]:
        """
        URL 차단 검사
        패턴은 차단할 도메인 목록 (쉼표 구분)
        빈 패턴이면 모든 URL 차단

        Args:
            content: 검사할 내용
            pattern: 차단할 도메인 목록

        Returns:
            (매칭 여부, 매칭된 URL)
        """
        # URL 패턴
        url_pattern = r'https?://[^\s<>"{}|\\^`\[\]]+'
        urls = re.findall(url_pattern, content)

        if not urls:
            return False, None

        # 패턴이 비어있으면 모든 URL 차단
        if not pattern.strip():
            return True, urls[0]

        # 특정 도메인만 차단
        blocked_domains = [d.strip().lower() for d in pattern.split(',') if d.strip()]
        for url in urls:
            for domain in blocked_domains:
                if domain in url.lower():
                    return True, url

        return False, None

    def _check_repeat(self, content: str, pattern: str) -> tuple[bool, Optional[str]]:
        """
        반복 문자 검사
        패턴은 반복 허용 횟수 (기본값: 5)

        Args:
            content: 검사할 내용
            pattern: 반복 허용 횟수

        Returns:
            (매칭 여부, 반복된 문자)
        """
        try:
            max_repeat = int(pattern) if pattern.strip() else 5
        except ValueError:
            max_repeat = 5

        # 같은 문자가 연속으로 반복되는 패턴
        repeat_pattern = rf'(.)\1{{{max_repeat},}}'
        match = re.search(repeat_pattern, content)
        if match:
            return True, match.group()

        return False, None

    def batch_filter(self, comments: list[str]) -> list[FilterResult]:
        """
        여러 댓글을 일괄 필터링

        Args:
            comments: 검사할 댓글 내용 목록

        Returns:
            FilterResult 목록
        """
        return [self.filter_comment(comment) for comment in comments]


# 싱글톤 인스턴스
_filter_service = None


def get_filter_service() -> CommentFilterService:
    """필터링 서비스 싱글톤 인스턴스 반환"""
    global _filter_service
    if _filter_service is None:
        _filter_service = CommentFilterService()
    return _filter_service
