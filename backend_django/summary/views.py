"""
요약 도메인 API Views
"""
import logging
import time
import uuid
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from django.conf import settings
from drf_yasg.utils import swagger_auto_schema
from drf_yasg import openapi

from .serializers import SummarizeRequestSerializer, SummarizeResponseSerializer
from .services.ai_summarizer import AISummarizer, check_ai_available
from .services.rule_summarizer import ArticleSummarizer

logger = logging.getLogger(__name__)


class SummarizeView(APIView):
    """텍스트/URL 요약 - POST /api/summarize"""

    @swagger_auto_schema(
        operation_id='summarize',
        operation_summary='텍스트/URL 요약',
        operation_description='뉴스 기사 URL 또는 텍스트를 입력받아 요약을 생성합니다.',
        request_body=SummarizeRequestSerializer,
        responses={
            200: openapi.Response(description='요약 성공'),
            400: openapi.Response(description='잘못된 요청'),
            422: openapi.Response(description='기사 추출 실패'),
            500: openapi.Response(description='서버 오류')
        },
        tags=['Summarize']
    )
    def post(self, request):
        start_time = time.time()
        request_id = uuid.uuid4()

        logger.info(f"[{request_id}] 요약 요청 수신")

        serializer = SummarizeRequestSerializer(data=request.data)
        if not serializer.is_valid():
            logger.warning(f"[{request_id}] 잘못된 요청: {serializer.errors}")
            return Response(
                {'error': 'Validation failed', 'details': serializer.errors},
                status=status.HTTP_400_BAD_REQUEST
            )

        validated_data = serializer.validated_data
        input_type = validated_data['input_type']
        language = validated_data['language']
        max_length = validated_data['max_length']
        min_length = validated_data['min_length']
        summarize_method = validated_data.get('summarize_method', 'rule')

        if summarize_method == 'ai' and not check_ai_available():
            logger.warning(f"[{request_id}] AI 사용 불가, 규칙 기반으로 전환")
            summarize_method = 'rule'

        try:
            if input_type == 'url':
                url = validated_data['url']
                logger.info(f"[{request_id}] URL에서 기사 추출: {url}")

                from news.services.extractor import ArticleExtractor
                extractor = ArticleExtractor()
                extraction_result = extractor.extract(url)

                if not extraction_result['success']:
                    logger.error(f"[{request_id}] 추출 실패: {extraction_result['error']}")
                    return Response(
                        {'error': 'Article extraction failed', 'details': extraction_result['error']},
                        status=status.HTTP_422_UNPROCESSABLE_ENTITY
                    )

                article_text = extraction_result['text']
                article_title = extraction_result['title']
                article_source = extraction_result['source']
                article_published_at = extraction_result['published_at']
            else:
                article_text = validated_data['text']
                article_title = None
                article_source = None
                article_published_at = None

            logger.info(f"[{request_id}] {summarize_method} 방식으로 요약 생성")

            if summarize_method == 'ai':
                summarizer = AISummarizer(language=language)
            else:
                summarizer = ArticleSummarizer(language=language)

            summary_result = summarizer.summarize(
                text=article_text, max_length=max_length, min_length=min_length
            )

            elapsed_ms = int((time.time() - start_time) * 1000)

            response_data = {
                'request_id': str(request_id),
                'input_type': input_type,
                'summarize_method': summarize_method,
                'title': article_title,
                'source': article_source,
                'published_at': article_published_at,
                'original_text': article_text,
                'summary': summary_result['summary'],
                'bullets': summary_result['bullets'],
                'keywords': summary_result['keywords'],
                'elapsed_ms': elapsed_ms
            }

            response_serializer = SummarizeResponseSerializer(data=response_data)
            if response_serializer.is_valid():
                logger.info(f"[{request_id}] 요약 완료 ({elapsed_ms}ms)")
                return Response(response_serializer.validated_data, status=status.HTTP_200_OK)
            else:
                logger.error(f"[{request_id}] 응답 검증 실패: {response_serializer.errors}")
                return Response(
                    {'error': 'Internal error', 'details': 'Response formatting failed'},
                    status=status.HTTP_500_INTERNAL_SERVER_ERROR
                )

        except Exception as e:
            elapsed_ms = int((time.time() - start_time) * 1000)
            logger.exception(f"[{request_id}] 예기치 않은 오류 ({elapsed_ms}ms)")

            # 보안: DEBUG 모드에서만 에러 상세 노출
            error_details = str(e) if settings.DEBUG else 'An unexpected error occurred'

            return Response(
                {'error': 'Internal server error', 'details': error_details, 'request_id': str(request_id)},
                status=status.HTTP_500_INTERNAL_SERVER_ERROR
            )


