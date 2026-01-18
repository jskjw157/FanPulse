"""
#######################
# Serializers 모듈
#######################
# 이 파일은 API 요청/응답 데이터의 유효성 검사와 직렬화를 담당합니다.
#
# 주요 클래스:
# - SummarizeRequestSerializer: 요약 요청 데이터 검증
# - SummarizeResponseSerializer: 요약 응답 데이터 형식 정의
#
# DRF(Django REST Framework) Serializer를 사용하여:
# - 입력 데이터 유효성 검사
# - 데이터 타입 변환
# - Swagger 문서 자동 생성
#######################
"""
from rest_framework import serializers
from django.core.validators import URLValidator
from django.core.exceptions import ValidationError as DjangoValidationError


#######################
# 요청 Serializer
#######################
class SummarizeRequestSerializer(serializers.Serializer):
    """
    요약 API 요청 데이터 검증 Serializer

    클라이언트가 보내는 요청 데이터를 검증하고 정규화합니다.

    필수 필드:
    - input_type: 입력 방식 ('url' 또는 'text')

    선택 필드 (조건부 필수):
    - url: input_type이 'url'일 때 필수
    - text: input_type이 'text'일 때 필수

    선택 필드 (기본값 있음):
    - summarize_method: 요약 방식 (기본값: 'rule')
    - language: 언어 (기본값: 'ko')
    - max_length: 최대 요약 길이 (기본값: 200)
    - min_length: 최소 요약 길이 (기본값: 50)
    """

    #######################
    # 선택 가능한 값 정의
    #######################
    INPUT_TYPE_CHOICES = ['url', 'text']           # 입력 타입 옵션
    LANGUAGE_CHOICES = ['ko', 'en']                # 지원 언어
    SUMMARIZE_METHOD_CHOICES = ['rule', 'ai']      # 요약 방식

    #######################
    # 필드 정의
    #######################

    # 입력 타입 (필수)
    input_type = serializers.ChoiceField(
        choices=INPUT_TYPE_CHOICES,
        required=True,
        help_text="입력 타입: 'url' (URL 입력) 또는 'text' (텍스트 직접 입력)"
    )

    # 요약 방식 (선택, 기본값: rule)
    summarize_method = serializers.ChoiceField(
        choices=SUMMARIZE_METHOD_CHOICES,
        required=False,
        default='rule',
        help_text="요약 방식: 'rule' (알고리즘) 또는 'ai' (AI 모델)"
    )

    # URL (input_type='url'일 때 필수)
    url = serializers.URLField(
        required=False,
        allow_blank=True,
        help_text="뉴스 기사 URL (input_type='url'일 때 필수)"
    )

    # 텍스트 (input_type='text'일 때 필수)
    text = serializers.CharField(
        required=False,
        allow_blank=True,
        help_text="요약할 텍스트 내용 (input_type='text'일 때 필수, 최소 10자)"
    )

    # 요약 언어 (선택, 기본값: ko)
    language = serializers.ChoiceField(
        choices=LANGUAGE_CHOICES,
        required=False,
        default='ko',
        help_text="요약 언어: 'ko' (한국어) 또는 'en' (영어)"
    )

    # 요약 최대 길이 (선택, 기본값: 200)
    max_length = serializers.IntegerField(
        required=False,
        default=200,
        min_value=50,      # 최소 50자
        max_value=1000,    # 최대 1000자
        help_text="요약 최대 길이 (50-1000)"
    )

    # 요약 최소 길이 (선택, 기본값: 50)
    min_length = serializers.IntegerField(
        required=False,
        default=50,
        min_value=10,      # 최소 10자
        max_value=500,     # 최대 500자
        help_text="요약 최소 길이 (10-500)"
    )

    #######################
    # Swagger 문서용 예시
    #######################
    class Meta:
        swagger_schema_fields = {
            "example": {
                "input_type": "text",
                "summarize_method": "rule",
                "text": "인공지능 기술의 발전으로 우리 생활에 많은 변화가 일어나고 있습니다. 특히 자연어 처리 기술은 번역, 요약, 대화 등 다양한 분야에서 활용되고 있으며, 최근에는 GPT와 같은 대규모 언어 모델이 주목받고 있습니다. 이러한 기술들은 업무 효율성을 높이고 새로운 서비스를 창출하는 데 기여하고 있습니다.",
                "language": "ko",
                "max_length": 200,
                "min_length": 50
            }
        }

    #######################
    # 교차 필드 유효성 검사
    #######################
    def validate(self, data):
        """
        여러 필드를 함께 검증하는 커스텀 유효성 검사

        검증 규칙:
        1. input_type='url'이면 url 필드 필수
        2. input_type='text'이면 text 필드 필수 (최소 10자)
        3. min_length <= max_length 확인
        """
        input_type = data.get('input_type')
        url = data.get('url', '').strip()
        text = data.get('text', '').strip()
        min_length = data.get('min_length')
        max_length = data.get('max_length')

        #######################
        # input_type별 필수 필드 검증
        #######################
        if input_type == 'url':
            # URL 입력 모드: url 필드 필수
            if not url:
                raise serializers.ValidationError({
                    'url': 'URL is required when input_type is "url"'
                })
            # URL 형식 추가 검증
            validator = URLValidator()
            try:
                validator(url)
            except DjangoValidationError:
                raise serializers.ValidationError({
                    'url': 'Invalid URL format'
                })

        elif input_type == 'text':
            # 텍스트 입력 모드: text 필드 필수
            if not text:
                raise serializers.ValidationError({
                    'text': 'Text is required when input_type is "text"'
                })
            # 최소 길이 검증
            if len(text) < 10:
                raise serializers.ValidationError({
                    'text': 'Text must be at least 10 characters long'
                })

        #######################
        # 길이 제약 조건 검증
        #######################
        if min_length > max_length:
            raise serializers.ValidationError({
                'min_length': 'min_length must be less than or equal to max_length',
                'max_length': 'max_length must be greater than or equal to min_length'
            })

        return data


