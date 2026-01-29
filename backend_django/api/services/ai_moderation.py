"""
#######################
# AI 기반 콘텐츠 모더레이션 서비스
#######################
# 이 파일은 AI 모델을 사용하여 댓글/게시글의 부적절한 콘텐츠를 감지합니다.
#
# 감지 카테고리:
# - profanity: 욕설/비속어
# - spam: 스팸 콘텐츠
# - adult: 성인 콘텐츠
# - violence: 폭력적 콘텐츠
# - hate: 혐오 발언
# - harassment: 괴롭힘/악성 댓글
#
# 필터링 수준:
# - allow: 허용
# - warning: 경고 (사용자에게 경고 메시지)
# - review: 관리자 검토 대기
# - block: 차단
#
# 사용 모델:
# - 한국어: beomi/KcBERT-base (한국어 악성 댓글 분류)
# - 다국어: facebook/roberta-hate-speech-dynabench-r4-target
#
# 주의사항:
# - 처음 실행 시 모델 다운로드 필요
# - GPU 있으면 빠름, 없으면 CPU로 동작
# - transformers, torch 패키지 필요
#######################
"""
import logging
import hashlib
import json
from typing import Optional, Dict, List, Any
from dataclasses import dataclass, field, asdict
from datetime import datetime, timedelta

logger = logging.getLogger(__name__)

#######################
# 설정 상수
#######################
# 콘텐츠 카테고리별 임계값 (0.0 ~ 1.0)
DEFAULT_THRESHOLDS = {
    'profanity': 0.7,    # 욕설/비속어
    'spam': 0.8,         # 스팸
    'adult': 0.7,        # 성인 콘텐츠
    'violence': 0.7,     # 폭력
    'hate': 0.7,         # 혐오 발언
    'harassment': 0.7,   # 괴롭힘
}

# 조치 수준 (낮은 순서)
ACTION_LEVELS = ['allow', 'warning', 'review', 'block']

#######################
# 결과 데이터 클래스
#######################
@dataclass
class ModerationCategory:
    """개별 카테고리 분석 결과"""
    category: str
    score: float
    is_flagged: bool
    threshold: float


@dataclass
class ModerationResult:
    """AI 모더레이션 분석 결과"""
    is_flagged: bool = False
    action: str = 'allow'  # allow, warning, review, block
    categories: List[ModerationCategory] = field(default_factory=list)
    highest_category: Optional[str] = None
    highest_score: float = 0.0
    confidence: float = 0.0
    model_used: str = ''
    processing_time_ms: int = 0
    cached: bool = False
    error: Optional[str] = None

    def to_dict(self) -> Dict[str, Any]:
        """딕셔너리 변환"""
        return {
            'is_flagged': self.is_flagged,
            'action': self.action,
            'categories': [
                {
                    'category': c.category,
                    'score': round(c.score, 4),
                    'is_flagged': c.is_flagged,
                    'threshold': c.threshold
                }
                for c in self.categories
            ],
            'highest_category': self.highest_category,
            'highest_score': round(self.highest_score, 4),
            'confidence': round(self.confidence, 4),
            'model_used': self.model_used,
            'processing_time_ms': self.processing_time_ms,
            'cached': self.cached,
            'error': self.error
        }


#######################
# 지연 로딩 변수
#######################
_pipeline = None
_models = {}
_tokenizers = {}
_cache = {}  # 간단한 메모리 캐시 (Redis 대체)
_cache_ttl = 300  # 캐시 유효시간 (5분)


#######################
# Pipeline 지연 로딩
#######################
def _get_pipeline():
    """transformers의 pipeline 함수를 지연 로딩"""
    global _pipeline

    if _pipeline is None:
        try:
            from transformers import pipeline
            _pipeline = pipeline
            logger.info("Transformers pipeline loaded for moderation")
        except ImportError as e:
            logger.error(f"Failed to import transformers: {e}")
            raise ImportError(
                "transformers library is not installed. "
                "Please run: pip install transformers torch"
            )

    return _pipeline


#######################
# 캐시 관리
#######################
def _get_cache_key(text: str, settings: dict) -> str:
    """캐시 키 생성"""
    content = f"{text}:{json.dumps(settings, sort_keys=True)}"
    return hashlib.md5(content.encode()).hexdigest()


