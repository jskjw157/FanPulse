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
from .permissions import ApiKeyPermission

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
    permission_classes = []  # Health check requires no auth

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
    permission_classes = [ApiKeyPermission]

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
                    'details': 'An internal error occurred',
                    'request_id': str(request_id)
                },
                status=status.HTTP_500_INTERNAL_SERVER_ERROR
            )


#######################
# 뉴스 검색 API
#######################
class NewsSearchView(APIView):
    """
    네이버 뉴스 검색 엔드포인트

    경로: GET /api/news/search
    쿼리 파라미터:
    - query: 검색 키워드 (필수)
    - display: 결과 개수 (기본 20, 최대 100)
    - start: 시작 위치 (기본 1)
    - sort: 정렬 방식 (date/sim, 기본 date)
    """
    permission_classes = [ApiKeyPermission]

    @swagger_auto_schema(
        operation_id='news_search',
        operation_summary='뉴스 검색',
        operation_description='네이버 뉴스 API를 통해 뉴스를 검색합니다.',
        manual_parameters=[
            openapi.Parameter('query', openapi.IN_QUERY, description='검색 키워드', type=openapi.TYPE_STRING, required=True),
            openapi.Parameter('display', openapi.IN_QUERY, description='결과 개수 (1~100)', type=openapi.TYPE_INTEGER, default=20),
            openapi.Parameter('start', openapi.IN_QUERY, description='시작 위치 (1~1000)', type=openapi.TYPE_INTEGER, default=1),
            openapi.Parameter('sort', openapi.IN_QUERY, description='정렬 (date: 최신순, sim: 관련도순)', type=openapi.TYPE_STRING, default='date'),
            openapi.Parameter('save', openapi.IN_QUERY, description='JSON 저장 여부 (true/false)', type=openapi.TYPE_BOOLEAN, default=False),
            openapi.Parameter('fetch_content', openapi.IN_QUERY, description='원본 기사 내용 포함 여부', type=openapi.TYPE_BOOLEAN, default=True),
            openapi.Parameter('async_save', openapi.IN_QUERY, description='비동기 저장 여부 (true/false)', type=openapi.TYPE_BOOLEAN, default=True),
        ],
        responses={
            200: openapi.Response(
                description='검색 성공',
                examples={
                    'application/json': {
                        'success': True,
                        'total': 12345,
                        'items': [
                            {
                                'title': '뉴스 제목',
                                'description': '뉴스 설명',
                                'link': 'https://...',
                                'pubDate': '2026-01-25 10:00'
                            }
                        ],
                        'saved': True,
                        'saved_async': True,
                        'saved_message': '20개 뉴스 저장이 백그라운드에서 진행 중입니다.'
                    }
                }
            )
        },
        tags=['News']
    )
    def get(self, request):
        from .services.news_crawler import NaverNewsCrawler

        query = request.query_params.get('query', '').strip()
        if not query:
            return Response(
                {'success': False, 'error': '검색어를 입력해주세요.'},
                status=status.HTTP_400_BAD_REQUEST
            )

        try:
            display = int(request.query_params.get('display', 20))
            start = int(request.query_params.get('start', 1))
        except ValueError:
            display, start = 20, 1

        sort = request.query_params.get('sort', 'date')
        if sort not in ['date', 'sim']:
            sort = 'date'

        # JSON 저장 옵션
        save = request.query_params.get('save', 'false').lower() == 'true'
        fetch_content = request.query_params.get('fetch_content', 'true').lower() == 'true'
        async_save = request.query_params.get('async_save', 'true').lower() == 'true'

        crawler = NaverNewsCrawler()

        if save:
            # 검색 + 저장 (비동기/동기)
            result = crawler.search_and_save(
                query=query,
                display=display,
                start=start,
                sort=sort,
                fetch_content=fetch_content,
                async_save=async_save
            )
        else:
            # 검색만
            result = crawler.search(query=query, display=display, start=start, sort=sort)

        if result['success']:
            return Response(result, status=status.HTTP_200_OK)
        else:
            return Response(result, status=status.HTTP_503_SERVICE_UNAVAILABLE)


