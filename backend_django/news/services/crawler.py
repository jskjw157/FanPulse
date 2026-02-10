"""
네이버 뉴스 검색 서비스

네이버 검색 API를 사용하여 뉴스를 검색합니다.
JSON 파일 저장 + PostgreSQL DB 저장 지원
"""
import os
import re
import logging
import urllib.request
import urllib.parse
import json
from datetime import datetime
from pathlib import Path
from concurrent.futures import ThreadPoolExecutor

from common.sanitizers import strip_html

logger = logging.getLogger(__name__)

# 데이터 폴더 경로 설정
NEWS_DATA_DIR = Path(__file__).parent.parent.parent / "news_data"
SUMMARIZED_DATA_DIR = Path(__file__).parent.parent.parent / "summarized_data"

# 비동기 저장을 위한 ThreadPoolExecutor
_executor = ThreadPoolExecutor(max_workers=3)


# =============================================
# Django ORM을 통한 DB 저장
# =============================================
def save_news_to_db(items: list, source: str = 'naver') -> dict:
    """뉴스 데이터를 PostgreSQL DB에 저장 (Django ORM 사용)"""
    try:
        from news.models import CrawledNews
        from dateutil import parser as date_parser

        saved_count = 0
        for item in items:
            published_at = None
            if item.get('pubDate'):
                try:
                    published_at = date_parser.parse(item['pubDate'])
                except Exception:
                    pass

            url = item.get('originallink') or item.get('link', '')
            origin_news = item.get('origainal_news') or item.get('original_news') or ''

            # Race Condition 방지: get_or_create 사용
            _, created = CrawledNews.objects.get_or_create(
                url=url,
                defaults={
                    'title': item.get('title', ''),
                    'content': item.get('description', ''),
                    'origin_news': origin_news,
                    'source': source,
                    'published_at': published_at
                }
            )
            if created:
                saved_count += 1
            else:
                logger.info(f"중복 뉴스 스킵: {url[:50]}...")

        logger.info(f"DB 저장 완료: {saved_count}개")
        return {'success': True, 'count': saved_count, 'error': None}
    except Exception as e:
        logger.exception("DB 저장 오류")
        return {'success': False, 'count': 0, 'error': str(e)}


def get_news_from_db(limit: int = 100, offset: int = 0, source: str = None) -> dict:
    """DB에서 뉴스 목록 조회"""
    try:
        from news.models import CrawledNews

        queryset = CrawledNews.objects.all().order_by('-created_at')
        if source:
            queryset = queryset.filter(source=source)

        total = queryset.count()
        items = list(queryset[offset:offset + limit].values(
            'id', 'title', 'content', 'origin_news', 'url', 'source', 'published_at', 'created_at'
        ))

        for item in items:
            item['id'] = str(item['id'])
            if item['published_at']:
                item['published_at'] = item['published_at'].isoformat()
            if item['created_at']:
                item['created_at'] = item['created_at'].isoformat()

        return {'success': True, 'items': items, 'total': total, 'error': None}
    except Exception as e:
        logger.exception("DB 조회 오류")
        return {'success': False, 'items': [], 'total': 0, 'error': str(e)}


def get_news_detail_from_db(news_id: str) -> dict:
    """DB에서 뉴스 상세 조회"""
    try:
        from news.models import CrawledNews

        news = CrawledNews.objects.get(id=news_id)
        item = {
            'id': str(news.id),
            'title': news.title,
            'content': news.content,
            'origin_news': news.origin_news,
            'url': news.url,
            'source': news.source,
            'published_at': news.published_at.isoformat() if news.published_at else None,
            'created_at': news.created_at.isoformat() if news.created_at else None,
        }
        return {'success': True, 'item': item, 'error': None}
    except CrawledNews.DoesNotExist:
        return {'success': False, 'item': None, 'error': '뉴스를 찾을 수 없습니다.'}
    except Exception as e:
        logger.exception("DB 상세 조회 오류")
        return {'success': False, 'item': None, 'error': str(e)}


def delete_news_from_db(news_id: str) -> dict:
    """DB에서 뉴스 삭제"""
    try:
        from news.models import CrawledNews

        news = CrawledNews.objects.get(id=news_id)
        news.delete()
        return {'success': True, 'message': '삭제 완료', 'error': None}
    except CrawledNews.DoesNotExist:
        return {'success': False, 'message': None, 'error': '뉴스를 찾을 수 없습니다.'}
    except Exception as e:
        logger.exception("DB 삭제 오류")
        return {'success': False, 'message': None, 'error': str(e)}


