"""
#######################
# 네이버 뉴스 검색 서비스
#######################
# 네이버 검색 API를 사용하여 뉴스를 검색합니다.
# JSON 파일로 저장 기능 포함 (비동기 지원)
# PostgreSQL 저장 기능 준비 (주석 처리)
#######################
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
import threading

logger = logging.getLogger(__name__)

# news_data 폴더 경로 설정
NEWS_DATA_DIR = Path(__file__).parent.parent.parent / "news_data"
# summarized_data 폴더 경로 설정
SUMMARIZED_DATA_DIR = Path(__file__).parent.parent.parent / "summarized_data"

# 비동기 저장을 위한 ThreadPoolExecutor
_executor = ThreadPoolExecutor(max_workers=3)

#######################
# Django ORM을 통한 DB 저장
#######################
def save_news_to_db(items: list, source: str = 'naver') -> dict:
    """
    뉴스 데이터를 PostgreSQL DB에 저장 (Django ORM 사용)

    Args:
        items: 뉴스 아이템 리스트
        source: 뉴스 출처 (기본값: 'naver')

    Returns:
        dict: {'success': bool, 'count': int, 'error': str}
    """
    try:
        from api.models import CrawledNews
        from dateutil import parser as date_parser

        saved_count = 0
        for item in items:
            # 발행일 파싱
            published_at = None
            if item.get('pubDate'):
                try:
                    published_at = date_parser.parse(item['pubDate'])
                except:
                    pass

            # 중복 체크 (URL 기준)
            url = item.get('originallink') or item.get('link', '')
            if CrawledNews.objects.filter(url=url).exists():
                logger.info(f"중복 뉴스 스킵: {url[:50]}...")
                continue

            # DB에 저장
            # origin_news: 원문 링크에서 추출한 뉴스 원본 데이터
            origin_news = item.get('origainal_news') or item.get('original_news') or ''
            CrawledNews.objects.create(
                title=item.get('title', ''),
                content=item.get('description', ''),
                origin_news=origin_news,
                url=url,
                source=source,
                published_at=published_at
            )
            saved_count += 1

        logger.info(f"DB 저장 완료: {saved_count}개")
        return {
            'success': True,
            'count': saved_count,
            'error': None
        }
    except Exception as e:
        logger.exception("DB 저장 오류")
        return {
            'success': False,
            'count': 0,
            'error': str(e)
        }


def get_news_from_db(limit: int = 100, offset: int = 0, source: str = None) -> dict:
    """
    DB에서 뉴스 목록 조회

    Args:
        limit: 조회 개수
        offset: 시작 위치
        source: 뉴스 출처 필터 (선택)

    Returns:
        dict: {'success': bool, 'items': list, 'total': int}
    """
    try:
        from api.models import CrawledNews

        queryset = CrawledNews.objects.all().order_by('-created_at')
        if source:
            queryset = queryset.filter(source=source)

        total = queryset.count()
        items = list(queryset[offset:offset + limit].values(
            'id', 'title', 'content', 'origin_news', 'url', 'source', 'published_at', 'created_at'
        ))

        # UUID와 datetime을 문자열로 변환
        for item in items:
            item['id'] = str(item['id'])
            if item['published_at']:
                item['published_at'] = item['published_at'].isoformat()
            if item['created_at']:
                item['created_at'] = item['created_at'].isoformat()

        return {
            'success': True,
            'items': items,
            'total': total,
            'error': None
        }
    except Exception as e:
        logger.exception("DB 조회 오류")
        return {
            'success': False,
            'items': [],
            'total': 0,
            'error': str(e)
        }


def get_news_detail_from_db(news_id: str) -> dict:
    """
    DB에서 뉴스 상세 조회

    Args:
        news_id: 뉴스 UUID

    Returns:
        dict: {'success': bool, 'item': dict}
    """
    try:
        from api.models import CrawledNews

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

        return {
            'success': True,
            'item': item,
            'error': None
        }
    except CrawledNews.DoesNotExist:
        return {
            'success': False,
            'item': None,
            'error': '뉴스를 찾을 수 없습니다.'
        }
    except Exception as e:
        logger.exception("DB 상세 조회 오류")
        return {
            'success': False,
            'item': None,
            'error': str(e)
        }


def delete_news_from_db(news_id: str) -> dict:
    """
    DB에서 뉴스 삭제

    Args:
        news_id: 뉴스 UUID

    Returns:
        dict: {'success': bool, 'message': str}
    """
    try:
        from api.models import CrawledNews

        news = CrawledNews.objects.get(id=news_id)
        news.delete()

        return {
            'success': True,
            'message': '삭제 완료',
            'error': None
        }
    except CrawledNews.DoesNotExist:
        return {
            'success': False,
            'message': None,
            'error': '뉴스를 찾을 수 없습니다.'
        }
    except Exception as e:
        logger.exception("DB 삭제 오류")
        return {
            'success': False,
            'message': None,
            'error': str(e)
        }
#######################


def strip_html(text: str) -> str:
    """HTML 태그 제거 및 특수문자 변환"""
    if not text:
        return ""
    text = re.sub(r"<[^>]+>", "", text)
    text = text.replace("&quot;", '"').replace("&amp;", "&")
    text = text.replace("&lt;", "<").replace("&gt;", ">")
    return text.strip()


class NaverNewsCrawler:
    """네이버 뉴스 검색 API 클라이언트"""

    API_URL = "https://openapi.naver.com/v1/search/news.json"

    def __init__(self):
        self.client_id = os.getenv("NAVER_CLIENT_ID")
        self.client_secret = os.getenv("NAVER_CLIENT_SECRET")

    def is_available(self) -> bool:
        return bool(self.client_id and self.client_secret)

    def search(self, query: str, display: int = 20, start: int = 1, sort: str = "date") -> dict:
        """
        네이버 뉴스 검색

        Args:
            query: 검색 키워드
            display: 결과 개수 (1~100)
            start: 시작 위치 (1~1000)
            sort: 정렬 ('date': 최신순, 'sim': 관련도순)
        """
        if not self.is_available():
            return {
                'success': False,
                'total': 0,
                'items': [],
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
            from .extractor import ArticleExtractor
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

            # 원본 기사 내용 추출
            if fetch_content:
                url = item.get('originallink') or item.get('link')
                if url:
                    save_item["origainal_news"] = self._fetch_original_content(url)

            save_items.append(save_item)
        return save_items

    def _save_to_json_sync(self, save_items: list, query: str) -> dict:
        """JSON 파일로 동기 저장 + DB 저장 (내부 함수)"""
        try:
            NEWS_DATA_DIR.mkdir(parents=True, exist_ok=True)

            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
            safe_query = re.sub(r'[^\w\s-]', '', query).strip().replace(' ', '_')[:30]
            filename = f"{safe_query}_{timestamp}.json"
            file_path = NEWS_DATA_DIR / filename

            with open(file_path, 'w', encoding='utf-8') as f:
                json.dump(save_items, f, ensure_ascii=False, indent=2)

            logger.info(f"뉴스 데이터 JSON 저장 완료: {file_path}")

            # DB에도 저장 (USE_POSTGRES 환경 변수가 true일 때)
            db_result = {'success': False, 'count': 0}
            if os.getenv('USE_POSTGRES', 'false').lower() == 'true':
                db_result = save_news_to_db(save_items, source='naver')
                if db_result['success']:
                    logger.info(f"뉴스 데이터 DB 저장 완료: {db_result['count']}개")
                else:
                    logger.warning(f"뉴스 데이터 DB 저장 실패: {db_result.get('error')}")

            return {
                'success': True,
                'file_path': str(file_path),
                'count': len(save_items),
                'db_saved': db_result['success'],
                'db_count': db_result.get('count', 0),
                'error': None
            }
        except Exception as e:
            logger.exception("JSON 저장 오류")
            return {
                'success': False,
                'file_path': None,
                'count': 0,
                'db_saved': False,
                'db_count': 0,
                'error': str(e)
            }

    #######################
    # PostgreSQL 저장 (주석 처리)
    #######################
    # def _save_to_postgres_sync(self, save_items: list, query: str) -> dict:
    #     """PostgreSQL로 동기 저장 (내부 함수)"""
    #     try:
    #         conn = get_postgres_connection()
    #         cursor = conn.cursor()
    #
    #         inserted_ids = []
    #         for item in save_items:
    #             cursor.execute("""
    #                 INSERT INTO news_articles (query, title, link, pub_date, content, created_at)
    #                 VALUES (%s, %s, %s, %s, %s, %s) RETURNING id
    #             """, (query, item.get('title'), item.get('link'), item.get('pubDate'),
    #                   item.get('original_news'), datetime.now()))
    #             inserted_ids.append(cursor.fetchone()['id'])
    #
    #         conn.commit()
    #         cursor.close()
    #         conn.close()
    #
    #         logger.info(f"PostgreSQL 저장 완료: {len(inserted_ids)}개")
    #
    #         return {
    #             'success': True,
    #             'inserted_ids': inserted_ids,
    #             'count': len(inserted_ids),
    #             'error': None
    #         }
    #     except Exception as e:
    #         logger.exception("PostgreSQL 저장 오류")
    #         return {
    #             'success': False,
    #             'inserted_ids': [],
    #             'count': 0,
    #             'error': str(e)
    #         }
    #######################

    def save_to_json_async(self, items: list, query: str, fetch_content: bool = True) -> dict:
        """
        검색 결과를 JSON 파일로 비동기 저장

        Args:
            items: 검색 결과 아이템 리스트
            query: 검색 키워드
            fetch_content: 원본 기사 내용 추출 여부

        Returns:
            dict: {'success': bool, 'message': str, 'async': True}
        """
        def _async_save():
            save_items = self._prepare_save_items(items, query, fetch_content)
            result = self._save_to_json_sync(save_items, query)
            logger.info(f"비동기 저장 완료: {result}")

        # 백그라운드에서 저장 실행
        _executor.submit(_async_save)

        return {
            'success': True,
            'message': f'{len(items)}개 뉴스 저장이 백그라운드에서 진행 중입니다.',
            'async': True,
            'count': len(items)
        }

    #######################
    # PostgreSQL 비동기 저장 (주석 처리)
    #######################
    # def save_to_postgres_async(self, items: list, query: str, fetch_content: bool = True) -> dict:
    #     """
    #     검색 결과를 PostgreSQL로 비동기 저장
    #     """
    #     def _async_save():
    #         save_items = self._prepare_save_items(items, query, fetch_content)
    #         result = self._save_to_postgres_sync(save_items, query)
    #         logger.info(f"PostgreSQL 비동기 저장 완료: {result}")
    #
    #     _executor.submit(_async_save)
    #
    #     return {
    #         'success': True,
    #         'message': f'{len(items)}개 뉴스가 PostgreSQL에 저장 중입니다.',
    #         'async': True,
    #         'count': len(items)
    #     }
    #######################

    def save_to_json(self, items: list, query: str, fetch_content: bool = True) -> dict:
        """
        검색 결과를 JSON 파일로 저장 (동기 방식 - 하위 호환성)
        """
        save_items = self._prepare_save_items(items, query, fetch_content)
        return self._save_to_json_sync(save_items, query)

    def search_and_save(self, query: str, display: int = 20, start: int = 1,
                        sort: str = "date", fetch_content: bool = True,
                        async_save: bool = False) -> dict:
        """
        뉴스 검색 후 저장

        Args:
            query: 검색 키워드
            display: 결과 개수
            start: 시작 위치
            sort: 정렬 방식
            fetch_content: 원본 기사 내용 추출 여부
            async_save: 비동기 저장 여부

        Returns:
            dict: 검색 결과 + 저장 정보
        """
        # 검색 실행
        result = self.search(query, display, start, sort)

        if not result['success']:
            return result

        # 저장 실행 (동기/비동기)
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

        #######################
        # PostgreSQL 저장 (주석 처리)
        #######################
        # if use_postgres:
        #     if async_save:
        #         save_result = self.save_to_postgres_async(result['items'], query, fetch_content)
        #     else:
        #         save_items = self._prepare_save_items(result['items'], query, fetch_content)
        #         save_result = self._save_to_postgres_sync(save_items, query)
        #     result['saved_to_postgres'] = save_result['success']
        #######################

        return result