#######################
# 저장된 뉴스 조회 API
#######################
class SavedNewsListView(APIView):
    """
    저장된 뉴스 파일 목록 조회

    경로: GET /api/news/saved
    """
    permission_classes = [ApiKeyPermission]

    @swagger_auto_schema(
        operation_id='saved_news_list',
        operation_summary='저장된 뉴스 파일 목록',
        operation_description='저장된 JSON 파일 목록을 조회합니다.',
        responses={
            200: openapi.Response(
                description='조회 성공',
                examples={
                    'application/json': {
                        'success': True,
                        'files': [
                            {
                                'filename': 'kpop_20260125_100000.json',
                                'size': 12345,
                                'created_at': '2026-01-25T10:00:00'
                            }
                        ]
                    }
                }
            )
        },
        tags=['News']
    )
    def get(self, request):
        from .services.news_crawler import SavedNewsReader

        files = SavedNewsReader.list_json_files()
        return Response({
            'success': True,
            'count': len(files),
            'files': files
        }, status=status.HTTP_200_OK)

        #######################
        # PostgreSQL 조회 (주석 처리)
        #######################
        # query = request.query_params.get('query', '')
        # limit = int(request.query_params.get('limit', 100))
        # offset = int(request.query_params.get('offset', 0))
        # result = SavedNewsReader.list_from_postgres(query, limit, offset)
        # return Response(result, status=status.HTTP_200_OK if result['success'] else status.HTTP_500_INTERNAL_SERVER_ERROR)
        #######################


class SavedNewsDetailView(APIView):
    """
    저장된 뉴스 파일 상세 조회

    경로: GET /api/news/saved/<filename>
    """
    permission_classes = [ApiKeyPermission]

    @swagger_auto_schema(
        operation_id='saved_news_detail',
        operation_summary='저장된 뉴스 파일 내용',
        operation_description='특정 JSON 파일의 뉴스 목록을 조회합니다.',
        responses={
            200: openapi.Response(description='조회 성공'),
            404: openapi.Response(description='파일 없음')
        },
        tags=['News']
    )
    def get(self, request, filename):
        from .services.news_crawler import SavedNewsReader

        result = SavedNewsReader.read_json_file(filename)

        if result['success']:
            return Response(result, status=status.HTTP_200_OK)
        else:
            return Response(result, status=status.HTTP_404_NOT_FOUND)

        #######################
        # PostgreSQL 조회 (주석 처리)
        #######################
        # result = SavedNewsReader.get_from_postgres(filename)  # filename을 record_id로 사용
        # return Response(result, status=status.HTTP_200_OK if result['success'] else status.HTTP_404_NOT_FOUND)
        #######################

    @swagger_auto_schema(
        operation_id='saved_news_delete',
        operation_summary='저장된 뉴스 파일 삭제',
        operation_description='특정 JSON 파일을 삭제합니다.',
        responses={
            200: openapi.Response(description='삭제 성공'),
            404: openapi.Response(description='파일 없음')
        },
        tags=['News']
    )
    def delete(self, request, filename):
        from .services.news_crawler import SavedNewsReader

        result = SavedNewsReader.delete_json_file(filename)

        if result['success']:
            return Response(result, status=status.HTTP_200_OK)
        else:
            return Response(result, status=status.HTTP_404_NOT_FOUND)

        #######################
        # PostgreSQL 삭제 (주석 처리)
        #######################
        # result = SavedNewsReader.delete_from_postgres(filename)  # filename을 record_id로 사용
        # return Response(result, status=status.HTTP_200_OK if result['success'] else status.HTTP_404_NOT_FOUND)
        #######################


