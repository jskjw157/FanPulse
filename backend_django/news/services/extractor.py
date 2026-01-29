"""
기사 추출 서비스

URL에서 뉴스 기사 내용을 추출합니다.
추출 전략: Fallback 패턴
1. newspaper3k 라이브러리로 시도 (1차)
2. 실패 시 BeautifulSoup으로 직접 파싱 (2차)
"""
import logging
import requests
from bs4 import BeautifulSoup
from urllib.parse import urlparse
from common.validators import validate_url

logger = logging.getLogger(__name__)


class ArticleExtractor:
    """URL에서 뉴스 기사 내용을 추출하는 클래스"""

    USER_AGENT = (
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) '
        'AppleWebKit/537.36 (KHTML, like Gecko) '
        'Chrome/120.0.0.0 Safari/537.36'
    )
    TIMEOUT = 10

    def __init__(self):
        self.headers = {'User-Agent': self.USER_AGENT}

    def extract(self, url):
        """URL에서 기사 내용 추출 (Fallback 전략)"""

        # SSRF 방지: URL 안전성 검증
        try:
            validate_url(url)
        except ValueError as e:
            logger.warning(f"URL 검증 실패: {url}, {e}")
            return {
                'success': False,
                'text': None,
                'title': None,
                'source': None,
                'published_at': None,
                'error': f'URL 검증 실패: {str(e)}'
            }

        # 1차: newspaper3k
        result = self._extract_with_newspaper(url)
        if result['success']:
            logger.info(f"newspaper3k 추출 성공: {url}")
            return result

        logger.warning(f"newspaper3k 실패, BeautifulSoup 폴백: {url}")

        # 2차: BeautifulSoup
        result = self._extract_with_bs4(url)
        if result['success']:
            logger.info(f"BeautifulSoup 추출 성공: {url}")
            return result

        logger.error(f"모든 추출 방법 실패: {url}")
        return result

    def _extract_with_newspaper(self, url):
        """newspaper3k 라이브러리를 사용한 기사 추출"""
        try:
            from newspaper import Article

            article = Article(url, language='ko')
            article.download()
            article.parse()

            title = article.title or None
            text = article.text or ''

            parsed_url = urlparse(url)
            source = parsed_url.netloc

            published_at = None
            if article.publish_date:
                published_at = article.publish_date.isoformat()

            if not text or len(text.strip()) < 50:
                return {
                    'success': False, 'text': None, 'title': None,
                    'source': None, 'published_at': None,
                    'error': 'Extracted text too short or empty'
                }

            return {
                'success': True, 'text': text.strip(), 'title': title,
                'source': source, 'published_at': published_at, 'error': None
            }

        except ImportError:
            logger.warning("newspaper3k not installed")
            return {
                'success': False, 'text': None, 'title': None,
                'source': None, 'published_at': None,
                'error': 'newspaper3k not available'
            }
        except Exception as e:
            logger.warning(f"newspaper3k 추출 실패: {str(e)}")
            return {
                'success': False, 'text': None, 'title': None,
                'source': None, 'published_at': None,
                'error': f'newspaper3k error: {str(e)}'
            }

    def _extract_with_bs4(self, url):
        """requests + BeautifulSoup을 사용한 기사 추출 (폴백)"""
        try:
            response = requests.get(
                url, headers=self.headers,
                timeout=self.TIMEOUT, allow_redirects=True
            )
            response.raise_for_status()

            soup = BeautifulSoup(response.content, 'lxml')

            title = None
            if soup.title:
                title = soup.title.string.strip()
            elif soup.find('meta', property='og:title'):
                title = soup.find('meta', property='og:title')['content'].strip()

            content = None
            for selector in ['article', 'main', '.article-content', '.post-content', '#content']:
                element = soup.select_one(selector)
                if element:
                    content = element
                    break

            if not content:
                content = soup.body

            if not content:
                return {
                    'success': False, 'text': None, 'title': None,
                    'source': None, 'published_at': None,
                    'error': 'Could not find article content'
                }

            for tag in content.find_all(['script', 'style', 'nav', 'header', 'footer']):
                tag.decompose()

            text = content.get_text(separator='\n', strip=True)
            parsed_url = urlparse(url)
            source = parsed_url.netloc

            if not text or len(text.strip()) < 50:
                return {
                    'success': False, 'text': None, 'title': None,
                    'source': None, 'published_at': None,
                    'error': 'Extracted text too short or empty'
                }

            return {
                'success': True, 'text': text.strip(), 'title': title,
                'source': source, 'published_at': None, 'error': None
            }

        except requests.Timeout:
            return {
                'success': False, 'text': None, 'title': None,
                'source': None, 'published_at': None, 'error': 'Request timeout'
            }
        except requests.RequestException as e:
            return {
                'success': False, 'text': None, 'title': None,
                'source': None, 'published_at': None,
                'error': f'Request failed: {str(e)}'
            }
        except Exception as e:
            logger.exception("BeautifulSoup extraction failed")
            return {
                'success': False, 'text': None, 'title': None,
                'source': None, 'published_at': None,
                'error': f'Extraction error: {str(e)}'
            }