#######################
# 저장된 뉴스 조회 클래스
#######################
class SavedNewsReader:
    """저장된 뉴스 데이터 조회"""

    @staticmethod
    def list_json_files() -> list:
        """저장된 JSON 파일 목록 조회"""
        if not NEWS_DATA_DIR.exists():
            return []

        files = []
        for file_path in sorted(NEWS_DATA_DIR.glob("*.json"), reverse=True):
            try:
                stat = file_path.stat()
                files.append({
                    'filename': file_path.name,
                    'path': str(file_path),
                    'size': stat.st_size,
                    'created_at': datetime.fromtimestamp(stat.st_ctime).isoformat(),
                    'modified_at': datetime.fromtimestamp(stat.st_mtime).isoformat()
                })
            except Exception as e:
                logger.warning(f"파일 정보 읽기 실패: {file_path}, {e}")

        return files

    @staticmethod
    def read_json_file(filename: str) -> dict:
        """특정 JSON 파일 내용 읽기"""
        file_path = NEWS_DATA_DIR / filename

        if not file_path.exists():
            return {'success': False, 'error': '파일을 찾을 수 없습니다.', 'items': []}

        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                items = json.load(f)

            return {
                'success': True,
                'filename': filename,
                'count': len(items),
                'items': items,
                'error': None
            }
        except Exception as e:
            logger.exception(f"JSON 파일 읽기 오류: {filename}")
            return {'success': False, 'error': str(e), 'items': []}

    @staticmethod
    def delete_json_file(filename: str) -> dict:
        """JSON 파일 삭제"""
        file_path = NEWS_DATA_DIR / filename

        if not file_path.exists():
            return {'success': False, 'error': '파일을 찾을 수 없습니다.'}

        try:
            file_path.unlink()
            logger.info(f"파일 삭제 완료: {filename}")
            return {'success': True, 'message': f'{filename} 삭제 완료'}
        except Exception as e:
            logger.exception(f"파일 삭제 오류: {filename}")
            return {'success': False, 'error': str(e)}

    #######################
    # PostgreSQL 조회 (주석 처리)
    #######################
    # @staticmethod
    # def list_from_postgres(query: str = None, limit: int = 100, offset: int = 0) -> dict:
    #     """PostgreSQL에서 저장된 뉴스 목록 조회"""
    #     try:
    #         conn = get_postgres_connection()
    #         cursor = conn.cursor()
    #
    #         # 필터 조건
    #         if query:
    #             cursor.execute("""
    #                 SELECT * FROM news_articles WHERE query ILIKE %s
    #                 ORDER BY created_at DESC LIMIT %s OFFSET %s
    #             """, (f'%{query}%', limit, offset))
    #         else:
    #             cursor.execute("""
    #                 SELECT * FROM news_articles ORDER BY created_at DESC LIMIT %s OFFSET %s
    #             """, (limit, offset))
    #
    #         items = cursor.fetchall()
    #
    #         # 전체 개수
    #         cursor.execute("SELECT COUNT(*) as count FROM news_articles")
    #         total = cursor.fetchone()['count']
    #
    #         cursor.close()
    #         conn.close()
    #
    #         return {
    #             'success': True,
    #             'total': total,
    #             'count': len(items),
    #             'items': items,
    #             'error': None
    #         }
    #     except Exception as e:
    #         logger.exception("PostgreSQL 조회 오류")
    #         return {'success': False, 'total': 0, 'count': 0, 'items': [], 'error': str(e)}
    #
    # @staticmethod
    # def get_from_postgres(record_id: str) -> dict:
    #     """PostgreSQL에서 특정 레코드 조회"""
    #     try:
    #         conn = get_postgres_connection()
    #         cursor = conn.cursor()
    #
    #         cursor.execute("SELECT * FROM news_articles WHERE id = %s", (record_id,))
    #         item = cursor.fetchone()
    #
    #         cursor.close()
    #         conn.close()
    #
    #         if not item:
    #             return {'success': False, 'error': '레코드를 찾을 수 없습니다.', 'item': None}
    #
    #         return {'success': True, 'item': dict(item), 'error': None}
    #     except Exception as e:
    #         logger.exception("PostgreSQL 레코드 조회 오류")
    #         return {'success': False, 'error': str(e), 'item': None}
    #
    # @staticmethod
    # def delete_from_postgres(record_id: str) -> dict:
    #     """PostgreSQL에서 레코드 삭제"""
    #     try:
    #         conn = get_postgres_connection()
    #         cursor = conn.cursor()
    #
    #         cursor.execute("DELETE FROM news_articles WHERE id = %s", (record_id,))
    #         deleted = cursor.rowcount
    #         conn.commit()
    #
    #         cursor.close()
    #         conn.close()
    #
    #         if deleted > 0:
    #             return {'success': True, 'message': '삭제 완료'}
    #         else:
    #             return {'success': False, 'error': '레코드를 찾을 수 없습니다.'}
    #     except Exception as e:
    #         logger.exception("PostgreSQL 삭제 오류")
    #         return {'success': False, 'error': str(e)}
    #######################


