"""
#######################
# 기사 추출 서비스
#######################
# 이 파일은 URL에서 뉴스 기사 내용을 추출하는 기능을 제공합니다.
#
# 추출 전략: Fallback 패턴
# 1. newspaper3k 라이브러리로 시도 (1차)
# 2. 실패 시 BeautifulSoup으로 직접 파싱 (2차)
#
# 추출 정보:
# - 기사 본문 텍스트
# - 기사 제목
# - 출처 도메인
# - 발행일 (가능한 경우)
#######################
"""
import logging
import requests
from bs4 import BeautifulSoup
from urllib.parse import urlparse

logger = logging.getLogger(__name__)


#######################
# 기사 추출기 클래스
#######################
class ArticleExtractor:
    """
    URL에서 뉴스 기사 내용을 추출하는 클래스

    특징:
    - newspaper3k 우선 사용 (정확도 높음)
    - 실패 시 BeautifulSoup으로 폴백
    - 다양한 뉴스 사이트 지원

    사용법:
        extractor = ArticleExtractor()
        result = extractor.extract('https://news.example.com/article/123')
        if result['success']:
            print(result['text'])
    """

    #######################
    # 상수 정의
    #######################
    # 브라우저로 위장하기 위한 User-Agent (일부 사이트 차단 방지)
    USER_AGENT = (
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) '
        'AppleWebKit/537.36 (KHTML, like Gecko) '
        'Chrome/120.0.0.0 Safari/537.36'
    )
    TIMEOUT = 10  # 요청 타임아웃 (초)

    def __init__(self):
        """추출기 초기화"""
        self.headers = {'User-Agent': self.USER_AGENT}

    #######################
    # 메인 추출 함수
    #######################
    def extract(self, url):
        """
        URL에서 기사 내용 추출 (Fallback 전략 적용)

        추출 순서:
        1. newspaper3k로 시도 (더 정확함)
        2. 실패 시 BeautifulSoup으로 재시도

        Args:
            url: 뉴스 기사 URL

        Returns:
            dict: {
                'success': 성공 여부 (bool),
                'text': 기사 본문 텍스트 (str or None),
                'title': 기사 제목 (str or None),
                'source': 출처 도메인 (str or None),
                'published_at': 발행일 ISO 형식 (str or None),
                'error': 에러 메시지 (str or None)
            }
        """
        #######################
        # 1차 시도: newspaper3k
        #######################
        result = self._extract_with_newspaper(url)
        if result['success']:
            logger.info(f"Successfully extracted with newspaper3k: {url}")
            return result

        logger.warning(f"newspaper3k failed, trying BeautifulSoup fallback: {url}")

        #######################
        # 2차 시도: BeautifulSoup
        #######################
        result = self._extract_with_bs4(url)
        if result['success']:
            logger.info(f"Successfully extracted with BeautifulSoup: {url}")
            return result

        logger.error(f"All extraction methods failed for: {url}")
        return result

    #######################
    # newspaper3k 추출
    #######################
    def _extract_with_newspaper(self, url):
        """
        newspaper3k 라이브러리를 사용한 기사 추출

        newspaper3k 특징:
        - 뉴스 기사 전용 라이브러리
        - 본문/제목/발행일 자동 추출
        - 다양한 뉴스 사이트 레이아웃 지원

        Args:
            url: 기사 URL

        Returns:
            dict: 추출 결과
        """
        try:
            #######################
            # newspaper3k 임포트
            #######################
            import newspaper
            from newspaper import Article

            #######################
            # 기사 객체 생성 및 다운로드
            #######################
            article = Article(url, language='ko')  # 한국어 설정
            article.download()  # HTML 다운로드
            article.parse()     # 내용 파싱

            #######################
            # 메타데이터 추출
            #######################
            title = article.title or None
            text = article.text or ''

            # URL에서 도메인 추출 (예: news.naver.com)
            parsed_url = urlparse(url)
            source = parsed_url.netloc

            # 발행일 추출 (있으면 ISO 형식으로 변환)
            published_at = None
            if article.publish_date:
                published_at = article.publish_date.isoformat()

            #######################
            # 추출 결과 검증
            #######################
            # 본문이 너무 짧으면 추출 실패로 간주
            if not text or len(text.strip()) < 50:
                return {
                    'success': False,
                    'text': None,
                    'title': None,
                    'source': None,
                    'published_at': None,
                    'error': 'Extracted text too short or empty'
                }

            return {
                'success': True,
                'text': text.strip(),
                'title': title,
                'source': source,
                'published_at': published_at,
                'error': None
            }

        #######################
        # 예외 처리
        #######################
        except ImportError:
            # newspaper3k 미설치
            logger.warning("newspaper3k not installed")
            return {
                'success': False,
                'text': None,
                'title': None,
                'source': None,
                'published_at': None,
                'error': 'newspaper3k not available'
            }
        except Exception as e:
            # 기타 오류
            logger.warning(f"newspaper3k extraction failed: {str(e)}")
            return {
                'success': False,
                'text': None,
                'title': None,
                'source': None,
                'published_at': None,
                'error': f'newspaper3k error: {str(e)}'
            }

    #######################
    # BeautifulSoup 추출 (폴백)
    #######################
    def _extract_with_bs4(self, url):
        """
        requests + BeautifulSoup을 사용한 기사 추출 (폴백)

        직접 파싱 방식:
        - HTML 직접 다운로드
        - 일반적인 기사 컨테이너 태그 탐색
        - 불필요한 요소(스크립트, 스타일 등) 제거

        Args:
            url: 기사 URL

        Returns:
            dict: 추출 결과
        """
        try:
            #######################
            # HTML 다운로드
            #######################
            response = requests.get(
                url,
                headers=self.headers,
                timeout=self.TIMEOUT,
                allow_redirects=True  # 리다이렉트 허용
            )
            response.raise_for_status()  # HTTP 에러 시 예외 발생

            #######################
            # HTML 파싱
            #######################
            soup = BeautifulSoup(response.content, 'lxml')

            #######################
            # 제목 추출
            #######################
            title = None
            # 1. <title> 태그에서 추출
            if soup.title:
                title = soup.title.string.strip()
            # 2. Open Graph 메타 태그에서 추출
            elif soup.find('meta', property='og:title'):
                title = soup.find('meta', property='og:title')['content'].strip()

            #######################
            # 본문 컨테이너 탐색
            #######################
            # 일반적인 기사 본문 컨테이너 선택자들
            content = None
            for selector in ['article', 'main', '.article-content', '.post-content', '#content']:
                element = soup.select_one(selector)
                if element:
                    content = element
                    break

            # 컨테이너를 못 찾으면 body 전체 사용
            if not content:
                content = soup.body

            if not content:
                return {
                    'success': False,
                    'text': None,
                    'title': None,
                    'source': None,
                    'published_at': None,
                    'error': 'Could not find article content'
                }

            #######################
            # 불필요한 요소 제거
            #######################
            # 스크립트, 스타일, 네비게이션 등 제거
            for tag in content.find_all(['script', 'style', 'nav', 'header', 'footer']):
                tag.decompose()

            #######################
            # 텍스트 추출
            #######################
            text = content.get_text(separator='\n', strip=True)

            # URL에서 도메인 추출
            parsed_url = urlparse(url)
            source = parsed_url.netloc

            #######################
            # 추출 결과 검증
            #######################
            if not text or len(text.strip()) < 50:
                return {
                    'success': False,
                    'text': None,
                    'title': None,
                    'source': None,
                    'published_at': None,
                    'error': 'Extracted text too short or empty'
                }

            return {
                'success': True,
                'text': text.strip(),
                'title': title,
                'source': source,
                'published_at': None,  # BeautifulSoup에서는 발행일 추출 어려움
                'error': None
            }

        #######################
        # 예외 처리
        #######################
        except requests.Timeout:
            # 요청 타임아웃
            return {
                'success': False,
                'text': None,
                'title': None,
                'source': None,
                'published_at': None,
                'error': 'Request timeout'
            }
        except requests.RequestException as e:
            # 네트워크 오류
            return {
                'success': False,
                'text': None,
                'title': None,
                'source': None,
                'published_at': None,
                'error': f'Request failed: {str(e)}'
            }
        except Exception as e:
            # 기타 오류
            logger.exception("BeautifulSoup extraction failed")
            return {
                'success': False,
                'text': None,
                'title': None,
                'source': None,
                'published_at': None,
                'error': f'Extraction error: {str(e)}'
            }
