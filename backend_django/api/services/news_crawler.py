"""
#######################
# 네이버 뉴스 검색 서비스
#######################
# 네이버 검색 API를 사용하여 뉴스를 검색합니다.
# JSON 파일로 저장 기능 포함 (비동기 지원)
# MongoDB 저장 기능 준비 (주석 처리)
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
# MongoDB 설정 (주석 처리)
#######################
# MongoDB를 사용하려면 아래 주석을 해제하고 pymongo를 설치하세요
# pip install pymongo
#
# from pymongo import MongoClient
#
# MONGO_URI = os.getenv("MONGO_URI", "mongodb://localhost:27017")
# MONGO_DB = os.getenv("MONGO_DB", "fanpulse")
# MONGO_COLLECTION = "news_articles"
#
# def get_mongo_client():
#     """MongoDB 클라이언트 반환"""
#     return MongoClient(MONGO_URI)
#
# def get_mongo_collection():
#     """MongoDB 컬렉션 반환"""
#     client = get_mongo_client()
#     db = client[MONGO_DB]
#     return db[MONGO_COLLECTION]
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
        """JSON 파일로 동기 저장 (내부 함수)"""
        try:
            NEWS_DATA_DIR.mkdir(parents=True, exist_ok=True)

            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
            safe_query = re.sub(r'[^\w\s-]', '', query).strip().replace(' ', '_')[:30]
            filename = f"{safe_query}_{timestamp}.json"
            file_path = NEWS_DATA_DIR / filename

            with open(file_path, 'w', encoding='utf-8') as f:
                json.dump(save_items, f, ensure_ascii=False, indent=2)

            logger.info(f"뉴스 데이터 저장 완료: {file_path}")

            return {
                'success': True,
                'file_path': str(file_path),
                'count': len(save_items),
                'error': None
            }
        except Exception as e:
            logger.exception("JSON 저장 오류")
            return {
                'success': False,
                'file_path': None,
                'count': 0,
                'error': str(e)
            }

    #######################
    # MongoDB 저장 (주석 처리)
    #######################
    # def _save_to_mongo_sync(self, save_items: list, query: str) -> dict:
    #     """MongoDB로 동기 저장 (내부 함수)"""
    #     try:
    #         collection = get_mongo_collection()
    #
    #         # 각 아이템에 메타데이터 추가
    #         for item in save_items:
    #             item['_query'] = query
    #             item['_created_at'] = datetime.now()
    #
    #         # 일괄 삽입
    #         result = collection.insert_many(save_items)
    #
    #         logger.info(f"MongoDB 저장 완료: {len(result.inserted_ids)}개")
    #
    #         return {
    #             'success': True,
    #             'inserted_ids': [str(id) for id in result.inserted_ids],
    #             'count': len(result.inserted_ids),
    #             'error': None
    #         }
    #     except Exception as e:
    #         logger.exception("MongoDB 저장 오류")
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
    # MongoDB 비동기 저장 (주석 처리)
    #######################
    # def save_to_mongo_async(self, items: list, query: str, fetch_content: bool = True) -> dict:
    #     """
    #     검색 결과를 MongoDB로 비동기 저장
    #     """
    #     def _async_save():
    #         save_items = self._prepare_save_items(items, query, fetch_content)
    #         result = self._save_to_mongo_sync(save_items, query)
    #         logger.info(f"MongoDB 비동기 저장 완료: {result}")
    #
    #     _executor.submit(_async_save)
    #
    #     return {
    #         'success': True,
    #         'message': f'{len(items)}개 뉴스가 MongoDB에 저장 중입니다.',
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
        # MongoDB 저장 (주석 처리)
        #######################
        # if use_mongo:
        #     if async_save:
        #         save_result = self.save_to_mongo_async(result['items'], query, fetch_content)
        #     else:
        #         save_items = self._prepare_save_items(result['items'], query, fetch_content)
        #         save_result = self._save_to_mongo_sync(save_items, query)
        #     result['saved_to_mongo'] = save_result['success']
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
    # MongoDB 조회 (주석 처리)
    #######################
    # @staticmethod
    # def list_from_mongo(query: str = None, limit: int = 100, skip: int = 0) -> dict:
    #     """MongoDB에서 저장된 뉴스 목록 조회"""
    #     try:
    #         collection = get_mongo_collection()
    #
    #         # 필터 조건
    #         filter_query = {}
    #         if query:
    #             filter_query['_query'] = {'$regex': query, '$options': 'i'}
    #
    #         # 조회
    #         cursor = collection.find(filter_query).sort('_created_at', -1).skip(skip).limit(limit)
    #         items = []
    #         for doc in cursor:
    #             doc['_id'] = str(doc['_id'])  # ObjectId를 문자열로 변환
    #             if '_created_at' in doc:
    #                 doc['_created_at'] = doc['_created_at'].isoformat()
    #             items.append(doc)
    #
    #         # 전체 개수
    #         total = collection.count_documents(filter_query)
    #
    #         return {
    #             'success': True,
    #             'total': total,
    #             'count': len(items),
    #             'items': items,
    #             'error': None
    #         }
    #     except Exception as e:
    #         logger.exception("MongoDB 조회 오류")
    #         return {'success': False, 'total': 0, 'count': 0, 'items': [], 'error': str(e)}
    #
    # @staticmethod
    # def get_from_mongo(doc_id: str) -> dict:
    #     """MongoDB에서 특정 문서 조회"""
    #     try:
    #         from bson import ObjectId
    #         collection = get_mongo_collection()
    #
    #         doc = collection.find_one({'_id': ObjectId(doc_id)})
    #         if not doc:
    #             return {'success': False, 'error': '문서를 찾을 수 없습니다.', 'item': None}
    #
    #         doc['_id'] = str(doc['_id'])
    #         if '_created_at' in doc:
    #             doc['_created_at'] = doc['_created_at'].isoformat()
    #
    #         return {'success': True, 'item': doc, 'error': None}
    #     except Exception as e:
    #         logger.exception("MongoDB 문서 조회 오류")
    #         return {'success': False, 'error': str(e), 'item': None}
    #
    # @staticmethod
    # def delete_from_mongo(doc_id: str) -> dict:
    #     """MongoDB에서 문서 삭제"""
    #     try:
    #         from bson import ObjectId
    #         collection = get_mongo_collection()
    #
    #         result = collection.delete_one({'_id': ObjectId(doc_id)})
    #         if result.deleted_count > 0:
    #             return {'success': True, 'message': '삭제 완료'}
    #         else:
    #             return {'success': False, 'error': '문서를 찾을 수 없습니다.'}
    #     except Exception as e:
    #         logger.exception("MongoDB 삭제 오류")
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
    # MongoDB 저장 (주석 처리)
    #######################
    # @staticmethod
    # def save_to_mongo(items: list, method: str = 'rule') -> dict:
    #     """요약 결과를 MongoDB에 저장"""
    #     try:
    #         collection = get_mongo_collection()
    #
    #         doc = {
    #             'type': 'summarized',
    #             'method': method,
    #             'created_at': datetime.now(),
    #             'count': len(items),
    #             'items': items
    #         }
    #
    #         result = collection.insert_one(doc)
    #         logger.info(f"MongoDB 요약 저장 완료: {result.inserted_id}")
    #
    #         return {
    #             'success': True,
    #             'doc_id': str(result.inserted_id),
    #             'count': len(items),
    #             'error': None
    #         }
    #     except Exception as e:
    #         logger.exception("MongoDB 요약 저장 오류")
    #         return {'success': False, 'doc_id': None, 'count': 0, 'error': str(e)}
    #
    # @staticmethod
    # def list_from_mongo(limit: int = 50, skip: int = 0) -> dict:
    #     """MongoDB에서 요약 목록 조회"""
    #     try:
    #         collection = get_mongo_collection()
    #
    #         cursor = collection.find({'type': 'summarized'}).sort('created_at', -1).skip(skip).limit(limit)
    #         items = []
    #         for doc in cursor:
    #             doc['_id'] = str(doc['_id'])
    #             if 'created_at' in doc:
    #                 doc['created_at'] = doc['created_at'].isoformat()
    #             items.append(doc)
    #
    #         total = collection.count_documents({'type': 'summarized'})
    #
    #         return {'success': True, 'total': total, 'count': len(items), 'items': items, 'error': None}
    #     except Exception as e:
    #         logger.exception("MongoDB 요약 목록 조회 오류")
    #         return {'success': False, 'total': 0, 'count': 0, 'items': [], 'error': str(e)}
    #######################
