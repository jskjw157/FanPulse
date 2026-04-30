import torch
from transformers import AutoTokenizer, AutoModelForCausalLM

from api.core.gpu import get_gpu_vram_gb

_model = {}

# ---------------------------
# LLM 로딩
# ---------------------------
def _get_llm_model(model_name: str):
    from transformers import BitsAndBytesConfig

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


def _get_llm_model_cpu(model_name: str):
    tokenizer = AutoTokenizer.from_pretrained(model_name)

    model = AutoModelForCausalLM.from_pretrained(
        model_name,
        device_map="cpu",
        dtype=torch.float32
    )

    model.eval()
    return tokenizer, model


def get_model():
    global _model
    if _model:
        return _model

    if get_gpu_vram_gb():
        try:
            tokenizer, model = _get_llm_model("mistralai/Mistral-7B-Instruct-v0.3")
        except Exception:
            tokenizer, model = _get_llm_model("Qwen/Qwen2.5-3B-Instruct")
    else:
        tokenizer, model = _get_llm_model_cpu("Qwen/Qwen3.5-0.8B")

    _model = {"tokenizer": tokenizer, "model": model}
    return _model