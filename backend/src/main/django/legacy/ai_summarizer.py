"""
#######################
# AI 기반 요약 서비스
#######################
# 이 파일은 HuggingFace Transformers를 사용한 AI 요약을 수행합니다.
#
# 요약 방식: 생성형 (Abstractive Summarization)
# - AI 모델이 텍스트를 이해하고 새로운 문장으로 요약 생성
# - 원문에 없는 표현도 사용 가능 (더 자연스러운 요약)
#
# 사용 모델:
# - 한국어: eenzeenee/t5-base-korean-summarization
# - 영어: facebook/bart-large-cnn
#
# 주의사항:
# - 처음 실행 시 모델 다운로드 필요 (수백MB~수GB)
# - GPU 있으면 빠름, 없으면 CPU로 동작 (느림)
# - transformers, torch, sentencepiece 패키지 필요
#######################
"""
import logging

logger = logging.getLogger(__name__)

#######################
# 지연 로딩 (Lazy Loading) 변수
#######################
# 모델은 무겁기 때문에 필요할 때만 로드합니다.
# 서버 시작 시 바로 로드하면 메모리 낭비 + 시작 시간 증가
_pipeline = None  # transformers의 pipeline 함수
_models = {}      # 언어별 모델 캐시 {'ko': model, 'en': model}


#######################
# Pipeline 지연 로딩
#######################
def _get_pipeline():
    """
    transformers의 pipeline 함수를 지연 로딩

    왜 지연 로딩?
    - transformers import 자체가 무거움 (수 초 소요)
    - AI 요약을 사용하지 않는 요청에서는 로드 불필요
    - 서버 시작 속도 향상

    Returns:
        pipeline 함수

    Raises:
        ImportError: transformers가 설치되지 않은 경우
    """
    global _pipeline

    if _pipeline is None:
        try:
            from transformers import pipeline
            _pipeline = pipeline
            logger.info("Transformers pipeline loaded successfully")
        except ImportError as e:
            logger.error(f"Failed to import transformers: {e}")
            raise ImportError(
                "transformers library is not installed. "
                "Please run: pip install transformers torch sentencepiece"
            )

    return _pipeline


#######################
# 모델 로딩 및 캐싱
#######################
def _get_model(language='ko'):
    """
    언어별 요약 모델을 로드하고 캐싱

    캐싱 이유:
    - 모델 로딩은 수 초~수십 초 소요
    - 한 번 로드한 모델은 메모리에 유지하여 재사용
    - 동일 언어 요청에서 빠른 응답

    Args:
        language: 언어 코드 ('ko' 또는 'en')

    Returns:
        로드된 요약 모델 (pipeline 객체)

    Raises:
        RuntimeError: 모델 로딩 실패 시
    """
    global _models

    # 이미 로드된 모델이 있으면 반환
    if language not in _models:
        pipeline_fn = _get_pipeline()

        #######################
        # GPU/CPU 자동 선택
        #######################
        try:
            import torch
            # CUDA GPU 사용 가능하면 GPU(device=0), 아니면 CPU(device=-1)
            device = 0 if torch.cuda.is_available() else -1
            device_name = "GPU" if device == 0 else "CPU"
        except ImportError:
            device = -1
            device_name = "CPU"

        #######################
        # 언어별 모델 선택
        #######################
        if language == 'ko':
            # 한국어 요약 모델 (T5 기반)
            model_name = 'eenzeenee/t5-base-korean-summarization'
        else:
            # 영어 요약 모델 (BART 기반)
            model_name = 'facebook/bart-large-cnn'

        logger.info(f"Loading model: {model_name} on {device_name}")

        #######################
        # 모델 로딩
        #######################
        try:
            _models[language] = pipeline_fn(
                'summarization',  # 요약 태스크
                model=model_name,
                device=device
            )
            logger.info(f"Model {model_name} loaded successfully")
        except Exception as e:
            logger.error(f"Failed to load model {model_name}: {e}")
            raise RuntimeError(f"Failed to load summarization model: {e}")

    return _models[language]