#######################
# 응답 Serializer
#######################
class SummarizeResponseSerializer(serializers.Serializer):
    """
    요약 API 응답 데이터 형식 정의 Serializer

    서버가 클라이언트에게 반환하는 응답 데이터의 형식을 정의합니다.
    응답 데이터의 유효성을 검사하고 일관된 형식을 보장합니다.

    응답 필드:
    - request_id: 요청 추적용 고유 ID
    - input_type: 사용된 입력 타입
    - summarize_method: 사용된 요약 방식
    - title: 기사 제목 (URL 입력 시에만)
    - source: 출처 도메인 (URL 입력 시에만)
    - published_at: 발행일 (URL 입력 시에만)
    - original_text: 원문 텍스트
    - summary: 생성된 요약
    - bullets: 핵심 포인트 목록
    - keywords: 추출된 키워드 목록
    - elapsed_ms: 처리 시간 (밀리초)
    """

    #######################
    # 필드 정의
    #######################

    # 요청 추적 ID (UUID 형식)
    request_id = serializers.UUIDField(help_text="고유 요청 ID")

    # 사용된 입력 타입
    input_type = serializers.CharField(help_text="입력 타입 (url/text)")

    # 사용된 요약 방식
    summarize_method = serializers.CharField(help_text="사용된 요약 방식 (rule/ai)")

    # 기사 제목 (URL 입력 시에만 값 있음, 없으면 null)
    title = serializers.CharField(allow_null=True, help_text="기사 제목 (URL 입력 시)")

    # 출처 도메인 (URL 입력 시에만 값 있음, 없으면 null)
    source = serializers.CharField(allow_null=True, help_text="출처 도메인 (URL 입력 시)")

    # 발행일 (URL 입력 시에만 값 있음, 없으면 null)
    published_at = serializers.DateTimeField(
        allow_null=True,
        help_text="발행일 (URL 입력 시)"
    )

    # 원문 텍스트
    original_text = serializers.CharField(help_text="원문 텍스트")

    # 생성된 요약 텍스트
    summary = serializers.CharField(help_text="생성된 요약")

    # 핵심 포인트 목록 (문자열 배열)
    bullets = serializers.ListField(
        child=serializers.CharField(),
        help_text="주요 포인트 목록"
    )

    # 추출된 키워드 목록 (문자열 배열)
    keywords = serializers.ListField(
        child=serializers.CharField(),
        help_text="추출된 키워드 목록"
    )

    # 처리 시간 (밀리초)
    elapsed_ms = serializers.IntegerField(help_text="처리 시간 (밀리초)")

    #######################
    # Swagger 문서용 예시
    #######################
    class Meta:
        swagger_schema_fields = {
            "example": {
                "request_id": "550e8400-e29b-41d4-a716-446655440000",
                "input_type": "text",
                "summarize_method": "rule",
                "title": None,
                "source": None,
                "published_at": None,
                "original_text": "인공지능 기술의 발전으로 우리 생활에 많은 변화가 일어나고 있습니다. 특히 자연어 처리 기술은 번역, 요약, 대화 등 다양한 분야에서 활용되고 있으며, 최근에는 GPT와 같은 대규모 언어 모델이 주목받고 있습니다.",
                "summary": "인공지능 기술의 발전으로 우리 생활에 많은 변화가 일어나고 있습니다. 자연어 처리 기술은 번역, 요약, 대화 등 다양한 분야에서 활용되고 있습니다.",
                "bullets": [
                    "인공지능 기술의 발전으로 생활에 변화 발생",
                    "자연어 처리 기술이 다양한 분야에서 활용",
                    "GPT와 같은 대규모 언어 모델이 주목받음"
                ],
                "keywords": ["인공지능", "자연어", "처리", "기술", "GPT"],
                "elapsed_ms": 125
            }
        }
