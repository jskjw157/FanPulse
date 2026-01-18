"""
#######################
# API Views 모듈
#######################
# 이 파일은 뉴스 요약 API의 핵심 엔드포인트를 정의합니다.
#
# 주요 기능:
# - HealthCheckView: 서버 상태 확인 API
# - SummarizeView: 텍스트/URL 요약 API (핵심 기능)
#
# 요약 방식:
# - rule: 단어 빈도 기반 알고리즘 (빠름)
# - ai: AI 모델 기반 요약 (자연스러움)
#######################
"""
import logging
import time
import uuid
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from django.utils.decorators import method_decorator
from django.views.decorators.cache import never_cache
from drf_yasg.utils import swagger_auto_schema
from drf_yasg import openapi

#######################
# 내부 모듈 임포트
#######################
from .serializers import SummarizeRequestSerializer, SummarizeResponseSerializer
from .services.extractor import ArticleExtractor      # URL에서 기사 추출
from .services.summarizer import ArticleSummarizer    # 규칙 기반 요약
from .services.ai_summarizer import AISummarizer, check_ai_available  # AI 기반 요약

#######################
# 로거 설정
#######################
logger = logging.getLogger(__name__)


#######################
# 헬스체크 API
#######################
class HealthCheckView(APIView):
    """
    서버 상태 확인 엔드포인트

    용도: 서버가 정상 동작하는지 확인 (모니터링, 로드밸런서 등에서 사용)
    경로: GET /api/health
    응답: {"status": "ok"}
    """

    @swagger_auto_schema(
        operation_id='health_check',
        operation_summary='서버 상태 확인',
        operation_description='서버가 정상적으로 동작하는지 확인합니다.',
        responses={
            200: openapi.Response(
                description='서버 정상',
                examples={
                    'application/json': {'status': 'ok'}
                }
            )
        },
        tags=['Health']
    )
    @method_decorator(never_cache)  # 캐시 비활성화 (항상 실시간 상태 확인)
    def get(self, request):
        return Response({'status': 'ok'}, status=status.HTTP_200_OK)


