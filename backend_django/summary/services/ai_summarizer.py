"""
AI 기반 요약 서비스

HuggingFace Transformers를 사용한 생성형 요약
- 한국어: Mistral-7B-Instruct-v0.3
         Qwen2.5-3B-Instruct
         => GPU 성능에 따라 사용 모델 분류
- 영어: facebook/bart-large-cnn
"""
import torch
import logging

logger = logging.getLogger(__name__)

# 지연 로딩 변수
_pipeline = None
_models = {}

# GPU vram 확인
def get_gpu_vram_gb():
    if not torch.cuda.is_available():
        return 0
    props = torch.cuda.get_device_properties(0)
    return round(props.total_memory / (1024 ** 3), 2)

def _get_pipeline():
    """transformers pipeline 지연 로딩"""
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


def _get_llm_model(model_name):
    """LLM 모델 로딩 (메모리 제한 포함)"""
    import sys

    logger.info("===== ENV CHECK =====")
    logger.info(f"Python exe : {sys.executable}")
    logger.info(f"Torch ver  : {torch.__version__}")
    logger.info(f"CUDA avail : {torch.cuda.is_available()}")
    logger.info(f"CUDA ver   : {torch.version.cuda}")
    logger.info(f"GPU name   : {torch.cuda.get_device_name(0) if torch.cuda.is_available() else 'NO GPU'}")
    logger.info("=====================")

    from transformers import (
        AutoTokenizer,
        AutoModelForCausalLM,
        BitsAndBytesConfig
    )

    bnb_config = BitsAndBytesConfig(
        load_in_4bit=True,
        bnb_4bit_quant_type="nf4",
        bnb_4bit_use_double_quant=True,
        bnb_4bit_compute_dtype=torch.float16,
    )

    tokenizer = AutoTokenizer.from_pretrained(model_name)

    # max_memory 설정으로 OOM 방지
    vram_gb = get_gpu_vram_gb()

    if vram_gb >= 8:
        max_memory = {0: "7GB", "cpu": "10GB"}
    else:
        max_memory = {0: "4GB", "cpu": "10GB"}

    model = AutoModelForCausalLM.from_pretrained(
        model_name,
        quantization_config=bnb_config,
        device_map="auto",
        max_memory=max_memory
    )

    model.eval()
    return tokenizer, model


def _get_model(language='ko'):
    """언어별 요약 모델 로드 및 캐싱"""
    global _models

    if language in _models:
        return _models[language]

    if language == 'ko':
        vram_gb = get_gpu_vram_gb()
        logger.info(f"Detected GPU VRAM: {vram_gb} GB")

        try:
            if vram_gb >= 8:
                model_name = "mistralai/Mistral-7B-Instruct-v0.3"
                logger.info("Trying Mistral-7B")
            else:
                raise RuntimeError("Insufficient VRAM for Mistral")

            tokenizer, model = _get_llm_model(model_name)

        except Exception as e:
            logger.warning(f"Mistral load failed, fallback to Qwen: {e}")
            model_name = "Qwen/Qwen2.5-3B-Instruct"
            tokenizer, model = _get_llm_model(model_name)

        _models[language] = {
            "type": "llm",
            "tokenizer": tokenizer,
            "model": model
        }
    else:
        pipeline_fn = _get_pipeline()
        _models[language] = {
            "type": "pipeline",
            "model": pipeline_fn(
                task="summarization",
                model="facebook/bart-large-cnn",
                device=0 if torch.cuda.is_available() else -1
            )
        }

    return _models[language]


def build_news_prompt(text: str) -> str:
    """LLM 뉴스 요약 프롬프트"""
    return f"""
너는 뉴스 요약 AI다.
아래 기사에 **있는 정보만 사용**하여 요약하라.
추측, 해석, 평가, 배경 설명은 절대 추가하지 마라.

요약 규칙:
- 사실만 사용
- 인물, 날짜, 수치는 원문 그대로
- 최대 1문장
- 불필요한 형용사 및 기호 제거
- 결과물 첫단어는 무조건 "[[요약]]"로 시작
- 요약은 반드시 한 문장만 출력하고, 추가 설명을 하지 마라.

기사:
\"\"\"
{text}
\"\"\"

요약:
"""


class AISummarizer:
    """AI 기반 생성형 요약기"""

    def __init__(self, language='ko'):
        self.language = language
        self._model = None

    def _ensure_model(self):
        if self._model is None:
            self._model = _get_model(self.language)
        return self._model

    def summarize(self, text, max_length=200, min_length=50):
        try:
            model_bundle = self._ensure_model()
            prompt = build_news_prompt(text)

            if model_bundle["type"] == "llm":
                tokenizer = model_bundle["tokenizer"]
                model = model_bundle["model"]

                inputs = tokenizer(prompt, return_tensors="pt").to(model.device)

                # with torch.no_grad():
                #     outputs = model.generate(
                #         **inputs,
                #         max_new_tokens=max_length,
                #         do_sample=False,
                #         repetition_penalty=1.1,
                #         pad_token_id=tokenizer.eos_token_id
                #     )
                with torch.no_grad():
                    outputs = model.generate(
                        **inputs,
                        max_new_tokens=max_length,
                        do_sample=False,
                        temperature=0.0,
                        top_p=1.0,
                        repetition_penalty=1.1,
                        pad_token_id=tokenizer.eos_token_id
                    )

                summary = tokenizer.decode(
                    outputs[0][inputs["input_ids"].shape[-1]:],
                    skip_special_tokens=True
                ).strip()
            else:
                pipeline_model = model_bundle["model"]
                result = pipeline_model(text, max_length=max_length, min_length=min_length)
                summary = result[0]["summary_text"].strip()

            bullets = self._extract_bullets(summary)
            keywords = self._extract_keywords(text)

            return {
                "summary": summary,
                "bullets": bullets,
                "keywords": keywords,
                "model_type": model_bundle["type"]
            }

        except Exception as e:
            logger.error(f"AI summarization failed: {e}")
            raise RuntimeError(f"AI summarization failed: {e}")

    def _extract_bullets(self, summary):
        """요약에서 핵심 포인트 추출"""
        import re
        if self.language == 'ko':
            sentences = re.split(r'[.!?]\s*', summary)
        else:
            sentences = re.split(r'(?<=[.!?])\s+', summary)

        bullets = [s.strip() for s in sentences if len(s.strip()) > 10]
        bullets = [b[:100] + '...' if len(b) > 100 else b for b in bullets]
        return bullets[:5]

    def _extract_keywords(self, text):
        """원문에서 키워드 추출"""
        import re
        from collections import Counter

        words = re.findall(r'\b\w+\b', text.lower())

        stop_words = set([
            'the', 'a', 'an', 'and', 'or', 'but', 'in', 'on', 'at', 'to', 'for',
            'is', 'are', 'was', 'were', 'be', 'been', 'being', 'have', 'has', 'had',
            'do', 'does', 'did', 'will', 'would', 'could', 'should', 'may', 'might',
            'this', 'that', 'these', 'those', 'it', 'its', 'of', 'with', 'as', 'by',
            '은', '는', '이', '가', '을', '를', '의', '에', '와', '과', '도', '로',
            '있다', '없다', '하다', '되다', '이다', '있는', '하는', '된', '할', '한'
        ])

        words = [w for w in words if w not in stop_words and len(w) > 2]
        freq = Counter(words)
        return [word for word, count in freq.most_common(10)]


def check_ai_available():
    """AI 요약 기능 사용 가능 여부 확인"""
    try:
        _get_pipeline()
        return True
    except ImportError:
        return False