# =============================================
# 네이버 뉴스 검색 클래스
# =============================================
class NaverNewsCrawler:
    """네이버 뉴스 검색 API 클라이언트"""

    API_URL = "https://openapi.naver.com/v1/search/news.json"

    def __init__(self):
        self.client_id = os.getenv("NAVER_CLIENT_ID")
        self.client_secret = os.getenv("NAVER_CLIENT_SECRET")

    def is_available(self) -> bool:
        return bool(self.client_id and self.client_secret)

    def search(self, query: str, display: int = 20, start: int = 1, sort: str = "date") -> dict:
        """네이버 뉴스 검색"""
        if not self.is_available():
            return {
                'success': False, 'total': 0, 'items': [],
                'error': 'NAVER API 키가 설정되지 않았습니다.'
            }

        try:
            encoded_query = urllib.parse.quote(query)
            url = f"{self.API_URL}?query={encoded_query}&display={min(display, 100)}&start={min(start, 1000)}&sort={sort}"

            request = urllib.request.Request(url)
            request.add_header("X-Naver-Client-Id", self.client_id)
            request.add_header("X-Naver-Client-Secret", self.client_secret)

            with urllib.request.urlopen(request, timeout=10) as response:
                data = json.loads(response.read().decode("utf-8"))

            items = []
            for item in data.get("items", []):
                items.append({
                    'title': strip_html(item.get("title", "")),
                    'description': strip_html(item.get("description", "")),
                    'originallink': item.get("originallink", ""),
                    'link': item.get("link", ""),
                    'pubDate': item.get("pubDate", ""),
                    'pubDateFormatted': self._format_date(item.get("pubDate", ""))
                })

            return {
                'success': True,
                'total': data.get("total", 0),
                'start': data.get("start", 1),
                'display': data.get("display", len(items)),
                'items': items,
                'error': None
            }

        except Exception as e:
            logger.exception("뉴스 검색 오류")
            return {'success': False, 'total': 0, 'items': [], 'error': str(e)}

    def _format_date(self, pub_date: str) -> str:
        if not pub_date:
            return ""
        try:
            from email.utils import parsedate_to_datetime
            return parsedate_to_datetime(pub_date).strftime("%Y-%m-%d %H:%M")
        except Exception:
            return pub_date

    def _fetch_original_content(self, url: str) -> str:
        """원본 기사 내용 추출"""
        try:
            from news.services.extractor import ArticleExtractor
            extractor = ArticleExtractor()
            result = extractor.extract(url)
            if result['success']:
                return result['text']
            return ""
        except Exception as e:
            logger.warning(f"원본 기사 추출 실패: {url}, {e}")
            return ""

    def _prepare_save_items(self, items: list, query: str, fetch_content: bool = True) -> list:
        """저장할 아이템 데이터 준비"""
        save_items = []
        for item in items:
            save_item = {
                "title": item.get('title', ''),
                "originallink": item.get('originallink', ''),
                "link": item.get('link', ''),
                "description": item.get('description', ''),
                "pubDate": item.get('pubDate', ''),
                "origainal_news": "",
                "query": query,
                "saved_at": datetime.now().isoformat()
            }

            if fetch_content:
                url = item.get('originallink') or item.get('link')
                if url:
                    save_item["origainal_news"] = self._fetch_original_content(url)

            save_items.append(save_item)
        return save_items

    def _save_to_json_sync(self, save_items: list, query: str) -> dict:
        """JSON 파일로 동기 저장 + DB 저장"""
        try:
            NEWS_DATA_DIR.mkdir(parents=True, exist_ok=True)

            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
            safe_query = re.sub(r'[^\w\s-]', '', query).strip().replace(' ', '_')[:30]
            filename = f"{safe_query}_{timestamp}.json"
            file_path = NEWS_DATA_DIR / filename

            with open(file_path, 'w', encoding='utf-8') as f:
                json.dump(save_items, f, ensure_ascii=False, indent=2)

            logger.info(f"뉴스 데이터 JSON 저장 완료: {file_path}")

            # DB에도 저장 (USE_POSTGRES=true일 때)
            db_result = {'success': False, 'count': 0}
            if os.getenv('USE_POSTGRES', 'false').lower() == 'true':
                db_result = save_news_to_db(save_items, source='naver')
                if db_result['success']:
                    logger.info(f"뉴스 데이터 DB 저장 완료: {db_result['count']}개")
                else:
                    logger.warning(f"뉴스 데이터 DB 저장 실패: {db_result.get('error')}")

            return {
                'success': True, 'file_path': str(file_path),
                'count': len(save_items), 'db_saved': db_result['success'],
                'db_count': db_result.get('count', 0), 'error': None
            }
        except Exception as e:
            logger.exception("JSON 저장 오류")
            return {
                'success': False, 'file_path': None, 'count': 0,
                'db_saved': False, 'db_count': 0, 'error': str(e)
            }

    def save_to_json_async(self, items: list, query: str, fetch_content: bool = True) -> dict:
        """검색 결과를 JSON 파일로 비동기 저장"""
        def _async_save():
            save_items = self._prepare_save_items(items, query, fetch_content)
            result = self._save_to_json_sync(save_items, query)
            logger.info(f"비동기 저장 완료: {result}")

        _executor.submit(_async_save)

        return {
            'success': True,
            'message': f'{len(items)}개 뉴스 저장이 백그라운드에서 진행 중입니다.',
            'async': True,
            'count': len(items)
        }

    def save_to_json(self, items: list, query: str, fetch_content: bool = True) -> dict:
        """검색 결과를 JSON 파일로 저장 (동기)"""
        save_items = self._prepare_save_items(items, query, fetch_content)
        return self._save_to_json_sync(save_items, query)

    def search_and_save(self, query: str, display: int = 20, start: int = 1,
                        sort: str = "date", fetch_content: bool = True,
                        async_save: bool = False) -> dict:
        """뉴스 검색 후 저장"""
        result = self.search(query, display, start, sort)
        if not result['success']:
            return result

        if async_save:
            save_result = self.save_to_json_async(result['items'], query, fetch_content)
            result['saved'] = True
            result['saved_async'] = True
            result['saved_message'] = save_result.get('message')
            result['saved_count'] = save_result.get('count', 0)
        else:
            save_result = self.save_to_json(result['items'], query, fetch_content)
            result['saved'] = save_result['success']
            result['saved_file'] = save_result.get('file_path')
            result['saved_count'] = save_result.get('count', 0)

        return result