#######################
# 배치 요약 API
#######################
class BatchSummarizeView(APIView):
    """
    선택된 뉴스 배치 요약 엔드포인트

    경로: POST /api/news/batch-summarize
    """
    permission_classes = [ApiKeyPermission]

    @swagger_auto_schema(
        operation_id='batch_summarize',
        operation_summary='선택된 뉴스 배치 요약',
        operation_description='저장된 뉴스에서 선택된 아이템들을 요약하고 결과를 저장합니다.',
        request_body=openapi.Schema(
            type=openapi.TYPE_OBJECT,
            required=['items', 'method'],
            properties={
                'items': openapi.Schema(
                    type=openapi.TYPE_ARRAY,
                    items=openapi.Schema(
                        type=openapi.TYPE_OBJECT,
                        properties={
                            'title': openapi.Schema(type=openapi.TYPE_STRING),
                            'origainal_news': openapi.Schema(type=openapi.TYPE_STRING),
                            'originallink': openapi.Schema(type=openapi.TYPE_STRING),
                            'pubDate': openapi.Schema(type=openapi.TYPE_STRING),
                        }
                    ),
                    description='요약할 뉴스 아이템 목록'
                ),
                'method': openapi.Schema(
                    type=openapi.TYPE_STRING,
                    enum=['rule', 'ai'],
                    description='요약 방식 (rule: 알고리즘, ai: AI 모델)'
                ),
                'max_length': openapi.Schema(type=openapi.TYPE_INTEGER, default=300),
                'min_length': openapi.Schema(type=openapi.TYPE_INTEGER, default=50),
            }
        ),
        responses={
            200: openapi.Response(
                description='요약 성공',
                examples={
                    'application/json': {
                        'success': True,
                        'filename': 'summarized_rule_20260125_120000.json',
                        'count': 5,
                        'items': []
                    }
                }
            )
        },
        tags=['News']
    )
    def post(self, request):
        items = request.data.get('items', [])
        method = request.data.get('method', 'rule')
        max_length = request.data.get('max_length', 300)
        min_length = request.data.get('min_length', 50)

        if not items:
            return Response(
                {'success': False, 'error': '요약할 뉴스를 선택해주세요.'},
                status=status.HTTP_400_BAD_REQUEST
            )

        if method not in ['rule', 'ai']:
            method = 'rule'

        # AI 사용 가능 여부 확인
        if method == 'ai' and not check_ai_available():
            logger.warning("AI requested but not available, falling back to rule-based")
            method = 'rule'

        # 요약기 선택
        if method == 'ai':
            summarizer = AISummarizer(language='ko')
        else:
            summarizer = ArticleSummarizer(language='ko')

        # 각 아이템 요약
        summarized_items = []
        for item in items:
            text = item.get('origainal_news', '')
            if not text or len(text.strip()) < 50:
                # 텍스트가 짧으면 건너뛰기
                summarized_items.append({
                    **item,
                    'summary': '',
                    'bullets': [],
                    'keywords': [],
                    'summarized': False,
                    'error': '텍스트가 너무 짧습니다.'
                })
                continue

            try:
                result = summarizer.summarize(
                    text=text,
                    max_length=max_length,
                    min_length=min_length
                )

                summarized_items.append({
                    'title': item.get('title', ''),
                    'originallink': item.get('originallink', ''),
                    'pubDate': item.get('pubDate', ''),
                    'original_text': text[:500] + '...' if len(text) > 500 else text,
                    'summary': result['summary'],
                    'bullets': result['bullets'],
                    'keywords': result['keywords'],
                    'summarized': True,
                    'error': None
                })
            except Exception as e:
                logger.exception(f"요약 실패: {item.get('title', '')}")
                summarized_items.append({
                    **item,
                    'summary': '',
                    'bullets': [],
                    'keywords': [],
                    'summarized': False,
                    'error': 'Summarization failed'
                })

        # 결과 저장
        from .services.news_crawler import SummarizedNewsManager
        save_result = SummarizedNewsManager.save_summarized_news(summarized_items, method)

        if save_result['success']:
            return Response({
                'success': True,
                'filename': save_result['filename'],
                'count': save_result['count'],
                'method': method,
                'items': summarized_items
            }, status=status.HTTP_200_OK)
        else:
            return Response({
                'success': False,
                'error': save_result['error'],
                'items': summarized_items
            }, status=status.HTTP_500_INTERNAL_SERVER_ERROR)