def _get_from_cache(key: str) -> Optional[ModerationResult]:
    """캐시에서 결과 조회"""
    if key in _cache:
        entry = _cache[key]
        if datetime.now() < entry['expires']:
            result = entry['result']
            result.cached = True
            return result
        else:
            del _cache[key]
    return None


def _set_cache(key: str, result: ModerationResult):
    """캐시에 결과 저장"""
    _cache[key] = {
        'result': result,
        'expires': datetime.now() + timedelta(seconds=_cache_ttl)
    }

    # 캐시 크기 제한 (최대 1000개)
    if len(_cache) > 1000:
        # 오래된 항목 삭제
        expired_keys = [
            k for k, v in _cache.items()
            if datetime.now() >= v['expires']
        ]
        for k in expired_keys[:100]:
            del _cache[k]


#######################
# 모델 로딩
#######################
def _get_korean_model():
    """한국어 악성 댓글 분류 모델 로드"""
    global _models

    if 'korean' in _models:
        return _models['korean']

    try:
        import torch
        pipeline_fn = _get_pipeline()

        # 한국어 악성 댓글 분류 모델
        # beomi/KcBERT는 한국어 BERT 모델
        # 실제 배포 시에는 fine-tuned 모델 사용 권장
        model = pipeline_fn(
            task="text-classification",
            model="beomi/kcbert-base",
            device=0 if torch.cuda.is_available() else -1
        )

        _models['korean'] = model
        logger.info("Korean moderation model loaded")
        return model

    except Exception as e:
        logger.warning(f"Failed to load Korean model: {e}")
        return None


def _get_multilingual_model():
    """다국어 혐오 발언 분류 모델 로드"""
    global _models

    if 'multilingual' in _models:
        return _models['multilingual']

    try:
        import torch
        pipeline_fn = _get_pipeline()

        # 다국어 혐오 발언 분류 모델
        model = pipeline_fn(
            task="text-classification",
            model="facebook/roberta-hate-speech-dynabench-r4-target",
            device=0 if torch.cuda.is_available() else -1
        )

        _models['multilingual'] = model
        logger.info("Multilingual moderation model loaded")
        return model

    except Exception as e:
        logger.warning(f"Failed to load multilingual model: {e}")
        return None