class BatchSummarizeView(APIView):
    """선택된 뉴스 배치 요약 - POST /api/news/batch-summarize"""

    @swagger_auto_schema(
        operation_id='batch_summarize',
        operation_summary='선택된 뉴스 배치 요약',
        operation_description='저장된 뉴스에서 선택된 아이템들을 요약합니다.',
        request_body=openapi.Schema(
            type=openapi.TYPE_OBJECT,
            required=['items', 'method'],
            properties={
                'items': openapi.Schema(type=openapi.TYPE_ARRAY, items=openapi.Schema(type=openapi.TYPE_OBJECT)),
                'method': openapi.Schema(type=openapi.TYPE_STRING, enum=['rule', 'ai']),
                'max_length': openapi.Schema(type=openapi.TYPE_INTEGER, default=300),
                'min_length': openapi.Schema(type=openapi.TYPE_INTEGER, default=50),
            }
        ),
        responses={200: openapi.Response(description='요약 성공')},
        tags=['Summarize']
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

        if method == 'ai' and not check_ai_available():
            logger.warning("AI 사용 불가, 규칙 기반으로 전환")
            method = 'rule'

        if method == 'ai':
            summarizer = AISummarizer(language='ko')
        else:
            summarizer = ArticleSummarizer(language='ko')

        summarized_items = []
        for item in items:
            text = item.get('origainal_news', '')
            if not text or len(text.strip()) < 50:
                summarized_items.append({
                    **item, 'summary': '', 'bullets': [], 'keywords': [],
                    'summarized': False, 'error': '텍스트가 너무 짧습니다.'
                })
                continue

            try:
                result = summarizer.summarize(text=text, max_length=max_length, min_length=min_length)
                summarized_items.append({
                    'title': item.get('title', ''),
                    'originallink': item.get('originallink', ''),
                    'pubDate': item.get('pubDate', ''),
                    'original_text': text[:500] + '...' if len(text) > 500 else text,
                    'summary': result['summary'],
                    'bullets': result['bullets'],
                    'keywords': result['keywords'],
                    'summarized': True, 'error': None
                })
            except Exception as e:
                logger.exception(f"요약 실패: {item.get('title', '')}")
                summarized_items.append({
                    **item, 'summary': '', 'bullets': [], 'keywords': [],
                    'summarized': False, 'error': str(e) if settings.DEBUG else '요약 처리 중 오류 발생'
                })

        from news.services.storage import SummarizedNewsManager
        save_result = SummarizedNewsManager.save_summarized_news(summarized_items, method)

        if save_result['success']:
            return Response({
                'success': True, 'filename': save_result['filename'],
                'count': save_result['count'], 'method': method, 'items': summarized_items
            }, status=status.HTTP_200_OK)
        else:
            return Response({
                'success': False, 'error': save_result['error'], 'items': summarized_items
            }, status=status.HTTP_500_INTERNAL_SERVER_ERROR)


class SummarizedNewsListView(APIView):
    """요약된 뉴스 파일 목록 - GET /api/news/summarized"""

    @swagger_auto_schema(
        operation_id='summarized_news_list',
        operation_summary='요약된 뉴스 파일 목록',
        responses={200: openapi.Response(description='조회 성공')},
        tags=['Summarize']
    )
    def get(self, request):
        from news.services.storage import SummarizedNewsManager

        files = SummarizedNewsManager.list_summarized_files()
        return Response({
            'success': True, 'count': len(files), 'files': files
        }, status=status.HTTP_200_OK)


class SummarizedNewsDetailView(APIView):
    """요약된 뉴스 파일 상세 - GET/DELETE /api/news/summarized/<filename>"""

    @swagger_auto_schema(
        operation_id='summarized_news_detail',
        operation_summary='요약된 뉴스 파일 내용',
        responses={200: openapi.Response(description='조회 성공'), 404: openapi.Response(description='파일 없음')},
        tags=['Summarize']
    )
    def get(self, request, filename):
        from news.services.storage import SummarizedNewsManager

        result = SummarizedNewsManager.read_summarized_file(filename)
        if result['success']:
            return Response(result, status=status.HTTP_200_OK)
        else:
            return Response(result, status=status.HTTP_404_NOT_FOUND)

    @swagger_auto_schema(
        operation_id='summarized_news_delete',
        operation_summary='요약된 뉴스 파일 삭제',
        responses={200: openapi.Response(description='삭제 성공'), 404: openapi.Response(description='파일 없음')},
        tags=['Summarize']
    )
    def delete(self, request, filename):
        from news.services.storage import SummarizedNewsManager

        result = SummarizedNewsManager.delete_summarized_file(filename)
        if result['success']:
            return Response(result, status=status.HTTP_200_OK)
        else:
            return Response(result, status=status.HTTP_404_NOT_FOUND)