#######################
# 요약 API (핵심 기능)
#######################
class SummarizeView(APIView):
    """
    뉴스 기사 요약 엔드포인트 (메인 기능)

    경로: POST /api/summarize

    지원 입력 방식:
    - url: 뉴스 기사 URL을 입력하면 자동으로 기사 추출 후 요약
    - text: 텍스트를 직접 입력하여 요약

    지원 요약 방식:
    - rule: 단어 빈도 기반 추출형 요약 (빠름, 기본값)
    - ai: HuggingFace 모델 기반 생성형 요약 (자연스러움)
    """

    #######################
    # Swagger 문서 설정
    #######################
    @swagger_auto_schema(
        operation_id='summarize',
        operation_summary='텍스트/URL 요약',
        operation_description='''
뉴스 기사 URL 또는 텍스트를 입력받아 요약을 생성합니다.

## 요약 방식
- **rule**: 단어 빈도 기반 알고리즘 요약 (빠름)
- **ai**: AI 모델 기반 요약 (자연스러움, 처음 실행 시 모델 다운로드 필요)

## 입력 타입
- **url**: 뉴스 기사 URL 입력
- **text**: 직접 텍스트 입력

## 지원 언어
- **ko**: 한국어
- **en**: 영어

---

## 요청 예시 1: 텍스트 요약 (알고리즘)
```json
{
    "input_type": "text",
    "summarize_method": "rule",
    "text": "인공지능 기술의 발전으로 우리 생활에 많은 변화가...",
    "language": "ko",
    "max_length": 200,
    "min_length": 50
}
```

## 요청 예시 2: URL 요약 (AI 모델)
```json
{
    "input_type": "url",
    "summarize_method": "ai",
    "url": "https://news.example.com/article/12345",
    "language": "ko",
    "max_length": 300,
    "min_length": 100
}
```
        ''',
        request_body=SummarizeRequestSerializer,
        responses={
            200: openapi.Response(
                description='요약 성공',
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        'request_id': openapi.Schema(type=openapi.TYPE_STRING, description='요청 ID'),
                        'input_type': openapi.Schema(type=openapi.TYPE_STRING, description='입력 타입'),
                        'summarize_method': openapi.Schema(type=openapi.TYPE_STRING, description='요약 방식'),
                        'title': openapi.Schema(type=openapi.TYPE_STRING, description='기사 제목', nullable=True),
                        'source': openapi.Schema(type=openapi.TYPE_STRING, description='출처', nullable=True),
                        'published_at': openapi.Schema(type=openapi.TYPE_STRING, description='발행일', nullable=True),
                        'original_text': openapi.Schema(type=openapi.TYPE_STRING, description='원문'),
                        'summary': openapi.Schema(type=openapi.TYPE_STRING, description='요약'),
                        'bullets': openapi.Schema(type=openapi.TYPE_ARRAY, items=openapi.Schema(type=openapi.TYPE_STRING), description='주요 포인트'),
                        'keywords': openapi.Schema(type=openapi.TYPE_ARRAY, items=openapi.Schema(type=openapi.TYPE_STRING), description='키워드'),
                        'elapsed_ms': openapi.Schema(type=openapi.TYPE_INTEGER, description='처리 시간(ms)'),
                    }
                ),
                examples={
                    'application/json': {
                        "request_id": "550e8400-e29b-41d4-a716-446655440000",
                        "input_type": "text",
                        "summarize_method": "rule",
                        "title": None,
                        "source": None,
                        "published_at": None,
                        "original_text": "인공지능 기술의 발전으로 우리 생활에 많은 변화가 일어나고 있습니다...",
                        "summary": "인공지능 기술의 발전으로 생활에 변화가 일어나고 있으며, 자연어 처리 기술이 다양한 분야에서 활용되고 있습니다.",
                        "bullets": [
                            "인공지능 기술의 발전으로 생활에 변화 발생",
                            "자연어 처리 기술이 번역, 요약, 대화 등에 활용",
                            "GPT와 같은 대규모 언어 모델이 주목받음"
                        ],
                        "keywords": ["인공지능", "자연어", "처리", "기술", "GPT"],
                        "elapsed_ms": 125
                    }
                }
            ),
            400: openapi.Response(
                description='잘못된 요청',
                examples={
                    'application/json': {
                        'error': 'Validation failed',
                        'details': {'url': ['URL is required when input_type is "url"']}
                    }
                }
            ),
            422: openapi.Response(
                description='기사 추출 실패',
                examples={
                    'application/json': {
                        'error': 'Article extraction failed',
                        'details': 'Failed to extract article content'
                    }
                }
            ),
            500: openapi.Response(
                description='서버 오류',
                examples={
                    'application/json': {
                        'error': 'Internal server error',
                        'details': 'Error message',
                        'request_id': '550e8400-e29b-41d4-a716-446655440000'
                    }
                }
            )
        },
        tags=['Summarize']
    )
    def post(self, request):
        """
        요약 요청 처리 메인 함수

        처리 흐름:
        1. 요청 유효성 검사 (Serializer 사용)
        2. 입력 타입에 따라 기사 추출 (URL) 또는 텍스트 직접 사용
        3. 요약 방식에 따라 rule 또는 ai 요약기 선택
        4. 요약 수행 및 결과 반환
        """

        #######################
        # 처리 시간 측정 시작
        #######################
        start_time = time.time()
        request_id = uuid.uuid4()  # 요청 추적용 고유 ID

        logger.info(f"[{request_id}] Summarization request received")

        #######################
        # 1단계: 요청 유효성 검사
        #######################
        serializer = SummarizeRequestSerializer(data=request.data)
        if not serializer.is_valid():
            logger.warning(f"[{request_id}] Invalid request: {serializer.errors}")
            return Response(
                {'error': 'Validation failed', 'details': serializer.errors},
                status=status.HTTP_400_BAD_REQUEST
            )

        #######################
        # 2단계: 요청 데이터 추출
        #######################
        validated_data = serializer.validated_data
        input_type = validated_data['input_type']           # 'url' 또는 'text'
        language = validated_data['language']               # 'ko' 또는 'en'
        max_length = validated_data['max_length']           # 요약 최대 길이
        min_length = validated_data['min_length']           # 요약 최소 길이
        summarize_method = validated_data.get('summarize_method', 'rule')  # 'rule' 또는 'ai'

        #######################
        # AI 사용 가능 여부 확인
        #######################
        # AI 요약이 요청되었지만 라이브러리가 없으면 rule로 폴백
        if summarize_method == 'ai' and not check_ai_available():
            logger.warning(f"[{request_id}] AI requested but not available, falling back to rule-based")
            summarize_method = 'rule'

        try:
            #######################
            # 3단계: 기사 내용 추출
            #######################
            if input_type == 'url':
                # URL에서 기사 추출
                url = validated_data['url']
                logger.info(f"[{request_id}] Extracting article from URL: {url}")

                extractor = ArticleExtractor()
                extraction_result = extractor.extract(url)

                # 추출 실패 시 에러 반환
                if not extraction_result['success']:
                    logger.error(f"[{request_id}] Extraction failed: {extraction_result['error']}")
                    return Response(
                        {
                            'error': 'Article extraction failed',
                            'details': extraction_result['error']
                        },
                        status=status.HTTP_422_UNPROCESSABLE_ENTITY
                    )

                # 추출된 데이터 저장
                article_text = extraction_result['text']
                article_title = extraction_result['title']
                article_source = extraction_result['source']
                article_published_at = extraction_result['published_at']

            else:
                # 직접 입력된 텍스트 사용
                article_text = validated_data['text']
                article_title = None
                article_source = None
                article_published_at = None
                logger.info(f"[{request_id}] Processing direct text input")

            #######################
            # 4단계: 요약 수행
            #######################
            logger.info(f"[{request_id}] Generating summary using {summarize_method} method")

            # 요약 방식에 따라 적절한 요약기 선택
            if summarize_method == 'ai':
                summarizer = AISummarizer(language=language)      # AI 기반 요약기
            else:
                summarizer = ArticleSummarizer(language=language)  # 규칙 기반 요약기

            # 요약 실행
            summary_result = summarizer.summarize(
                text=article_text,
                max_length=max_length,
                min_length=min_length
            )

            #######################
            # 5단계: 응답 데이터 구성
            #######################
            elapsed_ms = int((time.time() - start_time) * 1000)  # 처리 시간 계산

            response_data = {
                'request_id': str(request_id),           # 요청 ID
                'input_type': input_type,                # 입력 타입
                'summarize_method': summarize_method,    # 사용된 요약 방식
                'title': article_title,                  # 기사 제목 (URL일 때만)
                'source': article_source,                # 출처 도메인 (URL일 때만)
                'published_at': article_published_at,    # 발행일 (URL일 때만)
                'original_text': article_text,           # 원문 텍스트
                'summary': summary_result['summary'],    # 요약 결과
                'bullets': summary_result['bullets'],    # 핵심 포인트 목록
                'keywords': summary_result['keywords'],  # 추출된 키워드
                'elapsed_ms': elapsed_ms                 # 처리 시간(ms)
            }

            #######################
            # 6단계: 응답 유효성 검사 및 반환
            #######################
            response_serializer = SummarizeResponseSerializer(data=response_data)
            if response_serializer.is_valid():
                logger.info(f"[{request_id}] Request completed successfully in {elapsed_ms}ms")
                return Response(response_serializer.validated_data, status=status.HTTP_200_OK)
            else:
                # 응답 형식 오류 (내부 에러)
                logger.error(f"[{request_id}] Response validation failed: {response_serializer.errors}")
                return Response(
                    {'error': 'Internal error', 'details': 'Response formatting failed'},
                    status=status.HTTP_500_INTERNAL_SERVER_ERROR
                )

        #######################
        # 예외 처리
        #######################
        except Exception as e:
            elapsed_ms = int((time.time() - start_time) * 1000)
            logger.exception(f"[{request_id}] Unexpected error after {elapsed_ms}ms")
            return Response(
                {
                    'error': 'Internal server error',
                    'details': str(e),
                    'request_id': str(request_id)
                },
                status=status.HTTP_500_INTERNAL_SERVER_ERROR
            )
