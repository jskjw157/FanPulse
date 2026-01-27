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
                    'error': str(e)
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


#######################
# DB 뉴스 조회 API
#######################
class DBNewsListView(APIView):
    """
    DB에 저장된 뉴스 목록 조회

    경로: GET /api/news/db
    """

    @swagger_auto_schema(
        operation_id='db_news_list',
        operation_summary='DB 뉴스 목록',
        operation_description='PostgreSQL DB에 저장된 뉴스 목록을 조회합니다.',
        manual_parameters=[
            openapi.Parameter('limit', openapi.IN_QUERY, description='조회 개수', type=openapi.TYPE_INTEGER, default=100),
            openapi.Parameter('offset', openapi.IN_QUERY, description='시작 위치', type=openapi.TYPE_INTEGER, default=0),
            openapi.Parameter('source', openapi.IN_QUERY, description='뉴스 출처 필터', type=openapi.TYPE_STRING),
        ],
        responses={
            200: openapi.Response(
                description='조회 성공',
                examples={
                    'application/json': {
                        'success': True,
                        'total': 150,
                        'count': 100,
                        'items': [
                            {
                                'id': 'uuid',
                                'title': '뉴스 제목',
                                'content': '본문...',
                                'url': 'https://...',
                                'source': 'naver',
                                'published_at': '2026-01-26T12:00:00',
                                'created_at': '2026-01-26T12:00:00'
                            }
                        ]
                    }
                }
            )
        },
        tags=['News DB']
    )
    def get(self, request):
        from .services.news_crawler import get_news_from_db

        limit = int(request.query_params.get('limit', 100))
        offset = int(request.query_params.get('offset', 0))
        source = request.query_params.get('source')

        result = get_news_from_db(limit=limit, offset=offset, source=source)

        return Response({
            'success': result['success'],
            'total': result.get('total', 0),
            'count': len(result.get('items', [])),
            'items': result.get('items', []),
            'error': result.get('error')
        }, status=status.HTTP_200_OK if result['success'] else status.HTTP_500_INTERNAL_SERVER_ERROR)


class DBNewsDetailView(APIView):
    """
    DB에 저장된 뉴스 상세 조회

    경로: GET /api/news/db/<news_id>
    """

    @swagger_auto_schema(
        operation_id='db_news_detail',
        operation_summary='DB 뉴스 상세',
        operation_description='PostgreSQL DB에 저장된 특정 뉴스를 조회합니다.',
        responses={
            200: openapi.Response(description='조회 성공'),
            404: openapi.Response(description='뉴스 없음')
        },
        tags=['News DB']
    )
    def get(self, request, news_id):
        from .services.news_crawler import get_news_detail_from_db

        result = get_news_detail_from_db(news_id)

        if result['success']:
            return Response(result, status=status.HTTP_200_OK)
        else:
            return Response(result, status=status.HTTP_404_NOT_FOUND)

    @swagger_auto_schema(
        operation_id='db_news_delete',
        operation_summary='DB 뉴스 삭제',
        operation_description='PostgreSQL DB에서 특정 뉴스를 삭제합니다.',
        responses={
            200: openapi.Response(description='삭제 성공'),
            404: openapi.Response(description='뉴스 없음')
        },
        tags=['News DB']
    )
    def delete(self, request, news_id):
        from .services.news_crawler import delete_news_from_db

        result = delete_news_from_db(news_id)

        if result['success']:
            return Response(result, status=status.HTTP_200_OK)
        else:
            return Response(result, status=status.HTTP_404_NOT_FOUND)