#######################
# 요약된 뉴스 조회 API
#######################
class SummarizedNewsListView(APIView):
    """
    요약된 뉴스 파일 목록 조회

    경로: GET /api/news/summarized
    """
    permission_classes = [ApiKeyPermission]

    @swagger_auto_schema(
        operation_id='summarized_news_list',
        operation_summary='요약된 뉴스 파일 목록',
        operation_description='요약된 뉴스 JSON 파일 목록을 조회합니다.',
        responses={
            200: openapi.Response(
                description='조회 성공',
                examples={
                    'application/json': {
                        'success': True,
                        'files': [
                            {
                                'filename': 'summarized_rule_20260125_120000.json',
                                'method': 'rule',
                                'count': 5,
                                'created_at': '2026-01-25T12:00:00'
                            }
                        ]
                    }
                }
            )
        },
        tags=['News']
    )
    def get(self, request):
        from .services.news_crawler import SummarizedNewsManager

        files = SummarizedNewsManager.list_summarized_files()
        return Response({
            'success': True,
            'count': len(files),
            'files': files
        }, status=status.HTTP_200_OK)


class SummarizedNewsDetailView(APIView):
    """
    요약된 뉴스 파일 상세 조회

    경로: GET /api/news/summarized/<filename>
    """
    permission_classes = [ApiKeyPermission]

    @swagger_auto_schema(
        operation_id='summarized_news_detail',
        operation_summary='요약된 뉴스 파일 내용',
        operation_description='특정 요약 파일의 내용을 조회합니다.',
        responses={
            200: openapi.Response(description='조회 성공'),
            404: openapi.Response(description='파일 없음')
        },
        tags=['News']
    )
    def get(self, request, filename):
        from .services.news_crawler import SummarizedNewsManager

        result = SummarizedNewsManager.read_summarized_file(filename)

        if result['success']:
            return Response(result, status=status.HTTP_200_OK)
        else:
            return Response(result, status=status.HTTP_404_NOT_FOUND)

    @swagger_auto_schema(
        operation_id='summarized_news_delete',
        operation_summary='요약된 뉴스 파일 삭제',
        operation_description='특정 요약 파일을 삭제합니다.',
        responses={
            200: openapi.Response(description='삭제 성공'),
            404: openapi.Response(description='파일 없음')
        },
        tags=['News']
    )
    def delete(self, request, filename):
        from .services.news_crawler import SummarizedNewsManager

        result = SummarizedNewsManager.delete_summarized_file(filename)

        if result['success']:
            return Response(result, status=status.HTTP_200_OK)
        else:
            return Response(result, status=status.HTTP_404_NOT_FOUND)


