from threading import Lock
import torch
from transformers import AutoTokenizer, AutoModelForCausalLM

from api.core.runtime import env_check, get_gpu_vram_gb

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
        max_memory=max_memory
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


def load_model():
    if get_gpu_vram_gb():
        try:
            return _get_llm_model("mistralai/Mistral-7B-Instruct-v0.3")
        except Exception:
            return _get_llm_model("Qwen/Qwen2.5-3B-Instruct")
    return _get_llm_model_cpu("Qwen/Qwen3.5-0.8B")


class LLM:
    def __init__(self):
        self.tokenizer, self.model = load_model()

    def generate(self, prompt, max_new_tokens=64, do_sample=False, repetition_penalty=1.0):
        inputs = self.tokenizer(prompt, return_tensors="pt").to(self.model.device)
        with torch.no_grad():
            outputs = self.model.generate(
                **inputs,
                max_new_tokens=max_new_tokens,
                do_sample=do_sample,
                pad_token_id=self.tokenizer.eos_token_id, repetition_penalty=repetition_penalty
            )
        return self.tokenizer.decode(
            outputs[0][inputs["input_ids"].shape[-1]:],
            skip_special_tokens=True,
        ).strip()


_llm = None
_lock = Lock()

def get_llm():
    global _llm

    if _llm is not None:
        return _llm
    
    env_check()

    with _lock:
        if _llm is not None:
            return _llm

        _llm = LLM()
        return _llm