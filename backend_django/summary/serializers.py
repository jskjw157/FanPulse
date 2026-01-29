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
                "text": "사진=SBS 캡처\n에이핑크\n윤보미\n와\n김남주\n가 상반된 근황으로 웃음을 안겼다.\n11일 방송된 SBS 예능 ‘\n런닝맨\n’에서는 그룹 에이핑크가 7년 만에 완전체로 출연해 ‘금 가방 추격자 : 골드 미 모어’ 레이스를 펼쳤다.\n이날 에이핑크 멤버들이 컴백 곡 ‘러브 미 모어’ 무대를 꾸미며 박수 속 등장했다. 근황을 나누며\n유재석\n은 “보미는 이제 결혼하지 않냐”고 물었다.\n윤보미는 “실감이 안 난다”고 수줍게 소감을 밝혔다. 유재석은 “아직 5월이 안되어서 그런다. 되면 실감 날 것”이라고 너스레를 떨었다.\n사진=SBS 캡처\n반면 김남주의 근황은 대학생이라 놀라움을 안겼다. 김남주는 “7년 만에 복학해서 10년 째 졸업을 못하고 있다”고 털어놨다.\n이에\n김종국\n은 “7년 정도면 시험 다시 봐야 되는거 아니냐”고 궁금해했고, 남주는 “그래서 심사받아 재입학을 했다”고 했다. 유재석이 “언제 졸업하냐”고 묻자, 남주는 “몰라요”라며 “\n신예은\n과 같이 복학해서 둘다 졸업을 못하고 있다”고 말해 웃음을 안겼다.\n이주인 기자 juin27@edaily.co.kr",
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