# NOTE: DBNewsListView, DBNewsDetailView 제거됨 (Phase 3 슬리밍)
# Spring이 DB를 직접 조회하므로 Django AI 사이드카에서 불필요
#######################
# 댓글 필터링 API
# NOTE: DBNewsListView, DBNewsDetailView 제거됨 (Phase 3 슬리밍)
# Spring이 DB를 직접 조회하므로 Django AI 사이드카에서 불필요
#######################
class CommentFilterTestView(APIView):
    """
    댓글 필터링 테스트 엔드포인트

    경로: POST /api/comments/filter/test
    """
    permission_classes = [ApiKeyPermission]

    @swagger_auto_schema(
        operation_id='comment_filter_test',
        operation_summary='댓글 필터링 테스트',
        operation_description='''
입력된 댓글 내용이 등록된 필터링 규칙에 걸리는지 테스트합니다.

## 지원하는 필터 타입
- **keyword**: 금칙어 필터 (쉼표로 구분된 키워드 목록)
- **regex**: 정규식 패턴 매칭
- **spam**: 스팸 패턴 (이모지/특수문자 과다 사용)
- **url**: URL 차단 (특정 도메인 또는 모든 URL)
- **repeat**: 반복 문자 탐지 (예: "ㅋㅋㅋㅋㅋㅋㅋ")

## 요청 예시
```json
{
    "content": "이 댓글은 테스트입니다."
}
```

## 응답 - 필터링됨
```json
{
    "is_filtered": true,
    "action": "block",
    "rule_id": "550e8400-e29b-41d4-a716-446655440000",
    "rule_name": "욕설 필터",
    "filter_type": "keyword",
    "matched_pattern": "금칙어",
    "reason": "[욕설 필터] keyword 규칙에 의해 필터링됨"
}
```

## 응답 - 통과
```json
{
    "is_filtered": false,
    "action": null,
    "rule_id": null,
    "rule_name": null,
    "filter_type": null,
    "matched_pattern": null,
    "reason": null
}
```
        ''',
        request_body=openapi.Schema(
            type=openapi.TYPE_OBJECT,
            required=['content'],
            properties={
                'content': openapi.Schema(
                    type=openapi.TYPE_STRING,
                    description='테스트할 댓글 내용',
                    example='좋은 게시글이네요! 응원합니다.'
                )
            },
            example={
                'content': '좋은 게시글이네요! 응원합니다.'
            }
        ),
        responses={
            200: openapi.Response(
                description='테스트 완료',
                examples={
                    'application/json': {
                        'is_filtered': False,
                        'action': None,
                        'rule_id': None,
                        'rule_name': None,
                        'filter_type': None,
                        'matched_pattern': None,
                        'reason': None
                    }
                }
            ),
            400: openapi.Response(
                description='잘못된 요청',
                examples={
                    'application/json': {
                        'error': 'Validation failed',
                        'details': {'content': ['이 필드는 필수 항목입니다.']}
                    }
                }
            )
        },
        tags=['Comment Filter']
    )
    def post(self, request):
        from .serializers import CommentFilterTestRequestSerializer
        from .services.comment_filter import get_filter_service

        serializer = CommentFilterTestRequestSerializer(data=request.data)
        if not serializer.is_valid():
            return Response(
                {'error': 'Validation failed', 'details': serializer.errors},
                status=status.HTTP_400_BAD_REQUEST
            )

        content = serializer.validated_data['content']
        filter_service = get_filter_service()
        result = filter_service.filter_comment(content)

        response_data = {
            'is_filtered': result.is_filtered,
            'action': result.action,
            'rule_id': result.rule_id,
            'rule_name': result.rule_name,
            'filter_type': result.filter_type,
            'matched_pattern': result.matched_pattern,
            'reason': result.reason
        }

        return Response(response_data, status=status.HTTP_200_OK)