#######################
# AI 모더레이션 서비스 클래스
#######################
class AIContentModerator:
    """
    AI 기반 콘텐츠 모더레이션 서비스

    사용법:
        moderator = AIContentModerator()
        result = moderator.check("검사할 텍스트")

        if result.is_flagged:
            print(f"부적절한 콘텐츠 감지: {result.highest_category}")
            print(f"조치: {result.action}")
    """

    def __init__(
        self,
        language: str = 'ko',
        thresholds: Optional[Dict[str, float]] = None,
        action_mapping: Optional[Dict[str, str]] = None
    ):
        """
        AI 모더레이터 초기화

        Args:
            language: 주요 언어 ('ko': 한국어, 'en': 영어)
            thresholds: 카테고리별 임계값 (기본값 사용 시 None)
            action_mapping: 점수 범위별 조치 매핑
        """
        self.language = language
        self.thresholds = thresholds or DEFAULT_THRESHOLDS.copy()
        self.action_mapping = action_mapping or {
            # 점수 범위별 조치
            'low': 'warning',     # 0.5 ~ 0.7
            'medium': 'review',   # 0.7 ~ 0.85
            'high': 'block',      # 0.85 이상
        }
        self._model = None

    def _ensure_model(self):
        """모델 로드 확인"""
        if self._model is None:
            if self.language == 'ko':
                self._model = _get_korean_model()
            else:
                self._model = _get_multilingual_model()
        return self._model

    def _determine_action(self, score: float) -> str:
        """점수에 따른 조치 결정"""
        if score >= 0.85:
            return self.action_mapping.get('high', 'block')
        elif score >= 0.7:
            return self.action_mapping.get('medium', 'review')
        elif score >= 0.5:
            return self.action_mapping.get('low', 'warning')
        return 'allow'

    def check(
        self,
        text: str,
        use_cache: bool = True,
        custom_thresholds: Optional[Dict[str, float]] = None
    ) -> ModerationResult:
        """
        텍스트 콘텐츠 검사

        Args:
            text: 검사할 텍스트
            use_cache: 캐시 사용 여부
            custom_thresholds: 이 요청에만 적용할 커스텀 임계값

        Returns:
            ModerationResult: 분석 결과
        """
        import time
        start_time = time.time()

        if not text or not text.strip():
            return ModerationResult(
                is_flagged=False,
                action='allow',
                model_used='none',
                processing_time_ms=0
            )

        # 임계값 설정
        thresholds = custom_thresholds or self.thresholds

        # 캐시 확인
        cache_key = _get_cache_key(text, thresholds)
        if use_cache:
            cached_result = _get_from_cache(cache_key)
            if cached_result:
                return cached_result

        try:
            # 규칙 기반 사전 필터링 (빠른 검사)
            rule_result = self._rule_based_check(text)
            if rule_result.is_flagged:
                rule_result.processing_time_ms = int((time.time() - start_time) * 1000)
                if use_cache:
                    _set_cache(cache_key, rule_result)
                return rule_result

            # AI 모델 기반 검사
            model = self._ensure_model()
            if model is None:
                # 모델 로드 실패 시 규칙 기반 결과 반환
                return ModerationResult(
                    is_flagged=False,
                    action='allow',
                    model_used='rule_based_only',
                    processing_time_ms=int((time.time() - start_time) * 1000),
                    error='AI model not available, using rule-based only'
                )

            # 모델 예측
            predictions = model(text[:512])  # 최대 512 토큰

            # 결과 분석
            result = self._analyze_predictions(predictions, thresholds)
            result.model_used = self.language
            result.processing_time_ms = int((time.time() - start_time) * 1000)

            # 캐시 저장
            if use_cache:
                _set_cache(cache_key, result)

            return result

        except Exception as e:
            logger.error(f"AI moderation failed: {e}")
            return ModerationResult(
                is_flagged=False,
                action='allow',
                model_used='error',
                processing_time_ms=int((time.time() - start_time) * 1000),
                error=str(e)
            )

    def _rule_based_check(self, text: str) -> ModerationResult:
        """
        규칙 기반 사전 필터링
        AI 모델 호출 전 빠른 검사
        """
        import re

        categories = []

        # 한국어 욕설 패턴 (기본적인 패턴만 포함)
        profanity_patterns = [
            r'시[발빠팔][ㄱ-ㅎㅏ-ㅣ]*',
            r'씨[발빠팔][ㄱ-ㅎㅏ-ㅣ]*',
            r'ㅅㅂ',
            r'ㅂㅅ',
            r'ㄱㅅㄲ',
            r'병[신싄][ㄱ-ㅎㅏ-ㅣ]*',
            r'ㅄ',
        ]

        profanity_score = 0.0
        for pattern in profanity_patterns:
            if re.search(pattern, text, re.IGNORECASE):
                profanity_score = 0.95
                break

        if profanity_score > 0:
            categories.append(ModerationCategory(
                category='profanity',
                score=profanity_score,
                is_flagged=True,
                threshold=self.thresholds.get('profanity', 0.7)
            ))

        # 스팸 패턴
        spam_indicators = [
            len(re.findall(r'http[s]?://\S+', text)) > 3,  # 과도한 URL
            len(re.findall(r'[\U0001F600-\U0001F64F]', text)) > 10,  # 과도한 이모지
            bool(re.search(r'(.)\1{10,}', text)),  # 10회 이상 반복 문자
            bool(re.search(r'(광고|홍보|클릭|무료|당첨)', text)),  # 스팸 키워드
        ]

        spam_score = sum(spam_indicators) / len(spam_indicators)
        if spam_score >= 0.5:
            categories.append(ModerationCategory(
                category='spam',
                score=spam_score,
                is_flagged=spam_score >= self.thresholds.get('spam', 0.8),
                threshold=self.thresholds.get('spam', 0.8)
            ))

        # 결과 생성
        if categories:
            flagged_cats = [c for c in categories if c.is_flagged]
            if flagged_cats:
                highest = max(flagged_cats, key=lambda x: x.score)
                return ModerationResult(
                    is_flagged=True,
                    action=self._determine_action(highest.score),
                    categories=categories,
                    highest_category=highest.category,
                    highest_score=highest.score,
                    confidence=highest.score,
                    model_used='rule_based'
                )

        return ModerationResult(
            is_flagged=False,
            action='allow',
            categories=categories,
            model_used='rule_based'
        )

    def _analyze_predictions(
        self,
        predictions: List[Dict],
        thresholds: Dict[str, float]
    ) -> ModerationResult:
        """AI 모델 예측 결과 분석"""
        categories = []

        # 예측 결과를 카테고리별로 매핑
        # 실제 모델 출력에 따라 조정 필요
        for pred in predictions:
            label = pred.get('label', '').lower()
            score = pred.get('score', 0.0)

            # 레이블 매핑 (모델에 따라 조정)
            category = self._map_label_to_category(label)
            threshold = thresholds.get(category, 0.7)

            categories.append(ModerationCategory(
                category=category,
                score=score,
                is_flagged=score >= threshold,
                threshold=threshold
            ))

        # 결과 분석
        flagged_cats = [c for c in categories if c.is_flagged]

        if flagged_cats:
            highest = max(flagged_cats, key=lambda x: x.score)
            return ModerationResult(
                is_flagged=True,
                action=self._determine_action(highest.score),
                categories=categories,
                highest_category=highest.category,
                highest_score=highest.score,
                confidence=highest.score
            )

        # 가장 높은 점수 찾기 (플래그되지 않았어도)
        if categories:
            highest = max(categories, key=lambda x: x.score)
            return ModerationResult(
                is_flagged=False,
                action='allow',
                categories=categories,
                highest_category=highest.category,
                highest_score=highest.score,
                confidence=1.0 - highest.score
            )

        return ModerationResult(
            is_flagged=False,
            action='allow',
            categories=categories,
            confidence=1.0
        )

    def _map_label_to_category(self, label: str) -> str:
        """모델 레이블을 표준 카테고리로 매핑"""
        label_mapping = {
            # 일반적인 분류 모델 레이블
            'hate': 'hate',
            'hateful': 'hate',
            'hate_speech': 'hate',
            'offensive': 'profanity',
            'toxic': 'harassment',
            'severe_toxic': 'harassment',
            'obscene': 'adult',
            'threat': 'violence',
            'insult': 'harassment',
            'identity_hate': 'hate',
            'spam': 'spam',
            'not_hate': 'clean',
            'nothate': 'clean',
            'normal': 'clean',
            'clean': 'clean',
        }
        return label_mapping.get(label, 'unknown')

    def batch_check(
        self,
        texts: List[str],
        use_cache: bool = True
    ) -> List[ModerationResult]:
        """
        여러 텍스트 일괄 검사

        Args:
            texts: 검사할 텍스트 목록
            use_cache: 캐시 사용 여부

        Returns:
            ModerationResult 목록
        """
        return [self.check(text, use_cache) for text in texts]