#######################
# AI 요약기 클래스
#######################
class AISummarizer:
    """
    AI 기반 생성형 요약기

    특징:
    - Transformer 모델 사용 (T5, BART)
    - 새로운 문장 생성 (Abstractive)
    - 더 자연스러운 요약 결과
    - 처음 실행 시 모델 다운로드 필요

    사용법:
        summarizer = AISummarizer(language='ko')
        result = summarizer.summarize(text, max_length=200, min_length=50)
    """

    def __init__(self, language='ko'):
        """
        AI 요약기 초기화

        Args:
            language: 텍스트 언어 ('ko': 한국어, 'en': 영어)
        """
        self.language = language
        self._model = None  # 지연 로딩을 위해 None으로 초기화

    #######################
    # 모델 지연 로딩
    #######################
    def _ensure_model(self):
        """
        모델이 로드되었는지 확인하고, 없으면 로드

        지연 로딩 패턴:
        - 객체 생성 시에는 모델 로드하지 않음
        - 실제 요약 요청 시 처음으로 로드
        - 이후 요청에서는 캐시된 모델 사용

        Returns:
            로드된 모델
        """
        if self._model is None:
            self._model = _get_model(self.language)
        return self._model

    #######################
    # 메인 요약 함수
    #######################
    def summarize(self, text, max_length=200, min_length=50):
        """
        AI 모델을 사용하여 텍스트 요약

        Args:
            text: 요약할 원문 텍스트
            max_length: 요약 최대 길이 (토큰 수 기준)
            min_length: 요약 최소 길이 (토큰 수 기준)

        Returns:
            dict: {
                'summary': 생성된 요약,
                'bullets': 핵심 포인트 목록,
                'keywords': 추출된 키워드 목록
            }

        Raises:
            RuntimeError: 요약 실패 시
        """
        try:
            # 모델 로드 (처음 호출 시에만 실제 로딩)
            model = self._ensure_model()

            #######################
            # 입력 길이 제한
            #######################
            # 모델마다 입력 길이 제한이 있음 (보통 512~1024 토큰)
            # 긴 텍스트는 잘라서 처리
            max_input_length = 1024
            if len(text) > max_input_length:
                text = text[:max_input_length]
                logger.warning(f"Input truncated to {max_input_length} characters")

            #######################
            # AI 요약 생성
            #######################
            result = model(
                text,
                max_length=max_length,   # 요약 최대 길이
                min_length=min_length,   # 요약 최소 길이
                do_sample=False,         # 결정적 생성 (항상 같은 결과)
                truncation=True          # 입력이 길면 자르기
            )

            # 생성된 요약 추출
            summary = result[0]['summary_text']

            #######################
            # 핵심 포인트 추출
            #######################
            # 요약 텍스트를 문장 단위로 분리하여 불릿 생성
            bullets = self._extract_bullets(summary)

            #######################
            # 키워드 추출
            #######################
            # 원문에서 주요 키워드 추출
            keywords = self._extract_keywords(text)

            return {
                'summary': summary,
                'bullets': bullets,
                'keywords': keywords
            }

        except Exception as e:
            logger.error(f"AI summarization failed: {e}")
            raise RuntimeError(f"AI summarization failed: {e}")

    #######################
    # 핵심 포인트 추출
    #######################
    def _extract_bullets(self, summary):
        """
        요약 텍스트에서 핵심 포인트 추출

        방식:
        - 요약을 문장 단위로 분리
        - 각 문장을 하나의 핵심 포인트로 사용

        Args:
            summary: AI가 생성한 요약 텍스트

        Returns:
            list: 핵심 포인트 리스트 (최대 5개)
        """
        import re

        # 언어에 따른 문장 분리
        if self.language == 'ko':
            # 한국어: 문장 부호로 분리
            sentences = re.split(r'[.!?]\s*', summary)
        else:
            # 영어: 문장 부호 뒤 공백으로 분리
            sentences = re.split(r'(?<=[.!?])\s+', summary)

        # 너무 짧은 문장 제거 (10자 미만)
        bullets = [s.strip() for s in sentences if len(s.strip()) > 10]

        # 긴 문장은 100자에서 자르기
        bullets = [b[:100] + '...' if len(b) > 100 else b for b in bullets]

        return bullets[:5]  # 최대 5개

    #######################
    # 키워드 추출
    #######################
    def _extract_keywords(self, text):
        """
        원문에서 주요 키워드 추출

        방식:
        - 단어 빈도수 기반 추출
        - 불용어(조사, 관사 등) 제거
        - 상위 빈도 단어 반환

        Args:
            text: 원문 텍스트

        Returns:
            list: 키워드 리스트 (최대 10개)
        """
        import re
        from collections import Counter

        # 단어 추출 (소문자 변환)
        words = re.findall(r'\b\w+\b', text.lower())

        #######################
        # 불용어 목록
        #######################
        # 의미 없이 자주 등장하는 단어들
        stop_words = set([
            # 영어 불용어
            'the', 'a', 'an', 'and', 'or', 'but', 'in', 'on', 'at', 'to', 'for',
            'is', 'are', 'was', 'were', 'be', 'been', 'being', 'have', 'has', 'had',
            'do', 'does', 'did', 'will', 'would', 'could', 'should', 'may', 'might',
            'this', 'that', 'these', 'those', 'it', 'its', 'of', 'with', 'as', 'by',
            # 한국어 불용어
            '은', '는', '이', '가', '을', '를', '의', '에', '와', '과', '도', '로',
            '있다', '없다', '하다', '되다', '이다', '있는', '하는', '된', '할', '한'
        ])

        # 불용어 및 2글자 이하 제거
        words = [w for w in words if w not in stop_words and len(w) > 2]

        # 빈도수 기준 상위 10개
        freq = Counter(words)
        keywords = [word for word, count in freq.most_common(10)]

        return keywords


#######################
# AI 사용 가능 여부 확인
#######################
def check_ai_available():
    """
    AI 요약 기능 사용 가능 여부 확인

    확인 내용:
    - transformers 라이브러리 설치 여부
    - (모델 로드는 확인하지 않음 - 너무 오래 걸림)

    Returns:
        bool: 사용 가능하면 True, 아니면 False

    사용 예:
        if check_ai_available():
            summarizer = AISummarizer()
        else:
            # rule 기반으로 폴백
            summarizer = ArticleSummarizer()
    """
    try:
        _get_pipeline()
        return True
    except ImportError:
        return False