class CommentFilterBatchView(APIView):
    """
    댓글 일괄 필터링 엔드포인트

    경로: POST /api/comments/filter/batch
    """
    permission_classes = [ApiKeyPermission]

    @swagger_auto_schema(
        operation_id='comment_filter_batch',
        operation_summary='댓글 일괄 필터링',
        operation_description='''
여러 댓글을 한 번에 필터링 테스트합니다. 최대 100개까지 가능합니다.

## 요청 예시
```json
{
    "comments": [
        "첫 번째 정상 댓글입니다.",
        "두 번째 댓글에 금칙어가 포함됨",
        "세 번째 정상 댓글입니다.",
        "네 번째 댓글 http://spam-link.com"
    ]
}
```

## 응답 예시
```json
{
    "total": 4,
    "filtered_count": 2,
    "results": [
        {"index": 0, "is_filtered": false},
        {"index": 1, "is_filtered": true, "action": "block", "rule_name": "욕설 필터", "filter_type": "keyword", "matched_pattern": "금칙어", "reason": "[욕설 필터] keyword 규칙에 의해 필터링됨"},
        {"index": 2, "is_filtered": false},
        {"index": 3, "is_filtered": true, "action": "block", "rule_name": "URL 차단", "filter_type": "url", "matched_pattern": "http://spam-link.com", "reason": "[URL 차단] url 규칙에 의해 필터링됨"}
    ]
}
```
        ''',
        request_body=openapi.Schema(
            type=openapi.TYPE_OBJECT,
            required=['comments'],
            properties={
                'comments': openapi.Schema(
                    type=openapi.TYPE_ARRAY,
                    items=openapi.Schema(type=openapi.TYPE_STRING),
                    description='테스트할 댓글 내용 목록 (최대 100개)',
                    example=['첫 번째 댓글입니다.', '두 번째 댓글입니다.', '세 번째 댓글입니다.']
                )
            },
            example={
                'comments': [
                    '좋은 게시글이네요!',
                    '응원합니다~',
                    '다음 소식도 기대할게요.'
                ]
            }
        ),
        responses={
            200: openapi.Response(
                description='테스트 완료',
                examples={
                    'application/json': {
                        'total': 3,
                        'filtered_count': 0,
                        'results': [
                            {'index': 0, 'is_filtered': False},
                            {'index': 1, 'is_filtered': False},
                            {'index': 2, 'is_filtered': False}
                        ]
                    }
                }
            ),
            400: openapi.Response(
                description='잘못된 요청',
                examples={
                    'application/json': {
                        'error': 'Validation failed',
                        'details': {'comments': ['이 필드는 필수 항목입니다.']}
                    }
                }
            )
        },
        tags=['Comment Filter']
    )
    def post(self, request):
        from .serializers import CommentFilterBatchRequestSerializer
        from .services.comment_filter import get_filter_service

        serializer = CommentFilterBatchRequestSerializer(data=request.data)
        if not serializer.is_valid():
            return Response(
                {'error': 'Validation failed', 'details': serializer.errors},
                status=status.HTTP_400_BAD_REQUEST
            )

        comments = serializer.validated_data['comments']
        filter_service = get_filter_service()
        results = filter_service.batch_filter(comments)

        response_results = []
        filtered_count = 0
        for i, result in enumerate(results):
            item = {
                'index': i,
                'is_filtered': result.is_filtered,
            }
            if result.is_filtered:
                filtered_count += 1
                item.update({
                    'action': result.action,
                    'rule_name': result.rule_name,
                    'filter_type': result.filter_type,
                    'matched_pattern': result.matched_pattern,
                    'reason': result.reason
                })
            response_results.append(item)

        return Response({
            'total': len(comments),
            'filtered_count': filtered_count,
            'results': response_results
        }, status=status.HTTP_200_OK)

# NOTE: CommentFilterRuleListView, CommentFilterRuleDetailView,
# FilteredCommentLogListView 제거됨 (Phase 3 슬리밍)
# 필터 규칙 CRUD 및 로그 조회는 Spring이 담당
# Django AI 사이드카는 필터 실행(test/batch)만 수행