#######################
# 싱글톤 인스턴스
#######################
_moderator_instance = None


def get_ai_moderator(language: str = 'ko') -> AIContentModerator:
    """AI 모더레이터 싱글톤 인스턴스 반환"""
    global _moderator_instance

    if _moderator_instance is None or _moderator_instance.language != language:
        _moderator_instance = AIContentModerator(language=language)

    return _moderator_instance


#######################
# AI 사용 가능 여부 확인
#######################
def check_ai_moderation_available() -> Dict[str, Any]:
    """
    AI 모더레이션 기능 사용 가능 여부 확인

    Returns:
        dict: 사용 가능 여부 및 상세 정보
    """
    result = {
        'available': False,
        'transformers_installed': False,
        'torch_installed': False,
        'gpu_available': False,
        'models_loaded': list(_models.keys()),
        'error': None
    }

    try:
        import transformers
        result['transformers_installed'] = True
        result['transformers_version'] = transformers.__version__
    except ImportError:
        result['error'] = 'transformers not installed'
        return result

    try:
        import torch
        result['torch_installed'] = True
        result['torch_version'] = torch.__version__
        result['gpu_available'] = torch.cuda.is_available()
        if result['gpu_available']:
            result['gpu_name'] = torch.cuda.get_device_name(0)
    except ImportError:
        result['error'] = 'torch not installed'
        return result

    result['available'] = True
    return result
