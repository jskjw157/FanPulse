"""
뉴스 도메인 API Views
"""
import logging
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from django.utils.decorators import method_decorator
from django.views.decorators.cache import never_cache
from drf_yasg.utils import swagger_auto_schema
from drf_yasg import openapi

logger = logging.getLogger(__name__)


class HealthCheckView(APIView):
    """서버 상태 확인 - GET /api/health"""

    @swagger_auto_schema(
        operation_id='health_check',
        operation_summary='서버 상태 확인',
        operation_description='서버가 정상적으로 동작하는지 확인합니다.',
        responses={200: openapi.Response(description='서버 정상')},
        tags=['Health']
    )
    @method_decorator(never_cache)
    def get(self, request):
        return Response({'status': 'ok'}, status=status.HTTP_200_OK)


class NewsSearchView(APIView):
    """네이버 뉴스 검색 - GET /api/news/search"""

    @swagger_auto_schema(
        operation_id='news_search',
        operation_summary='뉴스 검색',
        operation_description='네이버 뉴스 API를 통해 뉴스를 검색합니다.',
        manual_parameters=[
            openapi.Parameter('query', openapi.IN_QUERY, description='검색 키워드', type=openapi.TYPE_STRING, required=True),
            openapi.Parameter('display', openapi.IN_QUERY, description='결과 개수 (1~100)', type=openapi.TYPE_INTEGER, default=20),
            openapi.Parameter('start', openapi.IN_QUERY, description='시작 위치 (1~1000)', type=openapi.TYPE_INTEGER, default=1),
            openapi.Parameter('sort', openapi.IN_QUERY, description='정렬 (date/sim)', type=openapi.TYPE_STRING, default='date'),
            openapi.Parameter('save', openapi.IN_QUERY, description='JSON 저장 여부', type=openapi.TYPE_BOOLEAN, default=False),
            openapi.Parameter('fetch_content', openapi.IN_QUERY, description='원본 기사 내용 포함 여부', type=openapi.TYPE_BOOLEAN, default=True),
            openapi.Parameter('async_save', openapi.IN_QUERY, description='비동기 저장 여부', type=openapi.TYPE_BOOLEAN, default=True),
        ],
        responses={200: openapi.Response(description='검색 성공')},
        tags=['News']
    )
    def get(self, request):
        from news.services.crawler import NaverNewsCrawler

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

        save = request.query_params.get('save', 'false').lower() == 'true'
        fetch_content = request.query_params.get('fetch_content', 'true').lower() == 'true'
        async_save = request.query_params.get('async_save', 'true').lower() == 'true'

        crawler = NaverNewsCrawler()

        if save:
            result = crawler.search_and_save(
                query=query, display=display, start=start,
                sort=sort, fetch_content=fetch_content, async_save=async_save
            )
        else:
            result = crawler.search(query=query, display=display, start=start, sort=sort)

        if result['success']:
            return Response(result, status=status.HTTP_200_OK)
        else:
            return Response(result, status=status.HTTP_503_SERVICE_UNAVAILABLE)


class SavedNewsListView(APIView):
    """저장된 뉴스 파일 목록 - GET /api/news/saved"""

    @swagger_auto_schema(
        operation_id='saved_news_list',
        operation_summary='저장된 뉴스 파일 목록',
        responses={200: openapi.Response(description='조회 성공')},
        tags=['News']
    )
    def get(self, request):
        from news.services.storage import SavedNewsReader

        files = SavedNewsReader.list_json_files()
        return Response({
            'success': True, 'count': len(files), 'files': files
        }, status=status.HTTP_200_OK)


class SavedNewsDetailView(APIView):
    """저장된 뉴스 파일 상세 - GET/DELETE /api/news/saved/<filename>"""

    @swagger_auto_schema(
        operation_id='saved_news_detail',
        operation_summary='저장된 뉴스 파일 내용',
        responses={200: openapi.Response(description='조회 성공'), 404: openapi.Response(description='파일 없음')},
        tags=['News']
    )
    def get(self, request, filename):
        from news.services.storage import SavedNewsReader

        result = SavedNewsReader.read_json_file(filename)
        if result['success']:
            return Response(result, status=status.HTTP_200_OK)
        else:
            return Response(result, status=status.HTTP_404_NOT_FOUND)

    @swagger_auto_schema(
        operation_id='saved_news_delete',
        operation_summary='저장된 뉴스 파일 삭제',
        responses={200: openapi.Response(description='삭제 성공'), 404: openapi.Response(description='파일 없음')},
        tags=['News']
    )
    def delete(self, request, filename):
        from news.services.storage import SavedNewsReader

        result = SavedNewsReader.delete_json_file(filename)
        if result['success']:
            return Response(result, status=status.HTTP_200_OK)
        else:
            return Response(result, status=status.HTTP_404_NOT_FOUND)


class DBNewsListView(APIView):
    """DB 뉴스 목록 - GET /api/news/db"""

    @swagger_auto_schema(
        operation_id='db_news_list',
        operation_summary='DB 뉴스 목록',
        operation_description='PostgreSQL DB에 저장된 뉴스 목록을 조회합니다.',
        manual_parameters=[
            openapi.Parameter('limit', openapi.IN_QUERY, description='조회 개수', type=openapi.TYPE_INTEGER, default=100),
            openapi.Parameter('offset', openapi.IN_QUERY, description='시작 위치', type=openapi.TYPE_INTEGER, default=0),
            openapi.Parameter('source', openapi.IN_QUERY, description='뉴스 출처 필터', type=openapi.TYPE_STRING),
        ],
        responses={200: openapi.Response(description='조회 성공')},
        tags=['News DB']
    )
    def get(self, request):
        from news.services.crawler import get_news_from_db

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
    """DB 뉴스 상세 - GET/DELETE /api/news/db/<news_id>"""

    @swagger_auto_schema(
        operation_id='db_news_detail',
        operation_summary='DB 뉴스 상세',
        responses={200: openapi.Response(description='조회 성공'), 404: openapi.Response(description='뉴스 없음')},
        tags=['News DB']
    )
    def get(self, request, news_id):
        from news.services.crawler import get_news_detail_from_db

        result = get_news_detail_from_db(news_id)
        if result['success']:
            return Response(result, status=status.HTTP_200_OK)
        else:
            return Response(result, status=status.HTTP_404_NOT_FOUND)

    @swagger_auto_schema(
        operation_id='db_news_delete',
        operation_summary='DB 뉴스 삭제',
        responses={200: openapi.Response(description='삭제 성공'), 404: openapi.Response(description='뉴스 없음')},
        tags=['News DB']
    )
    def delete(self, request, news_id):
        from news.services.crawler import delete_news_from_db

        result = delete_news_from_db(news_id)
        if result['success']:
            return Response(result, status=status.HTTP_200_OK)
        else:
            return Response(result, status=status.HTTP_404_NOT_FOUND)