#######################
# AI 모더레이션 API
#######################
class AIModerationCheckView(APIView):
    """
    AI 기반 콘텐츠 모더레이션 검사 엔드포인트

    경로: POST /api/moderation/check
    """
    permission_classes = [ApiKeyPermission]

    @swagger_auto_schema(
        operation_id='ai_moderation_check',
        operation_summary='AI 콘텐츠 모더레이션 검사',
        operation_description='''
AI 모델을 사용하여 텍스트 콘텐츠의 부적절한 내용을 감지합니다.

## 감지 카테고리
- **profanity**: 욕설/비속어
- **spam**: 스팸 콘텐츠
- **adult**: 성인 콘텐츠
- **violence**: 폭력적 콘텐츠
- **hate**: 혐오 발언
- **harassment**: 괴롭힘/악성 댓글

## 권장 조치 (action)
- **allow**: 허용 (점수 < 0.5)
- **warning**: 경고 표시 (0.5 <= 점수 < 0.7)
- **review**: 관리자 검토 대기 (0.7 <= 점수 < 0.85)
- **block**: 차단 (점수 >= 0.85)

## 요청 예시
```json
{
    "text": "검사할 댓글 또는 게시글 내용",
    "use_cache": true,
    "thresholds": {
        "profanity": 0.7,
        "spam": 0.8
    }
}
```

## 응답 예시 - 부적절한 콘텐츠 감지
```json
{
    "is_flagged": true,
    "action": "block",
    "categories": [
        {
            "category": "profanity",
            "score": 0.95,
            "is_flagged": true,
            "threshold": 0.7
        }
    ],
    "highest_category": "profanity",
    "highest_score": 0.95,
    "confidence": 0.95,
    "model_used": "ko",
    "processing_time_ms": 45,
    "cached": false,
    "error": null
}
```

## 응답 예시 - 정상 콘텐츠
```json
{
    "is_flagged": false,
    "action": "allow",
    "categories": [],
    "highest_category": null,
    "highest_score": 0.1,
    "confidence": 0.9,
    "model_used": "ko",
    "processing_time_ms": 38,
    "cached": false,
    "error": null
}
```
        ''',
        request_body=openapi.Schema(
            type=openapi.TYPE_OBJECT,
            required=['text'],
            properties={
                'text': openapi.Schema(
                    type=openapi.TYPE_STRING,
                    description='검사할 텍스트 내용',
                    example='검사할 댓글 또는 게시글 내용입니다.'
                ),
                'use_cache': openapi.Schema(
                    type=openapi.TYPE_BOOLEAN,
                    default=True,
                    description='캐시 사용 여부 (기본값: true)'
                ),
                'thresholds': openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    description='카테고리별 커스텀 임계값',
                    example={'profanity': 0.7, 'spam': 0.8}
                )
            },
            example={
                'text': '검사할 댓글 또는 게시글 내용입니다.',
                'use_cache': True
            }
        ),
        responses={
            200: openapi.Response(
                description='검사 완료',
                examples={
                    'application/json': {
                        'is_flagged': False,
                        'action': 'allow',
                        'categories': [],
                        'highest_category': None,
                        'highest_score': 0.1,
                        'confidence': 0.9,
                        'model_used': 'ko',
                        'processing_time_ms': 38,
                        'cached': False,
                        'error': None
                    }
                }
            ),
            400: openapi.Response(
                description='잘못된 요청',
                examples={
                    'application/json': {
                        'error': 'Validation failed',
                        'details': {'text': ['이 필드는 필수 항목입니다.']}
                    }
                }
            )
        },
        tags=['AI Moderation']
    )
    def post(self, request):
        from .serializers import AIModerationCheckRequestSerializer
        from .services.ai_moderation import get_ai_moderator

        serializer = AIModerationCheckRequestSerializer(data=request.data)
        if not serializer.is_valid():
            return Response(
                {'error': 'Validation failed', 'details': serializer.errors},
                status=status.HTTP_400_BAD_REQUEST
            )

        text = serializer.validated_data['text']
        use_cache = serializer.validated_data.get('use_cache', True)
        thresholds = serializer.validated_data.get('thresholds')

        moderator = get_ai_moderator(language='ko')
        result = moderator.check(
            text=text,
            use_cache=use_cache,
            custom_thresholds=thresholds
        )

        return Response(result.to_dict(), status=status.HTTP_200_OK)


