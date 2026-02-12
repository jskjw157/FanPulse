"""
AI 기반 댓글 필터링 서비스 (LLM)

- HuggingFace Transformers 사용
- 한국어 LLM:
  - mistralai/Mistral-7B-Instruct-v0.3 (우선)
  - Qwen/Qwen2.5-3B-Instruct (fallback)
"""

import torch
import logging

logger = logging.getLogger(__name__)

_models = {}
_filter_service = None


# ---------------------------
# GPU VRAM
# ---------------------------
def get_gpu_vram_gb():
    if not torch.cuda.is_available():
        return 0
    props = torch.cuda.get_device_properties(0)
    return round(props.total_memory / (1024 ** 3), 2)


# ---------------------------
# LLM 로딩
# ---------------------------
def _get_llm_model(model_name: str):
    from transformers import (
        AutoTokenizer,
        AutoModelForCausalLM,
        BitsAndBytesConfig,
    )

    bnb_config = BitsAndBytesConfig(
        load_in_4bit=True,
        bnb_4bit_quant_type="nf4",
        bnb_4bit_use_double_quant=True,
        bnb_4bit_compute_dtype=torch.float16,
    )

    tokenizer = AutoTokenizer.from_pretrained(model_name)

    vram_gb = get_gpu_vram_gb()
    max_memory = {0: "7GB", "cpu": "10GB"} if vram_gb >= 8 else {0: "4GB", "cpu": "10GB"}

    model = AutoModelForCausalLM.from_pretrained(
        model_name,
        quantization_config=bnb_config,
        device_map="auto",
        max_memory=max_memory,
    )

    model.eval()
    return tokenizer, model


def _get_model():
    if "llm" in _models:
        return _models["llm"]

    try:
        tokenizer, model = _get_llm_model("mistralai/Mistral-7B-Instruct-v0.3")
    except Exception:
        tokenizer, model = _get_llm_model("Qwen/Qwen2.5-3B-Instruct")

    _models["llm"] = {"tokenizer": tokenizer, "model": model}
    return _models["llm"]


# ---------------------------
# 프롬프트
# ---------------------------
def build_filter_prompt(text: str) -> str:
    return f"""
너는 댓글 필터링 AI다.

아래 댓글이 욕설, 혐오, 위협, 성적 표현 등
플랫폼 정책 위반이면 "BLOCK"
정상이면 "ALLOW"만 출력하라.

설명, 이유, 문장은 절대 출력하지 마라.

댓글:
\"\"\"{text}\"\"\"

출력:
"""


# ---------------------------
# 결과 객체 (views.py 완전 호환)
# ---------------------------
class FilterResult:
    def __init__(
        self,
        is_filtered: bool,
        reason: str | None = None,
        rule_id: str | None = None,
        rule_name: str | None = None,
        filter_type: str | None = None,
        matched_pattern: str | None = None,
    ):
        self.is_filtered = is_filtered

        # action 규칙 (views 기대 형태)
        self.action = "BLOCK" if is_filtered else None

        self.rule_id = rule_id
        self.rule_name = rule_name
        self.filter_type = filter_type
        self.matched_pattern = matched_pattern
        self.reason = reason




# ---------------------------
# 서비스
# ---------------------------
class CommentFilterService:
    def __init__(self):
        self._bundle = None

    def _ensure_model(self):
        if self._bundle is None:
            self._bundle = _get_model()  # LLM bundle(dict) 반환
        return self._bundle

    def filter_comment(self, text: str) -> FilterResult:
        bundle = self._ensure_model()
        tokenizer = bundle["tokenizer"]
        model = bundle["model"]

        prompt = build_filter_prompt(text)
        inputs = tokenizer(prompt, return_tensors="pt").to(model.device)

        with torch.no_grad():
            outputs = model.generate(
                **inputs,
                max_new_tokens=64,
                do_sample=False,
                temperature=0.0,
                pad_token_id=tokenizer.eos_token_id,
            )

        raw = tokenizer.decode(
            outputs[0][inputs["input_ids"].shape[-1]:],
            skip_special_tokens=True,
        ).strip().upper()

        if raw.startswith("BLOCK"):
            return FilterResult(
                is_filtered=True,
                reason="LLM 판단: 부적절한 표현",
                rule_id="LLM_001",
                rule_name="LLM Toxicity Filter",
                filter_type="LLM",
            )

        return FilterResult(
            is_filtered=False,
            filter_type="LLM",
        )




# ---------------------------
# 외부 진입점
# ---------------------------
def get_filter_service() -> CommentFilterService:
    global _filter_service
    if _filter_service is None:
        _filter_service = CommentFilterService()
    return _filter_service
