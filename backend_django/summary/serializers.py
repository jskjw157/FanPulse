"""
요약 도메인 Serializers
"""
from rest_framework import serializers
from django.core.validators import URLValidator
from django.core.exceptions import ValidationError as DjangoValidationError


class SummarizeRequestSerializer(serializers.Serializer):
    """요약 API 요청 데이터 검증"""

    INPUT_TYPE_CHOICES = ['url', 'text']
    LANGUAGE_CHOICES = ['ko', 'en']
    SUMMARIZE_METHOD_CHOICES = ['rule', 'ai']

    input_type = serializers.ChoiceField(choices=INPUT_TYPE_CHOICES, required=True)
    summarize_method = serializers.ChoiceField(choices=SUMMARIZE_METHOD_CHOICES, required=False, default='rule')
    url = serializers.URLField(required=False, allow_blank=True)
    text = serializers.CharField(required=False, allow_blank=True)
    language = serializers.ChoiceField(choices=LANGUAGE_CHOICES, required=False, default='ko')
    max_length = serializers.IntegerField(required=False, default=200, min_value=50, max_value=1000)
    min_length = serializers.IntegerField(required=False, default=50, min_value=10, max_value=500)

    class Meta:
        swagger_schema_fields = {
            "example": {
                "input_type": "text",
                "summarize_method": "rule",
                "text": "인공지능 기술의 발전으로 우리 생활에 많은 변화가 일어나고 있습니다.",
                "language": "ko",
                "max_length": 200,
                "min_length": 50
            }
        }

    def validate(self, data):
        input_type = data.get('input_type')
        url = data.get('url', '').strip()
        text = data.get('text', '').strip()
        min_length = data.get('min_length')
        max_length = data.get('max_length')

        if input_type == 'url':
            if not url:
                raise serializers.ValidationError({'url': 'URL is required when input_type is "url"'})
            validator = URLValidator()
            try:
                validator(url)
            except DjangoValidationError:
                raise serializers.ValidationError({'url': 'Invalid URL format'})
        elif input_type == 'text':
            if not text:
                raise serializers.ValidationError({'text': 'Text is required when input_type is "text"'})
            if len(text) < 10:
                raise serializers.ValidationError({'text': 'Text must be at least 10 characters long'})

        if min_length > max_length:
            raise serializers.ValidationError({
                'min_length': 'min_length must be less than or equal to max_length'
            })

        return data


class SummarizeResponseSerializer(serializers.Serializer):
    """요약 API 응답 데이터 형식"""

    request_id = serializers.UUIDField()
    input_type = serializers.CharField()
    summarize_method = serializers.CharField()
    title = serializers.CharField(allow_null=True)
    source = serializers.CharField(allow_null=True)
    published_at = serializers.DateTimeField(allow_null=True)
    original_text = serializers.CharField()
    summary = serializers.CharField()
    bullets = serializers.ListField(child=serializers.CharField())
    keywords = serializers.ListField(child=serializers.CharField())
    elapsed_ms = serializers.IntegerField()