class AIModerationBatchView(APIView):
    """
    AI 기반 콘텐츠 일괄 모더레이션 검사 엔드포인트

    경로: POST /api/moderation/batch
    """
    permission_classes = [ApiKeyPermission]

    @swagger_auto_schema(
        operation_id='ai_moderation_batch',
        operation_summary='AI 콘텐츠 일괄 모더레이션',
        operation_description='''
여러 텍스트를 한 번에 AI 모더레이션 검사합니다. 최대 50개까지 가능합니다.

## 요청 예시
```json
{
    "texts": [
        "첫 번째 검사 텍스트",
        "두 번째 검사 텍스트",
        "세 번째 검사 텍스트"
    ],
    "use_cache": true
}
```

## 응답 예시
```json
{
    "total": 3,
    "flagged_count": 1,
    "results": [
        {
            "index": 0,
            "is_flagged": false,
            "action": "allow",
            "highest_category": null,
            "highest_score": 0.1
        },
        {
            "index": 1,
            "is_flagged": true,
            "action": "block",
            "highest_category": "profanity",
            "highest_score": 0.95
        },
        {
            "index": 2,
            "is_flagged": false,
            "action": "allow",
            "highest_category": null,
            "highest_score": 0.2
        }
    ]
}
```
        ''',
        request_body=openapi.Schema(
            type=openapi.TYPE_OBJECT,
            required=['texts'],
            properties={
                'texts': openapi.Schema(
                    type=openapi.TYPE_ARRAY,
                    items=openapi.Schema(type=openapi.TYPE_STRING),
                    description='검사할 텍스트 목록 (최대 50개)',
                    example=['첫 번째 텍스트', '두 번째 텍스트', '세 번째 텍스트']
                ),
                'use_cache': openapi.Schema(
                    type=openapi.TYPE_BOOLEAN,
                    default=True,
                    description='캐시 사용 여부'
                )
            },
            example={
                'texts': ['첫 번째 텍스트', '두 번째 텍스트', '세 번째 텍스트'],
                'use_cache': True
            }
        ),
        responses={
            200: openapi.Response(
                description='검사 완료',
                examples={
                    'application/json': {
                        'total': 3,
                        'flagged_count': 0,
                        'results': [
                            {'index': 0, 'is_flagged': False, 'action': 'allow', 'highest_category': None, 'highest_score': 0.1},
                            {'index': 1, 'is_flagged': False, 'action': 'allow', 'highest_category': None, 'highest_score': 0.15},
                            {'index': 2, 'is_flagged': False, 'action': 'allow', 'highest_category': None, 'highest_score': 0.08}
                        ]
                    }
                }
            ),
            400: openapi.Response(
                description='잘못된 요청',
                examples={
                    'application/json': {
                        'error': 'Validation failed',
                        'details': {'texts': ['이 필드는 필수 항목입니다.']}
                    }
                }
            )
        },
        tags=['AI Moderation']
    )
    def post(self, request):
        from .serializers import AIModerationBatchRequestSerializer
        from .services.ai_moderation import get_ai_moderator

        serializer = AIModerationBatchRequestSerializer(data=request.data)
        if not serializer.is_valid():
            return Response(
                {'error': 'Validation failed', 'details': serializer.errors},
                status=status.HTTP_400_BAD_REQUEST
            )

        texts = serializer.validated_data['texts']
        use_cache = serializer.validated_data.get('use_cache', True)

        moderator = get_ai_moderator(language='ko')
        results = moderator.batch_check(texts, use_cache)

        response_results = []
        flagged_count = 0
        for i, result in enumerate(results):
            item = {
                'index': i,
                'is_flagged': result.is_flagged,
                'action': result.action,
                'highest_category': result.highest_category,
                'highest_score': round(result.highest_score, 4)
            }
            if result.is_flagged:
                flagged_count += 1
            response_results.append(item)

        return Response({
            'total': len(texts),
            'flagged_count': flagged_count,
            'results': response_results
        }, status=status.HTTP_200_OK)


class AIModerationStatusView(APIView):
    """
    AI 모더레이션 상태 확인 엔드포인트

    경로: GET /api/moderation/status
    """
    permission_classes = [ApiKeyPermission]

    @swagger_auto_schema(
        operation_id='ai_moderation_status',
        operation_summary='AI 모더레이션 상태 확인',
        operation_description='''
AI 모더레이션 기능의 사용 가능 여부와 시스템 상태를 확인합니다.

## 응답 예시
```json
{
    "available": true,
    "transformers_installed": true,
    "torch_installed": true,
    "gpu_available": true,
    "gpu_name": "NVIDIA GeForce RTX 3080",
    "models_loaded": ["korean"],
    "error": null
}
```
        ''',
        responses={
            200: openapi.Response(
                description='상태 조회 성공',
                examples={
                    'application/json': {
                        'available': True,
                        'transformers_installed': True,
                        'torch_installed': True,
                        'gpu_available': True,
                        'models_loaded': ['korean'],
                        'error': None
                    }
                }
            )
        },
        tags=['AI Moderation']
    )
    def get(self, request):
        from .services.ai_moderation import check_ai_moderation_available

        status_info = check_ai_moderation_available()
        return Response(status_info, status=status.HTTP_200_OK)