#######################
# 요약된 뉴스 관리 클래스
#######################
class SummarizedNewsManager:
    """요약된 뉴스 데이터 저장/조회"""

    @staticmethod
    def save_summarized_news(items: list, method: str = 'rule') -> dict:
        """
        요약 결과를 JSON 파일로 저장

        Args:
            items: 요약된 뉴스 아이템 리스트
            method: 요약 방식 ('rule' 또는 'ai')

        Returns:
            dict: {'success': bool, 'filename': str, 'error': str}
        """
        try:
            SUMMARIZED_DATA_DIR.mkdir(parents=True, exist_ok=True)

            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
            filename = f"summarized_{method}_{timestamp}.json"
            file_path = SUMMARIZED_DATA_DIR / filename

            save_data = {
                'method': method,
                'created_at': datetime.now().isoformat(),
                'count': len(items),
                'items': items
            }

            with open(file_path, 'w', encoding='utf-8') as f:
                json.dump(save_data, f, ensure_ascii=False, indent=2)

            logger.info(f"요약 데이터 저장 완료: {file_path}")

            return {
                'success': True,
                'filename': filename,
                'file_path': str(file_path),
                'count': len(items),
                'error': None
            }
        except Exception as e:
            logger.exception("요약 데이터 저장 오류")
            return {
                'success': False,
                'filename': None,
                'file_path': None,
                'count': 0,
                'error': str(e)
            }

    @staticmethod
    def list_summarized_files() -> list:
        """요약된 뉴스 파일 목록 조회"""
        if not SUMMARIZED_DATA_DIR.exists():
            return []

        files = []
        for file_path in sorted(SUMMARIZED_DATA_DIR.glob("*.json"), reverse=True):
            try:
                stat = file_path.stat()

                # 파일 내용에서 메타데이터 추출
                with open(file_path, 'r', encoding='utf-8') as f:
                    data = json.load(f)

                files.append({
                    'filename': file_path.name,
                    'path': str(file_path),
                    'size': stat.st_size,
                    'method': data.get('method', 'unknown'),
                    'count': data.get('count', 0),
                    'created_at': data.get('created_at') or datetime.fromtimestamp(stat.st_ctime).isoformat()
                })
            except Exception as e:
                logger.warning(f"파일 정보 읽기 실패: {file_path}, {e}")

        return files

    @staticmethod
    def read_summarized_file(filename: str) -> dict:
        """특정 요약 파일 내용 읽기"""
        file_path = SUMMARIZED_DATA_DIR / filename

        if not file_path.exists():
            return {'success': False, 'error': '파일을 찾을 수 없습니다.', 'items': []}

        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                data = json.load(f)

            return {
                'success': True,
                'filename': filename,
                'method': data.get('method', 'unknown'),
                'created_at': data.get('created_at'),
                'count': data.get('count', len(data.get('items', []))),
                'items': data.get('items', []),
                'error': None
            }
        except Exception as e:
            logger.exception(f"요약 파일 읽기 오류: {filename}")
            return {'success': False, 'error': str(e), 'items': []}

    @staticmethod
    def delete_summarized_file(filename: str) -> dict:
        """요약 파일 삭제"""
        file_path = SUMMARIZED_DATA_DIR / filename

        if not file_path.exists():
            return {'success': False, 'error': '파일을 찾을 수 없습니다.'}

        try:
            file_path.unlink()
            logger.info(f"요약 파일 삭제 완료: {filename}")
            return {'success': True, 'message': f'{filename} 삭제 완료'}
        except Exception as e:
            logger.exception(f"요약 파일 삭제 오류: {filename}")
            return {'success': False, 'error': str(e)}

    #######################
    # PostgreSQL 저장 (주석 처리)
    #######################
    # @staticmethod
    # def save_to_postgres(items: list, method: str = 'rule') -> dict:
    #     """요약 결과를 PostgreSQL에 저장"""
    #     try:
    #         conn = get_postgres_connection()
    #         cursor = conn.cursor()
    #
    #         cursor.execute("""
    #             INSERT INTO summarized_news (method, created_at, count, items)
    #             VALUES (%s, %s, %s, %s) RETURNING id
    #         """, (method, datetime.now(), len(items), json.dumps(items, ensure_ascii=False)))
    #
    #         record_id = cursor.fetchone()['id']
    #         conn.commit()
    #
    #         cursor.close()
    #         conn.close()
    #
    #         logger.info(f"PostgreSQL 요약 저장 완료: {record_id}")
    #
    #         return {
    #             'success': True,
    #             'record_id': record_id,
    #             'count': len(items),
    #             'error': None
    #         }
    #     except Exception as e:
    #         logger.exception("PostgreSQL 요약 저장 오류")
    #         return {'success': False, 'record_id': None, 'count': 0, 'error': str(e)}
    #
    # @staticmethod
    # def list_from_postgres(limit: int = 50, offset: int = 0) -> dict:
    #     """PostgreSQL에서 요약 목록 조회"""
    #     try:
    #         conn = get_postgres_connection()
    #         cursor = conn.cursor()
    #
    #         cursor.execute("""
    #             SELECT * FROM summarized_news ORDER BY created_at DESC LIMIT %s OFFSET %s
    #         """, (limit, offset))
    #         items = cursor.fetchall()
    #
    #         cursor.execute("SELECT COUNT(*) as count FROM summarized_news")
    #         total = cursor.fetchone()['count']
    #
    #         cursor.close()
    #         conn.close()
    #
    #         return {'success': True, 'total': total, 'count': len(items), 'items': items, 'error': None}
    #     except Exception as e:
    #         logger.exception("PostgreSQL 요약 목록 조회 오류")
    #         return {'success': False, 'total': 0, 'count': 0, 'items': [], 'error': str(e)}
    #######################
