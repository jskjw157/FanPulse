"""
뉴스 파일 저장/조회 서비스

JSON 파일 기반 뉴스 및 요약 데이터 관리
"""
import json
import logging
from datetime import datetime
from pathlib import Path

from common.validators import validate_filename

logger = logging.getLogger(__name__)

NEWS_DATA_DIR = Path(__file__).parent.parent.parent / "news_data"
SUMMARIZED_DATA_DIR = Path(__file__).parent.parent.parent / "summarized_data"


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
        # 파일명 검증 (경로 조작 방지)
        try:
            validate_filename(filename)
        except ValueError as e:
            return {'success': False, 'error': str(e), 'items': []}

        file_path = NEWS_DATA_DIR / filename

        if not file_path.exists():
            return {'success': False, 'error': '파일을 찾을 수 없습니다.', 'items': []}

        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                items = json.load(f)

            return {
                'success': True, 'filename': filename,
                'count': len(items), 'items': items, 'error': None
            }
        except Exception as e:
            logger.exception(f"JSON 파일 읽기 오류: {filename}")
            return {'success': False, 'error': str(e), 'items': []}

    @staticmethod
    def delete_json_file(filename: str) -> dict:
        """JSON 파일 삭제"""
        # 파일명 검증 (경로 조작 방지)
        try:
            validate_filename(filename)
        except ValueError as e:
            return {'success': False, 'error': str(e)}

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


class SummarizedNewsManager:
    """요약된 뉴스 데이터 저장/조회"""

    @staticmethod
    def save_summarized_news(items: list, method: str = 'rule') -> dict:
        """요약 결과를 JSON 파일로 저장"""
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
                'success': True, 'filename': filename,
                'file_path': str(file_path), 'count': len(items), 'error': None
            }
        except Exception as e:
            logger.exception("요약 데이터 저장 오류")
            return {
                'success': False, 'filename': None,
                'file_path': None, 'count': 0, 'error': str(e)
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
        try:
            validate_filename(filename)
        except ValueError as e:
            return {'success': False, 'error': str(e), 'items': []}

        file_path = SUMMARIZED_DATA_DIR / filename

        if not file_path.exists():
            return {'success': False, 'error': '파일을 찾을 수 없습니다.', 'items': []}

        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                data = json.load(f)

            return {
                'success': True, 'filename': filename,
                'method': data.get('method', 'unknown'),
                'created_at': data.get('created_at'),
                'count': data.get('count', len(data.get('items', []))),
                'items': data.get('items', []), 'error': None
            }
        except Exception as e:
            logger.exception(f"요약 파일 읽기 오류: {filename}")
            return {'success': False, 'error': str(e), 'items': []}

    @staticmethod
    def delete_summarized_file(filename: str) -> dict:
        """요약 파일 삭제"""
        try:
            validate_filename(filename)
        except ValueError as e:
            return {'success': False, 'error': str(e)}

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
